package dev.gzsakura_miitong.asm.accessors;

import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommonPongC2SPacket.class)
public interface ICommonPongC2SPacket {
    @Accessor("parameter")
    int getParameter();
}