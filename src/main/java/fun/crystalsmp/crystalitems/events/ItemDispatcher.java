package fun.crystalsmp.crystalitems.events;

import fun.crystalsmp.crystalitems.items.*;
import fun.crystalsmp.crystalitems.managers.ItemManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemDispatcher implements Listener {
    private final ItemManager manager;

    public ItemDispatcher(ItemManager manager, JavaPlugin plugin) {
        this.manager = manager;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) scanForPassives(player);
        }, 0L, 10L);
    }

    private boolean isSoulVictim(Player player) {
        return manager.getItems().values().stream()
                .anyMatch(i -> i instanceof SoulCrystal soul && soul.isVictim(player.getUniqueId()));
    }

    private boolean isSoulCaster(Player player) {
        return manager.getItems().values().stream()
                .anyMatch(i -> i instanceof SoulCrystal soul && soul.isCaster(player.getUniqueId()));
    }

    // --- TRANSFERT DE DÉGÂTS (HITBOX) ---
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (isSoulCaster(player)) {
            event.setCancelled(true);
            for (CustomItem item : manager.getItems().values()) {
                if (item instanceof SoulCrystal soul) {
                    UUID victimUUID = soul.getVictimByCaster(player.getUniqueId());
                    if (victimUUID != null) {
                        Player victim = Bukkit.getPlayer(victimUUID);
                        if (victim != null) {
                            victim.damage(event.getDamage());
                            victim.playHurtAnimation(0);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (isSoulVictim(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) return;
        manager.getCustomItemByItemStack(item).ifPresent(ci -> {
            if (event.getAction().name().contains("RIGHT")) ci.onRightClick(event);
        });
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        boolean hasAgility = false;

        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            CustomItem ci = manager.getCustomItemByItemStack(is).orElse(null);
            
            if (ci instanceof AgilityCrystal) hasAgility = true; // On détecte la pierre
            if (ci instanceof MorphCrystal morph) morph.checkMovement(player);
        }

        // --- GESTION DU DOUBLE SAUT ---
        if (hasAgility) {
            // Si le joueur est au sol, on lui redonne l'autorisation de double-cliquer sur ESPACE
            if (player.isOnGround()) {
                player.setAllowFlight(true);
            }
        } else {
            // Sécurité : si on lâche l'item, on perd le droit de voler
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }

        // Spider Crystal
        ItemStack hand = player.getInventory().getItemInMainHand();
        manager.getCustomItemByItemStack(hand).ifPresent(ci -> {
            if (ci instanceof SpiderCrystal spider) spider.handleWallClimbing(player);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(ci -> {
                if (ci instanceof AgilityCrystal agility) agility.onDoubleJump(event);
            });
            if (event.isCancelled()) return;
        }
    }

    private void scanForPassives(Player player) {
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(ci -> ci.applyPassive(player));
        }
    }
}