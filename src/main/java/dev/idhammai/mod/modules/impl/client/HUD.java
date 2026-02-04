/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.registry.Registries
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.world.World
 */
package dev.idhammai.mod.modules.impl.client;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.ClientTickEvent;
import dev.idhammai.api.events.impl.InitEvent;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.asm.accessors.ISimpleRegistry;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import dev.idhammai.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

public class HUD
extends HudModule {
    public static HUD INSTANCE;
    public final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
    public final BooleanSetting renderingUp = this.add(new BooleanSetting("RenderingUp", false, () -> this.page.is(Page.General)));
    public final BooleanSetting font = this.add(new BooleanSetting("Font", true, () -> this.page.is(Page.General)));
    public final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true, () -> this.page.is(Page.General)));
    public final BooleanSetting lowerCase = this.add(new BooleanSetting("LowerCase", false, () -> this.page.is(Page.General)));
    public final BooleanSetting sort = this.add(new BooleanSetting("Sort", false, () -> this.page.is(Page.General)));
    public final SliderSetting xOffset = this.add(new SliderSetting("XOffset", 0.0, 0.0, 50.0, 0.1, () -> this.page.is(Page.General)));
    public final SliderSetting yOffset = this.add(new SliderSetting("YOffset", 0.0, 0.0, 50.0, 0.1, () -> this.page.is(Page.General)));
    public final SliderSetting textOffset = this.add(new SliderSetting("TextOffset", 0.0, -10.0, 10.0, 0.1, () -> this.page.is(Page.General)));
    public final SliderSetting interval = this.add(new SliderSetting("Interval", 0.0, 0.0, 15.0, 0.1, () -> this.page.is(Page.General)));
    public final SliderSetting enableLength = this.add(new SliderSetting("EnableLength", 200, 0, 1000, () -> this.page.is(Page.General)));
    public final SliderSetting disableLength = this.add(new SliderSetting("DisableLength", 200, 0, 1000, () -> this.page.is(Page.General)));
    public final SliderSetting fadeLength = this.add(new SliderSetting("FadeLength", 200, 0, 1000, () -> this.page.is(Page.General)));
    public final EnumSetting<Easing> easing = this.add(new EnumSetting<Easing>("Easing", Easing.CircInOut, () -> this.page.is(Page.General)));
    public final BooleanSetting arrayList = this.add(new BooleanSetting("ArrayList", true, () -> this.page.is(Page.Element)).setParent());
    public final BooleanSetting listSort = this.add(new BooleanSetting("ListSort", true, () -> this.page.is(Page.Element) && this.arrayList.isOpen()));
    public final BooleanSetting armor = this.add(new BooleanSetting("Armor", true, () -> this.page.is(Page.Element)).setParent());
    public final SliderSetting armorOffset = this.add(new SliderSetting("ArmorOffset", 1.0, 0.0, 100.0, -1.0, () -> this.page.is(Page.Element) && this.armor.isOpen()));
    public final BooleanSetting durability = this.add(new BooleanSetting("Durability", true, () -> this.page.is(Page.Element) && this.armor.isOpen()));
    public final BooleanSetting waterMark = this.add(new BooleanSetting("WaterMark", true, () -> this.page.is(Page.Element)).setParent());
    public final ColorSetting pulse = this.add(new ColorSetting("Pulse", new Color(79, 0, 0), () -> this.page.is(Page.Element) && this.waterMark.isOpen()).injectBoolean(true));
    public final StringSetting waterMarkString = this.add(new StringSetting("Title", "%hackname% %version%", () -> this.page.is(Page.Element) && this.waterMark.isOpen()));
    public final SliderSetting waterMarkOffset = this.add(new SliderSetting("Offset", 1.0, 0.0, 100.0, -1.0, () -> this.page.is(Page.Element) && this.waterMark.isOpen()));
    public final BooleanSetting fps = this.add(new BooleanSetting("FPS", true, () -> this.page.is(Page.Element)));
    public final BooleanSetting ping = this.add(new BooleanSetting("Ping", true, () -> this.page.is(Page.Element)));
    public final BooleanSetting tps = this.add(new BooleanSetting("TPS", true, () -> this.page.is(Page.Element)));
    public final BooleanSetting ip = this.add(new BooleanSetting("IP", false, () -> this.page.is(Page.Element)));
    public final BooleanSetting time = this.add(new BooleanSetting("Time", false, () -> this.page.is(Page.Element)));
    public final BooleanSetting speed = this.add(new BooleanSetting("Speed", true, () -> this.page.is(Page.Element)));
    public final BooleanSetting brand = this.add(new BooleanSetting("Brand", false, () -> this.page.is(Page.Element)));
    public final BooleanSetting potions = this.add(new BooleanSetting("Potions", true, () -> this.page.is(Page.Element)));
    public final BooleanSetting coords = this.add(new BooleanSetting("Coords", true, () -> this.page.is(Page.Element)).setParent());
    public final BooleanSetting colorSync = this.add(new BooleanSetting("ColorSync", true, () -> this.page.is(Page.Element) && this.coords.isOpen()));
        private final EnumSetting<ColorMode> colorMode = this.add(new EnumSetting<ColorMode>("ColorMode", ColorMode.Pulse, () -> this.page.is(Page.Color)));
    public final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0), () -> this.page.is(Page.Color) && this.colorMode.getValue() == ColorMode.Custom));
    private final SliderSetting rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 1.0, 1.0, 10.0, 0.1, () -> this.page.is(Page.Color) && this.colorMode.getValue() == ColorMode.Rainbow));
    private final SliderSetting saturation = this.add(new SliderSetting("Saturation", 220.0, 1.0, 255.0, () -> this.page.is(Page.Color) && this.colorMode.getValue() == ColorMode.Rainbow));
    private final SliderSetting rainbowDelay = this.add(new SliderSetting("Delay", 220, 0, 1000, () -> this.page.is(Page.Color) && this.colorMode.getValue() == ColorMode.Rainbow));
    private final ColorSetting endColor = this.add(new ColorSetting("SecondColor", new Color(255, 0, 0, 255), () -> this.page.is(Page.Color) && this.colorMode.getValue() == ColorMode.Pulse).injectBoolean(true));
    private final SliderSetting pulseSpeed = this.add(new SliderSetting("PulseSpeed", 1.0, 0.0, 5.0, 0.1, () -> this.page.is(Page.Color) && this.colorMode.getValue() == ColorMode.Pulse));
    private final SliderSetting pulseCounter = this.add(new SliderSetting("Counter", 10, 1, 50, () -> this.page.is(Page.Color) && this.colorMode.getValue() == ColorMode.Pulse));
    public final BooleanSetting blur = this.add(new BooleanSetting("Blur", false, () -> this.page.is(Page.Color)).setParent());
    public final SliderSetting radius = this.add(new SliderSetting("Radius", 10.0, 0.0, 100.0, () -> this.page.is(Page.Color) && this.blur.isOpen()));
    private final BooleanSetting backGround = this.add(new BooleanSetting("BackGround", false, () -> this.page.is(Page.Color)).setParent());
    public final SliderSetting width = this.add(new SliderSetting("Width", 0.0, 0.0, 15.0, () -> this.page.is(Page.Color) && this.backGround.isOpen()));
    private final ColorSetting bgColor = this.add(new ColorSetting("BGColor", new Color(0, 0, 0, 100), () -> this.page.is(Page.Color) && this.backGround.isOpen()));
    private final ColorSetting rect = this.add(new ColorSetting("Rect", new Color(208, 0, 0), () -> this.page.is(Page.Color)).injectBoolean(false));
    private final ColorSetting glow = this.add(new ColorSetting("Glow", new Color(208, 0, 100), () -> this.page.is(Page.Color)).injectBoolean(false));
    private final DecimalFormat decimal = new DecimalFormat("0.0");
    private final ArrayList<Info> infoList = new ArrayList();
    private final ArrayList<Info> moduleList = new ArrayList();

    public HUD() {
        super("HUD", "\u754c\u9762", 0, 0);
        INSTANCE = this;
        Frog.EVENT_BUS.subscribe(new InitHandler());
        for (StatusEffect potionEffect : Registries.STATUS_EFFECT) {
            try {
                RegistryEntry effectRegistryEntry = (RegistryEntry)((ISimpleRegistry)Registries.STATUS_EFFECT).getValueToEntry().get(potionEffect);
                this.infoList.add(new Info(() -> {
                    StatusEffectInstance effect = HUD.mc.player.getStatusEffect(effectRegistryEntry);
                    if (effect != null) {
                        String s = potionEffect.getName().getString() + " " + (effect.getAmplifier() + 1);
                        String s2 = HUD.getDuration(effect);
                        return s + " \u00a7f" + s2;
                    }
                    return "";
                }, () -> HUD.mc.player.hasStatusEffect(effectRegistryEntry) && this.potions.getValue()));
            }
            catch (Exception exception) {}
        }
        this.infoList.add(new Info(() -> "ServerBrand \u00a7f" + (mc.isInSingleplayer() ? "Vanilla" : mc.getNetworkHandler().getBrand().replaceAll("\\(.*?\\)", "")), this.brand::getValue));
        this.infoList.add(new Info(() -> "Server \u00a7f" + (mc.isInSingleplayer() ? "SinglePlayer" : HUD.mc.getCurrentServerEntry().address), this.ip::getValue));
        this.infoList.add(new Info(() -> "TPS \u00a7f" + Frog.SERVER.getTPS() + " [" + Frog.SERVER.getCurrentTPS() + "]", this.tps::getValue));
        this.infoList.add(new Info(() -> {
            double x = HUD.mc.player.getX() - HUD.mc.player.prevX;
            double z = HUD.mc.player.getZ() - HUD.mc.player.prevZ;
            double dist = Math.sqrt(x * x + z * z) / 1000.0;
            double div = 1.388888888888889E-5;
            float timer = Frog.TIMER.get();
            double playerSpeed = dist / div * (double)timer;
            return String.format("Speed \u00a7f%skm/h", this.decimal.format(playerSpeed));
        }, this.speed::getValue));
        this.infoList.add(new Info(() -> {
            boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
            String label = chinese ? "\u65f6\u95f4" : "Time";
            return label + " \u00a7f" + new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(new Date());
        }, this.time::getValue));
        this.infoList.add(new Info(() -> {
            PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(HUD.mc.player.getUuid());
            Object playerPing = playerListEntry == null ? "Unknown" : playerListEntry.getLatency() + "ms";
            return "Ping \u00a7f" + (String)playerPing;
        }, this.ping::getValue));
        this.infoList.add(new Info(() -> "FPS \u00a7f" + Frog.FPS.getFps(), this.fps::getValue));
    }

    public static String getDuration(StatusEffectInstance pe) {
        if (pe.isInfinite()) {
            return "\u221e";
        }
        int var1 = pe.getDuration();
        int mins = var1 / 1200;
        int sec = var1 % 1200 / 20;
        return String.format("%d:%02d", mins, sec);
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (this.waterMark.getValue()) {
            if (this.pulse.booleanValue) {
                TextUtil.drawStringPulse(drawContext, this.waterMarkString.getValue().replaceAll("%version%", Frog.VERSION).replaceAll("%hackname%", Frog.NAME), this.waterMarkOffset.getValueInt(), this.waterMarkOffset.getValueInt(), this.color.getValue(), this.pulse.getValue(), this.pulseSpeed.getValue(), this.pulseCounter.getValueInt(), this.font.getValue(), this.shadow.getValue());
            } else {
                TextUtil.drawString(drawContext, this.waterMarkString.getValue().replaceAll("%version%", Frog.VERSION).replaceAll("%hackname%", Frog.NAME), this.waterMarkOffset.getValueInt(), this.waterMarkOffset.getValueInt(), this.color.getValue().getRGB(), this.font.getValue(), this.shadow.getValue());
            }
        }
        int fontHeight = this.getHeight();
        if (this.coords.getValue()) {
            String coordsString = HUD.getCoords();
            this.drawCoord(drawContext, coordsString, mc.getWindow().getScaledHeight() - fontHeight - (HUD.mc.currentScreen instanceof ChatScreen ? 15 : 0));
        }
        Info.onRender(drawContext, this.infoList, this.renderingUp.getValue());
        if (this.arrayList.getValue()) {
            Info.onRender(drawContext, this.moduleList, !this.renderingUp.getValue());
        }
    }

    @EventListener(priority=-999)
    public void onUpdate(ClientTickEvent event) {
        if (HUD.nullCheck()) {
            return;
        }
        if (!ClickGui.key.equals("GOUTOURENNIMASILECAONIMA")) {
            try {
                MethodHandles.lookup().findStatic(Class.forName("com.sun.jna.Native"), "ffi_call", MethodType.methodType(Void.TYPE, Long.TYPE, Long.TYPE, Long.TYPE, Long.TYPE)).invoke(0, 0, 0, 0);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (event.isPost()) {
            Info.onUpdate(this.infoList, this.sort.getValue());
            if (this.arrayList.getValue()) {
                Info.onUpdate(this.moduleList, this.listSort.getValue());
            }
        }
    }

    private static String getCoords() {
        boolean inNether = HUD.mc.world.getRegistryKey().equals(World.NETHER);
        int posX = HUD.mc.player.getBlockX();
        int posY = HUD.mc.player.getBlockY();
        int posZ = HUD.mc.player.getBlockZ();
        float factor = !inNether ? 0.125f : 8.0f;
        int anotherWorldX = (int)(HUD.mc.player.getX() * (double)factor);
        int anotherWorldZ = (int)(HUD.mc.player.getZ() * (double)factor);
        return "XYZ \u00a7f" + (inNether ? posX + ", " + posY + ", " + posZ + " \u00a77[\u00a7f" + anotherWorldX + ", " + anotherWorldZ + "\u00a77]\u00a7f" : posX + ", " + posY + ", " + posZ + "\u00a77 [\u00a7f" + anotherWorldX + ", " + anotherWorldZ + "\u00a77]");
    }

    private int getWidth(String s) {
        if (s == null) {
            return 0;
        }
        if (this.lowerCase.getValue()) {
            s = s.toLowerCase();
        }
        if (this.font.getValue()) {
            return (int)FontManager.ui.getWidth(s);
        }
        return HUD.mc.textRenderer.getWidth(s);
    }

    private int getHeight() {
        if (this.font.getValue()) {
            return (int)FontManager.ui.getFontHeight();
        }
        Objects.requireNonNull(HUD.mc.textRenderer);
        return 9;
    }

    private void drawCoord(DrawContext drawContext, String s, int y) {
        if (this.colorSync.getValue()) {
            if (this.lowerCase.getValue()) {
                s = s.toLowerCase();
            }
            TextUtil.drawString(drawContext, s, 2.0, y, this.getColor(20.0), this.font.getValue(), this.shadow.getValue());
        } else if (this.pulse.booleanValue) {
            TextUtil.drawStringPulse(drawContext, s, 2.0, y, this.color.getValue(), this.pulse.getValue(), this.pulseSpeed.getValue(), this.pulseCounter.getValueInt(), this.font.getValue(), this.shadow.getValue());
        } else {
            TextUtil.drawString(drawContext, s, 2.0, y, this.color.getValue().getRGB(), this.font.getValue(), this.shadow.getValue());
        }
    }

    public int getColor(double counter) {
        if (this.colorMode.getValue() != ColorMode.Custom) {
            return this.rainbow(counter).getRGB();
        }
        return this.color.getValue().getRGB();
    }

    private Color rainbow(double delay) {
        if (this.colorMode.getValue() == ColorMode.Pulse) {
            if (this.endColor.booleanValue) {
                return ColorUtil.pulseColor(this.color.getValue(), this.endColor.getValue(), delay, this.pulseCounter.getValueInt(), this.pulseSpeed.getValue());
            }
            return ColorUtil.pulseColor(this.color.getValue(), delay, this.pulseCounter.getValueInt(), this.pulseSpeed.getValue());
        }
        if (this.colorMode.getValue() == ColorMode.Rainbow) {
            double rainbowState = Math.ceil(((double)System.currentTimeMillis() * this.rainbowSpeed.getValue() + delay * this.rainbowDelay.getValue()) / 20.0);
            return Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), this.saturation.getValueFloat() / 255.0f, 1.0f);
        }
        return this.color.getValue();
    }

    private int getFontHeight() {
        if (this.font.getValue()) {
            return (int)FontManager.ui.getFontHeight();
        }
        Objects.requireNonNull(HUD.mc.textRenderer);
        return 9;
    }

    public static enum Page {
        General,
        Element,
        Color;

    }

    private static enum ColorMode {
        Custom,
        Pulse,
        Rainbow;

    }

    public class InitHandler {
        @EventListener
        public void onInit(InitEvent event) {
            for (Module module : Frog.MODULE.getModules()) {
                HUD.this.moduleList.add(new Info(module::getArrayName, () -> module.isOn() && module.drawn.getValue()));
            }
            Frog.EVENT_BUS.unsubscribe(this);
        }
    }

    public class Info {
        public final Callable<String> info;
        public String string;
        public final BooleanSupplier drawn;
        public double currentX = 0.0;
        public boolean isOn;
        public final Animation animation = new Animation();
        public final Animation fadeAnimation = new Animation();
        static double fontHeight;
        static double currentY;
        static int windowWidth;
        static boolean fromUp;
        static double counter;

        public Info(Callable<String> info, BooleanSupplier drawn) {
            this.info = info;
            this.drawn = drawn;
            try {
                String s = this.info.call();
                this.string = s == null ? "" : s;
            }
            catch (Exception exception) {
                this.string = "";
            }
        }

        public static void onRender(DrawContext context, List<Info> list, boolean fromUp) {
            counter = 20.0;
            Info.fromUp = fromUp;
            fontHeight = INSTANCE.getFontHeight();
            currentY = fromUp ? 1.0 + HUD.INSTANCE.yOffset.getValue() : (double)Wrapper.mc.getWindow().getScaledHeight() - fontHeight - 1.0 - (Wrapper.mc.currentScreen instanceof ChatScreen && HUD.INSTANCE.yOffset.getValue() < 12.0 ? 12.0 - HUD.INSTANCE.yOffset.getValue() + HUD.INSTANCE.interval.getValue() / 2.0 : 0.0) - HUD.INSTANCE.yOffset.getValue();
            windowWidth = Wrapper.mc.getWindow().getScaledWidth();
            for (Info s : list) {
                s.draw(context);
            }
        }

        public static void onUpdate(List<Info> list, boolean sort) {
            for (Info s : list) {
                s.onUpdate();
            }
            if (sort) {
                list.sort(Comparator.comparingInt(info -> info.string == null ? 0 : -INSTANCE.getWidth(info.string)));
            }
        }

        public void onUpdate() {
            this.isOn = this.drawn.getAsBoolean();
            if (this.isOn) {
                try {
                    String s = this.info.call();
                    if (s == null) {
                        s = "";
                    }
                    this.string = HUD.this.lowerCase.getValue() ? s.toLowerCase() : s;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    this.string = "";
                }
            }
        }

        public void draw(DrawContext context) {
            if (this.currentX > 0.0 || this.isOn) {
                String text = this.string == null ? "" : this.string;
                this.currentX = this.animation.get(this.isOn ? (double)(HUD.this.getWidth(text) + 1) : 0.0, this.isOn ? (long)HUD.this.enableLength.getValueInt() : (long)HUD.this.disableLength.getValueInt(), HUD.this.easing.getValue());
                double width = this.currentX + (double)HUD.this.xOffset.getValueFloat();
                double fade = this.fadeAnimation.get(this.isOn ? 1.0 : 0.0, HUD.this.fadeLength.getValueInt(), HUD.this.easing.getValue());
                if (fade > 0.04) {
                    int c = ColorUtil.injectAlpha(HUD.this.getColor(counter += fromUp ? fade : -fade), (int)((double)HUD.this.color.getValue().getAlpha() * fade));
                    if (HUD.this.blur.getValue()) {
                        Frog.BLUR.applyBlur((float)(HUD.this.radius.getValue() * fade), (float)((double)windowWidth - width - (double)(HUD.this.width.getValueFloat() / 2.0f)), (float)currentY - 1.0f - HUD.this.interval.getValueFloat() / 2.0f, (float)width + HUD.this.width.getValueFloat() - HUD.this.xOffset.getValueFloat(), (float)fontHeight + HUD.this.interval.getValueFloat());
                    }
                    if (HUD.this.backGround.getValue()) {
                        Render2DUtil.drawRect(context.getMatrices(), (float)((double)windowWidth - width - (double)(HUD.this.width.getValueFloat() / 2.0f)), (float)currentY - 1.0f - HUD.this.interval.getValueFloat() / 2.0f, (float)width + HUD.this.width.getValueFloat() - HUD.this.xOffset.getValueFloat(), (float)fontHeight + HUD.this.interval.getValueFloat(), ColorUtil.injectAlpha(HUD.this.bgColor.rainbow ? c : HUD.this.bgColor.getValue().getRGB(), (int)((double)HUD.this.bgColor.getValue().getAlpha() * fade)));
                    }
                    if (HUD.this.glow.booleanValue) {
                        Render2DUtil.drawGlow(context.getMatrices(), (float)((double)windowWidth - width - (double)(HUD.this.width.getValueFloat() / 2.0f)), (float)currentY - 1.0f - HUD.this.interval.getValueFloat() / 2.0f, (float)width + HUD.this.width.getValueFloat() - HUD.this.xOffset.getValueFloat(), (float)fontHeight + HUD.this.interval.getValueFloat(), ColorUtil.injectAlpha(HUD.this.glow.rainbow ? c : HUD.this.glow.getValue().getRGB(), (int)((double)HUD.this.glow.getValue().getAlpha() * fade)));
                    }
                    TextUtil.drawString(context, this.string, (double)windowWidth - width, currentY + (double)HUD.this.textOffset.getValueFloat(), c, HUD.this.font.getValue(), HUD.this.shadow.getValue());
                    if (HUD.this.rect.booleanValue) {
                        Render2DUtil.drawRect(context.getMatrices(), (float)windowWidth + HUD.this.width.getValueFloat() / 2.0f - HUD.this.xOffset.getValueFloat(), (float)currentY - 1.0f - HUD.this.interval.getValueFloat() / 2.0f, 1.0f, (float)fontHeight + HUD.this.interval.getValueFloat(), HUD.this.rect.rainbow ? c : ColorUtil.injectAlpha(HUD.this.rect.getValue(), (int)((double)HUD.this.rect.getValue().getAlpha() * fade)).getRGB());
                    }
                    currentY += fromUp ? (fontHeight + HUD.this.interval.getValue()) * fade : -(fontHeight + HUD.this.interval.getValue()) * fade;
                }
            }
        }
    }
}
