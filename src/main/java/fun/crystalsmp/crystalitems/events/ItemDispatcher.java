package fun.crystalsmp.crystalitems.events;

import fun.crystalsmp.crystalitems.items.AgilityCrystal;
import fun.crystalsmp.crystalitems.items.MorphCrystal;
import fun.crystalsmp.crystalitems.items.SpiderCrystal;
import fun.crystalsmp.crystalitems.managers.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
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

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        manager.getCustomItemByItemStack(item).ifPresent(customItem -> {
            if (event.getAction().name().contains("RIGHT")) {
                customItem.onRightClick(event);
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
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        // On parcourt l'inventaire pour gérer les cristaux passifs ou actifs (Agility et Morph)
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;

            manager.getCustomItemByItemStack(is).ifPresent(customItem -> {
                
                // 1. GESTION AGILITY (Reset au sol)
                if (customItem instanceof AgilityCrystal && ((org.bukkit.entity.Entity) player).isOnGround()) {
                    if (!player.getAllowFlight()) player.setAllowFlight(true);
                }

                // 2. GESTION MORPH (Démorph si le joueur quitte son bloc)
                if (customItem instanceof MorphCrystal morph) {
                    morph.checkMovement(player);
                }
            });
        }

        // 3. GESTION SPIDER (Main en main)
        ItemStack hand = player.getInventory().getItemInMainHand();
        manager.getCustomItemByItemStack(hand).ifPresent(customItem -> {
            if (customItem instanceof SpiderCrystal spider) {
                spider.handleWallClimbing(player);
            }
        });
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