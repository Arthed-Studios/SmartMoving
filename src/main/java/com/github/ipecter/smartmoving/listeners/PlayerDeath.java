package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SMPlayer;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    private final ConfigManager config = ConfigManager.getInstance();
    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (!config.isEnablePlugin()) return;
        SMPlayer smPlayer = smartMovingManager.getPlayer(e.getPlayer());
        if (smPlayer.isCrawling()) {
            smPlayer.stopCrawling();
        }

    }

}
