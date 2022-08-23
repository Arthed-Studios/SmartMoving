package com.github.ipecter.smartmoving.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.github.ipecter.smartmoving.SmartMovingManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerJump implements Listener {

    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onJump(PlayerJumpEvent e) {
        Player player = e.getPlayer();
        System.out.println("C1: " + smartMovingManager.isCrawling(player));
        if (smartMovingManager.isCrawling(player)) {
            System.out.println("F3");
            smartMovingManager.stopCrawling(player);
        }
    }
}
