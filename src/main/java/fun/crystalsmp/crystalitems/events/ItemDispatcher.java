package fun.crystalsmp.crystalitems.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fun.crystalsmp.crystalitems.managers.ItemManager;

public class ItemDispatcher implements Listener {
  private final ItemManager manager;
  
  public ItemDispatcher(ItemManager manager) {
    this.manager = manager;
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    ItemStack item = event.getItem();
    if (item == null) return;

    manager.getCustomItemByItemStack(item).ifPresent(customStack -> {
      if (event.getAction().name().contains("RIGHT")) {
        customStack.onRightClick(event);
        
      }
    });
  }
}
