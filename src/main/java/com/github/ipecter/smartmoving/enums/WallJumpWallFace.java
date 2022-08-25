package com.github.ipecter.smartmoving.enums;

import org.bukkit.block.BlockFace;

public enum WallJumpWallFace {

    NORTH(0, 0, -1, 1.42f),
    SOUTH(0, 0, 1, 0.42f),
    WEST(-1, 0, 0, 1.42f),
    EAST(1, 0, 0, 0.42f);


    public final int xOffset;
    public final int yOffset;
    public final int zOffset;

    public final float distance;

    WallJumpWallFace(int xOffset, int yOffset, int zOffset, float distance) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.distance = distance;
    }

    public static WallJumpWallFace fromBlockFace(BlockFace blockFace) {
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
