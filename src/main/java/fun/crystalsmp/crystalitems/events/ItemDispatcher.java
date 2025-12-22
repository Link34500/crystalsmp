package fun.crystalsmp.crystalitems.events;

import fun.crystalsmp.crystalitems.items.SpiderCrystal; // Importation nécessaire
import fun.crystalsmp.crystalitems.managers.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemDispatcher implements Listener {
    private final ItemManager manager;

    public ItemDispatcher(ItemManager manager, JavaPlugin plugin) {
        this.manager = manager;

        // Tâche pour les effets passifs (toutes les secondes / 20 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                scanForPassives(player);
            }
        }, 0L, 20L);
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

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(customItem -> {
                customItem.onDoubleJump(event);
            });
            if (event.isCancelled()) return;
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // --- 1. RESET DU SAUT ---
        if (((Entity) player).isOnGround() && !player.getAllowFlight()) {
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                for (ItemStack is : player.getInventory().getContents()) {
                    if (is == null) continue;
                    if (manager.getCustomItemByItemStack(is).isPresent()) {
                        player.setAllowFlight(true);
                        break;
                    }
                }
            }
        }

        // --- 2. LOGIQUE SPÉCIFIQUE SPIDER (Grimper aux murs) ---
        // On vérifie si le joueur possède le SpiderCrystal dans son inventaire
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            manager.getCustomItemByItemStack(is).ifPresent(customItem -> {
                if (customItem instanceof SpiderCrystal spider) {
                    spider.handleWallClimbing(player);
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