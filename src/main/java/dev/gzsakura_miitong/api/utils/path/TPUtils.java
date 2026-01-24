/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$OnGroundOnly
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.util.math.Vec3d
 */
package dev.gzsakura_miitong.api.utils.path;

import com.google.common.collect.Lists;
import dev.gzsakura_miitong.api.utils.Wrapper;

import java.util.List;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class TPUtils
implements Wrapper {
    public static void teleportWithBack(Vec3d newPos, TeleportType type, Runnable runnable) {
        switch (type.ordinal()) {
            case 0: {
                TPUtils.legacyTeleportWithBack(newPos, runnable);
                break;
            }
            case 1: {
                TPUtils.newTeleportWithBack(newPos, runnable);
            }
        }
    }

    public static void legacyTeleportWithBack(Vec3d newPos, Runnable runnable) {
        List<Vec3> tpPath = PathUtils.computePath(newPos);
        tpPath.removeFirst();
        tpPath.forEach(vec3 -> TPUtils.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(vec3.x(), vec3.y(), vec3.z(), false)));
        runnable.run();
        tpPath = Lists.reverse(tpPath);
        tpPath.removeFirst();
        tpPath.forEach(vec3 -> TPUtils.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(vec3.x(), vec3.y(), vec3.z(), false)));
        TPUtils.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(TPUtils.mc.player.getX(), TPUtils.mc.player.getY(), TPUtils.mc.player.getZ(), false));
    }

    public static void newTeleportWithBack(Vec3d newPos, Runnable runnable) {
        int i;
        int packetsRequired = (int)Math.ceil(TPUtils.mc.player.getPos().distanceTo(newPos) / 10.0) - 1;
        for (i = 0; i < packetsRequired; ++i) {
            TPUtils.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
        TPUtils.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, true));
        runnable.run();
        for (i = 0; i < packetsRequired; ++i) {
            TPUtils.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
        TPUtils.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(TPUtils.mc.player.getX(), TPUtils.mc.player.getY(), TPUtils.mc.player.getZ(), false));
    }

    public static void newTeleport(Vec3d newPos) {
        int packetsRequired = (int)Math.ceil(TPUtils.mc.player.getPos().distanceTo(newPos) / 10.0) - 1;
        for (int i = 0; i < packetsRequired; ++i) {
            TPUtils.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
        TPUtils.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, true));
    }

    public static enum TeleportType {
        Legacy,
        New;

    }
}

