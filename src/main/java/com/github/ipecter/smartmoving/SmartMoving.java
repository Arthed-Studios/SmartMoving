package com.github.ipecter.smartmoving;

import com.github.ipecter.rtu.pluginlib.RTUPluginLib;
import com.github.ipecter.rtu.pluginlib.managers.VersionManager;
import com.github.ipecter.smartmoving.commands.Command;
import com.github.ipecter.smartmoving.dependencies.WorldGuard;
import com.github.ipecter.smartmoving.listeners.*;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.nms.LegacyIndependentNmsPackets;
import com.github.ipecter.smartmoving.nms.VersionIndependentNmsPackets;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SmartMoving extends JavaPlugin {

    private String prefix = IridiumColorAPI.process("<GRADIENT:9ba832>[ SmartMoving ]</GRADIENT:a3a3a3> ");
    private VersionManager versionManager = RTUPluginLib.getVersionManager();

    public static final void debug(String debugMessage) {
        if (ConfigManager.getInstance().isDebug()) {
            System.out.println(debugMessage);
        }
    }

    @Override
    public void onDisable() {
        clearBlock();
        Bukkit.getLogger().info(RTUPluginLib.getTextManager().formatted(prefix + "&cDisable&f!"));
        SmartMovingManager manager = SmartMovingManager.getInstance();
        for (Player player : Bukkit.getOnlinePlayers()) {
            manager.removePlayer(player);
        }
    }

    @Override
    public void onLoad() {
        RTUPluginLib.init(this);
        loadDependencies();
    }

    @Override
    public void onEnable() {
        if (!versionManager.isSupportVersion("v1_14_R1", "v1_19_R1")) {
            Bukkit.getLogger().info(RTUPluginLib.getTextManager().formatted(prefix + "&cThis plugin works only on 1.14 or higher versions."));
            Bukkit.getLogger().info(RTUPluginLib.getTextManager().formatted(prefix + "&c이 플러그인은 1.14 이상에서만 작동합니다"));
            Bukkit.getPluginManager().disablePlugin(this);
        }
        loadNMS();
        registerEvent();
        setExecutor();
        ConfigManager.getInstance().initConfigFiles();
        SmartMovingManager manager = SmartMovingManager.getInstance();
        for (Player player : Bukkit.getOnlinePlayers()) {
            manager.addPlayer(player);
        }
        Bukkit.getLogger().info(RTUPluginLib.getTextManager().formatted(prefix + "&aEnable&f!"));
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
        Bukkit.getPluginManager().registerEvents(new PlayerQuit(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJump(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerToggleSneak(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerToggleSwim(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeath(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamage(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractBlock(), this);

    }

    private void loadPAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            RTUPluginLib.getDependencyManager().setUsePAPI(true);
        }
    }

    private void loadDependencies() {
        loadPAPI();
        loadWG();
    }

    private void loadWG() {
        Plugin worldGuardPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin != null) {
            SmartMovingManager.getInstance().worldGuard = new WorldGuard(worldGuardPlugin, this);
        }
    }

    private void loadNMS() {
        //Checking which NmsPacketManager should be used.
        if (versionManager.isLegacy()) {
            SmartMovingManager.getInstance().nmsPacketManager = new LegacyIndependentNmsPackets(Bukkit.getWorlds().get(0));
        } else {
            SmartMovingManager.getInstance().nmsPacketManager = new VersionIndependentNmsPackets(Bukkit.getWorlds().get(0));
        }
    }
}
