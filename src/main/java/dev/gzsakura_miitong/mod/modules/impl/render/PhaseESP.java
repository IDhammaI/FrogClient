/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.world.BlockView
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.render.Render3DUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.BlockView;

public class PhaseESP
extends Module {
    public static PhaseESP INSTANCE;
    private final SliderSetting getDistance = this.add(new SliderSetting("Distance", 0.1, 0.0, 1.0, 0.1));
    private final SliderSetting bevelDistance = this.add(new SliderSetting("BevelDistance", 0.2, 0.0, 1.0, 0.1));
    private final ColorSetting safeFill = this.add(new ColorSetting("SafeFill", new Color(0, 255, 0, 50)).injectBoolean(true));
    private final ColorSetting safeBox = this.add(new ColorSetting("SafeBox", new Color(0, 255, 0, 100)).injectBoolean(true));
    private final ColorSetting semiSafeFill = this.add(new ColorSetting("SemiSafeFill", new Color(244, 255, 0, 50)).injectBoolean(true));
    private final ColorSetting semiSafeBox = this.add(new ColorSetting("SemiSafeBox", new Color(244, 255, 0, 100)).injectBoolean(true));
    private final ColorSetting unsafeFill = this.add(new ColorSetting("UnsafeFill", new Color(148, 0, 0, 50)).injectBoolean(true));
    private final ColorSetting unsafeBox = this.add(new ColorSetting("UnsafeBox", new Color(148, 0, 0, 100)).injectBoolean(true));
    List<BlockPos> safe = new ArrayList<BlockPos>();
    List<BlockPos> semiSafe = new ArrayList<BlockPos>();
    List<BlockPos> unsafe = new ArrayList<BlockPos>();
    int[] offsets = new int[]{1, 0, -1};

    public PhaseESP() {
        super("PhaseESP", Module.Category.Render);
        this.setChinese("\u7a7f\u5899\u663e\u793a");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.safe.clear();
        this.semiSafe.clear();
        this.unsafe.clear();
        for (int x : this.offsets) {
            for (int z : this.offsets) {
                Block downBlock;
                BlockPos pos = PhaseESP.mc.player.getBlockPos().add(x, 0, z);
                double d = PhaseESP.mc.player.getPos().distanceTo(pos.toBottomCenterPos());
                double d2 = x != 0 && z != 0 ? this.bevelDistance.getValue() + 1.0 : this.getDistance.getValue() + 0.8;
                if (!(d <= d2)) continue;
                BlockState blockState = PhaseESP.mc.world.getBlockState(pos);
                BlockPos downPos = pos.down();
                if (blockState.getBlock() == Blocks.BEDROCK) {
                    downBlock = PhaseESP.mc.world.getBlockState(downPos).getBlock();
                    if (downBlock == Blocks.BEDROCK) {
                        this.safe.add(pos);
                        continue;
                    }
                    this.unsafe.add(pos);
                    continue;
                }
                if (!blockState.isFullCube((BlockView)PhaseESP.mc.world, pos)) continue;
                downBlock = PhaseESP.mc.world.getBlockState(downPos).getBlock();
                if (downBlock == Blocks.BEDROCK) {
                    this.semiSafe.add(pos);
                    continue;
                }
                this.unsafe.add(pos);
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        this.draw(matrixStack, this.safe, this.safeFill, this.safeBox);
        this.draw(matrixStack, this.unsafe, this.unsafeFill, this.unsafeBox);
        this.draw(matrixStack, this.semiSafe, this.semiSafeFill, this.semiSafeBox);
    }

    private void draw(MatrixStack matrixStack, List<BlockPos> list, ColorSetting fill, ColorSetting box) {
        for (BlockPos pos : list) {
            Box espBox = new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)pos.getY(), (double)(pos.getZ() + 1));
            if (fill.booleanValue) {
                Render3DUtil.drawFill(matrixStack, espBox, fill.getValue());
            }
            if (!box.booleanValue) continue;
            Render3DUtil.drawBox(matrixStack, espBox, box.getValue());
        }
    }

    public static enum Type {
        None,
        Air,
        Normal,
        Bedrock;

    }
}

