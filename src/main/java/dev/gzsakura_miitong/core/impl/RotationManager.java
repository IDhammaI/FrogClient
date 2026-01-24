/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package dev.gzsakura_miitong.core.impl;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.DoAttackEvent;
import dev.gzsakura_miitong.api.events.impl.FireworkShooterRotationEvent;
import dev.gzsakura_miitong.api.events.impl.InteractBlockEvent;
import dev.gzsakura_miitong.api.events.impl.InteractItemEvent;
import dev.gzsakura_miitong.api.events.impl.JumpEvent;
import dev.gzsakura_miitong.api.events.impl.KeyboardInputEvent;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.RotationEvent;
import dev.gzsakura_miitong.api.events.impl.SendMovementPacketsEvent;
import dev.gzsakura_miitong.api.events.impl.TickEvent;
import dev.gzsakura_miitong.api.events.impl.TickMovementEvent;
import dev.gzsakura_miitong.api.events.impl.TravelEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateRotateEvent;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.path.BaritoneUtil;
import dev.gzsakura_miitong.asm.accessors.IClientPlayerEntity;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import dev.gzsakura_miitong.mod.modules.impl.movement.HoleSnap;
import dev.gzsakura_miitong.mod.modules.impl.player.Freecam;
import dev.gzsakura_miitong.mod.modules.settings.enums.SnapBack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager
implements Wrapper {
    public static final Timer ROTATE_TIMER = new Timer();
    public static Vec3d directionVec = null;
    public static boolean snapBack = false;
    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevRenderPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    public float nextYaw;
    public float nextPitch;
    public float rotationYaw;
    public float rotationPitch;
    public float lastYaw;
    public float lastPitch;
    public Vec3d crossHairUpdatePos;
    private int ticksExisted;
    public static float fixYaw;
    public static float fixPitch;
    private float prevYaw;
    private float prevPitch;

    public RotationManager() {
        Alien.EVENT_BUS.subscribe(this);
    }

    @EventListener
    public void onInteract(InteractItemEvent event) {
        if (AntiCheat.INSTANCE.interactRotation.getValue() && RotationManager.mc.player != null) {
            if (event.isPre()) {
                this.snapAt(RotationManager.mc.player.getYaw(), RotationManager.mc.player.getPitch());
            } else {
                this.snapBack();
            }
        }
    }

    @EventListener
    public void onInteract(InteractBlockEvent event) {
        if (AntiCheat.INSTANCE.interactRotation.getValue() && RotationManager.mc.player != null) {
            if (event.isPre()) {
                this.snapAt(RotationManager.mc.player.getYaw(), RotationManager.mc.player.getPitch());
            } else {
                this.snapBack();
            }
        }
    }

    @EventListener
    public void doAttack(DoAttackEvent event) {
        if (AntiCheat.INSTANCE.interactRotation.getValue() && RotationManager.mc.player != null) {
            if (event.isPre()) {
                this.snapAt(RotationManager.mc.player.getYaw(), RotationManager.mc.player.getPitch());
            } else {
                this.snapBack();
            }
        }
    }

    public void snapBack() {
        if (AntiCheat.INSTANCE.snapBackEnum.is(SnapBack.Force)) {
            mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.Full(RotationManager.mc.player.getX(), RotationManager.mc.player.getY(), RotationManager.mc.player.getZ(), this.rotationYaw, this.rotationPitch, RotationManager.mc.player.isOnGround()));
        } else if (AntiCheat.INSTANCE.snapBackEnum.is(SnapBack.Tick)) {
            snapBack = true;
        }
    }

    public void lookAt(Vec3d directionVec) {
        this.rotationTo(directionVec);
        this.snapAt(directionVec);
    }

    public void lookAt(BlockPos pos, Direction side) {
        Vec3d hitVec = pos.toCenterPos().add(new Vec3d((double)side.getVector().getX() * 0.5, (double)side.getVector().getY() * 0.5, (double)side.getVector().getZ() * 0.5));
        this.lookAt(hitVec);
    }

    public void snapAt(float yaw, float pitch) {
        this.setRenderRotation(yaw, pitch, true);
        if (AntiCheat.INSTANCE.grimRotation.getValue()) {
            mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.Full(RotationManager.mc.player.getX(), RotationManager.mc.player.getY(), RotationManager.mc.player.getZ(), yaw, pitch, RotationManager.mc.player.isOnGround()));
        } else {
            mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, RotationManager.mc.player.isOnGround()));
        }
    }

    public void snapAt(Vec3d directionVec) {
        float[] angle = RotationManager.getRotation(directionVec);
        this.snapAt(angle[0], angle[1]);
    }

    public void rotationTo(Vec3d vec3d) {
        ROTATE_TIMER.reset();
        directionVec = vec3d;
    }

    public boolean inFov(Vec3d directionVec, float fov) {
        float[] angle = RotationManager.getRotation(this.crossHairUpdatePos != null ? this.crossHairUpdatePos : new Vec3d(RotationManager.mc.player.getX(), RotationManager.mc.player.getY() + (double)RotationManager.mc.player.getEyeHeight(RotationManager.mc.player.getPose()), RotationManager.mc.player.getZ()), directionVec);
        return this.inFov(angle[0], angle[1], fov);
    }

    public boolean inFov(float yaw, float pitch, float fov) {
        float pitchDifferent;
        float yawDifferent = MathHelper.angleBetween((float)yaw, (float)this.rotationYaw);
        return yawDifferent * yawDifferent + (pitchDifferent = Math.abs(pitch - this.rotationPitch)) * pitchDifferent <= fov * fov;
    }

    @EventListener
    public void onTickMovement(TickMovementEvent event) {
        if (RotationManager.mc.player == null) {
            return;
        }
        this.crossHairUpdatePos = new Vec3d(RotationManager.mc.player.getX(), RotationManager.mc.player.getY() + (double)RotationManager.mc.player.getEyeHeight(RotationManager.mc.player.getPose()), RotationManager.mc.player.getZ());
    }

    @EventListener
    public void update(SendMovementPacketsEvent event) {
        if (AntiCheat.INSTANCE.movementSync() && !BaritoneUtil.isActive()) {
            event.setYaw(this.nextYaw);
            event.setPitch(this.nextPitch);
        } else {
            UpdateRotateEvent updateRotateEvent = UpdateRotateEvent.get(event.getYaw(), event.getPitch());
            Alien.EVENT_BUS.post(updateRotateEvent);
            event.setYaw(updateRotateEvent.getYaw());
            event.setPitch(updateRotateEvent.getPitch());
        }
    }

    @EventListener(priority=999)
    public void update(TickMovementEvent event) {
        if (RotationManager.mc.player == null) {
            return;
        }
        if (AntiCheat.INSTANCE.movementSync() && !BaritoneUtil.isActive()) {
            UpdateRotateEvent updateRotateEvent = UpdateRotateEvent.get(RotationManager.mc.player.getYaw(), RotationManager.mc.player.getPitch());
            Alien.EVENT_BUS.post(updateRotateEvent);
            this.nextYaw = updateRotateEvent.getYaw();
            this.nextPitch = updateRotateEvent.getPitch();
            fixYaw = this.nextYaw;
            fixPitch = this.nextPitch;
        }
    }

    @EventListener(priority=-200)
    public void onLastRotation(UpdateRotateEvent event) {
        RotationEvent rotationEvent = RotationEvent.get();
        Alien.EVENT_BUS.post(rotationEvent);
        if (rotationEvent.getRotation()) {
            float[] newAngle = this.injectStep(new float[]{rotationEvent.getYaw(), rotationEvent.getPitch()}, rotationEvent.getSpeed());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        } else if (rotationEvent.getTarget() != null) {
            float[] newAngle = this.injectStep(rotationEvent.getTarget(), rotationEvent.getSpeed());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        } else if (!event.isModified() && AntiCheat.INSTANCE.look.getValue() && directionVec != null && !ROTATE_TIMER.passed((long)(AntiCheat.INSTANCE.rotateTime.getValue() * 1000.0))) {
            float[] newAngle = this.injectStep(directionVec, AntiCheat.INSTANCE.steps.getValueFloat());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        }
    }

    @EventListener
    public void travel(TravelEvent e) {
        if (!AntiCheat.INSTANCE.movementSync()) {
            return;
        }
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (RotationManager.mc.player.isRiding()) {
            return;
        }
        if (e.isPre()) {
            this.prevYaw = RotationManager.mc.player.getYaw();
            this.prevPitch = RotationManager.mc.player.getPitch();
            RotationManager.mc.player.setYaw(fixYaw);
            RotationManager.mc.player.setPitch(fixPitch);
        } else {
            RotationManager.mc.player.setYaw(this.prevYaw);
            RotationManager.mc.player.setPitch(this.prevPitch);
        }
    }

    @EventListener
    public void onJump(JumpEvent e) {
        if (!AntiCheat.INSTANCE.movementSync()) {
            return;
        }
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (RotationManager.mc.player.isRiding()) {
            return;
        }
        if (e.isPre()) {
            this.prevYaw = RotationManager.mc.player.getYaw();
            this.prevPitch = RotationManager.mc.player.getPitch();
            RotationManager.mc.player.setYaw(fixYaw);
            RotationManager.mc.player.setPitch(fixPitch);
        } else {
            RotationManager.mc.player.setYaw(this.prevYaw);
            RotationManager.mc.player.setPitch(this.prevPitch);
        }
    }

    @EventListener
    public void onFirework(FireworkShooterRotationEvent event) {
        if (!AntiCheat.INSTANCE.movementSync()) {
            return;
        }
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (event.shooter == RotationManager.mc.player) {
            event.yaw = fixYaw;
            event.pitch = fixPitch;
            event.cancel();
        }
    }

    @EventListener(priority=-999)
    public void onKeyInput(KeyboardInputEvent e) {
        if (!AntiCheat.INSTANCE.movementSync()) {
            return;
        }
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (HoleSnap.INSTANCE.isOn()) {
            return;
        }
        if (RotationManager.mc.player.isRiding() || Freecam.INSTANCE.isOn()) {
            return;
        }
        float mF = RotationManager.mc.player.input.movementForward;
        float mS = RotationManager.mc.player.input.movementSideways;
        float delta = (RotationManager.mc.player.getYaw() - fixYaw) * ((float)Math.PI / 180);
        float cos = MathHelper.cos((float)delta);
        float sin = MathHelper.sin((float)delta);
        RotationManager.mc.player.input.movementSideways = Math.round(mS * cos - mF * sin);
        RotationManager.mc.player.input.movementForward = Math.round(mF * cos + mS * sin);
    }

    public float[] injectStep(Vec3d vec, float steps) {
        float currentYaw = AntiCheat.INSTANCE.serverSide.getValue() ? this.getLastYaw() : this.rotationYaw;
        float currentPitch = AntiCheat.INSTANCE.serverSide.getValue() ? this.getLastPitch() : this.rotationPitch;
        float yawDelta = MathHelper.wrapDegrees((float)((float)MathHelper.wrapDegrees((double)(Math.toDegrees(Math.atan2(vec.z - RotationManager.mc.player.getZ(), vec.x - RotationManager.mc.player.getX())) - 90.0)) - currentYaw));
        float pitchDelta = (float)(-Math.toDegrees(Math.atan2(vec.y - (RotationManager.mc.player.getPos().y + (double)RotationManager.mc.player.getEyeHeight(RotationManager.mc.player.getPose())), Math.sqrt(Math.pow(vec.x - RotationManager.mc.player.getX(), 2.0) + Math.pow(vec.z - RotationManager.mc.player.getZ(), 2.0))))) - currentPitch;
        if (AntiCheat.INSTANCE.random.getValue()) {
            float angleToRad = (float)Math.toRadians(27 * (RotationManager.mc.player.age % 30));
            yawDelta = (float)((double)yawDelta + Math.sin(angleToRad) * 3.0) + MathUtil.random(-1.0f, 1.0f);
            pitchDelta += MathUtil.random(-0.6f, 0.6f);
        }
        if (yawDelta > 180.0f) {
            yawDelta -= 180.0f;
        }
        float yawStepVal = 180.0f * steps;
        float clampedYawDelta = MathHelper.clamp((float)MathHelper.abs((float)yawDelta), (float)(-yawStepVal), (float)yawStepVal);
        float clampedPitchDelta = MathHelper.clamp((float)pitchDelta, (float)-45.0f, (float)45.0f);
        float newYaw = currentYaw + (yawDelta > 0.0f ? clampedYawDelta : -clampedYawDelta);
        float newPitch = MathHelper.clamp((float)(currentPitch + clampedPitchDelta), (float)-90.0f, (float)90.0f);
        return new float[]{newYaw, newPitch};
    }

    public float[] injectStep(float[] angle, float steps) {
        float currentYaw = AntiCheat.INSTANCE.serverSide.getValue() ? this.getLastYaw() : this.rotationYaw;
        float currentPitch = AntiCheat.INSTANCE.serverSide.getValue() ? this.getLastPitch() : this.rotationPitch;
        float yawDelta = MathHelper.wrapDegrees((float)(angle[0] - currentYaw));
        float pitchDelta = angle[1] - currentPitch;
        if (AntiCheat.INSTANCE.random.getValue()) {
            float angleToRad = (float)Math.toRadians(27 * (RotationManager.mc.player.age % 30));
            yawDelta = (float)((double)yawDelta + Math.sin(angleToRad) * 3.0) + MathUtil.random(-1.0f, 1.0f);
            pitchDelta += MathUtil.random(-0.6f, 0.6f);
        }
        if (yawDelta > 180.0f) {
            yawDelta -= 180.0f;
        }
        float yawStepVal = 180.0f * steps;
        float pitchStepVal = 90.0f * steps;
        float clampedYawDelta = MathHelper.clamp((float)MathHelper.abs((float)yawDelta), (float)(-yawStepVal), (float)yawStepVal);
        float clampedPitchDelta = MathHelper.clamp((float)pitchDelta, (float)(-pitchStepVal), (float)pitchStepVal);
        float newYaw = currentYaw + (yawDelta > 0.0f ? clampedYawDelta : -clampedYawDelta);
        float newPitch = MathHelper.clamp((float)(currentPitch + clampedPitchDelta), (float)-90.0f, (float)90.0f);
        return new float[]{newYaw, newPitch};
    }

    @EventListener(priority=-999)
    public void onPacketSend(PacketEvent.Sent event) {
        PlayerMoveC2SPacket packet;
        if (RotationManager.mc.player == null) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof PlayerMoveC2SPacket && (packet = (PlayerMoveC2SPacket)packet2).changesLook()) {
            this.setLastYaw(packet.getYaw(this.getLastYaw()));
            this.setLastPitch(packet.getPitch(this.getLastPitch()));
            this.setRenderRotation(this.getLastYaw(), this.getLastPitch(), ClientSetting.INSTANCE.sync.getValue());
        }
    }

    @EventListener
    public void onUpdateWalkingPost(TickEvent event) {
        if (event.isPost()) {
            this.setRenderRotation(this.getLastYaw(), this.getLastPitch(), false);
        }
    }

    public void setRenderRotation(float yaw, float pitch, boolean force) {
        if (RotationManager.mc.player == null) {
            return;
        }
        if (RotationManager.mc.player.age == this.ticksExisted && !force) {
            return;
        }
        this.ticksExisted = RotationManager.mc.player.age;
        prevRenderPitch = renderPitch;
        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = this.getRenderYawOffset(yaw, prevRenderYawOffset);
        prevRotationYawHead = rotationYawHead;
        rotationYawHead = yaw;
        renderPitch = pitch;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float offset;
        double zDif;
        float result = offsetIn;
        double xDif = RotationManager.mc.player.getX() - RotationManager.mc.player.prevX;
        if (xDif * xDif + (zDif = RotationManager.mc.player.getZ() - RotationManager.mc.player.prevZ) * zDif > 0.002500000176951289) {
            offset = (float)MathHelper.atan2((double)zDif, (double)xDif) * 57.295776f - 90.0f;
            float wrap = MathHelper.abs((float)(MathHelper.wrapDegrees((float)yaw) - offset));
            result = 95.0f < wrap && wrap < 265.0f ? offset - 180.0f : offset;
        }
        if (RotationManager.mc.player.handSwingProgress > 0.0f) {
            result = yaw;
        }
        if ((offset = MathHelper.wrapDegrees((float)(yaw - (result = offsetIn + MathHelper.wrapDegrees((float)(result - offsetIn)) * 0.3f)))) < -75.0f) {
            offset = -75.0f;
        } else if (offset >= 75.0f) {
            offset = 75.0f;
        }
        result = yaw - offset;
        if (offset * offset > 2500.0f) {
            result += offset * 0.2f;
        }
        return result;
    }

    public static float[] getRotation(Vec3d eyesPos, Vec3d vec) {
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{MathHelper.wrapDegrees((float)yaw), MathHelper.wrapDegrees((float)pitch)};
    }

    public static float[] getRotation(Vec3d vec) {
        Vec3d eyesPos = RotationManager.mc.player.getEyePos();
        return RotationManager.getRotation(eyesPos, vec);
    }

    public static float getRenderPitch() {
        return renderPitch;
    }

    public static float getRotationYawHead() {
        return rotationYawHead;
    }

    public static float getRenderYawOffset() {
        return renderYawOffset;
    }

    public static float getPrevRenderPitch() {
        return prevRenderPitch;
    }

    public static float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public static float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    public float getLastYaw() {
        return this.lastYaw;
    }

    public void setLastYaw(float lastYaw) {
        this.lastYaw = lastYaw;
        if (AntiCheat.INSTANCE.forceSync.getValue() && Alien.SERVER.playerNull.passedS(0.15)) {
            ((IClientPlayerEntity)RotationManager.mc.player).setLastYaw(lastYaw);
        }
    }

    public float getLastPitch() {
        return this.lastPitch;
    }

    public void setLastPitch(float lastPitch) {
        this.lastPitch = lastPitch;
        if (AntiCheat.INSTANCE.forceSync.getValue() && Alien.SERVER.playerNull.passedS(0.15)) {
            ((IClientPlayerEntity)RotationManager.mc.player).setLastPitch(lastPitch);
        }
    }
}

