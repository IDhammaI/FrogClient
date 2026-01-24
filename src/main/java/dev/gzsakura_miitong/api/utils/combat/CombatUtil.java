/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 */
package dev.gzsakura_miitong.api.utils.combat;

import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.player.EntityUtil;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class CombatUtil
implements Wrapper {
    public static final Timer breakTimer = new Timer();
    public static boolean terrainIgnore = false;
    public static BlockPos modifyPos;
    public static BlockState modifyBlockState;

    public static List<PlayerEntity> getEnemies(double range) {
        ArrayList<PlayerEntity> list = new ArrayList<PlayerEntity>();
        for (AbstractClientPlayerEntity player : Vitality.THREAD.getPlayers()) {
            if (!CombatUtil.isValid((Entity)player, range)) continue;
            list.add((PlayerEntity)player);
        }
        return list;
    }

    public static void attackCrystal(BlockPos pos, boolean rotate, boolean eatingPause) {
        CombatUtil.attackCrystal(new Box(pos), rotate, eatingPause);
    }

    public static void attackCrystal(Box box, boolean rotate, boolean eatingPause) {
        for (EndCrystalEntity entity : BlockUtil.getEndCrystals(box)) {
            CombatUtil.attackWithDelay((Entity)entity, rotate, eatingPause);
        }
    }

    public static void attackWithDelay(Entity entity, boolean rotate, boolean usingPause) {
        if (!breakTimer.passed((long)(AntiCheat.INSTANCE.attackDelay.getValue() * 1000.0))) {
            return;
        }
        if (usingPause && CombatUtil.mc.player.isUsingItem()) {
            return;
        }
        CombatUtil.attack(entity, rotate);
    }

    public static void attack(Entity entity, boolean rotate) {
        if (entity != null) {
            Vec3d attackVec = MathUtil.getClosestPointToBox(CombatUtil.mc.player.getEyePos(), entity.getBoundingBox());
            if (CombatUtil.mc.player.getEyePos().distanceTo(attackVec) > AntiCheat.INSTANCE.ieRange.getValue()) {
                return;
            }
            breakTimer.reset();
            if (rotate && AntiCheat.INSTANCE.attackRotate.getValue()) {
                Vitality.ROTATION.lookAt(attackVec);
            }
            mc.getNetworkHandler().sendPacket((Packet)PlayerInteractEntityC2SPacket.attack((Entity)entity, (boolean)CombatUtil.mc.player.isSneaking()));
            CombatUtil.mc.player.resetLastAttackedTicks();
            EntityUtil.swingHand(Hand.MAIN_HAND, AntiCheat.INSTANCE.attackSwing.getValue());
            if (rotate && AntiCheat.INSTANCE.attackRotate.getValue()) {
                Vitality.ROTATION.snapBack();
            }
        }
    }

    public static boolean isntValid(Entity entity, double range) {
        return !CombatUtil.isValid(entity, range);
    }

    public static boolean isValid(Entity entity, double range) {
        PlayerEntity player;
        boolean invalid = entity == null || !entity.isAlive() || entity.equals((Object)CombatUtil.mc.player) || entity instanceof PlayerEntity && Vitality.FRIEND.isFriend(player = (PlayerEntity)entity) || CombatUtil.mc.player.getPos().distanceTo(entity.getPos()) > range;
        return !invalid;
    }

    public static boolean isValid(Entity entity) {
        PlayerEntity player;
        boolean invalid = entity == null || !entity.isAlive() || entity.equals((Object)CombatUtil.mc.player) || entity instanceof PlayerEntity && Vitality.FRIEND.isFriend(player = (PlayerEntity)entity);
        return !invalid;
    }

    public static PlayerEntity getClosestEnemy(double getDistance) {
        PlayerEntity closest = null;
        for (PlayerEntity player : CombatUtil.getEnemies(getDistance)) {
            if (closest == null) {
                closest = player;
                continue;
            }
            if (!(CombatUtil.mc.player.squaredDistanceTo(player.getPos()) < CombatUtil.mc.player.squaredDistanceTo((Entity)closest))) continue;
            closest = player;
        }
        return closest;
    }

    static {
        modifyBlockState = Blocks.AIR.getDefaultState();
    }
}

