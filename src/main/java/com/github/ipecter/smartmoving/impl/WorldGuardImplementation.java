package com.github.ipecter.smartmoving.impl;

import com.github.ipecter.smartmoving.SmartMovingManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class WorldGuardImplementation {
    public static StateFlag ALLOW_CRAWLING;
    private final Plugin owningPlugin = SmartMovingManager.getInstance().getPlugin();
    private Object worldGuard = null;
    private WorldGuardPlugin worldGuardPlugin = null;
    private Object regionContainer = null;
    private Method regionContainerGetMethod = null;
    private Method worldAdaptMethod = null;
    private Method regionManagerGetMethod = null;
    private Constructor<?> vectorConstructor = null;
    private Method vectorConstructorAsAMethodBecauseWhyNot = null;
    private boolean initialized = false;


    public WorldGuardImplementation(Plugin plugin, Plugin owningPlugin) {
        if (plugin instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin) plugin;

            try {
                Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
                Method getInstanceMethod = worldGuardClass.getMethod("getInstance");
                worldGuard = getInstanceMethod.invoke(null);
                owningPlugin.getLogger().info("Found WorldGuard 7+");
            } catch (Exception ex) {
                owningPlugin.getLogger().info("Found WorldGuard <7");
            }

            try {
                owningPlugin.getLogger().info("Pre-check for WorldGuard custom flag registration");
                registerFlag();
                // registerFlag();
            } catch (NoSuchMethodError incompatible) {
                owningPlugin.getLogger().log(Level.WARNING, "NOFLAGS", incompatible);
                // Ignored, will follow up in checkFlagSupport
            } catch (Throwable ex) {
                owningPlugin.getLogger().log(Level.WARNING, "Unexpected error setting up custom flags, please make sure you are on WorldGuard 6.2 or above", ex);
            }
        }
    }

    public boolean isEnabled() {
        return worldGuardPlugin != null;
    }

    protected RegionAssociable getAssociable(Player player) {
        RegionAssociable associable;
        if (player == null) {
            associable = Associables.constant(Association.NON_MEMBER);
        } else {
            associable = worldGuardPlugin.wrapPlayer(player);
        }

        return associable;
    }

    private void registerFlag() {
        FlagRegistry registry = null;
        try {
            Method getFlagRegistryMethod = worldGuard.getClass().getMethod("getFlagRegistry");
            registry = (FlagRegistry) getFlagRegistryMethod.invoke(worldGuard);
            try {
                StateFlag flag = new StateFlag("crawling", true);
                registry.register(flag);
                ALLOW_CRAWLING = flag;
            } catch (IllegalStateException | FlagConflictException e) {
                Flag<?> existing = registry.get("crawling");
                if (existing instanceof StateFlag) {
                    ALLOW_CRAWLING = (StateFlag) existing;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initialize() {
        if (!initialized) {
            initialized = true;
            // Super hacky reflection to deal with differences in WorldGuard 6 and 7+
            if (worldGuard != null) {
                try {
                    Method getPlatFormMethod = worldGuard.getClass().getMethod("getPlatform");
                    Object platform = getPlatFormMethod.invoke(worldGuard);
                    Method getRegionContainerMethod = platform.getClass().getMethod("getRegionContainer");
                    regionContainer = getRegionContainerMethod.invoke(platform);
                    Class<?> worldEditWorldClass = Class.forName("com.sk89q.worldedit.world.World");
                    Class<?> worldEditAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
                    worldAdaptMethod = worldEditAdapterClass.getMethod("adapt", World.class);
                    regionContainerGetMethod = regionContainer.getClass().getMethod("get", worldEditWorldClass);
                    //registering custom flag

                } catch (Exception ex) {
                    owningPlugin.getLogger().log(Level.WARNING, "Failed to bind to WorldGuard, integration will not work!", ex);
                    regionContainer = null;
                    return;
                }
            } else {
                //regionContainer = worldGuardPlugin.getRegionContainer();
                try {
                    regionContainerGetMethod = regionContainer.getClass().getMethod("get", World.class);
                } catch (Exception ex) {
                    owningPlugin.getLogger().log(Level.WARNING, "Failed to bind to WorldGuard, integration will not work!", ex);
                    regionContainer = null;
                    return;
                }
            }

            // Ugh guys, API much?
            try {
                Class<?> vectorClass = Class.forName("com.sk89q.worldedit.Vector");
                vectorConstructor = vectorClass.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE);
                regionManagerGetMethod = RegionManager.class.getMethod("getApplicableRegions", vectorClass);
            } catch (Exception ex) {
                try {
                    Class<?> vectorClass = Class.forName("com.sk89q.worldedit.math.BlockVector3");
                    vectorConstructorAsAMethodBecauseWhyNot = vectorClass.getMethod("at", Double.TYPE, Double.TYPE, Double.TYPE);
                    regionManagerGetMethod = RegionManager.class.getMethod("getApplicableRegions", vectorClass);
                } catch (Exception sodonewiththis) {
                    owningPlugin.getLogger().log(Level.WARNING, "Failed to bind to WorldGuard (no Vector class?), integration will not work!", ex);
                    regionContainer = null;
                    return;
                }
            }

            if (regionContainer == null) {
                owningPlugin.getLogger().warning("Failed to find RegionContainer, WorldGuard integration will not function!");
            }
        }
    }

    private RegionManager getRegionManager(World world) {
        initialize();
        if (regionContainer == null || regionContainerGetMethod == null) return null;
        RegionManager regionManager = null;
        try {
            if (worldAdaptMethod != null) {
                Object worldEditWorld = worldAdaptMethod.invoke(null, world);
                regionManager = (RegionManager) regionContainerGetMethod.invoke(regionContainer, worldEditWorld);
            } else {
                regionManager = (RegionManager) regionContainerGetMethod.invoke(regionContainer, world);
            }
        } catch (Exception ex) {
            owningPlugin.getLogger().log(Level.WARNING, "An error occurred looking up a WorldGuard RegionManager", ex);
        }
        return regionManager;
    }

    private ApplicableRegionSet getRegionSet(Location location) {
        RegionManager regionManager = getRegionManager(location.getWorld());
        if (regionManager == null) return null;
        // The Location version of this method is gone in 7.0
        // Oh and then they also randomly changed the Vector class at some point without even a version bump.
        // So awesome!
        try {
            Object vector = vectorConstructorAsAMethodBecauseWhyNot == null
                    ? vectorConstructor.newInstance(location.getX(), location.getY(), location.getZ())
                    : vectorConstructorAsAMethodBecauseWhyNot.invoke(null, location.getX(), location.getY(), location.getZ());
            return (ApplicableRegionSet) regionManagerGetMethod.invoke(regionManager, vector);
        } catch (Exception ex) {
            owningPlugin.getLogger().log(Level.WARNING, "An error occurred looking up a WorldGuard ApplicableRegionSet", ex);
        }
        return null;
    }

    public boolean canCrawl(Player player) {
        Location location = player.getLocation();
        if (worldGuardPlugin == null || location == null) return true;

        ApplicableRegionSet checkSet = getRegionSet(location);
        if (checkSet == null) return true;

        return checkSet.queryState(getAssociable(player), ALLOW_CRAWLING) != StateFlag.State.DENY;
    }

}