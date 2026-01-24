/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 */
package dev.gzsakura_miitong.mod.modules.impl.combat;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.combat.CombatUtil;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.player.EntityUtil;
import dev.gzsakura_miitong.api.utils.player.InventoryUtil;
import dev.gzsakura_miitong.api.utils.world.BlockPosX;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import dev.gzsakura_miitong.mod.modules.impl.exploit.Blink;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class Blocker
extends Module {
    public static Blocker INSTANCE;
    private final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
    final List<BlockPos> placePos = new ArrayList<BlockPos>();
    final List<BlockPos> blockerPos = new ArrayList<BlockPos>();
    final List<BlockPos> list = new ArrayList<BlockPos>();
    private final Timer timer = new Timer();
    private final SliderSetting delay = this.add(new SliderSetting("PlaceDelay", 50, 0, 500, () -> this.page.getValue() == Page.General));
    private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 1, 1, 8, () -> this.page.getValue() == Page.General));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting breakCrystal = this.add(new BooleanSetting("Break", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting inventorySwap = this.add(new BooleanSetting("InventorySwap", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting bevelCev = this.add(new BooleanSetting("BevelCev", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting burrow = this.add(new BooleanSetting("Burrow", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting face = this.add(new BooleanSetting("Face", true, () -> this.page.getValue() == Page.Target).setParent());
    private final BooleanSetting faceUp = this.add(new BooleanSetting("FaceUp", false, () -> this.page.getValue() == Page.Target && this.face.isOpen()));
    private final BooleanSetting feet = this.add(new BooleanSetting("Feet", true, () -> this.page.getValue() == Page.Target).setParent());
    private final BooleanSetting extend = this.add(new BooleanSetting("Extend", false, () -> this.page.getValue() == Page.Target && this.feet.isOpen()));
    private final BooleanSetting onlySurround = this.add(new BooleanSetting("OnlySurround", true, () -> this.page.getValue() == Page.Target && this.feet.isOpen()));
    private final BooleanSetting inAirPause = this.add(new BooleanSetting("InAirPause", false, () -> this.page.getValue() == Page.Check));
    private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", true, () -> this.page.getValue() == Page.Check));
    private final BooleanSetting eatingPause = this.add(new BooleanSetting("EatingPause", true, () -> this.page.getValue() == Page.Check));
    private int placeProgress = 0;
    private BlockPos playerBP;

    public Blocker() {
        super("Blocker", Module.Category.Combat);
        this.setChinese("\u6c34\u6676\u963b\u6321");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        BlockPos blockerPos;
        this.list.clear();
        if (this.inventorySwap.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (!this.timer.passedMs(this.delay.getValue())) {
            return;
        }
        if (this.eatingPause.getValue() && Blocker.mc.player.isUsingItem()) {
            return;
        }
        this.placeProgress = 0;
        if (this.playerBP != null && !this.playerBP.equals((Object)EntityUtil.getPlayerPos(true))) {
            this.placePos.clear();
            this.blockerPos.clear();
        }
        this.playerBP = EntityUtil.getPlayerPos(true);
        double[] offset = new double[]{AntiCheat.getOffset(), -AntiCheat.getOffset(), 0.0};
        if (this.bevelCev.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || this.isBedrock(this.playerBP.offset(i).up()) || !this.crystalHere(blockerPos = this.playerBP.offset(i).up(2)) || this.placePos.contains(blockerPos)) continue;
                this.placePos.add(blockerPos);
            }
        }
        if (this.face.getValue() && (!this.onlySurround.getValue() || Surround.INSTANCE.isOn() || SelfTrap.INSTANCE.isOn())) {
            for (double x : offset) {
                for (double z : offset) {
                    for (Direction i : Direction.values()) {
                        for (int d = 0; d < 3; ++d) {
                            BlockPos aroundPos = new BlockPosX(Blocker.mc.player.getX() + x, Blocker.mc.player.getY() + 0.5, Blocker.mc.player.getZ() + z).offset(i, 1).up();
                            BlockPos blockerPos2 = new BlockPosX(Blocker.mc.player.getX() + x, Blocker.mc.player.getY() + 0.5, Blocker.mc.player.getZ() + z).offset(i, d).up();
                            if (!this.crystalHere(blockerPos2) || this.placePos.contains(blockerPos2) || Alien.HOLE.isHard(aroundPos)) continue;
                            this.placePos.add(blockerPos2);
                        }
                    }
                }
            }
            if (this.faceUp.getValue()) {
                for (Direction i : Direction.values()) {
                    if (i == Direction.DOWN || this.isBedrock(this.playerBP.offset(i).up()) || !this.crystalHere(blockerPos = this.playerBP.offset(i).up(2)) || this.placePos.contains(blockerPos)) continue;
                    this.placePos.add(blockerPos);
                }
            }
        }
        if (this.getObsidian() == -1) {
            return;
        }
        this.placePos.removeIf(pos -> !BlockUtil.clientCanPlace(pos, true));
        if (this.burrow.getValue()) {
            for (double x : offset) {
                for (double z : offset) {
                    BlockPosX surroundPos = new BlockPosX(Blocker.mc.player.getX() + x, Blocker.mc.player.getY(), Blocker.mc.player.getZ() + z);
                    if (this.isBedrock(surroundPos) || !Alien.BREAK.isMining(surroundPos)) continue;
                    Direction[] directionArray = Direction.values();
                    int n = directionArray.length;
                    for (int i = 0; i < n; ++i) {
                        Direction direction = directionArray[i];
                        if (direction == Direction.DOWN || direction == Direction.UP) continue;
                        BlockPos defensePos = surroundPos.offset(direction);
                        if (this.detectMining.getValue() && Alien.BREAK.isMining(defensePos)) continue;
                        if (this.breakCrystal.getValue()) {
                            CombatUtil.attackCrystal(defensePos, this.rotate.getValue(), false);
                        }
                        if (!BlockUtil.canPlace(defensePos, 6.0, this.breakCrystal.getValue())) continue;
                        this.blockerPos.add(defensePos);
                    }
                }
            }
        }
        if (this.feet.getValue() && (!this.onlySurround.getValue() || Surround.INSTANCE.isOn() || SelfTrap.INSTANCE.isOn())) {
            for (double x : offset) {
                for (double z : offset) {
                    for (Direction i : Direction.values()) {
                        BlockPos surroundPos = new BlockPosX(Blocker.mc.player.getX() + x, Blocker.mc.player.getY() + 0.5, Blocker.mc.player.getZ() + z).offset(i);
                        if (this.isBedrock(surroundPos) || !Alien.BREAK.isMining(surroundPos)) continue;
                        for (Direction direction : Direction.values()) {
                            BlockPos defensePos = surroundPos.offset(direction);
                            if (this.detectMining.getValue() && Alien.BREAK.isMining(defensePos)) continue;
                            if (this.breakCrystal.getValue()) {
                                CombatUtil.attackCrystal(defensePos, this.rotate.getValue(), false);
                            }
                            if (BlockUtil.canPlace(defensePos, 6.0, this.breakCrystal.getValue())) {
                                this.blockerPos.add(defensePos);
                                continue;
                            }
                            if (!BlockUtil.canReplace(defensePos) || BlockUtil.hasEntity(defensePos, true) || this.getHelper(defensePos) == null) continue;
                            this.blockerPos.add(this.getHelper(defensePos));
                        }
                    }
                }
            }
        }
        if (this.feet.getValue() && this.extend.getValue() && (!this.onlySurround.getValue() || Surround.INSTANCE.isOn() || SelfTrap.INSTANCE.isOn())) {
            for (double x : offset) {
                for (double z : offset) {
                    for (Direction i : Direction.values()) {
                        BlockPos surroundPos;
                        if (i == Direction.UP || i == Direction.DOWN || this.isBedrock(surroundPos = new BlockPosX(Blocker.mc.player.getX() + x, Blocker.mc.player.getY() + 0.5, Blocker.mc.player.getZ() + z).offset(i))) continue;
                        for (Direction direction : Direction.values()) {
                            BlockPos blockPos;
                            if (direction == Direction.UP || direction == Direction.DOWN || !AutoCrystal.INSTANCE.canPlaceCrystal(blockPos = surroundPos.offset(direction), true, true) || this.detectMining.getValue() && Alien.BREAK.isMining(blockPos)) continue;
                            if (this.breakCrystal.getValue()) {
                                CombatUtil.attackCrystal(blockPos, this.rotate.getValue(), false);
                            }
                            if (!BlockUtil.canPlace(blockPos, 6.0, this.breakCrystal.getValue())) continue;
                            this.blockerPos.add(blockPos);
                        }
                    }
                }
            }
        }
        this.blockerPos.removeIf(pos -> !BlockUtil.clientCanPlace(pos, true));
        if (this.inAirPause.getValue() && !Blocker.mc.player.isOnGround()) {
            return;
        }
        if (this.blockerPos.isEmpty()) {
            return;
        }
        int n = Blocker.mc.player.getInventory().selectedSlot;
        int block = this.getObsidian();
        if (block == -1) {
            return;
        }
        this.doSwap(block);
        for (BlockPos defensePos : this.blockerPos) {
            if (!BlockUtil.canPlace(defensePos, 6.0, this.breakCrystal.getValue())) continue;
            this.doPlace(defensePos);
        }
        if (this.inventorySwap.getValue()) {
            this.doSwap(block);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(n);
        }
    }

    public BlockPos getHelper(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || this.detectMining.getValue() && Alien.BREAK.isMining(pos.offset(i)) || !BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite()) || !BlockUtil.canPlace(pos.offset(i), 6.0)) continue;
            return pos.offset(i);
        }
        return null;
    }

    private boolean crystalHere(BlockPos pos) {
        return BlockUtil.getEndCrystals(new Box(pos)).stream().anyMatch(entity -> entity.getBlockPos().equals((Object)pos));
    }

    private boolean isBedrock(BlockPos pos) {
        return Blocker.mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
    }

    private void doPlace(BlockPos pos) {
        if (this.list.contains(pos)) {
            return;
        }
        this.list.add(pos);
        if (!((double)this.placeProgress < this.blocksPer.getValue())) {
            return;
        }
        BlockUtil.placeBlock(pos, this.rotate.getValue());
        this.timer.reset();
        ++this.placeProgress;
    }

    private void tryPlaceObsidian(BlockPos pos) {
        if (this.list.contains(pos)) {
            return;
        }
        this.list.add(pos);
        if (!((double)this.placeProgress < this.blocksPer.getValue())) {
            return;
        }
        if (this.detectMining.getValue() && Alien.BREAK.isMining(pos)) {
            return;
        }
        int oldSlot = Blocker.mc.player.getInventory().selectedSlot;
        int block = this.getObsidian();
        if (block == -1) {
            return;
        }
        this.doSwap(block);
        BlockUtil.placeBlock(pos, this.rotate.getValue());
        this.timer.reset();
        if (this.inventorySwap.getValue()) {
            this.doSwap(block);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(oldSlot);
        }
        ++this.placeProgress;
    }

    private void doSwap(int slot) {
        if (this.inventorySwap.getValue()) {
            InventoryUtil.inventorySwap(slot, Blocker.mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getObsidian() {
        if (this.inventorySwap.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        }
        return InventoryUtil.findBlock(Blocks.OBSIDIAN);
    }

    public static enum Page {
        General,
        Target,
        Check;

    }
}

