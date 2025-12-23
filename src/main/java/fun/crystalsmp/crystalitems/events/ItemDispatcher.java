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
import org.bukkit.event.entity.PlayerDeathEvent;
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

    // --- HITBOX & DAMAGE TRANSFER ---
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Si on tape le lanceur, on transfère à la victime
        if (isSoulCaster(player)) {
            event.setCancelled(true); // Le lanceur ne prend rien
            
            for (CustomItem item : manager.getItems().values()) {
                if (item instanceof SoulCrystal soul) {
                    UUID victimUUID = soul.getVictimByCaster(player.getUniqueId());
                    if (victimUUID != null) {
                        Player victim = Bukkit.getPlayer(victimUUID);
                        if (victim != null) {
                            victim.damage(event.getDamage());
                            victim.playHurtAnimation(0); // Effet visuel
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
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(ci -> {
                if (ci instanceof AgilityCrystal && player.isOnGround()) player.setAllowFlight(true);
                if (ci instanceof MorphCrystal morph) morph.checkMovement(player);
            });
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        handleCleanup(event.getEntity());
    }

    private void handleCleanup(Player player) {
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(ci -> {
                if (ci instanceof MorphCrystal morph) morph.removeMorph(player);
            });
        }
    }

    private void scanForPassives(Player player) {
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(ci -> ci.applyPassive(player));
        }
    }
}