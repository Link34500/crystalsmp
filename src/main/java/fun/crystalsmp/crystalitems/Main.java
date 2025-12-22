package fun.crystalsmp.crystalitems;

import org.bukkit.plugin.java.JavaPlugin;
import fun.crystalsmp.crystalitems.events.ItemDispatcher;
import fun.crystalsmp.crystalitems.items.AgilityCrystal;
import fun.crystalsmp.crystalitems.items.ChaosCrystal;
import fun.crystalsmp.crystalitems.items.SpiderCrystal;
import fun.crystalsmp.crystalitems.items.ToxinCrystal;
import fun.crystalsmp.crystalitems.managers.ItemManager;

public class Main extends JavaPlugin {
    private ItemManager itemManager;

    @Override
    public void onEnable() {
        // 1. Initialisation du Manager
        this.itemManager = new ItemManager();

        // 2. Enregistrement des items
        // Tu peux en ajouter autant que tu veux ici
        itemManager.registerItem(new AgilityCrystal());
        itemManager.registerItem(new ToxinCrystal());
        itemManager.registerItem(new SpiderCrystal());
        itemManager.registerItem(new ChaosCrystal());

        // 3. Initialisation du Dispatcher
        // On lui passe le manager et l'instance du plugin (pour les tâches/schedulers)
        ItemDispatcher dispatcher = new ItemDispatcher(itemManager, this);

        // 4. Enregistrement unique des événements via le Dispatcher
        getServer().getPluginManager().registerEvents(dispatcher, this);

        getLogger().info("CrystalItems has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CrystalItems has been disabled.");
    }
}