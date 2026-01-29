/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.projectile.FishingBobberEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Direction$AxisDirection
 *  net.minecraft.world.World
 */
package dev.idhammai.mod.modules.impl.movement;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.EntityVelocityUpdateEvent;
import dev.idhammai.api.events.impl.PacketEvent;
import dev.idhammai.api.events.impl.TickEvent;
import dev.idhammai.api.events.impl.UpdateRotateEvent;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.MovementUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.exploit.Blink;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class Velocity
extends Module {
    public static Velocity INSTANCE;
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Plain));
    private final SliderSetting lagPause = this.add(new SliderSetting("LagPause", 50, 0, 500, () -> this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)));
    public final BooleanSetting ignorePearlLag = this.add(new BooleanSetting("IgnorePearlLag", true, () -> this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)).setParent());
    private final SliderSetting phaseTime = this.add(new SliderSetting("PhaseTime", 250, 0, 1000, () -> (this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)) && this.ignorePearlLag.isOpen()));
    public final BooleanSetting noRotation = this.add(new BooleanSetting("NoRotation", false, () -> this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)));
    public final BooleanSetting flagInWall = this.add(new BooleanSetting("FlagInWall", false, () -> this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)).setParent());
    public final BooleanSetting whenPushOutOfBlocks = this.add(new BooleanSetting("WhilePushOut", false, () -> (this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)) && this.flagInWall.isOpen()));
    public final BooleanSetting staticSetting = this.add(new BooleanSetting("Static", false, () -> this.mode.is(Mode.Grim)));
    public final BooleanSetting cancelAll = this.add(new BooleanSetting("CancelAll", false, () -> !this.mode.is(Mode.None)));
    private final SliderSetting horizontal = this.add(new SliderSetting("Horizontal", 0.0, 0.0, 100.0, 1.0, () -> !this.mode.is(Mode.None) && !this.cancelAll.getValue()));
    private final SliderSetting vertical = this.add(new SliderSetting("Vertical", 0.0, 0.0, 100.0, 1.0, () -> !this.mode.is(Mode.None) && !this.cancelAll.getValue()));
    public final BooleanSetting whileLiquid = this.add(new BooleanSetting("WhileLiquid", false));
    public final BooleanSetting whileElytra = this.add(new BooleanSetting("FallFlying", false));
    public final BooleanSetting noClimb = this.add(new BooleanSetting("NoClimb", false));
    public final BooleanSetting waterPush = this.add(new BooleanSetting("NoWaterPush", false));
    public final BooleanSetting entityPush = this.add(new BooleanSetting("NoEntityPush", true));
    public final BooleanSetting blockPush = this.add(new BooleanSetting("NoBlockPush", true));
    public final BooleanSetting fishBob = this.add(new BooleanSetting("NoFishBob", true));
    public final Timer pearlTimer = new Timer();
    private final Timer lagBackTimer = new Timer();
    private boolean flag;
    static boolean pushOutOfBlocks;

    public Velocity() {
        super("Velocity", Module.Category.Movement);
        this.setChinese("\u53cd\u51fb\u9000");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        if (this.mode.is(Mode.None)) {
            return null;
        }
        return this.mode.getValue().name() + ", " + (String)(this.cancelAll.getValue() ? "Cancel" : this.horizontal.getValueInt() + "%, " + this.vertical.getValueInt() + "%");
    }

    @EventListener
    public void onRotate(UpdateRotateEvent event) {
        if (this.noRotation()) {
            event.setRotation(Frog.ROTATION.rotationYaw, 89.0f);
        }
    }

    public boolean noRotation() {
        return this.isOn() && (this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)) && EntityUtil.isInsideBlock() && this.noRotation.getValue();
    }

    @EventListener
    public void onVelocity(EntityVelocityUpdateEvent event) {
        if (Velocity.nullCheck()) {
            return;
        }
        if (event.getEntity() != Velocity.mc.player) {
            return;
        }
        if (this.mode.is(Mode.None)) {
            return;
        }
        if (Velocity.mc.player.isInFluid() && !this.whileLiquid.getValue()) {
            return;
        }
        if (Velocity.mc.player.isFallFlying() && !this.whileElytra.getValue()) {
            return;
        }
        switch (this.mode.getValue().ordinal()) {
            case 1: {
                if (!this.lagBackTimer.passedMs(this.lagPause.getValue())) {
                    return;
                }
                if (EntityUtil.isInsideBlock() || this.getPos() != null || this.staticSetting.getValue() && MovementUtil.isStatic()) {
                    if (event.getX() == 0.0 && event.getZ() == 0.0) break;
                    this.flag = true;
                    break;
                }
                return;
            }
            case 2: {
                if (!this.lagBackTimer.passedMs(this.lagPause.getValue())) {
                    return;
                }
                if (EntityUtil.isInsideBlock()) {
                    if (event.getX() == 0.0 && event.getZ() == 0.0) break;
                    this.flag = true;
                    break;
                }
                return;
            }
        }
        if (this.cancelAll.getValue()) {
            event.cancel();
        } else {
            double h = (double)this.horizontal.getValueInt() / 100.0;
            double v = (double)this.vertical.getValueInt() / 100.0;
            event.setX(event.getX() * h);
            event.setZ(event.getZ() * h);
            event.setY(event.getY() * v);
        }
    }

    @EventListener
    public void onReceivePacket(PacketEvent.Receive event) {
        FishingBobberEntity fishHook;
        EntityStatusS2CPacket packet;
        Entity entity;
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && (!this.ignorePearlLag.getValue() || this.pearlTimer.passed(this.phaseTime.getValueInt()))) {
            this.lagBackTimer.reset();
        }
        if (Velocity.nullCheck()) {
            return;
        }
        if (Velocity.mc.player.isInFluid() && !this.whileLiquid.getValue()) {
            return;
        }
        if (this.fishBob.getValue() && event.getPacket() instanceof EntityStatusS2CPacket) {
            packet = (EntityStatusS2CPacket)event.getPacket();
            if (packet.getStatus() == 31) {
                entity = packet.getEntity((World)Velocity.mc.world);
                if (entity instanceof FishingBobberEntity) {
                    fishHook = (FishingBobberEntity)entity;
                    if (fishHook.getHookedEntity() == Velocity.mc.player) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventListener
    public void onUpdate(TickEvent event) {
        if (Velocity.nullCheck()) {
            return;
        }
        if (event.isPost() || Velocity.mc.player.isInFluid() && !this.whileLiquid.getValue()) {
            return;
        }
        if (this.flagInWall.getValue()) {
            pushOutOfBlocks = false;
            Velocity.pushOutOfBlocks(Velocity.mc.player.getX() - (double)Velocity.mc.player.getWidth() * 0.35, Velocity.mc.player.getZ() + (double)Velocity.mc.player.getWidth() * 0.35);
            Velocity.pushOutOfBlocks(Velocity.mc.player.getX() - (double)Velocity.mc.player.getWidth() * 0.35, Velocity.mc.player.getZ() - (double)Velocity.mc.player.getWidth() * 0.35);
            Velocity.pushOutOfBlocks(Velocity.mc.player.getX() + (double)Velocity.mc.player.getWidth() * 0.35, Velocity.mc.player.getZ() - (double)Velocity.mc.player.getWidth() * 0.35);
            Velocity.pushOutOfBlocks(Velocity.mc.player.getX() + (double)Velocity.mc.player.getWidth() * 0.35, Velocity.mc.player.getZ() + (double)Velocity.mc.player.getWidth() * 0.35);
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.flag) {
            if (this.lagBackTimer.passedMs(this.lagPause.getValue()) && (this.flagInWall.getValue() && (!pushOutOfBlocks || this.whenPushOutOfBlocks.getValue()) || !EntityUtil.isInsideBlock())) {
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.Full(Velocity.mc.player.getX(), Velocity.mc.player.getY(), Velocity.mc.player.getZ(), Frog.ROTATION.rotationYaw, Frog.ROTATION.rotationPitch, Velocity.mc.player.isOnGround()));
                BlockPos pos = this.getPos();
                if (pos != null) {
                    mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Velocity.mc.player.getHorizontalFacing().getOpposite()));
                }
            }
            this.flag = false;
        }
    }

    public BlockPos getPos() {
        if (Velocity.mc.world.getBlockState(Velocity.mc.player.getBlockPos().down()).getBlock() == Blocks.OBSIDIAN) {
            return Velocity.mc.player.getBlockPos().down();
        }
        return null;
    }

    private static void pushOutOfBlocks(double x, double z) {
        BlockPos blockPos = BlockPos.ofFloored((double)x, (double)Velocity.mc.player.getY(), (double)z);
        if (Velocity.wouldCollideAt(blockPos)) {
            Direction[] directions;
            double d = x - (double)blockPos.getX();
            double e = z - (double)blockPos.getZ();
            Direction direction = null;
            double f = Double.MAX_VALUE;
            for (Direction direction2 : directions = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}) {
                double h;
                double g = direction2.getAxis().choose(d, 0.0, e);
                double d2 = h = direction2.getDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - g : g;
                if (!(h < f) || Velocity.wouldCollideAt(blockPos.offset(direction2))) continue;
                f = h;
                direction = direction2;
            }
            if (direction != null) {
                pushOutOfBlocks = true;
            }
        }
    }

    private static boolean wouldCollideAt(BlockPos pos) {
        Box box = Velocity.mc.player.getBoundingBox();
        Box box2 = new Box((double)pos.getX(), box.minY, (double)pos.getZ(), (double)pos.getX() + 1.0, box.maxY, (double)pos.getZ() + 1.0).contract(1.0E-7);
        return Velocity.mc.player.getWorld().canCollide((Entity)Velocity.mc.player, box2);
    }

    static {
        pushOutOfBlocks = false;
    }

    public static enum Mode {
        Plain,
        Grim,
        Wall,
        None;

    }
}

