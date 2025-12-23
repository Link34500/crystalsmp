package fun.crystalsmp.crystalitems;

import org.bukkit.plugin.java.JavaPlugin;
import fun.crystalsmp.crystalitems.events.ItemDispatcher;
import fun.crystalsmp.crystalitems.items.AgilityCrystal;
import fun.crystalsmp.crystalitems.items.ChaosCrystal;
import fun.crystalsmp.crystalitems.items.FlashbangCrystal;
import fun.crystalsmp.crystalitems.items.MorphCrystal;
import fun.crystalsmp.crystalitems.items.PhoenixCrystal;
import fun.crystalsmp.crystalitems.items.SpiderCrystal;
import fun.crystalsmp.crystalitems.items.ToxinCrystal;
import fun.crystalsmp.crystalitems.managers.ItemManager;
import fun.crystalsmp.crystalitems.managers.CrystalChestManager;
import fun.crystalsmp.crystalitems.hooks.CrystalExpansion;
import org.bukkit.Bukkit;

public class Main extends JavaPlugin {
    private ItemManager itemManager;
    private CrystalChestManager chestManager;
    @Override
    public void onEnable() {
        // 1. Initialisation du Manager
        this.itemManager = new ItemManager();
        getCommand("crystalspawn").setExecutor((sender, command, label, args) -> {
    if (!sender.hasPermission("crystalsmp.admin")) {
        sender.sendMessage("§cTu n'as pas la permission !");
        return true;
    }
    chestManager.spawnCrystalChest();
    sender.sendMessage("§a[CrystalSMP] Coffre forcé avec succès !");
    return true;
});
        // 2. Enregistrement des items
        // Tu peux en ajouter autant que tu veux ici
        itemManager.registerItem(new AgilityCrystal());
        itemManager.registerItem(new ToxinCrystal());
        itemManager.registerItem(new SpiderCrystal());
        itemManager.registerItem(new ChaosCrystal());
        itemManager.registerItem(new FlashbangCrystal(this));
        itemManager.registerItem(new MorphCrystal());
        itemManager.registerItem(new PhoenixCrystal());

        // Initialisation du système de coffre
        this.chestManager = new CrystalChestManager(this, itemManager);

        // Enregistrement de PlaceholderAPI si présent
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CrystalExpansion(chestManager).register();
        }

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