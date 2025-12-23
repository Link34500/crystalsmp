package fun.crystalsmp.crystalitems.managers;

import dev.lone.itemsadder.api.CustomStack;
import fun.crystalsmp.crystalitems.items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class CrystalChestManager {
    private final JavaPlugin plugin;
    private final ItemManager itemManager;
    private final Random random = new Random();
    private int timeLeft;
    private final int SPAWN_INTERVAL = 2 * 60;
    private final int BORDER_SIZE = 10000;

    public CrystalChestManager(JavaPlugin plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.timeLeft = SPAWN_INTERVAL;
        startTimer();
    }

    private void startTimer() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (timeLeft > 0) {
                timeLeft--;
            } else {
                spawnCrystalChest();
                timeLeft = SPAWN_INTERVAL;
            }
        }, 0L, 20L);
    }

    public void spawnCrystalChest() {
        World world = Bukkit.getWorld("world");
        if (world == null) return;

        // Génération de coordonnées aléatoires dans la limite de 10k
        int x = random.nextInt(BORDER_SIZE * 2) - BORDER_SIZE;
        int z = random.nextInt(BORDER_SIZE * 2) - BORDER_SIZE;
        int y = world.getHighestBlockYAt(x, z) + 1;

        Location loc = new Location(world, x, y, z);
        loc.getBlock().setType(Material.CHEST);

        if (loc.getBlock().getState() instanceof Chest chest) {
            CustomItem randomItem = itemManager.getRandomItem();
            CustomStack stack = CustomStack.getInstance(randomItem.getItemAdderId());

            if (stack != null) {
                chest.getInventory().setItem(13, stack.getItemStack());
                
                // Création du NameTag (Hologramme via ArmorStand invisible)
                createHologram(loc.clone().add(0.5, 1.2, 0.5), stack.getDisplayName());

                Bukkit.broadcastMessage("§6§l[LARGAGE] §eUn coffre contenant un " + stack.getDisplayName() + " §ea spawn en: §f" + x + ", " + y + ", " + z);
            }
        }
    }

    private void createHologram(Location loc, String name) {
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setGravity(false);
        as.setCustomName(name);
        as.setCustomNameVisible(true);
        as.setMarker(true); // Empêche toute collision
    }

    public String getTimeRemaining() {
        return String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
    }
}