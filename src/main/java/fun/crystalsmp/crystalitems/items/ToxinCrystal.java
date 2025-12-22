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
        // Cooldown de 10 secondes pour l'activation
        super("toxin_crystal", 50);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Empêche de poser le bloc
        event.setCancelled(true);

        // Vérifie si le cristal est prêt
        if (isCooldownItem(player)) return;

        // Application de l'effet aux joueurs proches (15 blocs)
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 15, 15, 15)) {
            if (entity instanceof Player target && !target.getUniqueId().equals(player.getUniqueId())) {
                
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0));
                target.playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 2.0f);
            }
        }

        // Feedback sonore pour l'utilisateur
        player.playSound(player.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1.0f, 0.5f);

        // Consommation de l'item
        if (item != null) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                item.setAmount(0);
            }
        }
    }
}