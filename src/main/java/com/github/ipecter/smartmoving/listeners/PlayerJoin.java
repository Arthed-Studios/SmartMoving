package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.rtu.pluginlib.RTUPluginLib;
import com.github.ipecter.rtu.pluginlib.managers.TextManager;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final ConfigManager config = ConfigManager.getInstance();
    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();
    private final TextManager textManager = RTUPluginLib.getTextManager();
    private final String motd = "<white>SmartMoving developed by IPECTER & Arthed (Original)</white>";

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (config.isMotd()) {
            player.sendMessage(textManager.formatted(player, config.getTranslation("prefix") + motd));
        } else {
            if (player.isOp())
                player.sendMessage(textManager.formatted(player, config.getTranslation("prefix") + motd));
        }
        smartMovingManager.addPlayer(player);
    }
}

