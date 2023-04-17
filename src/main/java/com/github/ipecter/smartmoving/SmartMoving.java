package com.github.ipecter.smartmoving;

import com.github.ipecter.rtu.pluginlib.RTUPluginLib;
import com.github.ipecter.rtu.pluginlib.managers.TextManager;
import com.github.ipecter.rtu.pluginlib.managers.VersionManager;
import com.github.ipecter.smartmoving.commands.Command;
import com.github.ipecter.smartmoving.dependencies.Placeholders;
import com.github.ipecter.smartmoving.dependencies.WorldGuard;
import com.github.ipecter.smartmoving.listeners.*;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.nms.VersionIndependentNmsPackets;
import lombok.extern.java.Log;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@Log
public class SmartMoving extends JavaPlugin {

    public static final Component prefix = RTUPluginLib.getTextManager().colored("<gradient:#47cc1f:#a3a3a3>[ SmartMoving ]</gradient> ");
    private final VersionManager versionManager = RTUPluginLib.getVersionManager();
    private final TextManager textManager = RTUPluginLib.getTextManager();

    private static boolean usePaper;
    public static boolean usesPaper() {
        return usePaper;
    }

    public static void debug(String debugMessage) {
        if (ConfigManager.getInstance().isDebug()) {
            log.info(debugMessage);
        }
    }

    @Override
    public void onDisable() {
        clearBlock();
        Bukkit.getLogger().info(textManager.toString(prefix.append(textManager.colored("<red>Disable</red>!"))));
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
        if (!(hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration")))
            usePaper = false;
        else
            usePaper = true;
        if (!versionManager.isSupportVersion("v1_14_R1", "v1_19_R3")) {
            Bukkit.getLogger().info(textManager.toString(prefix.append(textManager.colored("<red>This plugin works only on 1.14 or higher versions.</red>"))));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        loadNMS();
        registerEvent();
        setExecutor();
        ConfigManager.getInstance().initConfigFiles();
        SmartMovingManager manager = SmartMovingManager.getInstance();
        for (Player player : Bukkit.getOnlinePlayers()) {
            manager.addPlayer(player);
        }
        Bukkit.getLogger().info(textManager.toString(prefix.append(textManager.colored("<green>Enable</green>!"))));
    }

    private boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void clearBlock() {
        for (Player playerCrawling : SmartMovingManager.getInstance().players.keySet()) {
            Block blockAbovePlayer = playerCrawling.getLocation().add(0, 1.5, 0).getBlock();
            playerCrawling.sendBlockChange(blockAbovePlayer.getLocation(), blockAbovePlayer.getBlockData());
            blockAbovePlayer.getState().update();
        }
    }

    private void setExecutor() {
        Objects.requireNonNull(getCommand("smartmoving")).setExecutor(new Command());
    }

    private void registerEvent() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuit(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerToggleSneak(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerToggleSwim(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeath(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamage(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractBlock(), this);

    }

    private void loadPAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            RTUPluginLib.getDependencyManager().setUsePAPI(true);
            new Placeholders(this);
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
        World world = Bukkit.getWorlds().get(0);
        //Checking which NmsPacketManager should be used.
        if (versionManager.isLegacy()) {
            SmartMovingManager.getInstance().nmsPacketManager = new LegacyIndependentNmsPackets(world);
        } else {
            SmartMovingManager.getInstance().nmsPacketManager = new VersionIndependentNmsPackets(world);
        }
    }
}
