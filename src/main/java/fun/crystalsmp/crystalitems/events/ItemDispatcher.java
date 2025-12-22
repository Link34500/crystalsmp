package fun.crystalsmp.crystalitems.events;

import fun.crystalsmp.crystalitems.items.AgilityCrystal;
import fun.crystalsmp.crystalitems.items.SpiderCrystal;
import fun.crystalsmp.crystalitems.managers.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

        // Scan des passifs + mise à jour visuelle (brillance)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                scanForPassives(player);
            }
        }, 0L, 10L); // Toutes les 0.5s pour plus de réactivité
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

    @EventHandler(priority = EventPriority.LOWEST )
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
        
        // --- 1. SÉCURITÉ GAME MODE ---
        // Si le joueur est en créatif ou spectateur, on ne touche à rien pour ne pas casser le vol vanilla
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // --- 2. GESTION DU DOUBLE SAUT (Reset au sol) ---
        // On vérifie si le joueur est au sol pour lui redonner son saut
        if (((org.bukkit.entity.Entity) player).isOnGround()) {
            boolean hasAgility = false;
            
            // Scan rapide pour voir s'il a le cristal d'Agilité
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && manager.getCustomItemByItemStack(is).orElse(null) instanceof AgilityCrystal) {
                    hasAgility = true;
                    break;
                }
            }

            // CORRECTIF : On force le setAllowFlight à true seulement si nécessaire
            // Cela réactive le cristal même après être sorti du mode Créatif
            if (hasAgility) {
                if (!player.getAllowFlight()) {
                    player.setAllowFlight(true);
                }
            } else {
                // Si le joueur n'a plus le cristal, on lui retire le droit de voler
                if (player.getAllowFlight()) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }
        }

    // --- 3. GESTION SPIDER (Grimpe) ---
    ItemStack hand = player.getInventory().getItemInMainHand();
    manager.getCustomItemByItemStack(hand).ifPresent(customItem -> {
        if (customItem instanceof SpiderCrystal spider) {
            spider.handleWallClimbing(player);
        }
    });
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