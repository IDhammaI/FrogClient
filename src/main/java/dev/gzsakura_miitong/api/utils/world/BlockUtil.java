/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractPressurePlateBlock
 *  net.minecraft.block.AnvilBlock
 *  net.minecraft.block.BedBlock
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.BlockWithEntity
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.ButtonBlock
 *  net.minecraft.block.CartographyTableBlock
 *  net.minecraft.block.CraftingTableBlock
 *  net.minecraft.block.DoorBlock
 *  net.minecraft.block.FenceGateBlock
 *  net.minecraft.block.GrindstoneBlock
 *  net.minecraft.block.LoomBlock
 *  net.minecraft.block.NoteBlock
 *  net.minecraft.block.StonecutterBlock
 *  net.minecraft.block.TrapdoorBlock
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ExperienceOrbEntity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.decoration.ArmorStandEntity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.entity.projectile.thrown.ExperienceBottleEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.ChunkPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockCollisionSpliterator
 *  net.minecraft.world.BlockView
 *  net.minecraft.world.CollisionView
 *  net.minecraft.world.World
 *  net.minecraft.world.chunk.WorldChunk
 *  org.jetbrains.annotations.Nullable
 */
package dev.gzsakura_miitong.api.utils.world;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.api.utils.player.EntityUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import dev.gzsakura_miitong.mod.modules.impl.combat.AutoCrystal;
import dev.gzsakura_miitong.mod.modules.impl.combat.AutoWeb;
import dev.gzsakura_miitong.mod.modules.impl.player.AirPlace;
import dev.gzsakura_miitong.mod.modules.settings.enums.SwingSide;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class BlockUtil
implements Wrapper {
    public static final List<BlockPos> placedPos = new ArrayList<BlockPos>();
    private static final double MIN_EYE_HEIGHT = 0.4;
    private static final double MAX_EYE_HEIGHT = 1.62;
    private static final double MOVEMENT_THRESHOLD = 2.0E-4;

    public static boolean canPlace(BlockPos pos) {
        return BlockUtil.canPlace(pos, 1000.0);
    }

    public static boolean canPlace(BlockPos pos, double getDistance) {
        if (BlockUtil.getPlaceSide(pos, getDistance) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !BlockUtil.hasEntity(pos, false);
    }

    public static boolean canPlace(BlockPos pos, double getDistance, boolean ignoreCrystal) {
        if (BlockUtil.getPlaceSide(pos, getDistance) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !BlockUtil.hasEntity(pos, ignoreCrystal);
    }

    public static boolean clientCanPlace(BlockPos pos, boolean ignoreCrystal) {
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !BlockUtil.hasEntity(pos, ignoreCrystal);
    }

    public static List<Entity> getEntities(Box box) {
        ArrayList<Entity> list = new ArrayList<Entity>();
        for (Entity entity : Vitality.THREAD.getEntities()) {
            if (entity == null || entity instanceof ArmorStandEntity && AntiCheat.INSTANCE.ignoreArmorStand.getValue() || !entity.getBoundingBox().intersects(box)) continue;
            list.add(entity);
        }
        return list;
    }

    public static List<EndCrystalEntity> getEndCrystals(Box box) {
        ArrayList<EndCrystalEntity> list = new ArrayList<EndCrystalEntity>();
        for (Entity entity : Vitality.THREAD.getEntities()) {
            EndCrystalEntity crystal;
            if (!(entity instanceof EndCrystalEntity) || !(crystal = (EndCrystalEntity)entity).getBoundingBox().intersects(box)) continue;
            list.add(crystal);
        }
        return list;
    }

    public static boolean hasEntity(BlockPos pos, boolean ignoreCrystal) {
        return BlockUtil.hasEntity(new Box(pos), ignoreCrystal);
    }

    public static boolean hasEntity(Box box, boolean ignoreCrystal) {
        for (Entity entity : BlockUtil.getEntities(box)) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || ignoreCrystal && entity instanceof EndCrystalEntity && BlockUtil.mc.player.getEyePos().distanceTo(MathUtil.getClosestPoint(entity)) <= AntiCheat.INSTANCE.ieRange.getValue()) continue;
            return true;
        }
        return false;
    }

    public static boolean hasCrystal(BlockPos pos) {
        for (Entity entity : BlockUtil.getEndCrystals(new Box(pos))) {
            if (!entity.isAlive() || !(entity instanceof EndCrystalEntity)) continue;
            return true;
        }
        return false;
    }

    public static boolean noEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal) {
        return BlockUtil.noEntityBlockCrystal(pos, ignoreCrystal, false);
    }

    public static boolean noEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : BlockUtil.getEntities(new Box(pos))) {
            if (!entity.isAlive() || ignoreItem && entity instanceof ItemEntity || ignoreCrystal && entity instanceof EndCrystalEntity && BlockUtil.mc.player.getEyePos().distanceTo(MathUtil.getClosestPoint(entity)) <= AntiCheat.INSTANCE.ieRange.getValue()) continue;
            return false;
        }
        return true;
    }

    public static boolean canPlaceCrystal(BlockPos pos) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        return !(BlockUtil.getBlock(obsPos) != Blocks.BEDROCK && BlockUtil.getBlock(obsPos) != Blocks.OBSIDIAN || BlockUtil.getClickSideStrict(obsPos) == null || !BlockUtil.mc.world.isAir(boost) || !BlockUtil.noEntityBlockCrystal(boost, false) || !BlockUtil.noEntityBlockCrystal(boost.up(), false) || ClientSetting.INSTANCE.lowVersion.getValue() && !BlockUtil.mc.world.isAir(boost.up()));
    }

    public static void placeCrystal(BlockPos pos, boolean rotate) {
        boolean offhand = BlockUtil.mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        Direction facing = BlockUtil.getClickSide(obsPos);
        Vec3d vec = obsPos.toCenterPos().add((double)facing.getVector().getX() * 0.5, (double)facing.getVector().getY() * 0.5, (double)facing.getVector().getZ() * 0.5);
        if (rotate) {
            Vitality.ROTATION.lookAt(vec);
        }
        BlockUtil.clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
    }

    public static void placeBlock(BlockPos pos, boolean rotate) {
        BlockUtil.placeBlock(pos, rotate, AntiCheat.INSTANCE.packetPlace.getValue());
    }

    public static void placeBlock(BlockPos pos, boolean rotate, boolean packet) {
        if (BlockUtil.allowAirPlace()) {
            placedPos.add(pos);
            BlockUtil.airPlace(pos, rotate, Hand.MAIN_HAND, packet);
            return;
        }
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) {
            return;
        }
        placedPos.add(pos);
        BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate, Hand.MAIN_HAND, packet);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate) {
        BlockUtil.clickBlock(pos, side, rotate, Hand.MAIN_HAND);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand) {
        BlockUtil.clickBlock(pos, side, rotate, hand, AntiCheat.INSTANCE.packetPlace.getValue());
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, boolean packet) {
        BlockUtil.clickBlock(pos, side, rotate, Hand.MAIN_HAND, packet);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, boolean packet) {
        Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
        if (rotate) {
            Vitality.ROTATION.lookAt(directionVec);
        }
        EntityUtil.swingHand(hand, AntiCheat.INSTANCE.interactSwing.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        if (packet) {
            Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        } else {
            BlockUtil.mc.interactionManager.interactBlock(BlockUtil.mc.player, hand, result);
        }
        BlockUtil.mc.itemUseCooldown = 4;
        if (rotate) {
            Alien.ROTATION.snapBack();
        }
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, SwingSide swingSide) {
        Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
        if (rotate) {
            Alien.ROTATION.lookAt(directionVec);
        }
        EntityUtil.swingHand(hand, swingSide);
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        BlockUtil.mc.itemUseCooldown = 4;
        if (rotate) {
            Alien.ROTATION.snapBack();
        }
    }

    public static void airPlace(BlockPos pos, boolean rotate) {
        BlockUtil.airPlace(pos, rotate, Hand.MAIN_HAND, AntiCheat.INSTANCE.packetPlace.getValue());
    }

    public static void airPlace(BlockPos pos, boolean rotate, Hand hand, boolean packet) {
        boolean bypass;
        boolean bl = bypass = hand == Hand.MAIN_HAND && AirPlace.INSTANCE.grimBypass.getValue();
        if (bypass) {
            mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
            hand = Hand.OFF_HAND;
        }
        Direction side = BlockUtil.getClickSide(pos);
        Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
        if (rotate) {
            Alien.ROTATION.lookAt(directionVec);
        }
        EntityUtil.swingHand(hand, AntiCheat.INSTANCE.interactSwing.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        if (packet) {
            Hand finalHand = hand;
            Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(finalHand, result, id));
        } else {
            BlockUtil.mc.interactionManager.interactBlock(BlockUtil.mc.player, hand, result);
        }
        BlockUtil.mc.itemUseCooldown = 4;
        if (rotate) {
            Alien.ROTATION.snapBack();
        }
        if (bypass) {
            mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
        }
    }

    public static double distanceToXZ(double x, double z, double x2, double z2) {
        double dx = x2 - x;
        double dz = z2 - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double distanceToXZ(double x, double z) {
        return BlockUtil.distanceToXZ(x, z, BlockUtil.mc.player.getX(), BlockUtil.mc.player.getZ());
    }

    public static Direction getPlaceSide(BlockPos pos) {
        if (BlockUtil.allowAirPlace()) {
            return BlockUtil.getClickSide(pos);
        }
        double minDistance = Double.MAX_VALUE;
        Direction side = null;
        for (Direction i : Direction.values()) {
            double vecDis;
            if (!BlockUtil.canClick(pos.offset(i)) || BlockUtil.canReplace(pos.offset(i)) || !BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite()) || (vecDis = BlockUtil.mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos().add((double)i.getVector().getX() * 0.5, (double)i.getVector().getY() * 0.5, (double)i.getVector().getZ() * 0.5))) > minDistance) continue;
            side = i;
            minDistance = vecDis;
        }
        return side;
    }

    public static Direction getBestNeighboring(BlockPos pos, Direction facing) {
        Direction bestFacing = null;
        double getDistance = 0.0;
        for (Direction i : Direction.values()) {
            if (facing != null && pos.offset(i).equals((Object)pos.offset(facing, -1)) || i == Direction.DOWN || BlockUtil.getPlaceSide(pos) == null || bestFacing != null && !(BlockUtil.mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()) < getDistance)) continue;
            bestFacing = i;
            getDistance = BlockUtil.mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos());
        }
        return bestFacing;
    }

    public static Direction getPlaceSide(BlockPos pos, double reachDistance) {
        if (BlockUtil.allowAirPlace()) {
            Direction i = BlockUtil.getClickSide(pos);
            double vecDis = BlockUtil.mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos().add((double)i.getVector().getX() * 0.5, (double)i.getVector().getY() * 0.5, (double)i.getVector().getZ() * 0.5));
            if (Math.sqrt(vecDis) > reachDistance) {
                return null;
            }
            return Direction.DOWN;
        }
        double minDistance = Double.MAX_VALUE;
        Direction side = null;
        for (Direction i : Direction.values()) {
            double vecDis;
            if (!BlockUtil.canClick(pos.offset(i)) || BlockUtil.canReplace(pos.offset(i)) || !BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite()) || Math.sqrt(vecDis = BlockUtil.mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos().add((double)i.getVector().getX() * 0.5, (double)i.getVector().getY() * 0.5, (double)i.getVector().getZ() * 0.5))) > reachDistance || vecDis > minDistance) continue;
            side = i;
            minDistance = vecDis;
        }
        return side;
    }

    public static Direction getClickSide(BlockPos pos) {
        Direction side = Direction.UP;
        double minDistance = Double.MAX_VALUE;
        for (Direction i : Direction.values()) {
            double disSq;
            if (!BlockUtil.isStrictDirection(pos, i) || (disSq = BlockUtil.mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > minDistance) continue;
            side = i;
            minDistance = disSq;
        }
        return side;
    }

    public static Direction getClickSideStrict(BlockPos pos) {
        Direction side = null;
        double minDistance = Double.MAX_VALUE;
        for (Direction i : Direction.values()) {
            double disSq;
            if (!BlockUtil.isStrictDirection(pos, i) || (disSq = BlockUtil.mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > minDistance) continue;
            side = i;
            minDistance = disSq;
        }
        return side;
    }

    public static boolean isStrictDirection(BlockPos pos, Direction side, double reachDistance) {
        double vecDis = BlockUtil.mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos().add((double)side.getVector().getX() * 0.5, (double)side.getVector().getY() * 0.5, (double)side.getVector().getZ() * 0.5));
        if (Math.sqrt(vecDis) > reachDistance) {
            return false;
        }
        return BlockUtil.isStrictDirection(pos, side);
    }

    public static boolean isStrictDirection(BlockPos pos, Direction side) {
        switch (AntiCheat.INSTANCE.placement.getValue()) {
            case Vanilla: {
                return true;
            }
            case Legit: {
                return EntityUtil.canSee(pos, side);
            }
            case Grim: {
                return BlockUtil.grimStrictDirectionCheck(pos, side, BlockUtil.mc.world, BlockUtil.mc.player);
            }
            case NCP: {
                if (BlockUtil.mc.world.getBlockState(pos.offset(side)).isFullCube((BlockView)BlockUtil.mc.world, pos.offset(side))) {
                    return false;
                }
                Vec3d eyePos = BlockUtil.mc.player.getEyePos();
                Vec3d blockCenter = pos.toCenterPos();
                ArrayList<Direction> validAxis = new ArrayList<Direction>();
                validAxis.addAll(BlockUtil.checkAxis(eyePos.x - blockCenter.x, Direction.WEST, Direction.EAST, false));
                validAxis.addAll(BlockUtil.checkAxis(eyePos.y - blockCenter.y, Direction.DOWN, Direction.UP, true));
                validAxis.addAll(BlockUtil.checkAxis(eyePos.z - blockCenter.z, Direction.NORTH, Direction.SOUTH, false));
                return validAxis.contains(side);
            }
        }
        return true;
    }

    public static boolean grimStrictDirectionCheck(BlockPos pos, Direction direction, ClientWorld level, ClientPlayerEntity player) {
        boolean bl;
        block10: {
            block9: {
                Box combined = BlockUtil.getCombinedBox(pos, (World)level);
                Box eyePositions = new Box(player.getX(), player.getY() + 0.4, player.getZ(), player.getX(), player.getY() + 1.62, player.getZ()).expand(2.0E-4);
                if (BlockUtil.isIntersected(eyePositions, combined)) {
                    return true;
                }
                switch (direction) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH: {
                        if (!(eyePositions.minZ > combined.minZ)) break;
                        break block9;
                    }
                    case SOUTH: {
                        if (!(eyePositions.maxZ < combined.maxZ)) break;
                        break block9;
                    }
                    case EAST: {
                        if (!(eyePositions.maxX < combined.maxX)) break;
                        break block9;
                    }
                    case WEST: {
                        if (!(eyePositions.minX > combined.minX)) break;
                        break block9;
                    }
                    case UP: {
                        if (!(eyePositions.maxY < combined.maxY)) break;
                        break block9;
                    }
                    case DOWN: {
                        if (eyePositions.minY > combined.minY) break block9;
                    }
                }
                bl = true;
                break block10;
            }
            bl = false;
        }
        return bl;
    }

    private static Box getCombinedBox(BlockPos pos, World level) {
        VoxelShape shape = level.getBlockState(pos).getCollisionShape((BlockView)level, pos).offset((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
        Box combined = new Box(pos);
        for (Box box : shape.getBoundingBoxes()) {
            double minX = Math.max(box.minX, combined.minX);
            double minY = Math.max(box.minY, combined.minY);
            double minZ = Math.max(box.minZ, combined.minZ);
            double maxX = Math.min(box.maxX, combined.maxX);
            double maxY = Math.min(box.maxY, combined.maxY);
            double maxZ = Math.min(box.maxZ, combined.maxZ);
            combined = new Box(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return combined;
    }

    private static boolean isIntersected(Box bb, Box other) {
        return other.maxX - 1.0E-7 > bb.minX && other.minX + 1.0E-7 < bb.maxX && other.maxY - 1.0E-7 > bb.minY && other.minY + 1.0E-7 < bb.maxY && other.maxZ - 1.0E-7 > bb.minZ && other.minZ + 1.0E-7 < bb.maxZ;
    }

    public static ArrayList<Direction> checkAxis(double diff, Direction negativeSide, Direction positiveSide, boolean vertical) {
        ArrayList<Direction> valid = new ArrayList<Direction>();
        if (vertical) {
            if (diff < -0.5) {
                valid.add(negativeSide);
            }
            if (AntiCheat.INSTANCE.upDirectionLimit.getValue()) {
                if (diff > 0.5) {
                    valid.add(positiveSide);
                }
            } else if (diff > -0.5) {
                valid.add(positiveSide);
            }
        } else {
            if (diff < -0.5) {
                valid.add(negativeSide);
            }
            if (diff > 0.5) {
                valid.add(positiveSide);
            }
        }
        return valid;
    }

    public static ArrayList<BlockEntity> getTileEntities() {
        return BlockUtil.getLoadedChunks().flatMap(chunk -> chunk.getBlockEntities().values().stream()).collect(Collectors.toCollection(ArrayList::new));
    }

    public static Stream<WorldChunk> getLoadedChunks() {
        int radius = Math.max(2, BlockUtil.mc.options.getClampedViewDistance()) + 3;
        int diameter = radius * 2 + 1;
        ChunkPos center = BlockUtil.mc.player.getChunkPos();
        ChunkPos min = new ChunkPos(center.x - radius, center.z - radius);
        ChunkPos max = new ChunkPos(center.x + radius, center.z + radius);
        return Stream.iterate(min, pos -> {
            int x = pos.x;
            int z = pos.z;
            if (++x > max.x) {
                x = min.x;
                ++z;
            }
            return new ChunkPos(x, z);
        }).limit((long)diameter * (long)diameter).filter(c -> BlockUtil.mc.world.isChunkLoaded(c.x, c.z)).map(c -> BlockUtil.mc.world.getChunk(c.x, c.z)).filter(Objects::nonNull);
    }

    public static ArrayList<BlockPos> getSphere(float range) {
        return BlockUtil.getSphere(range, BlockUtil.mc.player.getEyePos());
    }

    public static BlockPos getBlock(Block block, float range) {
        for (BlockPos pos : BlockUtil.getSphere(range)) {
            if (BlockUtil.mc.world.getBlockState(pos).getBlock() != block) continue;
            return pos;
        }
        return null;
    }

    public static BlockPos getBlock(Class<?> block, float range) {
        for (BlockPos pos : BlockUtil.getSphere(range)) {
            if (!block.isInstance(BlockUtil.mc.world.getBlockState(pos).getBlock())) continue;
            return pos;
        }
        return null;
    }

    public static ArrayList<BlockPos> getSphere(float range, Vec3d pos) {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        for (double y = pos.getY() + (double)range; y > pos.getY() - (double)range; y -= 1.0) {
            if (y < -64.0) continue;
            for (double x = pos.getX() - (double)range; x < pos.getX() + (double)range; x += 1.0) {
                for (double z = pos.getZ() - (double)range; z < pos.getZ() + (double)range; z += 1.0) {
                    BlockPosX curPos = new BlockPosX(x, y, z);
                    if (curPos.toCenterPos().distanceTo(pos) > (double)range) continue;
                    list.add(curPos);
                }
            }
        }
        return list;
    }

    public static Block getBlock(BlockPos pos) {
        return BlockUtil.mc.world.getBlockState(pos).getBlock();
    }

    public static boolean canReplace(BlockPos pos) {
        if (pos.getY() >= 320) {
            return false;
        }
        if (AntiCheat.INSTANCE.multiPlace.getValue() && placedPos.contains(pos)) {
            return false;
        }
        BlockState state = BlockUtil.mc.world.getBlockState(pos);
        if (state.getBlock() == Blocks.COBWEB && AutoWeb.ignore && AutoCrystal.INSTANCE.replace.getValue()) {
            return true;
        }
        return state.isReplaceable();
    }

    public static boolean canClick(BlockPos pos) {
        if (AntiCheat.INSTANCE.multiPlace.getValue() && placedPos.contains(pos)) {
            return true;
        }
        BlockState state = BlockUtil.mc.world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.COBWEB && AutoWeb.ignore) {
            return AutoCrystal.INSTANCE.airPlace.getValue();
        }
        return BlockUtil.mc.player.isSneaking() || !BlockUtil.isClickable(block);
    }

    public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock || block instanceof AnvilBlock || block instanceof LoomBlock || block instanceof CartographyTableBlock || block instanceof GrindstoneBlock || block instanceof StonecutterBlock || block instanceof ButtonBlock || block instanceof AbstractPressurePlateBlock || block instanceof BlockWithEntity || block instanceof BedBlock || block instanceof FenceGateBlock || block instanceof DoorBlock || block instanceof NoteBlock || block instanceof TrapdoorBlock;
    }

    public static boolean canCollide(Box box) {
        return BlockUtil.canCollide((Entity)BlockUtil.mc.player, box);
    }

    public static boolean canCollide(@Nullable Entity entity, Box box) {
        BlockCollisionSpliterator blockCollisionSpliterator = new BlockCollisionSpliterator((CollisionView)BlockUtil.mc.world, entity, box, false, (pos, voxelShape) -> voxelShape);
        do {
            if (blockCollisionSpliterator.hasNext()) continue;
            return false;
        } while (((VoxelShape)blockCollisionSpliterator.next()).isEmpty());
        return true;
    }

    public static boolean allowAirPlace() {
        return AirPlace.INSTANCE.isOn() && AirPlace.INSTANCE.module.getValue();
    }
}

