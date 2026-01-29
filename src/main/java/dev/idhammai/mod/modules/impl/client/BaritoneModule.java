/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  baritone.api.BaritoneAPI
 *  baritone.api.IBaritone
 *  baritone.api.pathing.calc.IPathingControlManager
 *  baritone.api.pathing.goals.Goal
 *  baritone.api.pathing.goals.GoalBlock
 *  baritone.api.pathing.goals.GoalXZ
 *  baritone.api.process.IBaritoneProcess
 *  baritone.api.process.ICustomGoalProcess
 *  net.minecraft.block.Block
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package dev.idhammai.mod.modules.impl.client;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.calc.IPathingControlManager;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.ICustomGoalProcess;
import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BaritoneModule
extends Module {
    public static BaritoneModule INSTANCE;
    private final SliderSetting rangeConfig = this.add(new SliderSetting("Range", 4.0, 1.0, 5.0));
    private final BooleanSetting placeConfig = this.add(new BooleanSetting("Place", true));
    private final BooleanSetting breakConfig = this.add(new BooleanSetting("Break", true));
    private final BooleanSetting sprintConfig = this.add(new BooleanSetting("Sprint", true));
    private final BooleanSetting inventoryConfig = this.add(new BooleanSetting("UseInventory", false));
    private final BooleanSetting vinesConfig = this.add(new BooleanSetting("Vines", true));
    private final BooleanSetting jump256Config = this.add(new BooleanSetting("JumpAt256", false));
    private final BooleanSetting waterBucketFallConfig = this.add(new BooleanSetting("WaterBucketFall", false));
    private final BooleanSetting parkourConfig = this.add(new BooleanSetting("Parkour", true));
    private final BooleanSetting parkourPlaceConfig = this.add(new BooleanSetting("ParkourPlace", false));
    private final BooleanSetting parkourAscendConfig = this.add(new BooleanSetting("ParkourAscend", true));
    private final BooleanSetting diagonalAscendConfig = this.add(new BooleanSetting("DiagonalAscend", false));
    private final BooleanSetting diagonalDescendConfig = this.add(new BooleanSetting("DiagonalDescend", false));
    private final BooleanSetting mineDownConfig = this.add(new BooleanSetting("MineDownward", true));
    private final BooleanSetting legitMineConfig = this.add(new BooleanSetting("LegitMine", false));
    private final BooleanSetting logOnArrivalConfig = this.add(new BooleanSetting("LogOnArrival", false));
    private final BooleanSetting freeLookConfig = this.add(new BooleanSetting("FreeLook", true));
    private final BooleanSetting antiCheatConfig = this.add(new BooleanSetting("AntiCheat", true));
    private final BooleanSetting strictLiquidConfig = this.add(new BooleanSetting("Strict-Liquid", false));
    private final BooleanSetting censorCoordsConfig = this.add(new BooleanSetting("CensorCoords", false));
    private final BooleanSetting censorCommandsConfig = this.add(new BooleanSetting("CensorCommands", false));
    private final BooleanSetting chatControl = this.add(new BooleanSetting("ChatControl", false));
    private final BooleanSetting debugConfig = this.add(new BooleanSetting("Debug", false));

    public BaritoneModule() {
        super("Baritone", Module.Category.Client);
        Frog.EVENT_BUS.subscribe(this);
        INSTANCE = this;
        this.setChinese("\u5bfb\u8def\u8bbe\u7f6e");
    }

    public static void forward() {
        ICustomGoalProcess customGoalProcess;
        Direction direction = BaritoneModule.mc.player.getHorizontalFacing();
        int x = BaritoneModule.mc.player.getBlockX() + direction.getVector().getX() * 30000000;
        int z = BaritoneModule.mc.player.getBlockZ() + direction.getVector().getZ() * 30000000;
        BaritoneModule.cancelEverything();
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (baritone != null && (customGoalProcess = baritone.getCustomGoalProcess()) != null) {
            customGoalProcess.setGoalAndPath((Goal)new GoalXZ(x, z));
        }
    }

    public static boolean isPathing() {
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        return baritone != null && baritone.getPathingBehavior() != null && baritone.getPathingBehavior().isPathing();
    }

    public static void gotoPos(BlockPos pos) {
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (baritone != null) {
            ICustomGoalProcess customGoalProcess = baritone.getCustomGoalProcess();
            if (customGoalProcess == null) {
                return;
            }
            customGoalProcess.setGoalAndPath((Goal)new GoalBlock(pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    public static void mine(Block block) {
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (baritone != null) {
            baritone.getMineProcess().mine(new Block[]{block});
        }
    }

    public static void cancelEverything() {
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (baritone != null && baritone.getPathingBehavior() != null) {
            baritone.getPathingBehavior().cancelEverything();
        }
    }

    public static boolean isActive() {
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (baritone != null) {
            ICustomGoalProcess customGoalProcess = baritone.getCustomGoalProcess();
            if (customGoalProcess != null && customGoalProcess.isActive()) {
                return true;
            }
            IPathingControlManager controlManager = baritone.getPathingControlManager();
            if (controlManager != null && controlManager.mostRecentInControl().isPresent()) {
                return ((IBaritoneProcess)controlManager.mostRecentInControl().get()).isActive();
            }
        }
        return false;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        BaritoneAPI.getSettings().blockReachDistance.value = Float.valueOf(this.rangeConfig.getValueFloat());
        BaritoneAPI.getSettings().allowPlace.value = this.placeConfig.getValue();
        BaritoneAPI.getSettings().allowBreak.value = this.breakConfig.getValue();
        BaritoneAPI.getSettings().allowSprint.value = this.sprintConfig.getValue();
        BaritoneAPI.getSettings().allowInventory.value = this.inventoryConfig.getValue();
        BaritoneAPI.getSettings().allowVines.value = this.vinesConfig.getValue();
        BaritoneAPI.getSettings().allowJumpAt256.value = this.jump256Config.getValue();
        BaritoneAPI.getSettings().allowWaterBucketFall.value = this.waterBucketFallConfig.getValue();
        BaritoneAPI.getSettings().allowParkour.value = this.parkourConfig.getValue();
        BaritoneAPI.getSettings().allowParkourAscend.value = this.parkourAscendConfig.getValue();
        BaritoneAPI.getSettings().allowParkourPlace.value = this.parkourPlaceConfig.getValue();
        BaritoneAPI.getSettings().allowDiagonalAscend.value = this.diagonalAscendConfig.getValue();
        BaritoneAPI.getSettings().allowDiagonalDescend.value = this.diagonalDescendConfig.getValue();
        BaritoneAPI.getSettings().allowDownward.value = this.mineDownConfig.getValue();
        BaritoneAPI.getSettings().legitMine.value = this.legitMineConfig.getValue();
        BaritoneAPI.getSettings().disconnectOnArrival.value = this.logOnArrivalConfig.getValue();
        BaritoneAPI.getSettings().freeLook.value = this.freeLookConfig.getValue();
        BaritoneAPI.getSettings().antiCheatCompatibility.value = this.antiCheatConfig.getValue();
        BaritoneAPI.getSettings().strictLiquidCheck.value = this.strictLiquidConfig.getValue();
        BaritoneAPI.getSettings().censorCoordinates.value = this.censorCoordsConfig.getValue();
        BaritoneAPI.getSettings().censorRanCommands.value = this.censorCommandsConfig.getValue();
        BaritoneAPI.getSettings().chatControl.value = this.chatControl.getValue();
        BaritoneAPI.getSettings().chatDebug.value = this.debugConfig.getValue();
    }

    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }
}
