/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.timeout.TimeoutException
 *  net.minecraft.network.ClientConnection
 *  net.minecraft.network.handler.PacketEncoderException
 *  net.minecraft.network.listener.PacketListener
 *  net.minecraft.network.packet.Packet
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.gzsakura_miitong.asm.mixins;

import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.core.impl.CommandManager;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.handler.PacketEncoderException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientConnection.class})
public class MixinClientConnection {
    @Inject(at={@At(value="HEAD")}, method={"handlePacket"}, cancellable=true)
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        PacketEvent.Receive event = new PacketEvent.Receive(packet);
        Vitality.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/packet/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendPacketPre(Packet<?> packet, CallbackInfo info) {
        PacketEvent.Send event = new PacketEvent.Send(packet);
        Vitality.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/packet/Packet;)V"}, at={@At(value="RETURN")})
    private void onSendPacketPost(Packet<?> packet, CallbackInfo info) {
        PacketEvent.Sent event = new PacketEvent.Sent(packet);
        Vitality.EVENT_BUS.post(event);
    }

    @Inject(method={"exceptionCaught"}, at={@At(value="HEAD")}, cancellable=true)
    private void exceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
        if (!(throwable instanceof TimeoutException) && !(throwable instanceof PacketEncoderException) && ClientSetting.INSTANCE.caughtException.getValue()) {
            if (ClientSetting.INSTANCE.log.getValue()) {
                CommandManager.sendMessage("\u00a74Caught exception \u00a77" + throwable.getMessage());
            }
            ci.cancel();
        }
    }
}

