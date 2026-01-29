/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtSizeTracker
 *  net.minecraft.network.PacketByteBuf
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.mod.modules.impl.exploit.AntiPacket;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={PacketByteBuf.class})
public abstract class MixinPacketByteBuf {
    @ModifyArg(method={"readNbt(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/nbt/NbtCompound;"}, at=@At(value="INVOKE", target="Lnet/minecraft/network/PacketByteBuf;readNbt(Lio/netty/buffer/ByteBuf;Lnet/minecraft/nbt/NbtSizeTracker;)Lnet/minecraft/nbt/NbtElement;"))
    private static NbtSizeTracker xlPackets(NbtSizeTracker sizeTracker) {
        return AntiPacket.INSTANCE.isOn() && AntiPacket.INSTANCE.decode.getValue() ? NbtSizeTracker.ofUnlimitedBytes() : sizeTracker;
    }
}

