/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 */
package dev.idhammai.core.impl;

import dev.idhammai.Frog;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.mod.modules.impl.combat.Breaker;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class HoleManager
implements Wrapper {
    public boolean isHole(BlockPos pos) {
        return this.isHole(pos, true, false, false);
    }

    public boolean isHole(BlockPos pos, boolean canStand, boolean checkTrap, boolean anyBlock) {
        int blockProgress = 0;
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || i == Direction.DOWN || (!anyBlock || HoleManager.mc.world.isAir(pos.offset(i))) && !Frog.HOLE.isHard(pos.offset(i))) continue;
            ++blockProgress;
        }
        return !(checkTrap && (!HoleManager.mc.world.isAir(pos) || !HoleManager.mc.world.isAir(pos.up()) || !HoleManager.mc.world.isAir(pos.up(1)) || !HoleManager.mc.world.isAir(pos.up(2)) || HoleManager.mc.player.getBlockY() - 1 > pos.getY() && !HoleManager.mc.world.isAir(pos.up(3)) || HoleManager.mc.player.getBlockY() - 2 > pos.getY() && !HoleManager.mc.world.isAir(pos.up(4))) || blockProgress <= 3 || canStand && !BlockUtil.canCollide(new Box(pos.add(0, -1, 0))));
    }

    public BlockPos getHole(float range, boolean doubleHole, boolean any, boolean up) {
        BlockPos bestPos = null;
        double bestDistance = range + 1.0f;
        for (BlockPos pos : BlockUtil.getSphere(range, HoleManager.mc.player.getPos())) {
            if ((pos.getX() != HoleManager.mc.player.getBlockX() || pos.getZ() != HoleManager.mc.player.getBlockZ()) && !up && (double)(pos.getY() + 1) > HoleManager.mc.player.getY() || !Frog.HOLE.isHole(pos, true, true, any) && (!doubleHole || !this.isDoubleHole(pos)) || pos.getY() - HoleManager.mc.player.getBlockY() > 1) continue;
            double getDistance = MathHelper.sqrt((float)((float)HoleManager.mc.player.squaredDistanceTo((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)));
            if (bestPos != null && !(getDistance < bestDistance)) continue;
            bestPos = pos;
            bestDistance = getDistance;
        }
        return bestPos;
    }

    public boolean isDoubleHole(BlockPos pos) {
        Direction unHardFacing = this.is3Block(pos);
        if (unHardFacing != null) {
            return (unHardFacing = this.is3Block(pos = pos.offset(unHardFacing))) != null;
        }
        return false;
    }

    public Direction is3Block(BlockPos pos) {
        if (!this.isHard(pos.down())) {
            return null;
        }
        if (!(HoleManager.mc.world.isAir(pos) && HoleManager.mc.world.isAir(pos.up()) && HoleManager.mc.world.isAir(pos.up(2)))) {
            return null;
        }
        int progress = 0;
        Direction unHardFacing = null;
        for (Direction facing : Direction.values()) {
            if (facing == Direction.UP || facing == Direction.DOWN) continue;
            if (this.isHard(pos.offset(facing))) {
                ++progress;
                continue;
            }
            int progress2 = 0;
            for (Direction facing2 : Direction.values()) {
                if (facing2 == Direction.DOWN || facing2 == facing.getOpposite() || !this.isHard(pos.offset(facing).offset(facing2))) continue;
                ++progress2;
            }
            if (progress2 == 4) {
                ++progress;
                continue;
            }
            unHardFacing = facing;
        }
        if (progress == 3) {
            return unHardFacing;
        }
        return null;
    }

    public boolean isHard(BlockPos pos) {
        Block block = HoleManager.mc.world.getBlockState(pos).getBlock();
        return this.isHard(block);
    }

    public boolean isHard(Block block) {
        return block == Blocks.BEDROCK || Breaker.hard.contains(block);
    }
}

