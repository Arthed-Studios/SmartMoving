package com.github.ipecter.smartmoving.utils;

import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;

public class PaperUtils {

    public static Sound getBlockSound(Block block, String sound) {
        SoundGroup soundGroup = block.getBlockSoundGroup();
        return switch (sound) {
            case "step" -> soundGroup.getStepSound();
            case "break" -> soundGroup.getBreakSound();
            case "place" -> soundGroup.getPlaceSound();
            case "hit" -> soundGroup.getHitSound();
            case "fall" -> soundGroup.getFallSound();
            default -> soundGroup.getStepSound();
        };
    }

}
