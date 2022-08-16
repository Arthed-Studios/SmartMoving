package com.github.ipecter.nms;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface NmsPackets {

    void spawnFakeBlocks(Player player, Block block, Block floorBlock);

    void removeFakeBlocks(Player player);

}
