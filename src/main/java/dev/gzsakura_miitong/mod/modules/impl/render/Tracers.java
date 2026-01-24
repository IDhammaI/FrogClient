/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.block.entity.ChestBlockEntity
 *  net.minecraft.block.entity.EnderChestBlockEntity
 *  net.minecraft.block.entity.ShulkerBoxBlockEntity
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.Vec3d
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.utils.render.Render3DUtil;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Tracers
extends Module {
    private final ColorSetting item = this.add(new ColorSetting("Item", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting player = this.add(new ColorSetting("Player", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting chest = this.add(new ColorSetting("Chest", new Color(255, 255, 255, 100)).injectBoolean(false));
    private final ColorSetting enderChest = this.add(new ColorSetting("EnderChest", new Color(255, 100, 255, 100)).injectBoolean(false));
    private final ColorSetting shulkerBox = this.add(new ColorSetting("ShulkerBox", new Color(15, 255, 255, 100)).injectBoolean(false));

    public Tracers() {
        super("Tracers", Module.Category.Render);
        this.setChinese("\u8ffd\u8e2a\u8005");
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        Tracers.mc.options.getBobView().setValue(false);
        if (this.item.booleanValue || this.player.booleanValue) {
            for (Entity entity : Alien.THREAD.getEntities()) {
                if (entity instanceof ItemEntity && this.item.booleanValue) {
                    this.drawLine(entity.getPos(), this.item.getValue());
                    continue;
                }
                if (!(entity instanceof PlayerEntity) || !this.player.booleanValue || entity == Tracers.mc.player) continue;
                this.drawLine(entity.getPos(), this.player.getValue());
            }
        }
        ArrayList<BlockEntity> blockEntities = BlockUtil.getTileEntities();
        for (BlockEntity blockEntity : blockEntities) {
            if (blockEntity instanceof ChestBlockEntity && this.chest.booleanValue) {
                this.drawLine(blockEntity.getPos().toCenterPos(), this.chest.getValue());
                continue;
            }
            if (blockEntity instanceof EnderChestBlockEntity && this.enderChest.booleanValue) {
                this.drawLine(blockEntity.getPos().toCenterPos(), this.enderChest.getValue());
                continue;
            }
            if (!(blockEntity instanceof ShulkerBoxBlockEntity) || !this.shulkerBox.booleanValue) continue;
            this.drawLine(blockEntity.getPos().toCenterPos(), this.shulkerBox.getValue());
        }
    }

    private void drawLine(Vec3d pos, Color color) {
        Render3DUtil.drawLine(pos, Tracers.mc.gameRenderer.getCamera().getPos().add(Vec3d.fromPolar((float)Tracers.mc.player.getPitch(mc.getRenderTickCounter().getTickDelta(true)), (float)Tracers.mc.player.getYaw(mc.getRenderTickCounter().getTickDelta(true))).multiply(0.2)), color);
    }
}

