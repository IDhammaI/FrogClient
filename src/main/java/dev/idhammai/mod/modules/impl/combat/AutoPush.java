/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.FacingBlock
 *  net.minecraft.block.PistonBlock
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.state.property.Property
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.combat.CombatUtil;
import dev.idhammai.api.utils.math.MathUtil;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.api.utils.world.BlockPosX;
import dev.idhammai.api.utils.world.BlockUtil;
import dev.idhammai.core.impl.RotationManager;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import dev.idhammai.mod.modules.impl.exploit.Blink;
import dev.idhammai.mod.modules.impl.player.PacketMine;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoPush
extends Module {
    public static AutoPush INSTANCE;
    private final BooleanSetting torch = this.add(new BooleanSetting("Torch", false));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotation", true));
    private final BooleanSetting yawDeceive = this.add(new BooleanSetting("YawDeceive", true));
    private final BooleanSetting pistonPacket = this.add(new BooleanSetting("PistonPacket", false));
    private final BooleanSetting powerPacket = this.add(new BooleanSetting("PowerPacket", true));
    private final BooleanSetting noEating = this.add(new BooleanSetting("EatingPause", true));
    private final BooleanSetting mine = this.add(new BooleanSetting("Mine", true));
    private final BooleanSetting allowWeb = this.add(new BooleanSetting("AllowWeb", true));
    private final SliderSetting updateDelay = this.add(new SliderSetting("Delay", 100, 0, 1000));
    private final BooleanSetting selfGround = this.add(new BooleanSetting("SelfGround", true));
    private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", false));
    private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 0.0, 6.0));
    private final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 5.0, 0.0, 6.0));
    private final SliderSetting surroundCheck = this.add(new SliderSetting("SurroundCheck", 2, 0, 4));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final Timer timer = new Timer();

    public AutoPush() {
        super("AutoPush", Module.Category.Combat);
        this.setChinese("\u6d3b\u585e\u63a8\u4eba");
        INSTANCE = this;
    }

    public static void pistonFacing(Direction i) {
        if (i == Direction.EAST) {
            Frog.ROTATION.snapAt(-90.0f, 5.0f);
        } else if (i == Direction.WEST) {
            Frog.ROTATION.snapAt(90.0f, 5.0f);
        } else if (i == Direction.NORTH) {
            Frog.ROTATION.snapAt(180.0f, 5.0f);
        } else if (i == Direction.SOUTH) {
            Frog.ROTATION.snapAt(0.0f, 5.0f);
        }
    }

    @Override
    public void onEnable() {
        AutoCrystal.INSTANCE.lastBreakTimer.reset();
    }

    boolean isTargetHere(BlockPos pos, Entity target) {
        return new Box(pos).intersects(target.getBoundingBox());
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!this.timer.passedMs(this.updateDelay.getValue())) {
            return;
        }
        if (this.selfGround.getValue() && !AutoPush.mc.player.isOnGround()) {
            return;
        }
        if (this.findBlock(this.getBlockType()) == -1 || this.findClass(PistonBlock.class) == -1) {
            if (this.autoDisable.getValue()) {
                this.disable();
            }
            return;
        }
        if (this.noEating.getValue() && AutoPush.mc.player.isUsingItem()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        for (PlayerEntity player : CombatUtil.getEnemies(this.range.getValue())) {
            BlockPos pos;
            BlockPosX playerPos;
            float[] offset;
            if (!this.canPush(player).booleanValue()) continue;
            for (float x : offset = new float[]{-0.25f, 0.0f, 0.25f}) {
                for (float z : offset) {
                    playerPos = new BlockPosX(player.getX() + (double)x, player.getY() + 0.5, player.getZ() + (double)z);
                    for (Direction i : Direction.values()) {
                        if (i == Direction.UP || i == Direction.DOWN || !this.isTargetHere(pos = playerPos.offset(i), (Entity)player) || !AutoPush.mc.world.canCollide((Entity)player, new Box(pos))) continue;
                        if (this.tryPush(playerPos.offset(i.getOpposite()), i)) {
                            this.timer.reset();
                            return;
                        }
                        if (!this.tryPush(playerPos.offset(i.getOpposite()).up(), i)) continue;
                        this.timer.reset();
                        return;
                    }
                }
            }
            if (!AutoPush.mc.world.canCollide((Entity)player, new Box((BlockPos)new BlockPosX(player.getX(), player.getY() + 2.5, player.getZ())))) {
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    BlockPos pos2 = EntityUtil.getEntityPos((Entity)player).offset(i);
                    Box box = player.getBoundingBox().offset(new Vec3d((double)i.getOffsetX(), (double)i.getOffsetY(), (double)i.getOffsetZ()));
                    if (this.getBlock(pos2.up()) == Blocks.PISTON_HEAD || AutoPush.mc.world.canCollide((Entity)player, box.offset(0.0, 1.0, 0.0)) || this.isTargetHere(pos2, (Entity)player)) continue;
                    if (this.tryPush(EntityUtil.getEntityPos((Entity)player).offset(i.getOpposite()).up(), i)) {
                        this.timer.reset();
                        return;
                    }
                    if (!this.tryPush(EntityUtil.getEntityPos((Entity)player).offset(i.getOpposite()), i)) continue;
                    this.timer.reset();
                    return;
                }
            }
            for (float x : offset) {
                for (float z : offset) {
                    playerPos = new BlockPosX(player.getX() + (double)x, player.getY() + 0.5, player.getZ() + (double)z);
                    for (Direction i : Direction.values()) {
                        if (i == Direction.UP || i == Direction.DOWN || !this.isTargetHere(pos = playerPos.offset(i), (Entity)player)) continue;
                        if (this.tryPush(playerPos.offset(i.getOpposite()).up(), i)) {
                            this.timer.reset();
                            return;
                        }
                        if (!this.tryPush(playerPos.offset(i.getOpposite()), i)) continue;
                        this.timer.reset();
                        return;
                    }
                }
            }
        }
    }

    /*
     * WARNING - void declaration
     */
    private boolean tryPush(BlockPos piston, Direction direction) {
        BlockState state;
        if (!AutoPush.mc.world.isAir(piston.offset(direction))) {
            return false;
        }
        if (this.isTrueFacing(piston, direction) && this.facingCheck(piston) && BlockUtil.clientCanPlace(piston, false)) {
            boolean canPower = false;
            if (BlockUtil.getPlaceSide(piston, this.placeRange.getValue()) != null) {
                CombatUtil.modifyPos = piston;
                CombatUtil.modifyBlockState = Blocks.PISTON.getDefaultState();
                for (Direction direction2 : Direction.values()) {
                    if (this.getBlock(piston.offset(direction2)) != this.getBlockType()) continue;
                    canPower = true;
                    break;
                }
                Direction[] directionArray = Direction.values();
                int n = directionArray.length;
                for (int i = 0; i < n; ++i) {
                    Direction direction3 = directionArray[i];
                    if (canPower) break;
                    if (!BlockUtil.canPlace(piston.offset(direction3), this.placeRange.getValue())) continue;
                    canPower = true;
                }
                CombatUtil.modifyPos = null;
                if (canPower) {
                    int pistonSlot = this.findClass(PistonBlock.class);
                    Direction side = BlockUtil.getPlaceSide(piston);
                    if (side != null) {
                        if (this.rotate.getValue()) {
                            Frog.ROTATION.lookAt(piston.offset(side), side.getOpposite());
                        }
                        if (this.yawDeceive.getValue()) {
                            AutoPush.pistonFacing(direction.getOpposite());
                        }
                        int old = AutoPush.mc.player.getInventory().selectedSlot;
                        this.doSwap(pistonSlot);
                        BlockUtil.placeBlock(piston, false, this.pistonPacket.getValue());
                        if (this.inventory.getValue()) {
                            this.doSwap(pistonSlot);
                            EntityUtil.syncInventory();
                        } else {
                            this.doSwap(old);
                        }
                        if (this.rotate.getValue() && this.yawDeceive.getValue()) {
                            Frog.ROTATION.lookAt(piston.offset(side), side.getOpposite());
                        }
                        if (this.rotate.getValue()) {
                            Frog.ROTATION.snapBack();
                        }
                        for (Direction i : Direction.values()) {
                            if (this.getBlock(piston.offset(i)) != this.getBlockType()) continue;
                            if (this.mine.getValue()) {
                                PacketMine.INSTANCE.mine(piston.offset(i));
                            }
                            if (this.autoDisable.getValue()) {
                                this.disable();
                            }
                            return true;
                        }
                        for (Direction i : Direction.values()) {
                            if (i == Direction.UP && this.torch.getValue() || !BlockUtil.canPlace(piston.offset(i), this.placeRange.getValue())) continue;
                            int oldSlot = AutoPush.mc.player.getInventory().selectedSlot;
                            int powerSlot = this.findBlock(this.getBlockType());
                            this.doSwap(powerSlot);
                            BlockUtil.placeBlock(piston.offset(i), this.rotate.getValue(), this.powerPacket.getValue());
                            if (this.inventory.getValue()) {
                                this.doSwap(powerSlot);
                                EntityUtil.syncInventory();
                            } else {
                                this.doSwap(oldSlot);
                            }
                            if (this.mine.getValue()) {
                                PacketMine.INSTANCE.mine(piston.offset(i));
                            }
                            return true;
                        }
                        return true;
                    }
                }
            } else {
                Direction powerFacing = null;
                for (Direction i : Direction.values()) {
                    if (i != Direction.UP || !this.torch.getValue()) {
                        if (powerFacing != null) break;
                        CombatUtil.modifyPos = piston.offset(i);
                        CombatUtil.modifyBlockState = this.getBlockType().getDefaultState();
                        if (BlockUtil.getPlaceSide(piston) != null) {
                            powerFacing = i;
                        }
                        CombatUtil.modifyPos = null;
                        if (powerFacing != null && !BlockUtil.canPlace(piston.offset(powerFacing))) {
                            powerFacing = null;
                        }
                    }
                }
                if (powerFacing != null) {
                    int oldSlot = AutoPush.mc.player.getInventory().selectedSlot;
                    int powerSlot = this.findBlock(this.getBlockType());
                    this.doSwap(powerSlot);
                    BlockUtil.placeBlock(piston.offset(powerFacing), this.rotate.getValue(), this.powerPacket.getValue());
                    if (this.inventory.getValue()) {
                        this.doSwap(powerSlot);
                        EntityUtil.syncInventory();
                    } else {
                        this.doSwap(oldSlot);
                    }
                    CombatUtil.modifyPos = piston.offset(powerFacing);
                    CombatUtil.modifyBlockState = this.getBlockType().getDefaultState();
                    int n = this.findClass(PistonBlock.class);
                    Direction side2 = BlockUtil.getPlaceSide(piston);
                    if (side2 != null) {
                        if (this.rotate.getValue()) {
                            Frog.ROTATION.lookAt(piston.offset(side2), side2.getOpposite());
                        }
                        if (this.yawDeceive.getValue()) {
                            AutoPush.pistonFacing(direction.getOpposite());
                        }
                        int old = AutoPush.mc.player.getInventory().selectedSlot;
                        this.doSwap(n);
                        BlockUtil.placeBlock(piston, false, this.pistonPacket.getValue());
                        if (this.inventory.getValue()) {
                            this.doSwap(n);
                            EntityUtil.syncInventory();
                        } else {
                            this.doSwap(old);
                        }
                        if (this.rotate.getValue() && this.yawDeceive.getValue()) {
                            Frog.ROTATION.lookAt(piston.offset(side2), side2.getOpposite());
                        }
                        if (this.rotate.getValue()) {
                            Frog.ROTATION.snapBack();
                        }
                    }
                    CombatUtil.modifyPos = null;
                    return true;
                }
            }
        }
        if ((state = AutoPush.mc.world.getBlockState(piston)).getBlock() instanceof PistonBlock && this.getBlockState(piston).get((Property)FacingBlock.FACING) == direction) {
            for (Direction direction4 : Direction.values()) {
                if (this.getBlock(piston.offset(direction4)) != this.getBlockType()) continue;
                if (this.autoDisable.getValue()) {
                    this.disable();
                    return true;
                }
                return false;
            }
            for (Direction direction5 : Direction.values()) {
                if (direction5 == Direction.UP && this.torch.getValue() || !BlockUtil.canPlace(piston.offset(direction5), this.placeRange.getValue())) continue;
                int oldSlot = AutoPush.mc.player.getInventory().selectedSlot;
                int powerSlot = this.findBlock(this.getBlockType());
                this.doSwap(powerSlot);
                BlockUtil.placeBlock(piston.offset(direction5), this.rotate.getValue(), this.powerPacket.getValue());
                if (this.inventory.getValue()) {
                    this.doSwap(powerSlot);
                    EntityUtil.syncInventory();
                } else {
                    this.doSwap(oldSlot);
                }
                return true;
            }
        }
        return false;
    }

    private boolean facingCheck(BlockPos pos) {
        if (ClientSetting.INSTANCE.lowVersion.getValue()) {
            Direction direction = MathUtil.getDirectionFromEntityLiving(pos, (LivingEntity)AutoPush.mc.player);
            return direction != Direction.UP && direction != Direction.DOWN;
        }
        return true;
    }

    private boolean isTrueFacing(BlockPos pos, Direction facing) {
        if (this.yawDeceive.getValue()) {
            return true;
        }
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) {
            return false;
        }
        Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
        float[] rotation = RotationManager.getRotation(directionVec);
        return MathUtil.getFacingOrder(rotation[0], rotation[1]).getOpposite() == facing;
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, AutoPush.mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    public int findBlock(Block blockIn) {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(blockIn);
        }
        return InventoryUtil.findBlock(blockIn);
    }

    public int findClass(Class<?> clazz) {
        if (this.inventory.getValue()) {
            return InventoryUtil.findClassInventorySlot(clazz);
        }
        return InventoryUtil.findClass(clazz);
    }

    private Boolean canPush(PlayerEntity player) {
        if (this.onlyGround.getValue() && !player.isOnGround()) {
            return false;
        }
        if (!this.allowWeb.getValue() && Frog.PLAYER.isInWeb(player)) {
            return false;
        }
        float[] offset = new float[]{-0.25f, 0.0f, 0.25f};
        int progress = 0;
        if (AutoPush.mc.world.canCollide((Entity)player, new Box((BlockPos)new BlockPosX(player.getX() + 1.0, player.getY() + 0.5, player.getZ())))) {
            ++progress;
        }
        if (AutoPush.mc.world.canCollide((Entity)player, new Box((BlockPos)new BlockPosX(player.getX() - 1.0, player.getY() + 0.5, player.getZ())))) {
            ++progress;
        }
        if (AutoPush.mc.world.canCollide((Entity)player, new Box((BlockPos)new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ() + 1.0)))) {
            ++progress;
        }
        if (AutoPush.mc.world.canCollide((Entity)player, new Box((BlockPos)new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ() - 1.0)))) {
            ++progress;
        }
        for (float x : offset) {
            for (float z : offset) {
                BlockPosX playerPos = new BlockPosX(player.getX() + (double)x, player.getY() + 0.5, player.getZ() + (double)z);
                for (Direction i : Direction.values()) {
                    BlockPos pos;
                    if (i == Direction.UP || i == Direction.DOWN || !this.isTargetHere(pos = playerPos.offset(i), (Entity)player)) continue;
                    if (AutoPush.mc.world.canCollide((Entity)player, new Box(pos))) {
                        return true;
                    }
                    if (!((double)progress > this.surroundCheck.getValue() - 1.0)) continue;
                    return true;
                }
            }
        }
        if (!AutoPush.mc.world.canCollide((Entity)player, new Box((BlockPos)new BlockPosX(player.getX(), player.getY() + 2.5, player.getZ())))) {
            for (Direction i : Direction.values()) {
                if (i == Direction.UP || i == Direction.DOWN) continue;
                BlockPos pos = EntityUtil.getEntityPos((Entity)player).offset(i);
                Box box = player.getBoundingBox().offset(new Vec3d((double)i.getOffsetX(), (double)i.getOffsetY(), (double)i.getOffsetZ()));
                if (this.getBlock(pos.up()) == Blocks.PISTON_HEAD || AutoPush.mc.world.canCollide((Entity)player, box.offset(0.0, 1.0, 0.0)) || this.isTargetHere(pos, (Entity)player) || !AutoPush.mc.world.canCollide((Entity)player, new Box((BlockPos)new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ())))) continue;
                return true;
            }
        }
        return (double)progress > this.surroundCheck.getValue() - 1.0 || Frog.HOLE.isHard(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ()));
    }

    private Block getBlock(BlockPos pos) {
        return AutoPush.mc.world.getBlockState(pos).getBlock();
    }

    private Block getBlockType() {
        if (this.torch.getValue()) {
            return Blocks.REDSTONE_TORCH;
        }
        return Blocks.REDSTONE_BLOCK;
    }

    private BlockState getBlockState(BlockPos pos) {
        return AutoPush.mc.world.getBlockState(pos);
    }
}

