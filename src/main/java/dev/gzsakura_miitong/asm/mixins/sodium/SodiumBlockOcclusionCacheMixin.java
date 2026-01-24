/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache
 *  net.minecraft.block.BlockState
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.world.BlockView
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package dev.gzsakura_miitong.asm.mixins.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.gzsakura_miitong.mod.modules.impl.render.Xray;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={BlockOcclusionCache.class}, remap=false)
public abstract class SodiumBlockOcclusionCacheMixin {
    @ModifyReturnValue(method={"shouldDrawSide"}, at={@At(value="RETURN")})
    private boolean shouldDrawSide(boolean original, BlockState state, BlockView view, BlockPos pos, Direction facing) {
        if (Xray.INSTANCE.isOn()) {
            return Xray.INSTANCE.modifyDrawSide(state, view, pos, facing, original);
        }
        return original;
    }
}

