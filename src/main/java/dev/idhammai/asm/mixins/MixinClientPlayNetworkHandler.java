/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientCommonNetworkHandler
 *  net.minecraft.client.network.ClientConnectionState
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.ClientConnection
 *  net.minecraft.network.NetworkThreadUtils
 *  net.minecraft.network.listener.ClientPlayPacketListener
 *  net.minecraft.network.listener.PacketListener
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket
 *  net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket
 *  net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket
 *  net.minecraft.network.packet.s2c.play.GameJoinS2CPacket
 *  net.minecraft.network.packet.s2c.play.InventoryS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.network.packet.s2c.play.PositionFlag
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.thread.ThreadExecutor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.Frog;
import dev.idhammai.api.events.impl.EntityVelocityUpdateEvent;
import dev.idhammai.api.events.impl.GameLeftEvent;
import dev.idhammai.api.events.impl.InventoryS2CPacketEvent;
import dev.idhammai.api.events.impl.S2CCloseScreenEvent;
import dev.idhammai.api.events.impl.SendMessageEvent;
import dev.idhammai.api.events.impl.ServerChangePositionEvent;
import dev.idhammai.mod.modules.impl.exploit.AntiPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPlayNetworkHandler.class})
public abstract class MixinClientPlayNetworkHandler
extends ClientCommonNetworkHandler {
    @Shadow
    private ClientWorld world;
    @Unique
    private boolean frog$worldNotNull;
    @Unique
    private boolean ignore;

    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method={"onEnterReconfiguration"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift=At.Shift.AFTER)})
    private void onEnterReconfiguration(EnterReconfigurationS2CPacket packet, CallbackInfo info) {
        Frog.EVENT_BUS.post(GameLeftEvent.INSTANCE);
    }

    @Inject(method={"onGameJoin"}, at={@At(value="HEAD")})
    private void onGameJoinHead(GameJoinS2CPacket packet, CallbackInfo info) {
        this.frog$worldNotNull = this.world != null;
    }

    @Inject(method={"onGameJoin"}, at={@At(value="TAIL")})
    private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info) {
        if (this.frog$worldNotNull) {
            Frog.EVENT_BUS.post(GameLeftEvent.INSTANCE);
        }
    }

    @Shadow
    public abstract void sendChatMessage(String var1);

    @Inject(method={"onInventory"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift=At.Shift.AFTER)}, cancellable=true)
    public void onInventoryS2CPacket(InventoryS2CPacket packet, CallbackInfo ci) {
        InventoryS2CPacketEvent event = InventoryS2CPacketEvent.get(packet);
        Frog.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"sendChatMessage"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (this.ignore) {
            return;
        }
        if (message.startsWith(Frog.getPrefix())) {
            Frog.COMMAND.command(message.split(" "));
            ci.cancel();
            } else {
                SendMessageEvent event = SendMessageEvent.get(message);
                Frog.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            } else if (!event.message.equals(event.defaultMessage)) {
                this.ignore = true;
                this.sendChatMessage(event.message);
                this.ignore = false;
                ci.cancel();
            }
        }
    }

    @Inject(method={"onCloseScreen"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift=At.Shift.AFTER)}, cancellable=true)
    public void onCloseScreen(CloseScreenS2CPacket packet, CallbackInfo ci) {
        S2CCloseScreenEvent event = S2CCloseScreenEvent.get();
        Frog.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method={"onEntityVelocityUpdate"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;setVelocityClient(DDD)V"), require=0)
    private void velocityHook(Entity instance, double x, double y, double z) {
        EntityVelocityUpdateEvent event = EntityVelocityUpdateEvent.get(instance, x, y, z, false);
        Frog.EVENT_BUS.post(event);
        if (!event.isCancelled()) {
            instance.setVelocityClient(event.getX(), event.getY(), event.getZ());
        }
    }

    @Redirect(method={"onExplosion"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"), require=0)
    private Vec3d velocityHook2(Vec3d instance, double x, double y, double z) {
        EntityVelocityUpdateEvent event = EntityVelocityUpdateEvent.get((Entity)this.client.player, x, y, z, true);
        Frog.EVENT_BUS.post(event);
        if (!event.isCancelled()) {
            return instance.add(event.getX(), event.getY(), event.getZ());
        }
        return instance;
    }

    @Inject(method={"onPlayerPositionLook"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift=At.Shift.AFTER)}, cancellable=true)
    public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        boolean noRotate;
        boolean bl = noRotate = AntiPacket.INSTANCE.isOn() && AntiPacket.INSTANCE.s2CRotate.getValue() && Frog.SERVER.playerNull.passedS(0.25);
        if (noRotate) {
            double i;
            double h;
            double g;
            double f;
            double e;
            double d;
            ci.cancel();
            NetworkThreadUtils.forceMainThread((Packet)packet, (PacketListener)((ClientPlayPacketListener)ClientPlayNetworkHandler.class.cast((Object)this)), (ThreadExecutor)this.client);
            ClientPlayerEntity playerEntity = this.client.player;
            Vec3d vec3d = playerEntity.getVelocity();
            boolean bl2 = packet.getFlags().contains(PositionFlag.X);
            boolean bl22 = packet.getFlags().contains(PositionFlag.Y);
            boolean bl3 = packet.getFlags().contains(PositionFlag.Z);
            if (bl2) {
                d = vec3d.getX();
                e = playerEntity.getX() + packet.getX();
                playerEntity.lastRenderX += packet.getX();
                playerEntity.prevX += packet.getX();
            } else {
                d = 0.0;
                playerEntity.lastRenderX = e = packet.getX();
                playerEntity.prevX = e;
            }
            if (bl22) {
                f = vec3d.getY();
                g = playerEntity.getY() + packet.getY();
                playerEntity.lastRenderY += packet.getY();
                playerEntity.prevY += packet.getY();
            } else {
                f = 0.0;
                playerEntity.lastRenderY = g = packet.getY();
                playerEntity.prevY = g;
            }
            if (bl3) {
                h = vec3d.getZ();
                i = playerEntity.getZ() + packet.getZ();
                playerEntity.lastRenderZ += packet.getZ();
                playerEntity.prevZ += packet.getZ();
            } else {
                h = 0.0;
                playerEntity.lastRenderZ = i = packet.getZ();
                playerEntity.prevZ = i;
            }
            playerEntity.setPosition(e, g, i);
            playerEntity.setVelocity(d, f, h);
            if (AntiPacket.INSTANCE.applyYaw.getValue()) {
                float yaw = packet.getYaw();
                float pitch = packet.getPitch();
                if (packet.getFlags().contains(PositionFlag.X_ROT)) {
                    pitch += Frog.ROTATION.getLastPitch();
                }
                if (packet.getFlags().contains(PositionFlag.Y_ROT)) {
                    yaw += Frog.ROTATION.getLastYaw();
                }
                this.connection.send((Packet)new TeleportConfirmC2SPacket(packet.getTeleportId()));
                this.connection.send((Packet)new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), yaw, pitch, false));
            } else {
                this.connection.send((Packet)new TeleportConfirmC2SPacket(packet.getTeleportId()));
                this.connection.send((Packet)new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), Frog.ROTATION.getLastYaw(), Frog.ROTATION.getLastPitch(), false));
            }
            Frog.EVENT_BUS.post(ServerChangePositionEvent.INSTANCE);
        }
    }
}
