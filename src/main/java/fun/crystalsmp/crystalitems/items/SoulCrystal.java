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
    private final HashMap<UUID, UUID> activePossessions = new HashMap<>();

    public SoulCrystal(JavaPlugin plugin) {
        super("soul_crystal", 0);
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        Player caster = event.getPlayer();
        if (isCaster(caster.getUniqueId()) || isCooldownItem(caster)) return;

        Player target = caster.getNearbyEntities(20, 20, 20).stream()
                .filter(e -> e instanceof Player && !e.equals(caster))
                .map(e -> (Player) e).findFirst().orElse(null);

        if (target == null) {
            caster.sendMessage("§cAucune âme à proximité.");
            return;
        }

        UUID casterUUID = caster.getUniqueId();
        
        // 1. SAUVEGARDE POSITION ET INVENTAIRE
        savedLocations.put(casterUUID, caster.getLocation().clone());
        savedInventories.put(casterUUID, caster.getInventory().getContents());

        // 2. TÉLÉPORTATION IMMÉDIATE DU LANCEUR SUR LA CIBLE
        caster.teleport(target.getLocation());

        // 3. VIDE L'INVENTAIRE POUR LA DURÉE DE LA POSSESSION
        caster.getInventory().clear();
        caster.getInventory().setArmorContents(null);
        caster.getInventory().setItemInOffHand(null);
        caster.updateInventory();

        // 4. EFFETS ET ÉTATS
        caster.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 310, 1, false, false));
        caster.hidePlayer(plugin, target);
        caster.setCollidable(false);
        target.setCollidable(false);
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 310, 1, false, false));

        activePossessions.put(casterUUID, target.getUniqueId());

        new BukkitRunnable() {
            int timer = 300; // 15 secondes
            @Override
            public void run() {
                if (timer <= 0 || !caster.isOnline() || !target.isOnline() || target.isDead()) {
                    stopPossession(caster, target);
                    this.cancel();
                    return;
                }
                
                // LA BOUCLE DE TP (La cible suit le lanceur)
                target.teleport(caster.getLocation());
                
                caster.getWorld().spawnParticle(Particle.SOUL, caster.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.02);
                timer -= 2;
            }
        }.runTaskTimer(plugin, 0, 2);

        caster.sendMessage("§3§lPOSSESSION ACTIVE !");
    }

    public void stopPossession(Player caster, Player target) {
        UUID casterUUID = caster.getUniqueId();
        
        if (caster != null && caster.isOnline()) {
            // A. TP À LA POSITION D'ORIGINE
            if (savedLocations.containsKey(casterUUID)) {
                caster.teleport(savedLocations.get(casterUUID));
            }

            // B. DROP DES ITEMS RAMASSÉS PENDANT LA POSSESSION
            // On fait drop tout ce qui est actuellement dans l'inventaire (le "vide" qui s'est rempli)
            for (ItemStack item : caster.getInventory().getContents()) {
                if (item != null && item.getType().isItem()) {
                    caster.getWorld().dropItemNaturally(caster.getLocation(), item);
                }
            }
            caster.getInventory().clear(); // On nettoie avant de rendre le vrai stuff

            // C. RESTAURATION DU VRAI STUFF SAUVEGARDÉ
            if (savedInventories.containsKey(casterUUID)) {
                caster.getInventory().setContents(savedInventories.remove(casterUUID));
            }
            
            // D. RESET DES ÉTATS
            savedLocations.remove(casterUUID);
            caster.setFireTicks(0);
            caster.setFallDistance(0);
            caster.setRemainingAir(caster.getMaximumAir());
            caster.removePotionEffect(PotionEffectType.INVISIBILITY);
            caster.setCollidable(true);
            if (target != null) caster.showPlayer(plugin, target);
            
            caster.sendMessage("§bLien rompu. Votre inventaire a été restauré.");
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
    public HashMap<UUID, ItemStack[]> getSavedInventories() { return savedInventories; }
}