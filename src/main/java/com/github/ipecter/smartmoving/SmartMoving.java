package com.github.ipecter.smartmoving;

import com.github.ipecter.rtu.utilapi.RTUUtilAPI;
import com.github.ipecter.smartmoving.commands.Command;
import com.github.ipecter.smartmoving.impl.WorldGuardImplementation;
import com.github.ipecter.smartmoving.listeners.*;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.nms.LegacyIndependentNmsPackets;
import com.github.ipecter.smartmoving.nms.VersionIndependentNmsPackets;
import com.github.ipecter.smartmoving.utils.Version;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmartMoving extends JavaPlugin {

    private String prefix = IridiumColorAPI.process("<GRADIENT:9ba832>[ SmartMoving ]</GRADIENT:a3a3a3> ");

    private final static Version bukkitVersion = new Version(Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf("-")));
    private final static Version maxSupportedVersion = new Version("1.20");
    private final static Version minSupportedVersion = new Version("1.14");

    private static boolean isLegacy() {
        try {
            // The minecraft server version was removed from the package name on newer versions.
            Class.forName("net.minecraft.server.MinecraftServer");
            return false;
        } catch (ClassNotFoundException e) {
            // Class was not found, therefore packages still have different names on each version.
            return true;
        }
    }

    @Override
    public void onEnable() {
        try {
            //Checking if version is lower than 1.14
            if (bukkitVersion.compareTo(minSupportedVersion) < 0) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSorry, This plugin works only on 1.14 or higher versions."));
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            loadNMS();
            RTUUtilAPI.init(this);
            registerEvent();
            setExecutor();
            ConfigManager.getInstance().initConfigFiles();
            Bukkit.getLogger().info(RTUUtilAPI.getTextManager().formatted(prefix + "&aEnable&f!"));

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Bukkit.getPluginManager().registerEvents(new SneakingListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new SwimmingToggleListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);

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
        if (isLegacy()) {
            SmartMovingManager.getInstance().nmsPacketManager = new LegacyIndependentNmsPackets(Bukkit.getWorlds().get(0));
        } else {
            SmartMovingManager.getInstance().nmsPacketManager = new VersionIndependentNmsPackets(Bukkit.getWorlds().get(0));

            //Checking if current version was not tested yet
            if (bukkitVersion.compareTo(maxSupportedVersion) > 0) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe plugin was not made for this version, proceed with caution."));
            }
        }
    }


}
