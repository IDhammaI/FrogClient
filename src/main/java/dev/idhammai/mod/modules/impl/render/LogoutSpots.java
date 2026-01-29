/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket$Action
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket$Entry
 *  net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.GameMode
 */
package dev.idhammai.mod.modules.impl.render;

import com.google.common.collect.Maps;
import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.PacketEvent;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.render.ModelPlayer;
import dev.idhammai.api.utils.render.Render3DUtil;
import dev.idhammai.asm.accessors.IEntity;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.TextRadar;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public class LogoutSpots
extends Module {
    private final ColorSetting box = this.add(new ColorSetting("Box", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting text = this.add(new ColorSetting("Text", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting chamsFill = this.add(new ColorSetting("ChamsFill", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting chamsLine = this.add(new ColorSetting("ChamsLine", new Color(255, 255, 255, 100)).injectBoolean(true));
    final Map<UUID, PlayerEntity> playerCache = Maps.newConcurrentMap();
    final Map<UUID, ModelPlayer> logoutCache = Maps.newConcurrentMap();
    private final BooleanSetting health = this.add(new BooleanSetting("Health", true));
    private final BooleanSetting totem = this.add(new BooleanSetting("Totem", true));
    private final BooleanSetting message = this.add(new BooleanSetting("Message", true));

    public LogoutSpots() {
        super("LogoutSpots", Module.Category.Render);
        this.setChinese("\u9000\u51fa\u8bb0\u5f55");
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (LogoutSpots.nullCheck()) {
            return;
        }
        Object object = event.getPacket();
        if (object instanceof PlayerListS2CPacket) {
            PlayerListS2CPacket packet = (PlayerListS2CPacket)object;
            if (packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                for (PlayerListS2CPacket.Entry addedPlayer : packet.getPlayerAdditionEntries()) {
                    if (addedPlayer.gameMode() == GameMode.SPECTATOR) continue;
                    for (UUID uuid : this.logoutCache.keySet()) {
                        if (!uuid.equals(addedPlayer.profile().getId())) continue;
                        PlayerEntity player = this.logoutCache.get((Object)uuid).player;
                        if (this.message.getValue()) {
                            mc.execute(() -> this.sendMessage("\u00a7f" + player.getName().getString() + " \u00a7rLogged back at \u00a7f" + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ()));
                        }
                        this.logoutCache.remove(uuid);
                    }
                }
            }
        } else {
            object = event.getPacket();
            if (object instanceof PlayerRemoveS2CPacket) {
                List<UUID> profileIds;
                PlayerRemoveS2CPacket playerRemoveS2CPacket = (PlayerRemoveS2CPacket)object;
                try {
                    List<UUID> addedPlayer;
                    profileIds = addedPlayer = playerRemoveS2CPacket.profileIds();
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
                for (UUID uuid2 : profileIds) {
                    for (UUID uuid : this.playerCache.keySet()) {
                        if (!uuid.equals(uuid2)) continue;
                        PlayerEntity player = this.playerCache.get(uuid);
                        if (this.logoutCache.containsKey(uuid) || player == null) continue;
                        ModelPlayer modelPlayer = new ModelPlayer(player);
                        if (this.message.getValue()) {
                            mc.execute(() -> this.sendMessage("\u00a7f" + player.getName().getString() + " \u00a7rLogged out at \u00a7f" + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ()));
                        }
                        this.logoutCache.put(uuid, modelPlayer);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.playerCache.clear();
        this.logoutCache.clear();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.playerCache.clear();
        for (AbstractClientPlayerEntity player : Frog.THREAD.getPlayers()) {
            if (player == null || player.equals((Object)LogoutSpots.mc.player)) continue;
            this.playerCache.put(player.getGameProfile().getId(), (PlayerEntity)player);
        }
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        for (ModelPlayer data : this.logoutCache.values()) {
            PlayerEntity player = data.player;
            Box box = ((IEntity)player).getDimensions().getBoxAt(player.getPos());
            if (this.box.booleanValue) {
                Render3DUtil.drawBox(matrixStack, box, this.box.getValue());
            }
            if (this.fill.booleanValue) {
                Render3DUtil.drawFill(matrixStack, box, this.fill.getValue());
            }
            if (this.chamsFill.booleanValue || this.chamsLine.booleanValue) {
                data.render(matrixStack, this.chamsFill, this.chamsLine);
            }
            if (!this.text.booleanValue) continue;
            Render3DUtil.drawText3D(player.getName().getString() + (String)(this.health.getValue() ? String.valueOf(TextRadar.getHealthColor(player)) + " " + LogoutSpots.round2(player.getHealth() + player.getAbsorptionAmount()) : "") + (String)(this.totem.getValue() && Frog.POP.getPop(player) > 0 ? String.valueOf(TextRadar.getPopColor(Frog.POP.getPop(player))) + " -" + Frog.POP.getPop(player) : ""), new Vec3d(player.getX(), ((IEntity)player).getDimensions().getBoxAt((Vec3d)player.getPos()).maxY + 0.5, player.getZ()), this.text.getValue());
        }
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}

