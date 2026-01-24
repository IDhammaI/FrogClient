/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Matrix4f
 *  org.lwjgl.opengl.GL11
 */
package dev.gzsakura_miitong.mod.modules.impl.movement;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.KeyboardInputEvent;
import dev.gzsakura_miitong.api.events.impl.MoveEvent;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.RotationEvent;
import dev.gzsakura_miitong.api.events.impl.TimerEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.player.MovementUtil;
import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import dev.gzsakura_miitong.mod.modules.impl.player.Freecam;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class HoleSnap
extends Module {
    public static HoleSnap INSTANCE;
    public final BooleanSetting any = this.add(new BooleanSetting("AnyHole", true));
    public final SliderSetting timer = this.add(new SliderSetting("Timer", 1.0, 0.1, 8.0, 0.1));
    public final BooleanSetting up = this.add(new BooleanSetting("Up", true));
    public final BooleanSetting grim = this.add(new BooleanSetting("Grim", false));
    public final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
    public final SliderSetting circleSize = this.add(new SliderSetting("CircleSize", 1.0, 0.1f, 2.5));
    public final BooleanSetting fade = this.add(new BooleanSetting("Fade", true));
    public final SliderSetting segments = this.add(new SliderSetting("Segments", 180, 0, 360));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5, 1, 50));
    private final SliderSetting timeoutTicks = this.add(new SliderSetting("TimeOut", 40, 0, 100));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.8, 0.0, 1.0, 0.01, this.grim::getValue));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 10, 0, 100, this.grim::getValue));
    boolean resetMove = false;
    boolean applyTimer = false;
    Vec3d targetPos;
    private BlockPos holePos;
    private int stuckTicks;
    private int enabledTicks;

    public HoleSnap() {
        super("HoleSnap", "HoleSnap", Module.Category.Movement);
        this.setChinese("\u62c9\u5751");
        INSTANCE = this;
    }

    public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
        Vec3d vec3d = posTo.subtract(posFrom);
        return HoleSnap.getRotationFromVec(vec3d);
    }

    public static void drawCircle(MatrixStack matrixStack, Color color, double circleSize, Vec3d pos, int segments) {
        Vec3d camPos = HoleSnap.mc.getBlockEntityRenderDispatcher().camera.getPos();
        RenderSystem.disableDepthTest();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        float keyCodec = (float)color.getAlpha() / 255.0f;
        float r = (float)color.getRed() / 255.0f;
        float g = (float)color.getGreen() / 255.0f;
        float elementCodec = (float)color.getBlue() / 255.0f;
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (double i = 0.0; i < 360.0; i += 360.0 / (double)segments) {
            double x = Math.sin(Math.toRadians(i)) * circleSize;
            double z = Math.cos(Math.toRadians(i)) * circleSize;
            Vec3d tempPos = new Vec3d(pos.x + x, pos.y, pos.z + z).add(-camPos.x, -camPos.y, -camPos.z);
            bufferBuilder.vertex(matrix, (float)tempPos.x, (float)tempPos.y, (float)tempPos.z).color(r, g, elementCodec, keyCodec);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.enableDepthTest();
    }

    private static Vec2f getRotationFromVec(Vec3d vec) {
        double d = vec.x;
        double d2 = vec.z;
        double xz = Math.hypot(d, d2);
        d2 = vec.z;
        double d3 = vec.x;
        double yaw = HoleSnap.normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
        double pitch = HoleSnap.normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
        return new Vec2f((float)yaw, (float)pitch);
    }

    private static double normalizeAngle(double angleIn) {
        double angle = angleIn;
        angle %= 360.0;
        if (angle >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return angle;
    }

    @EventListener(priority=-99)
    public void onTimer(TimerEvent event) {
        if (this.applyTimer) {
            event.set(this.timer.getValueFloat());
        }
    }

    @Override
    public void onEnable() {
        this.applyTimer = false;
        if (HoleSnap.nullCheck()) {
            this.disable();
            return;
        }
        this.resetMove = false;
        this.holePos = Alien.HOLE.getHole((float)this.range.getValue(), true, this.any.getValue(), this.up.getValue());
    }

    @Override
    public void onDisable() {
        this.holePos = null;
        this.stuckTicks = 0;
        this.enabledTicks = 0;
        if (HoleSnap.nullCheck()) {
            return;
        }
        if (this.resetMove && !this.grim.getValue()) {
            MovementUtil.setMotionX(0.0);
            MovementUtil.setMotionZ(0.0);
        }
    }

    @EventListener
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.disable();
        }
    }

    @EventListener(priority=-999)
    public void onKeyInput(KeyboardInputEvent e) {
        if (!this.grim.getValue()) {
            return;
        }
        if (!AntiCheat.INSTANCE.movementSync()) {
            this.sendMessage("\u00a74HoleSnap require MovementSync.");
            this.disable();
            return;
        }
        if (HoleSnap.mc.player.isRiding() || Freecam.INSTANCE.isOn()) {
            return;
        }
        HoleSnap.mc.player.input.movementSideways = 0.0f;
        HoleSnap.mc.player.input.movementForward = 1.0f;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        Direction facing;
        this.holePos = Alien.HOLE.getHole((float)this.range.getValue(), true, this.any.getValue(), this.up.getValue());
        if (this.holePos == null) {
            this.disable();
            return;
        }
        ++this.enabledTicks;
        if ((double)this.enabledTicks > this.timeoutTicks.getValue() - 1.0) {
            this.disable();
            return;
        }
        this.applyTimer = true;
        if (!this.grim.getValue()) {
            return;
        }
        if (!HoleSnap.mc.player.isAlive() || HoleSnap.mc.player.isFallFlying()) {
            this.disable();
            return;
        }
        if (this.stuckTicks > 8) {
            this.disable();
            return;
        }
        if (this.holePos == null) {
            this.disable();
            return;
        }
        Vec3d playerPos = HoleSnap.mc.player.getPos();
        this.targetPos = new Vec3d((double)this.holePos.getX() + 0.5, HoleSnap.mc.player.getY(), (double)this.holePos.getZ() + 0.5);
        if (Alien.HOLE.isDoubleHole(this.holePos) && (facing = Alien.HOLE.is3Block(this.holePos)) != null) {
            this.targetPos = this.targetPos.add(new Vec3d((double)facing.getVector().getX() * 0.5, (double)facing.getVector().getY() * 0.5, (double)facing.getVector().getZ() * 0.5));
        }
        this.applyTimer = true;
        this.resetMove = true;
        float rotation = HoleSnap.getRotationTo((Vec3d)playerPos, (Vec3d)this.targetPos).x;
        float yawRad = rotation / 180.0f * (float)Math.PI;
        double dist = playerPos.distanceTo(this.targetPos);
        double cappedSpeed = Math.min(0.2873, dist);
        double x = (double)(-((float)Math.sin(yawRad))) * cappedSpeed;
        double z = (double)((float)Math.cos(yawRad)) * cappedSpeed;
        if (Math.abs(x) < 0.25 && Math.abs(z) < 0.25 && playerPos.y <= (double)this.holePos.getY() + 0.8) {
            this.disable();
            return;
        }
        this.stuckTicks = HoleSnap.mc.player.horizontalCollision ? ++this.stuckTicks : 0;
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (this.grim.getValue() && this.holePos != null) {
            Direction facing;
            this.targetPos = new Vec3d((double)this.holePos.getX() + 0.5, HoleSnap.mc.player.getY(), (double)this.holePos.getZ() + 0.5);
            if (Alien.HOLE.isDoubleHole(this.holePos) && (facing = Alien.HOLE.is3Block(this.holePos)) != null) {
                this.targetPos = this.targetPos.add(new Vec3d((double)facing.getVector().getX() * 0.5, (double)facing.getVector().getY() * 0.5, (double)facing.getVector().getZ() * 0.5));
            }
            event.setTarget(this.targetPos, this.steps.getValueFloat(), this.priority.getValueFloat());
        }
    }

    @EventListener
    public void onMove(MoveEvent event) {
        Direction facing;
        if (this.grim.getValue()) {
            return;
        }
        if (!HoleSnap.mc.player.isAlive() || HoleSnap.mc.player.isFallFlying()) {
            this.disable();
            return;
        }
        if (this.stuckTicks > 8) {
            this.disable();
            return;
        }
        if (this.holePos == null) {
            this.disable();
            return;
        }
        Vec3d playerPos = HoleSnap.mc.player.getPos();
        this.targetPos = new Vec3d((double)this.holePos.getX() + 0.5, HoleSnap.mc.player.getY(), (double)this.holePos.getZ() + 0.5);
        if (Alien.HOLE.isDoubleHole(this.holePos) && (facing = Alien.HOLE.is3Block(this.holePos)) != null) {
            this.targetPos = this.targetPos.add(new Vec3d((double)facing.getVector().getX() * 0.5, (double)facing.getVector().getY() * 0.5, (double)facing.getVector().getZ() * 0.5));
        }
        this.applyTimer = true;
        this.resetMove = true;
        float rotation = HoleSnap.getRotationTo((Vec3d)playerPos, (Vec3d)this.targetPos).x;
        float yawRad = rotation / 180.0f * (float)Math.PI;
        double dist = playerPos.distanceTo(this.targetPos);
        double cappedSpeed = Math.min(0.2873, dist);
        double x = (double)(-((float)Math.sin(yawRad))) * cappedSpeed;
        double z = (double)((float)Math.cos(yawRad)) * cappedSpeed;
        event.setX(x);
        event.setZ(z);
        if (Math.abs(x) < 0.1 && Math.abs(z) < 0.1 && playerPos.y <= (double)this.holePos.getY() + 0.5) {
            this.disable();
        }
        this.stuckTicks = HoleSnap.mc.player.horizontalCollision ? ++this.stuckTicks : 0;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (this.targetPos == null || this.holePos == null) {
            return;
        }
        GL11.glEnable((int)3042);
        Color color = this.color.getValue();
        Vec3d pos = new Vec3d(this.targetPos.x, (double)this.holePos.getY(), this.targetPos.getZ());
        if (this.fade.getValue()) {
            double temp = 0.01;
            for (double i = 0.0; i < this.circleSize.getValue(); i += temp) {
                HoleSnap.drawCircle(matrixStack, ColorUtil.injectAlpha(color, (int)Math.min((double)(color.getAlpha() * 2) / (this.circleSize.getValue() / temp), 255.0)), i, pos, this.segments.getValueInt());
            }
        } else {
            HoleSnap.drawCircle(matrixStack, color, this.circleSize.getValue(), pos, this.segments.getValueInt());
        }
        GL11.glDisable((int)3042);
    }
}

