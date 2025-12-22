package fun.crystalsmp.crystalitems.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class SpiderCrystal extends CustomItem {

    public SpiderCrystal() {
        super("spider_crystal", 100);
    }

    @Override
    public void applyPassive(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, false, false, false));
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isCooldownItem(player)) return;

        event.setCancelled(true);
        boolean found = false;

        // On boucle sur les entités proches
        for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
            if (entity instanceof Player target && !target.equals(player)) {
                found = true;
                Location loc = target.getLocation();
                
                // Logique du cube 3x3x3 directement ici
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 2; y++) {
                        for (int z = -1; z <= 1; z++) {
                            Block b = loc.clone().add(x, y, z).getBlock();
                            if (b.getType() == Material.AIR) {
                                b.setType(Material.COBWEB);
                            }
                        }
                    }
                }
                target.playSound(loc, Sound.ENTITY_SPIDER_DEATH, 1.0f, 0.5f);
            }
        }
        if (found) player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0f, 1.0f);
    }

    public void handleWallClimbing(Player player) {
        if (!player.isSneaking()) return;

        // Calcul de la direction face au joueur
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) yaw += 360;
        yaw %= 360;
        BlockFace face = (yaw <= 45 || yaw >= 315) ? BlockFace.SOUTH : 
                         (yaw <= 135) ? BlockFace.WEST : 
                         (yaw <= 225) ? BlockFace.NORTH : BlockFace.EAST;

        Block block = player.getLocation().getBlock().getRelative(face);

        if (block.getType().isSolid()) {
            player.setVelocity(new Vector(player.getVelocity().getX(), 0.25, player.getVelocity().getZ()));
            player.setFallDistance(0);
        }
    }
}