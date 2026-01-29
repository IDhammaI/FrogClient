/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.ScaffoldingBlock
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ExperienceOrbEntity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.decoration.ItemFrameEntity
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.entity.projectile.thrown.ExperienceBottleEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.combat.CombatUtil;
import dev.idhammai.api.utils.math.MathUtil;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.api.utils.world.BlockPosX;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.AntiCheat;
import dev.idhammai.mod.modules.impl.exploit.Blink;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AntiPhase
extends Module {
    private final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 4, 0, 8));
    private final BooleanSetting ladder = this.add(new BooleanSetting("Ladder", true).setParent());
    private final BooleanSetting onlyHard = this.add(new BooleanSetting("OnlyHard", true, this.ladder::isOpen));
    private final BooleanSetting itemFrame = this.add(new BooleanSetting("ItemFrame", true).setParent());
    private final BooleanSetting fill = this.add(new BooleanSetting("Fill", false, this.itemFrame::isOpen));
    private final BooleanSetting scaffolding = this.add(new BooleanSetting("Scaffolding", true));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private final BooleanSetting eatingPause = this.add(new BooleanSetting("EatingPause", true));
    private final BooleanSetting collideSkip = this.add(new BooleanSetting("CollideSkip", true));
    private final BooleanSetting crawlingSkip = this.add(new BooleanSetting("CrawlingSkip", true));
    private final BooleanSetting onlyGround = this.add(new BooleanSetting("InAirSkip", false));
    private final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 5.0, 0.0, 7.0, 0.1));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 100.0, 0.0, 2000.0, 1.0));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final Timer timer = new Timer();

    public AntiPhase() {
        super("AntiPhase", Module.Category.Combat);
        this.setChinese("\u53cd\u7a7f\u5899");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.eatingPause.getValue() && AntiPhase.mc.player.isUsingItem()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        for (Entity entity : CombatUtil.getEnemies(this.targetRange.getValue())) {
            Direction facing;
            int block;
            if (this.crawlingSkip.getValue() && entity.isCrawling() || this.onlyGround.getValue() && !entity.isOnGround() || this.collideSkip.getValue() && BlockUtil.canCollide(entity, entity.getBoundingBox()) || !this.timer.passed(this.delay.getValueInt())) continue;
            if (this.scaffolding.getValue() && BlockUtil.canReplace(entity.getBlockPos()) && (block = this.getScaffolding()) != -1) {
                BlockPos bp = entity.getBlockPos();
                Direction downSide = null;
                Direction placeSide = BlockUtil.getPlaceSide(bp, 6.0);
                if (placeSide != null || (downSide = this.getSideIgnore(bp.down())) != null && BlockUtil.getBlock(bp.down()) instanceof ScaffoldingBlock && !AntiPhase.mc.player.isSneaking() || AntiPhase.mc.player.isSneaking() && (downSide = this.getSideOnly(bp.down())) != null && BlockUtil.getBlock(bp.down()) instanceof ScaffoldingBlock) {
                    Vec3d targetPos = placeSide != null ? bp.offset(placeSide).toCenterPos().add((double)placeSide.getOpposite().getVector().getX() * 0.5, (double)placeSide.getOpposite().getVector().getY() * 0.5, (double)placeSide.getOpposite().getVector().getZ() * 0.5) : bp.down().toCenterPos().add((double)downSide.getVector().getX() * 0.5, (double)downSide.getVector().getY() * 0.5, (double)downSide.getVector().getZ() * 0.5);
                    double getDistance = AntiPhase.mc.player.getEyePos().distanceTo(targetPos);
                    if (getDistance <= this.placeRange.getValue()) {
                        int old = AntiPhase.mc.player.getInventory().selectedSlot;
                        this.doSwap(block);
                        if (BlockUtil.getBlock(bp.down()) instanceof ScaffoldingBlock && downSide != null) {
                            BlockUtil.clickBlock(bp.down(), downSide, this.rotate.getValue());
                        } else {
                            BlockUtil.placeBlock(bp, this.rotate.getValue());
                        }
                        this.timer.reset();
                        if (this.inventory.getValue()) {
                            this.doSwap(block);
                            EntityUtil.syncInventory();
                        } else {
                            this.doSwap(old);
                        }
                    }
                }
            }
            if (this.itemFrame.getValue() && AntiPhase.mc.world.isAir(entity.getBlockPos())) {
                int block2;
                ItemFrameEntity itemFrameEntity = this.hasItemFrame(new Box(entity.getBlockPos()));
                if (itemFrameEntity == null && (block2 = this.getItemFrame()) != -1) {
                    BlockPos bp = entity.getBlockPos().down();
                    double getDistance = AntiPhase.mc.player.getEyePos().distanceTo(bp.toBottomCenterPos().add(0.0, 1.0, 0.0));
                    if (getDistance <= this.placeRange.getValue() && BlockUtil.isStrictDirection(bp, Direction.UP) && !BlockUtil.canReplace(bp) && BlockUtil.canClick(bp)) {
                        int old = AntiPhase.mc.player.getInventory().selectedSlot;
                        this.doSwap(block2);
                        BlockUtil.clickBlock(bp, Direction.UP, this.rotate.getValue());
                        this.timer.reset();
                        if (this.inventory.getValue()) {
                            this.doSwap(block2);
                            EntityUtil.syncInventory();
                        } else {
                            this.doSwap(old);
                        }
                    }
                }
                if (this.fill.getValue() && itemFrameEntity != null && itemFrameEntity.getHeldItemStack().isEmpty()) {
                    int block3;
                    Vec3d hitVec = MathUtil.getClosestPointToBox(AntiPhase.mc.player.getEyePos(), itemFrameEntity.getBoundingBox());
                    if (AntiPhase.mc.player.getEyePos().distanceTo(hitVec) <= AntiCheat.INSTANCE.ieRange.getValue() && (block3 = this.getObsidian()) != -1) {
                        int old = AntiPhase.mc.player.getInventory().selectedSlot;
                        this.doSwap(block3);
                        if (this.rotate.getValue()) {
                            Frog.ROTATION.snapAt(hitVec);
                        }
                        AntiPhase.mc.player.networkHandler.sendPacket((Packet)PlayerInteractEntityC2SPacket.interact((Entity)itemFrameEntity, (boolean)AntiPhase.mc.player.isSneaking(), (Hand)Hand.MAIN_HAND));
                        this.timer.reset();
                        if (this.inventory.getValue()) {
                            this.doSwap(block3);
                            EntityUtil.syncInventory();
                        } else {
                            this.doSwap(old);
                        }
                        if (this.rotate.getValue()) {
                            Frog.ROTATION.snapBack();
                        }
                    }
                }
            }
            if (!this.ladder.getValue() || (block = this.getLadder()) == -1 || !BlockUtil.canReplace(entity.getBlockPos()) || (facing = this.targetFacing(entity.getPos())) == null) continue;
            BlockPos bp = entity.getBlockPos().offset(facing);
            double getDistance = AntiPhase.mc.player.getEyePos().distanceTo(bp.toCenterPos().add((double)facing.getOpposite().getVector().getX() * 0.5, (double)facing.getOpposite().getVector().getY() * 0.5, (double)facing.getOpposite().getVector().getZ() * 0.5));
            if (!(getDistance <= this.placeRange.getValue())) continue;
            BlockUtil.placedPos.add(entity.getBlockPos());
            int old = AntiPhase.mc.player.getInventory().selectedSlot;
            this.doSwap(block);
            BlockUtil.clickBlock(bp, facing.getOpposite(), this.rotate.getValue());
            this.timer.reset();
            if (this.inventory.getValue()) {
                this.doSwap(block);
                EntityUtil.syncInventory();
                continue;
            }
            this.doSwap(old);
        }
    }

    private Direction getSideOnly(BlockPos pos) {
        if (BlockUtil.isStrictDirection(pos, Direction.UP)) {
            return Direction.UP;
        }
        return null;
    }

    private Direction getSideIgnore(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || !BlockUtil.isStrictDirection(pos, i)) continue;
            return i;
        }
        return null;
    }

    private ItemFrameEntity hasItemFrame(Box box) {
        for (Entity entity : BlockUtil.getEntities(box)) {
            if (!(entity instanceof ItemFrameEntity)) continue;
            ItemFrameEntity itemFrameEntity = (ItemFrameEntity)entity;
            if (entity.getFacing() != Direction.UP) continue;
            return itemFrameEntity;
        }
        return null;
    }

    private static Box getBox(Direction facing, BlockPos bp) {
        Box box = null;
        double wide = 0.1875;
        double x = (double)facing.getOffsetX() * 0.5 + (double)bp.getX() + 0.5;
        double y = bp.getY();
        double z = (double)facing.getOffsetZ() * 0.5 + (double)bp.getZ() + 0.5;
        switch (facing) {
            case WEST: {
                box = new Box(x, y, z, x + wide, y + 1.0, z + 1.0);
                break;
            }
            case EAST: {
                box = new Box(x, y, z, x - wide, y + 1.0, z + 1.0);
                break;
            }
            case NORTH: {
                box = new Box(x, y, z, x + 1.0, y + 1.0, z + wide);
                break;
            }
            case SOUTH: {
                box = new Box(x, y, z, x + 1.0, y + 1.0, z - wide);
            }
        }
        return box;
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, AntiPhase.mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getFlintAndSteel() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findItemInventorySlot(Items.FLINT_AND_STEEL);
        }
        return InventoryUtil.findItem(Items.FLINT_AND_STEEL);
    }

    private int getObsidian() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findItemInventorySlot(Items.OBSIDIAN);
        }
        return InventoryUtil.findItem(Items.OBSIDIAN);
    }

    private int getItemFrame() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findItemInventorySlot(Items.ITEM_FRAME);
        }
        return InventoryUtil.findItem(Items.ITEM_FRAME);
    }

    private int getLadder() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.LADDER);
        }
        return InventoryUtil.findBlock(Blocks.LADDER);
    }

    private int getScaffolding() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.SCAFFOLDING);
        }
        return InventoryUtil.findBlock(Blocks.SCAFFOLDING);
    }

    private Direction targetFacing(Vec3d vec3d) {
        BlockPosX blockPos = new BlockPosX(vec3d);
        Vec3d centerPos = blockPos.toBottomCenterPos();
        float factorValue = 0.4f;
        double minDistance = Double.MAX_VALUE;
        Direction facing = null;
        for (Direction direction : Direction.values()) {
            Vec3d tempPos;
            double getDistance;
            BlockPos bp;
            if (direction == Direction.UP || direction == Direction.DOWN || !BlockUtil.isStrictDirection(bp = blockPos.offset(direction), direction.getOpposite()) || (!this.onlyHard.getValue() ? BlockUtil.canReplace(bp) || !BlockUtil.canClick(bp) : !Frog.HOLE.isHard(bp))) continue;
            Box box = AntiPhase.getBox(direction, blockPos);
            if (box == null || AntiPhase.hasEntity(box) || !((getDistance = (tempPos = centerPos.add((double)((float)direction.getOffsetX() * factorValue), 0.0, (double)((float)direction.getOffsetZ() * factorValue))).distanceTo(vec3d)) < minDistance)) continue;
            minDistance = getDistance;
            facing = direction;
        }
        return facing;
    }

    public static boolean hasEntity(Box box) {
        for (Entity entity : BlockUtil.getEntities(box)) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof ItemFrameEntity) continue;
            return true;
        }
        return false;
    }
}

// yyy i love u