package com.github.ipecter.smartmoving;

import com.github.ipecter.rtu.utilapi.RTUUtilAPI;
import com.github.ipecter.rtu.utilapi.managers.VersionManager;
import com.github.ipecter.smartmoving.commands.Command;
import com.github.ipecter.smartmoving.impl.WorldGuardImplementation;
import com.github.ipecter.smartmoving.listeners.*;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.nms.LegacyIndependentNmsPackets;
import com.github.ipecter.smartmoving.nms.VersionIndependentNmsPackets;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class SmartMoving extends JavaPlugin {

    private String prefix = IridiumColorAPI.process("<GRADIENT:9ba832>[ SmartMoving ]</GRADIENT:a3a3a3> ");
    private VersionManager versionManager = RTUUtilAPI.getVersionManager();

    public final static void debug(String debugMessage) {
        getPlugin(SmartMoving.class).getLogger().log(Level.FINEST, debugMessage);
    }

    @Override
    public void onDisable() {
        clearBlock();
        Bukkit.getLogger().info(RTUUtilAPI.getTextManager().formatted(prefix + "&cDisable&f!"));
    }

    @Override
    public void onLoad() {
        loadDependencies();
    }

    @Override
    public void onEnable() {
        RTUUtilAPI.init(this);
        loadNMS();
        registerEvent();
        setExecutor();
        ConfigManager.getInstance().initConfigFiles();
        Bukkit.getLogger().info(RTUUtilAPI.getTextManager().formatted(prefix + "&aEnable&f!"));
    }

    private void clearBlock() {
        for (Player playerCrawling : SmartMovingManager.getInstance().players.keySet()) {
            Block blockAbovePlayer = playerCrawling.getLocation().add(0, 1.5, 0).getBlock();
            playerCrawling.sendBlockChange(blockAbovePlayer.getLocation(), blockAbovePlayer.getBlockData());
            blockAbovePlayer.getState().update();
        }
    }

    private void setExecutor() {
        getCommand("smartmoving").setExecutor(new Command());
    }

    private void registerEvent() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJump(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerToggleSneak(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerToggleSwim(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeath(), this);

    }

    private void loadPAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            RTUUtilAPI.getDependencyManager().setUsePAPI(true);
        }
    }

    private void loadDependencies() {
        loadPAPI();
        loadWG();
    }

    private void loadWG() {
        Plugin worldGuardPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin != null) {
            SmartMovingManager.getInstance().worldGuard = new WorldGuardImplementation(worldGuardPlugin, this);
        }
    }

    private void loadNMS() {
        //Checking which NmsPacketManager should be used.
        if (versionManager.isLegacy()) {
            SmartMovingManager.getInstance().nmsPacketManager = new LegacyIndependentNmsPackets(Bukkit.getWorlds().get(0));
        } else {
            SmartMovingManager.getInstance().nmsPacketManager = new VersionIndependentNmsPackets(Bukkit.getWorlds().get(0));

            //Checking if current version was not tested yet
            if (versionManager.isSupportVersion("v1_14_R1", "v1_19_R1")) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe plugin was not made for this version, proceed with caution."));
            }
        }
    }
}
