package fun.crystalsmp.crystalitems.items;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class CustomItem {
    private final String itemAdderId;
    private final int cooldownSeconds; 
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public CustomItem(String id, int cooldownSeconds) {
        this.itemAdderId = "crystalsmp:" + id;
        this.cooldownSeconds = cooldownSeconds;
    }

    public CustomItem(String id) {
        this(id, 0);
    }

    public String getItemAdderId() {
        return itemAdderId;
    }

    protected boolean isCooldownItem(Player player) {
        if (cooldownSeconds <= 0) return false;

        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        if (cooldowns.containsKey(uuid)) {
            long expiration = cooldowns.get(uuid);
            
            if (now < expiration) {
                // Calcul du temps restant (arrondi au supérieur pour plus de clarté)
                long timeLeft = (expiration - now) / 1000;
                if (timeLeft <= 0) timeLeft = 1;

                player.sendMessage("§cYou must wait " + timeLeft + " more seconds to use this item again.");
                return true; // Bloqué : le cooldown est encore actif
            }
        }

        // Si on arrive ici, soit c'est la 1ère fois, soit le cooldown est passé.
        // On enregistre le nouveau cooldown et on retourne FALSE (non bloqué).
        cooldowns.put(uuid, now + (cooldownSeconds * 1000L));
        return false; 
    }

    // --- Méthodes à surcharger ---
    public void onRightClick(PlayerInteractEvent event) {}
    public void onInteract(PlayerInteractEvent event) {}
    public void onDoubleJump(PlayerToggleFlightEvent event) {}
    public void applyPassive(Player player) {}
}