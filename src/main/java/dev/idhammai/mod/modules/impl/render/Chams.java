/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.mob.MobEntity
 *  net.minecraft.entity.mob.SlimeEntity
 *  net.minecraft.entity.passive.AnimalEntity
 *  net.minecraft.entity.passive.VillagerEntity
 *  net.minecraft.entity.passive.WanderingTraderEntity
 *  net.minecraft.entity.player.PlayerEntity
 */
package dev.idhammai.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.Render3DEvent;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;

public class Chams
extends Module {
    public static Chams INSTANCE;
    public final BooleanSetting crystal = this.add(new BooleanSetting("Crystal", true).setParent());
    public final BooleanSetting custom = this.add(new BooleanSetting("Custom", false, this.crystal::isOpen).setParent());
    public final BooleanSetting depth = this.add(new BooleanSetting("Depth", false, () -> this.crystal.isOpen() && this.custom.isOpen()));
    public final BooleanSetting chamsTexture = this.add(new BooleanSetting("ChamsTexture", true, () -> this.crystal.isOpen() && this.custom.isOpen()));
    public final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(255, 255, 255, 100), () -> this.crystal.isOpen() && this.custom.isOpen()).injectBoolean(false));
    public final ColorSetting line = this.add(new ColorSetting("Line", new Color(255, 255, 255, 100), () -> this.crystal.isOpen() && this.custom.isOpen()).injectBoolean(false));
    public final ColorSetting core = this.add(new ColorSetting("Core", new Color(255, 255, 255, 255), this.crystal::isOpen).injectBoolean(true));
    public final ColorSetting outerFrame = this.add(new ColorSetting("OuterFrame", new Color(255, 255, 255, 255), this.crystal::isOpen).injectBoolean(true));
    public final ColorSetting innerFrame = this.add(new ColorSetting("InnerFrame", new Color(255, 255, 255, 255), this.crystal::isOpen).injectBoolean(true));
    public final BooleanSetting glint = this.add(new BooleanSetting("Glint", true, this.crystal::isOpen));
    public final BooleanSetting texture = this.add(new BooleanSetting("Texture", true, this.crystal::isOpen));
    public final BooleanSetting spinSync = this.add(new BooleanSetting("SpinSync", false, this.crystal::isOpen));
    public final SliderSetting scale = this.add(new SliderSetting("Scale", 1.0, 0.0, 3.0, 0.01, this.crystal::isOpen));
    public final SliderSetting spinValue = this.add(new SliderSetting("SpinSpeed", 1.0, 0.0, 3.0, 0.01, this.crystal::isOpen));
    public final SliderSetting bounceHeight = this.add(new SliderSetting("BounceHeight", 1.0, 0.0, 3.0, 0.01, this.crystal::isOpen));
    public final SliderSetting floatValue = this.add(new SliderSetting("BounceSpeed", 1.0, 0.0, 3.0, 0.01, this.crystal::isOpen));
    public final SliderSetting floatOffset = this.add(new SliderSetting("YOffset", 0.0, -1.0, 1.0, 0.01, this.crystal::isOpen));
    public final BooleanSetting throughWall = this.add(new BooleanSetting("ThroughWall", false).setParent());
    private final BooleanSetting Crystals = this.add(new BooleanSetting("Crystals", true, this.throughWall::isOpen));
    private final BooleanSetting Players = this.add(new BooleanSetting("Players", true, this.throughWall::isOpen));
    private final BooleanSetting Mobs = this.add(new BooleanSetting("Mobs", true, this.throughWall::isOpen));
    private final BooleanSetting Animals = this.add(new BooleanSetting("Animals", true, this.throughWall::isOpen));
    private final BooleanSetting Villagers = this.add(new BooleanSetting("Villagers", true, this.throughWall::isOpen));
    private final BooleanSetting Slimes = this.add(new BooleanSetting("Slimes", true, this.throughWall::isOpen));
    public final ColorSetting hand = this.add(new ColorSetting("Hand", -1).injectBoolean(true));
    public int age;

    public Chams() {
        super("Chams", Module.Category.Render);
        this.setChinese("\u6a21\u578b\u4e0a\u8272");
        INSTANCE = this;
    }

    public boolean customCrystal() {
        return this.isOn() && this.crystal.getValue();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        ++this.age;
    }

    @EventListener(priority=-2147483648)
    public void onRender3D(Render3DEvent event) {
        if (this.hand.booleanValue) {
            Color color = this.hand.getValue();
            RenderSystem.setShaderColor((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
        }
    }

    public boolean chams(Entity entity) {
        if (entity instanceof EndCrystalEntity) {
            return this.Crystals.getValue();
        }
        if (entity instanceof SlimeEntity) {
            return this.Slimes.getValue();
        }
        if (entity instanceof PlayerEntity) {
            return this.Players.getValue();
        }
        if (entity instanceof VillagerEntity || entity instanceof WanderingTraderEntity) {
            return this.Villagers.getValue();
        }
        if (entity instanceof AnimalEntity) {
            return this.Animals.getValue();
        }
        if (entity instanceof MobEntity) {
            return this.Mobs.getValue();
        }
        return false;
    }
}

