package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SMPlayer;
import com.github.ipecter.smartmoving.SmartMoving;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SneakingListener implements Listener {

    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();
    private final ConfigManager config = ConfigManager.getInstance();

    private final Set<Player> doubleSneakingCheck = new HashSet<>();
    private final Map<Player, BukkitTask> holdCheck = new HashMap<>();

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!Utils.canCrawl(event.getPlayer())) {
            return; //Because canCrawl is not work in CrPlayer Class.
        }
        Bukkit.getScheduler().runTaskAsynchronously(SmartMoving.plugin, () -> {
            Player player = event.getPlayer();
            SMPlayer smPlayer = smartMovingManager.getPlayerCrawling(player);

            if (event.isSneaking()) {

                // Stop Crawling
                if (smPlayer != null && smPlayer.toggleMode() != null && smPlayer.toggleMode() && config.getCrawlingModes().contains("TOGGLE")) {
                    Bukkit.getScheduler().runTask(SmartMoving.plugin, smPlayer::stopCrawling);
                    return;
                }

                // Start crawling when sneaking while in a tunnel if HOLD mode is enabled
                if (smPlayer == null && player.isSwimming() && config.getCrawlingModes().contains("HOLD") && !player.getLocation().getBlock().isLiquid()) {
                    Bukkit.getScheduler().runTask(SmartMoving.plugin, () -> smartMovingManager.startCrawling(player));
                    return;
                }

                // Tunnels
                if (config.getCrawlingModes().contains("TUNNELS")) {
                    if (Utils.isInFrontOfATunnel(player)) {

                        Bukkit.getScheduler().runTask(SmartMoving.plugin, () -> smartMovingManager.startCrawling(player));

                        Utils.WallFace facing = Utils.WallFace.fromBlockFace(player.getFacing());

                        Bukkit.getScheduler().runTaskLater(SmartMoving.plugin, () -> {
                            SMPlayer smPlayer1 = smartMovingManager.getPlayerCrawling(player);
                            if (smPlayer1 != null) {
                                smPlayer1.stopCrawling();
                            }
                        }, 10);
                        return;
                    }
                }

                if (player.getLocation().getPitch() > 87) { // The player is looking downwards and is not crawling
                    // Double Sneaking
                    if (config.getCrawlingKeys().contains("DOUBLE_SHIFT") || config.getCrawlingKeys().contains("DOWN_DOUBLE_SHIFT")) { //if double sneaking is enabled
                        if (!doubleSneakingCheck.contains(player)) {
                            doubleSneakingCheck.add(player);
                            Bukkit.getScheduler().runTaskLaterAsynchronously(SmartMoving.plugin, () -> doubleSneakingCheck.remove(player), 8);
                        } else {
                            doubleSneakingCheck.remove(player);
                            Bukkit.getScheduler().runTask(SmartMoving.plugin, () -> smartMovingManager.startCrawling(player));
                        }
                        return;
                    }

                    // Hold
                    for (String start_crawling : config.getCrawlingKeys()) {
                        if (start_crawling.contains("HOLD")) {
                            this.holdCheck.remove(player);
                            int time = Integer.parseInt(start_crawling.split("_")[1]);
                            this.holdCheck.put(player, Bukkit.getScheduler().runTaskLater(SmartMoving.plugin, () -> {
                                if (player.isSneaking() && player.getLocation().getPitch() > 87) {
                                    smartMovingManager.startCrawling(player);
                                    this.holdCheck.remove(player);
                                }
                            }, time));
                            return;
                        }
                    }
                }

            } else { // If the player is not sneaking

                // Stop Crawling
                if (smPlayer != null && smPlayer.toggleMode() != null && !smPlayer.toggleMode() && config.getCrawlingModes().contains("HOLD")) {
                    Bukkit.getScheduler().runTask(SmartMoving.plugin, smPlayer::stopCrawling);
                }
            }
        });
    }

}
