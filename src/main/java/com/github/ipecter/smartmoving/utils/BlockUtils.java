package com.github.ipecter.smartmoving.utils;

import com.github.ipecter.smartmoving.SmartMoving;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public class BlockUtils {

    public static boolean isSolid(Block block) {
        return !block.isPassable();
    }

    public static Sound getBlockSound(Block block, String sound) {
        if(SmartMoving.usesPaper())
            return PaperUtils.getBlockSound(block, sound);
        else
            //TODO: Get sounds without paper
            return Sound.BLOCK_STONE_STEP;
    }

}
