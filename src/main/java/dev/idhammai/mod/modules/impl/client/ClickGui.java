/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.sound.PositionedSoundInstance
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.sound.SoundEvents
 */
package dev.idhammai.mod.modules.impl.client;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.Render2DEvent;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.mod.gui.ClickGuiScreen;
import dev.idhammai.mod.gui.items.Component;
import dev.idhammai.mod.gui.items.Item;
import dev.idhammai.mod.gui.items.buttons.Button;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;

public class ClickGui
extends Module {
    private static ClickGui INSTANCE;
    public final EnumSetting<Style> style = this.add(new EnumSetting<Style>("Style", Style.Dark).injectTask(this::updateStyle));
    public final BooleanSetting autoSave = this.add(new BooleanSetting("AutoSave", true));
    public final BooleanSetting font = this.add(new BooleanSetting("Font", true));
    public final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    public final BooleanSetting disableNotification = this.add(new BooleanSetting("DisableNotification", false));
    public final BooleanSetting sound = this.add(new BooleanSetting("Sound", true).setParent());
    public final SliderSetting soundPitch = this.add(new SliderSetting("SoundPitch", 1.0, 0.0, 2.0, 0.1, this.sound::isOpen));
    public final BooleanSetting guiSound = this.add(new BooleanSetting("GuiSound", true));
    public final SliderSetting height = this.add(new SliderSetting("Height", 3, 0, 7).injectTask(this::applyHeights));
    public final SliderSetting categoryWidth = this.add(new SliderSetting("CategoryWidth", 93, 60, 160).injectTask(this::applyHeights));
    public final SliderSetting categoryBarHeight = this.add(new SliderSetting("CategoryBarHeight", 13, 8, 30).injectTask(this::applyHeights));
    public final SliderSetting textOffset = this.add(new SliderSetting("TextOffset", 0.0, -5.0, 5.0, 1.0));
    public final SliderSetting titleOffset = this.add(new SliderSetting("TitleOffset", 1, -5.0, 5.0, 1.0));
    public final SliderSetting alpha = this.add(new SliderSetting("Alpha", 150, 0, 255));
    public final SliderSetting hoverAlpha = this.add(new SliderSetting("HoverAlpha", 220, 0, 255));
    public final SliderSetting topAlpha = this.add(new SliderSetting("TopAlpha", 128, 0, 255));
    public final SliderSetting backgroundAlpha = this.add(new SliderSetting("BackgroundAlpha", 236, 0, 255));
    public final BooleanSetting fade = this.add(new BooleanSetting("Fade", true).setParent());
    public final SliderSetting length = this.add(new SliderSetting("Length", 400, 0, 1000, this.fade::isOpen));
    public final EnumSetting<Easing> easing = this.add(new EnumSetting<Easing>("Easing", Easing.BackInOut, this.fade::isOpen));
    public final BooleanSetting scrollAnim = this.add(new BooleanSetting("ScrollAnim", true).setParent());
    public final SliderSetting scrollAnimLength = this.add(new SliderSetting("ScrollAnimLength", 220, 1, 1000, this.scrollAnim::isOpen));
    public final EnumSetting<Easing> scrollAnimEasing = this.add(new EnumSetting<Easing>("ScrollAnimEasing", Easing.SineOut, this.scrollAnim::isOpen));
    public final BooleanSetting mouseMove = this.add(new BooleanSetting("MouseMove", false).setParent());
    public final SliderSetting mouseMoveStrength = this.add(new SliderSetting("MouseMoveStrength", 6.0, 0.0, 30.0, 0.5, this.mouseMove::isOpen));
    public final SliderSetting mouseMoveSmooth = this.add(new SliderSetting("MouseMoveSmooth", 10.0, 0.0, 30.0, 0.5, this.mouseMove::isOpen));
    public final BooleanSetting walkShake = this.add(new BooleanSetting("WalkShake", false).setParent());
    public final SliderSetting walkShakeStrength = this.add(new SliderSetting("WalkShakeStrength", 8.0, 0.0, 20.0, 0.5, this.walkShake::isOpen));
    public final SliderSetting walkShakeSpeed = this.add(new SliderSetting("WalkShakeSpeed", 12.0, 0.0, 30.0, 0.5, this.walkShake::isOpen));
    public final SliderSetting walkShakeSmooth = this.add(new SliderSetting("WalkShakeSmooth", 14.0, 0.0, 30.0, 0.5, this.walkShake::isOpen));
    public final SliderSetting walkShakeMax = this.add(new SliderSetting("WalkShakeMax", 8.0, 0.0, 30.0, 0.5, this.walkShake::isOpen));
    public final BooleanSetting blur = this.add(new BooleanSetting("Blur", true).setParent());
    public final EnumSetting<BlurType> blurType = this.add(new EnumSetting<BlurType>("BlurType", BlurType.Radial, this.blur::isOpen));
    public final SliderSetting radius = this.add(new SliderSetting("Radius", 5.0, 0.0, 100.0, this.blur::isOpen));
    public final BooleanSetting elements = this.add(new BooleanSetting("Elements", false).setParent().injectTask(this::keyCodec));
    public final BooleanSetting line = this.add(new BooleanSetting("Line", true, this.elements::isOpen));
    public final ColorSetting gear = this.add(new ColorSetting("Gear", -1, this.elements::isOpen).injectBoolean(false));
    public final EnumSetting<ExpandIcon> expandIcon = this.add(new EnumSetting<ExpandIcon>("ExpandIcon", ExpandIcon.PlusMinus, this.elements::isOpen));
    public final BooleanSetting colors = this.add(new BooleanSetting("Colors", false).setParent().injectTask(this::elementCodec));
    public final EnumSetting<ColorMode> colorMode = this.add(new EnumSetting<ColorMode>("ColorMode", ColorMode.Custom, this.colors::isOpen));
    public final SliderSetting rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 4.0, 1.0, 10.0, 0.1, () -> this.colors.isOpen() && this.colorMode.getValue() == ColorMode.Rainbow));
    public final SliderSetting saturation = this.add(new SliderSetting("Saturation", 130.0, 1.0, 255.0, () -> this.colors.isOpen() && this.colorMode.getValue() == ColorMode.Rainbow));
    public final SliderSetting rainbowDelay = this.add(new SliderSetting("Delay", 350, 0, 1000, () -> this.colors.isOpen() && this.colorMode.getValue() == ColorMode.Rainbow));
    public final ColorSetting color = this.add(new ColorSetting("FirstColor", new Color(0, 120, 212), this.colors::isOpen));
    public final ColorSetting secondColor = this.add(new ColorSetting("SecondColor", new Color(255, 0, 0, 255), () -> this.colors.isOpen() && this.colorMode.getValue() == ColorMode.Pulse).injectBoolean(true));
    public final SliderSetting pulseSpeed = this.add(new SliderSetting("PulseSpeed", 1.0, 0.0, 5.0, 0.1, () -> this.colors.isOpen() && this.colorMode.getValue() == ColorMode.Pulse));
    public final SliderSetting pulseCounter = this.add(new SliderSetting("Counter", 10, 1, 50, () -> this.colors.isOpen() && this.colorMode.getValue() == ColorMode.Pulse));
    public final ColorSetting activeColor = this.add(new ColorSetting("ActiveColor", new Color(0, 120, 212), this.colors::isOpen));
    public final ColorSetting hoverColor = this.add(new ColorSetting("HoverColor", new Color(50, 50, 50, 200), this.colors::isOpen));
    public final ColorSetting defaultColor = this.add(new ColorSetting("DefaultColor", new Color(30, 30, 30, 236), this.colors::isOpen));
    public final ColorSetting defaultTextColor = this.add(new ColorSetting("DefaultTextColor", new Color(220, 220, 220), this.colors::isOpen));
    public final ColorSetting enableTextColor = this.add(new ColorSetting("EnableTextColor", new Color(255, 255, 255), this.colors::isOpen));
    public final ColorSetting backGround = this.add(new ColorSetting("BackGround", new Color(30, 30, 30, 236), this.colors::isOpen).injectBoolean(true));
    public final ColorSetting tint = this.add(new ColorSetting("Tint", new Color(12, 60, 95, 56)).injectBoolean(true));
    public final ColorSetting endColor = this.add(new ColorSetting("End", new Color(255, 120, 240, 72), () -> this.tint.booleanValue));
    public double alphaValue;
    private final Animation animation = new Animation();
    public static String key;
    private boolean styleApplied = false;

    public ClickGui() {
        super("ClickGui", Module.Category.Client);
        this.setChinese("\u70b9\u51fb\u754c\u9762");
        INSTANCE = this;
        Frog.EVENT_BUS.subscribe(new FadeOut());
    }

    public static ClickGui getInstance() {
        return INSTANCE;
    }

    public void keyCodec() {
        this.elements.setValueWithoutTask(false);
        this.elements.setOpen(!this.elements.isOpen());
    }

    public void elementCodec() {
        this.colors.setValueWithoutTask(false);
        this.colors.setOpen(!this.colors.isOpen());
    }

    private void applyHeights() {
        java.util.ArrayList<Component> components = ClickGuiScreen.getInstance().getComponents();
        boolean defaultLayout = true;
        int expectedX = 10;
        for (int i = 0; i < components.size(); ++i) {
            Component component = components.get(i);
            if (component.getX() != expectedX || component.getY() != 4) {
                defaultLayout = false;
                break;
            }
            expectedX += 94;
        }
        int categoryWidth = this.categoryWidth.getValueInt();
        int componentHeight = this.categoryBarHeight.getValueInt() + 5;
        int x = 10;
        for (int i = 0; i < components.size(); ++i) {
            Component component = components.get(i);
            component.setWidth(categoryWidth);
            component.setHeight(componentHeight);
            if (defaultLayout) {
                component.setX(x);
                component.setY(4);
                x += categoryWidth + 1;
            }
            for (Item item : component.getItems()) {
                item.setHeight(10 + this.height.getValueInt());
            }
        }
    }

    @Override
    public void onEnable() {
        if (ClickGui.nullCheck()) {
            this.disable();
            return;
        }
        if (!key.equals("GOUTOURENNIMASILECAONIMA")) {
            try {
                MethodHandles.lookup().findStatic(Class.forName("com.sun.jna.Native"), "ffi_call", MethodType.methodType(Void.TYPE, Long.TYPE, Long.TYPE, Long.TYPE, Long.TYPE)).invoke(0, 0, 0, 0);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        this.updateColor();
        if (this.guiSound.getValue() && mc.getSoundManager() != null) {
            mc.getSoundManager().play((SoundInstance)PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, (float)this.soundPitch.getValueFloat()));
        }
        this.applyHeights();
        mc.setScreen((Screen)ClickGuiScreen.getInstance());
    }

    @Override
    public void onDisable() {
        if (ClickGui.mc.currentScreen instanceof ClickGuiScreen) {
            ClickGui.mc.currentScreen.close();
        }
        if (this.guiSound.getValue() && mc.getSoundManager() != null) {
            mc.getSoundManager().play((SoundInstance)PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, (float)this.soundPitch.getValueFloat()));
        }
        if (this.autoSave.getValue()) {
            Frog.save();
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!this.styleApplied) {
            this.updateStyle();
            this.styleApplied = true;
        }
        this.updateColor();
        if (!(ClickGui.mc.currentScreen instanceof ClickGuiScreen)) {
            this.disable();
        }
    }

    public void updateColor() {
        Button.hoverColor = this.hoverColor.getValue().getRGB();
        Button.defaultTextColor = this.defaultTextColor.getValue().getRGB();
        Button.defaultColor = this.defaultColor.getValue().getRGB();
        Button.enableTextColor = this.enableTextColor.getValue().getRGB();
    }

    public Color getColor() {
        return this.getColor(0.0);
    }

    public Color getColor(double delay) {
        return this.getModeColor(this.color.getValue(), delay);
    }

    public Color getActiveColor() {
        return this.getActiveColor(0.0);
    }

    public Color getActiveColor(double delay) {
        return this.getModeColor(this.activeColor.getValue(), delay);
    }

    private Color getModeColor(Color customColor, double delay) {
        if (this.colorMode.getValue() == ColorMode.Custom) {
            return customColor;
        }
        return this.dynamicColor(delay);
    }

    private Color dynamicColor(double delay) {
        if (this.colorMode.getValue() == ColorMode.Pulse) {
            if (this.secondColor.booleanValue) {
                return ColorUtil.pulseColor(this.color.getValue(), this.secondColor.getValue(), delay, this.pulseCounter.getValueInt(), this.pulseSpeed.getValue());
            }
            return ColorUtil.pulseColor(this.color.getValue(), delay, this.pulseCounter.getValueInt(), this.pulseSpeed.getValue());
        }
        if (this.colorMode.getValue() == ColorMode.Rainbow) {
            double rainbowState = Math.ceil(((double)System.currentTimeMillis() * this.rainbowSpeed.getValue() + delay * this.rainbowDelay.getValue()) / 20.0);
            return Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), this.saturation.getValueFloat() / 255.0f, 1.0f);
        }
        return this.color.getValue();
    }

    public void updateStyle() {
        this.color.setValue(new Color(0, 120, 212));
        this.hoverColor.setValue(new Color(50, 50, 50, 200));
        this.defaultColor.setValue(new Color(30, 30, 30, 236));
        this.defaultTextColor.setValue(new Color(220, 220, 220));
        this.enableTextColor.setValue(new Color(255, 255, 255));
        this.backGround.setValue(new Color(30, 30, 30, 236));
        this.tint.setValue(new Color(0, 120, 212, 36));
        this.endColor.setValue(new Color(0, 120, 212, 18));
    }

    public Color dynamicColor(int delay) {
        return this.dynamicColor((double)delay);
    }

    public enum Style {
        Dark
    }

    public enum ColorMode {
        Custom,
        Pulse,
        Rainbow
    }

    public enum ExpandIcon {
        PlusMinus,
        Chevron,
        Gear
    }

    public enum BlurType {
        Box,
        Tent,
        Gaussian,
        Kawase,
        Radial
    }

    static {
        key = "";
    }

    public class FadeOut {
        @EventListener(priority=-99999)
        public void onRender2D(Render2DEvent event) {
            if (ClickGui.this.fade.getValue()) {
                if (ClickGui.this.alphaValue > 0.0 || ClickGui.this.isOn()) {
                    ClickGui.this.alphaValue = ClickGui.this.animation.get(ClickGui.this.isOn() ? 1.0 : 0.0, ClickGui.this.length.getValueInt(), ClickGui.this.easing.getValue());
                }
                if (ClickGui.this.alphaValue > 0.0 && !(Wrapper.mc.currentScreen instanceof ClickGuiScreen)) {
                    event.drawContext.getMatrices().push();
                    event.drawContext.getMatrices().translate(0.0f, 0.0f, 5000.0f);
                    ClickGuiScreen.getInstance().render(event.drawContext, 0, 0, event.tickDelta);
                    event.drawContext.getMatrices().pop();
                }
            } else {
                ClickGui.this.alphaValue = 1.0;
            }
        }
    }
}

