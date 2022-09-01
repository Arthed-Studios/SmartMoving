package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.rtu.pluginlib.RTUPluginLib;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!configManager.isEnablePlugin()) return;
        Player player = e.getPlayer();
        if (configManager.isMotd()) {
            player.sendMessage(RTUPluginLib.getTextManager().formatted(player, configManager.getTranslation("prefix") + "&fSmartMoving developed by IPECTER, Arthed (Original)"));
        } else {
            if (player.isOp())
                player.sendMessage(RTUPluginLib.getTextManager().formatted(player, configManager.getTranslation("prefix") + "&fSmartMoving developed by IPECTER, Arthed (Original)"));
        }
        smartMovingManager.addPlayer(player);
    }
}

