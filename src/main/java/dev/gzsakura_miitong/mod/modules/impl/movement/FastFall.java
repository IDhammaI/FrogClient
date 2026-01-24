/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package dev.gzsakura_miitong.mod.modules.impl.movement;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.MoveEvent;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.TimerEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.player.MovementUtil;
import dev.gzsakura_miitong.api.utils.world.BlockPosX;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.util.HashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class FastFall
extends Module {
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Fast));
    private final BooleanSetting noLag = this.add(new BooleanSetting("NoLag", true, () -> this.mode.getValue() == Mode.Fast));
    private final BooleanSetting useTimerSetting = this.add(new BooleanSetting("UseTimer", false));
    private final SliderSetting timer = this.add(new SliderSetting("Timer", 2.5, 1.0, 8.0, 0.1, this.useTimerSetting::getValue));
    private final BooleanSetting anchor = this.add(new BooleanSetting("Anchor", true));
    private final SliderSetting height = this.add(new SliderSetting("Height", 10.0, 1.0, 20.0, 0.5));
    private final Timer lagTimer = new Timer();
    boolean onGround = false;
    private boolean useTimer;

    public FastFall() {
        super("FastFall", "Miyagi son simulator", Module.Category.Movement);
        this.setChinese("\u5feb\u901f\u5760\u843d");
    }

    @Override
    public void onDisable() {
        this.useTimer = false;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener(priority=-100)
    public void onMove(MoveEvent event) {
        if (FastFall.nullCheck()) {
            return;
        }
        if (FastFall.mc.player.isOnGround() && this.anchor.getValue() && this.traceDown() != 0 && (double)this.traceDown() <= this.height.getValue() && this.trace()) {
            event.setX(event.getX() * 0.05);
            event.setZ(event.getZ() * 0.05);
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.height.getValue() > 0.0 && (double)this.traceDown() > this.height.getValue() || FastFall.mc.player.isInsideWall() || FastFall.mc.player.isSubmergedInWater() || FastFall.mc.player.isInLava() || FastFall.mc.player.isHoldingOntoLadder() || !this.lagTimer.passed(1000L) || FastFall.mc.player.isFallFlying() || Fly.INSTANCE.isOn() || FastFall.nullCheck()) {
            return;
        }
        if (Alien.PLAYER.isInWeb((PlayerEntity)FastFall.mc.player)) {
            return;
        }
        if (FastFall.mc.player.isOnGround() && this.mode.getValue() == Mode.Fast) {
            MovementUtil.setMotionY(MovementUtil.getMotionY() - (double)(this.noLag.getValue() ? 0.62f : 1.0f));
        }
        if (this.useTimerSetting.getValue()) {
            if (!FastFall.mc.player.isOnGround()) {
                if (this.onGround) {
                    this.useTimer = true;
                }
                if (MovementUtil.getMotionY() >= 0.0) {
                    this.useTimer = false;
                }
                this.onGround = false;
            } else {
                this.useTimer = false;
                MovementUtil.setMotionY(-0.08);
                this.onGround = true;
            }
        } else {
            this.useTimer = false;
        }
    }

    @EventListener
    public void onTimer(TimerEvent event) {
        if (FastFall.nullCheck()) {
            return;
        }
        if (!FastFall.mc.player.isOnGround() && this.useTimer) {
            event.set(this.timer.getValueFloat());
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Receive event) {
        if (!FastFall.nullCheck() && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.lagTimer.reset();
        }
    }

    private int traceDown() {
        int y;
        int retval = 0;
        for (int tracey = y = (int)Math.round(FastFall.mc.player.getY()) - 1; tracey >= 0; --tracey) {
            BlockHitResult trace = FastFall.mc.world.raycast(new RaycastContext(FastFall.mc.player.getPos(), new Vec3d(FastFall.mc.player.getX(), (double)tracey, FastFall.mc.player.getZ()), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)FastFall.mc.player));
            if (trace != null && trace.getType() == HitResult.Type.BLOCK) {
                return retval;
            }
            ++retval;
        }
        return retval;
    }

    private boolean trace() {
        Box bbox = FastFall.mc.player.getBoundingBox();
        Vec3d basepos = bbox.getCenter();
        double minX = bbox.minX;
        double minZ = bbox.minZ;
        double maxX = bbox.maxX;
        double maxZ = bbox.maxZ;
        HashMap<Vec3d, Vec3d> positions = new HashMap<Vec3d, Vec3d>();
        positions.put(basepos, new Vec3d(basepos.x, basepos.y - 1.0, basepos.z));
        positions.put(new Vec3d(minX, basepos.y, minZ), new Vec3d(minX, basepos.y - 1.0, minZ));
        positions.put(new Vec3d(maxX, basepos.y, minZ), new Vec3d(maxX, basepos.y - 1.0, minZ));
        positions.put(new Vec3d(minX, basepos.y, maxZ), new Vec3d(minX, basepos.y - 1.0, maxZ));
        positions.put(new Vec3d(maxX, basepos.y, maxZ), new Vec3d(maxX, basepos.y - 1.0, maxZ));
        for (Vec3d key : positions.keySet()) {
            RaycastContext context = new RaycastContext(key, (Vec3d)positions.get(key), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)FastFall.mc.player);
            BlockHitResult result = FastFall.mc.world.raycast(context);
            if (result == null || result.getType() != HitResult.Type.BLOCK) continue;
            return false;
        }
        BlockState state = FastFall.mc.world.getBlockState((BlockPos)new BlockPosX(FastFall.mc.player.getX(), FastFall.mc.player.getY() - 1.0, FastFall.mc.player.getZ()));
        return state.isAir();
    }

    private static enum Mode {
        Fast,
        None;

    }
}

