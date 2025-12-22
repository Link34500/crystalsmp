package fun.crystalsmp.crystalitems.items;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class AgilityCrystal extends CustomItem {

    public AgilityCrystal() {
        // Cooldown de 2 secondes pour le dash
        super("agility_crystal", 5);
    }

    @Override
    public void applyPassive(Player player) {
        // Effet permanent, pas besoin de check isReady() ici
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false, true));
    }

    @Override
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        // Utilisation du système de cooldown hérité
        if (isCooldownItem(player)) return;

        // Action du Dash
        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        
        Vector dash = player.getLocation().getDirection().multiply(1.5).setY(0.5);
        player.setVelocity(dash);
        
        player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.2f);
    }
}