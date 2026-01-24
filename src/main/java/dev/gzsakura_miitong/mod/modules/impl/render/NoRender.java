/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.CampfireSmokeParticle
 *  net.minecraft.client.particle.ElderGuardianAppearanceParticle
 *  net.minecraft.client.particle.ExplosionLargeParticle
 *  net.minecraft.client.particle.FireworksSparkParticle$FireworkParticle
 *  net.minecraft.client.particle.FireworksSparkParticle$Flash
 *  net.minecraft.client.particle.SpellParticle
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.entity.projectile.thrown.EggEntity
 *  net.minecraft.entity.projectile.thrown.ExperienceBottleEntity
 *  net.minecraft.entity.projectile.thrown.PotionEntity
 *  net.minecraft.network.packet.s2c.play.TitleS2CPacket
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.api.events.Event;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.ParticleEvent;
import dev.gzsakura_miitong.api.events.impl.RenderEntityEvent;
import dev.gzsakura_miitong.api.events.impl.TickEntityEvent;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.ElderGuardianAppearanceParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;

public class NoRender
extends Module {
    public static NoRender INSTANCE;
    public final BooleanSetting fastItem = this.add(new BooleanSetting("2DItem", false).setParent());
    public final BooleanSetting castShadow = this.add(new BooleanSetting("CastShadow", true, this.fastItem::isOpen));
    public final BooleanSetting renderSidesOfItems = this.add(new BooleanSetting("RenderSidesOfItems", false, this.fastItem::isOpen));
    public final BooleanSetting potionsIcon = this.add(new BooleanSetting("PotionsIcon", false));
    public final BooleanSetting weather = this.add(new BooleanSetting("Weather", true));
    public final BooleanSetting invisible = this.add(new BooleanSetting("Invisible", false));
    public final BooleanSetting lightsUpdate = this.add(new BooleanSetting("LightsUpdate", false));
    public final BooleanSetting potions = this.add(new BooleanSetting("Potions", true));
    public final BooleanSetting xp = this.add(new BooleanSetting("XP", true));
    public final BooleanSetting arrows = this.add(new BooleanSetting("Arrows", false));
    public final BooleanSetting eggs = this.add(new BooleanSetting("Eggs", false));
    public final BooleanSetting item = this.add(new BooleanSetting("Items", false));
    public final BooleanSetting armorParts = this.add(new BooleanSetting("ArmorParts", false));
    public final BooleanSetting armorTrim = this.add(new BooleanSetting("ArmorTrim", false));
    public final BooleanSetting armorGlint = this.add(new BooleanSetting("ArmorGlint", false));
    public final BooleanSetting hurtCam = this.add(new BooleanSetting("HurtCam", true));
    public final BooleanSetting fireOverlay = this.add(new BooleanSetting("FireOverlay", true));
    public final BooleanSetting waterOverlay = this.add(new BooleanSetting("WaterOverlay", true));
    public final BooleanSetting blockOverlay = this.add(new BooleanSetting("BlockOverlay", true));
    public final BooleanSetting portal = this.add(new BooleanSetting("Portal", true));
    public final BooleanSetting totem = this.add(new BooleanSetting("Totem", true));
    public final BooleanSetting nausea = this.add(new BooleanSetting("Nausea", true));
    public final BooleanSetting blindness = this.add(new BooleanSetting("Blindness", true));
    public final BooleanSetting fog = this.add(new BooleanSetting("Fog", false));
    public final BooleanSetting darkness = this.add(new BooleanSetting("Darkness", true));
    public final BooleanSetting fireEntity = this.add(new BooleanSetting("EntityFire", true));
    public final BooleanSetting antiTitle = this.add(new BooleanSetting("Title", false));
    public final BooleanSetting antiPlayerCollision = this.add(new BooleanSetting("PlayerCollision", true));
    public final BooleanSetting effect = this.add(new BooleanSetting("Effect", true));
    public final BooleanSetting elderGuardian = this.add(new BooleanSetting("Guardian", false));
    public final BooleanSetting explosions = this.add(new BooleanSetting("Explosions", true));
    public final BooleanSetting campFire = this.add(new BooleanSetting("CampFire", false));
    public final BooleanSetting fireworks = this.add(new BooleanSetting("Fireworks", false));
    public final BooleanSetting guiToast = this.add(new BooleanSetting("GuiToast", false));

    public NoRender() {
        super("NoRender", "Disables all overlays and potion effects.", Module.Category.Render);
        this.setChinese("\u7981\u7528\u6e32\u67d3");
        INSTANCE = this;
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof TitleS2CPacket && this.antiTitle.getValue()) {
            event.setCancelled(true);
        }
    }

    @EventListener
    public void onRender(TickEntityEvent event) {
        this.cancelEvent(event.getEntity(), event);
    }

    @EventListener
    public void onRender(RenderEntityEvent event) {
        this.cancelEvent(event.getEntity(), event);
    }

    private void cancelEvent(Entity entity, Event event) {
        if (entity instanceof PotionEntity && this.potions.getValue()) {
            event.cancel();
        } else if (entity instanceof ExperienceBottleEntity && this.xp.getValue()) {
            event.cancel();
        } else if (entity instanceof ArrowEntity && this.arrows.getValue()) {
            event.cancel();
        } else if (entity instanceof EggEntity && this.eggs.getValue()) {
            event.cancel();
        } else if (entity instanceof ItemEntity && this.item.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onParticle(ParticleEvent event) {
        if (this.elderGuardian.getValue() && event.particle instanceof ElderGuardianAppearanceParticle) {
            event.cancel();
        } else if (this.explosions.getValue() && event.particle instanceof ExplosionLargeParticle) {
            event.cancel();
        } else if (this.campFire.getValue() && event.particle instanceof CampfireSmokeParticle) {
            event.cancel();
        } else if (this.fireworks.getValue() && (event.particle instanceof FireworksSparkParticle.FireworkParticle || event.particle instanceof FireworksSparkParticle.Flash)) {
            event.cancel();
        } else if (this.effect.getValue() && event.particle instanceof SpellParticle) {
            event.cancel();
        }
    }
}

