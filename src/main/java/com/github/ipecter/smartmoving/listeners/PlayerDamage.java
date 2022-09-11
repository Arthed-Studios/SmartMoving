package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SMPlayer;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamage implements Listener {

    private final ConfigManager config = ConfigManager.getInstance();
    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!config.isEnablePlugin()) return;
        if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL) && e.getEntity() instanceof Player player) {
            SMPlayer smPlayer = smartMovingManager.getPlayer(player);
            if (smPlayer != null && smPlayer.isSliding()) {
                e.setCancelled(true);
                smPlayer.stopWallJump(false);
            }
        }

    }

}
