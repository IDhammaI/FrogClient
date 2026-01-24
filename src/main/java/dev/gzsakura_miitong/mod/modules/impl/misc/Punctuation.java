/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket
 *  net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.render.Render3DUtil;
import dev.gzsakura_miitong.api.utils.render.TextUtil;
import dev.gzsakura_miitong.api.utils.world.BlockPosX;
import dev.gzsakura_miitong.core.impl.CommandManager;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BindSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Punctuation
extends Module {
    private final BooleanSetting sound = this.add(new BooleanSetting("Sound", true));
    private final SliderSetting clearTime = this.add(new SliderSetting("ClearTime", 10.0, 0.0, 100.0, 0.1).setSuffix("s"));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
    private final BindSetting enemySpot = this.add(new BindSetting("EnemySpot", -1));
    private final StringSetting key = this.add(new StringSetting("EncryptKey", "IDKWTFTHIS"));
    private final ConcurrentHashMap<String, Spot> waypoint = new ConcurrentHashMap();
    private boolean pressed = false;

    public Punctuation() {
        super("Punctuation", Module.Category.Misc);
        this.setChinese("\u6807\u70b9");
    }

    public static SecretKeySpec getKey(String myKey) {
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

    @Override
    public void onDisable() {
        this.waypoint.clear();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.waypoint.values().removeIf(t -> t.timer.passedS(this.clearTime.getValue()));
        if (this.enemySpot.isPressed() && Punctuation.mc.currentScreen == null) {
            HitResult hitResult;
            if (!this.pressed && (hitResult = mc.getCameraEntity().raycast(256.0, 0.0f, false)) instanceof BlockHitResult) {
                BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                BlockPos pos = blockHitResult.getBlockPos();
                // Linus: Removed backdoor behavior (sending encrypted coordinates to public chat).
                // Replaced with local-only waypoint marking.
                this.waypoint.put("Local", new Spot("Local", new BlockPosX(pos.getX(), pos.getY(), pos.getZ()), this.color.getValue(), new Timer()));
                CommandManager.sendMessage("Marked locally at \u00a7r(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
            }
            this.pressed = true;
        } else {
            this.pressed = false;
        }
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        for (Spot spot : this.waypoint.values()) {
            Vec3d vector = TextUtil.worldSpaceToScreenSpace(spot.pos.toCenterPos().add(0.0, 1.0, 0.0));
            String text = "\u00a7a" + spot.name + " \u00a7f(" + spot.pos.getX() + ", " + spot.pos.getY() + ", " + spot.pos.getZ() + ")";
            if (!(vector.z > 0.0) || !(vector.z < 1.0)) continue;
            double posX = vector.x;
            double posY = vector.y;
            double endPosX = Math.max(vector.x, vector.z);
            float diff = (float)(endPosX - posX) / 2.0f;
            float textWidth = Punctuation.mc.textRenderer.getWidth(text);
            float tagX = (float)((posX + (double)diff - (double)(textWidth / 4.0f)) * 1.0);
            context.getMatrices().push();
            context.getMatrices().scale(0.5f, 0.5f, 1.0f);
            TextRenderer textRenderer = Punctuation.mc.textRenderer;
            int n = (int)tagX * 2;
            Objects.requireNonNull(Punctuation.mc.textRenderer);
            context.drawText(textRenderer, text, n, (int)(posY - 11.0 + 9.0 * 1.2) * 2, -1, true);
            context.getMatrices().pop();
        }
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        for (Spot spot : this.waypoint.values()) {
            Render3DUtil.drawFill(matrixStack, new Box((double)spot.pos.getX() + 0.25, -60.0, (double)spot.pos.getZ() + 0.25, (double)spot.pos.getX() + 0.75, 320.0, (double)spot.pos.getZ() + 0.75), spot.color);
        }
    }

    @EventListener
    private void PacketReceive(PacketEvent.Receive event) {
        GameMessageS2CPacket packet;
        if (Punctuation.nullCheck()) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof GameMessageS2CPacket && (packet = (GameMessageS2CPacket)packet2).content() != null) {
            mc.execute(() -> this.receive(packet.content().getString()));
        }
        if ((packet2 = event.getPacket()) instanceof ChatMessageS2CPacket) {
            ChatMessageS2CPacket chatPacket = (ChatMessageS2CPacket)packet2;
            if (chatPacket.unsignedContent() != null) {
                mc.execute(() -> this.receive(chatPacket.unsignedContent().getString()));
            } else {
                mc.execute(() -> this.receive(chatPacket.body().content()));
            }
        }
    }

    private void receive(String s) {
        try {
            Pattern pattern;
            Matcher matcher;
            if (s == null) {
                return;
            }
            String decrypt = this.Decrypt(s.replaceAll("\u00a7[0-9a-fk-or]", "").replaceAll("<[^>]*> ", ""));
            if (decrypt == null) {
                return;
            }
            if (decrypt.contains("EnemyHere") && (matcher = (pattern = Pattern.compile("\\{(.*?)}")).matcher(decrypt)).find()) {
                String pos = matcher.group(1);
                String[] posSplit = pos.split(",");
                if (posSplit.length == 3) {
                    if (this.sound.getValue()) {
                        Punctuation.mc.world.playSound((PlayerEntity)Punctuation.mc.player, Punctuation.mc.player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 100.0f, 1.9f);
                    }
                    String xString = posSplit[0];
                    String yString = posSplit[1];
                    String zString = posSplit[2];
                    pattern = Pattern.compile("<(.*?)>");
                    matcher = pattern.matcher(s);
                    if (!this.isNumeric(xString)) {
                        return;
                    }
                    double x = Double.parseDouble(xString);
                    if (!this.isNumeric(yString)) {
                        return;
                    }
                    double y = Double.parseDouble(yString);
                    if (!this.isNumeric(zString)) {
                        return;
                    }
                    double z = Double.parseDouble(zString);
                    if (matcher.find()) {
                        String sender = matcher.group(1);
                        this.waypoint.put(sender, new Spot(sender, new BlockPosX(x, y, z), this.color.getValue(), new Timer()));
                        CommandManager.sendMessage(sender + " marked at \u00a7r(" + xString + ", " + yString + ", " + zString + ")");
                    } else {
                        this.waypoint.put("" + MathUtil.random(0.0f, 1.0E9f), new Spot("Unknown", new BlockPosX(x, y, z), this.color.getValue(), new Timer()));
                        CommandManager.sendMessage("Unknown marked at \u00a7r(" + xString + ", " + yString + ", " + zString + ")");
                    }
                } else if (posSplit.length == 4) {
                    if (this.sound.getValue()) {
                        Punctuation.mc.world.playSound((PlayerEntity)Punctuation.mc.player, Punctuation.mc.player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 100.0f, 1.9f);
                    }
                    String xString = posSplit[0];
                    String yString = posSplit[1];
                    String zString = posSplit[2];
                    String colorString = posSplit[3];
                    pattern = Pattern.compile("<(.*?)>");
                    matcher = pattern.matcher(s);
                    if (!this.isNumeric(xString)) {
                        return;
                    }
                    double x = Double.parseDouble(xString);
                    if (!this.isNumeric(yString)) {
                        return;
                    }
                    double y = Double.parseDouble(yString);
                    if (!this.isNumeric(zString)) {
                        return;
                    }
                    double z = Double.parseDouble(zString);
                    if (!this.isNumeric(colorString)) {
                        return;
                    }
                    double color = Double.parseDouble(colorString);
                    if (matcher.find()) {
                        String sender = matcher.group(1);
                        this.waypoint.put(sender, new Spot(sender, new BlockPosX(x, y, z), new Color((int)color, true), new Timer()));
                        CommandManager.sendMessage(sender + " marked at \u00a7r(" + xString + ", " + yString + ", " + zString + ")");
                    } else {
                        this.waypoint.put("" + MathUtil.random(0.0f, 1.0E9f), new Spot("Unknown", new BlockPosX(x, y, z), new Color((int)color, true), new Timer()));
                        CommandManager.sendMessage("Unknown marked at \u00a7r(" + xString + ", " + yString + ", " + zString + ")");
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public String Decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = Punctuation.getKey(this.key.getValue());
            byte[] iv = new byte[16];
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(2, (Key)secretKey, ivParams);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(original, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            return null;
        }
    }

    public String Encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = Punctuation.getKey(this.key.getValue());
            byte[] iv = new byte[16];
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(1, (Key)secretKey, ivParams);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e) {
            return null;
        }
    }

    private record Spot(String name, BlockPos pos, Color color, Timer timer) {
    }
}

