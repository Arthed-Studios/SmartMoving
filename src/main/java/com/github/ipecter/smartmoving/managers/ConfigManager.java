package com.github.ipecter.smartmoving.managers;

import com.github.ipecter.rtu.utilapi.RTUUtilAPI;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private boolean enablePlugin = true;
    private boolean motd = true;
    private String locale = "EN";
    private String prefix = IridiumColorAPI.process("<GRADIENT:4aa832>[ SmartMoving ]</GRADIENT:a3a3a3> ");
    private String reloadMsg = "";
    private String commandWrongUsage = "";
    private String noPermission = "";
    private List<String> crawlingModes = Collections.synchronizedList(new ArrayList<>());
    private List<String> crawlingKeys = Collections.synchronizedList(new ArrayList<>());
    private List<String> crawlingBlockBlackList = Collections.synchronizedList(new ArrayList<>());
    private List<String> crawlingWorldBlackList = Collections.synchronizedList(new ArrayList<>());

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

    public ConfigManager() {
    }

    public final static ConfigManager getInstance() {
        return InnerInstanceClass.instance;
    }

    public List<String> getCrawlingModes() {
        return crawlingModes;
    }

    public void setCrawlingModes(List<String> crawlingModes) {
        this.crawlingModes = crawlingModes;
    }

    public List<String> getCrawlingKeys() {
        return crawlingKeys;
    }

    public void setCrawlingKeys(List<String> crawlingKeys) {
        this.crawlingKeys = crawlingKeys;
    }

    public List<String> getCrawlingBlockBlackList() {
        return crawlingBlockBlackList;
    }

    public void setCrawlingBlockBlackList(List<String> crawlingBlockBlackList) {
        this.crawlingBlockBlackList = crawlingBlockBlackList;
    }

    public void initConfigFiles() {
        initSetting(RTUUtilAPI.getFileManager().copyResource("Setting.yml"));
        initMessage(RTUUtilAPI.getFileManager().copyResource("Translations", "Locale_" + locale + ".yml"));
    }

    public List<String> getCrawlingWorldBlackList() {
        return crawlingWorldBlackList;
    }

    public void setCrawlingWorldBlackList(List<String> crawlingWorldBlackList) {
        this.crawlingWorldBlackList = crawlingWorldBlackList;
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

    private void initSetting(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        enablePlugin = config.getBoolean("enablePlugin");
        motd = config.getBoolean("motd");
        locale = config.getString("locale");

        initCrawling(config);

    }

    private void initCrawling(YamlConfiguration config) {
        crawlingModes.addAll(config.getStringList("crawling.modes"));
        crawlingKeys.addAll(config.getStringList("crawling.keys"));
        crawlingWorldBlackList.addAll(config.getStringList("crawling.blackList.worlds"));
        crawlingBlockBlackList.addAll(config.getStringList("crawling.blackList.blocks"));
    }

    private static class InnerInstanceClass {
        private static final ConfigManager instance = new ConfigManager();
    }

}
