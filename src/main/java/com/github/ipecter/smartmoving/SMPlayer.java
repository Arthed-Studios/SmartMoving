package com.github.ipecter.smartmoving;

import com.github.ipecter.nms.NmsPackets;
import com.github.ipecter.smartmoving.enums.WallJumpWallFace;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.utils.CrawlingUtil;
import com.github.ipecter.smartmoving.utils.WallJumpUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class SMPlayer {

    private final static SmartMovingManager manager = SmartMovingManager.getInstance();
    private final static ConfigManager config = ConfigManager.getInstance();
    private final static NmsPackets nmsPacketManager = manager.getNmsPacketManager();

    //[ General Part ]
    private final Player player;


    private boolean wallJumping;
    private boolean crawling;

    //[ Crawling Part ]
    //Every 1 Tick Task for Movement
    private BukkitTask moveTask;

    private Block barrierBlock;

    private BukkitTask canCrawlTask;

    //For Checking Player Non-Ground Time
    private int nonGround = 0;

    private Boolean toggleMode;

    //[ WallJump Part ]
    private boolean sliding;

    private int remainingJumps = -1;
    private WallJumpWallFace lastFacing;
    private Location lastJumpLocation;

    private float velocityY;
    private BukkitTask velocityTask;
    private BukkitTask fallTask;
    private BukkitTask stopWallJumpingTask;

    public boolean isWallJumping() {
        return wallJumping;
    }

    public void setWallJumping(boolean wallJumping) {
        this.wallJumping = wallJumping;
    }

    public boolean isCrawling() {
        return crawling;
    }

    public void setCrawling(boolean crawling) {
        this.crawling = crawling;
    }


    public boolean isSliding() {
        return sliding;
    }

    public int getRemainingJumps() {
        return remainingJumps;
    }

    public WallJumpWallFace getLastFacing() {
        return lastFacing;
    }

    public Location getLastJumpLocation() {
        return lastJumpLocation;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public SMPlayer(Player player) {
        this.player = player;
    }

    public void startCrawling() {
        if (!CrawlingUtil.canCrawl(player)) {
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
                if (CrawlingUtil.checkAbove(blockAbovePlayer)) {
                    player.sendBlockChange(blockAbovePlayer.getLocation(), CrawlingUtil.BARRIER_BLOCK_DATA);
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
            if (!CrawlingUtil.canCrawlCancel(this.player)) {
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

        crawling = true;
    }

    public void replaceBarrier(Block blockAbovePlayer) {
        CrawlingUtil.revertBlockPacket(player, barrierBlock);
        barrierBlock = blockAbovePlayer;
        if (CrawlingUtil.checkAbove(blockAbovePlayer)) {
            player.sendBlockChange(blockAbovePlayer.getLocation(), CrawlingUtil.BARRIER_BLOCK_DATA);
        }
    }


    public void stopCrawling() {
        player.setSwimming(false);

        if (barrierBlock != null) {
            CrawlingUtil.revertBlockPacket(player, barrierBlock);
            CrawlingUtil.revertBlockPacket(player, barrierBlock.getLocation().subtract(0, 2, 0).getBlock());
            nmsPacketManager.removeFakeBlocks(player);
        }

        if (moveTask != null) {
            moveTask.cancel();
        }

        if (canCrawlTask != null) {
            canCrawlTask.cancel();
        }
        crawling = false;
    }

    public Boolean toggleMode() {
        return toggleMode;
    }


    public void startWallJump() {
        if (!WallJumpUtil.canWallJump(player))
            return;

        wallJumping = true;
        wallJumping = true;
        lastFacing = WallJumpUtil.getPlayerFacing(player);
        lastJumpLocation = player.getLocation();
        if (remainingJumps > 0)
            remainingJumps--;

        //play sound and spawn particles
        WallJumpUtil.playWallJumpSound(player, lastFacing);
        WallJumpUtil.spawnSlidingParticles(player, 5, lastFacing);

        //stop the player from falling and moving while on the wall
        //or make them slide down
        velocityY = 0;
/*        if(BukkitUtils.isVersionBefore(BukkitUtils.Version.V1_9))
            velocityY = 0.04f;*/
        velocityTask = Bukkit.getScheduler().runTaskTimerAsynchronously(manager.getPlugin(), () -> {
            player.setVelocity(new Vector(0, velocityY, 0));
            if (velocityY != 0) {
                WallJumpUtil.spawnSlidingParticles(player, 2, lastFacing);
                if (sliding) {
                    if (player.isOnGround() || !WallJumpUtil.getBlockPlayerIsStuckOn(player, lastFacing).isSolid()) {
                        Bukkit.getScheduler().runTask(manager.getPlugin(), () -> {
                            player.setFallDistance(0);
                            player.teleport(player.getLocation());
                            stopWallJump(false);
                        });
                    }
                    if (lastJumpLocation.getY() - player.getLocation().getY() >= 1.2) {
                        lastJumpLocation = player.getLocation();
                        WallJumpUtil.playWallJumpSound(player, lastFacing);
                    }
                }
            }
        }, 0, 1);

        //make the player fall | slide when the time runs out
        if (fallTask != null)
            fallTask.cancel();
        fallTask = Bukkit.getScheduler().runTaskLaterAsynchronously(manager.getPlugin(), () -> {
            if (wallJumping) {
                if (config.isSlideEnable()) {
                    velocityY = (float) -config.getSlideSpeed();
                    sliding = true;
                } else {
                    Bukkit.getScheduler().runTask(manager.getPlugin(), (Runnable) this::stopWallJump);
                }
            }
        }, (long) (config.getTimeOnWall() * 20));

        //cancel the task for resetting wall jumping if the player wall jumps
        if (stopWallJumpingTask != null)
            stopWallJumpingTask.cancel();
    }

    public void stopWallJump() {
        stopWallJump(true);
    }

    public void stopWallJump(boolean jump) {
        wallJumping = false;
        sliding = false;

        //allow the player to move again
        player.setFallDistance(0);
        velocityTask.cancel();

        //if the player is not sliding or can jump while sliding and is not looking down
        if (jump &&// !event.isCancelled() &&
                ((velocityY == 0 && player.getLocation().getPitch() < 85) ||
                        (config.isSlideCanJumpWhile() && player.getLocation().getPitch() < 60))) {
            //push the player in the direction that they are looking
            WallJumpUtil.pushPlayerInFront(player,
                    config.getJumpPowerHorizontal(),
                    config.getJumpPowerVertical());
        }
        //after 1.5 seconds, if the player hasn't wall jumped again, reset everything
        Bukkit.getScheduler().runTaskLaterAsynchronously(manager.getPlugin(), () -> {
            if (WallJumpUtil.isOnGround(player)) {
                reset();
            }
        }, 12);
        stopWallJumpingTask = Bukkit.getScheduler().runTaskLaterAsynchronously(manager.getPlugin(), this::reset, 24);
    }

    private void reset() {
        wallJumping = false;

        lastFacing = null;
        lastJumpLocation = null;
        remainingJumps = config.getMaxJump();
        if (remainingJumps == 0)
            remainingJumps = -1;
        if (stopWallJumpingTask != null)
            stopWallJumpingTask.cancel();
        stopWallJumpingTask = null;
    }
}
