/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.hud.ChatHudLine$Visible
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.api.interfaces.IChatHudLineHook;
import dev.idhammai.api.utils.math.FadeUtils;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={ChatHudLine.Visible.class})
public class MixinChatHudLineVisible
implements IChatHudLineHook {
    @Unique
    private int id = 0;
    @Unique
    private boolean sync = false;
    @Unique
    private FadeUtils fade;

    @Override
    public int frogClient$getMessageId() {
        return this.id;
    }

    @Override
    public void frogClient$setMessageId(int id) {
        this.id = id;
    }

    @Override
    public boolean frogClient$getSync() {
        return this.sync;
    }

    @Override
    public void frogClient$setSync(boolean sync) {
        this.sync = sync;
    }

    @Override
    public FadeUtils frogClient$getFade() {
        return this.fade;
    }

    @Override
    public void frogClient$setFade(FadeUtils fade) {
        this.fade = fade;
    }
}

