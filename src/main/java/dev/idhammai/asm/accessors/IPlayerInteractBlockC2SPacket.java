package dev.idhammai.asm.accessors;

import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInteractBlockC2SPacket.class)
public interface IPlayerInteractBlockC2SPacket {
    @Accessor("blockHitResult")
    BlockHitResult getBlockHitResult();

    @Accessor("hand")
    Hand getHand();
}