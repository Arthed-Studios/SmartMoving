package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!configManager.isEnablePlugin()) return;
        smartMovingManager.removePlayer(e.getPlayer());
    }
}

