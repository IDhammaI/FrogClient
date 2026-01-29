/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.combat.CombatUtil;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import dev.idhammai.mod.modules.impl.player.PacketMine;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CevBreaker
extends Module {
    public static CevBreaker INSTANCE;
    private final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 5.0, 0.0, 8.0, 0.1));
    private final SliderSetting breakRange = this.add(new SliderSetting("BreakRange", 5.0, 0.0, 8.0, 0.1));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 100, 0, 500).setSuffix("ms"));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private final BooleanSetting ground = this.add(new BooleanSetting("Ground", true));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting top = this.add(new BooleanSetting("Top", false));
    private final BooleanSetting bevel = this.add(new BooleanSetting("Bevel", true));
    private final Timer timer = new Timer();
    private PlayerEntity target = null;

    public CevBreaker() {
        super("CevBreaker", Module.Category.Combat);
        this.setChinese("\u81ea\u52a8\u70b8\u5934");
        INSTANCE = this;
    }

    public static boolean canPlaceCrystal(BlockPos pos) {
        return CevBreaker.mc.world.isAir(pos) && BlockUtil.noEntityBlockCrystal(pos, false) && BlockUtil.noEntityBlockCrystal(pos.up(), false) && (!ClientSetting.INSTANCE.lowVersion.getValue() || CevBreaker.mc.world.isAir(pos.up()));
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (this.ground.getValue() && !CevBreaker.mc.player.isOnGround()) {
            return;
        }
        PacketMine.INSTANCE.crystal.setValue(true);
        this.target = CombatUtil.getClosestEnemy(this.targetRange.getValue());
        if (this.target != null) {
            BlockPos pos;
            BlockPos targetPos = EntityUtil.getEntityPos((Entity)this.target);
            if (PacketMine.getBreakPos() != null) {
                for (Direction facing : Direction.values()) {
                    if (facing == Direction.DOWN || (facing != Direction.UP ? !this.bevel.getValue() : !this.top.getValue())) continue;
                    pos = targetPos.up(1).offset(facing);
                    if (pos.up().toCenterPos().distanceTo(CevBreaker.mc.player.getPos()) > this.breakRange.getValue() || !PacketMine.getBreakPos().equals((Object)targetPos.up(1).offset(facing))) continue;
                    if (CevBreaker.canPlaceCrystal(targetPos.up(2).offset(facing))) {
                        if (CevBreaker.mc.world.isAir(pos)) {
                            if (!BlockUtil.canPlace(pos)) continue;
                            if (!this.timer.passedMs(this.delay.getValue())) {
                                return;
                            }
                            this.placeBlock(pos);
                            this.timer.reset();
                            return;
                        }
                        if (this.getBlock(pos) != Blocks.OBSIDIAN) continue;
                        PacketMine.INSTANCE.mine(pos);
                        this.timer.reset();
                        return;
                    }
                    if (!BlockUtil.hasCrystal(targetPos.up(2).offset(facing))) continue;
                    if (CevBreaker.mc.world.isAir(pos)) {
                        return;
                    }
                    if (this.getBlock(pos) != Blocks.OBSIDIAN) continue;
                    PacketMine.INSTANCE.mine(pos);
                    this.timer.reset();
                    return;
                }
            }
            for (Direction facing : Direction.values()) {
                if (facing == Direction.DOWN || (facing != Direction.UP ? !this.bevel.getValue() : !this.top.getValue()) || (pos = targetPos.up(1).offset(facing)).up().toCenterPos().distanceTo(CevBreaker.mc.player.getPos()) > this.breakRange.getValue()) continue;
                if (CevBreaker.canPlaceCrystal(targetPos.up(2).offset(facing))) {
                    if (CevBreaker.mc.world.isAir(pos)) {
                        if (!BlockUtil.canPlace(pos)) continue;
                        if (!this.timer.passedMs(this.delay.getValue())) {
                            return;
                        }
                        this.placeBlock(pos);
                        this.timer.reset();
                        break;
                    }
                    if (this.getBlock(pos) != Blocks.OBSIDIAN) continue;
                    PacketMine.INSTANCE.mine(pos);
                    this.timer.reset();
                    break;
                }
                if (!BlockUtil.hasCrystal(targetPos.up(2).offset(facing))) continue;
                if (CevBreaker.mc.world.isAir(pos)) break;
                if (this.getBlock(pos) != Blocks.OBSIDIAN) continue;
                PacketMine.INSTANCE.mine(pos);
                this.timer.reset();
                break;
            }
        }
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, CevBreaker.mc.player.getInventory().selectedSlot);
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

    private void placeBlock(BlockPos pos) {
        int block = this.getBlock();
        if (block == -1) {
            return;
        }
        int oldSlot = CevBreaker.mc.player.getInventory().selectedSlot;
        if (BlockUtil.canPlace(pos)) {
            if (BlockUtil.allowAirPlace()) {
                this.doSwap(block);
                BlockUtil.placedPos.add(pos);
                BlockUtil.airPlace(pos, this.rotate.getValue());
                if (this.inventory.getValue()) {
                    this.doSwap(block);
                    EntityUtil.syncInventory();
                } else {
                    this.doSwap(oldSlot);
                }
                return;
            }
            Direction side = BlockUtil.getPlaceSide(pos);
            if (side == null) {
                return;
            }
            this.doSwap(block);
            BlockUtil.placedPos.add(pos);
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), this.rotate.getValue());
            if (this.inventory.getValue()) {
                this.doSwap(block);
                EntityUtil.syncInventory();
            } else {
                this.doSwap(oldSlot);
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

    private Block getBlock(BlockPos pos) {
        return CevBreaker.mc.world.getBlockState(pos).getBlock();
    }
}

