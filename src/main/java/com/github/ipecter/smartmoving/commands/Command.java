package com.github.ipecter.smartmoving.commands;

import com.github.ipecter.rtu.pluginlib.RTUPluginLib;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Command implements CommandExecutor, TabCompleter {

    private ConfigManager configManager = ConfigManager.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("smartmoving.reload")) {
                configManager.initConfigFiles();
                sender.sendMessage(RTUPluginLib.getTextManager().formatted(sender instanceof Player ? (Player) sender : null, configManager.getTranslation("prefix") + configManager.getTranslation("reloadMsg")));
            } else {
                sender.sendMessage(RTUPluginLib.getTextManager().formatted(sender instanceof Player ? (Player) sender : null, configManager.getTranslation("prefix") + configManager.getTranslation("noPermission")));
            }
            return true;
        } else {
            sender.sendMessage(RTUPluginLib.getTextManager().formatted(sender instanceof Player ? (Player) sender : null, configManager.getTranslation("prefix") + configManager.getTranslation("commandWrongUsage")));
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("smartmoving.reload")) {
            return Arrays.asList("reload");
        }
        return Arrays.asList();
    }
}
