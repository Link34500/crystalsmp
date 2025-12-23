package fun.crystalsmp.crystalitems.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FlashbangCrystal extends CustomItem {

    private final JavaPlugin plugin;

    public FlashbangCrystal(JavaPlugin plugin) {
        // 30 seconds cooldown
        super("flashbang_crystal", 30);
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isCooldownItem(player)) return;

        double radius = 15.0;
        int durationTicks = 60; // 3 seconds = 60 ticks

        int affectedCount = 0;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target && !target.equals(player)) {
                
                // 1. SOUND MIX (Tinnitus + Explosion)
                // Low muffled explosion
                target.playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                // High-pitched ringing (Anvil at max pitch)
                target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.6f, 2.0f);
                // Wind burst
                target.playSound(target.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.2f);
                // Low hum (Totem at min pitch)
                target.playSound(target.getLocation(), Sound.ITEM_TOTEM_USE, 0.8f, 0.1f);

                // 2. POTION EFFECTS
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks + 20, 0, false, false));

                // 3. WHITE SCREEN LOOP
                // Spawning FLASH particles every 2 ticks to blind the player's screen
                for (int i = 0; i < durationTicks; i += 2) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (target.isOnline()) {
                            // Spawn particle exactly at eye level
                            Location eyeLoc = target.getEyeLocation();
                            // Offset slightly forward to fill the vision
                            Location flashLoc = eyeLoc.add(eyeLoc.getDirection().multiply(0.2));
                            
                            target.spawnParticle(Particle.FLASH, flashLoc, 10, 0.05, 0.05, 0.05, 0);
                        }
                    }, i);
                }

                target.sendMessage("§f§l⚠ §c§lBLINDED BY A FLASHBANG! §f§l⚠");
                affectedCount++;
            }
        }

        if (affectedCount > 0) {
            player.sendMessage("§fThe flashbang exploded!");
        } else {
            player.sendMessage("§cNo one in range to be flashed.");
        }
    }
}