package fun.crystalsmp.crystalitems.items;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ToxinCrystal extends CustomItem {

    public ToxinCrystal() {
        // Cooldown de 50 secondes
        super("toxin_crystal", 60);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isCooldownItem(player)) return;

        int count = 0;
        for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
            if (entity instanceof Player target) {
                if (!target.getUniqueId().equals(player.getUniqueId())) {

                    target.addPotionEffect(
                            new PotionEffect(PotionEffectType.NAUSEA, 300, 8, true, true),
                            true
                    );

                    target.addPotionEffect(
                            new PotionEffect(PotionEffectType.POISON, 120, 1, true, true),
                            true
                    );

                    target.playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 1f);
                    count++;
                }
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1f, 0.5f);

        if (count > 0) {
            player.sendMessage("§aToxin applied to " + count + " players !");
        } else {
            player.sendMessage("§cNo one within range of the toxin.");
        }
    }
}
