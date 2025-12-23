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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
            for (Player player : Bukkit.getOnlinePlayers()) {
                scanForPassives(player);
            }
        }, 0L, 10L);
    }

    // --- LOGIQUE DE VÉRIFICATION SOUL CRYSTAL ---
    
    private boolean isSoulVictim(Player player) {
        for (CustomItem item : manager.getItems().values()) {
            if (item instanceof SoulCrystal soul && soul.isVictim(player.getUniqueId())) return true;
        }
        return false;
    }

    private boolean isSoulCaster(Player player) {
        for (CustomItem item : manager.getItems().values()) {
            if (item instanceof SoulCrystal soul && soul.isCaster(player.getUniqueId())) return true;
        }
        return false;
    }

    // --- ÉVÉNEMENTS DE BLOCAGE ET SÉCURITÉ ---

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Bloquer uniquement la VICTIME. Le CASTER peut interagir/poser des blocs.
        if (isSoulVictim(player)) {
            event.setCancelled(true);
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) return;

        manager.getCustomItemByItemStack(item).ifPresent(customItem -> {
            if (event.getAction().name().contains("RIGHT")) {
                customItem.onRightClick(event);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            // GOD MODE : Le lanceur est invincible pendant la possession
            if (isSoulCaster(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            // Seule la victime est empêchée de frapper
            if (isSoulVictim(player)) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        // Seule la victime ne peut pas casser de blocs
        if (isSoulVictim(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        // Seule la victime ne peut pas poser de blocs
        if (isSoulVictim(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        // La victime ne peut rien jeter
        if (isSoulVictim(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (isSoulVictim(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cVotre âme est possédée...");
        }
    }

    // --- LOGIQUE PASSIVE ET MOUVEMENTS ---

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        // On scanne l'inventaire pour les effets passifs (Double Saut, Morph, etc.)
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(customItem -> {
                if (customItem instanceof AgilityCrystal && player.isOnGround()) {
                    if (!player.getAllowFlight()) player.setAllowFlight(true);
                }
                if (customItem instanceof MorphCrystal morph) {
                    morph.checkMovement(player);
                }
            });
        }

        // Spider Crystal (Doit être tenu en main)
        ItemStack hand = player.getInventory().getItemInMainHand();
        manager.getCustomItemByItemStack(hand).ifPresent(customItem -> {
            if (customItem instanceof SpiderCrystal spider) {
                spider.handleWallClimbing(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(customItem -> {
                if (customItem instanceof AgilityCrystal agility) {
                    agility.onDoubleJump(event);
                }
            });
            if (event.isCancelled()) return;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        handleCleanup(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        handleCleanup(event.getEntity());
    }

    private void handleCleanup(Player player) {
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(customItem -> {
                if (customItem instanceof MorphCrystal morph) {
                    morph.removeMorph(player);
                }
            });
        }
    }

    private void scanForPassives(Player player) {
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(customItem -> {
                customItem.applyPassive(player);
            });
        }
    }
}