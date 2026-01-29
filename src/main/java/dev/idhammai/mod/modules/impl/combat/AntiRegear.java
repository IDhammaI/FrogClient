/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.ShulkerBoxBlock
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.block.entity.ShulkerBoxBlockEntity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.PlaceBlockEvent;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.player.PacketMine;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class AntiRegear
extends Module {
    public static AntiRegear INSTANCE;
    public final List<BlockPos> safe = new ArrayList<BlockPos>();
    private final SliderSetting safeRange = this.add(new SliderSetting("SafeRange", 2.0, 0.0, 8.0, 0.1));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 0.0, 8.0, 0.1));
    private final BooleanSetting checkSelf = this.add(new BooleanSetting("CheckSelf", true));

    public AntiRegear() {
        super("AntiRegear", Module.Category.Combat);
        this.setChinese("\u53cd\u8865\u7ed9");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (PacketMine.getBreakPos() != null && AntiRegear.mc.world.getBlockState(PacketMine.getBreakPos()).getBlock() instanceof ShulkerBoxBlock) {
            return;
        }
        this.safe.removeIf(pos -> !(AntiRegear.mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock));
        if (this.getBlock() != null) {
            PacketMine.INSTANCE.mine(this.getBlock().getPos());
        }
    }

    @EventListener
    public void onPlace(PlaceBlockEvent event) {
        if (event.block instanceof ShulkerBoxBlock) {
            this.safe.add(event.blockPos);
        }
    }

    private ShulkerBoxBlockEntity getBlock() {
        for (BlockEntity entity : BlockUtil.getTileEntities()) {
            ShulkerBoxBlockEntity shulker;
            if (!(entity instanceof ShulkerBoxBlockEntity) || (double)MathHelper.sqrt((float)((float)AntiRegear.mc.player.squaredDistanceTo((shulker = (ShulkerBoxBlockEntity)entity).getPos().toCenterPos()))) <= this.safeRange.getValue() || this.checkSelf.getValue() && (this.safe.contains(shulker.getPos()) || shulker.getPos().equals((Object)AutoRegear.INSTANCE.placePos) && !AutoRegear.INSTANCE.timeoutTimer.passed(100L)) || !((double)MathHelper.sqrt((float)((float)AntiRegear.mc.player.squaredDistanceTo(shulker.getPos().toCenterPos()))) <= this.range.getValue())) continue;
            return shulker;
        }
        return null;
    }
}

