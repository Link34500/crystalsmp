package fun.crystalsmp.crystalitems.hooks;

import fun.crystalsmp.crystalitems.managers.CrystalChestManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class CrystalExpansion extends PlaceholderExpansion {
    private final CrystalChestManager chestManager;

    public CrystalExpansion(CrystalChestManager chestManager) {
        this.chestManager = chestManager;
    }

    @Override
    public @NotNull String getIdentifier() { return "crystalsmp"; }

    @Override
    public @NotNull String getAuthor() { return "Gemini,Fishink"; }

    @Override
    public @NotNull String getVersion() { return "1.0"; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("timer")) {
            return chestManager.getTimeRemaining();
        }
        return null;
    }
}