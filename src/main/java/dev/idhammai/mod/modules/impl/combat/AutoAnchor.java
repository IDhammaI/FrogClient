/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.ClientTickEvent;
import dev.idhammai.api.events.impl.Render3DEvent;
import dev.idhammai.api.events.impl.RotationEvent;
import dev.idhammai.api.utils.combat.CombatUtil;
import dev.idhammai.api.utils.entity.PlayerEntityPredict;
import dev.idhammai.api.utils.math.AnimateUtil;
import dev.idhammai.api.utils.math.ExplosionUtil;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.exploit.Blink;
import dev.idhammai.mod.modules.impl.movement.ElytraFly;
import dev.idhammai.mod.modules.impl.movement.Velocity;
import dev.idhammai.mod.modules.impl.player.AirPlace;
import dev.idhammai.mod.modules.settings.enums.SwingSide;
import dev.idhammai.mod.modules.settings.enums.Timing;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;

public class AutoAnchor
extends Module {
    public static AutoAnchor INSTANCE;
    static Vec3d placeVec3d;
    static Vec3d curVec3d;
    public final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
    public final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 0.0, 6.0, 0.1, () -> this.page.getValue() == Page.General).setSuffix("m"));
    public final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 8.0, 0.1, 12.0, 0.1, () -> this.page.getValue() == Page.General).setSuffix("m"));
    public final SliderSetting minDamage = this.add(new SliderSetting("Min", 4.0, 0.0, 36.0, 0.1, () -> this.page.getValue() == Page.Interact).setSuffix("dmg"));
    public final SliderSetting breakMin = this.add(new SliderSetting("ExplosionMin", 4.0, 0.0, 36.0, 0.1, () -> this.page.getValue() == Page.Interact).setSuffix("dmg"));
    public final SliderSetting headDamage = this.add(new SliderSetting("ForceHead", 7.0, 0.0, 36.0, 0.1, () -> this.page.getValue() == Page.Interact).setSuffix("dmg"));
    private final SliderSetting selfPredict = this.add(new SliderSetting("SelfPredict", 4, 0, 10, () -> this.page.getValue() == Page.Predict).setSuffix("ticks"));
    private final SliderSetting predictTicks = this.add(new SliderSetting("Predict", 4, 0, 10, () -> this.page.getValue() == Page.Predict).setSuffix("ticks"));
    private final SliderSetting simulation = this.add(new SliderSetting("Simulation", 5.0, 0.0, 20.0, 1.0, () -> this.page.getValue() == Page.Predict));
    private final SliderSetting maxMotionY = this.add(new SliderSetting("MaxMotionY", 0.34, 0.0, 2.0, 0.01, () -> this.page.getValue() == Page.Predict));
    private final BooleanSetting step = this.add(new BooleanSetting("Step", false, () -> this.page.getValue() == Page.Predict));
    private final BooleanSetting doubleStep = this.add(new BooleanSetting("DoubleStep", false, () -> this.page.getValue() == Page.Predict));
    private final BooleanSetting jump = this.add(new BooleanSetting("Jump", false, () -> this.page.getValue() == Page.Predict));
    private final BooleanSetting inBlockPause = this.add(new BooleanSetting("InBlockPause", true, () -> this.page.getValue() == Page.Predict));
    final ArrayList<BlockPos> chargeList = new ArrayList();
    private final BooleanSetting assist = this.add(new BooleanSetting("Assist", true, () -> this.page.getValue() == Page.Assist));
    private final BooleanSetting obsidian = this.add(new BooleanSetting("Obsidian", true, () -> this.page.getValue() == Page.Assist));
    private final BooleanSetting checkMine = this.add(new BooleanSetting("DetectMining", false, () -> this.page.getValue() == Page.Assist));
    private final SliderSetting assistRange = this.add(new SliderSetting("AssistRange", 5.0, 0.0, 6.0, 0.1, () -> this.page.getValue() == Page.Assist).setSuffix("m"));
    private final SliderSetting assistDamage = this.add(new SliderSetting("AssistDamage", 6.0, 0.0, 36.0, 0.1, () -> this.page.getValue() == Page.Assist).setSuffix("h"));
    private final SliderSetting delay = this.add(new SliderSetting("AssistDelay", 0.1, 0.0, 1.0, 0.01, () -> this.page.getValue() == Page.Assist).setSuffix("s"));
    private final BooleanSetting preferCrystal = this.add(new BooleanSetting("PreferCrystal", false, () -> this.page.getValue() == Page.General));
    private final BooleanSetting thread = this.add(new BooleanSetting("Thread", false, () -> this.page.getValue() == Page.General));
    private final BooleanSetting light = this.add(new BooleanSetting("LessCPU", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting inventorySwap = this.add(new BooleanSetting("InventorySwap", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting breakCrystal = this.add(new BooleanSetting("BreakCrystal", true, () -> this.page.getValue() == Page.General).setParent());
    private final BooleanSetting spam = this.add(new BooleanSetting("Spam", true, () -> this.page.getValue() == Page.General).setParent());
    private final BooleanSetting mineSpam = this.add(new BooleanSetting("OnlyMining", true, () -> this.page.getValue() == Page.General && this.spam.isOpen()));
    private final BooleanSetting spamPlace = this.add(new BooleanSetting("Fast", true, () -> this.page.getValue() == Page.General).setParent());
    private final BooleanSetting inSpam = this.add(new BooleanSetting("WhenSpamming", true, () -> this.page.getValue() == Page.General && this.spamPlace.isOpen()));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true, () -> this.page.getValue() == Page.General));
    private final EnumSetting<SwingSide> swingMode = this.add(new EnumSetting<SwingSide>("Swing", SwingSide.All, () -> this.page.getValue() == Page.General));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All, () -> this.page.getValue() == Page.General));
    private final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 100.0, 0.0, 500.0, 1.0, () -> this.page.getValue() == Page.General).setSuffix("ms"));
    private final SliderSetting fillDelay = this.add(new SliderSetting("FillDelay", 100.0, 0.0, 500.0, 1.0, () -> this.page.getValue() == Page.General).setSuffix("ms"));
    private final SliderSetting breakDelay = this.add(new SliderSetting("BreakDelay", 100.0, 0.0, 500.0, 1.0, () -> this.page.getValue() == Page.General).setSuffix("ms"));
    private final SliderSetting spamDelay = this.add(new SliderSetting("SpamDelay", 200.0, 0.0, 1000.0, 1.0, () -> this.page.getValue() == Page.General).setSuffix("ms"));
    private final SliderSetting updateDelay = this.add(new SliderSetting("UpdateDelay", 200.0, 0.0, 1000.0, 1.0, () -> this.page.getValue() == Page.General).setSuffix("ms"));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true, () -> this.page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting yawStep = this.add(new BooleanSetting("YawStep", true, () -> this.rotate.isOpen() && this.page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting whenElytra = this.add(new BooleanSetting("FallFlying", true, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.05, 0.0, 1.0, 0.01, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final BooleanSetting checkFov = this.add(new BooleanSetting("OnlyLooking", true, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final SliderSetting fov = this.add(new SliderSetting("Fov", 20.0, 0.0, 360.0, 0.1, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.checkFov.getValue() && this.page.getValue() == Page.Rotate));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 10, 0, 100, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final BooleanSetting noSuicide = this.add(new BooleanSetting("NoSuicide", true, () -> this.page.getValue() == Page.Interact));
    private final BooleanSetting smart = this.add(new BooleanSetting("Smart", true, () -> this.page.getValue() == Page.Interact));
    private final BooleanSetting terrainIgnore = this.add(new BooleanSetting("TerrainIgnore", true, () -> this.page.getValue() == Page.Interact));
    private final SliderSetting minPrefer = this.add(new SliderSetting("Prefer", 7.0, 0.0, 36.0, 0.1, () -> this.page.getValue() == Page.Interact).setSuffix("dmg"));
    private final SliderSetting maxSelfDamage = this.add(new SliderSetting("MaxSelf", 8.0, 0.0, 36.0, 0.1, () -> this.page.getValue() == Page.Interact).setSuffix("dmg"));
    private final EnumSetting<Aura.TargetESP> mode = this.add(new EnumSetting<Aura.TargetESP>("TargetESP", Aura.TargetESP.Jello, () -> this.page.getValue() == Page.Render));
    private final ColorSetting color = this.add(new ColorSetting("TargetColor", new Color(255, 255, 255, 250), () -> this.page.getValue() == Page.Render));
    private final ColorSetting outlineColor = this.add(new ColorSetting("TargetOutlineColor", new Color(255, 255, 255, 250), () -> this.page.getValue() == Page.Render));
    private final BooleanSetting render = this.add(new BooleanSetting("Render", true, () -> this.page.getValue() == Page.Render));
    private final BooleanSetting shrink = this.add(new BooleanSetting("Shrink", true, () -> this.page.getValue() == Page.Render && this.render.getValue()));
    private final ColorSetting box = this.add(new ColorSetting("Box", new Color(255, 255, 255, 255), () -> this.page.getValue() == Page.Render && this.render.getValue()).injectBoolean(true));
    private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(255, 255, 255, 100), () -> this.page.getValue() == Page.Render && this.render.getValue()).injectBoolean(true));
    private final SliderSetting sliderSpeed = this.add(new SliderSetting("SliderSpeed", 0.2, 0.0, 1.0, 0.01, () -> this.page.getValue() == Page.Render && this.render.getValue()));
    private final SliderSetting startFadeTime = this.add(new SliderSetting("StartFade", 0.3, 0.0, 2.0, 0.01, () -> this.page.getValue() == Page.Render && this.render.getValue()).setSuffix("s"));
    private final SliderSetting fadeSpeed = this.add(new SliderSetting("FadeSpeed", 0.2, 0.01, 1.0, 0.01, () -> this.page.getValue() == Page.Render && this.render.getValue()));
    private final Timer delayTimer = new Timer();
    private final Timer calcTimer = new Timer();
    private final Timer noPosTimer = new Timer();
    private final Timer assistTimer = new Timer();
    public Vec3d directionVec = null;
    public PlayerEntity displayTarget;
    public BlockPos currentPos;
    public BlockPos tempPos;
    public double lastDamage;
    double fade = 0.0;
    BlockPos assistPos;

    public AutoAnchor() {
        super("AutoAnchor", Module.Category.Combat);
        this.setChinese("\u91cd\u751f\u951a\u5149\u73af");
        INSTANCE = this;
        Frog.EVENT_BUS.subscribe(new AnchorRender());
    }

    public static boolean canSee(Vec3d from, Vec3d to) {
        BlockHitResult result = AutoAnchor.mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)AutoAnchor.mc.player));
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    @Override
    public String getInfo() {
        if (this.displayTarget != null && this.currentPos != null) {
            return this.displayTarget.getName().getString();
        }
        return null;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (this.displayTarget != null && this.currentPos != null) {
            Aura.doRender(matrixStack, mc.getRenderTickCounter().getTickDelta(true), (Entity)this.displayTarget, this.color.getValue(), this.outlineColor.getValue(), this.mode.getValue());
        }
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (this.currentPos != null && this.rotate.getValue() && this.shouldYawStep() && this.directionVec != null) {
            event.setTarget(this.directionVec, this.steps.getValueFloat(), this.priority.getValueFloat());
        }
    }

    @Override
    public void onDisable() {
        this.tempPos = null;
        this.currentPos = null;
    }

    public void onThread() {
        if (this.isOff() || AutoAnchor.nullCheck()) {
            return;
        }
        if (this.thread.getValue()) {
            int unBlock;
            if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
                this.currentPos = null;
                return;
            }
            if (AutoCrystal.INSTANCE.isOn() && AutoCrystal.INSTANCE.crystalPos != null && this.preferCrystal.getValue()) {
                this.currentPos = null;
                return;
            }
            int anchor = this.inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR) : InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
            int glowstone = this.inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.GLOWSTONE) : InventoryUtil.findBlock(Blocks.GLOWSTONE);
            int n = unBlock = this.inventorySwap.getValue() ? anchor : InventoryUtil.findUnBlock();
            if (anchor == -1) {
                this.currentPos = null;
                return;
            }
            if (glowstone == -1) {
                this.currentPos = null;
                return;
            }
            if (unBlock == -1) {
                this.currentPos = null;
                return;
            }
            if (AutoAnchor.mc.player.isSneaking()) {
                this.currentPos = null;
                return;
            }
            if (this.usingPause.getValue() && AutoAnchor.mc.player.isUsingItem()) {
                this.currentPos = null;
                return;
            }
            this.calc();
        }
    }

    private boolean shouldYawStep() {
        if (!this.whenElytra.getValue() && (AutoAnchor.mc.player.isFallFlying() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying())) {
            return false;
        }
        return this.yawStep.getValue() && !Velocity.INSTANCE.noRotation();
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        BlockPos pos;
        if (AutoAnchor.nullCheck()) {
            return;
        }
        if (this.timing.is(Timing.Pre) && event.isPost() || this.timing.is(Timing.Post) && event.isPre()) {
            return;
        }
        int anchor = this.inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR) : InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
        int glowstone = this.inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.GLOWSTONE) : InventoryUtil.findBlock(Blocks.GLOWSTONE);
        int unBlock = this.inventorySwap.getValue() ? anchor : InventoryUtil.findUnBlock();
        int old = AutoAnchor.mc.player.getInventory().selectedSlot;
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            this.currentPos = null;
            return;
        }
        if (AutoCrystal.INSTANCE.isOn() && AutoCrystal.INSTANCE.crystalPos != null) {
            this.currentPos = null;
            return;
        }
        if (anchor == -1) {
            this.currentPos = null;
            return;
        }
        if (glowstone == -1) {
            this.currentPos = null;
            return;
        }
        if (unBlock == -1) {
            this.currentPos = null;
            return;
        }
        if (AutoAnchor.mc.player.isSneaking()) {
            this.currentPos = null;
            return;
        }
        if (this.usingPause.getValue() && AutoAnchor.mc.player.isUsingItem()) {
            this.currentPos = null;
            return;
        }
        if (this.inventorySwap.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (this.assist.getValue()) {
            this.onAssist();
        }
        if (!this.thread.getValue()) {
            this.calc();
        }
        if ((pos = this.currentPos) != null) {
            boolean shouldSpam;
            if (this.breakCrystal.getValue()) {
                CombatUtil.attackCrystal(new BlockPos((Vec3i)pos), this.rotate.getValue(), false);
            }
            boolean bl = shouldSpam = this.spam.getValue() && (!this.mineSpam.getValue() || Frog.BREAK.isMining(pos));
            if (shouldSpam) {
                if (!this.delayTimer.passed((long)this.spamDelay.getValueFloat())) {
                    return;
                }
                this.delayTimer.reset();
                if (BlockUtil.canPlace(pos, this.range.getValue(), this.breakCrystal.getValue())) {
                    this.placeBlock(pos, this.rotate.getValue(), anchor);
                }
                if (!this.chargeList.contains(pos)) {
                    this.delayTimer.reset();
                    this.clickBlock(pos, BlockUtil.getClickSide(pos), this.rotate.getValue(), glowstone);
                    this.chargeList.add(pos);
                }
                this.chargeList.remove(pos);
                this.clickBlock(pos, BlockUtil.getClickSide(pos), this.rotate.getValue(), unBlock);
                if (this.spamPlace.getValue() && this.inSpam.getValue()) {
                    if (this.shouldYawStep() && this.checkFov.getValue()) {
                        Direction side = BlockUtil.getClickSide(pos);
                        Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
                        if (Frog.ROTATION.inFov(directionVec, this.fov.getValueFloat())) {
                            CombatUtil.modifyPos = pos;
                            CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                            this.placeBlock(pos, this.rotate.getValue(), anchor);
                            CombatUtil.modifyPos = null;
                        }
                    } else {
                        CombatUtil.modifyPos = pos;
                        CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                        this.placeBlock(pos, this.rotate.getValue(), anchor);
                        CombatUtil.modifyPos = null;
                    }
                }
            } else if (BlockUtil.canPlace(pos, this.range.getValue(), this.breakCrystal.getValue())) {
                if (!this.delayTimer.passed((long)this.placeDelay.getValueFloat())) {
                    return;
                }
                this.delayTimer.reset();
                this.placeBlock(pos, this.rotate.getValue(), anchor);
            } else if (BlockUtil.getBlock(pos) == Blocks.RESPAWN_ANCHOR) {
                if (!this.chargeList.contains(pos)) {
                    if (!this.delayTimer.passed((long)this.fillDelay.getValueFloat())) {
                        return;
                    }
                    this.delayTimer.reset();
                    this.clickBlock(pos, BlockUtil.getClickSide(pos), this.rotate.getValue(), glowstone);
                    this.chargeList.add(pos);
                } else {
                    if (!this.delayTimer.passed((long)this.breakDelay.getValueFloat())) {
                        return;
                    }
                    this.delayTimer.reset();
                    this.chargeList.remove(pos);
                    this.clickBlock(pos, BlockUtil.getClickSide(pos), this.rotate.getValue(), unBlock);
                    if (this.spamPlace.getValue()) {
                        if (this.shouldYawStep() && this.checkFov.getValue()) {
                            Direction side = BlockUtil.getClickSide(pos);
                            Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
                            if (Frog.ROTATION.inFov(directionVec, this.fov.getValueFloat())) {
                                CombatUtil.modifyPos = pos;
                                CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                                this.placeBlock(pos, this.rotate.getValue(), anchor);
                                CombatUtil.modifyPos = null;
                            }
                        } else {
                            CombatUtil.modifyPos = pos;
                            CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                            this.placeBlock(pos, this.rotate.getValue(), anchor);
                            CombatUtil.modifyPos = null;
                        }
                    }
                }
            }
            if (!this.inventorySwap.getValue()) {
                this.doSwap(old);
            }
        }
    }

    private void calc() {
        if (AutoAnchor.nullCheck()) {
            return;
        }
        if (this.calcTimer.passed((long)this.updateDelay.getValueFloat())) {
            double damage;
            this.calcTimer.reset();
            PlayerEntityPredict selfPredict = new PlayerEntityPredict((PlayerEntity)AutoAnchor.mc.player, this.maxMotionY.getValue(), this.selfPredict.getValueInt(), this.simulation.getValueInt(), this.step.getValue(), this.doubleStep.getValue(), this.jump.getValue(), this.inBlockPause.getValue());
            this.tempPos = null;
            double placeDamage = this.minDamage.getValue();
            double breakDamage = this.breakMin.getValue();
            boolean anchorFound = false;
            List<PlayerEntity> enemies = CombatUtil.getEnemies(this.targetRange.getValue());
            ArrayList<PlayerEntityPredict> list = new ArrayList<PlayerEntityPredict>();
            for (PlayerEntity player : enemies) {
                list.add(new PlayerEntityPredict(player, this.maxMotionY.getValue(), this.predictTicks.getValueInt(), this.simulation.getValueInt(), this.step.getValue(), this.doubleStep.getValue(), this.jump.getValue(), this.inBlockPause.getValue()));
            }
            for (PlayerEntityPredict pap : list) {
                double selfDamage;
                BlockPos pos = EntityUtil.getEntityPos((Entity)pap.player, true).up(2);
                if (!BlockUtil.canPlace(pos, this.range.getValue(), this.breakCrystal.getValue()) && (BlockUtil.getBlock(pos) != Blocks.RESPAWN_ANCHOR || BlockUtil.getClickSideStrict(pos) == null) || (selfDamage = this.getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > this.maxSelfDamage.getValue() || this.noSuicide.getValue() && selfDamage > (double)(AutoAnchor.mc.player.getHealth() + AutoAnchor.mc.player.getAbsorptionAmount())) continue;
                damage = this.getAnchorDamage(pos, pap.player, pap.predict);
                if (!(damage > (double)this.headDamage.getValueFloat()) || this.smart.getValue() && selfDamage > damage) continue;
                this.lastDamage = damage;
                this.displayTarget = pap.player;
                this.tempPos = pos;
                break;
            }
            if (this.tempPos == null) {
                for (BlockPos pos : BlockUtil.getSphere(this.range.getValueFloat() + 1.0f, AutoAnchor.mc.player.getEyePos())) {
                    for (PlayerEntityPredict pap : list) {
                        double selfDamage;
                        boolean skip;
                        if (this.light.getValue()) {
                            CombatUtil.modifyPos = pos;
                            CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                            skip = !AutoAnchor.canSee(pos.toCenterPos(), pap.predict.getPos());
                            CombatUtil.modifyPos = null;
                            if (skip) continue;
                        }
                        if (BlockUtil.getBlock(pos) != Blocks.RESPAWN_ANCHOR) {
                            double selfDamage2;
                            if (anchorFound || !BlockUtil.canPlace(pos, this.range.getValue(), this.breakCrystal.getValue())) continue;
                            CombatUtil.modifyPos = pos;
                            CombatUtil.modifyBlockState = Blocks.OBSIDIAN.getDefaultState();
                            skip = BlockUtil.getClickSideStrict(pos) == null;
                            CombatUtil.modifyPos = null;
                            if (skip || !((damage = this.getAnchorDamage(pos, pap.player, pap.predict)) >= placeDamage) || AutoCrystal.INSTANCE.crystalPos != null && !AutoCrystal.INSTANCE.isOff() && !((double)AutoCrystal.INSTANCE.lastDamage < damage) || (selfDamage2 = this.getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > this.maxSelfDamage.getValue() || this.noSuicide.getValue() && selfDamage2 > (double)(AutoAnchor.mc.player.getHealth() + AutoAnchor.mc.player.getAbsorptionAmount()) || this.smart.getValue() && selfDamage2 > damage) continue;
                            this.lastDamage = damage;
                            this.displayTarget = pap.player;
                            placeDamage = damage;
                            this.tempPos = pos;
                            continue;
                        }
                        double damage2 = this.getAnchorDamage(pos, pap.player, pap.predict);
                        if (BlockUtil.getClickSideStrict(pos) == null || !(damage2 >= breakDamage)) continue;
                        if (damage2 >= this.minPrefer.getValue()) {
                            anchorFound = true;
                        }
                        if (!anchorFound && damage2 < placeDamage || AutoCrystal.INSTANCE.crystalPos != null && !AutoCrystal.INSTANCE.isOff() && !((double)AutoCrystal.INSTANCE.lastDamage < damage2) || (selfDamage = this.getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > this.maxSelfDamage.getValue() || this.noSuicide.getValue() && selfDamage > (double)(AutoAnchor.mc.player.getHealth() + AutoAnchor.mc.player.getAbsorptionAmount()) || this.smart.getValue() && selfDamage > damage2) continue;
                        this.lastDamage = damage2;
                        this.displayTarget = pap.player;
                        breakDamage = damage2;
                        this.tempPos = pos;
                    }
                }
            }
        }
        this.currentPos = this.tempPos;
    }

    public double getAnchorDamage(BlockPos anchorPos, PlayerEntity target, PlayerEntity predict) {
        if (this.terrainIgnore.getValue()) {
            CombatUtil.terrainIgnore = true;
        }
        double damage = ExplosionUtil.anchorDamage(anchorPos, (LivingEntity)target, (LivingEntity)predict);
        CombatUtil.terrainIgnore = false;
        return damage;
    }

    public void placeBlock(BlockPos pos, boolean rotate, int slot) {
        if (BlockUtil.allowAirPlace()) {
            this.airPlace(pos, rotate, slot);
            return;
        }
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) {
            return;
        }
        this.clickBlock(pos.offset(side), side.getOpposite(), rotate, slot);
    }

    public void clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
        if (pos == null) {
            return;
        }
        Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
        if (rotate && !this.faceVector(directionVec)) {
            return;
        }
        this.doSwap(slot);
        EntityUtil.swingHand(Hand.MAIN_HAND, this.swingMode.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, id));
        if (this.inventorySwap.getValue()) {
            this.doSwap(slot);
        }
        if (rotate && !this.shouldYawStep()) {
            Frog.ROTATION.snapBack();
        }
    }

    public void airPlace(BlockPos pos, boolean rotate, int slot) {
        if (pos == null) {
            return;
        }
        Direction side = BlockUtil.getClickSide(pos);
        Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
        if (rotate && !this.faceVector(directionVec)) {
            return;
        }
        this.doSwap(slot);
        boolean bypass = AirPlace.INSTANCE.grimBypass.getValue();
        if (bypass) {
            mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
        }
        EntityUtil.swingHand(Hand.MAIN_HAND, this.swingMode.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(bypass ? Hand.OFF_HAND : Hand.MAIN_HAND, result, id));
        if (bypass) {
            mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
        }
        if (this.inventorySwap.getValue()) {
            this.doSwap(slot);
        }
        if (rotate && !this.shouldYawStep()) {
            Frog.ROTATION.snapBack();
        }
    }

    private void doSwap(int slot) {
        if (this.inventorySwap.getValue()) {
            InventoryUtil.inventorySwap(slot, AutoAnchor.mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
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

    public void onAssist() {
        BlockPos placePos;
        this.assistPos = null;
        int anchor = this.inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR) : InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
        int glowstone = this.inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.GLOWSTONE) : InventoryUtil.findBlock(Blocks.GLOWSTONE);
        int old = AutoAnchor.mc.player.getInventory().selectedSlot;
        if (anchor == -1) {
            return;
        }
        if (this.obsidian.getValue()) {
            int n = anchor = this.inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) : InventoryUtil.findBlock(Blocks.OBSIDIAN);
            if (anchor == -1) {
                return;
            }
        }
        if (glowstone == -1) {
            return;
        }
        if (AutoAnchor.mc.player.isSneaking()) {
            return;
        }
        if (this.usingPause.getValue() && AutoAnchor.mc.player.isUsingItem()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (!this.assistTimer.passed((long)(this.delay.getValueFloat() * 1000.0f))) {
            return;
        }
        this.assistTimer.reset();
        ArrayList<PlayerEntityPredict> list = new ArrayList<PlayerEntityPredict>();
        for (PlayerEntity player : CombatUtil.getEnemies(this.assistRange.getValue())) {
            list.add(new PlayerEntityPredict(player, this.maxMotionY.getValue(), this.predictTicks.getValueInt(), this.simulation.getValueInt(), this.step.getValue(), this.doubleStep.getValue(), this.jump.getValue(), this.inBlockPause.getValue()));
        }
        double bestDamage = this.assistDamage.getValue();
        for (PlayerEntityPredict pap : list) {
            double damage;
            BlockPos pos = EntityUtil.getEntityPos((Entity)pap.player, true).up(2);
            if (AutoAnchor.mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR) {
                return;
            }
            if (BlockUtil.clientCanPlace(pos, false) && (damage = this.getAnchorDamage(pos, pap.player, pap.predict)) >= bestDamage) {
                bestDamage = damage;
                this.assistPos = pos;
            }
            for (Direction i : Direction.values()) {
                double damage2;
                if (i == Direction.UP || i == Direction.DOWN || !BlockUtil.clientCanPlace(pos.offset(i), false) || !((damage2 = this.getAnchorDamage(pos.offset(i), pap.player, pap.predict)) >= bestDamage)) continue;
                bestDamage = damage2;
                this.assistPos = pos.offset(i);
            }
        }
        if (this.assistPos != null && BlockUtil.getPlaceSide(this.assistPos, this.range.getValue()) == null && (placePos = this.getHelper(this.assistPos)) != null) {
            this.doSwap(anchor);
            BlockUtil.placeBlock(placePos, this.rotate.getValue());
            if (this.inventorySwap.getValue()) {
                this.doSwap(anchor);
            } else {
                this.doSwap(old);
            }
        }
    }

    public BlockPos getHelper(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (this.checkMine.getValue() && Frog.BREAK.isMining(pos.offset(i)) || !BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite()) || !BlockUtil.canPlace(pos.offset(i))) continue;
            return pos.offset(i);
        }
        return null;
    }

    public static enum Page {
        General,
        Interact,
        Predict,
        Rotate,
        Assist,
        Render;

    }

    public class AnchorRender {
        @EventListener
        public void onRender3D(Render3DEvent event) {
            BlockPos currentPos = AutoAnchor.INSTANCE.currentPos;
            if (currentPos != null) {
                AutoAnchor.this.noPosTimer.reset();
                placeVec3d = currentPos.toCenterPos();
            }
            if (placeVec3d == null) {
                return;
            }
            AutoAnchor.this.fade = AutoAnchor.this.fadeSpeed.getValue() >= 1.0 ? (AutoAnchor.this.noPosTimer.passed((long)(AutoAnchor.this.startFadeTime.getValue() * 1000.0)) ? 0.0 : 0.5) : AnimateUtil.animate(AutoAnchor.this.fade, AutoAnchor.this.noPosTimer.passed((long)(AutoAnchor.this.startFadeTime.getValue() * 1000.0)) ? 0.0 : 0.5, AutoAnchor.this.fadeSpeed.getValue() / 10.0);
            if (AutoAnchor.this.fade == 0.0) {
                curVec3d = null;
                return;
            }
            curVec3d = curVec3d == null || AutoAnchor.this.sliderSpeed.getValue() >= 1.0 ? placeVec3d : new Vec3d(AnimateUtil.animate(AutoAnchor.curVec3d.x, AutoAnchor.placeVec3d.x, AutoAnchor.this.sliderSpeed.getValue() / 10.0), AnimateUtil.animate(AutoAnchor.curVec3d.y, AutoAnchor.placeVec3d.y, AutoAnchor.this.sliderSpeed.getValue() / 10.0), AnimateUtil.animate(AutoAnchor.curVec3d.z, AutoAnchor.placeVec3d.z, AutoAnchor.this.sliderSpeed.getValue() / 10.0));
            if (AutoAnchor.this.render.getValue()) {
                Box cbox = new Box(curVec3d, curVec3d);
                cbox = AutoAnchor.this.shrink.getValue() ? cbox.expand(AutoAnchor.this.fade) : cbox.expand(0.5);
                if (AutoAnchor.this.fill.booleanValue) {
                    event.drawFill(cbox, ColorUtil.injectAlpha(AutoAnchor.this.fill.getValue(), (int)((double)AutoAnchor.this.fill.getValue().getAlpha() * AutoAnchor.this.fade * 2.0)));
                }
                if (AutoAnchor.this.box.booleanValue) {
                    event.drawBox(cbox, ColorUtil.injectAlpha(AutoAnchor.this.box.getValue(), (int)((double)AutoAnchor.this.box.getValue().getAlpha() * AutoAnchor.this.fade * 2.0)));
                }
            }
        }
    }
}

