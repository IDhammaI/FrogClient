/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.modules.impl.client;

import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.movement.MovementSync;
import dev.idhammai.mod.modules.settings.enums.Placement;
import dev.idhammai.mod.modules.settings.enums.SnapBack;
import dev.idhammai.mod.modules.settings.enums.SwingSide;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;

public class AntiCheat
extends Module {
    public static AntiCheat INSTANCE;
    public final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
    public final BooleanSetting attackCDFix = this.add(new BooleanSetting("TrueAttackCD", false, () -> this.page.is(Page.General)));
    public final BooleanSetting multiPlace = this.add(new BooleanSetting("MultiPlace", true, () -> this.page.is(Page.General)));
    public final BooleanSetting packetPlace = this.add(new BooleanSetting("PacketPlace", true, () -> this.page.is(Page.General)));
    public final BooleanSetting attackRotate = this.add(new BooleanSetting("AttackRotation", false, () -> this.page.is(Page.General)));
    public final BooleanSetting invSwapBypass = this.add(new BooleanSetting("PickSwap", false, () -> this.page.is(Page.General)));
    public final BooleanSetting priorHotbar = this.add(new BooleanSetting("PriorHotbar", false, () -> this.page.is(Page.General)));
    public final SliderSetting ieRange = this.add(new SliderSetting("InteractEntityRange", 3.0, 0.0, 8.0, 0.1, () -> this.page.is(Page.General)));
    public final SliderSetting boxSize = this.add(new SliderSetting("HitBoxSize", 0.6, 0.0, 1.0, 0.01, () -> this.page.is(Page.General)));
    public final SliderSetting attackDelay = this.add(new SliderSetting("BreakDelay", 0.2, 0.0, 1.0, 0.01, () -> this.page.is(Page.General)).setSuffix("s"));
    public final BooleanSetting noBadSlot = this.add(new BooleanSetting("NoBadSlot", false, () -> this.page.is(Page.General)));
    public final EnumSetting<Placement> placement = this.add(new EnumSetting<Placement>("Placement", Placement.Vanilla, () -> this.page.is(Page.General)));
    public final BooleanSetting upDirectionLimit = this.add(new BooleanSetting("UPDirectionLimit", true, () -> this.page.is(Page.General) && this.placement.is(Placement.NCP)));
    public final EnumSetting<SwingSide> interactSwing = this.add(new EnumSetting<SwingSide>("InteractSwing", SwingSide.All, () -> this.page.is(Page.General)));
    public final EnumSetting<SwingSide> attackSwing = this.add(new EnumSetting<SwingSide>("AttackSwing", SwingSide.All, () -> this.page.is(Page.General)));
    public final BooleanSetting grimRotation = this.add(new BooleanSetting("GrimRotation", false, () -> this.page.is(Page.Rotation)));
    public final EnumSetting<SnapBack> snapBackEnum = this.add(new EnumSetting<SnapBack>("SnapBack", SnapBack.None, () -> this.page.is(Page.Rotation)));
    public final BooleanSetting look = this.add(new BooleanSetting("Look", true, () -> this.page.is(Page.Rotation)));
    public final SliderSetting rotateTime = this.add(new SliderSetting("LookTime", 0.5, 0.0, 1.0, 0.01, () -> this.page.is(Page.Rotation)));
    public final BooleanSetting random = this.add(new BooleanSetting("Random", true, () -> this.page.is(Page.Rotation)));
    public final SliderSetting steps = this.add(new SliderSetting("Steps", 0.6, 0.0, 1.0, 0.01, () -> this.page.is(Page.Rotation)));
    public final BooleanSetting serverSide = this.add(new BooleanSetting("ServerSide", false, () -> this.page.is(Page.Rotation)));
    public final BooleanSetting fullPackets = this.add(new BooleanSetting("FullPackets", false, () -> this.page.is(Page.Rotation)).setParent());
    public final BooleanSetting force = this.add(new BooleanSetting("AlwaysSend", false, () -> this.page.is(Page.Rotation) && this.fullPackets.isOpen()));
    public final BooleanSetting forceSync = this.add(new BooleanSetting("ForceSync", true, () -> this.page.is(Page.Rotation)));
    public final BooleanSetting interactRotation = this.add(new BooleanSetting("InteractRotation", false, () -> this.page.is(Page.Rotation)));
    public final BooleanSetting detectDouble = this.add(new BooleanSetting("DetectDouble", true, () -> this.page.is(Page.Misc)));
    public final SliderSetting doubleMineTimeout = this.add(new SliderSetting("DoubleTimeout", 2.0, 0.0, 3.0, 0.1, () -> this.page.is(Page.Misc)).setSuffix("*"));
    public final SliderSetting minTimeout = this.add(new SliderSetting("MinTimeout", 2.0, 0.0, 10.0, 0.1, () -> this.page.is(Page.Misc)).setSuffix("s"));
    public final SliderSetting breakTimeout = this.add(new SliderSetting("BreakFailed", 1.5, 0.0, 3.0, 0.1, () -> this.page.is(Page.Misc)).setSuffix("*"));
    public final BooleanSetting ignoreArmorStand = this.add(new BooleanSetting("IgnoreArmorStand", false, () -> this.page.is(Page.Misc)));
    public final BooleanSetting closeScreen = this.add(new BooleanSetting("CloseScreen", false, () -> this.page.is(Page.Misc)));
    public final EnumSetting<Motion> motion = this.add(new EnumSetting<Motion>("Motion", Motion.Position, () -> this.page.getValue() == Page.Predict));
    public final SliderSetting predictTicks = this.add(new SliderSetting("Predict", 4, 0, 10, () -> this.page.getValue() == Page.Predict).setSuffix("ticks"));
    public final SliderSetting simulation = this.add(new SliderSetting("Simulation", 5.0, 0.0, 20.0, 1.0, () -> this.page.getValue() == Page.Predict));
    public final SliderSetting maxMotionY = this.add(new SliderSetting("MaxMotionY", 0.34, 0.0, 2.0, 0.01, () -> this.page.getValue() == Page.Predict));
    public final BooleanSetting step = this.add(new BooleanSetting("Step", false, () -> this.page.getValue() == Page.Predict));
    public final BooleanSetting doubleStep = this.add(new BooleanSetting("DoubleStep", false, () -> this.page.getValue() == Page.Predict));
    public final BooleanSetting jump = this.add(new BooleanSetting("Jump", false, () -> this.page.getValue() == Page.Predict));
    public final BooleanSetting inBlockPause = this.add(new BooleanSetting("InBlockPause", true, () -> this.page.getValue() == Page.Predict));

    public AntiCheat() {
        super("AntiCheat", Module.Category.Client);
        this.setChinese("\u53cd\u4f5c\u5f0a\u9009\u9879");
        INSTANCE = this;
    }

    public boolean movementSync() {
        return MovementSync.INSTANCE.isOn();
    }

    public static double getOffset() {
        if (INSTANCE != null) {
            return AntiCheat.INSTANCE.boxSize.getValue() / 2.0;
        }
        return 0.3;
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

    public static enum Page {
        General,
        Rotation,
        Misc,
        Predict;

    }

    public static enum Motion {
        Velocity,
        Position;

    }
}

