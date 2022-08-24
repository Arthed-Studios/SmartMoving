package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SmartMovingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {


    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (smartMovingManager.isCrawling(e.getEntity()))
        smartMovingManager.stopCrawling(e.getEntity());
    }

}
