package fun.crystalsmp.crystalitems.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.inventory.ItemStack;

import dev.lone.itemsadder.api.CustomStack;
import fun.crystalsmp.crystalitems.items.CustomItem;

public class ItemManager {
  private final Map<String, CustomItem> items = new HashMap<>();

  public void registerItem(CustomItem item) {
    items.put(item.getItemAdderId(), item);
  }

  // Ajoute ça dans ton ItemManager.java
  public Map<String, CustomItem> getItems() {
    return items;
  }
  
  public Optional<CustomItem> getCustomItemByItemStack(ItemStack item) {
    CustomStack customItem = CustomStack.byItemStack(item);
    if (customItem == null) return Optional.empty();

    return Optional.ofNullable(items.get(customItem.getNamespacedID()));
  }
  public CustomItem getRandomItem() {
    Object[] values = items.values().toArray();
    return (CustomItem) values[new java.util.Random().nextInt(values.length)];
}
}
