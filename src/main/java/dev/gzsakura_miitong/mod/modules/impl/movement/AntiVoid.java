/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.BlockPos
 */
package dev.gzsakura_miitong.mod.modules.impl.movement;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.player.MovementUtil;
import dev.gzsakura_miitong.api.utils.world.BlockPosX;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class AntiVoid
extends Module {
    private final SliderSetting voidHeight = this.add(new SliderSetting("VoidHeight", -64.0, -64.0, 319.0, 1.0));
    private final SliderSetting height = this.add(new SliderSetting("Height", 100.0, -40.0, 256.0, 1.0));

    public AntiVoid() {
        super("AntiVoid", "Allows you to fly over void blocks", Module.Category.Movement);
        this.setChinese("\u53cd\u865a\u7a7a");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        boolean isVoid = true;
        for (int i = (int)AntiVoid.mc.player.getY(); i > this.voidHeight.getValueInt() - 1; --i) {
            if (AntiVoid.mc.world.getBlockState((BlockPos)new BlockPosX(AntiVoid.mc.player.getX(), i, AntiVoid.mc.player.getZ())).isAir() || AntiVoid.mc.world.getBlockState((BlockPos)new BlockPosX(AntiVoid.mc.player.getX(), i, AntiVoid.mc.player.getZ())).getBlock() == Blocks.VOID_AIR) continue;
            isVoid = false;
            break;
        }
        if (AntiVoid.mc.player.getY() < this.height.getValue() + this.voidHeight.getValue() && isVoid) {
            MovementUtil.setMotionY(0.0);
        }
    }
}

