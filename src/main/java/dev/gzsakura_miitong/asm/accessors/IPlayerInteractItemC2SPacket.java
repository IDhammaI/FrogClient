package dev.gzsakura_miitong.asm.accessors;

import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInteractItemC2SPacket.class)
public interface IPlayerInteractItemC2SPacket {
    @Accessor("hand")
    Hand getHand();
}