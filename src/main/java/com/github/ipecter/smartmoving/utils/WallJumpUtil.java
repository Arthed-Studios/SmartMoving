package com.github.ipecter.smartmoving.utils;

import com.github.ipecter.smartmoving.enums.WallJumpWallFace;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class WallJumpUtil {

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
}
