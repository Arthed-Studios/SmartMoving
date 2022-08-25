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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerToggleSneak implements Listener {

    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();
    private final ConfigManager config = ConfigManager.getInstance();

    private final Set<Player> doubleSneakingCheck = new HashSet<>();
    private final Map<Player, BukkitTask> holdCheck = new HashMap<>();
    private final Plugin plugin = smartMovingManager.getPlugin();

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        SMPlayer smPlayer = smartMovingManager.getPlayerCrawling(player);
        if (!Utils.canCrawl(player)) {
            return;
        }
        if (e.isSneaking()) {
            if (smartMovingManager.isCrawling(player)) {
                SmartMoving.debug("Stop Crawling - Sneak");
                smartMovingManager.stopCrawling(player);
                return;
            }
            if (smPlayer != null && smPlayer.toggleMode() != null && smPlayer.toggleMode() && config.getCrawlingModes().contains("TOGGLE")) {
                Bukkit.getScheduler().runTask(plugin, smPlayer::stopCrawling);
                return;
            }

            if (smPlayer == null && player.isSwimming() && config.getCrawlingModes().contains("HOLD") && !player.getLocation().getBlock().isLiquid()) {
                Bukkit.getScheduler().runTask(plugin, () -> smartMovingManager.startCrawling(player));
                return;
            }
            SmartMoving.debug("Modes: " + config.getCrawlingModes());
            SmartMoving.debug("Keys: " + config.getCrawlingKeys());
            if (config.getCrawlingModes().contains("TUNNELS")) {
                if (Utils.isInFrontOfATunnel(player)) {
                    Bukkit.getScheduler().runTask(plugin, () -> smartMovingManager.startCrawling(player));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        SMPlayer smPlayer1 = smartMovingManager.getPlayerCrawling(player);
                        if (smPlayer1 != null) {
                            smPlayer1.stopCrawling();
                        }
                    }, 10);
                    return;
                }
            }

            if (player.getLocation().getPitch() > 85) { // The player is looking downwards and is not crawling
                if (config.getCrawlingKeys().contains("DOUBLE_SHIFT")) { //if double sneaking is enabled
                    if (!doubleSneakingCheck.contains(player)) {
                        doubleSneakingCheck.add(player);
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> doubleSneakingCheck.remove(player), 8);
                    } else {
                        doubleSneakingCheck.remove(player);
                        smartMovingManager.startCrawling(player);
                    }
                    return;
                }
                for (String start_crawling : config.getCrawlingKeys()) {
                    if (start_crawling.contains("HOLD")) {
                        this.holdCheck.remove(player);
                        int time = Integer.parseInt(start_crawling.split("_")[1]);
                        this.holdCheck.put(player, Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (player.isSneaking() && player.getLocation().getPitch() > 85) {
                                smartMovingManager.startCrawling(player);
                                this.holdCheck.remove(player);
                            }
                        }, time));
                        return;
                    }
                }
            }

        } else {
            if (smPlayer != null && smPlayer.toggleMode() != null && !smPlayer.toggleMode() && config.getCrawlingModes().contains("HOLD")) {
                smPlayer.stopCrawling();
            }
        }
    }
}
