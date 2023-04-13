package com.github.ipecter.smartmoving;

import com.github.ipecter.nms.NmsPackets;
import com.github.ipecter.smartmoving.dependencies.WorldGuard;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SmartMovingManager {

    protected final Map<Player, SMPlayer> players = Collections.synchronizedMap(new HashMap<>());
    @Getter
    private final Plugin plugin = SmartMoving.getPlugin(SmartMoving.class);
    @Getter
    protected NmsPackets nmsPacketManager;
    @Getter
    protected WorldGuard worldGuard;

    public final static SmartMovingManager getInstance() {
        return SmartMovingManager.InnerInstanceClass.instance;
    }

    public SMPlayer addPlayer(Player player) {
        SMPlayer smPlayer = new SMPlayer(player);
        players.put(player, smPlayer);
        return smPlayer;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }


    public SMPlayer getPlayer(Player player) {
        return players.get(player);
    }

    public Collection<SMPlayer> getPlayers() {
        return players.values();
    }

    private static class InnerInstanceClass {
        private static final SmartMovingManager instance = new SmartMovingManager();
    }
}
