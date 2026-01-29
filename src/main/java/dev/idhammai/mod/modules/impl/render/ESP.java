/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.block.entity.ChestBlockEntity
 *  net.minecraft.block.entity.EndPortalBlockEntity
 *  net.minecraft.block.entity.EnderChestBlockEntity
 *  net.minecraft.block.entity.ShulkerBoxBlockEntity
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.thrown.EnderPearlEntity
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 */
package dev.idhammai.mod.modules.impl.render;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.EntitySpawnedEvent;
import dev.idhammai.api.utils.math.MathUtil;
import dev.idhammai.api.utils.render.Render3DUtil;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.asm.accessors.IEntity;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ESP
extends Module {
    public static ESP INSTANCE;
    public final BooleanSetting box = this.add(new BooleanSetting("BoxESP", true).setParent());
    private final ColorSetting endPortalFill = this.add(new ColorSetting("EndPortalFill", new Color(255, 243, 129, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting endPortalOutline = this.add(new ColorSetting("EndPortalOutline", new Color(255, 243, 129, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting itemFill = this.add(new ColorSetting("ItemFill", new Color(255, 255, 255, 100), this.box::isOpen).injectBoolean(true));
    private final ColorSetting itemOutline = this.add(new ColorSetting("ItemOutline", new Color(255, 255, 255, 100), this.box::isOpen).injectBoolean(true));
    private final ColorSetting playerFill = this.add(new ColorSetting("PlayerFill", new Color(255, 255, 255, 100), this.box::isOpen).injectBoolean(true));
    private final ColorSetting playerOutline = this.add(new ColorSetting("PlayerOutline", new Color(255, 255, 255, 100), this.box::isOpen).injectBoolean(true));
    private final ColorSetting chestFill = this.add(new ColorSetting("ChestFill", new Color(255, 198, 123, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting chestOutline = this.add(new ColorSetting("ChestOutline", new Color(255, 198, 123, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting enderChestFill = this.add(new ColorSetting("EnderChestFill", new Color(255, 100, 255, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting enderChestOutline = this.add(new ColorSetting("EnderChestOutline", new Color(255, 100, 255, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting shulkerBoxFill = this.add(new ColorSetting("ShulkerBoxFill", new Color(15, 255, 255, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting shulkerBoxOutline = this.add(new ColorSetting("ShulkerBoxOutline", new Color(15, 255, 255, 100), this.box::isOpen).injectBoolean(false));
    public final BooleanSetting item = this.add(new BooleanSetting("ItemName", false).setParent());
    public final BooleanSetting customName = this.add(new BooleanSetting("CustomName", false, this.item::isOpen));
    public final BooleanSetting count = this.add(new BooleanSetting("Count", true, this.item::isOpen));
    private final ColorSetting text = this.add(new ColorSetting("Text", new Color(255, 255, 255, 255), this.item::isOpen));
    public final BooleanSetting pearl = this.add(new BooleanSetting("PearlOwner", true));

    public ESP() {
        super("ESP", Module.Category.Render);
        this.setChinese("\u900f\u89c6");
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (this.item.getValue()) {
            for (Entity entity : Frog.THREAD.getEntities()) {
                if (!(entity instanceof ItemEntity)) continue;
                ItemEntity itemEntity = (ItemEntity)entity;
                int itemCount = itemEntity.getStack().getCount();
                String s = this.count.getValue() && itemCount > 1 ? " x" + itemCount : "";
                String name = (this.customName.getValue() ? itemEntity.getStack().getName() : itemEntity.getStack().getItem().getName()).getString();
                Render3DUtil.drawText3D(name + s, ((IEntity)itemEntity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(itemEntity.lastRenderX, itemEntity.getX(), (double)mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(itemEntity.lastRenderY, itemEntity.getY(), (double)mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(itemEntity.lastRenderZ, itemEntity.getZ(), (double)mc.getRenderTickCounter().getTickDelta(true)))).expand(0.0, 0.1, 0.0).getCenter().add(0.0, 0.5, 0.0), this.text.getValue());
            }
        }
        if (this.box.getValue()) {
            if (this.itemFill.booleanValue || this.playerFill.booleanValue) {
                for (Entity entity : Frog.THREAD.getEntities()) {
                    Color color;
                    if (entity instanceof ItemEntity && (this.itemFill.booleanValue || this.itemOutline.booleanValue)) {
                        color = this.itemFill.getValue();
                        Render3DUtil.draw3DBox(matrixStack, ((IEntity)entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), (double)mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderY, entity.getY(), (double)mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), (double)mc.getRenderTickCounter().getTickDelta(true)))), color, this.itemOutline.getValue(), this.itemOutline.booleanValue, this.itemFill.booleanValue);
                        continue;
                    }
                    if (!(entity instanceof PlayerEntity) || !this.playerFill.booleanValue && !this.playerOutline.booleanValue) continue;
                    color = this.playerFill.getValue();
                    Render3DUtil.draw3DBox(matrixStack, ((IEntity)entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), (double)mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderY, entity.getY(), (double)mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), (double)mc.getRenderTickCounter().getTickDelta(true)))).expand(0.0, 0.1, 0.0), color, this.playerOutline.getValue(), this.playerOutline.booleanValue, this.playerFill.booleanValue);
                }
            }
            ArrayList<BlockEntity> blockEntities = BlockUtil.getTileEntities();
            for (BlockEntity blockEntity : blockEntities) {
                Box box;
                if (blockEntity instanceof ChestBlockEntity && (this.chestFill.booleanValue || this.chestOutline.booleanValue)) {
                    box = new Box(blockEntity.getPos());
                    Render3DUtil.draw3DBox(matrixStack, box, this.chestFill.getValue(), this.chestOutline.getValue(), this.chestOutline.booleanValue, this.chestFill.booleanValue);
                    continue;
                }
                if (blockEntity instanceof EnderChestBlockEntity && (this.enderChestFill.booleanValue || this.enderChestOutline.booleanValue)) {
                    box = new Box(blockEntity.getPos());
                    Render3DUtil.draw3DBox(matrixStack, box, this.enderChestFill.getValue(), this.enderChestOutline.getValue(), this.enderChestOutline.booleanValue, this.enderChestFill.booleanValue);
                    continue;
                }
                if (blockEntity instanceof ShulkerBoxBlockEntity && (this.shulkerBoxFill.booleanValue || this.shulkerBoxOutline.booleanValue)) {
                    box = new Box(blockEntity.getPos());
                    Render3DUtil.draw3DBox(matrixStack, box, this.shulkerBoxFill.getValue(), this.shulkerBoxOutline.getValue(), this.shulkerBoxOutline.booleanValue, this.shulkerBoxFill.booleanValue);
                    continue;
                }
                if (!(blockEntity instanceof EndPortalBlockEntity) || !this.endPortalFill.booleanValue && !this.endPortalOutline.booleanValue) continue;
                box = new Box(blockEntity.getPos());
                Render3DUtil.draw3DBox(matrixStack, box, this.endPortalFill.getValue(), this.endPortalOutline.getValue(), this.endPortalOutline.booleanValue, this.endPortalFill.booleanValue);
            }
        }
    }

    @EventListener
    public void onReceivePacket(EntitySpawnedEvent event) {
        Entity entity;
        if (ESP.nullCheck()) {
            return;
        }
        if (this.pearl.getValue() && (entity = event.getEntity()) instanceof EnderPearlEntity) {
            EnderPearlEntity pearlEntity = (EnderPearlEntity)entity;
            if (pearlEntity.getOwner() != null) {
                pearlEntity.setCustomName(pearlEntity.getOwner().getName());
                pearlEntity.setCustomNameVisible(true);
            } else {
                ESP.mc.world.getPlayers().stream().min(Comparator.comparingDouble(p -> p.getPos().distanceTo(new Vec3d(pearlEntity.getX(), pearlEntity.getY(), pearlEntity.getZ())))).ifPresent(player -> {
                    pearlEntity.setCustomName(player.getName());
                    pearlEntity.setCustomNameVisible(true);
                });
            }
        }
    }
}

