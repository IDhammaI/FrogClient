/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
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
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.combat.CombatUtil;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.api.utils.player.MovementUtil;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.core.impl.BreakManager;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.exploit.Blink;
import dev.idhammai.mod.modules.impl.movement.ElytraFly;
import dev.idhammai.mod.modules.impl.movement.Velocity;
import dev.idhammai.mod.modules.impl.player.PacketMine;
import dev.idhammai.mod.modules.settings.enums.Timing;
import dev.idhammai.mod.modules.settings.impl.BindSetting;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class SelfTrap
extends Module {
    public static SelfTrap INSTANCE;
    private final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
    private final BooleanSetting godMode = this.add(new BooleanSetting("GodMode", true, () -> this.page.is(Page.GodMode)));
    private final SliderSetting breakTime = this.add(new SliderSetting("BreakTime", 2.0, 0.0, 3.0, () -> this.page.is(Page.GodMode)));
    private final BooleanSetting failedSkip = this.add(new BooleanSetting("FailedSkip", true, () -> this.page.is(Page.GodMode)));
    private final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 50, 0, 500, () -> this.page.is(Page.General)));
    private final BooleanSetting mineDownward = this.add(new BooleanSetting("MineDownward", false, () -> this.page.is(Page.General)));
    private final BooleanSetting extend = this.add(new BooleanSetting("Extend", true, () -> this.page.is(Page.General)));
    private final Timer timer = new Timer();
    private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 1, 1, 8, () -> this.page.is(Page.General)));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All, () -> this.page.is(Page.General)));
    private final BooleanSetting packetPlace = this.add(new BooleanSetting("PacketPlace", true, () -> this.page.is(Page.General)));
    private final BooleanSetting breakCrystal = this.add(new BooleanSetting("Break", true, () -> this.page.is(Page.General)).setParent());
    private final BooleanSetting eatPause = this.add(new BooleanSetting("EatingPause", true, () -> this.page.is(Page.General) && this.breakCrystal.isOpen()));
    private final BooleanSetting center = this.add(new BooleanSetting("Center", true, () -> this.page.is(Page.General)));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true, () -> this.page.is(Page.General)));
    private final BooleanSetting enderChest = this.add(new BooleanSetting("EnderChest", true, () -> this.page.is(Page.General)));
    private final BooleanSetting head = this.add(new BooleanSetting("Head", true, () -> this.page.is(Page.General)));
    private final BooleanSetting feet = this.add(new BooleanSetting("Feet", true, () -> this.page.is(Page.General)));
    private final BooleanSetting chest = this.add(new BooleanSetting("Chest", true, () -> this.page.is(Page.General)));
    private final BindSetting headKey = this.add(new BindSetting("HeadKey", -1, () -> this.page.is(Page.General)));
    private final BooleanSetting inAir = this.add(new BooleanSetting("InAir", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", false, () -> this.page.is(Page.Check)));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting noBlockDisable = this.add(new BooleanSetting("NoBlockDisable", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting moveDisable = this.add(new BooleanSetting("MoveDisable", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting jumpDisable = this.add(new BooleanSetting("JumpDisable", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true, () -> this.page.getValue() == Page.Rotate));
    private final BooleanSetting yawStep = this.add(new BooleanSetting("YawStep", false, () -> this.rotate.isOpen() && this.page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting whenElytra = this.add(new BooleanSetting("FallFlying", true, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.05, 0.0, 1.0, 0.01, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final BooleanSetting checkFov = this.add(new BooleanSetting("OnlyLooking", true, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()).setParent());
    private final SliderSetting fov = this.add(new SliderSetting("Fov", 20.0, 0.0, 360.0, 0.1, () -> this.checkFov.isOpen() && this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 10, 0, 100, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    public Vec3d directionVec = null;
    double startX = 0.0;
    double startY = 0.0;
    double startZ = 0.0;
    int progress = 0;
    private boolean shouldCenter = true;
    public static final List<BlockPos> airList;

    public SelfTrap() {
        super("SelfTrap", Module.Category.Combat);
        this.setChinese("\u81ea\u6211\u56f0\u4f4f");
        INSTANCE = this;
        Frog.EVENT_BUS.subscribe(new SelfTrapTick());
    }

    public static boolean selfIntersectPos(BlockPos pos) {
        return SelfTrap.mc.player.getBoundingBox().intersects(new Box(pos));
    }

    public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
        Vec3d vec3d = posTo.subtract(posFrom);
        return SelfTrap.getRotationFromVec(vec3d);
    }

    private static Vec2f getRotationFromVec(Vec3d vec) {
        double d = vec.x;
        double d2 = vec.z;
        double xz = Math.hypot(d, d2);
        d2 = vec.z;
        double d3 = vec.x;
        double yaw = SelfTrap.normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
        double pitch = SelfTrap.normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
        return new Vec2f((float)yaw, (float)pitch);
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (this.directionVec != null && this.rotate.getValue() && this.shouldYawStep()) {
            event.setTarget(this.directionVec, this.steps.getValueFloat(), this.priority.getValueFloat());
        }
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

    @Override
    public void onEnable() {
        if (SelfTrap.nullCheck()) {
            if (this.moveDisable.getValue() || this.jumpDisable.getValue()) {
                this.disable();
            }
            return;
        }
        this.startX = SelfTrap.mc.player.getX();
        this.startY = SelfTrap.mc.player.getY();
        this.startZ = SelfTrap.mc.player.getZ();
        this.shouldCenter = true;
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (SelfTrap.nullCheck()) {
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
        if (!MovementUtil.isMoving() && !SelfTrap.mc.options.jumpKey.isPressed()) {
            this.startX = SelfTrap.mc.player.getX();
            this.startY = SelfTrap.mc.player.getY();
            this.startZ = SelfTrap.mc.player.getZ();
        }
        BlockPos pos = EntityUtil.getPlayerPos(true);
        double distanceToStart = MathHelper.sqrt((float)((float)SelfTrap.mc.player.squaredDistanceTo(this.startX, this.startY, this.startZ)));
        if (this.getBlock() == -1) {
            if (this.noBlockDisable.getValue()) {
                this.disable();
            }
            return;
        }
        if (this.moveDisable.getValue() && distanceToStart > 1.0 || this.jumpDisable.getValue() && SelfTrap.mc.player.input.jumping) {
            this.disable();
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.usingPause.getValue() && SelfTrap.mc.player.isUsingItem()) {
            return;
        }
        if (!this.inAir.getValue() && !SelfTrap.mc.player.isOnGround()) {
            return;
        }
        if (this.head.getValue()) {
            Block block = BlockUtil.getBlock(pos.up());
            if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
                this.clickDown(pos.up(2));
            } else {
                this.tryPlaceBlock(pos.up(2));
            }
        }
        if (this.feet.getValue()) {
            this.doSurround(pos);
        }
        if (this.chest.getValue()) {
            this.doSurround(pos.up());
        }
        airList.clear();
    }

    private boolean shouldYawStep() {
        if (!this.whenElytra.getValue() && (SelfTrap.mc.player.isFallFlying() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying())) {
            return false;
        }
        return this.yawStep.getValue() && !Velocity.INSTANCE.noRotation();
    }

    @EventListener(priority=-1)
    public void onMove(MoveEvent event) {
        if (SelfTrap.nullCheck() || !this.center.getValue() || SelfTrap.mc.player.isFallFlying()) {
            return;
        }
        BlockPos blockPos = EntityUtil.getPlayerPos(true);
        if (SelfTrap.mc.player.getX() - (double)blockPos.getX() - 0.5 <= 0.2 && SelfTrap.mc.player.getX() - (double)blockPos.getX() - 0.5 >= -0.2 && SelfTrap.mc.player.getZ() - (double)blockPos.getZ() - 0.5 <= 0.2 && SelfTrap.mc.player.getZ() - 0.5 - (double)blockPos.getZ() >= -0.2) {
            if (this.shouldCenter && (SelfTrap.mc.player.isOnGround() || MovementUtil.isMoving())) {
                event.setX(0.0);
                event.setZ(0.0);
                this.shouldCenter = false;
            }
        } else if (this.shouldCenter) {
            Vec3d centerPos = EntityUtil.getPlayerPos(true).toCenterPos();
            float rotation = SelfTrap.getRotationTo((Vec3d)SelfTrap.mc.player.getPos(), (Vec3d)centerPos).x;
            float yawRad = rotation / 180.0f * (float)Math.PI;
            double dist = SelfTrap.mc.player.getPos().distanceTo(new Vec3d(centerPos.x, SelfTrap.mc.player.getY(), centerPos.z));
            double cappedSpeed = Math.min(0.2873, dist);
            double x = (double)(-((float)Math.sin(yawRad))) * cappedSpeed;
            double z = (double)((float)Math.cos(yawRad)) * cappedSpeed;
            event.setX(x);
            event.setZ(z);
        }
    }

    private void doSurround(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (i == Direction.UP) continue;
            BlockPos offsetPos = pos.offset(i);
            if (this.godMode.getValue()) {
                for (BreakManager.BreakData breakData : Frog.BREAK.breakMap.values()) {
                    if (breakData.getEntity() == null || this.failedSkip.getValue() && breakData.failed || !breakData.pos.equals((Object)offsetPos)) continue;
                    if (!((double)breakData.timer.getMs() >= this.breakTime.getValue() * 1000.0)) break;
                    airList.add(offsetPos);
                    break;
                }
            }
            if (BlockUtil.getPlaceSide(offsetPos) != null) {
                this.tryPlaceBlock(offsetPos);
            } else if (BlockUtil.canReplace(offsetPos)) {
                this.tryPlaceBlock(this.getHelperPos(offsetPos));
            }
            if (!SelfTrap.selfIntersectPos(offsetPos) || !this.extend.getValue()) continue;
            for (Direction i2 : Direction.values()) {
                if (i2 == Direction.UP) continue;
                BlockPos offsetPos2 = offsetPos.offset(i2);
                if (this.godMode.getValue()) {
                    for (BreakManager.BreakData breakData : Frog.BREAK.breakMap.values()) {
                        if (breakData.getEntity() == null || this.failedSkip.getValue() && breakData.failed || !breakData.pos.equals((Object)offsetPos2)) continue;
                        if (!((double)breakData.timer.getMs() >= this.breakTime.getValue() * 1000.0)) break;
                        airList.add(offsetPos2);
                        break;
                    }
                }
                if (SelfTrap.selfIntersectPos(offsetPos2)) {
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

    private void clickDown(BlockPos pos) {
        if (pos == null) {
            return;
        }
        if (this.detectMining.getValue() && Frog.BREAK.isMining(pos)) {
            return;
        }
        int block = this.getBlock();
        if (block == -1) {
            return;
        }
        Direction side = Direction.DOWN;
        Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
        if (!BlockUtil.clientCanPlace(pos, true) || !airList.contains(pos)) {
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
        int old = SelfTrap.mc.player.getInventory().selectedSlot;
        this.doSwap(block);
        BlockUtil.placedPos.add(pos);
        if (BlockUtil.allowAirPlace()) {
            BlockUtil.airPlace(pos, false, Hand.MAIN_HAND, this.packetPlace.getValue());
        } else {
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), false, Hand.MAIN_HAND, this.packetPlace.getValue());
        }
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
        this.timer.reset();
    }

    private void tryPlaceBlock(BlockPos pos) {
        if (pos == null) {
            return;
        }
        if (this.detectMining.getValue() && Frog.BREAK.isMining(pos)) {
            return;
        }
        BlockPos self = EntityUtil.getPlayerPos(true);
        if (this.mineDownward.getValue() && Objects.equals(PacketMine.getBreakPos(), self.down()) && Objects.equals(PacketMine.getBreakPos(), pos)) {
            return;
        }
        if (!((double)this.progress < this.blocksPer.getValue())) {
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
        int old = SelfTrap.mc.player.getInventory().selectedSlot;
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

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, SelfTrap.mc.player.getInventory().selectedSlot);
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

    static {
        airList = new ArrayList<BlockPos>();
    }

    public static enum Page {
        General,
        Rotate,
        Check,
        GodMode;

    }

    public class SelfTrapTick {
        boolean pressed = false;

        @EventListener
        public void onTick(ClientTickEvent event) {
            if (Module.nullCheck()) {
                return;
            }
            if (SelfTrap.this.timing.is(Timing.Pre) && event.isPost() || SelfTrap.this.timing.is(Timing.Post) && event.isPre()) {
                return;
            }
            if (SelfTrap.this.headKey.isPressed()) {
                if (this.pressed) {
                    return;
                }
            } else {
                this.pressed = false;
                return;
            }
            this.pressed = true;
            SelfTrap.this.directionVec = null;
            SelfTrap.this.progress = 0;
            BlockPos pos = EntityUtil.getPlayerPos(true);
            if (SelfTrap.this.getBlock() == -1) {
                return;
            }
            if (!SelfTrap.this.inAir.getValue() && !Wrapper.mc.player.isOnGround()) {
                return;
            }
            Block block = BlockUtil.getBlock(pos.up());
            if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
                SelfTrap.this.clickDown(pos.up(2));
            } else {
                SelfTrap.this.tryPlaceBlock(pos.up(2));
            }
        }
    }
}

