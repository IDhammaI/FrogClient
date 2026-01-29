package dev.idhammai.asm.accessors;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerActionC2SPacket.class)
public interface IPlayerActionC2SPacket {
    @Accessor("pos")
    BlockPos getPos();

    @Accessor("direction")
    Direction getDirection();

    @Accessor("action")
    PlayerActionC2SPacket.Action getAction();
}