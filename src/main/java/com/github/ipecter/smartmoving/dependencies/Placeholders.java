package com.github.ipecter.smartmoving.dependencies;

import com.github.ipecter.smartmoving.SmartMoving;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    private SmartMoving plugin;

    public Placeholders(SmartMoving plugin) {
        this.plugin = plugin;
    }

    public boolean persist() {
        return true;
    }

    public boolean canRegister() {
        return true;
    }

    public String getAuthor() {
        return "IPECTER";
    }

    public String getIdentifier() {
        return "smartmoving";
    }

    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        Player player = offlinePlayer.getPlayer();
        if (player.isOnline()) {

        }
        return "Player is offline!";
    }
}