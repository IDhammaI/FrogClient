/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 */
package dev.idhammai.core.impl;

import dev.idhammai.mod.gui.fonts.FontRenderer;
import dev.idhammai.mod.modules.impl.client.Fonts;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.client.util.math.MatrixStack;

public class FontManager {
    public static FontRenderer ui;
    public static FontRenderer small;
    public static FontRenderer icon;

    public static void init() {
        try {
            ui = FontManager.assets(8.0f, "default", 0);
            small = FontManager.assets(6.0f, "default", 0);
            icon = FontManager.assetsWithoutOffset(8.0f, "icon", 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FontRenderer assets(float size, String font, int style, String alternate) throws IOException, FontFormatException {
        ClassLoader classLoader = FontManager.class.getClassLoader();
        InputStream primary = classLoader.getResourceAsStream("assets/frogclient/font/" + font + ".ttf");
        InputStream fallback = classLoader.getResourceAsStream("assets/minecraft/font/font.ttf");
        InputStream stream = primary != null ? primary : fallback;
        return new FontRenderer(Font.createFont(0, Objects.requireNonNull(stream)).deriveFont(style, size), FontManager.getFont(alternate, style, (int)size), size){

            @Override
            public void drawString(MatrixStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                super.drawString(stack, s, x + (float)Fonts.INSTANCE.translate.getValueInt(), y + (float)Fonts.INSTANCE.shift.getValueInt(), r, g, elementCodec, keyCodec, shadow);
            }
        };
    }

    public static FontRenderer assetsWithoutOffset(float size, String name, int style) throws IOException, FontFormatException {
        ClassLoader classLoader = FontManager.class.getClassLoader();
        InputStream primary = classLoader.getResourceAsStream("assets/frog/icon/" + name + ".ttf");
        InputStream fallback = classLoader.getResourceAsStream("assets/minecraft/font/font.ttf");
        InputStream stream = primary != null ? primary : fallback;
        return new FontRenderer(Font.createFont(0, Objects.requireNonNull(stream)).deriveFont(style, size), size);
    }

    public static FontRenderer assets(float size, String name, int style) throws IOException, FontFormatException {
        ClassLoader classLoader = FontManager.class.getClassLoader();
        InputStream primary = classLoader.getResourceAsStream("assets/frogclient/font/" + name + ".ttf");
        InputStream fallback = classLoader.getResourceAsStream("assets/minecraft/font/font.ttf");
        InputStream stream = primary != null ? primary : fallback;
        return new FontRenderer(Font.createFont(0, Objects.requireNonNull(stream)).deriveFont(style, size), size){

            @Override
            public void drawString(MatrixStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                super.drawString(stack, s, x + (float)Fonts.INSTANCE.translate.getValueInt(), y + (float)Fonts.INSTANCE.shift.getValueInt(), r, g, elementCodec, keyCodec, shadow);
            }
        };
    }

    public static FontRenderer create(int size, String font, int style, String alternate) {
        return new FontRenderer(FontManager.getFont(font, style, size), FontManager.getFont(alternate, style, size), size){

            @Override
            public void drawString(MatrixStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                super.drawString(stack, s, x + (float)Fonts.INSTANCE.translate.getValueInt(), y + (float)Fonts.INSTANCE.shift.getValueInt(), r, g, elementCodec, keyCodec, shadow);
            }
        };
    }

    public static FontRenderer create(int size, String font, int style) {
        return new FontRenderer(FontManager.getFont(font, style, size), size){

            @Override
            public void drawString(MatrixStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                super.drawString(stack, s, x + (float)Fonts.INSTANCE.translate.getValueInt(), y + (float)Fonts.INSTANCE.shift.getValueInt(), r, g, elementCodec, keyCodec, shadow);
            }
        };
    }

    private static Font getFont(String font, int style, int size) {
        File fontDir = new File("C:\\Windows\\Fonts");
        try {
            for (File file : fontDir.listFiles()) {
                if (!file.getName().replace(".ttf", "").replace(".ttc", "").replace(".otf", "").equalsIgnoreCase(font)) continue;
                try {
                    return Font.createFont(0, file).deriveFont(style, size);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (File file : fontDir.listFiles()) {
                if (!file.getName().startsWith(font)) continue;
                try {
                    return Font.createFont(0, file).deriveFont(style, size);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new Font(null, style, size);
    }
}

