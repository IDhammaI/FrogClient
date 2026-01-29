/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 *  net.minecraft.world.chunk.WorldChunk
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.Frog;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.combat.CombatUtil;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import dev.idhammai.mod.modules.impl.combat.SelfTrap;
import dev.idhammai.mod.modules.impl.player.InteractTweaks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={World.class})
public abstract class MixinWorld {
    @Inject(method={"getBlockState"}, at={@At(value="HEAD")}, cancellable=true)
    public void blockStateHook(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (Wrapper.mc.world != null && Wrapper.mc.world.isInBuildLimit(pos)) {
            if (SelfTrap.airList.contains(pos)) {
                cir.setReturnValue(Blocks.AIR.getDefaultState());
                return;
            }
            if (!ClientSetting.INSTANCE.mioCompatible.getValue()) {
                WorldChunk worldChunk;
                BlockState tempState;
                boolean terrainIgnore = CombatUtil.terrainIgnore;
                BlockPos modifyPos = CombatUtil.modifyPos;
                BlockState modifyBlockState = CombatUtil.modifyBlockState;
                if (terrainIgnore || modifyPos != null) {
                    WorldChunk worldChunk2 = Wrapper.mc.world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
                    BlockState tempState2 = worldChunk2.getBlockState(pos);
                    if (modifyPos != null && modifyBlockState != null && pos.equals((Object)modifyPos)) {
                        cir.setReturnValue(modifyBlockState);
                        return;
                    }
                    if (terrainIgnore) {
                        if (Frog.HOLE.isHard(tempState2.getBlock())) {
                            return;
                        }
                        cir.setReturnValue(Blocks.AIR.getDefaultState());
                    }
                } else if (InteractTweaks.INSTANCE.isActive && (tempState = (worldChunk = Wrapper.mc.world.getChunk(pos.getX() >> 4, pos.getZ() >> 4)).getBlockState(pos)).getBlock() == Blocks.BEDROCK) {
                    cir.setReturnValue(Blocks.AIR.getDefaultState());
                }
            }
        }
    }
}
