package com.github.ipecter.smartmoving.utils;

import com.github.ipecter.smartmoving.SMPlayer;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.dependencies.WorldGuard;
import com.github.ipecter.smartmoving.enums.WallJumpWallFace;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WallJumpUtil {

    private final static SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();
    private final static ConfigManager config = ConfigManager.getInstance();
    private final static WorldGuard worldGuard = smartMovingManager.getWorldGuard();

    public static boolean canWallJump(Player player) {
        WallJumpWallFace facing = getPlayerFacing(player);
        SMPlayer smPlayer = new SMPlayer(player);
        boolean onWall = smPlayer.isOnWall();

        int remainingJumps = smPlayer.getRemainingJumps();
        WallJumpWallFace lastFacing = smPlayer.getLastFacing();
        Location lastJumpLocation = smPlayer.getLastJumpLocation();

        if (lastJumpLocation != null)
            //used so height doesn't matter when calculating distance between the players location and the last jump location
            lastJumpLocation.setY(player.getLocation().getY());
        if (
                onWall || //player is already stuck to an wall
                        remainingJumps == 0 || //player reached jump limit
                        (config.isRequireDirectionChange() && lastFacing != null && lastFacing.equals(facing)) || //player is facing the same direction as the last jump
                        (lastJumpLocation != null && player.getLocation().distance(lastJumpLocation) <= config.getMinimumDistance()) ||  //player is too close to the last jump location
                        player.getVelocity().getY() < config.getMaximumVelocity() || //player is falling too fast
                        (!player.hasPermission("smartmoving.walljump.use")) || //player does not have the permission to wall-jump
                        (worldGuard != null && !worldGuard.canWallJump(player)) //wall-jumping is not allowed in the region the player is in
        ) {
            SmartMovingManager.getInstance().getPlugin().getLogger().info("a1");
            return false;
        }
        //check if the block the player is wall jumping on is blacklisted
        boolean onBlacklistedBlock = config.getWallJumpBlockList().contains(
                player.getLocation().clone().add(facing.xOffset,
                        facing.yOffset,
                        facing.zOffset)
                        .getBlock()
                        .getType().name());
        boolean isBlockBlackListMode = config.isWallJumpBlockBlackList();
        if ((!isBlockBlackListMode && !onBlacklistedBlock) ||
                (isBlockBlackListMode && onBlacklistedBlock)) {
            SmartMovingManager.getInstance().getPlugin().getLogger().info("a2" + onBlacklistedBlock + " / " + isBlockBlackListMode);
            SmartMovingManager.getInstance().getPlugin().getLogger().info("a2" + player.getLocation().clone().add(facing.xOffset,
                    facing.yOffset,
                    facing.zOffset)
                    .getBlock()
                    .getType().name() + " / " + config.getWallJumpBlockList());
            return false;
        }


        //check if the world the player is in is blacklisted
        boolean inBlacklistedWorld = config.getWallJumpWorldList().contains(
                player.getWorld());
        boolean isWorldBlackListMode = config.isWallJumpWorldBlackList();
        if ((!isWorldBlackListMode && !inBlacklistedWorld) ||
                (isWorldBlackListMode && inBlacklistedWorld)) {
            SmartMovingManager.getInstance().getPlugin().getLogger().info("a3");
            return false;
        }
        return true;
    }

    public static void spawnSlidingParticles(Player player, int count, WallJumpWallFace facing) {
        Object data;
        Location location = player.getLocation();
        Block block = location.clone().add(facing.xOffset, facing.yOffset, facing.zOffset).getBlock();
        data = block.getBlockData();
        player.getWorld().spawnParticle(
                Particle.BLOCK_DUST,
                location.clone().add(facing.xOffset * 0.3, facing.yOffset * 0.3 - 0.3, facing.zOffset * 0.3),
                count,
                0.2f,
                0.2f,
                0.2f,
                data);
    }

    public static void playWallJumpSound(Player player, WallJumpWallFace facing) {
        player.getWorld().playSound(player.getLocation(),
                getBlockPlayerIsStuckOn(player, facing).getBlockSoundGroup().getStepSound(),
                1.0f, 1.0f);
    }

    public static boolean isTouchingAWall(Player player) {
        WallJumpWallFace facing = getPlayerFacing(player);

        Location location = player.getLocation();
        Block block = location.clone().add(facing.xOffset, facing.yOffset, facing.zOffset).getBlock();
        float distanceLimit = facing.distance;

        if (block.getType().isSolid()) {
            if (facing.equals(WallJumpWallFace.EAST) || facing.equals(WallJumpWallFace.WEST))
                return Math.abs(location.getX() - block.getX()) < distanceLimit;
            else
                return Math.abs(location.getZ() - block.getZ()) < distanceLimit;
        }

        return false;
    }

    public static boolean isOnGround(Player player) {
        return player.getLocation().clone().subtract(0, 0.2, 0).getBlock().getType().isSolid();
    }

    public static Block getBlockPlayerIsStuckOn(Player player, WallJumpWallFace facing) {
        return player.getLocation().clone().add(facing.xOffset, facing.yOffset, facing.zOffset).getBlock();
    }

    public static WallJumpWallFace getPlayerFacing(Player player) {
        return WallJumpWallFace.fromBlockFace(player.getFacing());
    }

    public static void pushPlayerInFront(Player player, double horizontalPower, double verticalPower) {
        Vector velocity = player.getLocation().getDirection().normalize().multiply(horizontalPower);
        velocity.setY(verticalPower);
        player.setVelocity(velocity);
    }
}
