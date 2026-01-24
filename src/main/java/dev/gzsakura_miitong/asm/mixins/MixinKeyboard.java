/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Keyboard
 *  net.minecraft.client.util.NarratorManager
 *  net.minecraft.util.Formatting
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.gzsakura_miitong.asm.mixins;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.core.impl.CommandManager;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Keyboard.class})
public class MixinKeyboard
implements Wrapper {
    @Inject(method={"onKey"}, at={@At(value="HEAD")})
    private void onKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        block4: {
            try {
                if (action == 1) {
                    Alien.MODULE.onKeyPressed(key);
                }
                if (action == 0) {
                    Alien.MODULE.onKeyReleased(key);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block4;
                CommandManager.sendMessage(String.valueOf(Formatting.DARK_RED) + "[ERROR] onKey " + e.getMessage());
            }
        }
    }

    @Redirect(method={"onKey"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/util/NarratorManager;isActive()Z"), require=0)
    public boolean hook(NarratorManager instance) {
        return false;
    }
}

