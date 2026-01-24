/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.util.Formatting
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.DeathEvent;
import dev.gzsakura_miitong.api.events.impl.EntitySpawnEvent;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.RemoveEntityEvent;
import dev.gzsakura_miitong.api.events.impl.TotemEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.player.InventoryUtil;
import dev.gzsakura_miitong.core.impl.CommandManager;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;

public class Tips
extends Module {
    public static Tips INSTANCE;
    public final BooleanSetting visualRange = this.add(new BooleanSetting("VisualRange", false).setParent());
    public final BooleanSetting friends = this.add(new BooleanSetting("Friends", false, this.visualRange::isOpen));
    public final BooleanSetting popCounter = this.add(new BooleanSetting("PopCounter", true));
    public final BooleanSetting deathCoords = this.add(new BooleanSetting("DeathCoords", true));
    public final BooleanSetting serverLag = this.add(new BooleanSetting("ServerLag", true));
    public final BooleanSetting lagBack = this.add(new BooleanSetting("LagBack", true));
    public final BooleanSetting potion = this.add(new BooleanSetting("Potion", true).setParent());
    public final BooleanSetting resistanceLevelCheck = this.add(new BooleanSetting("ResistanceLevelCheck", true, this.potion::isOpen));
    private final SliderSetting yOffset = this.add(new SliderSetting("YOffset", 0, -200, 200, this.potion::isOpen));
    final DecimalFormat df = new DecimalFormat("0.0");
    final int color = new Color(190, 0, 0).getRGB();
    private final Timer lagTimer = new Timer();
    private final Timer lagBackTimer = new Timer();
    int turtles = 0;

    public Tips() {
        super("Tips", Module.Category.Misc);
        this.setChinese("\u63d0\u793a");
        INSTANCE = this;
    }

    @EventListener
    public void onAddEntity(EntitySpawnEvent event) {
        if (!this.visualRange.getValue() || !(event.getEntity() instanceof PlayerEntity) || event.getEntity().getDisplayName() == null) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = Alien.FRIEND.isFriend(playerName);
        if (isFriend && !this.friends.getValue() || event.getEntity() == Tips.mc.player) {
            return;
        }
        CommandManager.sendMessageId((isFriend ? String.valueOf(Formatting.AQUA) + playerName : String.valueOf(Formatting.WHITE) + playerName) + "\u00a7f entered your visual range.", event.getEntity().getId() + 777);
        Tips.mc.world.playSound((PlayerEntity)Tips.mc.player, Tips.mc.player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 100.0f, 1.9f);
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        if (!this.visualRange.getValue() || !(event.getEntity() instanceof PlayerEntity) || event.getEntity().getDisplayName() == null) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = Alien.FRIEND.isFriend(playerName);
        if (isFriend && !this.friends.getValue() || event.getEntity() == Tips.mc.player) {
            return;
        }
        CommandManager.sendMessageId((isFriend ? String.valueOf(Formatting.AQUA) + playerName : String.valueOf(Formatting.WHITE) + playerName) + "\u00a7f left your visual range.", event.getEntity().getId() + 777);
        Tips.mc.world.playSound((PlayerEntity)Tips.mc.player, Tips.mc.player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 100.0f, 1.9f);
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.potion.getValue()) {
            this.turtles = InventoryUtil.getPotionCount((StatusEffect)StatusEffects.RESISTANCE.value());
        }
    }

    @EventListener
    public void onPacketEvent(PacketEvent.Receive event) {
        this.lagTimer.reset();
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.lagBackTimer.reset();
        }
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        String text;
        if (this.serverLag.getValue() && this.lagTimer.passedS(1.4)) {
            text = "Server not responding (" + this.df.format((double)this.lagTimer.getMs() / 1000.0) + "s)";
            TextRenderer textRenderer = Tips.mc.textRenderer;
            int n = mc.getWindow().getScaledWidth() / 2 - Tips.mc.textRenderer.getWidth(text) / 2;
            Objects.requireNonNull(Tips.mc.textRenderer);
            drawContext.drawText(textRenderer, text, n, 10 + 9, this.color, true);
        }
        if (this.lagBack.getValue() && !this.lagBackTimer.passedS(1.5)) {
            text = "Lagback (" + this.df.format((double)(1500L - this.lagBackTimer.getMs()) / 1000.0) + "s)";
            TextRenderer textRenderer = Tips.mc.textRenderer;
            int n = mc.getWindow().getScaledWidth() / 2 - Tips.mc.textRenderer.getWidth(text) / 2;
            Objects.requireNonNull(Tips.mc.textRenderer);
            drawContext.drawText(textRenderer, text, n, 10 + 9 * 2, this.color, true);
        }
        if (this.potion.getValue()) {
            StringBuilder stringBuilder = new StringBuilder();
            if (this.turtles > 0) {
                stringBuilder.append("\u00a7e").append(this.turtles);
            }
            if (Tips.mc.player.hasStatusEffect(StatusEffects.RESISTANCE) && (!this.resistanceLevelCheck.getValue() || Tips.mc.player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() > 0)) {
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("\u00a79").append(Tips.mc.player.getStatusEffect(StatusEffects.RESISTANCE).getDuration() / 20 + 1);
            }
            if (Tips.mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("\u00a74").append(Tips.mc.player.getStatusEffect(StatusEffects.STRENGTH).getDuration() / 20 + 1);
            }
            if (Tips.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("\u00a7b").append(Tips.mc.player.getStatusEffect(StatusEffects.SPEED).getDuration() / 20 + 1);
            }
            TextRenderer textRenderer = Tips.mc.textRenderer;
            String string = stringBuilder.toString();
            int n = mc.getWindow().getScaledWidth() / 2 - Tips.mc.textRenderer.getWidth(stringBuilder.toString()) / 2;
            int n2 = mc.getWindow().getScaledHeight() / 2;
            Objects.requireNonNull(Tips.mc.textRenderer);
            drawContext.drawText(textRenderer, string, n, n2 + 9 - this.yOffset.getValueInt(), -1, true);
        }
    }

    @EventListener
    public void onPlayerDeath(DeathEvent event) {
        PlayerEntity player = event.getPlayer();
        if (this.popCounter.getValue()) {
            if (Alien.POP.popContainer.containsKey(player.getName().getString())) {
                int l_Count = Alien.POP.popContainer.get(player.getName().getString());
                if (l_Count == 1) {
                    if (player.equals((Object)Tips.mc.player)) {
                        this.sendMessage("\u00a7fYou\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totem.", player.getId());
                    } else {
                        this.sendMessage("\u00a7f" + player.getName().getString() + "\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totem.", player.getId());
                    }
                } else if (player.equals((Object)Tips.mc.player)) {
                    this.sendMessage("\u00a7fYou\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totems.", player.getId());
                } else {
                    this.sendMessage("\u00a7f" + player.getName().getString() + "\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totems.", player.getId());
                }
            } else if (player.equals((Object)Tips.mc.player)) {
                this.sendMessage("\u00a7fYou\u00a7r died.", player.getId());
            } else {
                this.sendMessage("\u00a7f" + player.getName().getString() + "\u00a7r died.", player.getId());
            }
        }
        if (this.deathCoords.getValue() && player == Tips.mc.player) {
            this.sendMessage("\u00a74You died at " + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ());
        }
    }

    @EventListener
    public void onTotem(TotemEvent event) {
        if (this.popCounter.getValue()) {
            PlayerEntity player = event.getPlayer();
            int l_Count = 1;
            if (Alien.POP.popContainer.containsKey(player.getName().getString())) {
                l_Count = Alien.POP.popContainer.get(player.getName().getString());
            }
            if (l_Count == 1) {
                if (player.equals((Object)Tips.mc.player)) {
                    this.sendMessage("\u00a7fYou\u00a7r popped \u00a7f" + l_Count + "\u00a7r totem.", player.getId());
                } else {
                    this.sendMessage("\u00a7f" + player.getName().getString() + " \u00a7rpopped \u00a7f" + l_Count + "\u00a7r totems.", player.getId());
                }
            } else if (player.equals((Object)Tips.mc.player)) {
                this.sendMessage("\u00a7fYou\u00a7r popped \u00a7f" + l_Count + "\u00a7r totem.", player.getId());
            } else {
                this.sendMessage("\u00a7f" + player.getName().getString() + " \u00a7rhas popped \u00a7f" + l_Count + "\u00a7r totems.", player.getId());
            }
        }
    }

    public void sendMessage(String message, int id) {
        if (!Tips.nullCheck()) {
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
                CommandManager.sendMessageId("\u00a7f[\u00a73" + this.getName() + "\u00a7f] " + message, id);
                return;
            }
            CommandManager.sendMessageId(message, id);
        }
    }
}

