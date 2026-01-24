/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.SpawnGroup
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.MathHelper
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.asm.accessors.IGameRenderer;
import dev.gzsakura_miitong.core.impl.ShaderManager;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class ShaderModule
extends Module {
    public static ShaderModule INSTANCE;
    private final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.Shader));
    public final EnumSetting<ShaderManager.Shader> mode = this.add(new EnumSetting<ShaderManager.Shader>("Mode", ShaderManager.Shader.Solid, () -> this.page.getValue() == Page.Shader));
    public final SliderSetting speed = this.add(new SliderSetting("Speed", 4.0, 0.0, 20.0, 0.1, () -> this.page.getValue() == Page.Shader));
    public final ColorSetting fill = this.add(new ColorSetting("Color", () -> this.page.getValue() == Page.Shader));
    public final SliderSetting maxSample = this.add(new SliderSetting("MaxSample", 10.0, 0.0, 20.0, () -> this.page.getValue() == Page.Shader));
    public final SliderSetting divider = this.add(new SliderSetting("Divider", 150.0, 0.0, 300.0, () -> this.page.getValue() == Page.Shader));
    public final SliderSetting radius = this.add(new SliderSetting("Radius", 2.0, 0.0, 6.0, () -> this.page.getValue() == Page.Shader));
    public final SliderSetting smoothness = this.add(new SliderSetting("Smoothness", 1.0, 0.0, 1.0, 0.01, () -> this.page.getValue() == Page.Shader));
    public final SliderSetting alpha = this.add(new SliderSetting("GlowAlpha", 255, 0, 255, () -> this.page.getValue() == Page.Shader));
    public final SliderSetting maxRange = this.add(new SliderSetting("MaxRange", 64, 16, 512, () -> this.page.getValue() == Page.Target));
    public final SliderSetting pulseSpeed = this.add(new SliderSetting("PulseSize", 10.0, 0.0, 200.0, 0.1, () -> this.page.getValue() == Page.Color));
    public final ColorSetting pulse = this.add(new ColorSetting("Pulse", new Color(255, 255, 255), () -> this.page.getValue() == Page.Color));
    public final SliderSetting step = this.add(new SliderSetting("Step", 0.5, 0.0, 4.0, 0.01f, () -> this.page.getValue() == Page.Color));
    public final SliderSetting octaves = this.add(new SliderSetting("Octaves", 10, 5, 30, () -> this.page.getValue() == Page.Color));
    public final ColorSetting smoke1 = this.add(new ColorSetting("Color1", new Color(255, 255, 255), () -> this.page.getValue() == Page.Color));
    public final ColorSetting smoke2 = this.add(new ColorSetting("Color2", new Color(255, 255, 255), () -> this.page.getValue() == Page.Color));
    public final ColorSetting smoke3 = this.add(new ColorSetting("Color3", new Color(255, 255, 255), () -> this.page.getValue() == Page.Color));
    public final ColorSetting smoke4 = this.add(new ColorSetting("Color4", new Color(255, 255, 255), () -> this.page.getValue() == Page.Color));
    private final BooleanSetting hands = this.add(new BooleanSetting("Hands", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting self = this.add(new BooleanSetting("Self", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting players = this.add(new BooleanSetting("Players", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting friends = this.add(new BooleanSetting("Friends", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting crystals = this.add(new BooleanSetting("Crystals", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting creatures = this.add(new BooleanSetting("Creatures", false, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting monsters = this.add(new BooleanSetting("Monsters", false, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting ambients = this.add(new BooleanSetting("Ambients", false, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting items = this.add(new BooleanSetting("Items", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting others = this.add(new BooleanSetting("Others", false, () -> this.page.getValue() == Page.Target));

    public ShaderModule() {
        super("Shader", Module.Category.Render);
        this.setChinese("\u7740\u8272\u5668");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    public boolean shouldRender(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (ShaderModule.mc.player == null) {
            return false;
        }
        if ((double)MathHelper.sqrt((float)((float)ShaderModule.mc.player.squaredDistanceTo(entity.getPos()))) > this.maxRange.getValue()) {
            return false;
        }
        Entity entity2 = entity;
        Objects.requireNonNull(entity2);
        Entity entity3 = entity2;
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            if (entity == ShaderModule.mc.player) {
                return this.self.getValue();
            }
            if (Alien.FRIEND.isFriend(playerEntity)) {
                return this.friends.getValue();
            }
            return this.players.getValue();
        }
        if (entity instanceof EndCrystalEntity) {
            return this.crystals.getValue();
        }
        if (entity instanceof ItemEntity) {
            return this.items.getValue();
        }
        return switch (entity.getType().getSpawnGroup()) {
            case SpawnGroup.CREATURE, SpawnGroup.WATER_CREATURE -> this.creatures.getValue();
            case SpawnGroup.MONSTER -> this.monsters.getValue();
            case SpawnGroup.AMBIENT, SpawnGroup.WATER_AMBIENT -> this.ambients.getValue();
            default -> this.others.getValue();
        };
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (this.hands.getValue()) {
            Alien.SHADER.renderShader(() -> ((IGameRenderer)ShaderModule.mc.gameRenderer).IRenderHand(ShaderModule.mc.gameRenderer.getCamera(), mc.getRenderTickCounter().getTickDelta(true), matrixStack.peek().getPositionMatrix()), this.mode.getValue());
        }
    }

    @Override
    public void onToggle() {
        Alien.SHADER.reloadShaders();
    }

    @Override
    public void onLogin() {
        Alien.SHADER.reloadShaders();
    }

    private static enum Page {
        Shader,
        Target,
        Color;

    }
}

