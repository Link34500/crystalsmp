package fun.crystalsmp.crystalitems.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.UUID;

public class MorphCrystal extends CustomItem {

    private final HashMap<UUID, BlockDisplay> activeMorphs = new HashMap<>();
    private final HashMap<UUID, Location> startLocations = new HashMap<>();

    public MorphCrystal() {
        super("morph_crystal", 60);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (isCooldownItem(player)) return;

        if (activeMorphs.containsKey(uuid)) {
            removeMorph(player);
            player.sendMessage("§aYou returned to your human form.");
        } else {
            // 1. Détection du bloc regardé
            Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType().isAir()) {
                player.sendMessage("§cLook at a block to morph!");
                return;
            }

            // 2. Position parfaitement centrée sur la grille
            Location gridLoc = player.getLocation().getBlock().getLocation();
            startLocations.put(uuid, gridLoc.clone());

            // 3. Création du BlockDisplay
            BlockDisplay display = (BlockDisplay) player.getWorld().spawnEntity(gridLoc, EntityType.BLOCK_DISPLAY);
            
            // On copie le BlockData exact (inclut l'orientation des coffres, dalles, etc.)
            display.setBlock(target.getBlockData());

            // --- FIX : CENTRAGE ET ALIGNEMENT ---
            // On centre le bloc pour qu'il ne dépasse pas
            Transformation transformation = display.getTransformation();
            // Translation de 0 car le spawnEntity est déjà sur le coin du bloc, 
            // le BlockDisplay s'aligne naturellement sur la grille de 1x1x1
            display.setTransformation(transformation);

            // 4. Rotation Cardinale (Horizontale uniquement)
            float yaw = getCardinalYaw(player.getFacing());
            display.setRotation(yaw, 0);

            // 5. Gestion spécifique pour le biome (Coloration)
            // On force le calcul du biome pour les blocs de type GRASS, LEAVES, etc.
            display.setBrightness(new BlockDisplay.Brightness(15, 15)); 

            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 1, false, false));
            activeMorphs.put(uuid, display);

            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1f, 1.2f);
            player.sendMessage("§dMorphed into §f" + target.getType().name().replace("_", " ") + "§d!");
        }
    }

    private float getCardinalYaw(BlockFace face) {
        return switch (face) {
            case NORTH -> 180;
            case SOUTH -> 0;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };
    }

    public void checkMovement(Player player) {
        UUID uuid = player.getUniqueId();
        if (!activeMorphs.containsKey(uuid)) return;

        Location currentLoc = player.getLocation().getBlock().getLocation();
        Location startLoc = startLocations.get(uuid);

        if (currentLoc.getBlockX() != startLoc.getBlockX() || currentLoc.getBlockZ() != startLoc.getBlockZ()) {
            removeMorph(player);
            player.sendMessage("§cMorph broken! You moved.");
        }
    }

    public void removeMorph(Player player) {
        UUID uuid = player.getUniqueId();
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        if (activeMorphs.containsKey(uuid)) {
            activeMorphs.get(uuid).remove();
            activeMorphs.remove(uuid);
            startLocations.remove(uuid);
        }
    }
}