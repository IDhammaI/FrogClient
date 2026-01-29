/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.ClientTickEvent;
import dev.idhammai.api.events.impl.MoveEvent;
import dev.idhammai.api.events.impl.RotationEvent;
import dev.idhammai.api.utils.combat.CombatUtil;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.api.utils.player.MovementUtil;
import dev.idhammai.api.utils.world.BlockPosX;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.core.impl.CommandManager;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.exploit.Blink;
import dev.idhammai.mod.modules.impl.movement.ElytraFly;
import dev.idhammai.mod.modules.impl.movement.Velocity;
import dev.idhammai.mod.modules.impl.player.PacketMine;
import dev.idhammai.mod.modules.settings.enums.Timing;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.util.Objects;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class Surround
extends Module {
    public static Surround INSTANCE;
    public final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
    public final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 50, 0, 500, () -> this.page.is(Page.General)));
    private final BooleanSetting mineDownward = this.add(new BooleanSetting("MineDownward", false, () -> this.page.is(Page.General)));
    public final BooleanSetting extend = this.add(new BooleanSetting("Extend", true, () -> this.page.is(Page.General))).setParent();
    public final BooleanSetting onlySelf = this.add(new BooleanSetting("OnlySelf", false, () -> this.page.is(Page.General) && this.extend.isOpen()));
    public final BooleanSetting inAir = this.add(new BooleanSetting("InAir", true, () -> this.page.is(Page.Check)));
    private final Timer timer = new Timer();
    private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 1, 1, 8, () -> this.page.is(Page.General)));
    private final BooleanSetting packetPlace = this.add(new BooleanSetting("PacketPlace", true, () -> this.page.is(Page.General)));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All, () -> this.page.is(Page.General)));
    private final BooleanSetting breakCrystal = this.add(new BooleanSetting("Break", true, () -> this.page.is(Page.General)).setParent());
    private final BooleanSetting eatPause = this.add(new BooleanSetting("EatingPause", true, () -> this.page.is(Page.General) && this.breakCrystal.isOpen()));
    private final BooleanSetting center = this.add(new BooleanSetting("Center", true, () -> this.page.is(Page.General)));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true, () -> this.page.is(Page.General)));
    private final BooleanSetting enderChest = this.add(new BooleanSetting("EnderChest", true, () -> this.page.is(Page.General)));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true, () -> this.page.getValue() == Page.Rotate));
    private final BooleanSetting yawStep = this.add(new BooleanSetting("YawStep", false, () -> this.rotate.isOpen() && this.page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting whenElytra = this.add(new BooleanSetting("FallFlying", true, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.05, 0.0, 1.0, 0.01, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final BooleanSetting checkFov = this.add(new BooleanSetting("OnlyLooking", true, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()).setParent());
    private final SliderSetting fov = this.add(new SliderSetting("Fov", 20.0, 0.0, 360.0, 0.1, () -> this.checkFov.isOpen() && this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 10, 0, 100, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", false, () -> this.page.is(Page.Check)));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting moveDisable = this.add(new BooleanSetting("MoveDisable", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting jumpDisable = this.add(new BooleanSetting("JumpDisable", true, () -> this.page.is(Page.Check)));
    public Vec3d directionVec = null;
    double startX = 0.0;
    double startY = 0.0;
    double startZ = 0.0;
    int progress = 0;
    private boolean shouldCenter = true;

    public Surround() {
        super("Surround", "Surrounds you with Obsidian", Module.Category.Combat);
        this.setChinese("\u56f4\u811a");
        INSTANCE = this;
    }

    public static boolean selfIntersectPos(BlockPos pos) {
        return Surround.mc.player.getBoundingBox().intersects(new Box(pos));
    }

    public static boolean otherIntersectPos(BlockPos pos) {
        for (AbstractClientPlayerEntity player : Frog.THREAD.getPlayers()) {
            if (!player.getBoundingBox().intersects(new Box(pos))) continue;
            return true;
        }
        return false;
    }

    public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
        Vec3d vec3d = posTo.subtract(posFrom);
        return Surround.getRotationFromVec(vec3d);
    }

    private static Vec2f getRotationFromVec(Vec3d vec) {
        double d = vec.x;
        double d2 = vec.z;
        double xz = Math.hypot(d, d2);
        d2 = vec.z;
        double d3 = vec.x;
        double yaw = Surround.normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
        double pitch = Surround.normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
        return new Vec2f((float)yaw, (float)pitch);
    }

    private static double normalizeAngle(double angleIn) {
        double angle = angleIn;
        angle %= 360.0;
        if (angle >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return angle;
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (this.directionVec != null && this.rotate.getValue() && this.shouldYawStep()) {
            event.setTarget(this.directionVec, this.steps.getValueFloat(), this.priority.getValueFloat());
        }
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (Surround.nullCheck()) {
            return;
        }
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (this.timing.is(Timing.Pre) && event.isPost() || this.timing.is(Timing.Post) && event.isPre()) {
            return;
        }
        if (!this.timer.passed((long)this.placeDelay.getValue())) {
            return;
        }
        this.directionVec = null;
        this.progress = 0;
        if (!MovementUtil.isMoving() && !Surround.mc.options.jumpKey.isPressed()) {
            this.startX = Surround.mc.player.getX();
            this.startY = Surround.mc.player.getY();
            this.startZ = Surround.mc.player.getZ();
        }
        double distanceToStart = MathHelper.sqrt((float)((float)Surround.mc.player.squaredDistanceTo(this.startX, this.startY, this.startZ)));
        if (this.getBlock() == -1) {
            CommandManager.sendMessageId("\u00a74No block found", this.hashCode() - 1);
            this.disable();
            return;
        }
        if (this.moveDisable.getValue() && distanceToStart > 1.0 || this.jumpDisable.getValue() && Surround.mc.player.input.jumping) {
            this.disable();
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.usingPause.getValue() && Surround.mc.player.isUsingItem()) {
            return;
        }
        if (!this.inAir.getValue() && !Surround.mc.player.isOnGround()) {
            return;
        }
        this.doSurround(new BlockPosX(Surround.mc.player.getX(), Surround.mc.player.getY(), Surround.mc.player.getZ()));
        this.doSurround(new BlockPosX(Surround.mc.player.getX(), Surround.mc.player.getY() + 0.8, Surround.mc.player.getZ()));
    }

    public void doSurround(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (i == Direction.UP) continue;
            BlockPos offsetPos = pos.offset(i);
            if (BlockUtil.getPlaceSide(offsetPos) != null) {
                this.tryPlaceBlock(offsetPos);
            } else if (BlockUtil.canReplace(offsetPos)) {
                this.tryPlaceBlock(this.getHelperPos(offsetPos));
            }
            if (!Surround.selfIntersectPos(offsetPos) && (this.onlySelf.getValue() || !Surround.otherIntersectPos(offsetPos)) || !this.extend.getValue()) continue;
            for (Direction i2 : Direction.values()) {
                if (i2 == Direction.UP) continue;
                BlockPos offsetPos2 = offsetPos.offset(i2);
                if (Surround.selfIntersectPos(offsetPos2) || !this.onlySelf.getValue() && Surround.otherIntersectPos(offsetPos2)) {
                    for (Direction i3 : Direction.values()) {
                        if (i3 == Direction.UP) continue;
                        this.tryPlaceBlock(offsetPos2);
                        BlockPos offsetPos3 = offsetPos2.offset(i3);
                        this.tryPlaceBlock(BlockUtil.getPlaceSide(offsetPos3) != null || !BlockUtil.canReplace(offsetPos3) ? offsetPos3 : this.getHelperPos(offsetPos3));
                    }
                }
                this.tryPlaceBlock(BlockUtil.getPlaceSide(offsetPos2) != null || !BlockUtil.canReplace(offsetPos2) ? offsetPos2 : this.getHelperPos(offsetPos2));
            }
        }
    }

    @Override
    public void onEnable() {
        if (Surround.nullCheck()) {
            if (this.moveDisable.getValue() || this.jumpDisable.getValue()) {
                this.disable();
            }
            return;
        }
        this.startX = Surround.mc.player.getX();
        this.startY = Surround.mc.player.getY();
        this.startZ = Surround.mc.player.getZ();
        this.shouldCenter = true;
    }

    private boolean shouldYawStep() {
        if (!this.whenElytra.getValue() && (Surround.mc.player.isFallFlying() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying())) {
            return false;
        }
        return this.yawStep.getValue() && !Velocity.INSTANCE.noRotation();
    }

    @EventListener(priority=-1)
    public void onMove(MoveEvent event) {
        if (Surround.nullCheck() || !this.center.getValue() || Surround.mc.player.isFallFlying()) {
            return;
        }
        BlockPos blockPos = EntityUtil.getPlayerPos(true);
        if (Surround.mc.player.getX() - (double)blockPos.getX() - 0.5 <= 0.2 && Surround.mc.player.getX() - (double)blockPos.getX() - 0.5 >= -0.2 && Surround.mc.player.getZ() - (double)blockPos.getZ() - 0.5 <= 0.2 && Surround.mc.player.getZ() - 0.5 - (double)blockPos.getZ() >= -0.2) {
            if (this.shouldCenter && (Surround.mc.player.isOnGround() || MovementUtil.isMoving())) {
                event.setX(0.0);
                event.setZ(0.0);
                this.shouldCenter = false;
            }
        } else if (this.shouldCenter) {
            Vec3d centerPos = EntityUtil.getPlayerPos(true).toCenterPos();
            float rotation = Surround.getRotationTo((Vec3d)Surround.mc.player.getPos(), (Vec3d)centerPos).x;
            float yawRad = rotation / 180.0f * (float)Math.PI;
            double dist = Surround.mc.player.getPos().distanceTo(new Vec3d(centerPos.x, Surround.mc.player.getY(), centerPos.z));
            double cappedSpeed = Math.min(0.2873, dist);
            double x = (double)(-((float)Math.sin(yawRad))) * cappedSpeed;
            double z = (double)((float)Math.cos(yawRad)) * cappedSpeed;
            event.setX(x);
            event.setZ(z);
        }
    }

    private void tryPlaceBlock(BlockPos pos) {
        if (pos == null) {
            return;
        }
        if (this.detectMining.getValue() && Frog.BREAK.isMining(pos)) {
            return;
        }
        if (!((double)this.progress < this.blocksPer.getValue())) {
            return;
        }
        BlockPos self = EntityUtil.getPlayerPos(true);
        if (this.mineDownward.getValue() && Objects.equals(PacketMine.getBreakPos(), self.down()) && Objects.equals(PacketMine.getBreakPos(), pos)) {
            return;
        }
        int block = this.getBlock();
        if (block == -1) {
            return;
        }
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) {
            return;
        }
        Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
        if (!BlockUtil.canPlace(pos, 6.0, true)) {
            return;
        }
        if (this.rotate.getValue() && !this.faceVector(directionVec)) {
            return;
        }
        if (this.breakCrystal.getValue()) {
            CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.eatPause.getValue());
        } else if (BlockUtil.hasEntity(pos, false)) {
            return;
        }
        int old = Surround.mc.player.getInventory().selectedSlot;
        this.doSwap(block);
        BlockUtil.placedPos.add(pos);
        if (BlockUtil.allowAirPlace()) {
            BlockUtil.airPlace(pos, false, Hand.MAIN_HAND, this.packetPlace.getValue());
        } else {
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), false, Hand.MAIN_HAND, this.packetPlace.getValue());
        }
        this.timer.reset();
        if (this.inventory.getValue()) {
            this.doSwap(block);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(old);
        }
        if (this.rotate.getValue() && !this.shouldYawStep()) {
            Frog.ROTATION.snapBack();
        }
        ++this.progress;
    }

    private boolean faceVector(Vec3d directionVec) {
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

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, Surround.mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (this.inventory.getValue()) {
            if (InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) != -1 || !this.enderChest.getValue()) {
                return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST);
        }
        if (InventoryUtil.findBlock(Blocks.OBSIDIAN) != -1 || !this.enderChest.getValue()) {
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
        return InventoryUtil.findBlock(Blocks.ENDER_CHEST);
    }

    public BlockPos getHelperPos(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (this.detectMining.getValue() && Frog.BREAK.isMining(pos.offset(i)) || !BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite()) || !BlockUtil.canPlace(pos.offset(i))) continue;
            return pos.offset(i);
        }
        return null;
    }

    public static enum Page {
        General,
        Rotate,
        Check;

    }
}

