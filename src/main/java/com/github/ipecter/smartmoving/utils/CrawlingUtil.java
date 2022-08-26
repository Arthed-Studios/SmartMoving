package com.github.ipecter.smartmoving.utils;

import com.github.ipecter.smartmoving.SmartMoving;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.dependencies.WorldGuard;
import com.github.ipecter.smartmoving.enums.CrawlingWallFace;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CrawlingUtil {

    private static final Plugin plugin = SmartMoving.getPlugin(SmartMoving.class);
    private static final WorldGuard worldGuard = SmartMovingManager.getInstance().getWorldGuard();
    private static final ConfigManager configManager = ConfigManager.getInstance();
    public static BlockData BARRIER_BLOCK_DATA = Bukkit.createBlockData(Material.BARRIER);

    public static void revertBlockPacket(Player player, final Block block) {
        player.sendBlockChange(block.getLocation(), block.getBlockData());
        Bukkit.getScheduler().runTask(plugin, () -> block.getState().update());
    }

    public static boolean canCrawl(Player player) {
        if (!player.hasPermission("smartmoving.crawling.use")) {
            return false;
        }
        if (worldGuard != null)
            if (!worldGuard.canCrawl(player))
                return false;
        Block playerBelowBlock = player.getLocation().clone().subtract(0, 0.4, 0).getBlock();
        boolean isOnBlacklistedBlock = configManager.getCrawlingBlockBlackList().contains(playerBelowBlock.getType().name().toUpperCase());
        if (isOnBlacklistedBlock) return false;
        boolean isInBlacklistedWorld = configManager.getCrawlingWorldBlackList().contains(player.getWorld());
        if (isInBlacklistedWorld) return false;
        return check(player);
    }

    public static boolean canCrawlCancel(Player player) {
        if (!player.hasPermission("smartmoving.crawling.use")) {
            return false;
        }
        if (worldGuard != null)
            if (!worldGuard.canCrawl(player))
                return false;
        Block playerBelowBlock = player.getLocation().clone().subtract(0, 0.4, 0).getBlock();
        boolean isOnBlacklistedBlock = configManager.getCrawlingBlockBlackList().contains(playerBelowBlock.getType().name().toUpperCase());
        if (isOnBlacklistedBlock) return false;
        boolean isInBlacklistedWorld = configManager.getCrawlingWorldBlackList().contains(player.getWorld());
        if (isInBlacklistedWorld) return false;
        if (isOnGround(player)) return true;
        return check(player);
    }

    public static boolean check(Player player) {
        boolean value1 = false;
        boolean value2 = false;
        if (isOnGround(player)) {
            value1 = true;
        }
        if (checkBlock(player)) {
            value2 = true;
        }
        return value1 && value2;
    }

    public static boolean isOnGround(Player player) {
        return !player.isFlying() && player.isOnGround() && !player.isInsideVehicle();
    }

    public static boolean checkBlock(Player player) {
        return checkAbove(player) && checkLeg(player);
    }

    public static boolean checkAbove(Player player) {
        Block block = player.getLocation().add(0, 1.5, 0).getBlock();
        if (player.getLocation().toBlockLocation().equals(player.getEyeLocation().toBlockLocation())) return true;
        return checkAbove(block);
    }

    public static boolean checkLeg(Player player) {
        Block block = player.getLocation().getBlock();
        return checkLeg(block);
    }

    public static boolean checkAbove(Block block) {
        if (block.getType().isAir()) return true;
        if (block.isSolid()) return false;
        if (block.isLiquid()) return false;
        return true;
    }

    public static boolean checkLeg(Block block) {
        if (block.getType().isAir()) return true;
        if (block.isSolid()) return true;
        if (block.isPassable()) return true;
        if (block.isCollidable()) return false;
        if (block.isLiquid()) return false;
        return true;
    }

    public static boolean isInFrontOfATunnel(Player player) {
        CrawlingWallFace facing = CrawlingWallFace.fromBlockFace(player.getFacing());

        Location location = player.getLocation().add(facing.xOffset, 1, facing.zOffset);
        Block block = location.getBlock();
        double distanceLimit = facing.distance;

        if (block.getType().isSolid() && location.subtract(0, 1, 0).getBlock().isPassable()) {
            if (facing.equals(CrawlingWallFace.EAST) || facing.equals(CrawlingWallFace.WEST)) {
                return Math.abs(location.getX() - block.getX()) < distanceLimit;
            } else {
                return Math.abs(location.getZ() - block.getZ()) < distanceLimit;
            }
        }

        return false;
    }
}
