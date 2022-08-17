package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SmartMoving;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class PlayerInteractListener implements Listener {

    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();
    private final Plugin plugin = SmartMoving.getPlugin(SmartMoving.class);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        System.out.println("E1: " + event.getClickedBlock().getType().name());
        System.out.println("E2: " + smartMovingManager.isCrawling(event.getPlayer()));
        if (smartMovingManager.isCrawling(event.getPlayer())) {
            System.out.println("E3");
            if (event.getClickedBlock().getType().isAir()) {
                System.out.println("E4");
                event.setCancelled(true);
                event.getPlayer().sendBlockChange(event.getClickedBlock().getLocation(), Utils.BARRIER_BLOCK_DATA);
            }
        }
//        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
//            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
//                if (event.getClickedBlock() != null) {
//                    if ((event.getClickedBlock().isPassable() || Utils.checkAboveLoc(event.getClickedBlock()) && event.getClickedBlock().equals(event.getPlayer().getLocation().add(0, 1.5, 0).getBlock()))) {
//                        if (smartMovingManager.isCrawling(event.getPlayer())) {
//                            event.setCancelled(true);
//                            smartMovingManager.getPlayerCrawling(event.getPlayer()).replaceBarrier(event.getClickedBlock());
//                        }
//                    }
//                }
//            }
//        });
    }

}
