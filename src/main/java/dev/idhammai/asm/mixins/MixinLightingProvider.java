/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.chunk.light.LightingProvider
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.mod.modules.impl.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LightingProvider.class})
public class MixinLightingProvider {
    private boolean shouldBlockLightUpdates() {
        if (NoRender.INSTANCE == null) {
            return false;
        }
        if (!NoRender.INSTANCE.isOn() || !NoRender.INSTANCE.lightsUpdate.getValue()) {
            return false;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc != null && mc.isOnThread();
    }

    @Inject(at={@At(value="HEAD")}, method={"checkBlock"}, cancellable=true)
    public void checkBlock(BlockPos pos, CallbackInfo ci) {
        if (this.shouldBlockLightUpdates()) {
            ci.cancel();
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"doLightUpdates"}, cancellable=true)
    public void doLightUpdates(CallbackInfoReturnable<Integer> cir) {
        if (this.shouldBlockLightUpdates()) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
}

