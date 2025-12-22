package fun.crystalsmp.crystalitems.items;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChaosCrystal extends CustomItem {

    public ChaosCrystal() {
        // Cooldown of 45 seconds
        super("chaos_crystal", 45);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (isCooldownItem(player)) return;

        int affectedCount = 0;

        // Search for players in a 12-block radius
        for (Entity entity : player.getNearbyEntities(12, 12, 12)) {
            if (entity instanceof Player target && !target.equals(player)) {
                
                shuffleInventory(target);
                
                // Effects to increase confusion
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0)); 
                target.playSound(target.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1f, 0.5f);
                target.sendMessage("§5§k!!!§r §dYour inventory has been plunged into CHAOS! §5§k!!!§r");
                
                affectedCount++;
            }
        }

        if (affectedCount > 0) {
            player.sendMessage("§dChaos has been unleashed upon " + affectedCount + " players!");
            player.playSound(player.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 1f, 1f);
            
            // Consommation de l'item (facultatif, à décommenter si besoin)
            // consumeOne(player);
        } else {
            player.sendMessage("§cNo one in range to experience the chaos.");
        }
    }

    /**
     * Shuffles ONLY the storage slots (0-35). Armor and Offhand are safe.
     */
    private void shuffleInventory(Player target) {
        // On récupère uniquement le contenu de stockage (les 36 slots principaux)
        ItemStack[] contents = target.getInventory().getStorageContents();
        List<ItemStack> itemList = new ArrayList<>();

        // On extrait les items existants
        for (ItemStack is : contents) {
            if (is != null) {
                itemList.add(is.clone());
            }
        }

        // On vide uniquement les slots de stockage (cela laisse l'armure intacte)
        target.getInventory().setStorageContents(new ItemStack[contents.length]);

        // On mélange la liste
        Collections.shuffle(itemList);

        // On remet les items dans l'inventaire
        for (ItemStack is : itemList) {
            target.getInventory().addItem(is);
        }
        
        // Mise à jour visuelle pour éviter les items "fantômes"
        target.updateInventory();
    }
}