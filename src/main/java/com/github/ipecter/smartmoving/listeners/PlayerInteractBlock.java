package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractBlock implements Listener {

    private final ConfigManager config = ConfigManager.getInstance();
    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onInteractBlock(PlayerInteractEvent e) {
        if (!config.isEnablePlugin()) return;
        if (e.getClickedBlock() != null && e.getClickedBlock().equals(e.getPlayer().getLocation().add(0, 1.5, 0).getBlock())) {
            if (smartMovingManager.getPlayer(e.getPlayer()).isCrawling()) {
                e.setCancelled(true);
            }
        }
    }
}
