package com.github.ipecter.smartmoving.utils;

import com.github.ipecter.smartmoving.enums.WallJumpWallFace;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WallJumpUtil {

    public static boolean canWallJump(Player player) {
        WallJumpWallFace facing = getPlayerFacing(player);
        WallJumpWallFace lastFacing;
        Location lastJumpLocation;
        int remainingJumps;
        if (lastJumpLocation != null)
            //used so height doesn't matter when calculating distance between the players location and the last jump location
            lastJumpLocation.setY(player.getLocation().getY());
        if (
                onWall || //player is already stuck to an wall
                        remainingJumps == 0 || //player reached jump limit
                        (config.getBoolean("requireDirectionChange") && lastFacing != null && lastFacing.equals(facing)) || //player is facing the same direction as the last jump
                        (lastJumpLocation != null && player.getLocation().distance(lastJumpLocation) <= config.getDouble("minimumDistance")) ||  //player is too close to the last jump location
                        player.getVelocity().getY() < config.getDouble("maximumVelocity") || //player is falling too fast
                        (config.getBoolean("needPermission") && !player.hasPermission("walljump.use")) || //player does not have the permission to wall-jump
                        (worldGuard != null && !worldGuard.canWallJump(player)) //wall-jumping is not allowed in the region the player is in
        )

            return false;
        //check if the block the player is wall jumping on is blacklisted
        boolean onBlacklistedBlock = config.getMaterialList("blacklistedBlocks").contains(
                player.getLocation().clone().add(facing.xOffset,
                        facing.yOffset,
                        facing.zOffset)
                        .getBlock()
                        .getType());
        boolean reverseBlockBlacklist = config.getBoolean("reversedBlockBlacklist");
        if ((!reverseBlockBlacklist && onBlacklistedBlock) ||
                (reverseBlockBlacklist && !onBlacklistedBlock))
            return false;

        //check if the world the player is in is blacklisted
        boolean inBlacklistedWorld = config.getWorldList("blacklistedWorlds").contains(
                player.getWorld());
        boolean reverseWorldBlacklist = config.getBoolean("reversedWorldBlacklist");
        if ((!reverseWorldBlacklist && inBlacklistedWorld) ||
                (reverseWorldBlacklist && !inBlacklistedWorld))
            return false;

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
