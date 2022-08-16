package com.github.ipecter.smartmoving;

import com.github.ipecter.nms.NmsPackets;
import com.github.ipecter.smartmoving.impl.WorldGuardImplementation;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class SmartMovingManager {
    protected final HashMap<Player, SMPlayer> players = new HashMap<>();
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

    public void addPlayer(Player player) {
        if (!players.containsKey(player)) {
            players.put(player, new SMPlayer(player));
        }
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
        if (!this.players.containsKey(player)) {
            this.players.put(player, new SMPlayer(player));
        }
    }

    public void stopCrawling(Player player) {
        this.players.remove(player);
    }

    public boolean isCrawling(Player player) {
        return player.isSwimming() && this.players.containsKey(player);
    }

    public SMPlayer getPlayerCrawling(Player player) {
        return players.get(player);
    }

    private static class InnerInstanceClass {
        private static final SmartMovingManager instance = new SmartMovingManager();
    }


}
