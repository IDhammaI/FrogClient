/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.network.OtherClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.Entity$RemovalReason
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.listener.ClientPlayPacketListener
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractType
 *  net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
 *  net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Vec3d
 */
package dev.idhammai.mod.modules.impl.misc;

import com.mojang.authlib.GameProfile;
import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.PacketEvent;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.math.DamageUtils;
import dev.idhammai.api.utils.world.BlockPosX;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.asm.accessors.ILivingEntity;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.combat.AutoAnchor;
import dev.idhammai.mod.modules.impl.combat.AutoCrystal;
import dev.idhammai.mod.modules.impl.combat.Criticals;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.StringSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class FakePlayer
extends Module {
    public static FakePlayer INSTANCE;
    public static FakePlayerEntity fakePlayer;
    final StringSetting name = this.add(new StringSetting("Name", "FakePlayer"));
    private final BooleanSetting damage = this.add(new BooleanSetting("Damage", true));
    private final BooleanSetting autoTotem = this.add(new BooleanSetting("AutoTotem", true));
    public final BooleanSetting record = this.add(new BooleanSetting("Record", false));
    public final BooleanSetting play = this.add(new BooleanSetting("Play", false));
    final List<PlayerState> positions = new ArrayList<PlayerState>();
    int movementTick;
    boolean lastRecordValue = false;

    public FakePlayer() {
        super("FakePlayer", Module.Category.Misc);
        this.setChinese("\u5047\u4eba");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.name.getValue();
    }

    @Override
    public void onEnable() {
        if (FakePlayer.nullCheck()) {
            this.disable();
            return;
        }
        fakePlayer = new FakePlayerEntity((PlayerEntity)FakePlayer.mc.player, this.name.getValue());
        FakePlayer.mc.world.addEntity((Entity)fakePlayer);
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (fakePlayer == null || FakePlayer.fakePlayer.clientWorld != FakePlayer.mc.world) {
            this.disable();
            return;
        }
        if (this.autoTotem.getValue()) {
            if (fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING));
            }
            if (fakePlayer.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.setStackInHand(Hand.MAIN_HAND, new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING));
            }
        }
        if (this.record.getValue() != this.lastRecordValue && this.record.getValue()) {
            this.positions.clear();
        }
        this.lastRecordValue = this.record.getValue();
        if (this.record.getValue()) {
            this.positions.add(new PlayerState(FakePlayer.mc.player.getX(), FakePlayer.mc.player.getY(), FakePlayer.mc.player.getZ(), FakePlayer.mc.player.getYaw(), FakePlayer.mc.player.getPitch()));
        }
        if (this.play.getValue() && !this.positions.isEmpty()) {
            ++this.movementTick;
            if (this.movementTick >= this.positions.size()) {
                this.movementTick = 0;
            }
            PlayerState p = this.positions.get(this.movementTick);
            fakePlayer.setYaw(p.yaw);
            fakePlayer.setPitch(p.pitch);
            fakePlayer.setHeadYaw(p.yaw);
            fakePlayer.updateTrackedPosition(p.x, p.y, p.z);
            fakePlayer.updateTrackedPositionAndAngles(p.x, p.y, p.z, p.yaw, p.pitch, 3);
        }
    }

    @Override
    public void onDisable() {
        if (fakePlayer == null) {
            return;
        }
        fakePlayer.kill();
        fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
        fakePlayer.onRemoved();
        fakePlayer = null;
    }

    @EventListener
    public void onAttack(PacketEvent.Send event) {
        PlayerInteractEntityC2SPacket packet;
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof PlayerInteractEntityC2SPacket && Criticals.getInteractType(packet = (PlayerInteractEntityC2SPacket)packet2) == PlayerInteractEntityC2SPacket.InteractType.ATTACK && Criticals.getEntity(packet) == fakePlayer) {
            FakePlayer.mc.world.playSound((PlayerEntity)FakePlayer.mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            float damage = DamageUtils.getAttackDamage((LivingEntity)FakePlayer.mc.player, (LivingEntity)fakePlayer);
            if (!(!(FakePlayer.mc.player.fallDistance > 0.0f) && (!Criticals.INSTANCE.isOn() || Criticals.INSTANCE.mode.is(Criticals.Mode.Ground) || !FakePlayer.mc.player.isOnGround() && Criticals.INSTANCE.onlyGround.getValue()) || FakePlayer.mc.player.isOnGround() && (!Criticals.INSTANCE.isOn() || Criticals.INSTANCE.mode.is(Criticals.Mode.Ground)) || FakePlayer.mc.player.isClimbing() || FakePlayer.mc.player.isTouchingWater() || FakePlayer.mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || FakePlayer.mc.player.hasVehicle())) {
                FakePlayer.mc.world.playSound((PlayerEntity)FakePlayer.mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                FakePlayer.mc.player.addCritParticles((Entity)fakePlayer);
            }
            if (FakePlayer.fakePlayer.hurtTime <= 0) {
                fakePlayer.onDamaged(FakePlayer.mc.world.getDamageSources().generic());
                if (fakePlayer.getAbsorptionAmount() >= damage) {
                    fakePlayer.setAbsorptionAmount(fakePlayer.getAbsorptionAmount() - damage);
                } else {
                    float damage2 = damage - fakePlayer.getAbsorptionAmount();
                    fakePlayer.setAbsorptionAmount(0.0f);
                    fakePlayer.setHealth(fakePlayer.getHealth() - damage2);
                }
                if (fakePlayer.isDead()) {
                    Frog.POP.onTotemPop((PlayerEntity)fakePlayer);
                    if (fakePlayer.tryUseTotem(FakePlayer.mc.world.getDamageSources().generic())) {
                        fakePlayer.setHealth(10.0f);
                        new EntityStatusS2CPacket((Entity)fakePlayer, (byte)35).apply((ClientPlayPacketListener)mc.getNetworkHandler());
                    }
                }
            }
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        Packet<?> packet;
        if (this.damage.getValue() && fakePlayer != null && FakePlayer.fakePlayer.hurtTime <= 0 && (packet = event.getPacket()) instanceof ExplosionS2CPacket) {
            ExplosionS2CPacket explosion = (ExplosionS2CPacket)packet;
            Vec3d vec3d = new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ());
            if (Math.sqrt(vec3d.squaredDistanceTo(fakePlayer.getPos())) > 10.0) {
                return;
            }
            float damage = BlockUtil.getBlock(new BlockPosX(explosion.getX(), explosion.getY(), explosion.getZ())) == Blocks.RESPAWN_ANCHOR ? (float)AutoAnchor.INSTANCE.getAnchorDamage(new BlockPosX(explosion.getX(), explosion.getY(), explosion.getZ()), (PlayerEntity)fakePlayer, (PlayerEntity)fakePlayer) : AutoCrystal.INSTANCE.calculateDamage(new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ()), (PlayerEntity)fakePlayer, (PlayerEntity)fakePlayer);
            fakePlayer.onDamaged(FakePlayer.mc.world.getDamageSources().generic());
            if (fakePlayer.getAbsorptionAmount() >= damage) {
                fakePlayer.setAbsorptionAmount(fakePlayer.getAbsorptionAmount() - damage);
            } else {
                float damage2 = damage - fakePlayer.getAbsorptionAmount();
                fakePlayer.setAbsorptionAmount(0.0f);
                fakePlayer.setHealth(fakePlayer.getHealth() - damage2);
            }
            if (fakePlayer.isDead()) {
                Frog.POP.onTotemPop((PlayerEntity)fakePlayer);
                if (fakePlayer.tryUseTotem(FakePlayer.mc.world.getDamageSources().generic())) {
                    fakePlayer.setHealth(10.0f);
                    new EntityStatusS2CPacket((Entity)fakePlayer, (byte)35).apply((ClientPlayPacketListener)mc.getNetworkHandler());
                }
            }
        }
    }

    public static class FakePlayerEntity
    extends OtherClientPlayerEntity {
        private final boolean onGround;

        public FakePlayerEntity(PlayerEntity player, String name) {
            super(Wrapper.mc.world, new GameProfile(UUID.fromString("66666666-6666-6666-6666-666666666666"), name));
            this.copyPositionAndRotation((Entity)player);
            this.prevX = player.prevX;
            this.prevZ = player.prevZ;
            this.prevY = player.prevY;
            this.bodyYaw = player.bodyYaw;
            this.headYaw = player.headYaw;
            this.handSwingProgress = player.handSwingProgress;
            this.handSwingTicks = player.handSwingTicks;
            this.limbAnimator.setSpeed(player.limbAnimator.getSpeed());
            this.limbAnimator.pos = player.limbAnimator.getPos();
            ((ILivingEntity)((Object)this)).setLeaningPitch(((ILivingEntity)player).getLeaningPitch());
            ((ILivingEntity)((Object)this)).setLastLeaningPitch(((ILivingEntity)player).getLeaningPitch());
            this.touchingWater = player.isTouchingWater();
            this.setSneaking(player.isSneaking());
            this.setPose(player.getPose());
            this.setFlag(7, player.isFallFlying());
            this.onGround = player.isOnGround();
            this.setOnGround(this.onGround);
            this.getInventory().clone(player.getInventory());
            this.setAbsorptionAmountUnclamped(player.getAbsorptionAmount());
            this.setHealth(player.getHealth());
            this.setBoundingBox(player.getBoundingBox());
        }

        public boolean isOnGround() {
            return this.onGround;
        }

        public boolean isSpectator() {
            return false;
        }

        public boolean isCreative() {
            return false;
        }
    }

    private record PlayerState(double x, double y, double z, float yaw, float pitch) {
    }
}

