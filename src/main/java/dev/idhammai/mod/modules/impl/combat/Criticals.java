/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractType
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.util.math.Box
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.PacketEvent;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.MovementUtil;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.asm.accessors.IPlayerMoveC2SPacket;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.exploit.Blink;
import dev.idhammai.mod.modules.impl.exploit.BowBomb;
import dev.idhammai.mod.modules.impl.exploit.Phase;
import dev.idhammai.mod.modules.impl.player.AutoPearl;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;

public class Criticals
extends Module {
    public static Criticals INSTANCE;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.OldNCP));
    public final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", true, () -> !this.mode.is(Mode.Ground)));
    private final BooleanSetting setOnGround = this.add(new BooleanSetting("SetNoGround", false, () -> this.mode.is(Mode.Ground)));
    private final BooleanSetting blockCheck = this.add(new BooleanSetting("BlockCheck", true, () -> this.mode.is(Mode.Ground)));
    private final BooleanSetting autoJump = this.add(new BooleanSetting("AutoJump", true, () -> this.mode.is(Mode.Ground)).setParent());
    private final BooleanSetting mini = this.add(new BooleanSetting("Mini", true, () -> this.mode.is(Mode.Ground) && this.autoJump.isOpen()));
    private final SliderSetting y = this.add(new SliderSetting("MotionY", 0.05, 0.0, 1.0, 1.0E-10, () -> this.mode.is(Mode.Ground) && this.autoJump.isOpen()));
    private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true, () -> this.mode.is(Mode.Ground)));
    private final BooleanSetting crawlingDisable = this.add(new BooleanSetting("CrawlingDisable", true, () -> this.mode.is(Mode.Ground)));
    private final BooleanSetting flight = this.add(new BooleanSetting("Flight", false, () -> this.mode.is(Mode.Ground)));
    boolean requireJump = false;

    public Criticals() {
        super("Criticals", Module.Category.Combat);
        this.setChinese("\u5200\u5200\u66b4\u51fb");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        Entity entity;
        PlayerInteractEntityC2SPacket packet;
        if (event.isCancelled()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.mode.is(Mode.Ground)) {
            if (BowBomb.send) {
                return;
            }
            if (AutoPearl.throwing || Phase.INSTANCE.isOn()) {
                return;
            }
            if (!this.setOnGround.getValue()) {
                return;
            }
            if (event.getPacket() instanceof PlayerMoveC2SPacket) {
                ((IPlayerMoveC2SPacket)event.getPacket()).setOnGround(false);
            }
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (!(!(packet2 instanceof PlayerInteractEntityC2SPacket) || Criticals.getInteractType(packet = (PlayerInteractEntityC2SPacket)packet2) != PlayerInteractEntityC2SPacket.InteractType.ATTACK || (entity = Criticals.getEntity(packet)) instanceof EndCrystalEntity || this.onlyGround.getValue() && !Criticals.mc.player.isOnGround() && !Criticals.mc.player.getAbilities().flying || Criticals.mc.player.isInLava() || Criticals.mc.player.isTouchingWater() || entity == null)) {
            this.doCrit(entity);
        }
    }

    @Override
    public void onLogout() {
        if (this.mode.is(Mode.Ground) && this.autoDisable.getValue()) {
            this.disable();
        }
    }

    @Override
    public void onEnable() {
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        this.requireJump = true;
        if (this.mode.is(Mode.Ground)) {
            if (Criticals.nullCheck()) {
                if (this.autoDisable.getValue()) {
                    this.disable();
                }
            } else if (MovementUtil.isMoving() && this.autoDisable.getValue()) {
                this.disable();
            } else if (this.crawlingDisable.getValue() && Criticals.mc.player.isCrawling()) {
                this.disable();
            } else if (Criticals.mc.player.isOnGround() && this.autoJump.getValue() && (!this.blockCheck.getValue() || BlockUtil.canCollide((Entity)Criticals.mc.player, new Box(EntityUtil.getPlayerPos(true).up(2))))) {
                this.jump();
            }
        }
    }

    public void jump() {
        if (this.mini.getValue()) {
            MovementUtil.setMotionY(this.y.getValue());
        } else {
            Criticals.mc.player.jump();
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.mode.is(Mode.Ground)) {
            if (this.crawlingDisable.getValue() && Criticals.mc.player.isCrawling()) {
                this.disable();
            } else if (MovementUtil.isMoving() && this.autoDisable.getValue()) {
                this.disable();
            } else if (this.flight.getValue() && Criticals.mc.player.fallDistance > 0.0f) {
                MovementUtil.setMotionY(0.0);
                MovementUtil.setMotionX(0.0);
                MovementUtil.setMotionZ(0.0);
                this.requireJump = false;
            } else if (this.blockCheck.getValue() && !BlockUtil.canCollide((Entity)Criticals.mc.player, new Box(EntityUtil.getPlayerPos(true).up(2)))) {
                this.requireJump = true;
            } else if (Criticals.mc.player.isOnGround() && this.autoJump.getValue() && (this.flight.getValue() || this.requireJump)) {
                this.jump();
                this.requireJump = false;
            }
        }
    }

    public void doCrit(Entity entity) {
        switch (this.mode.getValue().ordinal()) {
            case 7: {
                if (MovementUtil.isMoving() || !MovementUtil.isStatic()) {
                    return;
                }
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), true));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.0625, Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.045, Criticals.mc.player.getZ(), false));
                break;
            }
            case 1: {
                Criticals.mc.player.addCritParticles(entity);
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.062600301692775, Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.07260029960661, Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), false));
                break;
            }
            case 2: {
                Criticals.mc.player.addCritParticles(entity);
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.0625, Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), false));
                break;
            }
            case 3: {
                Criticals.mc.player.addCritParticles(entity);
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 1.058293536E-5, Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 9.16580235E-6, Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 1.0371854E-7, Criticals.mc.player.getZ(), false));
                break;
            }
            case 0: {
                Criticals.mc.player.addCritParticles(entity);
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 2.71875E-7, Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), false));
                break;
            }
            case 4: {
                Criticals.mc.player.addCritParticles(entity);
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.0045, Criticals.mc.player.getZ(), true));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 1.52121E-4, Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.3, Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.025, Criticals.mc.player.getZ(), false));
                break;
            }
            case 5: {
                Criticals.mc.player.addCritParticles(entity);
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 5.0E-4, Criticals.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 1.0E-4, Criticals.mc.player.getZ(), false));
            }
        }
    }

    public static Entity getEntity(PlayerInteractEntityC2SPacket packet) {
        return Criticals.mc.world == null ? null : Criticals.mc.world.getEntityById(((dev.idhammai.asm.accessors.IPlayerInteractEntityC2SPacket)packet).getEntityId());
    }

    public static PlayerInteractEntityC2SPacket.InteractType getInteractType(PlayerInteractEntityC2SPacket packet) {
        final PlayerInteractEntityC2SPacket.InteractType[] result = new PlayerInteractEntityC2SPacket.InteractType[1];
        packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
            @Override
            public void interact(net.minecraft.util.Hand hand) {
                result[0] = PlayerInteractEntityC2SPacket.InteractType.INTERACT;
            }

            @Override
            public void interactAt(net.minecraft.util.Hand hand, net.minecraft.util.math.Vec3d pos) {
                result[0] = PlayerInteractEntityC2SPacket.InteractType.INTERACT_AT;
            }

            @Override
            public void attack() {
                result[0] = PlayerInteractEntityC2SPacket.InteractType.ATTACK;
            }
        });
        return result[0];
    }

    public static enum Mode {
        UpdatedNCP,
        Strict,
        NCP,
        OldNCP,
        Hypixel2K22,
        Packet,
        Ground,
        BBTT;

    }
}

