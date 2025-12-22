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
                // Calcul du temps restant en secondes
                long timeLeft = (expiration - now) / 1000;
                // Si le temps est inférieur à 1s mais pas encore expiré, on affiche au moins 1s
                if (timeLeft == 0) timeLeft = 1;

                // Envoi du message en rouge et en anglais
                player.sendMessage("§cYou must wait " + timeLeft + " more seconds to use this item again.");
                return true; 
            }
        }

        // Si prêt, on enregistre le nouveau cooldown
        cooldowns.put(uuid, now + (cooldownSeconds * 1000L));
        return true;
    }

    // --- Méthodes à surcharger ---
    public void onRightClick(PlayerInteractEvent event) {}
    public void onInteract(PlayerInteractEvent event) {}
    public void onDoubleJump(PlayerToggleFlightEvent event) {}
    public void applyPassive(Player player) {}
}