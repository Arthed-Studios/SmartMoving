package com.github.ipecter.smartmoving.managers;

import com.github.ipecter.rtu.pluginlib.RTUPluginLib;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ConfigManager {

    private boolean enablePlugin = true;
    private boolean motd = true;
    private boolean debug = false;
    private String locale = "EN";
    private String prefix = IridiumColorAPI.process("<GRADIENT:47cc1f>[ SmartMoving ]</GRADIENT:a3a3a3> ");
    private Map<String, String> msgKeyMap = Collections.synchronizedMap(new HashMap<>());

    //[ Crawling Part ]
    private List<String> crawlingModes = Collections.synchronizedList(new ArrayList<>());
    private List<String> crawlingKeys = Collections.synchronizedList(new ArrayList<>());
    // blackList
    private boolean crawlingWorldBlackList;
    private boolean crawlingBlockBlackList;
    private List<String> crawlingBlockList = Collections.synchronizedList(new ArrayList<>());
    private List<String> crawlingWorldList = Collections.synchronizedList(new ArrayList<>());

    //[ WallJump Part ]
    private boolean requireDirectionChange;
    private double minimumDistance;
    private double maximumVelocity;
    private int maxJump;
    private double timeOnWall;
    // jumpPower
    private double jumpPowerHorizontal;
    private double jumpPowerVertical;
    // slide
    private boolean slideEnable;
    private double slideSpeed;
    private boolean slideCanJumpWhile;
    // blackList
    private boolean wallJumpWorldBlackList;
    private boolean wallJumpBlockBlackList;
    private List<String> wallJumpBlockList = Collections.synchronizedList(new ArrayList<>());
    private List<String> wallJumpWorldList = Collections.synchronizedList(new ArrayList<>());


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

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean motd) {
        this.debug = debug;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTranslation(String key) {
        return msgKeyMap.getOrDefault(key, "");
    }

    public ConfigManager() {
    }

    public final static ConfigManager getInstance() {
        return InnerInstanceClass.instance;
    }

    //[ Crawling Part ]
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

    public List<String> getCrawlingBlockList() {
        return crawlingBlockList;
    }

    public void setCrawlingBlockList(List<String> crawlingBlockList) {
        this.crawlingBlockList = crawlingBlockList;
    }

    public boolean isCrawlingWorldBlackList() {
        return crawlingWorldBlackList;
    }

    public void setCrawlingWorldBlackList(boolean crawlingWorldBlackList) {
        this.crawlingWorldBlackList = crawlingWorldBlackList;
    }

    public boolean isCrawlingBlockBlackList() {
        return crawlingWorldBlackList;
    }

    public void setCrawlingBlockBlackList(boolean crawlingJumpBlockBlackList) {
        this.crawlingBlockBlackList = crawlingBlockBlackList;
    }

    //[ WallJump Part ]
    public boolean isRequireDirectionChange() {
        return requireDirectionChange;
    }

    public void setRequireDirectionChange(boolean requireDirectionChange) {
        this.requireDirectionChange = requireDirectionChange;
    }

    public double getMinimumDistance() {
        return minimumDistance;
    }

    public void setMinimumDistance(double minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    public double getMaximumVelocity() {
        return maximumVelocity;
    }

    public void setMaximumVelocity(double maximumVelocity) {
        this.maximumVelocity = maximumVelocity;
    }

    public int getMaxJump() {
        return maxJump;
    }

    public void setMaxJump(int maxJump) {
        this.maxJump = maxJump;
    }

    public double getTimeOnWall() {
        return timeOnWall;
    }

    public void setTimeOnWall(double timeOnWall) {
        this.timeOnWall = timeOnWall;
    }

    public double getJumpPowerHorizontal() {
        return jumpPowerHorizontal;
    }

    public void setJumpPowerHorizontal(double jumpPowerHorizontal) {
        this.jumpPowerHorizontal = jumpPowerHorizontal;
    }

    public double getJumpPowerVertical() {
        return jumpPowerVertical;
    }

    public void setJumpPowerVertical(double jumpPowerVertical) {
        this.jumpPowerVertical = jumpPowerVertical;
    }

    public boolean isSlideEnable() {
        return slideEnable;
    }

    public void setSlideEnable(boolean slideEnable) {
        this.slideEnable = slideEnable;
    }

    public double getSlideSpeed() {
        return slideSpeed;
    }

    public void setSlideSpeed(double slideSpeed) {
        this.slideSpeed = slideSpeed;
    }

    public boolean isSlideCanJumpWhile() {
        return slideCanJumpWhile;
    }

    public void setSlideCanJumpWhile(boolean slideCanJumpWhile) {
        this.slideCanJumpWhile = slideCanJumpWhile;
    }

    public boolean isWallJumpWorldBlackList() {
        return wallJumpWorldBlackList;
    }

    public void setWallJumpWorldBlackList(boolean wallJumpWorldBlackList) {
        this.wallJumpWorldBlackList = wallJumpWorldBlackList;
    }

    public boolean isWallJumpBlockBlackList() {
        return wallJumpWorldBlackList;
    }

    public void setWallJumpBlockBlackList(boolean wallJumpBlockBlackList) {
        this.wallJumpBlockBlackList = wallJumpBlockBlackList;
    }

    public List<String> getWallJumpBlockList() {
        return wallJumpBlockList;
    }

    public void setWallJumpBlockList(List<String> wallJumpBlockList) {
        this.wallJumpBlockList = wallJumpBlockList;
    }

    public List<String> getWallJumpWorldList() {
        return wallJumpWorldList;
    }

    public void setWallJumpWorldList(List<String> wallJumpWorldList) {
        this.wallJumpWorldList = wallJumpWorldList;
    }

    public void initConfigFiles() {
        initSetting(RTUPluginLib.getFileManager().copyResource("Setting.yml"));
        initMessage(RTUPluginLib.getFileManager().copyResource("Translations", "Locale_" + locale + ".yml"));
    }

    public List<String> getCrawlingWorldList() {
        return crawlingWorldList;
    }

    public void setCrawlingWorldList(List<String> crawlingWorldList) {
        this.crawlingWorldList = crawlingWorldList;
    }

    private void initMessage(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            if (key.equals("prefix")) {
                msgKeyMap.put(key, config.getString("prefix", "").isEmpty() ? prefix : config.getString("prefix"));
            } else {
                msgKeyMap.put(key, config.getString(key));
            }
        }
        RTUPluginLib.getFileManager().copyResource("Translations", "Locale_EN.yml");
        RTUPluginLib.getFileManager().copyResource("Translations", "Locale_KR.yml");
    }

    private void initSetting(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        enablePlugin = config.getBoolean("enablePlugin");
        motd = config.getBoolean("motd");
        locale = config.getString("locale");
        debug = config.getBoolean("debug");

        initCrawling(config);

    }

    private void initCrawling(YamlConfiguration config) {

        //[ Crawling Part ]
        crawlingModes.clear();
        crawlingKeys.clear();
        crawlingModes.addAll(config.getStringList("crawling.modes"));
        crawlingKeys.addAll(config.getStringList("crawling.keys"));
        crawlingWorldBlackList = config.getBoolean("crawling.list.worldBlackList", true);
        crawlingBlockBlackList = config.getBoolean("crawling.list.blockBlackList", true);
        crawlingWorldList.clear();
        crawlingBlockList.clear();
        crawlingWorldList.addAll(config.getStringList("crawling.list.worlds"));
        crawlingBlockList.addAll(config.getStringList("crawling.list.blocks"));

        //[ WallJump Part ]
        requireDirectionChange = config.getBoolean("wallJump.requireDirectionChange", true);
        minimumDistance = config.getDouble("wallJump.minimumDistance", 0.3);
        maximumVelocity = config.getDouble("wallJump.maximumVelocity", -1);
        maxJump = config.getInt("wallJump.maxJump", 0);
        timeOnWall = config.getDouble("timeOnWall", 0.6);

        jumpPowerHorizontal = config.getDouble("wallJump.jumpPower.horizontal", 0.3);
        jumpPowerVertical = config.getDouble("wallJump.jumpPower.vertical", 0.5);

        slideEnable = config.getBoolean("slide.enable", true);
        slideSpeed = config.getDouble("slide.speed", 0.17);
        slideCanJumpWhile = config.getBoolean("slide.canJumpWhile", true);

        wallJumpWorldList.clear();
        wallJumpBlockList.clear();
        wallJumpWorldBlackList = config.getBoolean("wallJump.list.worldBlackList", true);
        wallJumpBlockBlackList = config.getBoolean("wallJump.list.blockBlackList", true);
        wallJumpWorldList.addAll(config.getStringList("wallJump.list.worlds"));
        wallJumpBlockList.addAll(config.getStringList("wallJump.list.blocks"));

    }

    private static class InnerInstanceClass {
        private static final ConfigManager instance = new ConfigManager();
    }

}
