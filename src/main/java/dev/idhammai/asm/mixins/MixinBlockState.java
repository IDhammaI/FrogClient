/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  net.minecraft.block.AbstractBlock$AbstractBlockState
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.state.property.Property
 *  net.minecraft.util.ActionResult
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.world.World
 *  org.spongepowered.asm.mixin.Mixin
 */
package dev.idhammai.asm.mixins;

import com.mojang.serialization.MapCodec;
import dev.idhammai.Frog;
import dev.idhammai.api.events.impl.BlockActivateEvent;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BlockState.class})
public abstract class MixinBlockState
extends AbstractBlock.AbstractBlockState {
    public MixinBlockState(Block block, Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> mapCodec) {
        super(block, propertyMap, mapCodec);
    }

    public ActionResult onUse(World world, PlayerEntity player, BlockHitResult hit) {
        Frog.EVENT_BUS.post(BlockActivateEvent.get((BlockState)BlockState.class.cast((Object)this)));
        return super.onUse(world, player, hit);
    }
}

