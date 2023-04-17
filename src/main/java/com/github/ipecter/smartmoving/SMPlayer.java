package com.github.ipecter.smartmoving;

import com.github.ipecter.nms.NmsPackets;
import com.github.ipecter.smartmoving.enums.WallJumpWallFace;
import com.github.ipecter.smartmoving.events.CrawlingStartEvent;
import com.github.ipecter.smartmoving.events.CrawlingStopEvent;
import com.github.ipecter.smartmoving.events.WallJumpStartEvent;
import com.github.ipecter.smartmoving.events.WallJumpStopEvent;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.utils.CrawlingUtil;
import com.github.ipecter.smartmoving.utils.WallJumpUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

@Getter
@Setter
public class SMPlayer {

    private final static SmartMovingManager manager = SmartMovingManager.getInstance();
    private final static ConfigManager config = ConfigManager.getInstance();
    private final static NmsPackets nmsPacketManager = manager.getNmsPacketManager();

    //[ General Part ]
    private final Player player;
    private boolean wallJumping;
    private boolean crawling;

    //[ Crawling Part ]
    //Movement tasks
    private BukkitTask moveTask;
    private Block barrierBlock;
    private BukkitTask canCrawlTask;
    private BukkitTask holdCheckTask;
    private boolean holdCheck;

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

    public SMPlayer(Player player) {
        this.player = player;
    }

    public void startCrawling() {
        if (!CrawlingUtil.canCrawl(player)) {
            this.stopCrawling();
            return;
        }
        CrawlingStartEvent crawlingEvent = new CrawlingStartEvent(this);
        Bukkit.getPluginManager().callEvent(crawlingEvent);
        if (crawlingEvent.isCancelled()) return;
        barrierBlock = player.getLocation().getBlock();
        player.setSwimming(true);
        holdCheck = false;
        holdCheckTask = Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
            holdCheck = true;
        }, 10);
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
        }, 0, config.getMovementTaskDelay());

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
            } else if (this.player.getVelocity().getY() > 0 && this.player.getNoDamageTicks() == 0) {
                Bukkit.getScheduler().runTask(manager.getPlugin(), this::stopCrawling);
            }
        }, 5, config.getMovementTaskDelay());

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
        CrawlingStopEvent crawlingEvent = new CrawlingStopEvent(this);
        Bukkit.getPluginManager().callEvent(crawlingEvent);
        player.setSwimming(false);
        if (barrierBlock != null) {
            CrawlingUtil.revertBlockPacket(player, barrierBlock);
            CrawlingUtil.revertBlockPacket(player, barrierBlock.getLocation().subtract(0, 2, 0).getBlock());
            nmsPacketManager.removeFakeBlocks(player);
        }

        holdCheck = false;
        if (holdCheckTask != null) {
            holdCheckTask.cancel();
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
        WallJumpStartEvent wallJumpEvent = new WallJumpStartEvent(this);
        Bukkit.getPluginManager().callEvent(wallJumpEvent);
        if (wallJumpEvent.isCancelled()) return;
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
        WallJumpStopEvent wallJumpEvent = new WallJumpStopEvent(this);
        Bukkit.getPluginManager().callEvent(wallJumpEvent);
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
