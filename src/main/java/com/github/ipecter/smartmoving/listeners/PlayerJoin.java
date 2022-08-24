package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.rtu.utilapi.RTUUtilAPI;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private ConfigManager configManager = ConfigManager.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!configManager.isEnablePlugin()) return;
        Player player = e.getPlayer();
        if (configManager.isMotd()) {
            player.sendMessage(RTUUtilAPI.getTextManager().formatted(player, configManager.getPrefix() + "&fSmartMoving developed by IPECTER, Arthed(Original)"));
        } else {
            if (player.isOp())
                player.sendMessage(RTUUtilAPI.getTextManager().formatted(player, configManager.getPrefix() + "&fSmartMoving developed by IPECTER, Arthed(Original)"));
        }
    }
}
