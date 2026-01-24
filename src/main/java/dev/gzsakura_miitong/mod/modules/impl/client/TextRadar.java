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
package dev.gzsakura_miitong.mod.modules.impl.client;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.core.impl.FontManager;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
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
extends Module {
    public static TextRadar INSTANCE;
    private final DecimalFormat df = new DecimalFormat("0.0");
    private final BooleanSetting font = this.add(new BooleanSetting("Font", true));
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final SliderSetting x = this.add(new SliderSetting("X", 0, 0, 1500));
    private final SliderSetting y = this.add(new SliderSetting("Y", 100, 0, 1000));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255)));
    private final ColorSetting friend = this.add(new ColorSetting("Friend").injectBoolean(true));
    private final BooleanSetting doubleBlank = this.add(new BooleanSetting("Double", false));
    private final BooleanSetting health = this.add(new BooleanSetting("Health", true));
    private final BooleanSetting pops = this.add(new BooleanSetting("Pops", true));
    public final BooleanSetting red = this.add(new BooleanSetting("Red", false));
    private final BooleanSetting getDistance = this.add(new BooleanSetting("Distance", true));
    private final BooleanSetting effects = this.add(new BooleanSetting("Effects", true));

    public TextRadar() {
        super("TextRadar", Module.Category.Client);
        this.setChinese("\u6587\u5b57\u96f7\u8fbe");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        int currentY = this.y.getValueInt();
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
            if (this.pops.getValue() && (totemPopped = Alien.POP.getPop(playerEntity)) > 0) {
                stringBuilder.append(blank);
                stringBuilder.append(TextRadar.getPopColor(totemPopped));
                stringBuilder.append("-");
                stringBuilder.append(totemPopped);
            }
            if ((isFriend = Alien.FRIEND.isFriend(playerEntity)) && !this.friend.booleanValue) continue;
            int n2 = color = isFriend ? this.friend.getValue().getRGB() : this.color.getValue().getRGB();
            if (this.font.getValue()) {
                FontManager.ui.drawString(drawContext.getMatrices(), stringBuilder.toString(), (double)this.x.getValueInt(), (double)currentY, color, this.shadow.getValue());
            } else {
                drawContext.drawText(TextRadar.mc.textRenderer, stringBuilder.toString(), this.x.getValueInt(), currentY, color, this.shadow.getValue());
            }
            if (this.font.getValue()) {
                n = (int)FontManager.ui.getFontHeight();
            } else {
                Objects.requireNonNull(TextRadar.mc.textRenderer);
                n = 9;
            }
            currentY += n;
        }
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

