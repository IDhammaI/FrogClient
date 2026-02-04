/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.Formatting
 */
package dev.idhammai.mod.modules.impl.client;

import dev.idhammai.Frog;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

public class TextRadar
extends HudModule {
    public static TextRadar INSTANCE;
    private final DecimalFormat df = new DecimalFormat("0.0");
    private final BooleanSetting font = this.add(new BooleanSetting("Font", true));
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255)));
    private final ColorSetting friend = this.add(new ColorSetting("Friend").injectBoolean(true));
    private final BooleanSetting doubleBlank = this.add(new BooleanSetting("Double", false));
    private final BooleanSetting health = this.add(new BooleanSetting("Health", true));
    private final BooleanSetting pops = this.add(new BooleanSetting("Pops", true));
    public final BooleanSetting red = this.add(new BooleanSetting("Red", false));
    private final BooleanSetting getDistance = this.add(new BooleanSetting("Distance", true));
    private final BooleanSetting effects = this.add(new BooleanSetting("Effects", true));

    public TextRadar() {
        super("TextRadar", "\u6587\u5b57\u96f7\u8fbe", 0, 100);
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        int startX = this.getHudX();
        int startY = this.getHudY();
        int currentY = startY;
        int maxW = 0;
        int lines = 0;
        ArrayList<AbstractClientPlayerEntity> players = new ArrayList<AbstractClientPlayerEntity>(TextRadar.mc.world.getPlayers());
        players.sort(Comparator.comparingDouble(player -> TextRadar.mc.player.distanceTo((Entity)player)));
        for (PlayerEntity playerEntity : players) {
            int n;
            int color;
            boolean isFriend;
            int totemPopped;
            String blank;
            if (playerEntity == TextRadar.mc.player) continue;
            StringBuilder stringBuilder = new StringBuilder();
            String string = blank = this.doubleBlank.getValue() ? "  " : " ";
            if (this.health.getValue()) {
                stringBuilder.append(TextRadar.getHealthColor(playerEntity));
                stringBuilder.append(this.df.format(playerEntity.getHealth() + playerEntity.getAbsorptionAmount()));
                stringBuilder.append(blank);
            }
            stringBuilder.append(Formatting.RESET);
            stringBuilder.append(playerEntity.getName().getString());
            if (this.getDistance.getValue()) {
                stringBuilder.append(blank);
                stringBuilder.append(Formatting.WHITE);
                stringBuilder.append(this.df.format(TextRadar.mc.player.distanceTo((Entity)playerEntity)));
                stringBuilder.append("m");
            }
            if (this.effects.getValue()) {
                if (playerEntity.hasStatusEffect(StatusEffects.SLOWNESS)) {
                    stringBuilder.append(blank);
                    stringBuilder.append(Formatting.GRAY);
                    stringBuilder.append("Lv.");
                    stringBuilder.append(playerEntity.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1);
                    stringBuilder.append(blank);
                    stringBuilder.append(playerEntity.getStatusEffect(StatusEffects.SLOWNESS).getDuration() / 20 + 1);
                    stringBuilder.append("s");
                }
                if (playerEntity.hasStatusEffect(StatusEffects.SPEED)) {
                    stringBuilder.append(blank);
                    stringBuilder.append(Formatting.AQUA);
                    stringBuilder.append("Lv.");
                    stringBuilder.append(playerEntity.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1);
                    stringBuilder.append(blank);
                    stringBuilder.append(playerEntity.getStatusEffect(StatusEffects.SPEED).getDuration() / 20 + 1);
                    stringBuilder.append("s");
                }
                if (playerEntity.hasStatusEffect(StatusEffects.STRENGTH)) {
                    stringBuilder.append(blank);
                    stringBuilder.append(Formatting.DARK_RED);
                    stringBuilder.append("Lv.");
                    stringBuilder.append(playerEntity.getStatusEffect(StatusEffects.STRENGTH).getAmplifier() + 1);
                    stringBuilder.append(blank);
                    stringBuilder.append(playerEntity.getStatusEffect(StatusEffects.STRENGTH).getDuration() / 20 + 1);
                    stringBuilder.append("s");
                }
                if (playerEntity.hasStatusEffect(StatusEffects.RESISTANCE)) {
                    stringBuilder.append(blank);
                    stringBuilder.append(Formatting.BLUE);
                    stringBuilder.append("Lv.");
                    stringBuilder.append(playerEntity.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1);
                    stringBuilder.append(blank);
                    stringBuilder.append(playerEntity.getStatusEffect(StatusEffects.RESISTANCE).getDuration() / 20 + 1);
                    stringBuilder.append("s");
                }
            }
            if (this.pops.getValue() && (totemPopped = Frog.POP.getPop(playerEntity)) > 0) {
                stringBuilder.append(blank);
                stringBuilder.append(TextRadar.getPopColor(totemPopped));
                stringBuilder.append("-");
                stringBuilder.append(totemPopped);
            }
            if ((isFriend = Frog.FRIEND.isFriend(playerEntity)) && !this.friend.booleanValue) continue;
            int n2 = color = isFriend ? this.friend.getValue().getRGB() : this.color.getValue().getRGB();
            String s = stringBuilder.toString();
            int w = this.font.getValue() ? (int)FontManager.ui.getWidth(s) : TextRadar.mc.textRenderer.getWidth(s);
            maxW = Math.max(maxW, w);
            if (this.font.getValue()) {
                FontManager.ui.drawString(drawContext.getMatrices(), s, (double)startX, (double)currentY, color, this.shadow.getValue());
            } else {
                drawContext.drawText(TextRadar.mc.textRenderer, s, startX, currentY, color, this.shadow.getValue());
            }
            if (this.font.getValue()) {
                n = (int)FontManager.ui.getFontHeight();
            } else {
                Objects.requireNonNull(TextRadar.mc.textRenderer);
                n = 9;
            }
            currentY += n;
            ++lines;
        }
        if (lines <= 0) {
            this.clearHudBounds();
            return;
        }
        this.setHudBounds(startX, startY, Math.max(1, maxW), Math.max(1, currentY - startY));
    }

    public static Formatting getHealthColor(PlayerEntity player) {
        double health = player.getHealth() + player.getAbsorptionAmount();
        if (health > 18.0) {
            return Formatting.GREEN;
        }
        if (health > 16.0) {
            return Formatting.DARK_GREEN;
        }
        if (health > 12.0) {
            return Formatting.YELLOW;
        }
        if (health > 8.0) {
            return Formatting.GOLD;
        }
        if (health > 4.0) {
            return Formatting.RED;
        }
        return Formatting.DARK_RED;
    }

    public static Formatting getPopColor(int totems) {
        if (TextRadar.INSTANCE.red.getValue()) {
            return Formatting.RED;
        }
        if (totems > 10) {
            return Formatting.DARK_RED;
        }
        if (totems > 8) {
            return Formatting.RED;
        }
        if (totems > 6) {
            return Formatting.GOLD;
        }
        if (totems > 4) {
            return Formatting.YELLOW;
        }
        if (totems > 2) {
            return Formatting.DARK_GREEN;
        }
        return Formatting.GREEN;
    }
}

