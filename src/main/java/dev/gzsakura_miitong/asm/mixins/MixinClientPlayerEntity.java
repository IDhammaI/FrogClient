/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.input.Input
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.entity.MovementType
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.gzsakura_miitong.asm.mixins;

import com.mojang.authlib.GameProfile;
import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.Event;
import dev.gzsakura_miitong.api.events.impl.MoveEvent;
import dev.gzsakura_miitong.api.events.impl.MovedEvent;
import dev.gzsakura_miitong.api.events.impl.SendMovementPacketsEvent;
import dev.gzsakura_miitong.api.events.impl.TickEvent;
import dev.gzsakura_miitong.api.events.impl.TickMovementEvent;
import dev.gzsakura_miitong.asm.accessors.IClientPlayerEntity;
import dev.gzsakura_miitong.core.impl.CommandManager;
import dev.gzsakura_miitong.core.impl.RotationManager;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import dev.gzsakura_miitong.mod.modules.impl.exploit.PacketControl;
import dev.gzsakura_miitong.mod.modules.impl.movement.NoSlow;
import dev.gzsakura_miitong.mod.modules.impl.movement.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPlayerEntity.class})
public abstract class MixinClientPlayerEntity
extends AbstractClientPlayerEntity {
    @Shadow
    public Input input;
    @Final
    @Shadow
    protected MinecraftClient client;
    @Unique
    private float preYaw;
    @Unique
    private float prePitch;
    @Unique
    private boolean rotation = false;
    @Shadow
    private double lastX;
    @Shadow
    private double lastBaseY;
    @Shadow
    private double lastZ;
    @Shadow
    private int ticksSinceLastPositionPacketSent;
    @Shadow
    private float lastYaw;
    @Shadow
    private float lastPitch;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method={"pushOutOfBlocks"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPushOutOfBlocksHook(double x, double d, CallbackInfo info) {
        if (Velocity.INSTANCE.isOn() && Velocity.INSTANCE.blockPush.getValue()) {
            info.cancel();
        }
    }

    @Redirect(method={"tickMovement"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require=0)
    private boolean tickMovementHook(ClientPlayerEntity player) {
        if (NoSlow.INSTANCE.noSlow()) {
            return false;
        }
        return player.isUsingItem();
    }

    @Inject(at={@At(value="HEAD")}, method={"tickNausea"}, cancellable=true)
    private void updateNausea(CallbackInfo ci) {
        if (ClientSetting.INSTANCE.portalGui()) {
            ci.cancel();
        }
    }

    @Inject(method={"move"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V")}, cancellable=true)
    public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        MoveEvent event = MoveEvent.get(movement.x, movement.y, movement.z);
        Alien.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        } else if (event.modify) {
            ci.cancel();
            super.move(movementType, new Vec3d(event.getX(), event.getY(), event.getZ()));
            Alien.EVENT_BUS.post(MovedEvent.INSTANCE);
        }
    }

    @Inject(method={"move"}, at={@At(value="TAIL")})
    public void onMoveReturnHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        Alien.EVENT_BUS.post(MovedEvent.INSTANCE);
    }

    @Shadow
    public abstract float getPitch(float var1);

    @Shadow
    public abstract float getYaw(float var1);

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void tickHead(CallbackInfo ci) {
        block2: {
            try {
                Alien.EVENT_BUS.post(TickEvent.get(Event.Stage.Pre));
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block2;
                CommandManager.sendMessage("\u00a74An error has occurred (ClientPlayerEntity.tick() [HEAD]) Message: [" + e.getMessage() + "]");
            }
        }
    }

    @Inject(method={"tick"}, at={@At(value="RETURN")})
    private void tickReturn(CallbackInfo ci) {
        block2: {
            try {
                Alien.EVENT_BUS.post(TickEvent.get(Event.Stage.Post));
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block2;
                CommandManager.sendMessage("\u00a74An error has occurred (ClientPlayerEntity.tick() [RETURN]) Message: [" + e.getMessage() + "]");
            }
        }
    }

    @Inject(method={"sendMovementPackets"}, at={@At(value="HEAD")})
    private void onSendMovementPacketsHead(CallbackInfo info) {
        this.rotation();
        if (PacketControl.INSTANCE.isOn() && PacketControl.INSTANCE.positionSync.getValue() && this.ticksSinceLastPositionPacketSent >= PacketControl.INSTANCE.positionDelay.getValueInt() - 1) {
            ((IClientPlayerEntity)((Object)this)).setTicksSinceLastPositionPacketSent(50);
        }
        if (RotationManager.snapBack) {
            ((IClientPlayerEntity)((Object)this)).setTicksSinceLastPositionPacketSent(50);
            ((IClientPlayerEntity)((Object)this)).setLastYaw(999.0f);
            RotationManager.snapBack = false;
            return;
        }
        if (AntiCheat.INSTANCE.fullPackets.getValue()) {
            boolean bl3;
            double d = this.getX() - this.lastX;
            double e = this.getY() - this.lastBaseY;
            double f = this.getZ() - this.lastZ;
            double g = this.getYaw() - this.lastYaw;
            double h = this.getPitch() - this.lastPitch;
            boolean bl = bl3 = g != 0.0 || h != 0.0;
            if (AntiCheat.INSTANCE.force.getValue() || !(MathHelper.squaredMagnitude((double)d, (double)e, (double)f) > MathHelper.square((double)2.0E-4)) && this.ticksSinceLastPositionPacketSent >= 19 || bl3) {
                ((IClientPlayerEntity)((Object)this)).setTicksSinceLastPositionPacketSent(50);
                ((IClientPlayerEntity)((Object)this)).setLastYaw(999.0f);
            }
        }
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal=0)})
    private void onTickHasVehicleBeforeSendPackets(CallbackInfo info) {
        this.rotation();
    }

    @Unique
    private void rotation() {
        this.rotation = true;
        this.preYaw = this.getYaw();
        this.prePitch = this.getPitch();
        SendMovementPacketsEvent event = SendMovementPacketsEvent.get(this.getYaw(), this.getPitch());
        Alien.EVENT_BUS.post(event);
        Alien.ROTATION.rotationYaw = event.getYaw();
        Alien.ROTATION.rotationPitch = event.getPitch();
        this.setYaw(event.getYaw());
        this.setPitch(event.getPitch());
    }

    @Inject(method={"sendMovementPackets"}, at={@At(value="TAIL")})
    private void onSendMovementPacketsTail(CallbackInfo info) {
        if (this.rotation) {
            this.setYaw(this.preYaw);
            this.setPitch(this.prePitch);
            this.rotation = false;
        }
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal=1, shift=At.Shift.AFTER)})
    private void onTickHasVehicleAfterSendPackets(CallbackInfo info) {
        if (this.rotation) {
            this.setYaw(this.preYaw);
            this.setPitch(this.prePitch);
            this.rotation = false;
        }
    }

    @Inject(method={"tickMovement"}, at={@At(value="HEAD")})
    private void tickMovement(CallbackInfo ci) {
        Alien.EVENT_BUS.post(TickMovementEvent.INSTANCE);
    }
}

