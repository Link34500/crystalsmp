package fun.crystalsmp.crystalitems.items;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class CustomItem {
  private final String itemAdderId;

  public CustomItem(String itemAdderId) {
    this.itemAdderId = "crstalsmp:" + itemAdderId;
  }

  public String getItemAdderId() {
    return itemAdderId;
  }

  // Liste des évenmments :
  public void onRightClick(PlayerInteractEvent event) {}

  protected void consumeAll(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    ItemStack item = event.getItem();
    player.getInventory().remove(item);
  }

  

}