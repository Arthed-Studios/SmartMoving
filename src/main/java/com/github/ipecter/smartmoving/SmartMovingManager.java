package com.github.ipecter.smartmoving;

import com.github.ipecter.nms.NmsPackets;
import com.github.ipecter.smartmoving.dependencies.WorldGuard;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SmartMovingManager {

    protected final Map<Player, SMPlayer> players = Collections.synchronizedMap(new HashMap<>());

    protected NmsPackets nmsPacketManager;
    protected WorldGuard worldGuard;
    private Plugin plugin = SmartMoving.getPlugin(SmartMoving.class);

    public SmartMovingManager() {
    }

    public final static SmartMovingManager getInstance() {
        return SmartMovingManager.InnerInstanceClass.instance;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public SMPlayer addPlayer(Player player) {
        players.put(player, new SMPlayer(player));
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public NmsPackets getNmsPacketManager() {
        return nmsPacketManager;
    }

    public WorldGuard getWorldGuard() {
        return this.worldGuard;
    }

    public SMPlayer getPlayer(Player player) {
        return players.get(player);
    }

    public void startCrawling(Player player) {
        if (!players.containsKey(player)) {
            SMPlayer smPlayer = new SMPlayer(player);
            smPlayer.setCrawling(true);
            players.put(player, smPlayer);
        } else {
            SMPlayer smPlayer = getPlayer(player);
            smPlayer.setCrawling(true);
            players.put(player, smPlayer);
        }
    }

    public void stopCrawling(Player player) {
        getPlayer(player).stopCrawling();
    }

    public boolean isCrawling(Player player) {
        SMPlayer smPlayer = players.get(player);
        if (smPlayer != null) {
            return smPlayer.isCrawling();
        }
        return false;
    }

    public void startWallJump(Player player) {
        if (!players.containsKey(player)) {
            SMPlayer smPlayer = new SMPlayer(player);
            smPlayer.setWallJumping(true);
            players.put(player, smPlayer);
        } else {
            SMPlayer smPlayer = getPlayer(player);
            smPlayer.setWallJumping(true);
            players.put(player, smPlayer);
        }
    }

    public void stopWallJump(Player player) {
        getPlayer(player).stopWallJump();
    }

    public boolean isWallJumping(Player player) {
        SMPlayer smPlayer = players.get(player);
        if (smPlayer != null) {
            return smPlayer.isWallJumping();
            return false;
        }

        private static class InnerInstanceClass {
            private static final SmartMovingManager instance = new SmartMovingManager();
        }


    }
