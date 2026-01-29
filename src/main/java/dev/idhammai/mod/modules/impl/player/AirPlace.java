/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.BlockItem
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.math.Box
 */
package dev.idhammai.mod.modules.impl.player;

import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.render.Render3DUtil;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;

public class AirPlace
extends Module {
    public static AirPlace INSTANCE;
    public final BooleanSetting module = this.add(new BooleanSetting("Module", true));
    public final BooleanSetting grimBypass = this.add(new BooleanSetting("GrimBypass", false));
    public final BooleanSetting crossHair = this.add(new BooleanSetting("Crosshair", true).setParent());
    private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 0.0, 6.0, this.crossHair::isOpen));
    private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(255, 0, 0, 50), this.crossHair::isOpen).injectBoolean(true));
    private final ColorSetting box = this.add(new ColorSetting("Box", new Color(255, 0, 0, 100), this.crossHair::isOpen).injectBoolean(true));
    private BlockHitResult hit;
    private int cooldown;

    public AirPlace() {
        super("AirPlace", Module.Category.Player);
        this.setChinese("\u7a7a\u6c14\u653e\u7f6e");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.crossHair.getValue()) {
            BlockHitResult bhr;
            HitResult hitResult;
            if (this.cooldown > 0) {
                --this.cooldown;
            }
            this.hit = (hitResult = mc.getCameraEntity().raycast(this.range.getValue(), 0.0f, false)) instanceof BlockHitResult ? (bhr = (BlockHitResult)hitResult) : null;
            if (this.hit == null || !AirPlace.mc.world.getBlockState(this.hit.getBlockPos()).getBlock().equals(Blocks.AIR) || !(AirPlace.mc.player.getMainHandStack().getItem() instanceof BlockItem)) {
                return;
            }
            boolean main = AirPlace.mc.player.getMainHandStack().getItem() instanceof BlockItem;
            if (AirPlace.mc.options.useKey.isPressed() && main && this.cooldown <= 0) {
                BlockUtil.airPlace(this.hit.getBlockPos(), false);
                this.cooldown = 2;
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (this.crossHair.getValue()) {
            if (this.hit == null || !AirPlace.mc.world.getBlockState(this.hit.getBlockPos()).getBlock().equals(Blocks.AIR) || !(AirPlace.mc.player.getMainHandStack().getItem() instanceof BlockItem)) {
                return;
            }
            Render3DUtil.draw3DBox(stack, new Box(this.hit.getBlockPos()), this.fill.getValue(), this.box.getValue(), this.box.booleanValue, this.fill.booleanValue);
        }
    }
}

