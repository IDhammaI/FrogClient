/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.util.shape.VoxelShapes
 *  net.minecraft.world.BlockView
 */
package dev.idhammai.mod.modules.impl.render;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.AmbientOcclusionEvent;
import dev.idhammai.api.events.impl.ChunkOcclusionEvent;
import dev.idhammai.api.events.impl.RenderBlockEntityEvent;
import dev.idhammai.mod.gui.windows.WindowsScreen;
import dev.idhammai.mod.gui.windows.impl.ItemSelectWindow;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class Xray
extends Module {
    public static Xray INSTANCE;
    public final BooleanSetting edit = this.add(new BooleanSetting("Edit", false).injectTask(this::openGui));
    private static final ThreadLocal<BlockPos.Mutable> EXPOSED_POS;

    public Xray() {
        super("Xray", Module.Category.Render);
        this.setChinese("\u77ff\u7269\u900f\u89c6");
        INSTANCE = this;
    }

    private void openGui() {
        this.edit.setValueWithoutTask(false);
        if (!Xray.nullCheck()) {
            mc.setScreen((Screen)new WindowsScreen(new ItemSelectWindow(Frog.XRAY)));
        }
    }

    @Override
    public void onEnable() {
        if (Xray.nullCheck()) {
            return;
        }
        Xray.mc.worldRenderer.reload();
    }

    @Override
    public void onDisable() {
        Xray.mc.worldRenderer.reload();
    }

    public boolean isBlocked(Block block) {
        return !Frog.XRAY.inWhitelist(block.getTranslationKey());
    }

    @EventListener
    private void onRenderBlockEntity(RenderBlockEntityEvent event) {
        if (this.isBlocked(event.blockEntity.getCachedState().getBlock())) {
            event.cancel();
        }
    }

    @EventListener
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventListener
    private void onAmbientOcclusion(AmbientOcclusionEvent event) {
        event.lightLevel = 1.0f;
    }

    public static boolean shouldBlock(BlockState state) {
        return INSTANCE.isOn() && INSTANCE.isBlocked(state.getBlock());
    }

    public boolean modifyDrawSide(BlockState state, BlockView view, BlockPos pos, Direction facing, boolean returns) {
        if (!returns && !this.isBlocked(state.getBlock())) {
            BlockPos adjPos = pos.offset(facing);
            BlockState adjState = view.getBlockState(adjPos);
            return adjState.getCullingFace(view, adjPos, facing.getOpposite()) != VoxelShapes.fullCube() || adjState.getBlock() != state.getBlock() || Xray.isExposed(adjPos);
        }
        return returns;
    }

    public static boolean isExposed(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (Xray.mc.world.getBlockState((BlockPos)EXPOSED_POS.get().set((Vec3i)blockPos, direction)).isOpaque()) continue;
            return true;
        }
        return false;
    }

    static {
        EXPOSED_POS = ThreadLocal.withInitial(BlockPos.Mutable::new);
    }
}

