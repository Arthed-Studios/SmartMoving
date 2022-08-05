package com.github.ipecter.smartmoving;

import com.github.ipecter.rtu.utilapi.RTUUtilAPI;
import com.github.ipecter.smartmoving.commands.Command;
import com.github.ipecter.smartmoving.listeners.PlayerJoin;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmartMoving extends JavaPlugin {

    private String prefix = IridiumColorAPI.process("<GRADIENT:9ba832>[ SmartMoving ]</GRADIENT:a3a3a3> ");

    @Override
    public void onEnable() {
        Bukkit.getLogger().info(RTUUtilAPI.getTextManager().formatted(prefix + "&aEnable&f!"));

    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(RTUUtilAPI.getTextManager().formatted(prefix + "&cDisable&f!"));

    }

    private void registerEvent() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(), this);
    }

    private void setExecutor() {
        getCommand("smartmoving").setExecutor(new Command());
    }

    private void loadDependencies() {
        loadPAPI();
    }

    private void loadPAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            RTUUtilAPI.getDependencyManager().setUsePAPI(true);
        }
    }

}
