/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  org.lwjgl.glfw.GLFW
 */
package dev.idhammai.mod.gui.items.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.core.impl.CommandManager;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class PickerButton
extends Button {
    static MatrixStack matrixStack;
    private final ColorSetting setting;
    boolean pickingColor;
    boolean pickingHue;
    boolean pickingAlpha;
    boolean open;
    private final Animation openAnimation = new Animation();
    private final Animation pickerCursorXAnimation = new Animation();
    private final Animation pickerCursorYAnimation = new Animation();
    private final Animation hueCursorAnimation = new Animation();
    private final Animation alphaCursorAnimation = new Animation();
    private final Animation toggleAnimation = new Animation();
    private boolean indicatorsInitialized;
    float[] hsb = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

    public PickerButton(ColorSetting setting) {
        super(setting.getName());
        this.setting = setting;
        double initial = this.getState() ? 1.0 : 0.0;
        this.toggleAnimation.from = initial;
        this.toggleAnimation.to = initial;
    }

    public static boolean mouseOver(int minX, int minY, int maxX, int maxY, int mX, int mY) {
        return mX >= minX && mY >= minY && mX <= maxX && mY <= maxY;
    }

    public static Color getColor(Color color, float alpha) {
        float red = (float)color.getRed() / 255.0f;
        float green = (float)color.getGreen() / 255.0f;
        float blue = (float)color.getBlue() / 255.0f;
        return new Color(red, green, blue, alpha);
    }

    public static void drawPickerBase(int pickerX, int pickerY, int pickerWidth, int pickerHeight, float red, float green, float blue, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)pickerX, (float)pickerY, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)pickerX, (float)(pickerY + pickerHeight), 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)(pickerX + pickerWidth), (float)(pickerY + pickerHeight), 0.0f).color(red, green, blue, 1.0f);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)(pickerX + pickerWidth), (float)pickerY, 0.0f).color(red, green, blue, 1.0f);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)pickerX, (float)pickerY, 0.0f).color(0.0f, 0.0f, 0.0f, 0.0f);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)pickerX, (float)(pickerY + pickerHeight), 0.0f).color(0.0f, 0.0f, 0.0f, 1.0f);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)(pickerX + pickerWidth), (float)(pickerY + pickerHeight), 0.0f).color(0.0f, 0.0f, 0.0f, 1.0f);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)(pickerX + pickerWidth), (float)pickerY, 0.0f).color(0.0f, 0.0f, 0.0f, 0.0f);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void drawLeftGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)right, (float)top, 0.0f).color((float)(endColor >> 24 & 0xFF) / 255.0f, (float)(endColor >> 16 & 0xFF) / 255.0f, (float)(endColor >> 8 & 0xFF) / 255.0f, (float)(endColor >> 24 & 0xFF) / 255.0f);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)left, (float)top, 0.0f).color((float)(startColor >> 16 & 0xFF) / 255.0f, (float)(startColor >> 8 & 0xFF) / 255.0f, (float)(startColor & 0xFF) / 255.0f, (float)(startColor >> 24 & 0xFF) / 255.0f);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)left, (float)bottom, 0.0f).color((float)(startColor >> 16 & 0xFF) / 255.0f, (float)(startColor >> 8 & 0xFF) / 255.0f, (float)(startColor & 0xFF) / 255.0f, (float)(startColor >> 24 & 0xFF) / 255.0f);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)right, (float)bottom, 0.0f).color((float)(endColor >> 24 & 0xFF) / 255.0f, (float)(endColor >> 16 & 0xFF) / 255.0f, (float)(endColor >> 8 & 0xFF) / 255.0f, (float)(endColor >> 24 & 0xFF) / 255.0f);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void gradient(int minX, int minY, int maxX, int maxY, int startColor, int endColor, boolean left) {
        float startA = (float)(startColor >> 24 & 0xFF) / 255.0f;
        float startR = (float)(startColor >> 16 & 0xFF) / 255.0f;
        float startG = (float)(startColor >> 8 & 0xFF) / 255.0f;
        float startB = (float)(startColor & 0xFF) / 255.0f;
        float endA = (float)(endColor >> 24 & 0xFF) / 255.0f;
        float endR = (float)(endColor >> 16 & 0xFF) / 255.0f;
        float endG = (float)(endColor >> 8 & 0xFF) / 255.0f;
        float endB = (float)(endColor & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)minX, (float)minY, 0.0f).color(startR, startG, startB, startA);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)minX, (float)maxY, 0.0f).color(startR, startG, startB, startA);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)maxX, (float)maxY, 0.0f).color(endR, endG, endB, endA);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)maxX, (float)minY, 0.0f).color(endR, endG, endB, endA);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static int gradientColor(int color, int percentage) {
        int r = ((color & 0xFF0000) >> 16) * (100 + percentage) / 100;
        int g = ((color & 0xFF00) >> 8) * (100 + percentage) / 100;
        int elementCodec = (color & 0xFF) * (100 + percentage) / 100;
        return new Color(r, g, elementCodec).hashCode();
    }

    public static void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor, boolean hovered) {
        if (hovered) {
            startColor = PickerButton.gradientColor(startColor, -20);
            endColor = PickerButton.gradientColor(endColor, -20);
        }
        float c = (float)(startColor >> 24 & 0xFF) / 255.0f;
        float c1 = (float)(startColor >> 16 & 0xFF) / 255.0f;
        float c2 = (float)(startColor >> 8 & 0xFF) / 255.0f;
        float c3 = (float)(startColor & 0xFF) / 255.0f;
        float c4 = (float)(endColor >> 24 & 0xFF) / 255.0f;
        float c5 = (float)(endColor >> 16 & 0xFF) / 255.0f;
        float c6 = (float)(endColor >> 8 & 0xFF) / 255.0f;
        float c7 = (float)(endColor & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), right, top, 0.0f).color(c1, c2, c3, c);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), left, top, 0.0f).color(c1, c2, c3, c);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), left, bottom, 0.0f).color(c5, c6, c7, c4);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), right, bottom, 0.0f).color(c5, c6, c7, c4);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static String readClipboard() {
        try {
            return (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        }
        catch (UnsupportedFlavorException | IOException exception) {
            return null;
        }
    }

    public static void drawOutlineRect(double left, double top, double right, double bottom, Color color, float lineWidth) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (float)(color.getRGB() >> 24 & 0xFF) / 255.0f;
        float f = (float)(color.getRGB() >> 16 & 0xFF) / 255.0f;
        float f1 = (float)(color.getRGB() >> 8 & 0xFF) / 255.0f;
        float f2 = (float)(color.getRGB() & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)left, (float)bottom, 0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)right, (float)bottom, 0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)right, (float)top, 0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), (float)left, (float)top, 0.0f).color(f, f1, f2, f3);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        matrixStack = context.getMatrices();
        boolean hovered = this.isHovering(mouseX, mouseY);
        double toggleProgress = this.toggleAnimation.get(this.getState() ? 1.0 : 0.0, 160L, Easing.CubicInOut);
        Color color = ClickGui.getInstance().getColor(this.getColorDelay());
        int accentA = hovered ? ClickGui.getInstance().hoverAlpha.getValueInt() : ClickGui.getInstance().alpha.getValueInt();
        Color unpressedFill = new Color(hovered ? hoverColor : defaultColor, true);
        Color pressedFill = new Color(color.getRed(), color.getGreen(), color.getBlue(), accentA);
        Color baseFill = ColorUtil.fadeColor(unpressedFill, pressedFill, toggleProgress);
        Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float)this.width + 7.0f, this.y + (float)this.height - 0.5f, baseFill.getRGB());
        Render2DUtil.rect(matrixStack, this.x - 1.5f + (float)this.width + 0.6f - 0.5f, this.y + 4.0f, this.x + (float)this.width + 7.0f - 2.5f, this.y + (float)this.height - 5.0f, ColorUtil.injectAlpha(this.setting.getValue(), 255).getRGB());
        float textY = this.getCenteredTextY(this.y, (float)this.height - 0.5f);
        if (this.isHovering(mouseX, mouseY) && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
            this.drawString("Reset Default", (double)(this.x + 2.3f), (double)textY, enableTextColor);
        } else {
            this.drawString(this.getName(), (double)(this.x + 2.3f), (double)textY, enableTextColor);
        }
        double openProgress = this.openAnimation.get(this.open ? 1.0 : 0.0, 200L, Easing.CubicInOut);
        if (openProgress > 0.01) {
            int scissorX1 = (int)this.x - 1;
            int scissorY1 = (int)this.y - 1;
            int scissorX2 = (int)(this.x + (float)this.width + 7.0f) + 1;
            int scissorY2 = (int)Math.round((double)(this.y + (float)this.height) + 119.0 * openProgress) + 1;
            context.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);
            int pickerX = (int)this.x;
            int pickerY = (int)(this.y + (float)this.height);
            int pickerWidth = (int)((float)this.width + 7.4f);
            int pickerHeight = 78;
            int alphaSliderY = pickerY + pickerHeight + 4;
            int hueSliderY = alphaSliderY + 8;
            this.drawPicker(this.setting, pickerX, pickerY, pickerX, hueSliderY, pickerX, alphaSliderY, mouseX, mouseY);
            float actionsRowTopY = (float)pickerY + 96.0f;
            float actionsTextY = this.getCenteredTextY(actionsRowTopY, (float)this.getFontHeight());
            this.drawString("copy", (double)(this.x + 2.3f), (double)actionsTextY, this.isInsideCopy(mouseX, mouseY) ? enableTextColor : defaultTextColor);
            this.drawString("paste", (double)(this.x + (float)this.width - 2.3f - (float)this.getWidth("paste") + 11.7f - 4.6f), (double)actionsTextY, this.isInsidePaste(mouseX, mouseY) ? enableTextColor : defaultTextColor);
            float syncRowTopY = actionsRowTopY + (float)this.getFontHeight();
            float syncTextY = this.getCenteredTextY(syncRowTopY, (float)this.getFontHeight());
            this.drawString("rainbow", (double)(this.x + 2.3f), (double)syncTextY, this.setting.rainbow ? ColorUtil.injectAlpha(color, 255).getRGB() : (this.isInsideRainbow(mouseX, mouseY) ? enableTextColor : defaultTextColor));
            context.disableScissor();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY) && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
            this.pickingColor = false;
            this.pickingHue = false;
            this.pickingAlpha = false;
            this.setting.setValue(this.setting.getDefaultValue());
            this.setting.rainbow = this.setting.getDefaultRainbow();
            if (this.setting.injectBoolean) {
                this.setting.booleanValue = this.setting.getDefaultBooleanValue();
            }
            PickerButton.sound();
            return;
        }
        if (mouseButton == 0) {
            int pickerX = (int)this.x;
            int pickerY = (int)(this.y + (float)this.height);
            int pickerWidth = (int)((float)this.width + 7.4f);
            int pickerHeight = 78;
            int hueSliderWidth = pickerWidth + 3;
            int hueSliderHeight = 7;
            int alphaSliderHeight = 7;
            int alphaSliderY = pickerY + pickerHeight + 4;
            int hueSliderY = alphaSliderY + alphaSliderHeight + 1;
            if (PickerButton.mouseOver(pickerX, pickerY, pickerX + pickerWidth, pickerY + pickerHeight, mouseX, mouseY)) {
                this.pickingColor = true;
            }
            if (PickerButton.mouseOver(pickerX, hueSliderY, pickerX + hueSliderWidth, hueSliderY + hueSliderHeight, mouseX, mouseY)) {
                this.pickingHue = true;
            }
            if (PickerButton.mouseOver(pickerX, alphaSliderY, pickerX + pickerWidth, alphaSliderY + alphaSliderHeight, mouseX, mouseY)) {
                this.pickingAlpha = true;
            }
        }
        if (this.isHovering(mouseX, mouseY)) {
            if (mouseButton == 1) {
                PickerButton.sound();
                this.open = !this.open;
            } else if (mouseButton == 0 && this.setting.injectBoolean) {
                PickerButton.sound();
                boolean bl = this.setting.booleanValue = !this.setting.booleanValue;
            }
        }
        if (mouseButton == 0 && this.isInsideRainbow(mouseX, mouseY) && this.open) {
            boolean bl = this.setting.rainbow = !this.setting.rainbow;
        }
        if (mouseButton == 0 && this.isInsideCopy(mouseX, mouseY) && this.open) {
            PickerButton.sound();
            String hex = String.format("#%02x%02x%02x%02x", this.setting.getValue().getAlpha(), this.setting.getValue().getRed(), this.setting.getValue().getGreen(), this.setting.getValue().getBlue());
            StringSelection selection = new StringSelection(hex);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            CommandManager.sendMessage("Copied the color to your clipboard.");
        }
        if (mouseButton == 0 && this.isInsidePaste(mouseX, mouseY) && this.open) {
            try {
                if (PickerButton.readClipboard() != null) {
                    if (Objects.requireNonNull(PickerButton.readClipboard()).startsWith("#")) {
                        String hex = Objects.requireNonNull(PickerButton.readClipboard());
                        int keyCodec = Integer.valueOf(hex.substring(1, 3), 16);
                        int r = Integer.valueOf(hex.substring(3, 5), 16);
                        int g = Integer.valueOf(hex.substring(5, 7), 16);
                        int elementCodec = Integer.valueOf(hex.substring(7, 9), 16);
                        this.setting.setValue(new Color(r, g, elementCodec, keyCodec));
                    } else {
                        String[] color = PickerButton.readClipboard().split(",");
                        this.setting.setValue(new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                CommandManager.sendMessage("\u00a74Bad color format! Use Hex (#FFFFFFFF)");
                this.setting.setValue(-1);
            }
        }
    }

    @Override
    public boolean getState() {
        return this.setting.booleanValue;
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        this.pickingAlpha = false;
        this.pickingHue = false;
        this.pickingColor = false;
    }

    public boolean isInsideCopy(int mouseX, int mouseY) {
        float actionsRowTopY = this.y + (float)this.height + 96.0f;
        int textY = (int)this.getCenteredTextY(actionsRowTopY, (float)this.getFontHeight());
        return PickerButton.mouseOver((int)((float)((int)this.x) + 2.3f), textY, (int)((float)((int)this.x) + 2.3f) + this.getWidth("copy"), textY + this.getFontHeight(), mouseX, mouseY);
    }

    public boolean isInsideRainbow(int mouseX, int mouseY) {
        float actionsRowTopY = this.y + (float)this.height + 96.0f;
        float syncRowTopY = actionsRowTopY + (float)this.getFontHeight();
        int textY = (int)this.getCenteredTextY(syncRowTopY, (float)this.getFontHeight());
        return PickerButton.mouseOver((int)((float)((int)this.x) + 2.3f), textY, (int)((float)((int)this.x) + 2.3f) + this.getWidth("rainbow"), textY + this.getFontHeight(), mouseX, mouseY);
    }

    public boolean isInsidePaste(int mouseX, int mouseY) {
        float actionsRowTopY = this.y + (float)this.height + 96.0f;
        int textY = (int)this.getCenteredTextY(actionsRowTopY, (float)this.getFontHeight());
        int x = (int)(this.x + (float)this.width - 2.3f - (float)this.getWidth("paste") + 11.7f - 4.6f);
        return PickerButton.mouseOver(x, textY, x + this.getWidth("paste"), textY + this.getFontHeight(), mouseX, mouseY);
    }

    @Override
    public int getHeight() {
        if (this.open) {
            return super.getHeight() + 119;
        }
        return super.getHeight();
    }

    public void drawPicker(ColorSetting setting, int pickerX, int pickerY, int hueSliderX, int hueSliderY, int alphaSliderX, int alphaSliderY, int mouseX, int mouseY) {
        float restrictedX;
        int pickerWidth = (int)((float)this.width + 7.4f);
        int pickerHeight = 78;
        int hueSliderHeight = 7;
        int alphaSliderHeight = 7;
        if (this.pickingColor && (GLFW.glfwGetMouseButton((long)mc.getWindow().getHandle(), (int)0) != 1 || !PickerButton.mouseOver(pickerX, pickerY, pickerX + pickerWidth, pickerY + pickerHeight, mouseX, mouseY))) {
            this.pickingColor = false;
        }
        if (!this.pickingColor) {
            this.hsb = Color.RGBtoHSB(setting.getValue().getRed(), setting.getValue().getGreen(), setting.getValue().getBlue(), null);
        }
        float[] color = new float[]{this.hsb[0], this.hsb[1], this.hsb[2], (float)setting.getValue().getAlpha() / 255.0f};
        if (this.pickingHue && (GLFW.glfwGetMouseButton((long)mc.getWindow().getHandle(), (int)0) != 1 || !PickerButton.mouseOver(hueSliderX, hueSliderY, hueSliderX + pickerWidth, hueSliderY + hueSliderHeight, mouseX, mouseY))) {
            this.pickingHue = false;
        }
        if (this.pickingAlpha && (GLFW.glfwGetMouseButton((long)mc.getWindow().getHandle(), (int)0) != 1 || !PickerButton.mouseOver(alphaSliderX, alphaSliderY, alphaSliderX + pickerWidth, alphaSliderY + alphaSliderHeight, mouseX, mouseY))) {
            this.pickingAlpha = false;
        }
        if (this.pickingHue) {
            restrictedX = Math.min(Math.max(hueSliderX, mouseX), hueSliderX + pickerWidth);
            color[0] = (restrictedX - (float)hueSliderX) / (float)pickerWidth;
        }
        if (this.pickingAlpha) {
            restrictedX = Math.min(Math.max(alphaSliderX, mouseX), alphaSliderX + pickerWidth);
            color[3] = 1.0f - (restrictedX - (float)alphaSliderX) / (float)pickerWidth;
        }
        if (this.pickingColor) {
            restrictedX = Math.min(Math.max(pickerX, mouseX), pickerX + pickerWidth);
            float restrictedY = Math.min(Math.max(pickerY, mouseY), pickerY + pickerHeight);
            color[1] = (restrictedX - (float)pickerX) / (float)pickerWidth;
            color[2] = 1.0f - (restrictedY - (float)pickerY) / (float)pickerHeight;
        }
        int selectedColor = Color.HSBtoRGB(color[0], 1.0f, 1.0f);
        float selectedRed = (float)(selectedColor >> 16 & 0xFF) / 255.0f;
        float selectedGreen = (float)(selectedColor >> 8 & 0xFF) / 255.0f;
        float selectedBlue = (float)(selectedColor & 0xFF) / 255.0f;
        PickerButton.drawPickerBase(pickerX, pickerY, pickerWidth, pickerHeight, selectedRed, selectedGreen, selectedBlue, color[3]);
        int targetCursorX = (int)((float)pickerX + color[1] * (float)pickerWidth);
        int targetCursorY = (int)((float)(pickerY + pickerHeight) - color[2] * (float)pickerHeight);
        int targetHueCursor = (int)((float)hueSliderX + (float)pickerWidth * color[0]);
        int targetAlphaCursor = (int)((float)(alphaSliderX + pickerWidth) - (float)pickerWidth * color[3]);
        if (!this.indicatorsInitialized) {
            this.pickerCursorXAnimation.from = targetCursorX;
            this.pickerCursorXAnimation.to = targetCursorX;
            this.pickerCursorYAnimation.from = targetCursorY;
            this.pickerCursorYAnimation.to = targetCursorY;
            this.hueCursorAnimation.from = targetHueCursor;
            this.hueCursorAnimation.to = targetHueCursor;
            this.alphaCursorAnimation.from = targetAlphaCursor;
            this.alphaCursorAnimation.to = targetAlphaCursor;
            this.indicatorsInitialized = true;
        }
        long pickerCursorLength = this.pickingColor ? 40L : 120L;
        int cursorX = (int)Math.round(this.pickerCursorXAnimation.get(targetCursorX, pickerCursorLength, Easing.SineOut));
        int cursorY = (int)Math.round(this.pickerCursorYAnimation.get(targetCursorY, pickerCursorLength, Easing.SineOut));
        this.drawHueSlider(hueSliderX, hueSliderY, pickerWidth, hueSliderHeight, color[0], this.pickingHue);
        setting.setValue(PickerButton.getColor(new Color(Color.HSBtoRGB(color[0], color[1], color[2])), color[3]));
        Render2DUtil.arrow(matrixStack, cursorX, cursorY, setting.getValue());
        this.drawAlphaSlider(alphaSliderX, alphaSliderY, pickerWidth - 1, alphaSliderHeight, selectedRed, selectedGreen, selectedBlue, color[3], this.pickingAlpha);
    }

    public void drawHueSlider(int x, int y, int width, int height, float hue, boolean snap) {
        int step = 0;
        if (height > width) {
            Render2DUtil.rect(matrixStack, x, y, x + width, y + 4, -65536);
            y += 4;
            for (int colorIndex = 0; colorIndex < 6; ++colorIndex) {
                int previousStep = Color.HSBtoRGB((float)step / 6.0f, 1.0f, 1.0f);
                int nextStep = Color.HSBtoRGB((float)(step + 1) / 6.0f, 1.0f, 1.0f);
                PickerButton.drawGradientRect(x, (float)y + (float)step * ((float)height / 6.0f), x + width, (float)y + (float)(step + 1) * ((float)height / 6.0f), previousStep, nextStep, false);
                ++step;
            }
            int targetSliderMinY = (int)((float)y + (float)height * hue) - 4;
            long cursorLength = snap ? 40L : 120L;
            int sliderMinY = (int)Math.round(this.hueCursorAnimation.get(targetSliderMinY, cursorLength, Easing.SineOut));
            Render2DUtil.rect(matrixStack, x, sliderMinY - 1, x + width, sliderMinY + 1, -1);
            PickerButton.drawOutlineRect(x, sliderMinY - 1, x + width, sliderMinY + 1, Color.BLACK, 1.0f);
        } else {
            for (int colorIndex = 0; colorIndex < 6; ++colorIndex) {
                int previousStep = Color.HSBtoRGB((float)step / 6.0f, 1.0f, 1.0f);
                int nextStep = Color.HSBtoRGB((float)(step + 1) / 6.0f, 1.0f, 1.0f);
                PickerButton.gradient(x + step * (width / 6), y, x + (step + 1) * (width / 6) + 3, y + height, previousStep, nextStep, true);
                ++step;
            }
            int targetSliderMinX = (int)((float)x + (float)width * hue);
            long cursorLength = snap ? 40L : 120L;
            int sliderMinX = (int)Math.round(this.hueCursorAnimation.get(targetSliderMinX, cursorLength, Easing.SineOut));
            Render2DUtil.rect(matrixStack, sliderMinX - 1, (float)y - 1.2f, sliderMinX + 1, (float)(y + height) + 1.2f, -1);
            PickerButton.drawOutlineRect((double)sliderMinX - 1.2, (double)y - 1.2, (double)sliderMinX + 1.2, (double)(y + height) + 1.2, Color.BLACK, 0.1f);
        }
    }

    public void drawAlphaSlider(int x, int y, int width, int height, float red, float green, float blue, float alpha, boolean snap) {
        boolean left = true;
        int checkerBoardSquareSize = height / 2;
        for (int squareIndex = -checkerBoardSquareSize; squareIndex < width; squareIndex += checkerBoardSquareSize) {
            if (!left) {
                Render2DUtil.rect(matrixStack, x + squareIndex, y, x + squareIndex + checkerBoardSquareSize, y + height, -1);
                Render2DUtil.rect(matrixStack, x + squareIndex, y + checkerBoardSquareSize, x + squareIndex + checkerBoardSquareSize, y + height, -7303024);
                if (squareIndex < width - checkerBoardSquareSize) {
                    int minX = x + squareIndex + checkerBoardSquareSize;
                    int maxX = Math.min(x + width, x + squareIndex + checkerBoardSquareSize * 2);
                    Render2DUtil.rect(matrixStack, minX, y, maxX, y + height, -7303024);
                    Render2DUtil.rect(matrixStack, minX, y + checkerBoardSquareSize, maxX, y + height, -1);
                }
            }
            left = !left;
        }
        PickerButton.drawLeftGradientRect(x, y, x + width, y + height, new Color(red, green, blue, 1.0f).getRGB(), 0);
        int targetSliderMinX = (int)((float)(x + width) - (float)width * alpha);
        long cursorLength = snap ? 40L : 120L;
        int sliderMinX = (int)Math.round(this.alphaCursorAnimation.get(targetSliderMinX, cursorLength, Easing.SineOut));
        Render2DUtil.rect(matrixStack, sliderMinX - 1, (float)y - 1.2f, sliderMinX + 1, (float)(y + height) + 1.2f, -1);
        PickerButton.drawOutlineRect((double)sliderMinX - 1.2, (double)y - 1.2, (double)sliderMinX + 1.2, (double)(y + height) + 1.2, Color.BLACK, 0.1f);
    }
}

