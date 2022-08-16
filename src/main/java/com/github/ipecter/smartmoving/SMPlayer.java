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

        this.barrierBlock = player.getLocation().getBlock();

        this.player.setSwimming(true);

        this.moveTask = Bukkit.getScheduler().runTaskTimer(manager.getPlugin(), () -> {

            Block blockAbovePlayer = this.player.getLocation().add(0, 1.5, 0).getBlock();
            if (!this.barrierBlock.equals(blockAbovePlayer)) {
                this.replaceBarrier(blockAbovePlayer);
            }

        }, 0, 1); // runs every tick

        this.canCrawlTask = Bukkit.getScheduler().runTaskTimerAsynchronously(manager.getPlugin(), () -> {
            if (!Utils.canCrawl(this.player)) {
                Bukkit.getScheduler().runTask(manager.getPlugin(), this::stopCrawling);
            } else if (this.player.getVelocity().getY() > 0 && this.player.getNoDamageTicks() == 0)
                Bukkit.getScheduler().runTask(manager.getPlugin(), this::stopCrawling);
        }, 20, 20); // runs every 20 ticks


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
        Utils.revertBlockPacket(this.player, this.barrierBlock);
        Utils.revertBlockPacket(this.player, this.barrierBlock.getLocation().subtract(0, 2, 0).getBlock());
        nmsPacketManager.removeFakeBlocks(this.player);
        this.barrierBlock = blockAbovePlayer;
        if (blockAbovePlayer.isPassable() || Utils.isFullBlock(blockAbovePlayer)) {
            this.player.sendBlockChange(blockAbovePlayer.getLocation(), Utils.BARRIER_BLOCK_DATA);
            if (!blockAbovePlayer.getType().isAir()) {
                Block floorBlock = blockAbovePlayer.getLocation().subtract(0, 2, 0).getBlock();
                nmsPacketManager.spawnFakeBlocks(this.player, blockAbovePlayer, floorBlock);
            }
        }
    }

    public void stopCrawling() {
        this.player.setSwimming(false);

        if (this.barrierBlock != null) {
            Utils.revertBlockPacket(this.player, this.barrierBlock);
            Utils.revertBlockPacket(this.player, this.barrierBlock.getLocation().subtract(0, 2, 0).getBlock());
            nmsPacketManager.removeFakeBlocks(this.player);
        }

        if (this.moveTask != null) {
            this.moveTask.cancel();
        }
        if (this.canCrawlTask != null) {
            this.canCrawlTask.cancel();
        }

        SmartMovingManager.getInstance().removePlayer(this.player);
    }

    public Boolean toggleMode() {
        return this.toggleMode;
    }

}
