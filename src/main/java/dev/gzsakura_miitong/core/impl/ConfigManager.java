/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  by.radioegor146.nativeobfuscator.Native
 *  com.google.common.base.Splitter
 *  org.apache.commons.io.IOUtils
 */
package dev.gzsakura_miitong.core.impl;

import com.google.common.base.Splitter;
import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.core.Manager;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.HUD;
import dev.gzsakura_miitong.mod.modules.settings.Setting;
import dev.gzsakura_miitong.mod.modules.settings.impl.BindSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;

public class ConfigManager
extends Manager {
    public static File options = ConfigManager.getFile("options.txt");
    private final Hashtable<String, String> settings = new Hashtable();

    public ConfigManager() {
        this.read();
    }

    public void load() {
        for (Module module : Alien.MODULE.getModules()) {
            for (Setting setting : module.getSettings()) {
                String line = module.getName() + "_" + setting.getName();
                Objects.requireNonNull(setting);
                if (setting instanceof BooleanSetting) {
                    BooleanSetting s = (BooleanSetting)setting;
                    s.setValueWithoutTask(Alien.CONFIG.getBoolean(line, s.getDefaultValue()));
                } else if (setting instanceof SliderSetting) {
                    SliderSetting s = (SliderSetting)setting;
                    s.setValue(Alien.CONFIG.getFloat(line, (float)s.getDefaultValue()));
                } else if (setting instanceof BindSetting) {
                    BindSetting s = (BindSetting)setting;
                    s.setValue(Alien.CONFIG.getInt(line, s.getDefaultValue()));
                    s.setHoldEnable(Alien.CONFIG.getBoolean(line + "_hold"));
                } else if (setting instanceof EnumSetting) {
                    EnumSetting s = (EnumSetting)setting;
                    s.loadSetting(Alien.CONFIG.getString(line));
                } else if (setting instanceof ColorSetting) {
                    ColorSetting s = (ColorSetting)setting;
                    s.setValue(new Color(Alien.CONFIG.getInt(line, s.getDefaultValue().getRGB()), true));
                    s.setSync(Alien.CONFIG.getBoolean(line + "Sync", s.getDefaultSync()));
                    if (s.injectBoolean) {
                        s.booleanValue = Alien.CONFIG.getBoolean(line + "Boolean", s.getDefaultBooleanValue());
                    }
                } else if (setting instanceof StringSetting) {
                    StringSetting s = (StringSetting)setting;
                    s.setValue(Alien.CONFIG.getString(line, s.getDefaultValue()));
                }
            }
            module.setState(Alien.CONFIG.getBoolean(module.getName() + "_state", module instanceof HUD));
        }
    }

    public void read() {
        PrintWriter printwriter = null;
        try {
            printwriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(options), StandardCharsets.UTF_8));
            for (Module module : Alien.MODULE.getModules()) {
                for (Setting setting : module.getSettings()) {
                    String line = module.getName() + "_" + setting.getName();
                    Objects.requireNonNull(setting);
                    if (setting instanceof BooleanSetting) {
                        BooleanSetting s = (BooleanSetting)setting;
                        printwriter.println(line + ":" + s.getValue());
                    } else if (setting instanceof SliderSetting) {
                        SliderSetting s = (SliderSetting)setting;
                        printwriter.println(line + ":" + s.getValue());
                    } else if (setting instanceof BindSetting) {
                        BindSetting s = (BindSetting)setting;
                        printwriter.println(line + ":" + s.getValue());
                        printwriter.println(line + "_hold:" + s.isHoldEnable());
                    } else if (setting instanceof EnumSetting) {
                        EnumSetting s = (EnumSetting)setting;
                        printwriter.println(line + ":" + ((Enum)s.getValue()).name());
                    } else if (setting instanceof ColorSetting) {
                        ColorSetting s = (ColorSetting)setting;
                        printwriter.println(line + ":" + s.getValue().getRGB());
                        printwriter.println(line + "Sync:" + s.sync);
                        if (s.injectBoolean) {
                            printwriter.println(line + "Boolean:" + s.booleanValue);
                        }
                    } else if (setting instanceof StringSetting) {
                        StringSetting s = (StringSetting)setting;
                        printwriter.println(line + ":" + s.getValue());
                    }
                }
                printwriter.println(module.getName() + "_state:" + module.isOn());
            }
            IOUtils.closeQuietly((Writer)printwriter);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("[Vitality] Failed to save settings");
        }
        finally {
            IOUtils.closeQuietly(printwriter);
        }
    }

    public void save() {
        Splitter COLON_SPLITTER = Splitter.on((char)':');
        try {
            if (!options.exists()) {
                return;
            }
            List<String> list = IOUtils.readLines((InputStream)new FileInputStream(options), (Charset)StandardCharsets.UTF_8);
            for (String s : list) {
                try {
                    Iterator iterator = COLON_SPLITTER.limit(2).split((CharSequence)s).iterator();
                    this.settings.put((String)iterator.next(), (String)iterator.next());
                }
                catch (Exception var10) {
                    System.out.println("Skipping bad option: " + s);
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("[Vitality] Failed to load settings");
        }
    }

    public int getInt(String setting, int defaultValue) {
        String s = this.settings.get(setting);
        if (s == null || !this.isInteger(s)) {
            return defaultValue;
        }
        return Integer.parseInt(s);
    }

    public float getFloat(String setting, float defaultValue) {
        String s = this.settings.get(setting);
        if (s == null || !this.isFloat(s)) {
            return defaultValue;
        }
        return Float.parseFloat(s);
    }

    public boolean getBoolean(String setting) {
        String s = this.settings.get(setting);
        return Boolean.parseBoolean(s);
    }

    public boolean getBoolean(String setting, boolean defaultValue) {
        if (this.settings.get(setting) != null) {
            String s = this.settings.get(setting);
            return Boolean.parseBoolean(s);
        }
        return defaultValue;
    }

    public String getString(String setting) {
        return this.settings.get(setting);
    }

    public String getString(String setting, String defaultValue) {
        if (this.settings.get(setting) == null) {
            return defaultValue;
        }
        return this.settings.get(setting);
    }

    public boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public boolean isFloat(String str) {
        String pattern = "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";
        return str.matches(pattern);
    }
}

