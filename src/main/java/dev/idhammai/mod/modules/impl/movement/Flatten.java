/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ExperienceOrbEntity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.entity.projectile.thrown.ExperienceBottleEntity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 */
package dev.idhammai.mod.modules.impl.movement;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.combat.CombatUtil;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.api.utils.world.BlockPosX;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.exploit.Blink;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class Flatten
extends Module {
    public static Flatten INSTANCE;
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private final BooleanSetting checkMine = this.add(new BooleanSetting("DetectMining", true));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting cover = this.add(new BooleanSetting("Cover", false));
    private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 2, 1, 8));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 100, 0, 1000));
    private final Timer timer = new Timer();
    int progress = 0;

    public Flatten() {
        super("Flatten", Module.Category.Movement);
        this.setChinese("\u586b\u5e73\u811a\u4e0b");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.progress = 0;
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.usingPause.getValue() && Flatten.mc.player.isUsingItem()) {
            return;
        }
        if (!Flatten.mc.player.isOnGround()) {
            return;
        }
        if (!this.timer.passed(this.delay.getValueInt())) {
            return;
        }
        int oldSlot = Flatten.mc.player.getInventory().selectedSlot;
        int block = this.getBlock();
        if (block == -1) {
            return;
        }
        if (!EntityUtil.isInsideBlock()) {
            return;
        }
        BlockPos pos1 = new BlockPosX(Flatten.mc.player.getX() + 0.5, Flatten.mc.player.getY() + 0.5, Flatten.mc.player.getZ() + 0.5).down();
        BlockPos pos2 = new BlockPosX(Flatten.mc.player.getX() - 0.5, Flatten.mc.player.getY() + 0.5, Flatten.mc.player.getZ() + 0.5).down();
        BlockPos pos3 = new BlockPosX(Flatten.mc.player.getX() + 0.5, Flatten.mc.player.getY() + 0.5, Flatten.mc.player.getZ() - 0.5).down();
        BlockPos pos4 = new BlockPosX(Flatten.mc.player.getX() - 0.5, Flatten.mc.player.getY() + 0.5, Flatten.mc.player.getZ() - 0.5).down();
        if (!(this.canPlace(pos1) || this.canPlace(pos2) || this.canPlace(pos3) || this.canPlace(pos4))) {
            return;
        }
        CombatUtil.attackCrystal(pos1, this.rotate.getValue(), this.usingPause.getValue());
        CombatUtil.attackCrystal(pos2, this.rotate.getValue(), this.usingPause.getValue());
        CombatUtil.attackCrystal(pos3, this.rotate.getValue(), this.usingPause.getValue());
        CombatUtil.attackCrystal(pos4, this.rotate.getValue(), this.usingPause.getValue());
        this.doSwap(block);
        this.tryPlaceObsidian(pos1, this.rotate.getValue());
        this.tryPlaceObsidian(pos2, this.rotate.getValue());
        this.tryPlaceObsidian(pos3, this.rotate.getValue());
        this.tryPlaceObsidian(pos4, this.rotate.getValue());
        if (this.inventory.getValue()) {
            this.doSwap(block);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(oldSlot);
        }
    }

    private void tryPlaceObsidian(BlockPos pos, boolean rotate) {
        if (this.canPlace(pos)) {
            if (!((double)this.progress < this.blocksPer.getValue())) {
                return;
            }
            if (BlockUtil.allowAirPlace()) {
                BlockUtil.placedPos.add(pos);
                BlockUtil.airPlace(pos, rotate);
                this.timer.reset();
                ++this.progress;
                return;
            }
            Direction side = BlockUtil.getPlaceSide(pos);
            if (side == null) {
                return;
            }
            ++this.progress;
            BlockUtil.placedPos.add(pos);
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate);
            this.timer.reset();
        }
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, Flatten.mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private boolean canPlace(BlockPos pos) {
        if (this.checkMine.getValue() && Frog.BREAK.isMining(pos)) {
            return false;
        }
        if (this.cover.getValue() && Flatten.mc.world.isAir(pos.up())) {
            return false;
        }
        if (BlockUtil.getPlaceSide(pos) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !this.hasEntity(pos);
    }

    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : BlockUtil.getEntities(new Box(pos))) {
            if (entity == Flatten.mc.player || !entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof EndCrystalEntity) continue;
            return true;
        }
        return false;
    }

    private int getBlock() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        }
        return InventoryUtil.findBlock(Blocks.OBSIDIAN);
    }
}

