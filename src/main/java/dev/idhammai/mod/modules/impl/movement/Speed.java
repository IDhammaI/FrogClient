/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.decoration.ArmorStandEntity
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.vehicle.BoatEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
 *  net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 */
package dev.idhammai.mod.modules.impl.movement;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.MoveEvent;
import dev.idhammai.api.events.impl.MovedEvent;
import dev.idhammai.api.events.impl.PacketEvent;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.path.BaritoneUtil;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.MovementUtil;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class Speed
extends Module {
    public static Speed INSTANCE;
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Strafe));
    public final SliderSetting collideSpeed = this.add(new SliderSetting("CollideSpeed", 0.08, 0.0, 0.08, 0.01, () -> this.mode.is(Mode.Grim)));
    private final BooleanSetting strict = this.add(new BooleanSetting("Strict", true, () -> this.mode.is(Mode.Grim)));
    private final BooleanSetting boat = this.add(new BooleanSetting("BoatLongJump", true, () -> this.mode.is(Mode.Grim)));
    public final SliderSetting boatExpand = this.add(new SliderSetting("BoatExpand", 0.2, 0.0, 1.0, 0.01, () -> this.mode.is(Mode.Grim)));
    public final SliderSetting boatSpeed = this.add(new SliderSetting("BoatSpeed", 0.2, -2.0, 2.0, 0.01, () -> this.mode.is(Mode.Grim)));
    public final SliderSetting boatJump = this.add(new SliderSetting("BoatJump", 0.2, 0.0, 2.0, 0.01, () -> this.mode.is(Mode.Grim)));
    private final BooleanSetting inWater = this.add(new BooleanSetting("InWater", false, () -> !this.mode.is(Mode.Grim)));
    private final BooleanSetting inBlock = this.add(new BooleanSetting("InBlock", false, () -> !this.mode.is(Mode.Grim)));
    private final BooleanSetting airStop = this.add(new BooleanSetting("AirStop", false, () -> !this.mode.is(Mode.Grim)));
    private final SliderSetting lagTime = this.add(new SliderSetting("LagTime", 500.0, 0.0, 1000.0, 1.0, () -> !this.mode.is(Mode.Grim)));
    private final BooleanSetting jump = this.add(new BooleanSetting("Jump", true, () -> this.mode.is(Mode.Strafe)));
    private final SliderSetting strafeSpeed = this.add(new SliderSetting("Speed", 0.2873, 0.0, 1.0, 1.0E-4, () -> this.mode.is(Mode.Strafe)));
    private final BooleanSetting explosions = this.add(new BooleanSetting("ExplosionsBoost", false, () -> this.mode.is(Mode.Strafe)));
    private final BooleanSetting velocity = this.add(new BooleanSetting("VelocityBoost", true, () -> this.mode.is(Mode.Strafe)));
    private final SliderSetting multiplier = this.add(new SliderSetting("H-Factor", 1.0, 0.0, 5.0, 0.01, () -> this.mode.is(Mode.Strafe)));
    private final SliderSetting vertical = this.add(new SliderSetting("V-Factor", 1.0, 0.0, 5.0, 0.01, () -> this.mode.is(Mode.Strafe)));
    private final SliderSetting coolDown = this.add(new SliderSetting("CoolDown", 1000.0, 0.0, 5000.0, 1.0, () -> this.mode.is(Mode.Strafe)));
    private final BooleanSetting slow = this.add(new BooleanSetting("Slowness", false, () -> this.mode.is(Mode.Strafe)));
    private final Timer expTimer = new Timer();
    private final Timer lagTimer = new Timer();
    private boolean stop;
    private double speed;
    private double getDistance;
    private int strictTicks;
    private int strafe = 4;
    private int stage;
    private double lastExp;
    private boolean boost;

    public Speed() {
        super("Speed", Module.Category.Movement);
        this.setChinese("\u52a0\u901f");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @Override
    public void onEnable() {
        if (Speed.mc.player != null) {
            this.speed = MovementUtil.getSpeed(false);
            this.getDistance = MovementUtil.getDistance2D();
        }
        this.stage = 4;
    }

    @EventListener(priority=100)
    public void invoke(PacketEvent.Receive event) {
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (this.mode.is(Mode.Strafe)) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof EntityVelocityUpdateS2CPacket) {
                EntityVelocityUpdateS2CPacket packet2 = (EntityVelocityUpdateS2CPacket)packet;
                if (Speed.mc.player != null && packet2.getEntityId() == Speed.mc.player.getId() && this.velocity.getValue()) {
                    double speed = Math.sqrt(packet2.getVelocityX() * packet2.getVelocityX() + packet2.getVelocityZ() * packet2.getVelocityZ());
                    double d = this.lastExp = this.expTimer.passed(this.coolDown.getValueInt()) ? speed : speed - this.lastExp;
                    if (this.lastExp > 0.0) {
                        this.expTimer.reset();
                        this.speed += this.lastExp * this.multiplier.getValue();
                        this.getDistance += this.lastExp * this.multiplier.getValue();
                        if (MovementUtil.getMotionY() > 0.0 && this.vertical.getValue() != 0.0) {
                            MovementUtil.setMotionY(MovementUtil.getMotionY() * this.vertical.getValue());
                        }
                    }
                }
            } else {
                Packet<?> speed = event.getPacket();
                if (speed instanceof ExplosionS2CPacket) {
                    ExplosionS2CPacket packet3 = (ExplosionS2CPacket)speed;
                    if (this.explosions.getValue()) {
                        Vec3d vec3d = new Vec3d(packet3.getX(), packet3.getY(), packet3.getZ());
                        if (Speed.mc.player.getPos().distanceTo(vec3d) < 15.0) {
                            double speed2 = Math.sqrt(packet3.getPlayerVelocityX() * packet3.getPlayerVelocityX() + packet3.getPlayerVelocityZ() * packet3.getPlayerVelocityZ());
                            double d = this.lastExp = this.expTimer.passed(this.coolDown.getValueInt()) ? speed2 : speed2 - this.lastExp;
                            if (this.lastExp > 0.0) {
                                this.expTimer.reset();
                                this.speed += this.lastExp * this.multiplier.getValue();
                                this.getDistance += this.lastExp * this.multiplier.getValue();
                                if (MovementUtil.getMotionY() > 0.0) {
                                    MovementUtil.setMotionY(MovementUtil.getMotionY() * this.vertical.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.lagTimer.reset();
            this.resetStrafe();
        }
    }

    @EventListener
    public void onMove(MovedEvent event) {
        if (Speed.nullCheck()) {
            return;
        }
        double dx = Speed.mc.player.getX() - Speed.mc.player.prevX;
        double dz = Speed.mc.player.getZ() - Speed.mc.player.prevZ;
        this.getDistance = Math.sqrt(dx * dx + dz * dz);
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.mode.is(Mode.Grim)) {
            if (!MovementUtil.isMoving()) {
                return;
            }
            int collisions = 0;
            Box box = this.strict.getValue() ? Speed.mc.player.getBoundingBox() : Speed.mc.player.getBoundingBox().expand(1.0);
            for (Entity entity : Frog.THREAD.getEntities()) {
                Box entityBox = entity.getBoundingBox();
                if (this.boat.getValue() && Speed.mc.player.isOnGround() && entity instanceof BoatEntity && box.intersects(entityBox.expand(this.boatExpand.getValue()))) {
                    double yaw = Math.toRadians(Sprint.getSprintYaw(Speed.mc.player.getYaw()));
                    double boost = this.boatSpeed.getValue();
                    Speed.mc.player.setVelocity(-Math.sin(yaw) * boost, this.boatJump.getValue(), Math.cos(yaw) * boost);
                    return;
                }
                if (!box.intersects(entityBox) || !this.canCauseSpeed(entity)) continue;
                ++collisions;
            }
            double yaw = Math.toRadians(Sprint.getSprintYaw(Speed.mc.player.getYaw()));
            double boost = this.collideSpeed.getValue() * (double)collisions;
            Speed.mc.player.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
        }
    }

    private boolean canCauseSpeed(Entity entity) {
        return entity != Speed.mc.player && entity instanceof LivingEntity && !(entity instanceof ArmorStandEntity);
    }

    @EventListener
    public void invoke(MoveEvent event) {
        double amplifier;
        if (!MovementUtil.isMoving() && this.airStop.getValue() && !this.mode.is(Mode.Grim)) {
            MovementUtil.setMotionX(0.0);
            MovementUtil.setMotionZ(0.0);
        }
        if (!this.inWater.getValue() && (Speed.mc.player.isSubmergedInWater() || Speed.mc.player.isTouchingWater() || Speed.mc.player.isInLava()) || Speed.mc.player.isRiding() || Speed.mc.player.isHoldingOntoLadder() || !this.inBlock.getValue() && EntityUtil.isInsideBlock() || Speed.mc.player.getAbilities().flying || Speed.mc.player.isFallFlying() || !MovementUtil.isMoving()) {
            this.resetStrafe();
            this.stop = true;
            return;
        }
        if (this.mode.is(Mode.Strafe)) {
            if (this.stop) {
                this.stop = false;
                return;
            }
            if (!this.lagTimer.passed(this.lagTime.getValueInt())) {
                return;
            }
            if (this.stage == 1) {
                this.speed = 1.35 * MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue()) - 0.01;
            } else if (this.stage == 2 && Speed.mc.player.isOnGround() && (Speed.mc.options.jumpKey.isPressed() || this.jump.getValue())) {
                double yMotion = 0.3999 + MovementUtil.getJumpSpeed();
                MovementUtil.setMotionY(yMotion);
                event.setY(yMotion);
                this.speed *= this.boost ? 1.6835 : 1.395;
            } else if (this.stage == 3) {
                this.speed = this.getDistance - 0.66 * (this.getDistance - MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue()));
                this.boost = !this.boost;
            } else {
                if ((BlockUtil.canCollide(null, Speed.mc.player.getBoundingBox().offset(0.0, MovementUtil.getMotionY(), 0.0)) || Speed.mc.player.collidedSoftly) && this.stage > 0) {
                    this.stage = 1;
                }
                this.speed = this.getDistance - this.getDistance / 159.0;
            }
            this.speed = Math.min(this.speed, 10.0);
            this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue()));
            double n = Speed.mc.player.input.movementForward;
            double n2 = Speed.mc.player.input.movementSideways;
            double n3 = Speed.mc.player.getYaw();
            if (n == 0.0 && n2 == 0.0) {
                event.setX(0.0);
                event.setZ(0.0);
            } else if (n != 0.0 && n2 != 0.0) {
                n *= Math.sin(0.7853981633974483);
                n2 *= Math.cos(0.7853981633974483);
            }
            event.setX((n * this.speed * -Math.sin(Math.toRadians(n3)) + n2 * this.speed * Math.cos(Math.toRadians(n3))) * 0.99);
            event.setZ((n * this.speed * Math.cos(Math.toRadians(n3)) - n2 * this.speed * -Math.sin(Math.toRadians(n3))) * 0.99);
            ++this.stage;
            return;
        }
        double speedEffect = 1.0;
        double slowEffect = 1.0;
        if (Speed.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            amplifier = Speed.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            speedEffect = 1.0 + 0.2 * (amplifier + 1.0);
        }
        if (Speed.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            amplifier = Speed.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            slowEffect = 1.0 + 0.2 * (amplifier + 1.0);
        }
        double base = (double)0.2873f * speedEffect / slowEffect;
        float jumpEffect = 0.0f;
        if (Speed.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            jumpEffect += (float)(Speed.mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;
        }
        if (this.mode.getValue() == Mode.StrafeStrict) {
            if (!this.lagTimer.passed(this.lagTime.getValueInt())) {
                return;
            }
            if (this.strafe == 1) {
                this.speed = (double)1.35f * base - (double)0.01f;
            } else if (this.strafe == 2) {
                if (Speed.mc.player.input.jumping || !Speed.mc.player.isOnGround()) {
                    return;
                }
                float jump = 0.39999995f + jumpEffect;
                event.setY(jump);
                MovementUtil.setMotionY(jump);
                this.speed *= 2.149;
            } else if (this.strafe == 3) {
                double moveSpeed = 0.66 * (this.getDistance - base);
                this.speed = this.getDistance - moveSpeed;
            } else {
                if ((!Speed.mc.world.isSpaceEmpty((Entity)Speed.mc.player, Speed.mc.player.getBoundingBox().offset(0.0, Speed.mc.player.getVelocity().getY(), 0.0)) || Speed.mc.player.verticalCollision) && this.strafe > 0) {
                    this.strafe = 1;
                }
                this.speed = this.getDistance - this.getDistance / 159.0;
            }
            ++this.strictTicks;
            this.speed = Math.max(this.speed, base);
            double baseMax = 0.465 * speedEffect / slowEffect;
            double baseMin = 0.44 * speedEffect / slowEffect;
            this.speed = Math.min(this.speed, this.strictTicks > 25 ? baseMax : baseMin);
            if (this.strictTicks > 50) {
                this.strictTicks = 0;
            }
            Vec2f motion = this.handleStrafeMotion((float)this.speed);
            event.setX(motion.x);
            event.setZ(motion.y);
            ++this.strafe;
        }
    }

    public Vec2f handleStrafeMotion(float speed) {
        float forward = Speed.mc.player.input.movementForward;
        float strafe = Speed.mc.player.input.movementSideways;
        float yaw = Speed.mc.player.prevYaw + (Speed.mc.player.getYaw() - Speed.mc.player.prevYaw) * mc.getRenderTickCounter().getTickDelta(true);
        if (forward == 0.0f && strafe == 0.0f) {
            return Vec2f.ZERO;
        }
        if (forward != 0.0f) {
            if (strafe >= 1.0f) {
                yaw += forward > 0.0f ? -45.0f : 45.0f;
                strafe = 0.0f;
            } else if (strafe <= -1.0f) {
                yaw += forward > 0.0f ? 45.0f : -45.0f;
                strafe = 0.0f;
            }
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        float rx = (float)Math.cos(Math.toRadians(yaw));
        float rz = (float)(-Math.sin(Math.toRadians(yaw)));
        return new Vec2f(forward * speed * rz + strafe * speed * rx, forward * speed * rx - strafe * speed * rz);
    }

    public void resetStrafe() {
        this.strafe = 4;
        this.strictTicks = 0;
        this.speed = 0.0;
        this.getDistance = 0.0;
    }

    public static enum Mode {
        Strafe,
        StrafeStrict,
        Grim;

    }
}

