package com.github.ipecter.smartmoving.nms;

import com.github.ipecter.nms.NmsPackets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class LegacyIndependentNmsPackets implements NmsPackets {

    private static final String NMS_VERSION;
    private static final Class<?> class_DataWatcher = nmsClass("DataWatcher");
    private static final Class<?> class_CraftEntity = bukkitClass("entity.CraftEntity");
    private static final Method method_CraftEntity_getHandle = getMethod(class_CraftEntity, "getHandle");
    private static final Class<?> class_Entity = nmsClass("Entity");
    private static final Method method_Entity_getDataWatcher = getMethod(class_Entity, "getDataWatcher");
    private static final Class<?> class_EntityTypes = nmsClass("EntityTypes");
    private static final Class<?> class_Vec3D = nmsClass("Vec3D");
    private static final Constructor<?> constructor_Vec3D = getConstructor(class_Vec3D, double.class, double.class, double.class);
    private static final Class<?> class_PacketPlayOutSpawnEntity = nmsClass("PacketPlayOutSpawnEntity");
    private static final Constructor<?> constructor_PacketPlayOutSpawnEntity = getConstructor(class_PacketPlayOutSpawnEntity, int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, class_EntityTypes, int.class, class_Vec3D);
    private static final Object fallingBlock_EntityTypes = findFallingBlockEntityTypes();
    private static final Class<?> class_IBlockData = nmsClass("IBlockData");
    private static final Class<?> class_Block = nmsClass("Block");
    private static final Method method_Block_getCombinedId = getMethod(class_Block, "getCombinedId", class_IBlockData);
    private static final Class<?> class_CraftBlock = bukkitClass("block.CraftBlock");
    private static final Method method_CraftBlock_getNMS = getMethod(class_CraftBlock, "getNMS");
    private static final Class<?> class_PacketPlayOutEntityMetadata = nmsClass("PacketPlayOutEntityMetadata");
    private static final Constructor<?> constructor_PacketPlayOutEntityMetadata = getConstructor(class_PacketPlayOutEntityMetadata, int.class, class_DataWatcher, boolean.class);
    private static final Class<?> class_CraftPlayer = bukkitClass("entity.CraftPlayer");
    private static final Method method_CraftPlayer_getHandle = getMethod(class_CraftPlayer, "getHandle");
    private static final Class<?> class_EntityPlayer = nmsClass("EntityPlayer");
    private static final Field field_EntityPlayer_playerConnection = getField(class_EntityPlayer, "playerConnection");
    private static final Class<?> class_Packet = nmsClass("Packet");
    private static final Class<?> class_PlayerConnection = nmsClass("PlayerConnection");
    private static final Method method_PlayerConnection_sendPacket = getMethod(class_PlayerConnection, "sendPacket", class_Packet);
    private static final Class<?> class_PacketPlayOutEntityDestroy = nmsClass("PacketPlayOutEntityDestroy");
    private static final Constructor<?> constructor_PacketPlayOutEntityDestroy = getConstructor(class_PacketPlayOutEntityDestroy, int[].class);

    static {
        if (getClass("org.bukkit.craftbukkit.CraftServer", false) == null) {
            String CRAFTBUKKIT_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

            if (getClass("net.minecraft.server.MinecraftServer", false) == null) {
                NMS_VERSION = CRAFTBUKKIT_VERSION;
            } else {
                NMS_VERSION = "";
            }
        } else {
            NMS_VERSION = "";
        }

        if (NMS_VERSION.equals(""))
            throw new UnsupportedOperationException("LegacyIndependentNmsPackets must only be used on versions lower than 1.17");
    }

    private final int blockId = -8854;
    private final int floorBlockId = -8855;
    private final Object dataWatcher;

    public LegacyIndependentNmsPackets(World world) {
        FallingBlock fallingBlock = (FallingBlock) world.spawnEntity(new Location(world, 0, 0, 0), EntityType.FALLING_BLOCK);
        fallingBlock.setGravity(false);
        try {
            this.dataWatcher = method_Entity_getDataWatcher.invoke(method_CraftEntity_getHandle.invoke(class_CraftEntity.cast(fallingBlock)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        fallingBlock.remove();
    }

    private static Class<?> getClass(String name, boolean b) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            if (b) {
                throw new RuntimeException(e);
            } else {
                return null;
            }
        }
    }

    private static Class<?> bukkitClass(String name) {
        return getClass(Bukkit.getServer().getClass().getPackage().getName() + "." + name, true);
    }

    private static Class<?> nmsClass(String name) {
        return getClass("net.minecraft.server." + NMS_VERSION + "." + name, true);
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... parameters) {
        try {
            return clazz.getMethod(name, parameters);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getField(Class<?> clazz, String name) {
        try {
            return clazz.getField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object findFallingBlockEntityTypes() {
        try {
            return getField(class_EntityTypes, "FALLING_BLOCK").get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void spawnFakeBlocks(Player player, Block block, Block floorBlock) {
        try {
            Object spawnBlockPacket = constructor_PacketPlayOutSpawnEntity.newInstance(
                    this.blockId,
                    UUID.randomUUID(), // entity uuid
                    block.getX() + 0.5,
                    block.getY(),
                    block.getZ() + 0.5,
                    0, //yaw
                    0, //pitch
                    fallingBlock_EntityTypes,
                    method_Block_getCombinedId.invoke(null, method_CraftBlock_getNMS.invoke(class_CraftBlock.cast(block))), //material id
                    constructor_Vec3D.newInstance(0, 1, 0) // velocity
            );
            Object blockMetadataPacket = constructor_PacketPlayOutEntityMetadata.newInstance(this.blockId, this.dataWatcher, true);
            Object spawnBlockPacket2 = constructor_PacketPlayOutSpawnEntity.newInstance(
                    this.floorBlockId,
                    UUID.randomUUID(), // entity uuid
                    floorBlock.getX() + 0.5,
                    floorBlock.getY() + 0.001f,
                    floorBlock.getZ() + 0.5,
                    90, //yaw
                    0, //pitch
                    fallingBlock_EntityTypes,
                    method_Block_getCombinedId.invoke(null, method_CraftBlock_getNMS.invoke(class_CraftBlock.cast(floorBlock))), //material id
                    constructor_Vec3D.newInstance(0, 1, 0) // velocity
            );
            Object blockMetadataPacket2 = constructor_PacketPlayOutEntityMetadata.newInstance(this.floorBlockId, this.dataWatcher, true);
            Object playerConnection = field_EntityPlayer_playerConnection.get(method_CraftPlayer_getHandle.invoke(class_CraftPlayer.cast(player)));

            method_PlayerConnection_sendPacket.invoke(playerConnection, spawnBlockPacket);
            method_PlayerConnection_sendPacket.invoke(playerConnection, blockMetadataPacket);
            method_PlayerConnection_sendPacket.invoke(playerConnection, spawnBlockPacket2);
            method_PlayerConnection_sendPacket.invoke(playerConnection, blockMetadataPacket2);
            player.sendBlockChange(floorBlock.getLocation(), Bukkit.createBlockData(floorBlock.getType()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeFakeBlocks(Player player) {
        try {
            Object destroyOldBlockPacket = constructor_PacketPlayOutEntityDestroy.newInstance(new int[]{this.blockId});
            Object destroyOldBlockPacket2 = constructor_PacketPlayOutEntityDestroy.newInstance(new int[]{this.blockId - 1});
            Object playerConnection = field_EntityPlayer_playerConnection.get(method_CraftPlayer_getHandle.invoke(class_CraftPlayer.cast(player)));

            method_PlayerConnection_sendPacket.invoke(playerConnection, destroyOldBlockPacket);
            method_PlayerConnection_sendPacket.invoke(playerConnection, destroyOldBlockPacket2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}