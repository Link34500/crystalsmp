package fun.crystalsmp.crystalitems.items;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.UUID;

public class SoulCrystal extends CustomItem {

    private final JavaPlugin plugin;
    private final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final HashMap<UUID, Location> savedLocations = new HashMap<>();
    private final HashMap<UUID, UUID> activePossessions = new HashMap<>(); // Caster -> Target

    public SoulCrystal(JavaPlugin plugin) {
        super("soul_crystal", 120);
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        Player caster = event.getPlayer();
        if (isCooldownItem(caster)) return;

        Player target = caster.getNearbyEntities(20, 20, 20).stream()
                .filter(e -> e instanceof Player && !e.equals(caster))
                .map(e -> (Player) e).findFirst().orElse(null);

        if (target == null) {
            caster.sendMessage("§cAucune âme à proximité.");
            return;
        }

        UUID casterUUID = caster.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        // Sauvegarde
        savedInventories.put(casterUUID, caster.getInventory().getContents());
        savedLocations.put(casterUUID, caster.getLocation().clone());
        
        caster.getInventory().clear();
        caster.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 310, 1, false, false));
        caster.hidePlayer(plugin, target);

        // Immobilisation
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 310, 1, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 310, 255, false, false));

        activePossessions.put(casterUUID, targetUUID);

        new BukkitRunnable() {
            int timer = 300;
            @Override
            public void run() {
                if (timer <= 0 || !caster.isOnline() || !target.isOnline()) {
                    stopPossession(caster, target);
                    this.cancel();
                    return;
                }
                target.teleport(caster.getLocation());
                caster.getWorld().spawnParticle(Particle.SOUL, caster.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.02);
                timer -= 2;
            }
        }.runTaskTimer(plugin, 0, 2);

        caster.sendMessage("§3§lPOSSESSION ACTIVE !");
    }

    private void stopPossession(Player caster, Player target) {
        UUID casterUUID = caster.getUniqueId();
        if (caster.isOnline()) {
            if (savedInventories.containsKey(casterUUID)) caster.getInventory().setContents(savedInventories.remove(casterUUID));
            if (savedLocations.containsKey(casterUUID)) caster.teleport(savedLocations.remove(casterUUID));
            caster.removePotionEffect(PotionEffectType.INVISIBILITY);
            if (target != null) caster.showPlayer(plugin, target);
        }
        if (target != null && target.isOnline()) {
            target.removePotionEffect(PotionEffectType.BLINDNESS);
            target.removePotionEffect(PotionEffectType.SLOWNESS);
        }
        activePossessions.remove(casterUUID);
    }

    // Méthode utilitaire pour le Dispatcher
    public boolean isVictim(UUID uuid) {
        return activePossessions.containsValue(uuid);
    }
}