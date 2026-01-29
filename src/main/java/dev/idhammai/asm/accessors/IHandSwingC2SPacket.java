package dev.idhammai.asm.accessors;

import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandSwingC2SPacket.class)
public interface IHandSwingC2SPacket {
    @Accessor("hand")
    Hand getHand();
}