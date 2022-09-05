package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SMPlayer;
import com.github.ipecter.smartmoving.SmartMoving;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.utils.CrawlingUtil;
import com.github.ipecter.smartmoving.utils.WallJumpUtil;
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
        SmartMovingManager.getInstance().getPlugin().getLogger().info("ToggleSneak - Start");
        Player player = e.getPlayer();
        SMPlayer smPlayer = smartMovingManager.getPlayer(player);
        if (!player.isFlying()) {
            if (smPlayer.isOnWall() && !e.isSneaking())
                smPlayer.stopWallJump();
            else if (WallJumpUtil.isTouchingAWall(player) && e.isSneaking() && !player.isOnGround())
                SmartMovingManager.getInstance().getPlugin().getLogger().info("ToggleSneak - WJ");
            smPlayer.startWallJump();
        }
        if (!CrawlingUtil.canCrawl(player)) {
            SmartMovingManager.getInstance().getPlugin().getLogger().info("ToggleSneak - return1");
            return;
        }
        if (e.isSneaking()) {
            SmartMovingManager.getInstance().getPlugin().getLogger().info("Trigger Sneak");
            if (smartMovingManager.isCrawling(player)) {
                SmartMovingManager.getInstance().getPlugin().getLogger().info("Stop Crawling - Sneak");
                smartMovingManager.stopCrawling(player);
                SmartMovingManager.getInstance().getPlugin().getLogger().info("ToggleSneak - return2");
                return;
            }
            if (smPlayer != null && smPlayer.toggleMode() != null && smPlayer.toggleMode() && config.getCrawlingModes().contains("TOGGLE")) {
                Bukkit.getScheduler().runTask(plugin, smPlayer::stopCrawling);
                SmartMoving.getPlugin(SmartMoving.class).getLogger().info("--------------------------");
                SmartMovingManager.getInstance().getPlugin().getLogger().info("Stop Crawling - Null1: " + smPlayer + " / " + smPlayer.toggleMode());
                SmartMovingManager.getInstance().getPlugin().getLogger().info(config.getCrawlingModes().toString());
                SmartMovingManager.getInstance().getPlugin().getLogger().info("ToggleSneak - return3");
                SmartMoving.getPlugin(SmartMoving.class).getLogger().info("--------------------------");
                return;
            }

            if (smPlayer == null && player.isSwimming() && config.getCrawlingModes().contains("HOLD") && !player.getLocation().getBlock().isLiquid()) {
                Bukkit.getScheduler().runTask(plugin, () -> smartMovingManager.startCrawling(player));
                SmartMovingManager.getInstance().getPlugin().getLogger().info("Starat Crawling - 1");
                SmartMovingManager.getInstance().getPlugin().getLogger().info("ToggleSneak - return4");
                return;
            }
            SmartMovingManager.getInstance().getPlugin().getLogger().info("Modes: " + config.getCrawlingModes());
            SmartMovingManager.getInstance().getPlugin().getLogger().info("Keys: " + config.getCrawlingKeys());
            if (config.getCrawlingModes().contains("TUNNELS")) {
                if (CrawlingUtil.isInFrontOfATunnel(player)) {
                    Bukkit.getScheduler().runTask(plugin, () -> smartMovingManager.startCrawling(player));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        SMPlayer smPlayer1 = smartMovingManager.getPlayer(player);
                        if (smPlayer1 != null) {
                            smPlayer1.stopCrawling();
                            SmartMovingManager.getInstance().getPlugin().getLogger().info("Stop Crawling - Null2");
                        }
                    }, 10);
                    SmartMovingManager.getInstance().getPlugin().getLogger().info("ToggleSneak - return5");
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
                    SmartMovingManager.getInstance().getPlugin().getLogger().info("ToggleSneak - return6");
                    return;
                }
                for (String startCrawling : config.getCrawlingKeys()) {
                    if (startCrawling.contains("HOLD")) {
                        BukkitTask bukkitTask = holdCheck.get(player);
                        if (bukkitTask != null) {
                            bukkitTask.cancel();
                        }
                        holdCheck.remove(player);
                        long time = Long.parseLong(startCrawling.split("_")[1]);
                        SmartMovingManager.getInstance().getPlugin().getLogger().info("HOLD_X " + String.valueOf(time));
                        holdCheck.put(player, Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (player.isSneaking() && player.getLocation().getPitch() > 85) {
                                smartMovingManager.startCrawling(player);
                                holdCheck.remove(player);
                            }
                        }, time * 20));
                        SmartMovingManager.getInstance().getPlugin().getLogger().info("ToggleSneak - return7");
                        return;
                    }
                }
            }

        } else {
            if (smPlayer != null && smPlayer.toggleMode() != null && !smPlayer.toggleMode() && config.getCrawlingModes().contains("HOLD")) {
                smPlayer.stopCrawling();
                SmartMovingManager.getInstance().getPlugin().getLogger().info("Stop Crawling - Else");
            }
        }
        SmartMovingManager.getInstance().getPlugin().getLogger().info("ToggleSneak - End");
    }
}
