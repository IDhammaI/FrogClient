package dev.idhammai.asm.accessors;

import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenHandler.class)
public interface IScreenHandler {
    @Accessor("revision")
    int getRevision();
    
    @Accessor("revision")
    void setRevision(int revision);
}