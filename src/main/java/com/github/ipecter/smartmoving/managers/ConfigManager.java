package com.github.ipecter.smartmoving.managers;

import com.github.ipecter.rtu.utilapi.RTUUtilAPI;
import com.github.ipecter.smartmoving.SmartMoving;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private Plugin plugin = SmartMoving.getPlugin(SmartMoving.class);
    private boolean enablePlugin = true;
    private boolean motd = true;
    private String locale = "EN";
    private Map<String, List<String>> cmdList = Collections.synchronizedMap(new HashMap<>());
    private String prefix = IridiumColorAPI.process("<GRADIENT:9ba832>[ RTUCommandControl ]</GRADIENT:a3a3a3> ");
    private String reloadMsg = "";
    private String commandWrongUsage = "";
    private String noPermission = "";

    public ConfigManager() {
    }

    public final static ConfigManager getInstance() {
        return InnerInstanceClass.instance;
    }

    public boolean isEnablePlugin() {
        return enablePlugin;
    }

    public void setEnablePlugin(boolean enablePlugin) {
        this.enablePlugin = enablePlugin;
    }

    public boolean isMotd() {
        return motd;
    }

    public void setMotd(boolean motd) {
        this.motd = motd;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getReloadMsg() {
        return reloadMsg;
    }

    public void setReloadMsg(String reloadMsg) {
        this.reloadMsg = reloadMsg;
    }

    public String getCommandWrongUsage() {
        return commandWrongUsage;
    }

    public void setCommandWrongUsage(String commandWrongUsage) {
        this.commandWrongUsage = commandWrongUsage;
    }

    public String getNoPermission() {
        return noPermission;
    }

    public void setNoPermission(String noPermission) {
        this.noPermission = noPermission;
    }

    public void initConfigFiles() {
        initSetting(RTUUtilAPI.getFileManager().copyResource("Setting.yml"));
        initMessage(RTUUtilAPI.getFileManager().copyResource("Translations", "Locale_" + locale + ".yml"));
    }

    private void initSetting(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        enablePlugin = config.getBoolean("enablePlugin");
        motd = config.getBoolean("motd");
        locale = config.getString("locale");
    }

    private void initMessage(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        prefix = config.getString("prefix", "").isEmpty() ? prefix : config.getString("prefix");
        reloadMsg = config.getString("reloadMsg");
        commandWrongUsage = config.getString("commandWrongUsage");
        noPermission = config.getString("noPermission");

        RTUUtilAPI.getFileManager().copyResource("Translations", "Locale_EN.yml");
        RTUUtilAPI.getFileManager().copyResource("Translations", "Locale_KR.yml");
    }

    private static class InnerInstanceClass {
        private static final ConfigManager instance = new ConfigManager();
    }


}
