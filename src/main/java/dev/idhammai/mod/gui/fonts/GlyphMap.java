/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.client.texture.NativeImage
 *  net.minecraft.client.texture.NativeImage$Format
 *  net.minecraft.client.texture.NativeImageBackedTexture
 *  net.minecraft.util.Identifier
 *  org.lwjgl.system.MemoryUtil
 */
package dev.idhammai.mod.gui.fonts;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

class GlyphMap {
    final char fromIncl;
    final char toExcl;
    final Font font;
    Font secondFont;
    final Identifier bindToTexture;
    final int pixelPadding;
    private final Char2ObjectArrayMap<Glyph> glyphs = new Char2ObjectArrayMap();
    int width;
    int height;
    boolean generated = false;

    public GlyphMap(char from, char to, Font font, Identifier identifier, int padding) {
        this.fromIncl = from;
        this.toExcl = to;
        this.font = font;
        this.bindToTexture = identifier;
        this.pixelPadding = padding;
    }

    public GlyphMap(char from, char to, Font font, Font secondFont, Identifier identifier, int padding) {
        this(from, to, font, identifier, padding);
        this.secondFont = secondFont;
    }

    public static void registerBufferedImageTexture(Identifier i, BufferedImage bi) {
        try {
            int ow = bi.getWidth();
            int oh = bi.getHeight();
            NativeImage image = new NativeImage(NativeImage.Format.RGBA, ow, oh, false);
            WritableRaster _ra = bi.getRaster();
            ColorModel _cm = bi.getColorModel();
            int nbands = _ra.getNumBands();
            int dataType = _ra.getDataBuffer().getDataType();
            Object _d = switch (dataType) {
                case 0 -> new byte[nbands];
                case 1 -> new short[nbands];
                case 3 -> new int[nbands];
                case 4 -> new float[nbands];
                case 5 -> new double[nbands];
                default -> throw new IllegalArgumentException("Unknown data buffer type: " + dataType);
            };
            for (int y = 0; y < oh; ++y) {
                for (int x = 0; x < ow; ++x) {
                    _ra.getDataElements(x, y, _d);
                    int keyCodec = _cm.getAlpha(_d);
                    int r = _cm.getRed(_d);
                    int g = _cm.getGreen(_d);
                    int elementCodec = _cm.getBlue(_d);
                    int abgr = keyCodec << 24 | elementCodec << 16 | g << 8 | r;
                    image.setColor(x, y, abgr);
                }
            }
            NativeImageBackedTexture tex = new NativeImageBackedTexture(image);
            if (RenderSystem.isOnRenderThread()) {
                tex.upload();
                MinecraftClient.getInstance().getTextureManager().registerTexture(i, (AbstractTexture)tex);
            } else {
                RenderSystem.recordRenderCall(() -> {
                    tex.upload();
                    MinecraftClient.getInstance().getTextureManager().registerTexture(i, (AbstractTexture)tex);
                });
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Glyph getGlyph(char c) {
        if (!this.generated) {
            this.generate();
        }
        return (Glyph)this.glyphs.get(c);
    }

    public void destroy() {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(this.bindToTexture);
        this.glyphs.clear();
        this.width = -1;
        this.height = -1;
        this.generated = false;
    }

    public boolean contains(char c) {
        return c >= this.fromIncl && c < this.toExcl;
    }

    private Font getFontForGlyph(char c) {
        if (this.font.canDisplay(c)) {
            return this.font;
        }
        return Objects.requireNonNullElse(this.secondFont, this.font);
    }

    public void generate() {
        if (this.generated) {
            return;
        }
        int range = this.toExcl - this.fromIncl - 1;
        int charsVert = (int)(Math.ceil(Math.sqrt(range)) * 1.5);
        this.glyphs.clear();
        int generatedChars = 0;
        int charNX = 0;
        int maxX = 0;
        int maxY = 0;
        int currentX = 0;
        int currentY = 0;
        int currentRowMaxY = 0;
        ArrayList<Glyph> glyphs1 = new ArrayList<Glyph>();
        AffineTransform af = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(af, true, false);
        while (generatedChars <= range) {
            char currentChar = (char)(this.fromIncl + generatedChars);
            Font font = this.getFontForGlyph(currentChar);
            Rectangle2D stringBounds = font.getStringBounds(String.valueOf(currentChar), frc);
            int width = (int)Math.ceil(stringBounds.getWidth());
            int height = (int)Math.ceil(stringBounds.getHeight());
            ++generatedChars;
            maxX = Math.max(maxX, currentX + width);
            maxY = Math.max(maxY, currentY + height);
            if (charNX >= charsVert) {
                currentX = 0;
                currentY += currentRowMaxY + this.pixelPadding;
                charNX = 0;
                currentRowMaxY = 0;
            }
            currentRowMaxY = Math.max(currentRowMaxY, height);
            glyphs1.add(new Glyph(currentX, currentY, width, height, currentChar, this));
            currentX += width + this.pixelPadding;
            ++charNX;
        }
        BufferedImage bi = new BufferedImage(Math.max(maxX + this.pixelPadding, 1), Math.max(maxY + this.pixelPadding, 1), 2);
        this.width = bi.getWidth();
        this.height = bi.getHeight();
        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(new Color(255, 255, 255, 0));
        g2d.fillRect(0, 0, this.width, this.height);
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        for (Glyph glyph : glyphs1) {
            g2d.setFont(this.getFontForGlyph(glyph.value()));
            FontMetrics fontMetrics = g2d.getFontMetrics();
            g2d.drawString(String.valueOf(glyph.value()), glyph.u(), glyph.v() + fontMetrics.getAscent());
            this.glyphs.put(glyph.value(), glyph);
        }
        GlyphMap.registerBufferedImageTexture(this.bindToTexture, bi);
        this.generated = true;
    }
}

