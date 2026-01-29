/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.ChatHud
 *  net.minecraft.client.gui.hud.ChatHudLine
 *  net.minecraft.client.gui.hud.ChatHudLine$Visible
 *  net.minecraft.client.gui.hud.MessageIndicator
 *  net.minecraft.network.message.MessageSignatureData
 *  net.minecraft.text.OrderedText
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.idhammai.asm.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import dev.idhammai.api.interfaces.IChatHudHook;
import dev.idhammai.api.interfaces.IChatHudLineHook;
import dev.idhammai.api.utils.math.FadeUtils;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.asm.accessors.IChatHud;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChatHud.class})
public abstract class MixinChatHud
implements IChatHudHook {
    @Unique
    private int nextMessageId = 0;
    @Unique
    private boolean nextSync;
    @Unique
    private int chatLineIndex;
    @Final
    @Shadow
    private List<ChatHudLine.Visible> visibleMessages;
    @Shadow
    @Final
    private List<ChatHudLine> messages;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    public void onInit(MinecraftClient client, CallbackInfo ci) {
        ((IChatHud)((Object)this)).setMessages(new CopyOnWriteArrayList<ChatHudLine>());
        ((IChatHud)((Object)this)).setVisibleMessages(new CopyOnWriteArrayList<ChatHudLine.Visible>());
    }

    @Override
    public void frogClient$addMessage(Text message, int id) {
        this.nextMessageId = id;
        this.nextSync = true;
        this.addMessage(message);
        this.nextSync = false;
        this.nextMessageId = 0;
    }

    @Override
    public void frogClient$addMessage(Text message) {
        this.nextSync = true;
        this.addMessage(message);
        this.nextSync = false;
    }

    @Override
    public void frogClient$addMessageOutSync(Text message, int id) {
        this.nextMessageId = id;
        this.addMessage(message);
        this.nextMessageId = 0;
    }

    @Inject(method={"addVisibleMessage"}, at={@At(value="INVOKE", target="Ljava/util/List;add(ILjava/lang/Object;)V", ordinal=0, shift=At.Shift.AFTER)})
    private void onAddMessageAfterNewChatHudLineVisible(ChatHudLine message, CallbackInfo ci) {
        IChatHudLineHook line = (IChatHudLineHook)(Object)this.visibleMessages.getFirst();
        if (line != null) {
            line.frogClient$setMessageId(this.nextMessageId);
            line.frogClient$setSync(this.nextSync);
            line.frogClient$setFade(new FadeUtils(ClientSetting.INSTANCE.animationTime.getValueInt()));
        }
    }

    @Inject(method={"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"}, at={@At(value="INVOKE", target="Ljava/util/List;add(ILjava/lang/Object;)V", ordinal=0, shift=At.Shift.AFTER)})
    private void onAddMessageAfterNewChatHudLine(ChatHudLine message, CallbackInfo ci) {
        IChatHudLineHook line = (IChatHudLineHook)(Object)this.messages.getFirst();
        if (line != null) {
            line.frogClient$setMessageId(this.nextMessageId);
            line.frogClient$setSync(this.nextSync);
            line.frogClient$setFade(new FadeUtils(ClientSetting.INSTANCE.animationTime.getValueInt()));
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"})
    private void onAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        if (this.nextMessageId != 0) {
            this.visibleMessages.removeIf(msg -> ((IChatHudLineHook)(Object)msg).frogClient$getMessageId() == this.nextMessageId);
            this.messages.removeIf(msg -> ((IChatHudLineHook)(Object)msg).frogClient$getMessageId() == this.nextMessageId);
        }
    }

    @Redirect(method={"addVisibleMessage"}, at=@At(value="INVOKE", target="Ljava/util/List;size()I", ordinal=2, remap=false), require=0)
    public int chatLinesSize(List<ChatHudLine.Visible> list) {
        return ClientSetting.INSTANCE.isOn() && ClientSetting.INSTANCE.infiniteChat.getValue() ? -2147483647 : list.size();
    }

    @Redirect(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"), require=0)
    private int drawStringWithShadow(DrawContext drawContext, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
        IChatHudLineHook line = (IChatHudLineHook)(Object)this.visibleMessages.get(this.chatLineIndex);
        if (line != null) {
            FadeUtils fadeUtils = line.frogClient$getFade();
            double ease = fadeUtils == null ? 0.0 : fadeUtils.ease(ClientSetting.INSTANCE.ease.getValue());
            double fade = 1.0 - ease;
            x += (int)(fade * ClientSetting.INSTANCE.animateOffset.getValue());
            double c = Math.max(10.0, (double)(color >> 24 & 0xFF) * ease);
            if (line.frogClient$getSync()) {
                return drawContext.drawTextWithShadow(textRenderer, text, x, y, ColorUtil.injectAlpha(ClientSetting.INSTANCE.color.getValue(), ClientSetting.INSTANCE.fade.getValue() ? (int)c : color >> 24 & 0xFF).getRGB());
            }
            return drawContext.drawTextWithShadow(textRenderer, text, x, y, ColorUtil.injectAlpha(color, ClientSetting.INSTANCE.fade.getValue() ? (int)c : color >> 24 & 0xFF));
        }
        return drawContext.drawTextWithShadow(textRenderer, text, x, y, color);
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I")})
    public void getChatLineIndex(CallbackInfo ci, @Local(ordinal=13) int chatLineIndex) {
        this.chatLineIndex = chatLineIndex;
    }

    @ModifyVariable(method={"render"}, at=@At(value="STORE"))
    private MessageIndicator removeMessageIndicator(MessageIndicator messageIndicator) {
        if (ClientSetting.INSTANCE.hideIndicator.getValue()) {
            return null;
        }
        return messageIndicator;
    }

    @Shadow
    public abstract void addMessage(Text var1);

    @Shadow
    public int getWidth() {
        return 0;
    }
}

