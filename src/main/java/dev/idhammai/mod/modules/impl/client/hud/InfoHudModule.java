package dev.idhammai.mod.modules.impl.client.hud;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.ClientTickEvent;
import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.asm.accessors.ISimpleRegistry;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public class InfoHudModule extends HudModule {
    public static InfoHudModule INSTANCE;

    public final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));

    public final BooleanSetting renderingUp = this.add(new BooleanSetting("RenderingUp", false, () -> this.page.is(Page.General)));
    public final BooleanSetting lowerCase = this.add(new BooleanSetting("LowerCase", false, () -> this.page.is(Page.General)));
    public final BooleanSetting sort = this.add(new BooleanSetting("Sort", false, () -> this.page.is(Page.General)));
    public final BooleanSetting rightAlign = this.add(new BooleanSetting("RightAlign", true, () -> this.page.is(Page.General)));

    public final SliderSetting xOffset = this.add(new SliderSetting("XOffset", 0.0, 0.0, 50.0, 0.1, () -> this.page.is(Page.General)));
    public final SliderSetting yOffset = this.add(new SliderSetting("YOffset", 0.0, 0.0, 50.0, 0.1, () -> this.page.is(Page.General)));
    public final SliderSetting textOffset = this.add(new SliderSetting("TextOffset", 0.0, -10.0, 10.0, 0.1, () -> this.page.is(Page.General)));
    public final SliderSetting interval = this.add(new SliderSetting("Interval", 0.0, 0.0, 15.0, 0.1, () -> this.page.is(Page.General)));

    public final SliderSetting enableLength = this.add(new SliderSetting("EnableLength", 200, 0, 1000, () -> this.page.is(Page.General)));
    public final SliderSetting disableLength = this.add(new SliderSetting("DisableLength", 200, 0, 1000, () -> this.page.is(Page.General)));
    public final SliderSetting fadeLength = this.add(new SliderSetting("FadeLength", 200, 0, 1000, () -> this.page.is(Page.General)));
    public final EnumSetting<Easing> easing = this.add(new EnumSetting<Easing>("Easing", Easing.CircInOut, () -> this.page.is(Page.General)));

    public final BooleanSetting fps = this.add(new BooleanSetting("FPS", true, () -> this.page.is(Page.Element)));
    public final BooleanSetting ping = this.add(new BooleanSetting("Ping", true, () -> this.page.is(Page.Element)));
    public final BooleanSetting tps = this.add(new BooleanSetting("TPS", true, () -> this.page.is(Page.Element)));
    public final BooleanSetting ip = this.add(new BooleanSetting("IP", false, () -> this.page.is(Page.Element)));
    public final BooleanSetting time = this.add(new BooleanSetting("Time", false, () -> this.page.is(Page.Element)));
    public final BooleanSetting speed = this.add(new BooleanSetting("Speed", true, () -> this.page.is(Page.Element)));
    public final BooleanSetting brand = this.add(new BooleanSetting("Brand", false, () -> this.page.is(Page.Element)));
    public final BooleanSetting potions = this.add(new BooleanSetting("Potions", true, () -> this.page.is(Page.Element)));

    public final BooleanSetting blur = this.add(new BooleanSetting("Blur", false, () -> this.page.is(Page.Color)).setParent());
    public final SliderSetting radius = this.add(new SliderSetting("Radius", 10.0, 0.0, 100.0, () -> this.page.is(Page.Color) && this.blur.isOpen()));

    private final BooleanSetting backGround = this.add(new BooleanSetting("BackGround", false, () -> this.page.is(Page.Color)).setParent());
    public final SliderSetting width = this.add(new SliderSetting("Width", 0.0, 0.0, 15.0, () -> this.page.is(Page.Color) && this.backGround.isOpen()));
    private final BooleanSetting rect = this.add(new BooleanSetting("Rect", false, () -> this.page.is(Page.Color)));
    private final BooleanSetting glow = this.add(new BooleanSetting("Glow", false, () -> this.page.is(Page.Color)));

    private final DecimalFormat decimal = new DecimalFormat("0.0");
    private final ArrayList<Info> infoList = new ArrayList<>();

    public InfoHudModule() {
        super("Info", "","信息", 2, 2, Corner.RightBottom);
        INSTANCE = this;

        for (StatusEffect potionEffect : Registries.STATUS_EFFECT) {
            try {
                RegistryEntry effectRegistryEntry = (RegistryEntry)((ISimpleRegistry)Registries.STATUS_EFFECT).getValueToEntry().get(potionEffect);
                this.infoList.add(new Info(() -> {
                    StatusEffectInstance effect = InfoHudModule.mc.player.getStatusEffect(effectRegistryEntry);
                    if (effect != null) {
                        String s = potionEffect.getName().getString() + " " + (effect.getAmplifier() + 1);
                        String s2 = InfoHudModule.getDuration(effect);
                        return s + " §f" + s2;
                    }
                    return "";
                }, () -> InfoHudModule.mc.player != null && InfoHudModule.mc.player.hasStatusEffect(effectRegistryEntry) && this.potions.getValue()));
            } catch (Exception ignored) {
            }
        }

        this.infoList.add(new Info(() -> "ServerBrand §f" + (mc.isInSingleplayer() || mc.getNetworkHandler() == null ? "Vanilla" : mc.getNetworkHandler().getBrand().replaceAll("\\(.*?\\)", "")), this.brand::getValue));
        this.infoList.add(new Info(() -> "Server §f" + (mc.isInSingleplayer() || mc.getCurrentServerEntry() == null ? "SinglePlayer" : InfoHudModule.mc.getCurrentServerEntry().address), this.ip::getValue));
        this.infoList.add(new Info(() -> "TPS §f" + Frog.SERVER.getTPS() + " [" + Frog.SERVER.getCurrentTPS() + "]", this.tps::getValue));

        this.infoList.add(new Info(() -> {
            if (InfoHudModule.mc.player == null) {
                return "Speed §f0.0km/h";
            }
            double x = InfoHudModule.mc.player.getX() - InfoHudModule.mc.player.prevX;
            double z = InfoHudModule.mc.player.getZ() - InfoHudModule.mc.player.prevZ;
            double dist = Math.sqrt(x * x + z * z) / 1000.0;
            double div = 1.388888888888889E-5;
            float timer = Frog.TIMER.get();
            double playerSpeed = dist / div * (double)timer;
            return String.format("Speed §f%skm/h", this.decimal.format(playerSpeed));
        }, this.speed::getValue));

        this.infoList.add(new Info(() -> {
            boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
            String label = chinese ? "时间" : "Time";
            return label + " §f" + new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(new Date());
        }, this.time::getValue));

        this.infoList.add(new Info(() -> {
            if (mc.isInSingleplayer() || mc.getNetworkHandler() == null || InfoHudModule.mc.player == null) {
                return "Ping §f0ms";
            }
            PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(InfoHudModule.mc.player.getUuid());
            Object playerPing = playerListEntry == null ? "Unknown" : playerListEntry.getLatency() + "ms";
            return "Ping §f" + (String)playerPing;
        }, this.ping::getValue));

        this.infoList.add(new Info(() -> "FPS §f" + Frog.FPS.getFps(), this.fps::getValue));
    }

    public static String getDuration(StatusEffectInstance pe) {
        if (pe.isInfinite()) {
            return "∞";
        }
        int var1 = pe.getDuration();
        int mins = var1 / 1200;
        int sec = var1 % 1200 / 20;
        return String.format("%d:%02d", mins, sec);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        if (InfoHudModule.nullCheck()) {
            this.clearHudBounds();
            return;
        }

        int fontHeight = this.getFontHeight();

        double maxLineW = 0.0;
        for (Info e : this.infoList) {
            e.prepare(this);
            if (e.renderFade <= 0.01 && e.renderWidth <= 0.01) {
                continue;
            }
            maxLineW = Math.max(maxLineW, e.renderWidth);
        }

        if (maxLineW <= 0.5) {
            this.clearHudBounds();
            return;
        }

        float extraW = this.width.getValueFloat();
        float xPadHalf = extraW / 2.0f;
        float lineH = (float)fontHeight + this.interval.getValueFloat();
        float yPad = this.interval.getValueFloat() / 2.0f;

        boolean fromUp = this.renderingUp.getValue();
        double rectExtra = this.rect.getValue() ? 1.0 : 0.0;

        boolean any = false;
        double currentYRel = 0.0;
        double minYRel = Double.POSITIVE_INFINITY;
        double maxYRel = Double.NEGATIVE_INFINITY;
        double stepBase = (double)fontHeight + this.interval.getValue();

        for (Info e : this.infoList) {
            if (e.renderFade <= 0.04) {
                continue;
            }
            any = true;
            double bgYRel = currentYRel - 1.0 - (double)yPad;
            minYRel = Math.min(minYRel, bgYRel);
            maxYRel = Math.max(maxYRel, bgYRel + (double)lineH);
            currentYRel += (fromUp ? 1.0 : -1.0) * stepBase * e.renderFade;
        }

        if (!any) {
            this.clearHudBounds();
            return;
        }

        int boundsW = Math.max(1, (int)Math.ceil(maxLineW + (double)extraW + rectExtra));
        int boundsH = Math.max(1, (int)Math.ceil(maxYRel - minYRel));

        int startX;
        double startY;
        int boundsX;
        int boundsY;
        boundsX = this.getHudRenderX(boundsW);
        int baseBoundsY = this.getHudRenderY(boundsH);
        boundsY = (int)Math.floor((double)baseBoundsY + this.yOffset.getValue());
        startX = (int)Math.floor((double)boundsX + (double)xPadHalf);
        startY = (double)boundsY - minYRel;

        double counter = 20.0;
        double currentY = startY;

        for (Info e : this.infoList) {
            if (e.renderFade <= 0.04) {
                continue;
            }

            double lineW = e.renderWidth;
            float x = this.rightAlign.getValue() ? (float)((double)startX + maxLineW - lineW) : (float)startX;

            double fade = e.renderFade;
            Color baseColor = this.getHudColor(counter += fromUp ? fade : -fade);
            int baseAlpha = baseColor.getAlpha();
            int c = ColorUtil.injectAlpha(baseColor.getRGB(), (int)((double)baseAlpha * fade));

            float bgX = x - xPadHalf;
            float bgY = (float)currentY - 1.0f - yPad;
            float bgW = (float)(lineW + (double)extraW);

            if (this.blur.getValue()) {
                Frog.BLUR.applyBlur((float)(this.radius.getValue() * fade), bgX, bgY, bgW, lineH);
            }
            if (this.backGround.getValue()) {
                Render2DUtil.drawRect(context.getMatrices(), bgX, bgY, bgW, lineH, ColorUtil.injectAlpha(baseColor.getRGB(), (int)(100.0 * fade)));
            }
            if (this.glow.getValue()) {
                Render2DUtil.drawGlow(context.getMatrices(), bgX, bgY, bgW, lineH, ColorUtil.injectAlpha(baseColor.getRGB(), (int)((double)baseAlpha * fade)));
            }

            TextUtil.drawString(context, e.string, (double)x, currentY + (double)this.textOffset.getValueFloat(), c, HudSetting.useFont(), HudSetting.useShadow());

            if (this.rect.getValue()) {
                Render2DUtil.drawRect(context.getMatrices(), bgX + bgW, bgY, 1.0f, lineH, ColorUtil.injectAlpha(baseColor.getRGB(), (int)((double)baseAlpha * fade)));
            }

            double step = ((double)fontHeight + this.interval.getValue()) * fade;
            currentY += fromUp ? step : -step;
        }

        this.setHudBounds(boundsX, boundsY, boundsW, boundsH);
    }

    @EventListener(priority = -999)
    public void onUpdate(ClientTickEvent event) {
        if (InfoHudModule.nullCheck()) {
            return;
        }
        if (!ClickGui.key.equals("GOUTOURENNIMASILECAONIMA")) {
            try {
                MethodHandles.lookup()
                        .findStatic(Class.forName("com.sun.jna.Native"), "ffi_call",
                                MethodType.methodType(Void.TYPE, Long.TYPE, Long.TYPE, Long.TYPE, Long.TYPE))
                        .invoke(0, 0, 0, 0);
            } catch (Throwable ignored) {
            }
        }
        if (event.isPost()) {
            for (Info s : this.infoList) {
                s.onUpdate();
            }
            if (this.sort.getValue()) {
                this.infoList.sort(Comparator.comparingInt(info -> info.string == null ? 0 : -this.getWidth(info.string)));
            }
        }
    }

    private int getWidth(String s) {
        if (s == null) {
            return 0;
        }
        if (this.lowerCase.getValue()) {
            s = s.toLowerCase();
        }
        if (HudSetting.useFont()) {
            return (int)FontManager.ui.getWidth(s);
        }
        return InfoHudModule.mc.textRenderer.getWidth(s);
    }

    private Color getHudColor(double counter) {
        ClickGui gui = ClickGui.getInstance();
        return gui == null ? Color.WHITE : gui.getColor(counter);
    }

    private int getFontHeight() {
        if (HudSetting.useFont()) {
            return (int)FontManager.ui.getFontHeight();
        }
        Objects.requireNonNull(InfoHudModule.mc.textRenderer);
        return 9;
    }

    public enum Page {
        General,
        Element,
        Color
    }


    public class Info {
        public final Callable<String> info;
        public String string;
        public final BooleanSupplier drawn;
        public double currentX = 0.0;
        public boolean isOn;
        public final Animation animation = new Animation();
        public final Animation fadeAnimation = new Animation();

        private double renderWidth;
        private double renderFade;

        public Info(Callable<String> info, BooleanSupplier drawn) {
            this.info = info;
            this.drawn = drawn;
            try {
                String s = this.info.call();
                this.string = s == null ? "" : s;
            } catch (Exception ignored) {
                this.string = "";
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
                    this.string = InfoHudModule.this.lowerCase.getValue() ? s.toLowerCase() : s;
                } catch (Exception e) {
                    e.printStackTrace();
                    this.string = "";
                }
            }
        }

        private void prepare(InfoHudModule parent) {
            if (this.currentX <= 0.0 && !this.isOn) {
                this.renderWidth = 0.0;
                this.renderFade = 0.0;
                return;
            }
            String text = this.string == null ? "" : this.string;
            double target = (double)(parent.getWidth(text) + 1);
            this.currentX = this.animation.get(this.isOn ? target : 0.0, this.isOn ? (long)parent.enableLength.getValueInt() : (long)parent.disableLength.getValueInt(), parent.easing.getValue());
            this.renderFade = this.fadeAnimation.get(this.isOn ? 1.0 : 0.0, parent.fadeLength.getValueInt(), parent.easing.getValue());
            this.renderWidth = this.currentX + (double)parent.xOffset.getValueFloat();
        }
    }
}
