/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.modules.settings;

public class EnumConverter {
    public static int currentEnum(Enum<?> clazz) {
        for (int i = 0; i < ((Enum[])clazz.getDeclaringClass().getEnumConstants()).length; ++i) {
            Enum e = ((Enum[])clazz.getDeclaringClass().getEnumConstants())[i];
            if (!e.name().equalsIgnoreCase(clazz.name())) continue;
            return i;
        }
        return -1;
    }

    public static Enum<?> increaseEnum(Enum<?> clazz) {
        int index = EnumConverter.currentEnum(clazz);
        for (int i = 0; i < ((Enum[])clazz.getDeclaringClass().getEnumConstants()).length; ++i) {
            if (i != index + 1) continue;
            return ((Enum[])clazz.getDeclaringClass().getEnumConstants())[i];
        }
        return ((Enum[])clazz.getDeclaringClass().getEnumConstants())[0];
    }

    public Enum<?> get(Enum<?> clazz, String string) {
        try {
            for (int i = 0; i < ((Enum[])clazz.getDeclaringClass().getEnumConstants()).length; ++i) {
                Enum e = ((Enum[])clazz.getDeclaringClass().getEnumConstants())[i];
                if (!e.name().equalsIgnoreCase(string)) continue;
                return e;
            }
            return null;
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}

