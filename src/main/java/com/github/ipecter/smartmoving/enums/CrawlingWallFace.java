package com.github.ipecter.smartmoving.enums;

import org.bukkit.block.BlockFace;

public enum CrawlingWallFace {

    NORTH(0, -1, 0.31),
    SOUTH(0, 1, 0.70),
    WEST(-1, 0, 0.31),
    EAST(1, 0, 0.70);


    public final int xOffset;
    public final int zOffset;

    public final double distance;

    CrawlingWallFace(int xOffset, int zOffset, double distance) {
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.distance = distance;
    }

    public static CrawlingWallFace fromBlockFace(BlockFace blockFace) {
        switch (blockFace) {
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            default: //EAST
                return EAST;
        }
    }
}