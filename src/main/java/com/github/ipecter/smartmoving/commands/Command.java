package com.github.ipecter.smartmoving.commands;

import com.github.ipecter.rtu.pluginlib.RTUPluginLib;
import com.github.ipecter.rtu.pluginlib.managers.TextManager;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Command implements CommandExecutor, TabCompleter {

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();
    private final TextManager textManager = RTUPluginLib.getTextManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("smartmoving.reload")) {
                configManager.initConfigFiles();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    smartMovingManager.removePlayer(player);
                    smartMovingManager.addPlayer(player);
                }
                sender.sendMessage(textManager.formatted(sender instanceof Player ? (Player) sender : null, configManager.getTranslation("prefix") + configManager.getTranslation("reloadMsg")));
            } else {
                sender.sendMessage(textManager.formatted(sender instanceof Player ? (Player) sender : null, configManager.getTranslation("prefix") + configManager.getTranslation("noPermission")));
            }
        } else {
            sender.sendMessage(textManager.formatted(sender instanceof Player ? (Player) sender : null, configManager.getTranslation("prefix") + configManager.getTranslation("commandWrongUsage")));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("smartmoving.reload")) {
            return List.of("reload");
        }
        return List.of();
    }
}
