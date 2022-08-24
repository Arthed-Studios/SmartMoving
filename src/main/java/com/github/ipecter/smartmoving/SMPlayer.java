package com.github.ipecter.smartmoving;

import com.github.ipecter.nms.NmsPackets;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class SMPlayer {

    private final static SmartMovingManager manager = SmartMovingManager.getInstance();
    private final static ConfigManager config = ConfigManager.getInstance();
    private final static NmsPackets nmsPacketManager = manager.getNmsPacketManager();

    private final Player player;

    private Block barrierBlock;

    private BukkitTask moveTask; // task running every 3 ticks making sure the barrier is above the player
    private BukkitTask canCrawlTask; // task running every 20 ticks checking if the player can continue crawling
    private int nonGround = 0;

    private Boolean toggleMode;

    protected SMPlayer(Player player) {
        this.player = player;
        startCrawling();
    }

    public void startCrawling() {
        if (!Utils.canCrawl(this.player)) {
            this.stopCrawling();
            return;
        }
        barrierBlock = player.getLocation().getBlock();
        player.setSwimming(true);

        moveTask = Bukkit.getScheduler().runTaskTimer(manager.getPlugin(), () -> {
            player.setSwimming(true);
            Block blockAbovePlayer = this.player.getLocation().add(0, 1.5, 0).getBlock();
            if (!this.barrierBlock.equals(blockAbovePlayer)) {
                this.replaceBarrier(blockAbovePlayer);
                barrierBlock = blockAbovePlayer;
            } else {
                barrierBlock = blockAbovePlayer;
                if (Utils.checkAbove(blockAbovePlayer)) {
                    player.sendBlockChange(blockAbovePlayer.getLocation(), Utils.BARRIER_BLOCK_DATA);
                }
            }
        }, 0, 1);

        canCrawlTask = Bukkit.getScheduler().runTaskTimerAsynchronously(manager.getPlugin(), () -> {
            if (!player.isOnGround()) {
                if (nonGround >= 8) {
                    Bukkit.getScheduler().runTask(manager.getPlugin(), this::stopCrawling);
                    nonGround = 0;
                } else {
                    nonGround++;
                }
                return;
            } else if (player.isOnGround()) {
                nonGround = 0;
                return;
            }
            if (!Utils.canCrawlCancel(this.player)) {
                Bukkit.getScheduler().runTask(manager.getPlugin(), this::stopCrawling);
                return;
            } else if (this.player.getVelocity().getY() > 0 && this.player.getNoDamageTicks() == 0) {
                Bukkit.getScheduler().runTask(manager.getPlugin(), this::stopCrawling);
                return;
            }
        }, 5, 1);


        // Check if toggle mode should be used
        boolean hold = config.getCrawlingModes().contains("HOLD");
        boolean toggle = config.getCrawlingModes().contains("TOGGLE");
        if (hold && toggle) {
            Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> this.toggleMode = !this.player.isSneaking(), 10);
        } else {
            this.toggleMode = toggle;
        }
    }

    public void replaceBarrier(Block blockAbovePlayer) {
        Utils.revertBlockPacket(player, barrierBlock);
        barrierBlock = blockAbovePlayer;
        if (Utils.checkAbove(blockAbovePlayer)) {
            player.sendBlockChange(blockAbovePlayer.getLocation(), Utils.BARRIER_BLOCK_DATA);
        }
    }


    public void stopCrawling() {
        player.setSwimming(false);

        if (barrierBlock != null) {
            Utils.revertBlockPacket(player, barrierBlock);
            Utils.revertBlockPacket(player, barrierBlock.getLocation().subtract(0, 2, 0).getBlock());
            nmsPacketManager.removeFakeBlocks(player);
        }

        if (moveTask != null) {
            moveTask.cancel();
        }

        if (canCrawlTask != null) {
            canCrawlTask.cancel();
        }
        SmartMovingManager.getInstance().removePlayer(player);
    }

    public Boolean toggleMode() {
        return toggleMode;
    }

}
