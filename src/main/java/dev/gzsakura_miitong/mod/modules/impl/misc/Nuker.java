/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.player.PacketMine;
import dev.gzsakura_miitong.mod.modules.impl.render.PlaceRender;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Nuker
extends Module {
    private final SliderSetting range = this.add(new SliderSetting("Range", 4.0, 0.0, 8.0, 0.1));
    private final BooleanSetting down = this.add(new BooleanSetting("Down", false));
    private final BooleanSetting sand = this.add(new BooleanSetting("Sand", false));
    private final SliderSetting breaks = this.add(new SliderSetting("Breaks", 10, 0, 20, this.sand::getValue));

    public Nuker() {
        super("Nuker", Module.Category.Misc);
        this.setChinese("\u8303\u56f4\u6316\u6398");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (PacketMine.getBreakPos() != null && !Nuker.mc.world.isAir(PacketMine.getBreakPos())) {
            return;
        }
        if (this.sand.getValue()) {
            if (!Nuker.mc.player.isOnGround()) {
                return;
            }
            int elementCodec = 0;
            for (BlockPos sand : BlockUtil.getSphere(this.range.getValueFloat(), Nuker.mc.player.getEyePos())) {
                Direction side;
                if (Blocks.SAND != Nuker.mc.world.getBlockState(sand).getBlock() && Blocks.RED_SAND != Nuker.mc.world.getBlockState(sand).getBlock() || (side = BlockUtil.getClickSideStrict(sand)) == null) continue;
                PlaceRender.INSTANCE.create(sand);
                Alien.ROTATION.snapAt(sand.toCenterPos());
                Nuker.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, sand, side, id));
                Nuker.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, sand, side, id));
                Nuker.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, sand, side, id));
                Alien.ROTATION.snapBack();
                if (!((double)(++elementCodec) >= this.breaks.getValue())) continue;
                return;
            }
        } else {
            BlockPos pos = this.getBlock();
            if (pos != null) {
                PacketMine.INSTANCE.mine(pos);
            }
        }
    }

    private BlockPos getBlock() {
        BlockPos down = null;
        for (BlockPos pos : BlockUtil.getSphere(this.range.getValueFloat(), Nuker.mc.player.getEyePos())) {
            if (Nuker.mc.world.isAir(pos) || PacketMine.unbreakable(pos) || BlockUtil.getClickSideStrict(pos) == null) continue;
            if ((double)pos.getY() < Nuker.mc.player.getY()) {
                if (down != null || !this.down.getValue()) continue;
                down = pos;
                continue;
            }
            return pos;
        }
        return down;
    }
}

