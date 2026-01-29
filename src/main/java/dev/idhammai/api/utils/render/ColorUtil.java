/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.api.utils.render;

import java.awt.Color;

public class ColorUtil {
    public static Color fadeColor(Color startColor, Color endColor, double progress) {
        progress = Math.min(Math.max(progress, 0.0), 1.0);
        int sR = startColor.getRed();
        int sG = startColor.getGreen();
        int sB = startColor.getBlue();
        int sA = startColor.getAlpha();
        int eR = endColor.getRed();
        int eG = endColor.getGreen();
        int eB = endColor.getBlue();
        int eA = endColor.getAlpha();
        return new Color(Math.min((int)((double)sR + (double)(eR - sR) * progress), 255), Math.min((int)((double)sG + (double)(eG - sG) * progress), 255), Math.min((int)((double)sB + (double)(eB - sB) * progress), 255), Math.min((int)((double)sA + (double)(eA - sA) * progress), 255));
    }

    public static Color hslToColor(float f, float f2, float f3, float f4) {
        if (f2 < 0.0f || f2 > 100.0f) {
            throw new IllegalArgumentException("Color parameter outside of expected range - Saturation");
        }
        if (f3 < 0.0f || f3 > 100.0f) {
            throw new IllegalArgumentException("Color parameter outside of expected range - Lightness");
        }
        if (f4 < 0.0f || f4 > 1.0f) {
            throw new IllegalArgumentException("Color parameter outside of expected range - Alpha");
        }
        f %= 360.0f;
        float f5 = (double)f3 < 0.5 ? f3 * (1.0f + f2) : (f3 /= 100.0f) + (f2 /= 100.0f) - f2 * f3;
        f2 = 2.0f * f3 - f5;
        f3 = Math.max(0.0f, ColorUtil.colorCalc(f2, f5, (f /= 360.0f) + 0.33333334f));
        float f6 = Math.max(0.0f, ColorUtil.colorCalc(f2, f5, f));
        f2 = Math.max(0.0f, ColorUtil.colorCalc(f2, f5, f - 0.33333334f));
        f3 = Math.min(f3, 1.0f);
        f6 = Math.min(f6, 1.0f);
        f2 = Math.min(f2, 1.0f);
        return new Color(f3, f6, f2, f4);
    }

    private static float colorCalc(float f, float f2, float f3) {
        if (f3 < 0.0f) {
            f3 += 1.0f;
        }
        if (f3 > 1.0f) {
            f3 -= 1.0f;
        }
        if (6.0f * f3 < 1.0f) {
            return f + (f2 - f) * 6.0f * f3;
        }
        if (2.0f * f3 < 1.0f) {
            return f2;
        }
        if (3.0f * f3 < 2.0f) {
            return f + (f2 - f) * 6.0f * (0.6666667f - f3);
        }
        return f;
    }

    public static Color injectAlpha(Color color, int alpha) {
        alpha = Math.max(Math.min(255, alpha), 0);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static int injectAlpha(int color, int alpha) {
        return color & 0xFFFFFF | alpha << 24;
    }

    public static Color pulseColor(Color startColor, Color endColor, double index, int count, double speed) {
        double brightness = Math.abs(((double)System.currentTimeMillis() * speed % 2000.0 / (double)Float.intBitsToFloat(Float.floatToIntBits(0.0013786979f) ^ 0x7ECEB56D) + index / (double)count * (double)Float.intBitsToFloat(Float.floatToIntBits(0.09192204f) ^ 0x7DBC419F)) % (double)Float.intBitsToFloat(Float.floatToIntBits(0.7858098f) ^ 0x7F492AD5) - (double)Float.intBitsToFloat(Float.floatToIntBits(6.46708f) ^ 0x7F4EF252));
        double quad = brightness % (double)Float.intBitsToFloat(Float.floatToIntBits(0.8992331f) ^ 0x7F663424);
        return ColorUtil.fadeColor(startColor, endColor, quad);
    }

    public static Color pulseColor(Color color, double index, int count, double speed) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        double brightness = Math.abs(((double)System.currentTimeMillis() * speed % 2000.0 / (double)Float.intBitsToFloat(Float.floatToIntBits(0.0013786979f) ^ 0x7ECEB56D) + index / (double)count * (double)Float.intBitsToFloat(Float.floatToIntBits(0.09192204f) ^ 0x7DBC419F)) % (double)Float.intBitsToFloat(Float.floatToIntBits(0.7858098f) ^ 0x7F492AD5) - (double)Float.intBitsToFloat(Float.floatToIntBits(6.46708f) ^ 0x7F4EF252));
        brightness = (double)Float.intBitsToFloat(Float.floatToIntBits(18.996923f) ^ 0x7E97F9B3) + (double)Float.intBitsToFloat(Float.floatToIntBits(2.7958195f) ^ 0x7F32EEB5) * brightness;
        hsb[2] = (float)(brightness % (double)Float.intBitsToFloat(Float.floatToIntBits(0.8992331f) ^ 0x7F663424));
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }
}

