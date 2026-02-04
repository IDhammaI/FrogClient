/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.InGameHud
 *  net.minecraft.client.render.RenderTickCounter
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.registry.tag.FluidTags
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.Frog;
import dev.idhammai.api.events.impl.PreRender2DEvent;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import dev.idhammai.mod.modules.impl.client.HUD;
import dev.idhammai.mod.modules.impl.render.Crosshair;
import dev.idhammai.mod.modules.impl.render.NoRender;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InGameHud.class})
public abstract class MixinInGameHud {
    @Final
    @Shadow
    private MinecraftClient client;
    @Unique
    final Color minColor = new Color(196, 0, 0);
    @Unique
    final Color maxColor = new Color(0, 227, 0);

    @Inject(method={"renderPortalOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.portal.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderStatusEffectOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderStatusEffectOverlay(CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.potionsIcon.getValue()) {
            info.cancel();
        }
    }


    @Shadow
    private int getHeartRows(int t) {
        return 0;
    }

    @Shadow
    private int getHeartCount(LivingEntity livingEntity) {
        return 0;
    }

    @Shadow
    private PlayerEntity getCameraPlayer() {
        return null;
    }

    @Shadow
    private LivingEntity getRiddenEntity() {
        return null;
    }

    @Shadow
    public abstract void render(DrawContext var1, RenderTickCounter var2);

    @Inject(at={@At(value="HEAD")}, method={"render"})
    public void renderStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Frog.EVENT_BUS.post(PreRender2DEvent.get(context));
    }

    @Inject(at={@At(value="TAIL")}, method={"render"})
    public void renderHook(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Frog.MODULE.onRender2D(context);
    }

    @Inject(method={"clear"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V")}, cancellable=true)
    private void onClear(CallbackInfo info) {
        if (ClientSetting.INSTANCE.isOn() && ClientSetting.INSTANCE.keepHistory.getValue()) {
            info.cancel();
        }
    }

    @ModifyArg(method={"renderHotbar"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal=1), index=1)
    private int selectedSlotX(int x) {
        if (ClientSetting.INSTANCE.hotbar()) {
            double hotbarX = ClientSetting.animation.get(x, ClientSetting.INSTANCE.hotbarTime.getValueInt(), ClientSetting.INSTANCE.animEase.getValue());
            return (int)hotbarX;
        }
        return x;
    }

    @Inject(method={"renderCrosshair"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderCrosshairBegin(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Crosshair.INSTANCE.isOn()) {
            Crosshair.INSTANCE.draw(context);
            ci.cancel();
        }
    }
}

