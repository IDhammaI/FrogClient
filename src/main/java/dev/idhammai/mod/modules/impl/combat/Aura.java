/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.mob.MobEntity
 *  net.minecraft.entity.mob.SlimeEntity
 *  net.minecraft.entity.passive.AnimalEntity
 *  net.minecraft.entity.passive.VillagerEntity
 *  net.minecraft.entity.passive.WanderingTraderEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Vec3d
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.ClientTickEvent;
import dev.idhammai.api.events.impl.PacketEvent;
import dev.idhammai.api.events.impl.RotationEvent;
import dev.idhammai.api.utils.combat.CombatUtil;
import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.math.MathUtil;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.JelloUtil;
import dev.idhammai.api.utils.render.Render3DUtil;
import dev.idhammai.asm.accessors.IEntity;
import dev.idhammai.asm.accessors.ILivingEntity;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.exploit.Blink;
import dev.idhammai.mod.modules.impl.movement.ElytraFly;
import dev.idhammai.mod.modules.impl.movement.Velocity;
import dev.idhammai.mod.modules.settings.enums.SwingSide;
import dev.idhammai.mod.modules.settings.enums.Timing;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class Aura
extends Module {
    public static Aura INSTANCE;
    public static Entity target;
    public final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
    public final SliderSetting range = this.add(new SliderSetting("Range", 6.0, (double)0.1f, 7.0, () -> this.page.getValue() == Page.General));
    private final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 8.0, (double)0.1f, 14.0, () -> this.page.getValue() == Page.General));
    private final EnumSetting<Cooldown> cooldownMode = this.add(new EnumSetting<Cooldown>("CooldownMode", Cooldown.Delay, () -> this.page.getValue() == Page.General));
    private final BooleanSetting reset = this.add(new BooleanSetting("Reset", true, () -> this.page.getValue() == Page.General && this.cooldownMode.is(Cooldown.Delay)));
    private final EnumSetting<SwingSide> swingMode = this.add(new EnumSetting<SwingSide>("Swing", SwingSide.All, () -> this.page.getValue() == Page.General));
    private final SliderSetting hurtTime = this.add(new SliderSetting("HurtTime", 10.0, 0.0, 10.0, 1.0, () -> this.page.getValue() == Page.General));
    private final SliderSetting cooldown = this.add(new SliderSetting("Cooldown", 1.1f, 0.0, 1.2f, 0.01, () -> this.page.getValue() == Page.General));
    private final SliderSetting wallRange = this.add(new SliderSetting("WallRange", 6.0, (double)0.1f, 7.0, () -> this.page.getValue() == Page.General));
    private final BooleanSetting whileEating = this.add(new BooleanSetting("WhileUsing", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting weaponOnly = this.add(new BooleanSetting("WeaponOnly", true, () -> this.page.getValue() == Page.General));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All, () -> this.page.getValue() == Page.General));
    private final BooleanSetting Players = this.add(new BooleanSetting("Players", true, () -> this.page.getValue() == Page.Target).setParent());
    private final BooleanSetting armorLow = this.add(new BooleanSetting("ArmorLow", true, () -> this.page.getValue() == Page.Target && this.Players.isOpen()));
    private final BooleanSetting Mobs = this.add(new BooleanSetting("Mobs", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting Animals = this.add(new BooleanSetting("Animals", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting Villagers = this.add(new BooleanSetting("Villagers", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting Slimes = this.add(new BooleanSetting("Slimes", true, () -> this.page.getValue() == Page.Target));
    private final EnumSetting<TargetMode> targetMode = this.add(new EnumSetting<TargetMode>("Filter", TargetMode.DISTANCE, () -> this.page.getValue() == Page.Target));
    private final EnumSetting<TargetESP> mode = this.add(new EnumSetting<TargetESP>("TargetESP", TargetESP.Fill, () -> this.page.getValue() == Page.Render));
    private final SliderSetting animationTime = this.add(new SliderSetting("AnimationTime", 200.0, 0.0, 2000.0, 1.0, () -> this.page.getValue() == Page.Render));
    private final EnumSetting<Easing> ease = this.add(new EnumSetting<Easing>("Ease", Easing.CubicInOut, () -> this.page.getValue() == Page.Render));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 50), () -> this.page.getValue() == Page.Render));
    private final ColorSetting outlineColor = this.add(new ColorSetting("OutlineColor", new Color(255, 255, 255, 50), () -> this.page.getValue() == Page.Render));
    private final ColorSetting hitColor = this.add(new ColorSetting("HitColor", new Color(255, 255, 255, 150), () -> this.page.getValue() == Page.Render));
    private final ColorSetting hitOutlineColor = this.add(new ColorSetting("HitOutlineColor", new Color(255, 255, 255, 150), () -> this.page.getValue() == Page.Render));
    private final Animation animation = new Animation();
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true, () -> this.page.getValue() == Page.Rotate));
    private final BooleanSetting yawStep = this.add(new BooleanSetting("YawStep", false, () -> this.rotate.isOpen() && this.page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting whenElytra = this.add(new BooleanSetting("FallFlying", true, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.05, 0.0, 1.0, 0.01, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final BooleanSetting checkFov = this.add(new BooleanSetting("OnlyLooking", true, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final SliderSetting fov = this.add(new SliderSetting("Fov", 20.0, 0.0, 360.0, 0.1, () -> this.checkFov.getValue() && this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 10, 0, 100, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final Timer tick = new Timer();
    public Vec3d directionVec = null;

    public Aura() {
        super("Aura", Module.Category.Combat);
        this.setChinese("\u6740\u622e\u5149\u73af");
        INSTANCE = this;
    }

    public static void doRender(MatrixStack stack, float partialTicks, Entity entity, Color color, Color outlineColor, TargetESP mode) {
        switch (mode.ordinal()) {
            case 1: {
                Render3DUtil.draw3DBox(stack, ((IEntity)entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), (double)partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), (double)partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), (double)partialTicks))).expand(0.0, 0.1, 0.0), color, outlineColor, true, true);
                break;
            }
            case 0: {
                Render3DUtil.draw3DBox(stack, ((IEntity)entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), (double)partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), (double)partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), (double)partialTicks))).expand(0.0, 0.1, 0.0), color, outlineColor, false, true);
                break;
            }
            case 2: {
                JelloUtil.drawJello(stack, entity, color);
                break;
            }
            case 3: {
                Render3DUtil.drawTargetEsp(stack, target, color);
            }
        }
    }

    public static float getAttackCooldownProgressPerTick() {
        return (float)(1.0 / Aura.mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (target != null) {
            this.doRender(matrixStack, mc.getRenderTickCounter().getTickDelta(true), target, this.mode.getValue());
        }
    }

    public void doRender(MatrixStack stack, float partialTicks, Entity entity, TargetESP mode) {
        switch (mode.ordinal()) {
            case 1: {
                Render3DUtil.draw3DBox(stack, ((IEntity)entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), (double)partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), (double)partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), (double)partialTicks))).expand(0.0, 0.1, 0.0), ColorUtil.fadeColor(this.color.getValue(), this.hitColor.getValue(), this.animation.get(0.0, this.animationTime.getValueInt(), this.ease.getValue())), ColorUtil.fadeColor(this.outlineColor.getValue(), this.hitOutlineColor.getValue(), this.animation.get(0.0, this.animationTime.getValueInt(), this.ease.getValue())), true, true);
                break;
            }
            case 0: {
                Render3DUtil.draw3DBox(stack, ((IEntity)entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), (double)partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), (double)partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), (double)partialTicks))).expand(0.0, 0.1, 0.0), ColorUtil.fadeColor(this.color.getValue(), this.hitColor.getValue(), this.animation.get(0.0, this.animationTime.getValueInt(), this.ease.getValue())), ColorUtil.fadeColor(this.outlineColor.getValue(), this.hitOutlineColor.getValue(), this.animation.get(0.0, this.animationTime.getValueInt(), this.ease.getValue())), false, true);
                break;
            }
            case 2: {
                JelloUtil.drawJello(stack, entity, this.color.getValue());
                break;
            }
            case 3: {
                Render3DUtil.drawTargetEsp(stack, target, this.color.getValue());
            }
        }
    }

    @Override
    public String getInfo() {
        return target == null ? null : target.getName().getString();
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (Aura.nullCheck()) {
            return;
        }
        if (this.timing.is(Timing.Pre) && event.isPost() || this.timing.is(Timing.Post) && event.isPre()) {
            return;
        }
        if (this.weaponOnly.getValue() && !EntityUtil.isHoldingWeapon((PlayerEntity)Aura.mc.player)) {
            target = null;
            return;
        }
        target = this.getTarget(this.range.getValueFloat());
        if (target == null) {
            target = this.getTarget(this.targetRange.getValueFloat());
            return;
        }
        this.doAura();
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (target != null && this.rotate.getValue() && this.shouldYawStep()) {
            this.directionVec = this.getAttackVec(target);
            event.setTarget(this.directionVec, this.steps.getValueFloat(), this.priority.getValueFloat());
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Send event) {
        Packet<?> packet;
        if (this.reset.getValue() && ((packet = event.getPacket()) instanceof HandSwingC2SPacket || packet instanceof PlayerInteractEntityC2SPacket && Criticals.getInteractType((PlayerInteractEntityC2SPacket)packet) == PlayerInteractEntityC2SPacket.InteractType.ATTACK)) {
            this.tick.reset();
        }
    }

    private boolean check() {
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return false;
        }
        int at = (int)(this.tick.getMs() / 50L);
        if (this.cooldownMode.getValue() == Cooldown.Vanilla) {
            at = ((ILivingEntity)Aura.mc.player).getLastAttackedTicks();
        }
        if (!((double)Math.max((float)(at = (int)((float)at * Frog.SERVER.getTPSFactor())) / Aura.getAttackCooldownProgressPerTick(), 0.0f) >= this.cooldown.getValue())) {
            return false;
        }
        Entity entity = target;
        if (entity instanceof LivingEntity) {
            LivingEntity entity2 = (LivingEntity)entity;
            if ((double)entity2.hurtTime > this.hurtTime.getValue()) {
                return false;
            }
        }
        return this.whileEating.getValue() || !Aura.mc.player.isUsingItem();
    }

    private void doAura() {
        Vec3d hitVec;
        if (!this.check()) {
            return;
        }
        if (this.rotate.getValue() && !this.faceVector(hitVec = this.getAttackVec(target))) {
            return;
        }
        this.animation.to = 1.0;
        this.animation.from = 1.0;
        mc.getNetworkHandler().sendPacket((Packet)PlayerInteractEntityC2SPacket.attack((Entity)target, (boolean)Aura.mc.player.isSneaking()));
        Aura.mc.player.resetLastAttackedTicks();
        EntityUtil.swingHand(Hand.MAIN_HAND, this.swingMode.getValue());
        this.tick.reset();
        if (this.rotate.getValue() && !this.shouldYawStep()) {
            Frog.ROTATION.snapBack();
        }
    }

    private Vec3d getAttackVec(Entity entity) {
        return MathUtil.getClosestPointToBox(Aura.mc.player.getEyePos(), entity.getBoundingBox());
    }

    private boolean shouldYawStep() {
        if (!this.whenElytra.getValue() && (Aura.mc.player.isFallFlying() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying())) {
            return false;
        }
        return this.yawStep.getValue() && !Velocity.INSTANCE.noRotation();
    }

    public boolean faceVector(Vec3d directionVec) {
        if (!this.shouldYawStep()) {
            Frog.ROTATION.lookAt(directionVec);
            return true;
        }
        this.directionVec = directionVec;
        if (Frog.ROTATION.inFov(directionVec, this.fov.getValueFloat())) {
            return true;
        }
        return !this.checkFov.getValue();
    }

    public Entity getTarget(double range) {
        Entity target = null;
        double getDistance = range;
        double maxHealth = 36.0;
        for (Entity entity : Frog.THREAD.getEntities()) {
            if (!this.isEnemy(entity)) continue;
            Vec3d hitVec = this.getAttackVec(entity);
            if (Aura.mc.player.getEyePos().distanceTo(hitVec) > range || !Aura.mc.player.canSee(entity) && Aura.mc.player.getEyePos().distanceTo(hitVec) > this.wallRange.getValue() || !CombatUtil.isValid(entity)) continue;
            if (target == null) {
                target = entity;
                getDistance = Aura.mc.player.getEyePos().distanceTo(hitVec);
                maxHealth = EntityUtil.getHealth(entity);
                continue;
            }
            if (this.armorLow.getValue() && entity instanceof PlayerEntity && EntityUtil.isArmorLow((PlayerEntity)entity, 10)) {
                target = entity;
                break;
            }
            if (this.targetMode.getValue() == TargetMode.HEALTH && (double)EntityUtil.getHealth(entity) < maxHealth) {
                target = entity;
                maxHealth = EntityUtil.getHealth(entity);
                continue;
            }
            if (this.targetMode.getValue() != TargetMode.DISTANCE || !(Aura.mc.player.getEyePos().distanceTo(hitVec) < getDistance)) continue;
            target = entity;
            getDistance = Aura.mc.player.getEyePos().distanceTo(hitVec);
        }
        return target;
    }

    private boolean isEnemy(Entity entity) {
        if (entity instanceof SlimeEntity) {
            return this.Slimes.getValue();
        }
        if (entity instanceof PlayerEntity) {
            return this.Players.getValue();
        }
        if (entity instanceof VillagerEntity || entity instanceof WanderingTraderEntity) {
            return this.Villagers.getValue();
        }
        if (entity instanceof AnimalEntity) {
            return this.Animals.getValue();
        }
        if (entity instanceof MobEntity) {
            return this.Mobs.getValue();
        }
        return false;
    }

    public static enum Page {
        General,
        Rotate,
        Target,
        Render;

    }

    public static enum Cooldown {
        Vanilla,
        Delay;

    }

    private static enum TargetMode {
        DISTANCE,
        HEALTH;

    }

    public static enum TargetESP {
        Fill,
        Box,
        Jello,
        ThunderHack,
        None;

    }

    public static enum Mode {
        Mace,
        Axe,
        Sword;

    }
}

