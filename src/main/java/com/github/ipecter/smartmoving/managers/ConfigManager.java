package com.github.ipecter.smartmoving.managers;

import com.github.ipecter.rtu.pluginlib.RTUPluginLib;
import com.github.ipecter.smartmoving.SmartMoving;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

@Getter
@Setter
public class ConfigManager {

    private boolean enablePlugin = true;
    private boolean debug = false;
    private String locale = "EN";
    private Map<String, String> msgKeyMap = Collections.synchronizedMap(new HashMap<>());

    //[ Crawling Part ]
    private List<String> crawlingModes = Collections.synchronizedList(new ArrayList<>());
    private String crawlingKey;
    // blackListsz
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
    // sound
    private String namespace;
    private String group;
    private float volume;
    private float pitch;
    // blackList
    private boolean wallJumpWorldBlackList;
    private boolean wallJumpBlockBlackList;
    private List<String> wallJumpBlockList = Collections.synchronizedList(new ArrayList<>());
    private List<String> wallJumpWorldList = Collections.synchronizedList(new ArrayList<>());

    public final static ConfigManager getInstance() {
        return InnerInstanceClass.instance;
    }

    public String getTranslation(String key) {
        return msgKeyMap.getOrDefault(key, "");
    }

    public void initConfigFiles() {
        initSetting(RTUPluginLib.getFileManager().copyResource("Settings.yml"));
        initMessage(RTUPluginLib.getFileManager().copyResource("Translations", "Locale_" + locale + ".yml"));
    }

    private void initMessage(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            if (key.equals("prefix")) {
                String prefixText = config.getString("prefix", "");
                msgKeyMap.put(key, prefixText.isEmpty() ? MiniMessage.miniMessage().serialize(SmartMoving.prefix) : prefixText);
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
        locale = config.getString("locale");
        debug = config.getBoolean("debug");

        initConfig(config);

    }

    private void initConfig(YamlConfiguration config) {

        //[ Crawling Part ]
        crawlingModes.clear();
        crawlingModes.addAll(config.getStringList("crawling.modes"));
        crawlingKey = config.getString("crawling.keys", "DOUBLE_SHIFT");
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
        timeOnWall = config.getDouble("wallJump.timeOnWall", 0.6);

        jumpPowerHorizontal = config.getDouble("wallJump.jumpPower.horizontal", 0.3);
        jumpPowerVertical = config.getDouble("wallJump.jumpPower.vertical", 0.5);

        slideEnable = config.getBoolean("wallJump.slide.enable", true);
        slideSpeed = config.getDouble("wallJump.slide.speed", 0.17);
        slideCanJumpWhile = config.getBoolean("wallJump.slide.canJumpWhile", true);

        namespace = config.getString("wallJump.sound.namespace", "minecraft");
        group = config.getString("wallJump.sound.group", "Step");
        volume = (float) config.getDouble("wallJump.sound.volume", 1.0);
        pitch = (float) config.getDouble("wallJump.sound.pitch", 1.0);

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
