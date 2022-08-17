package com.github.ipecter.smartmoving.nms;

import com.github.ipecter.nms.NmsPackets;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class VersionIndependentNmsPackets implements NmsPackets {

    private static final Class<?> class_CraftEntity = bukkitClass("entity.CraftEntity");
    private static final Method method_CraftEntity_getNMS = getMethod(class_CraftEntity, "getHandle");
    private static final Method method_Entity_getDataWatcher = find_Entity_getDataWatcher();
    private static final Class<?> class_CraftBlock = bukkitClass("block.CraftBlock");
    private static final Method method_CraftBlock_getNMS = getMethod(class_CraftBlock, "getNMS");
    private static final Method method_Block_getCombinedId = find_Block_getCombinedId();
    private static final EntityTypes<?> entityFallingBlockType = findEntityTypesFallingBlock();
    private static final Class<?> class_CraftPlayer = bukkitClass("entity.CraftPlayer");
    private static final Method method_CraftPlayer_getHandle = getMethod(class_CraftPlayer, "getHandle");
    private static final Field field_EntityPlayer_playerConnection = findField(EntityPlayer.class, PlayerConnection.class);
    private static final Method method_PlayerConnection_sendPacket = findMethod(PlayerConnection.class, Packet.class);
    private final int blockId = -8854;
    private final int floorBlockId = -8855;
    private final DataWatcher dataWatcher;
    public VersionIndependentNmsPackets(World world) {
        FallingBlock fallingBlock = (FallingBlock) world.spawnEntity(new Location(world, 0, 0, 0), EntityType.FALLING_BLOCK);
        fallingBlock.setGravity(false);
        try {
            this.dataWatcher = (DataWatcher) method_Entity_getDataWatcher.invoke(method_CraftEntity_getNMS.invoke(class_CraftEntity.cast(fallingBlock)));
        } catch (Exception e) {
            throw new RuntimeException();
        }
        fallingBlock.remove();
    }

    private static Class<?> bukkitClass(String name) {
        try {
            return Class.forName(Bukkit.getServer().getClass().getPackage().getName() + "." + name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... parameters) {
        try {
            return clazz.getMethod(name, parameters);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method findMethod(Class<?> clazz, Class<?>... parameters) {
        for (Method m : clazz.getMethods()) {
            if (Arrays.equals(parameters, m.getParameterTypes())) {
                return m;
            }
        }

        throw new RuntimeException(new NoSuchMethodException("Method with parameters " + Arrays.stream(parameters).collect(Collectors.toList()) + " not found in class '" + clazz + "'"));
    }

    private static Field findField(Class<?> clazz, Class<?> type) {
        for (Field f : clazz.getFields()) {
            if (f.getType().equals(type)) {
                return f;
            }
        }

        throw new RuntimeException(new NoSuchFieldException("Field with type '" + type + "' not found in class '" + clazz + "'"));
    }

    private static Method find_Block_getCombinedId() {
        for (Method m : net.minecraft.world.level.block.Block.class.getMethods()) {
            if (m.getReturnType().equals(int.class) && m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(IBlockData.class)) {
                return m;
            }
        }

        throw new RuntimeException(new NoSuchMethodException("Could not find equivalent of method 'net.minecraft.world.level.block.Block#getCombinedId' in this minecraft version."));
    }

    private static Method find_Entity_getDataWatcher() {
        for (Method m : Entity.class.getMethods()) {
            if (m.getReturnType().equals(DataWatcher.class)) {
                return m;
            }
        }

        throw new RuntimeException(new NoSuchMethodException("Could not find equivalent of method 'net.minecraft.world.entity.Entity#ai' in this minecraft version."));
    }

    private static EntityTypes<?> findEntityTypesFallingBlock() {
        for (Field f : EntityTypes.class.getFields()) {
            if (f.getGenericType().getTypeName().equals("net.minecraft.world.entity.EntityTypes<net.minecraft.world.entity.item.EntityFallingBlock>")) {
                try {
                    return (EntityTypes<?>) f.get(null);
                } catch (Exception ignored) {
                }
                break;
            }
        }

        throw new RuntimeException(new NoSuchMethodException("Could not find equivalent of field 'net.minecraft.world.entity.EntityTypes#FALLING_BLOCK' in this minecraft version."));
    }

    @Override
    public void spawnFakeBlocks(Player player, Block block, Block floorBlock) {
        int blockMaterialId;
        int floorBlockMaterialId;

        try {
            blockMaterialId = (int) method_Block_getCombinedId.invoke(null, method_CraftBlock_getNMS.invoke(class_CraftBlock.cast(block)));
            floorBlockMaterialId = (int) method_Block_getCombinedId.invoke(null, method_CraftBlock_getNMS.invoke(class_CraftBlock.cast(floorBlock)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PacketPlayOutSpawnEntity spawnBlockPacket = new PacketPlayOutSpawnEntity(
                this.blockId,
                UUID.randomUUID(), // entity uuid
                block.getX() + 0.5,
                block.getY(),
                block.getZ() + 0.5,
                0, //yaw
                0, //pitch
                entityFallingBlockType,
                blockMaterialId, //material id
                new Vec3D(0, 1, 0) // velocity
        );
        PacketPlayOutEntityMetadata blockMetadataPacket = new PacketPlayOutEntityMetadata(this.blockId, this.dataWatcher, true);
        PacketPlayOutSpawnEntity spawnBlockPacket2 = new PacketPlayOutSpawnEntity(
                this.floorBlockId,
                UUID.randomUUID(), // entity uuid
                floorBlock.getX() + 0.5,
                floorBlock.getY() + 0.001f,
                floorBlock.getZ() + 0.5,
                90, //yaw
                0, //pitch
                entityFallingBlockType,
                floorBlockMaterialId, //material id
                new Vec3D(0, 1, 0) // velocity
        );
        PacketPlayOutEntityMetadata blockMetadataPacket2 = new PacketPlayOutEntityMetadata(this.floorBlockId, this.dataWatcher, true);

        //Sending packets.
        try {
            Object playerConnection = field_EntityPlayer_playerConnection.get(method_CraftPlayer_getHandle.invoke(class_CraftPlayer.cast(player)));
            method_PlayerConnection_sendPacket.invoke(playerConnection, spawnBlockPacket);
            method_PlayerConnection_sendPacket.invoke(playerConnection, blockMetadataPacket);
            method_PlayerConnection_sendPacket.invoke(playerConnection, spawnBlockPacket2);
            method_PlayerConnection_sendPacket.invoke(playerConnection, blockMetadataPacket2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        player.sendBlockChange(floorBlock.getLocation(), Bukkit.createBlockData(floorBlock.getType()));
    }

    @Override
    public void removeFakeBlocks(Player player) {
        PacketPlayOutEntityDestroy destroyOldBlockPacket = new PacketPlayOutEntityDestroy(this.blockId);
        PacketPlayOutEntityDestroy destroyOldBlockPacket2 = new PacketPlayOutEntityDestroy(this.blockId - 1);
        try {
            Object playerConnection = field_EntityPlayer_playerConnection.get(method_CraftPlayer_getHandle.invoke(class_CraftPlayer.cast(player)));

            method_PlayerConnection_sendPacket.invoke(playerConnection, destroyOldBlockPacket);
            method_PlayerConnection_sendPacket.invoke(playerConnection, destroyOldBlockPacket2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
