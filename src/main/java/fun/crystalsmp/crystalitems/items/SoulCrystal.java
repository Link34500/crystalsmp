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
    private final HashMap<UUID, UUID> activePossessions = new HashMap<>(); // Caster -> Victim

    public SoulCrystal(JavaPlugin plugin) {
        super("soul_crystal", 0);
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        Player caster = event.getPlayer();
        UUID casterUUID = caster.getUniqueId();

        // Sécurité : Pas de double possession
        if (isCaster(casterUUID) || isVictim(casterUUID) || isCooldownItem(caster)) return;

        Player target = caster.getNearbyEntities(20, 20, 20).stream()
                .filter(e -> e instanceof Player && !e.equals(caster))
                .map(e -> (Player) e).findFirst().orElse(null);

        if (target == null) {
            caster.sendMessage("§cAucune âme à proximité.");
            return;
        }

        UUID targetUUID = target.getUniqueId();
        if (isCaster(targetUUID) || isVictim(targetUUID)) {
            caster.sendMessage("§cCette âme est déjà liée.");
            return;
        }

        // --- DÉBUT ---
        savedLocations.put(casterUUID, caster.getLocation().clone());
        savedInventories.put(casterUUID, caster.getInventory().getContents());

        caster.teleport(target.getLocation());
        caster.getInventory().clear();
        caster.updateInventory();

        caster.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 310, 1, false, false));
        caster.hidePlayer(plugin, target);
        caster.setCollidable(false);
        target.setCollidable(false);
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 310, 1, false, false));

        activePossessions.put(casterUUID, targetUUID);

        new BukkitRunnable() {
            int timer = 300; 
            @Override
            public void run() {
                if (timer <= 0 || !caster.isOnline() || !target.isOnline() || target.isDead()) {
                    stopPossession(caster, target);
                    this.cancel();
                    return;
                }
                // TP Victim sur Caster (Hitbox alignée)
                target.teleport(caster.getLocation());
                caster.getWorld().spawnParticle(Particle.SOUL, caster.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.02);
                timer -= 2;
            }
        }.runTaskTimer(plugin, 0, 2);

        caster.sendMessage("§3§lPOSSESSION ACTIVE !");
    }

    public void stopPossession(Player caster, Player target) {
            if (caster == null) return;
            UUID casterUUID = caster.getUniqueId();
            
            if (caster.isOnline()) {
                // 1. ÉTEINDRE LE FEU IMMÉDIATEMENT
                caster.setFireTicks(0); 
                
                // 2. TP retour
                if (savedLocations.containsKey(casterUUID)) {
                    caster.teleport(savedLocations.get(casterUUID));
                }

                // 3. DROP DES ITEMS RAMASSÉS
                for (ItemStack item : caster.getInventory().getContents()) {
                    if (item != null) {
                        caster.getWorld().dropItemNaturally(caster.getLocation(), item);
                    }
                }
                caster.getInventory().clear();

                // 4. RESTAURATION DU STUFF
                if (savedInventories.containsKey(casterUUID)) {
                    caster.getInventory().setContents(savedInventories.remove(casterUUID));
                }
                
                // 5. RESET DES ÉTATS ET NETTOYAGE
                savedLocations.remove(casterUUID);
                caster.setFallDistance(0); // Évite les dégâts de chute au retour
                caster.removePotionEffect(PotionEffectType.INVISIBILITY);
                caster.setCollidable(true);
                
                if (target != null) {
                    caster.showPlayer(plugin, target);
                    // On éteint aussi la cible par sécurité si elle brûlait
                    target.setFireTicks(0); 
                }
                
                caster.sendMessage("§bLien rompu. Vous avez été purifié des flammes.");
                caster.updateInventory();
            }

            if (target != null && target.isOnline()) {
                target.removePotionEffect(PotionEffectType.BLINDNESS);
                target.setCollidable(true);
            }
            activePossessions.remove(casterUUID);
        }
      
    public boolean isVictim(UUID uuid) { return activePossessions.containsValue(uuid); }
    public boolean isCaster(UUID uuid) { return activePossessions.containsKey(uuid); }
    public UUID getVictimByCaster(UUID casterUUID) { return activePossessions.get(casterUUID); }
}