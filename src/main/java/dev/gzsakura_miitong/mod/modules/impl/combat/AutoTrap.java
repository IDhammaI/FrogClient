/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.ConcretePowderBlock
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ElytraItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package dev.gzsakura_miitong.mod.modules.impl.combat;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.combat.CombatUtil;
import dev.gzsakura_miitong.api.utils.math.PredictUtil;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.player.EntityUtil;
import dev.gzsakura_miitong.api.utils.player.InventoryUtil;
import dev.gzsakura_miitong.api.utils.world.BlockPosX;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.exploit.Blink;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class AutoTrap
extends Module {
    public static AutoTrap INSTANCE;
    public final SliderSetting delay = this.add(new SliderSetting("Delay", 100, 0, 500).setSuffix("ms"));
    private final EnumSetting<TargetMode> targetMod = this.add(new EnumSetting<TargetMode>("TargetMode", TargetMode.Single));
    private final EnumSetting<Mode> headMode = this.add(new EnumSetting<Mode>("BlockForHead", Mode.Anchor));
    final ArrayList<BlockPos> trapList = new ArrayList();
    final ArrayList<BlockPos> placeList = new ArrayList();
    private final Timer timer = new Timer();
    private final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 4.0, 1.0, 6.0).setSuffix("m"));
    private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 1, 1, 8));
    public final SliderSetting predictTicks = this.add(new SliderSetting("PredictTicks", 2.0, 0.0, 50.0, 1.0));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 1.0, 8.0).setSuffix("m"));
    private final BooleanSetting checkMine = this.add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting helper = this.add(new BooleanSetting("Helper", true));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting onlyCrawling = this.add(new BooleanSetting("OnlyCrawling", false));
    private final BooleanSetting checkElytra = this.add(new BooleanSetting("CheckElytra", false));
    private final BooleanSetting extend = this.add(new BooleanSetting("Extend", true));
    private final BooleanSetting antiStep = this.add(new BooleanSetting("AntiStep", false));
    private final BooleanSetting onlyBreak = this.add(new BooleanSetting("OnlyBreak", false, this.antiStep::getValue));
    private final BooleanSetting head = this.add(new BooleanSetting("Head", true));
    private final BooleanSetting headExtend = this.add(new BooleanSetting("HeadExtend", true));
    private final BooleanSetting chestUp = this.add(new BooleanSetting("ChestUp", true));
    private final BooleanSetting onlyBreaking = this.add(new BooleanSetting("OnlyBreaking", false, this.chestUp::getValue));
    private final BooleanSetting chest = this.add(new BooleanSetting("Chest", true));
    private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", false, this.chest::getValue));
    private final BooleanSetting ignoreCrawling = this.add(new BooleanSetting("IgnoreCrawling", false, this.chest::getValue));
    private final BooleanSetting legs = this.add(new BooleanSetting("Legs", false));
    private final BooleanSetting legAnchor = this.add(new BooleanSetting("LegAnchor", true));
    private final BooleanSetting down = this.add(new BooleanSetting("Down", false));
    private final BooleanSetting onlyHole = this.add(new BooleanSetting("OnlyHole", false));
    private final BooleanSetting breakCrystal = this.add(new BooleanSetting("Break", true));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting selfGround = this.add(new BooleanSetting("SelfGround", true));
    public PlayerEntity target;
    int progress = 0;

    public AutoTrap() {
        super("AutoTrap", Module.Category.Combat);
        this.setChinese("\u81ea\u52a8\u56f0\u4f4f");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.trapList.clear();
        this.placeList.clear();
        this.progress = 0;
        this.target = null;
        if (this.selfGround.getValue() && !AutoTrap.mc.player.isOnGround()) {
            return;
        }
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.usingPause.getValue() && AutoTrap.mc.player.isUsingItem()) {
            return;
        }
        if (!this.timer.passed((long)this.delay.getValue())) {
            return;
        }
        if (this.targetMod.getValue() == TargetMode.Single) {
            this.target = CombatUtil.getClosestEnemy(this.range.getValue());
            if (this.target == null) {
                if (this.autoDisable.getValue()) {
                    this.disable();
                }
                return;
            }
            this.trapTarget(this.target);
        } else if (this.targetMod.getValue() == TargetMode.Multi) {
            boolean found = false;
            for (PlayerEntity player : CombatUtil.getEnemies(this.range.getValue())) {
                found = true;
                this.target = player;
                this.trapTarget(this.target);
            }
            if (!found) {
                if (this.autoDisable.getValue()) {
                    this.disable();
                }
                this.target = null;
            }
        }
    }

    private void trapTarget(PlayerEntity target) {
        if (this.onlyHole.getValue() && !Alien.HOLE.isHole(EntityUtil.getEntityPos((Entity)target))) {
            return;
        }
        if (this.onlyCrawling.getValue() && !target.isCrawling() && (!this.checkElytra.getValue() || !(((ItemStack)target.getInventory().armor.get(2)).getItem() instanceof ElytraItem) || AutoTrap.mc.player.getY() < target.getY() + 1.0 && !target.isFallFlying())) {
            return;
        }
        Vec3d playerPos = this.predictTicks.getValue() > 0.0 ? PredictUtil.getPos(target, this.predictTicks.getValueInt()) : target.getPos();
        this.doTrap(target, new BlockPosX(playerPos.getX(), playerPos.getY(), playerPos.getZ()));
    }

    private void doTrap(PlayerEntity player, BlockPos pos) {
        BlockPos offsetPos;
        int chestOffset;
        if (pos == null) {
            return;
        }
        if (this.trapList.contains(pos)) {
            return;
        }
        this.trapList.add(pos);
        int headOffset = player.isCrawling() ? 1 : 2;
        chestOffset = player.isCrawling() ? 0 : 1;
        if (this.legs.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos legPos = pos.offset(i);
                this.tryPlaceBlock(legPos, this.legAnchor.getValue(), false, false);
                if (BlockUtil.getPlaceSide(legPos) != null || !BlockUtil.clientCanPlace(legPos, this.breakCrystal.getValue()) || this.getHelper(legPos) == null) continue;
                this.tryPlaceObsidian(this.getHelper(legPos));
            }
        }
        if (this.headExtend.getValue()) {
            int[] xOffsets = new int[]{1, 0, -1};
            for (int x : xOffsets) {
                for (int z : xOffsets) {
                    offsetPos = pos.add(z, 0, x);
                    if (!this.checkEntity(new BlockPos((Vec3i)offsetPos))) continue;
                    this.tryPlaceBlock(offsetPos.up(headOffset), this.headMode.getValue() == Mode.Anchor, this.headMode.getValue() == Mode.Concrete, this.headMode.getValue() == Mode.Web);
                }
            }
        }
        if (this.head.getValue() && BlockUtil.clientCanPlace(pos.up(headOffset), this.breakCrystal.getValue())) {
            if (BlockUtil.getPlaceSide(pos.up(headOffset)) == null) {
                boolean trapChest = this.helper.getValue();
                if (this.getHelper(pos.up(headOffset)) != null) {
                    this.tryPlaceObsidian(this.getHelper(pos.up(headOffset)));
                    trapChest = false;
                }
                if (trapChest) {
                    Direction i;
                    int x;
                    Direction[] directionArray = Direction.values();
                    int n2 = directionArray.length;
                    for (x = 0; x < n2; ++x) {
                        i = directionArray[x];
                        if (i == Direction.DOWN || i == Direction.UP) continue;
                        BlockPos offsetPos3 = pos.offset(i).up(chestOffset);
                        if (!BlockUtil.isStrictDirection(pos.offset(i).up(), i.getOpposite()) || !BlockUtil.clientCanPlace(offsetPos3.up(chestOffset), this.breakCrystal.getValue()) || !BlockUtil.canPlace(offsetPos3, this.placeRange.getValue(), this.breakCrystal.getValue())) continue;
                        this.tryPlaceObsidian(offsetPos3);
                        trapChest = false;
                        break;
                    }
                    if (trapChest) {
                        directionArray = Direction.values();
                        n2 = directionArray.length;
                        for (x = 0; x < n2; ++x) {
                            i = directionArray[x];
                            if (i == Direction.DOWN || i == Direction.UP) continue;
                            BlockPos offsetPos4 = pos.offset(i).up(chestOffset);
                            if (!BlockUtil.isStrictDirection(pos.offset(i).up(), i.getOpposite()) || !BlockUtil.clientCanPlace(offsetPos4.up(chestOffset), this.breakCrystal.getValue()) || BlockUtil.getPlaceSide(offsetPos4) != null || !BlockUtil.clientCanPlace(offsetPos4, this.breakCrystal.getValue()) || this.getHelper(offsetPos4) == null) continue;
                            this.tryPlaceObsidian(this.getHelper(offsetPos4));
                            trapChest = false;
                            break;
                        }
                        if (trapChest) {
                            directionArray = Direction.values();
                            n2 = directionArray.length;
                            for (x = 0; x < n2; ++x) {
                                i = directionArray[x];
                                if (i == Direction.DOWN || i == Direction.UP) continue;
                                BlockPos offsetPos5 = pos.offset(i).up(chestOffset);
                                if (!BlockUtil.isStrictDirection(pos.offset(i).up(), i.getOpposite()) || !BlockUtil.clientCanPlace(offsetPos5.up(chestOffset), this.breakCrystal.getValue()) || BlockUtil.getPlaceSide(offsetPos5) != null || !BlockUtil.clientCanPlace(offsetPos5, this.breakCrystal.getValue()) || this.getHelper(offsetPos5) == null || BlockUtil.getPlaceSide(offsetPos5.down()) != null || !BlockUtil.clientCanPlace(offsetPos5.down(), this.breakCrystal.getValue()) || this.getHelper(offsetPos5.down()) == null) continue;
                                this.tryPlaceObsidian(this.getHelper(offsetPos5.down()));
                                break;
                            }
                        }
                    }
                }
            }
            this.tryPlaceBlock(pos.up(headOffset), this.headMode.getValue() == Mode.Anchor, this.headMode.getValue() == Mode.Concrete, this.headMode.getValue() == Mode.Web);
        }
        if (this.antiStep.getValue() && (Alien.BREAK.isMining(pos.up(headOffset)) || !this.onlyBreak.getValue())) {
            if (BlockUtil.getPlaceSide(pos.up(3)) == null && BlockUtil.clientCanPlace(pos.up(3), this.breakCrystal.getValue()) && this.getHelper(pos.up(3), Direction.DOWN) != null) {
                this.tryPlaceObsidian(this.getHelper(pos.up(3)));
            }
            this.tryPlaceObsidian(pos.up(3));
        }
        if (this.down.getValue()) {
            BlockPos offsetPos6 = pos.down();
            this.tryPlaceObsidian(offsetPos6);
            if (BlockUtil.getPlaceSide(offsetPos6) == null && BlockUtil.clientCanPlace(offsetPos6, this.breakCrystal.getValue()) && this.getHelper(offsetPos6) != null) {
                this.tryPlaceObsidian(this.getHelper(offsetPos6));
            }
        }
        if (this.chestUp.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos chestUpPos = pos.offset(i).up(headOffset);
                if (this.onlyBreaking.getValue() && !Alien.BREAK.isMining(pos.up(headOffset))) continue;
                this.tryPlaceObsidian(chestUpPos);
                if (BlockUtil.getPlaceSide(chestUpPos) != null || !BlockUtil.clientCanPlace(chestUpPos, this.breakCrystal.getValue())) continue;
                if (this.getHelper(chestUpPos) != null) {
                    this.tryPlaceObsidian(this.getHelper(chestUpPos));
                    continue;
                }
                if (BlockUtil.getPlaceSide(chestUpPos.down()) != null || !BlockUtil.clientCanPlace(chestUpPos.down(), this.breakCrystal.getValue()) || this.getHelper(chestUpPos.down()) == null) continue;
                this.tryPlaceObsidian(this.getHelper(chestUpPos.down()));
            }
        }
        if (!(!this.chest.getValue() || this.onlyGround.getValue() && !this.target.isOnGround() || this.ignoreCrawling.getValue() && this.target.isCrawling())) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos chestPos = pos.offset(i).up(chestOffset);
                this.tryPlaceObsidian(chestPos);
                if (BlockUtil.getPlaceSide(chestPos) != null || !BlockUtil.clientCanPlace(chestPos, this.breakCrystal.getValue())) continue;
                if (this.getHelper(chestPos) != null) {
                    this.tryPlaceObsidian(this.getHelper(chestPos));
                    continue;
                }
                if (BlockUtil.getPlaceSide(chestPos.down()) != null || !BlockUtil.clientCanPlace(chestPos.down(), this.breakCrystal.getValue()) || this.getHelper(chestPos.down()) == null) continue;
                this.tryPlaceObsidian(this.getHelper(chestPos.down()));
            }
        }
        if (this.extend.getValue()) {
            for (int x : new int[]{1, 0, -1}) {
                for (int z : new int[]{1, 0, -1}) {
                    offsetPos = pos.add(x, 0, z);
                    if (!this.checkEntity(new BlockPos((Vec3i)offsetPos))) continue;
                    this.doTrap(player, offsetPos);
                }
            }
        }
    }

    @Override
    public String getInfo() {
        if (this.target != null) {
            return this.target.getName().getString();
        }
        return null;
    }

    public BlockPos getHelper(BlockPos pos) {
        if (!this.helper.getValue()) {
            return null;
        }
        for (Direction i : Direction.values()) {
            if (this.checkMine.getValue() && Alien.BREAK.isMining(pos.offset(i)) || !BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite()) || !BlockUtil.canPlace(pos.offset(i), this.placeRange.getValue(), this.breakCrystal.getValue())) continue;
            return pos.offset(i);
        }
        return null;
    }

    public BlockPos getHelper(BlockPos pos, Direction ignore) {
        if (!this.helper.getValue()) {
            return null;
        }
        for (Direction i : Direction.values()) {
            if (i == ignore || this.checkMine.getValue() && Alien.BREAK.isMining(pos.offset(i)) || !BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite()) || !BlockUtil.canPlace(pos.offset(i), this.placeRange.getValue(), this.breakCrystal.getValue())) continue;
            return pos.offset(i);
        }
        return null;
    }

    private boolean checkEntity(BlockPos pos) {
        if (AutoTrap.mc.player.getBoundingBox().intersects(new Box(pos))) {
            return false;
        }
        for (Entity entity : Alien.THREAD.getPlayers()) {
            if (!entity.getBoundingBox().intersects(new Box(pos)) || !entity.isAlive()) continue;
            return true;
        }
        return false;
    }

    private void tryPlaceBlock(BlockPos pos, boolean anchor, boolean sand, boolean web) {
        int block;
        if (this.placeList.contains(pos)) {
            return;
        }
        if (Alien.BREAK.isMining(pos)) {
            return;
        }
        if (!BlockUtil.canPlace(pos, 6.0, this.breakCrystal.getValue())) {
            return;
        }
        if (!((double)this.progress < this.blocksPer.getValue())) {
            return;
        }
        if ((double)MathHelper.sqrt((float)((float)AutoTrap.mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos()))) > this.placeRange.getValue()) {
            return;
        }
        int old = AutoTrap.mc.player.getInventory().selectedSlot;
        if (sand) {
            block = this.getConcrete();
        } else if (web) {
            block = this.getWeb() != -1 ? this.getWeb() : this.getBlock();
        } else {
            block = anchor && this.getAnchor() != -1 ? this.getAnchor() : this.getBlock();
        }
        if (block == -1) {
            return;
        }
        this.placeList.add(pos);
        CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.usingPause.getValue());
        this.doSwap(block);
        BlockUtil.placeBlock(pos, this.rotate.getValue());
        if (this.inventory.getValue()) {
            this.doSwap(block);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(old);
        }
        this.timer.reset();
        ++this.progress;
    }

    private void tryPlaceObsidian(BlockPos pos) {
        if (pos == null) {
            return;
        }
        if (this.placeList.contains(pos)) {
            return;
        }
        if (Alien.BREAK.isMining(pos)) {
            return;
        }
        if (!BlockUtil.canPlace(pos, 6.0, this.breakCrystal.getValue())) {
            return;
        }
        if (!((double)this.progress < this.blocksPer.getValue())) {
            return;
        }
        if ((double)MathHelper.sqrt((float)((float)AutoTrap.mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos()))) > this.placeRange.getValue()) {
            return;
        }
        int old = AutoTrap.mc.player.getInventory().selectedSlot;
        int block = this.getBlock();
        if (block == -1) {
            return;
        }
        BlockUtil.placedPos.add(pos);
        this.placeList.add(pos);
        CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.usingPause.getValue());
        this.doSwap(block);
        BlockUtil.placeBlock(pos, this.rotate.getValue());
        if (this.inventory.getValue()) {
            this.doSwap(block);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(old);
        }
        this.timer.reset();
        ++this.progress;
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, AutoTrap.mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        }
        return InventoryUtil.findBlock(Blocks.OBSIDIAN);
    }

    private int getConcrete() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findClassInventorySlot(ConcretePowderBlock.class);
        }
        return InventoryUtil.findClass(ConcretePowderBlock.class);
    }

    private int getWeb() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.COBWEB);
        }
        return InventoryUtil.findBlock(Blocks.COBWEB);
    }

    private int getAnchor() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR);
        }
        return InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
    }

    public static enum TargetMode {
        Single,
        Multi;

    }

    private static enum Mode {
        Obsidian,
        Anchor,
        Web,
        Concrete;

    }
}

