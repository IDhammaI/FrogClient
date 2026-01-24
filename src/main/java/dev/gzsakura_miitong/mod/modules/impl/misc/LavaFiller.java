/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.fluid.LavaFluid$Still
 *  net.minecraft.util.math.BlockPos
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.player.EntityUtil;
import dev.gzsakura_miitong.api.utils.player.InventoryUtil;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;

public class LavaFiller
extends Module {
    public final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 50, 0, 500).setSuffix("ms"));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 0.0, 8.0, 0.1));
    private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 1, 1, 8));
    private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private final BooleanSetting packetPlace = this.add(new BooleanSetting("PacketPlace", true));
    private final Timer timer = new Timer();
    int progress = 0;

    public LavaFiller() {
        super("LavaFiller", Module.Category.Misc);
        this.setChinese("\u81ea\u52a8\u586b\u5ca9\u6d46");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!this.timer.passed((long)this.placeDelay.getValue())) {
            return;
        }
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        this.progress = 0;
        if (this.getBlock() == -1) {
            return;
        }
        if (this.usingPause.getValue() && LavaFiller.mc.player.isUsingItem()) {
            return;
        }
        for (BlockPos pos : BlockUtil.getSphere(this.range.getValueFloat())) {
            if (LavaFiller.mc.world.getBlockState(pos).getBlock() != Blocks.LAVA || !(LavaFiller.mc.world.getBlockState(pos).getFluidState().getFluid() instanceof LavaFluid.Still)) continue;
            this.tryPlaceBlock(pos);
        }
    }

    private void tryPlaceBlock(BlockPos pos) {
        if (pos == null) {
            return;
        }
        if (this.detectMining.getValue() && Alien.BREAK.isMining(pos)) {
            return;
        }
        if (!((double)this.progress < this.blocksPer.getValue())) {
            return;
        }
        int block = this.getBlock();
        if (block == -1) {
            return;
        }
        if (!BlockUtil.canPlace(pos, this.range.getValue(), false)) {
            return;
        }
        int old = LavaFiller.mc.player.getInventory().selectedSlot;
        this.doSwap(block);
        BlockUtil.placeBlock(pos, this.rotate.getValue(), this.packetPlace.getValue());
        if (this.inventory.getValue()) {
            this.doSwap(block);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(old);
        }
        ++this.progress;
        this.timer.reset();
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, LavaFiller.mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        }
        return InventoryUtil.findBlock(Blocks.OBSIDIAN);
    }
}

