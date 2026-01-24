/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  org.lwjgl.opengl.GL11
 */
package dev.gzsakura_miitong.mod.modules.impl.movement;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.MoveEvent;
import dev.gzsakura_miitong.api.events.impl.RotationEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.math.AnimateUtil;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.player.InventoryUtil;
import dev.gzsakura_miitong.api.utils.player.MovementUtil;
import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render3DUtil;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class Scaffold
extends Module {
    private static Vec3d lastVec3d;
    public final SliderSetting rotateTime = this.add(new SliderSetting("KeepRotate", 1000.0, 0.0, 3000.0, 10.0));
    private final BooleanSetting tower = this.add(new BooleanSetting("Tower", true));
    private final BooleanSetting packetPlace = this.add(new BooleanSetting("PacketPlace", false));
    private final BooleanSetting safeWalk = this.add(new BooleanSetting("SafeWalk", false));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true).setParent());
    private final BooleanSetting yawStep = this.add(new BooleanSetting("YawStep", false, () -> this.rotate.isOpen() && this.rotate.isOpen()).setParent());
    private final BooleanSetting whenElytra = this.add(new BooleanSetting("FallFlying", true, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.rotate.isOpen()));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.05, 0.0, 1.0, 0.01, () -> this.rotate.isOpen() && this.yawStep.isOpen()));
    private final BooleanSetting checkFov = this.add(new BooleanSetting("OnlyLooking", true, () -> this.rotate.isOpen() && this.yawStep.isOpen()).setParent());
    private final SliderSetting fov = this.add(new SliderSetting("Fov", 20.0, 0.0, 360.0, 0.1, () -> this.checkFov.isOpen() && this.rotate.isOpen() && this.yawStep.isOpen()));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 10, 0, 100, () -> this.rotate.isOpen() && this.yawStep.isOpen()));
    private final BooleanSetting render = this.add(new BooleanSetting("Render", true).setParent());
    public final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100), this.render::isOpen));
    public final ColorSetting outlineColor = this.add(new ColorSetting("OutlineColor", new Color(255, 255, 255, 100), this.render::isOpen));
    public final SliderSetting sliderSpeed = this.add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1.0, 0.01, this.render::isOpen));
    private final BooleanSetting esp = this.add(new BooleanSetting("ESP", true, this.render::isOpen));
    private final BooleanSetting fill = this.add(new BooleanSetting("Fill", true, this.render::isOpen));
    private final BooleanSetting outline = this.add(new BooleanSetting("Box", true, this.render::isOpen));
    private final Timer timer = new Timer();
    private final Timer towerTimer = new Timer();
    private Vec3d vec;
    private BlockPos pos;

    public Scaffold() {
        super("Scaffold", Module.Category.Movement);
        this.setChinese("\u81ea\u52a8\u642d\u8def");
    }

    @EventListener(priority=-100)
    public void onMove(MoveEvent event) {
        if (!this.safeWalk.getValue()) {
            return;
        }
        SafeWalk.INSTANCE.onMove(event);
    }

    @EventListener
    public void onRotation(RotationEvent event) {
        if (this.rotate.getValue() && !this.timer.passed(this.rotateTime.getValueInt()) && this.vec != null) {
            event.setTarget(this.vec, this.steps.getValueFloat(), this.priority.getValueFloat());
        }
    }

    @Override
    public void onEnable() {
        lastVec3d = null;
        this.pos = null;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (this.render.getValue()) {
            if (this.esp.getValue()) {
                GL11.glEnable((int)3042);
                double temp = 0.01;
                for (double i = 0.0; i < 0.8; i += temp) {
                    HoleSnap.drawCircle(matrixStack, ColorUtil.injectAlpha(this.color.getValue(), (int)Math.min((double)(this.color.getValue().getAlpha() * 2) / (0.8 / temp), 255.0)), i, new Vec3d(MathUtil.interpolate(Scaffold.mc.player.lastRenderX, Scaffold.mc.player.getX(), (double)mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(Scaffold.mc.player.lastRenderY, Scaffold.mc.player.getY(), (double)mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(Scaffold.mc.player.lastRenderZ, Scaffold.mc.player.getZ(), (double)mc.getRenderTickCounter().getTickDelta(true))), 5);
                }
                GL11.glDisable((int)3042);
            }
            if (this.pos != null) {
                Vec3d cur = this.pos.toCenterPos();
                lastVec3d = lastVec3d == null ? cur : new Vec3d(AnimateUtil.animate(lastVec3d.getX(), cur.x, this.sliderSpeed.getValue()), AnimateUtil.animate(lastVec3d.getY(), cur.y, this.sliderSpeed.getValue()), AnimateUtil.animate(lastVec3d.getZ(), cur.z, this.sliderSpeed.getValue()));
                Render3DUtil.draw3DBox(matrixStack, new Box(lastVec3d.add(0.5, 0.5, 0.5), lastVec3d.add(-0.5, -0.5, -0.5)), ColorUtil.injectAlpha(this.color.getValue(), this.color.getValue().getAlpha()), ColorUtil.injectAlpha(this.outlineColor.getValue(), this.outlineColor.getValue().getAlpha()), this.outline.getValue(), this.fill.getValue());
            }
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        int block = InventoryUtil.findBlock();
        if (block == -1) {
            return;
        }
        BlockPos placePos = Scaffold.mc.player.getBlockPos().down();
        if (BlockUtil.clientCanPlace(placePos, false)) {
            int old = Scaffold.mc.player.getInventory().selectedSlot;
            if (BlockUtil.getPlaceSide(placePos) == null) {
                double getDistance = 1000.0;
                BlockPos bestPos = null;
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || !BlockUtil.canPlace(placePos.offset(i)) || bestPos != null && !(Scaffold.mc.player.squaredDistanceTo(placePos.offset(i).toCenterPos()) < getDistance)) continue;
                    bestPos = placePos.offset(i);
                    getDistance = Scaffold.mc.player.squaredDistanceTo(placePos.offset(i).toCenterPos());
                }
                if (bestPos != null) {
                    placePos = bestPos;
                } else {
                    return;
                }
            }
            if (this.rotate.getValue()) {
                Direction side = BlockUtil.getPlaceSide(placePos);
                this.vec = placePos.offset(side).toCenterPos().add((double)side.getOpposite().getVector().getX() * 0.5, (double)side.getOpposite().getVector().getY() * 0.5, (double)side.getOpposite().getVector().getZ() * 0.5);
                this.timer.reset();
                if (!this.faceVector(this.vec)) {
                    return;
                }
            }
            InventoryUtil.switchToSlot(block);
            BlockUtil.placeBlock(placePos, false, this.packetPlace.getValue());
            InventoryUtil.switchToSlot(old);
            if (this.rotate.getValue()) {
                Alien.ROTATION.snapBack();
            }
            this.pos = placePos;
            if (this.tower.getValue() && Scaffold.mc.options.jumpKey.isPressed() && !MovementUtil.isMoving()) {
                MovementUtil.setMotionY(0.42);
                MovementUtil.setMotionX(0.0);
                MovementUtil.setMotionZ(0.0);
                if (this.towerTimer.passed(1500L)) {
                    MovementUtil.setMotionY(-0.28);
                    this.towerTimer.reset();
                }
            } else {
                this.towerTimer.reset();
            }
        }
    }

    private boolean shouldYawStep() {
        if (!this.whenElytra.getValue() && (Scaffold.mc.player.isFallFlying() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying())) {
            return false;
        }
        return this.yawStep.getValue() && !Velocity.INSTANCE.noRotation();
    }

    private boolean faceVector(Vec3d directionVec) {
        if (!this.shouldYawStep()) {
            Alien.ROTATION.lookAt(directionVec);
            return true;
        }
        if (Alien.ROTATION.inFov(directionVec, this.fov.getValueFloat())) {
            return true;
        }
        return !this.checkFov.getValue();
    }
}

