/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.CobwebBlock
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package dev.idhammai.mod.modules.impl.movement;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.TimerEvent;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.player.MovementUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class FastWeb
extends Module {
    public static FastWeb INSTANCE;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Vanilla));
    public final BooleanSetting onlySneak = this.add(new BooleanSetting("OnlySneak", true));
    public final BooleanSetting grim = this.add(new BooleanSetting("Grim", false).setParent());
    public final BooleanSetting abortPacket = this.add(new BooleanSetting("AbortPacket", true, this.grim::isOpen));
    public final SliderSetting xZSlow = this.add(new SliderSetting("XZSpeed", 25.0, 0.0, 100.0, 0.1, () -> this.mode.getValue() == Mode.Custom).setSuffix("%"));
    public final SliderSetting ySlow = this.add(new SliderSetting("YSpeed", 100.0, 0.0, 100.0, 0.1, () -> this.mode.getValue() == Mode.Custom).setSuffix("%"));
    private final SliderSetting fastSpeed = this.add(new SliderSetting("Speed", 3.0, 0.0, 8.0, () -> this.mode.getValue() == Mode.Vanilla || this.mode.getValue() == Mode.Strict));
    private boolean work = false;

    public FastWeb() {
        super("FastWeb", "So you don't need to keep timer on keybind", Module.Category.Movement);
        this.setChinese("\u8718\u86db\u7f51\u52a0\u901f");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        boolean bl = this.work = !FastWeb.mc.player.isOnGround() && (FastWeb.mc.options.sneakKey.isPressed() || !this.onlySneak.getValue()) && Frog.PLAYER.isInWeb((PlayerEntity)FastWeb.mc.player);
        if (this.work && this.mode.is(Mode.Vanilla)) {
            MovementUtil.setMotionY(-this.fastSpeed.getValue());
        }
        if (this.grim.getValue() && (FastWeb.mc.options.sneakKey.isPressed() || !this.onlySneak.getValue())) {
            for (BlockPos pos : this.getIntersectingWebs()) {
                if (this.abortPacket.getValue()) {
                    mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.DOWN));
                }
                mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
            }
        }
    }

    @EventListener(priority=-100)
    public void onTimer(TimerEvent event) {
        if (this.work && this.mode.getValue() == Mode.Strict) {
            event.set(this.fastSpeed.getValueFloat());
        }
    }

    public List<BlockPos> getIntersectingWebs() {
        int radius = 2;
        ArrayList<BlockPos> blocks = new ArrayList<BlockPos>();
        for (int x = radius; x > -radius; --x) {
            for (int y = radius; y > -radius; --y) {
                for (int z = radius; z > -radius; --z) {
                    BlockState state;
                    BlockPos blockPos = BlockPos.ofFloored((double)(FastWeb.mc.player.getX() + (double)x), (double)(FastWeb.mc.player.getY() + (double)y), (double)(FastWeb.mc.player.getZ() + (double)z));
                    if (FastWeb.mc.player.getPos().distanceTo(blockPos.toCenterPos()) > 1.0 && FastWeb.mc.player.getEyePos().distanceTo(blockPos.toCenterPos()) > 1.0 || !((state = FastWeb.mc.world.getBlockState(blockPos)).getBlock() instanceof CobwebBlock)) continue;
                    blocks.add(blockPos);
                }
            }
        }
        return blocks;
    }

    public static enum Mode {
        Vanilla,
        Strict,
        Custom,
        Ignore;

    }
}

