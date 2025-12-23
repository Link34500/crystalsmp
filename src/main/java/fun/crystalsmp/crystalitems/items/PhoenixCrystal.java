package fun.crystalsmp.crystalitems.items;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector; // Import manquant ajouté

public class PhoenixCrystal extends CustomItem {

    public PhoenixCrystal() {
        super("phoenix_crystal", 0);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isCooldownItem(player)) return;

        Location loc = player.getLocation().add(0, 1, 0); 
        double radius = 5.0;

        // 1. Création de la sphère de particules
        createParticleSphere(loc, radius);

        // 2. SONS COMPOSÉS (Pour un effet Phoenix réaliste)
        // Son de souffle de feu puissant
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.5f); 
        // Son d'explosion sourde
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.2f);
        // Son de feu qui crépite
        loc.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1f, 0.5f);
        // Son de cloche/magie pour le côté divin
        loc.getWorld().playSound(loc, Sound.BLOCK_BELL_RESONATE, 1f, 2f);

        // Éclatement de flammes massif (800 particules, large zone, vitesse rapide)
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 800, 2.5, 1.5, 2.5, 0.2);

        // Onde de choc de fumée
        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 100, 1.0, 0.5, 1.0, 0.05);

        // Étincelles de lave qui volent loin
        loc.getWorld().spawnParticle(Particle.LAVA, loc, 50, 4.0, 2.0, 4.0, 0.5);

        // 4. DÉGÂTS ET KNOCKBACK
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !entity.equals(player)) {
                // Dégâts
                target.damage(20.0, player);
                // Feu (5 secondes)
                target.setFireTicks(100);
                
                // Knockback calculé manuellement vers l'extérieur
                Vector velocity = target.getLocation().toVector().subtract(loc.toVector());
                if (velocity.length() > 0) {
                    velocity.normalize().multiply(1.5).setY(0.5); // On les propulse aussi un peu en l'air
                    target.setVelocity(velocity);
                }
            }
        }

        player.sendMessage("§6§lPHOENIX BURST! §eYou unleashed the fire within.");
    }

    private void createParticleSphere(Location center, double radius) {
        for (double i = 0; i <= Math.PI; i += Math.PI / 10) {
            double r = Math.sin(i) * radius;
            double y = Math.cos(i) * radius;
            for (double j = 0; j < Math.PI * 2; j += Math.PI / 10) {
                double x = Math.cos(j) * r;
                double z = Math.sin(j) * r;
                center.add(x, y, z);
                center.getWorld().spawnParticle(Particle.FLAME, center, 1, 0, 0, 0, 0);
                center.subtract(x, y, z);
            }
        }
    }
}