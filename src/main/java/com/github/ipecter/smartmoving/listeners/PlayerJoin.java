package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SmartMovingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        smartMovingManager.addPlayer(e.getPlayer());
    }
}

