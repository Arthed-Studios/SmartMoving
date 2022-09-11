package com.github.ipecter.smartmoving.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.github.ipecter.smartmoving.SMPlayer;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerJump implements Listener {

    private final ConfigManager config = ConfigManager.getInstance();
    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onJump(PlayerJumpEvent e) {
        if (!config.isEnablePlugin()) return;
        Player player = e.getPlayer();
        SMPlayer smPlayer = smartMovingManager.getPlayer(player);
        if (smPlayer.isCrawling()) {
            smPlayer.stopCrawling();
        }
    }
}
