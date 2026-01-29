/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.world.BlockView
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.idhammai.asm.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.idhammai.mod.modules.impl.movement.NoSlow;
import dev.idhammai.mod.modules.impl.render.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Block.class})
public abstract class MixinBlock
implements ItemConvertible {
    @Inject(at={@At(value="HEAD")}, method={"getVelocityMultiplier()F"}, cancellable=true)
    private void onGetVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
        if (NoSlow.INSTANCE.soulSand() && cir.getReturnValueF() < 1.0f) {
            cir.setReturnValue(1.0f);
        }
    }

    @ModifyReturnValue(method={"shouldDrawSide"}, at={@At(value="RETURN")})
    private static boolean onShouldDrawSide(boolean original, BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos) {
        Xray xray = Xray.INSTANCE;
        if (xray.isOn()) {
            return xray.modifyDrawSide(state, world, pos, side, original);
        }
        return original;
    }
}

