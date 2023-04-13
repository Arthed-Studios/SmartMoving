package com.github.ipecter.smartmoving.dependencies;

import com.github.ipecter.smartmoving.SmartMoving;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    private final SmartMoving plugin;
    private final ConfigManager configManager = ConfigManager.getInstance();
    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

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
        return plugin.getPluginMeta().getVersion();
    }

    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        String[] name = params.split("_");
        if (name[0].equals("walljump")) {
            if (name.length > 1 && !name[1].isEmpty()) {
                if (name[1].equals("remaining")) {
                    if (name.length > 2 && !name[2].isEmpty()) {
                        if (name[2].equals("jumps")) {
                            if (isOnline(offlinePlayer)) {
                                int maxJump = configManager.getMaxJump();
                                if (maxJump == 0) return "∞";
                                int jump = smartMovingManager.getPlayer(Bukkit.getPlayer(offlinePlayer.getUniqueId())).getRemainingJumps();
                                return jump < 0 ? String.valueOf(maxJump) : String.valueOf(jump);
                            } else {
                                return "Player is Offline!";
                            }
                        }
                    }
                }
            }
        }
        return "ERROR: " + params;
    }

    public boolean isOnline(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline()) {
            Player player = Bukkit.getPlayer(offlinePlayer.getUniqueId());
            return smartMovingManager.getPlayer(player) != null;
        }
        return false;
    }
}