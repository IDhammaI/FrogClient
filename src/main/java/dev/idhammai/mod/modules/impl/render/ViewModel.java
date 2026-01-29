/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.RotationAxis
 */
package dev.idhammai.mod.modules.impl.render;

import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.HeldItemRendererEvent;
import dev.idhammai.asm.accessors.IHeldItemRenderer;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

public class ViewModel
extends Module {
    public static ViewModel INSTANCE;
    public final BooleanSetting mainhandSwap = this.add(new BooleanSetting("MainhandSwap", true));
    public final BooleanSetting offhandSwap = this.add(new BooleanSetting("OffhandSwap", true));
    public final SliderSetting scaleMainX = this.add(new SliderSetting("ScaleMainX", 1.0, (double)0.1f, 5.0, 0.01));
    public final SliderSetting scaleMainY = this.add(new SliderSetting("ScaleMainY", 1.0, (double)0.1f, 5.0, 0.01));
    public final SliderSetting scaleMainZ = this.add(new SliderSetting("ScaleMainZ", 1.0, (double)0.1f, 5.0, 0.01));
    public final SliderSetting positionMainX = this.add(new SliderSetting("PositionMainX", 0.0, -3.0, 3.0, 0.01));
    public final SliderSetting positionMainY = this.add(new SliderSetting("PositionMainY", 0.0, -3.0, 3.0, 0.01));
    public final SliderSetting positionMainZ = this.add(new SliderSetting("PositionMainZ", 0.0, -3.0, 3.0, 0.01));
    public final SliderSetting rotationMainX = this.add(new SliderSetting("RotationMainX", 0.0, -180.0, 180.0, 0.01));
    public final SliderSetting rotationMainY = this.add(new SliderSetting("RotationMainY", 0.0, -180.0, 180.0, 0.01));
    public final SliderSetting rotationMainZ = this.add(new SliderSetting("RotationMainZ", 0.0, -180.0, 180.0, 0.01));
    public final SliderSetting scaleOffX = this.add(new SliderSetting("ScaleOffX", 1.0, (double)0.1f, 5.0, 0.01));
    public final SliderSetting scaleOffY = this.add(new SliderSetting("ScaleOffY", 1.0, (double)0.1f, 5.0, 0.01));
    public final SliderSetting scaleOffZ = this.add(new SliderSetting("ScaleOffZ", 1.0, (double)0.1f, 5.0, 0.01));
    public final SliderSetting positionOffX = this.add(new SliderSetting("PositionOffX", 0.0, -3.0, 3.0, 0.01));
    public final SliderSetting positionOffY = this.add(new SliderSetting("PositionOffY", 0.0, -3.0, 3.0, 0.01));
    public final SliderSetting positionOffZ = this.add(new SliderSetting("PositionOffZ", 0.0, -3.0, 3.0, 0.01));
    public final SliderSetting rotationOffX = this.add(new SliderSetting("RotationOffX", 0.0, -180.0, 180.0, 0.01));
    public final SliderSetting rotationOffY = this.add(new SliderSetting("RotationOffY", 0.0, -180.0, 180.0, 0.01));
    public final SliderSetting rotationOffZ = this.add(new SliderSetting("RotationOffZ", 0.0, -180.0, 180.0, 0.01));
    public final BooleanSetting slowAnimation = this.add(new BooleanSetting("SwingSpeed", true));
    public final SliderSetting slowAnimationVal = this.add(new SliderSetting("Value", 6, 1, 50));

    public ViewModel() {
        super("ViewModel", Module.Category.Render);
        this.setChinese("\u624b\u6301\u6a21\u578b");
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (!this.mainhandSwap.getValue() && ((IHeldItemRenderer)mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressMainHand() <= 1.0f) {
            ((IHeldItemRenderer)mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressMainHand(1.0f);
            ((IHeldItemRenderer)mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackMainHand(ViewModel.mc.player.getMainHandStack());
        }
        if (!this.offhandSwap.getValue() && ((IHeldItemRenderer)mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressOffHand() <= 1.0f) {
            ((IHeldItemRenderer)mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressOffHand(1.0f);
            ((IHeldItemRenderer)mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackOffHand(ViewModel.mc.player.getOffHandStack());
        }
    }

    @EventListener
    private void onHeldItemRender(HeldItemRendererEvent event) {
        if (event.getHand() == Hand.MAIN_HAND) {
            event.getStack().translate(this.positionMainX.getValueFloat(), this.positionMainY.getValueFloat(), this.positionMainZ.getValueFloat());
            event.getStack().scale(this.scaleMainX.getValueFloat(), this.scaleMainY.getValueFloat(), this.scaleMainZ.getValueFloat());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.rotationMainX.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotationMainY.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotationMainZ.getValueFloat()));
        } else {
            event.getStack().translate(this.positionOffX.getValueFloat(), this.positionOffY.getValueFloat(), this.positionOffZ.getValueFloat());
            event.getStack().scale(this.scaleOffX.getValueFloat(), this.scaleOffY.getValueFloat(), this.scaleOffZ.getValueFloat());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.rotationOffX.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotationOffY.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotationOffZ.getValueFloat()));
        }
    }
}

