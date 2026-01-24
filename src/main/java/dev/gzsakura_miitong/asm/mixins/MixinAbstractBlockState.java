/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractBlock$AbstractBlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.gzsakura_miitong.asm.mixins;

import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.render.Ambience;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractBlock.AbstractBlockState.class})
public class MixinAbstractBlockState {
    @Inject(method={"getLuminance"}, at={@At(value="HEAD")}, cancellable=true)
    public void getLuminanceHook(CallbackInfoReturnable<Integer> cir) {
        if (Module.nullCheck()) {
            return;
        }
        if (Ambience.INSTANCE.customLuminance.getValue()) {
            cir.setReturnValue(Ambience.INSTANCE.luminance.getValueInt());
        }
    }
}

