/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.Mouse
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.Frog;
import dev.idhammai.api.interfaces.IMouseHook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Mouse.class})
public class MixinMouse
implements IMouseHook {
    @Shadow
    private boolean cursorLocked;
    @Final
    @Shadow
    private MinecraftClient client;
    @Shadow
    private double x;
    @Shadow
    private double y;

    @Inject(method={"onMouseButton"}, at={@At(value="HEAD")})
    private void onMouse(long window, int button, int action, int mods, CallbackInfo ci) {
        int key = -(button + 2);
        if (action == 1) {
            Frog.MODULE.onKeyPressed(key);
        }
        if (action == 0) {
            Frog.MODULE.onKeyReleased(key);
        }
    }

    @Override
    public void alienClient$lock() {
        if (this.client.isWindowFocused() && !this.cursorLocked) {
            if (!MinecraftClient.IS_SYSTEM_MAC) {
                KeyBinding.updatePressedStates();
            }
            this.cursorLocked = true;
            this.x = (double)this.client.getWindow().getWidth() / 2.0;
            this.y = (double)this.client.getWindow().getHeight() / 2.0;
            InputUtil.setCursorParameters((long)this.client.getWindow().getHandle(), (int)212995, (double)this.x, (double)this.y);
        }
    }
}

