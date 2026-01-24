/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.PreRender2DEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class Ambience
extends Module {
    public static Ambience INSTANCE;
    public final ColorSetting filter = this.add(new ColorSetting("Filter", new Color(255, 255, 255, 20)).injectBoolean(false));
    public final ColorSetting worldColor = this.add(new ColorSetting("WorldColor", new Color(-1, true)).injectBoolean(true));
    public final BooleanSetting customTime = this.add(new BooleanSetting("CustomTime", false).setParent());
    public final SliderSetting time = this.add(new SliderSetting("Time", 0, 0, 24000, this.customTime::isOpen));
    public final ColorSetting fog = this.add(new ColorSetting("FogColor", new Color(13401557)).injectBoolean(false));
    public final ColorSetting sky = this.add(new ColorSetting("SkyColor", new Color(0)).injectBoolean(false));
    public final ColorSetting cloud = this.add(new ColorSetting("CloudColor", new Color(0)).injectBoolean(false));
    public final ColorSetting dimensionColor = this.add(new ColorSetting("DimensionColor", new Color(0)).injectBoolean(false));
    public final BooleanSetting fogDistance = this.add(new BooleanSetting("FogDistance", false).setParent());
    public final SliderSetting fogStart = this.add(new SliderSetting("FogStart", 50, 0, 1000, this.fogDistance::isOpen));
    public final SliderSetting fogEnd = this.add(new SliderSetting("FogEnd", 100, 0, 1000, this.fogDistance::isOpen));
    public final BooleanSetting fullBright = this.add(new BooleanSetting("FullBright", false));
    public final BooleanSetting forceOverworld = this.add(new BooleanSetting("ForceOverworld", false));
    public final BooleanSetting customLuminance = this.add(new BooleanSetting("CustomLuminance", false).setParent().injectTask(() -> {
        if (!Ambience.nullCheck()) {
            Ambience.mc.worldRenderer.reload();
        }
    }));
    public final SliderSetting luminance = this.add(new SliderSetting("Luminance", 15, 0, 15, this.customLuminance::isOpen).injectTask(() -> {
        if (!Ambience.nullCheck() && this.customLuminance.getValue()) {
            Ambience.mc.worldRenderer.reload();
        }
    }));
    long oldTime;

    public Ambience() {
        super("Ambience", "Custom ambience", Module.Category.Render);
        this.setChinese("\u81ea\u5b9a\u4e49\u73af\u5883");
        INSTANCE = this;
    }

    @EventListener
    public void onRender2D(PreRender2DEvent event) {
        if (this.filter.booleanValue) {
            event.drawContext.fill(0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), this.filter.getValue().getRGB());
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.customTime.getValue()) {
            Ambience.mc.world.setTimeOfDay((long)this.time.getValue());
        }
    }

    @Override
    public void onEnable() {
        if (Ambience.nullCheck()) {
            return;
        }
        this.oldTime = Ambience.mc.world.getTimeOfDay();
        if (this.customTime.getValue()) {
            Ambience.mc.world.setTimeOfDay((long)this.time.getValue());
        }
    }

    @Override
    public void onDisable() {
        if (Ambience.nullCheck()) {
            return;
        }
        Ambience.mc.world.setTimeOfDay(this.oldTime);
    }

    @EventListener
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket && this.customTime.getValue()) {
            this.oldTime = ((WorldTimeUpdateS2CPacket)event.getPacket()).getTime();
            event.cancel();
        }
    }
}

