package fun.crystalsmp.crystalitems.items;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class MorphCrystal extends CustomItem {

    private final HashMap<UUID, FallingBlock> activeMorphs = new HashMap<>();
    private final HashMap<UUID, Location> startLocations = new HashMap<>();

    public MorphCrystal() {
        super("morph_crystal", 0);
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
            Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType().isAir()) return;

            // --- FIX CENTRAGE PARFAIT ---
            // On prend le centre exact du bloc où se trouve le joueur
            Location gridLoc = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
            startLocations.put(uuid, player.getLocation().getBlock().getLocation());

            // --- FIX BIOME (FallingBlock) ---
            // Un FallingBlock est considéré comme un vrai bloc et prend la couleur du biome !
            FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(gridLoc, target.getBlockData());
            
            // On empêche le bloc de tomber et de disparaître
            fallingBlock.setGravity(false);
            fallingBlock.setInvulnerable(true);
            fallingBlock.setDropItem(false);
            fallingBlock.setTicksLived(1); 
            
            // Note: Les FallingBlocks ne tournent pas facilement vers N/S/E/W, 
            // mais ils sont 100% alignés sur la grille et ont la bonne couleur.

            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 1, false, false));
            activeMorphs.put(uuid, fallingBlock);

            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
            player.sendMessage("§dMorphed into §f" + target.getType().name() + "§d!");
        }
    }

    public void checkMovement(Player player) {
        UUID uuid = player.getUniqueId();
        if (!activeMorphs.containsKey(uuid)) return;

        Location current = player.getLocation().getBlock().getLocation();
        Location start = startLocations.get(uuid);

        // Si le joueur quitte son bloc de 1x1
        if (current.getBlockX() != start.getBlockX() || current.getBlockZ() != start.getBlockZ()) {
            removeMorph(player);
            player.sendMessage("§cDisguise broken!");
        } else {
            // Empêcher le FallingBlock de despawn (il despawn après 30s normalement)
            FallingBlock fb = activeMorphs.get(uuid);
            if (fb != null) fb.setTicksLived(1);
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