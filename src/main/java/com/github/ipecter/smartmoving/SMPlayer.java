package com.github.ipecter.smartmoving;

import com.github.ipecter.nms.NmsPackets;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.utils.CrawlingUtil;
import org.bukkit.Bukkit;
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
    //Every 1 Tick Task for Movement
    private BukkitTask moveTask;
    
    //[ Crawling Part ]
    private Block barrierBlock;
    
    private BukkitTask canCrawlTask;
    
    private int nonGround = 0;

    private Boolean toggleMode;

    //[ WallJump Part ]
    
    
    
    protected SMPlayer(Player player) {
        this.player = player;
        startCrawling();
    }

    public void startCrawling() {
        if (!CrawlingUtil.canCrawl(this.player)) {
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
        SmartMovingManager.getInstance().removePlayer(player);
    }

    public Boolean toggleMode() {
        return toggleMode;
    }


    public void onWallJumpStart() {
        if (!canWallJump())
            return;

        WallJumpStartEvent event = new WallJumpStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        onWall = true;
        wallJumping = true;
        lastFacing = LocationUtils.getPlayerFacing(player);
        lastJumpLocation = player.getLocation();
        if (remainingJumps > 0)
            remainingJumps--;

        //Stop some anti cheat checks that might be caused by wall-jumping
        AntiCheatUtils.stopPotentialAntiCheatChecks(player);

        //play sound and spawn particles
        EffectUtils.playWallJumpSound(player, lastFacing);
        EffectUtils.spawnSlidingParticles(player, 5, lastFacing);

        //stop the player from falling and moving while on the wall
        //or make them slide down
        velocityY = 0;
/*        if(BukkitUtils.isVersionBefore(BukkitUtils.Version.V1_9))
            velocityY = 0.04f;*/
        velocityTask = Bukkit.getScheduler().runTaskTimerAsynchronously(WallJump.getInstance(), () -> {
            player.setVelocity(new Vector(0, velocityY, 0));
            if (velocityY != 0) {
                EffectUtils.spawnSlidingParticles(player, 2, lastFacing);
                if (sliding) {
                    if (player.isOnGround() || !LocationUtils.getBlockPlayerIsStuckOn(player, lastFacing).getType().isSolid()) {
                        Bukkit.getScheduler().runTask(WallJump.getInstance(), () -> {
                            player.setFallDistance(0);
                            player.teleport(player.getLocation());
                            onWallJumpEnd(false);
                        });
                    }
                    if (lastJumpLocation.getY() - player.getLocation().getY() >= 1.2) {
                        lastJumpLocation = player.getLocation();
                        EffectUtils.playWallJumpSound(player, lastFacing);
                    }
                }
            }
        }, 0, 1);

        //make the player fall | slide when the time runs out
        if (fallTask != null)
            fallTask.cancel();
        fallTask = Bukkit.getScheduler().runTaskLaterAsynchronously(WallJump.getInstance(), () -> {
            if (onWall) {
                if (config.getBoolean("slide")) {
                    velocityY = (float) -config.getDouble("slidingSpeed");
                    sliding = true;
                } else {
                    Bukkit.getScheduler().runTask(WallJump.getInstance(), (Runnable) this::onWallJumpEnd);
                }
            }
        }, (long) (config.getDouble("timeOnWall") * 20));

        //cancel the task for resetting wall jumping if the player wall jumps
        if (stopWallJumpingTask != null)
            stopWallJumpingTask.cancel();


    }
}
