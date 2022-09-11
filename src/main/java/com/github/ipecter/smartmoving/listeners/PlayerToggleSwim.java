package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SMPlayer;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;

public class PlayerToggleSwim implements Listener {

    private final ConfigManager config = ConfigManager.getInstance();
    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onEntityToggleSwim(EntityToggleSwimEvent e) {
        if (!config.isEnablePlugin()) return;
        if (!e.isSwimming() && e.getEntity() instanceof Player player) {
            SMPlayer smPlayer = smartMovingManager.getPlayer(player);
            if (smPlayer.isCrawling()) {
                e.setCancelled(true);
            }
        }
    }

}
