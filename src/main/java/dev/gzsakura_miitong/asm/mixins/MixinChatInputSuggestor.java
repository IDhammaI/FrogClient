/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.ChatInputSuggestor
 *  net.minecraft.client.gui.widget.TextFieldWidget
 *  net.minecraft.text.OrderedText
 *  net.minecraft.text.Text
 *  org.apache.commons.lang3.StringUtils
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.gzsakura_miitong.asm.mixins;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.mod.commands.Command;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChatInputSuggestor.class})
public abstract class MixinChatInputSuggestor {
    @Final
    @Shadow
    TextFieldWidget textField;
    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Final
    @Shadow
    private List<OrderedText> messages;
    @Unique
    private boolean showOutline = false;

    @Shadow
    public abstract void show(boolean var1);

    @Inject(at={@At(value="HEAD")}, method={"render"})
    private void onRender(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.showOutline) {
            int x = this.textField.getX() - 3;
            int y = this.textField.getY() - 3;
            Render2DUtil.drawRect(context.getMatrices(), (float)x, (float)y, (float)(this.textField.getWidth() + 1), 1.0f, ClientSetting.INSTANCE.color.getValue().getRGB());
            Render2DUtil.drawRect(context.getMatrices(), (float)x, (float)(y + this.textField.getHeight() + 1), (float)(this.textField.getWidth() + 1), 1.0f, ClientSetting.INSTANCE.color.getValue().getRGB());
            Render2DUtil.drawRect(context.getMatrices(), (float)x, (float)y, 1.0f, (float)(this.textField.getHeight() + 1), ClientSetting.INSTANCE.color.getValue().getRGB());
            Render2DUtil.drawRect(context.getMatrices(), (float)(x + this.textField.getWidth() + 1), (float)y, 1.0f, (float)(this.textField.getHeight() + 2), ClientSetting.INSTANCE.color.getValue().getRGB());
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/widget/TextFieldWidget;getCursor()I", ordinal=0)}, method={"refresh()V"})
    private void onRefresh(CallbackInfo ci) {
        int cursorPos;
        String string2;
        String prefix = Vitality.getPrefix();
        String string = this.textField.getText();
        this.showOutline = string.startsWith(prefix);
        if (string.isEmpty() || !prefix.startsWith(string2 = string.substring(0, cursorPos = this.textField.getCursor())) && !string2.startsWith(prefix)) return;
        int j = 0;
        Matcher matcher = Pattern.compile("(\\s+)").matcher(string2);
        while (matcher.find()) {
            j = matcher.end();
        }
        SuggestionsBuilder builder = new SuggestionsBuilder(string2, j);
        if (string2.length() < prefix.length()) {
            if (!prefix.startsWith(string2)) return;
            builder.suggest(prefix);
        } else {
            if (!string2.startsWith(prefix)) return;
            int count = StringUtils.countMatches((CharSequence)string2, (CharSequence)" ");
            List<String> seperated = Arrays.asList(string2.split(" "));
            if (count == 0) {
                for (Object strObj : Vitality.COMMAND.getCommands().keySet().toArray()) {
                    String str = (String)strObj;
                    builder.suggest(Vitality.getPrefix() + str + " ");
                }
            } else {
                if (seperated.isEmpty()) {
                    return;
                }
                Command c = Vitality.COMMAND.getCommandBySyntax(seperated.getFirst().substring(prefix.length()));
                if (c == null) {
                    this.messages.add(Text.of((String)(" \u00a74no commands found: \u00a7f" + seperated.getFirst().substring(prefix.length()))).asOrderedText());
                    return;
                }
                String[] suggestions = c.getAutocorrect(count, seperated);
                if (suggestions == null || suggestions.length == 0) {
                    return;
                }
                for (String str : suggestions) {
                    builder.suggest(str + " ");
                }
            }
        }
        this.pendingSuggestions = builder.buildFuture();
        this.show(false);
    }
}

