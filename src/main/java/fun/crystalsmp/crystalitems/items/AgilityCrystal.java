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
        super("agility_crystal", 3); // cooldown 3s
    }

    @Override
    public void applyPassive(Player player) {
        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        120,
                        2,
                        false,
                        false,
                        false
                )
        );
    }

    @Override
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // Annule le vol vanilla
        event.setCancelled(true);
        player.setFlying(false);

        // Cooldown
        if (isCooldownItem(player)) return;

        // Propulsion
        Vector direction = player.getLocation().getDirection();
        Vector launch = direction.multiply(1.5).setY(0.8);
        player.setVelocity(launch);

        // Effets son & particules
        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_GENERIC_EXPLODE,
                0.8f,
                1.5f
        );

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_WIND_CHARGE_WIND_BURST,
                1.0f,
                1.0f
        );

        player.getWorld().spawnParticle(
                Particle.EXPLOSION,
                player.getLocation(),
                3,
                0.1, 0.1, 0.1,
                0.1
        );

        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation(),
                15,
                0.5, 0.5, 0.5,
                0.1
        );

        // Sécurité chute
        player.setFallDistance(0);
    }
}
