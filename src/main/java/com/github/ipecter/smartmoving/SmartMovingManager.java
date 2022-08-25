package com.github.ipecter.smartmoving;

import com.github.ipecter.nms.NmsPackets;
import com.github.ipecter.smartmoving.impl.WorldGuardImplementation;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SmartMovingManager {

    protected final Map<Player, SMPlayer> players = Collections.synchronizedMap(new HashMap<>());

    protected NmsPackets nmsPacketManager;
    protected WorldGuardImplementation worldGuard;
    private Plugin plugin = SmartMoving.getPlugin(SmartMoving.class);

    public SmartMovingManager() {
    }

    public final static SmartMovingManager getInstance() {
        return SmartMovingManager.InnerInstanceClass.instance;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public NmsPackets getNmsPacketManager() {
        return nmsPacketManager;
    }

    public WorldGuardImplementation getWorldGuard() {
        return this.worldGuard;
    }

    public void startCrawling(Player player) {
        if (!players.containsKey(player)) {
            players.put(player, new SMPlayer(player));
        }
    }

    public void stopCrawling(Player player) {
        getPlayerCrawling(player).stopCrawling();
        players.remove(player);
    }

    public boolean isCrawling(Player player) {
        return players.containsKey(player);
    }

    public SMPlayer getPlayerCrawling(Player player) {
        return players.get(player);
    }

    private static class InnerInstanceClass {
        private static final SmartMovingManager instance = new SmartMovingManager();
    }


}
