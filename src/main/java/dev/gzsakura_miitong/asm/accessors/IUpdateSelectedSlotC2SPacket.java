package dev.gzsakura_miitong.asm.accessors;

import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UpdateSelectedSlotC2SPacket.class)
public interface IUpdateSelectedSlotC2SPacket {
    @Accessor("selectedSlot")
    int getSelectedSlot();
}