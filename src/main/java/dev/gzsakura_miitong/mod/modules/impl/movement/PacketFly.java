/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.ConcurrentSet
 *  net.minecraft.client.gui.screen.DownloadingTerrainScreen
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.math.Vec3d
 */
package dev.gzsakura_miitong.mod.modules.impl.movement;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.MoveEvent;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.TickEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.player.MovementUtil;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import io.netty.util.internal.ConcurrentSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class PacketFly
extends Module {
    public static PacketFly INSTANCE;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Factor));
    public final SliderSetting factor = this.add(new SliderSetting("Factor", 1.0, 0.0, 10.0));
    public final EnumSetting<Phase> phase = this.add(new EnumSetting<Phase>("Phase", Phase.Full));
    public final EnumSetting<Type> type = this.add(new EnumSetting<Type>("Type", Type.Up));
    public final BooleanSetting antiKick = this.add(new BooleanSetting("AntiKick", true));
    public final BooleanSetting noRotation = this.add(new BooleanSetting("NoRotation", false));
    public final BooleanSetting noMovePacket = this.add(new BooleanSetting("NoMovePacket", false));
    public final BooleanSetting bbOffset = this.add(new BooleanSetting("BB-Offset", false));
    public final SliderSetting invalidY = this.add(new SliderSetting("Invalid-Offset", 1337, 0, 1337));
    public final SliderSetting invalids = this.add(new SliderSetting("Invalids", 1, 0, 10));
    public final SliderSetting sendTeleport = this.add(new SliderSetting("Teleport", 1, 0, 10));
    public final SliderSetting concealY = this.add(new SliderSetting("C-Y", 0.0, -256.0, 256.0));
    public final SliderSetting conceal = this.add(new SliderSetting("C-Multiplier", 1.0, 0.0, 2.0));
    public final SliderSetting ySpeed = this.add(new SliderSetting("Y-Multiplier", 1.0, 0.0, 2.0));
    public final SliderSetting xzSpeed = this.add(new SliderSetting("X/Z-Multiplier", 1.0, 0.0, 2.0));
    public final BooleanSetting elytra = this.add(new BooleanSetting("Elytra", false));
    public final BooleanSetting xzJitter = this.add(new BooleanSetting("Jitter-XZ", false));
    public final BooleanSetting yJitter = this.add(new BooleanSetting("Jitter-Y", false));
    public final BooleanSetting zeroSpeed = this.add(new BooleanSetting("Zero-Speed", false));
    public final BooleanSetting zeroY = this.add(new BooleanSetting("Zero-Y", false));
    public final BooleanSetting zeroTeleport = this.add(new BooleanSetting("Zero-Teleport", true));
    public final SliderSetting zoomer = this.add(new SliderSetting("Zoomies", 3, 0, 10));
    public final Map<Integer, TimeVec> posLooks = new ConcurrentHashMap<Integer, TimeVec>();
    public final Set<Packet<?>> playerPackets = new ConcurrentSet();
    public final AtomicInteger teleportID = new AtomicInteger();
    public Vec3d vecDelServer;
    public int packetCounter;
    public boolean zoomies;
    public float lastFactor;
    public int zoomTimer = 0;

    public PacketFly() {
        super("PacketFly", Module.Category.Movement);
        this.setChinese("\u53d1\u5305\u98de\u884c");
        INSTANCE = this;
    }

    @Override
    public void onLogin() {
        this.disable();
        this.clearValues();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.posLooks.entrySet().removeIf(entry -> System.currentTimeMillis() - ((TimeVec)((Object)((Object)entry.getValue()))).getTime() > TimeUnit.SECONDS.toMillis(30L));
    }

    @EventListener
    public void invoke(TickEvent event) {
        if (event.isPre() && this.mode.getValue() != Mode.Compatibility) {
            double ySpeed = 0.0;
            MovementUtil.setMotionX(0.0);
            MovementUtil.setMotionY(0.0);
            MovementUtil.setMotionZ(0.0);
            if (this.mode.getValue() != Mode.Setback && this.teleportID.get() == 0) {
                if (this.checkPackets(6)) {
                    this.sendPackets(0.0, 0.0, 0.0, true);
                }
                return;
            }
            boolean isPhasing = this.isPlayerCollisionBoundingBoxEmpty();
            if (PacketFly.mc.player.input.jumping && (isPhasing || !MovementUtil.isMoving())) {
                ySpeed = this.antiKick.getValue() && !isPhasing ? (this.checkPackets(this.mode.getValue() == Mode.Setback ? 10 : 20) ? -0.032 : 0.062) : (this.yJitter.getValue() && this.zoomies ? 0.061 : 0.062);
            } else if (PacketFly.mc.player.input.sneaking) {
                ySpeed = this.yJitter.getValue() && this.zoomies ? -0.061 : -0.062;
            } else {
                double d = !isPhasing ? (this.checkPackets(4) ? (this.antiKick.getValue() ? -0.04 : 0.0) : 0.0) : (ySpeed = 0.0);
            }
            if (this.phase.getValue() == Phase.Full && isPhasing && MovementUtil.isMoving() && ySpeed != 0.0) {
                ySpeed /= 2.5;
            }
            double high = this.xzJitter.getValue() && this.zoomies ? 0.25 : 0.26;
            double low = this.xzJitter.getValue() && this.zoomies ? 0.03 : 0.031;
            double[] dirSpeed = MovementUtil.directionSpeed(this.phase.getValue() == Phase.Full && isPhasing ? low : high);
            if (this.mode.getValue() == Mode.Increment) {
                if ((double)this.lastFactor >= this.factor.getValue()) {
                    this.lastFactor = 1.0f;
                } else {
                    this.lastFactor += 1.0f;
                    if ((double)this.lastFactor > this.factor.getValue()) {
                        this.lastFactor = this.factor.getValueFloat();
                    }
                }
            } else {
                this.lastFactor = this.factor.getValueFloat();
            }
            int i = 1;
            while (true) {
                float f = i;
                float f2 = this.mode.getValue() == Mode.Factor || this.mode.getValue() == Mode.Slow || this.mode.getValue() == Mode.Increment ? this.lastFactor : 1.0f;
                if (!(f <= f2)) break;
                double conceal = PacketFly.mc.player.getY() < this.concealY.getValue() && MovementUtil.isMoving() ? this.conceal.getValue() : 1.0;
                MovementUtil.setMotionX(dirSpeed[0] * (double)i * conceal * this.xzSpeed.getValue());
                MovementUtil.setMotionY(ySpeed * (double)i * this.ySpeed.getValue());
                MovementUtil.setMotionZ(dirSpeed[1] * (double)i * conceal * this.xzSpeed.getValue());
                this.sendPackets(MovementUtil.getMotionX(), MovementUtil.getMotionY(), MovementUtil.getMotionZ(), this.mode.getValue() != Mode.Setback);
                ++i;
            }
            ++this.zoomTimer;
            if ((double)this.zoomTimer > this.zoomer.getValue()) {
                this.zoomies = !this.zoomies;
                this.zoomTimer = 0;
            }
        }
    }

    @EventListener
    public void invoke(PacketEvent.Receive event) {
        if (PacketFly.nullCheck()) {
            return;
        }
        if (this.mode.getValue() == Mode.Compatibility) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerPositionLookS2CPacket) {
            TimeVec vec;
            PlayerPositionLookS2CPacket packet2 = (PlayerPositionLookS2CPacket)packet;
            if (PacketFly.mc.player.isAlive() && this.mode.getValue() != Mode.Setback && this.mode.getValue() != Mode.Slow && !(PacketFly.mc.currentScreen instanceof DownloadingTerrainScreen) && (vec = this.posLooks.remove(packet2.getTeleportId())) != null && vec.x == packet2.getX() && vec.y == packet2.getY() && vec.z == packet2.getZ()) {
                event.setCancelled(true);
                return;
            }
            this.teleportID.set(packet2.getTeleportId());
        }
    }

    @EventListener
    public void invoke(MoveEvent event) {
        if (this.phase.getValue() == Phase.Semi || this.isPlayerCollisionBoundingBoxEmpty()) {
            PacketFly.mc.player.noClip = true;
        }
        if (this.mode.getValue() != Mode.Compatibility && (this.mode.getValue() == Mode.Setback || this.teleportID.get() != 0)) {
            if (this.zeroSpeed.getValue()) {
                event.setX(0.0);
                event.setY(0.0);
                event.setZ(0.0);
            } else {
                event.setX(MovementUtil.getMotionX());
                event.setY(MovementUtil.getMotionY());
                event.setZ(MovementUtil.getMotionZ());
            }
            if (this.zeroY.getValue()) {
                event.setY(0.0);
            }
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Send event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet2 = (PlayerMoveC2SPacket)packet;
            if (this.mode.getValue() != Mode.Compatibility && !this.playerPackets.remove(event.getPacket())) {
                if (packet2 instanceof PlayerMoveC2SPacket.LookAndOnGround && !this.noRotation.getValue()) {
                    return;
                }
                if (!this.noMovePacket.getValue()) {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onEnable() {
        this.clearValues();
        if (PacketFly.mc.player == null) {
            this.disable();
        }
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().toString();
    }

    public void clearValues() {
        this.lastFactor = 1.0f;
        this.packetCounter = 0;
        this.teleportID.set(0);
        this.playerPackets.clear();
        this.posLooks.clear();
        this.vecDelServer = null;
    }

    public boolean isPlayerCollisionBoundingBoxEmpty() {
        double o = this.bbOffset.getValue() ? -0.0625 : 0.0;
        return BlockUtil.canCollide((Entity)PacketFly.mc.player, PacketFly.mc.player.getBoundingBox().expand(o, o, o));
    }

    public boolean checkPackets(int amount) {
        if (++this.packetCounter >= amount) {
            this.packetCounter = 0;
            return true;
        }
        return false;
    }

    public void sendPackets(double x, double y, double z, boolean confirm) {
        int i;
        Vec3d vec;
        Vec3d offset = new Vec3d(x, y, z);
        this.vecDelServer = vec = PacketFly.mc.player.getPos().add(offset);
        Vec3d oOB = this.type.getValue().createOutOfBounds(vec, this.invalidY.getValueInt());
        this.sendCPacket((Packet<?>)new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, PacketFly.mc.player.isOnGround()));
        if (!mc.isInSingleplayer()) {
            i = 0;
            while ((double)i < this.invalids.getValue()) {
                this.sendCPacket((Packet<?>)new PlayerMoveC2SPacket.PositionAndOnGround(oOB.x, oOB.y, oOB.z, PacketFly.mc.player.isOnGround()));
                oOB = this.type.getValue().createOutOfBounds(oOB, this.invalidY.getValueInt());
                ++i;
            }
        }
        if (confirm && (this.zeroTeleport.getValue() || this.teleportID.get() != 0)) {
            i = 0;
            while ((double)i < this.sendTeleport.getValue()) {
                this.sendConfirmTeleport(vec);
                ++i;
            }
        }
        if (this.elytra.getValue()) {
            mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)PacketFly.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    public void sendConfirmTeleport(Vec3d vec) {
        int id = this.teleportID.incrementAndGet();
        mc.getNetworkHandler().sendPacket((Packet)new TeleportConfirmC2SPacket(id));
        this.posLooks.put(id, new TimeVec(vec));
    }

    public void sendCPacket(Packet<?> packet) {
        this.playerPackets.add(packet);
        mc.getNetworkHandler().sendPacket(packet);
    }

    public static enum Mode {
        Setback,
        Fast,
        Factor,
        Slow,
        Increment,
        Compatibility;

    }

    public static enum Phase {
        Off,
        Semi,
        Full;

    }

    public static enum Type {
        Down{

            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add(0.0, (double)(-invalid), 0.0);
            }
        }
        ,
        Up{

            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add(0.0, (double)invalid, 0.0);
            }
        }
        ,
        Preserve{
            final Random random = new Random();

            private int randomInt() {
                int result = this.random.nextInt(29000000);
                if (this.random.nextBoolean()) {
                    return result;
                }
                return -result;
            }

            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add((double)this.randomInt(), 0.0, (double)this.randomInt());
            }
        }
        ,
        Switch{
            final Random random = new Random();

            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                boolean down = this.random.nextBoolean();
                return down ? vec3d.add(0.0, (double)(-invalid), 0.0) : vec3d.add(0.0, (double)invalid, 0.0);
            }
        }
        ,
        X{

            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add((double)invalid, 0.0, 0.0);
            }
        }
        ,
        Z{

            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add(0.0, 0.0, (double)invalid);
            }
        }
        ,
        XZ{

            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add((double)invalid, 0.0, (double)invalid);
            }
        };


        public abstract Vec3d createOutOfBounds(Vec3d var1, int var2);
    }

    public static class TimeVec
    extends Vec3d {
        final long time;

        public TimeVec(Vec3d vec3d) {
            this(vec3d.x, vec3d.y, vec3d.z, System.currentTimeMillis());
        }

        public TimeVec(double xIn, double yIn, double zIn, long time) {
            super(xIn, yIn, zIn);
            this.time = time;
        }

        public long getTime() {
            return this.time;
        }
    }
}

