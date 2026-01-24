/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.PickaxeItem
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Position
 */
package dev.gzsakura_miitong.mod.modules.impl.combat;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.combat.CombatUtil;
import dev.gzsakura_miitong.api.utils.player.EntityUtil;
import dev.gzsakura_miitong.api.utils.world.BlockPosX;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.exploit.Blink;
import dev.gzsakura_miitong.mod.modules.impl.player.PacketMine;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;

public class Breaker
extends Module {
    public static Breaker INSTANCE;
    public final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));
    public final SliderSetting range = this.add(new SliderSetting("Range", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));
    private final BooleanSetting burrow = this.add(new BooleanSetting("Burrow", true));
    private final BooleanSetting head = this.add(new BooleanSetting("Head", true));
    private final BooleanSetting face = this.add(new BooleanSetting("Face", true));
    private final BooleanSetting down = this.add(new BooleanSetting("Down", false));
    private final BooleanSetting surround = this.add(new BooleanSetting("Surround", true));
    private final BooleanSetting cevPause = this.add(new BooleanSetting("CevPause", true));
    private final BooleanSetting forceDouble = this.add(new BooleanSetting("ForceDouble", false));
    public static final List<Block> hard;

    public Breaker() {
        super("Breaker", Module.Category.Combat);
        this.setChinese("\u81ea\u52a8\u6316\u6398");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (CevBreaker.INSTANCE.isOn() && this.cevPause.getValue()) {
            return;
        }
        if (AntiCrawl.INSTANCE.work) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        PlayerEntity player = CombatUtil.getClosestEnemy(this.targetRange.getValue());
        if (player == null) {
            return;
        }
        this.doBreak(player);
    }

    /*
     * WARNING - void declaration
     */
    private void doBreak(PlayerEntity player) {
        BlockPos pos = EntityUtil.getEntityPos((Entity)player, true);
        if (PacketMine.getBreakPos() != null && !PacketMine.getBreakPos().equals((Object)PacketMine.secondPos) && PacketMine.secondPos != null && !Breaker.mc.world.isAir(PacketMine.secondPos) && this.forceDouble.getValue()) {
            return;
        }
        double[] dArray = new double[]{-0.8, 0.3, 1.1};
        double[] xzOffset = new double[]{0.3, -0.3};
        for (PlayerEntity playerEntity : CombatUtil.getEnemies(this.targetRange.getValue())) {
            for (double y : dArray) {
                for (double x : xzOffset) {
                    for (double z : xzOffset) {
                        BlockPosX offsetPos = new BlockPosX(playerEntity.getX() + x, playerEntity.getY() + y, playerEntity.getZ() + z);
                        if (!this.canBreak(offsetPos) || !offsetPos.equals(PacketMine.getBreakPos())) continue;
                        return;
                    }
                }
            }
        }
        ArrayList<Float> yList = new ArrayList<Float>();
        if (this.down.getValue()) {
            yList.add(Float.valueOf(-0.8f));
        }
        if (this.head.getValue()) {
            yList.add(Float.valueOf(2.3f));
        }
        if (this.burrow.getValue()) {
            yList.add(Float.valueOf(0.3f));
        }
        if (this.face.getValue()) {
            yList.add(Float.valueOf(1.1f));
        }
        Iterator iterator = yList.iterator();
        while (iterator.hasNext()) {
            double y = ((Float)iterator.next()).floatValue();
            double[] dArray2 = xzOffset;
            int n = dArray2.length;
            for (int i = 0; i < n; ++i) {
                double offset = dArray2[i];
                BlockPosX offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
                if (!this.canBreak(offsetPos)) continue;
                PacketMine.INSTANCE.mine(offsetPos);
                return;
            }
        }
        Iterator iterator2 = yList.iterator();
        while (iterator2.hasNext()) {
            double y = ((Float)iterator2.next()).floatValue();
            for (double offset : xzOffset) {
                for (double offset2 : xzOffset) {
                    BlockPosX offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
                    if (!this.canBreak(offsetPos)) continue;
                    PacketMine.INSTANCE.mine(offsetPos);
                    return;
                }
            }
        }
        if (this.surround.getValue()) {
            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP || direction == Direction.DOWN || Math.sqrt(Breaker.mc.player.getEyePos().squaredDistanceTo(pos.offset(direction).toCenterPos())) > this.range.getValue() || !Breaker.mc.world.isAir(pos.offset(direction)) && !pos.offset(direction).equals((Object)PacketMine.getBreakPos()) || !this.canPlaceCrystal(pos.offset(direction), false) || pos.offset(direction).equals((Object)PacketMine.secondPos)) continue;
                return;
            }
            ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
            for (Direction i : Direction.values()) {
                if (i != Direction.UP && i != Direction.DOWN && !(Math.sqrt(Breaker.mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > this.range.getValue()) && this.canBreak(pos.offset(i)) && this.canPlaceCrystal(pos.offset(i), true) && !this.isSurroundPos(pos.offset(i))) {
                    arrayList.add(pos.offset(i));
                }
            }
            if (!arrayList.isEmpty()) {
                PacketMine.INSTANCE.mine(arrayList.stream().min(Comparator.comparingDouble(E -> E.getSquaredDistance((Position)Breaker.mc.player.getEyePos()))).get());
            } else {
                for (Direction i : Direction.values()) {
                    if (i != Direction.UP && i != Direction.DOWN && !(Math.sqrt(Breaker.mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > this.range.getValue()) && this.canBreak(pos.offset(i)) && this.canPlaceCrystal(pos.offset(i), false)) {
                        arrayList.add(pos.offset(i));
                    }
                }
                if (!arrayList.isEmpty()) {
                    PacketMine.INSTANCE.mine(arrayList.stream().min(Comparator.comparingDouble(E -> E.getSquaredDistance((Position)Breaker.mc.player.getEyePos()))).get());
                }
            }
        }
    }

    private boolean isSurroundPos(BlockPos pos) {
        for (Direction i : Direction.values()) {
            BlockPos self;
            if (i == Direction.UP || i == Direction.DOWN || !(self = EntityUtil.getPlayerPos(true)).offset(i).equals((Object)pos)) continue;
            return true;
        }
        return false;
    }

    public boolean canPlaceCrystal(BlockPos pos, boolean block) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        return (BlockUtil.getBlock(obsPos) == Blocks.BEDROCK || BlockUtil.getBlock(obsPos) == Blocks.OBSIDIAN || !block) && BlockUtil.noEntityBlockCrystal(boost, true, true) && BlockUtil.noEntityBlockCrystal(boost.up(), true, true);
    }

    private boolean isObsidian(BlockPos pos) {
        return Breaker.mc.player.getEyePos().distanceTo(pos.toCenterPos()) <= PacketMine.INSTANCE.range.getValue() && hard.contains(BlockUtil.getBlock(pos)) && BlockUtil.getClickSideStrict(pos) != null;
    }

    private boolean canBreak(BlockPos pos) {
        return this.isObsidian(pos) && (BlockUtil.getClickSideStrict(pos) != null || pos.equals((Object)PacketMine.getBreakPos())) && (!pos.equals((Object)PacketMine.secondPos) || !(Breaker.mc.player.getMainHandStack().getItem() instanceof PickaxeItem) && !PacketMine.INSTANCE.autoSwitch.getValue() && !PacketMine.INSTANCE.noGhostHand.getValue());
    }

    static {
        hard = Arrays.asList(Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.NETHERITE_BLOCK, Blocks.CRYING_OBSIDIAN, Blocks.RESPAWN_ANCHOR, Blocks.ANCIENT_DEBRIS, Blocks.ANVIL);
    }
}

