package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SmartMoving;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(SmartMoving.plugin, () -> {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                if (event.getClickedBlock() != null) {
                    if ((event.getClickedBlock().isPassable() || Utils.isFullBlock(event.getClickedBlock()) && event.getClickedBlock().equals(event.getPlayer().getLocation().add(0, 1.5, 0).getBlock()))) {
                        if (smartMovingManager.isCrawling(event.getPlayer())) {
                            event.setCancelled(true);
                            smartMovingManager.getPlayerCrawling(event.getPlayer()).replaceBarrier(event.getClickedBlock());
                        }
                    }
                }
            }
        });
    }

}
