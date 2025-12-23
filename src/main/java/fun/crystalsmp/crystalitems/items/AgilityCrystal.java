package fun.crystalsmp.crystalitems.items;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class AgilityCrystal extends CustomItem {

    public AgilityCrystal() {
        super("agility_crystal", 3); // Cooldown de 3 secondes
    }

    @Override
    public void applyPassive(Player player) {
        // Applique Speed II de manière fluide
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1, false, false, false));
    }

    @Override
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // --- FIX ANTI-GLIDE ---
        // On annule l'event TOUJOURS pour empêcher le mode vol vanilla
        event.setCancelled(true);
        player.setFlying(false);
        player.setAllowFlight(false); // Retire l'autorisation (sera remis par le onMove au sol)

        // Vérification du cooldown
        if (isCooldownItem(player)) return;

        // Propulsion : Vers l'avant (direction du regard) + vers le haut
        Vector direction = player.getLocation().getDirection();
        Vector launch = direction.multiply(1.2).setY(0.7); // Ajusté pour être puissant mais contrôlable
        player.setVelocity(launch);

        // Effets sonores
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.8f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.2f);

        // Particules : Explosion et Nuage pour l'effet de souffle
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 2, 0.1, 0.1, 0.1, 0.05);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.3, 0.3, 0.3, 0.1);

        // Sécurité contre les dégâts de chute
        player.setFallDistance(0);
    }
}