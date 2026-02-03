/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  by.radioegor146.nativeobfuscator.Native
 *  com.google.common.base.Splitter
 *  org.apache.commons.io.IOUtils
 */
package dev.idhammai.core.impl;

import com.google.common.base.Splitter;
import dev.idhammai.Frog;
import dev.idhammai.core.Manager;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.Fonts;
import dev.idhammai.mod.modules.impl.client.HUD;
import dev.idhammai.mod.modules.settings.Setting;
import dev.idhammai.mod.modules.settings.impl.BindSetting;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import dev.idhammai.mod.modules.settings.impl.StringSetting;
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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
    }

    public void load() {
        this.settings.clear();
        Splitter COLON_SPLITTER = Splitter.on((char)':');
        try {
            if (options.exists()) {
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
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("[Frog Client] Failed to load settings");
        }
        for (Module module : Frog.MODULE.getModules()) {
            for (Setting setting : module.getSettings()) {
                String line = module.getName() + "_" + setting.getName();
                Objects.requireNonNull(setting);
                if (setting instanceof BooleanSetting) {
                    BooleanSetting s = (BooleanSetting)setting;
                    s.setValueWithoutTask(Frog.CONFIG.getBoolean(line, s.getDefaultValue()));
                } else if (setting instanceof SliderSetting) {
                    SliderSetting s = (SliderSetting)setting;
                    s.setValue(Frog.CONFIG.getFloat(line, (float)s.getDefaultValue()));
                } else if (setting instanceof BindSetting) {
                    BindSetting s = (BindSetting)setting;
                    s.setValue(Frog.CONFIG.getInt(line, s.getDefaultValue()));
                    s.setHoldEnable(Frog.CONFIG.getBoolean(line + "_hold"));
                } else if (setting instanceof EnumSetting) {
                    EnumSetting s = (EnumSetting)setting;
                    s.loadSetting(Frog.CONFIG.getString(line));
                } else if (setting instanceof ColorSetting) {
                    ColorSetting s = (ColorSetting)setting;
                    s.setValue(new Color(Frog.CONFIG.getInt(line, s.getDefaultValue().getRGB()), true));
                    boolean rainbow;
                    if (Frog.CONFIG.getString(line + "Rainbow") != null) {
                        rainbow = Frog.CONFIG.getBoolean(line + "Rainbow", s.getDefaultRainbow());
                    } else {
                        rainbow = Frog.CONFIG.getBoolean(line + "Sync", s.getDefaultRainbow());
                    }
                    s.setRainbow(rainbow);
                    if (s.injectBoolean) {
                        s.booleanValue = Frog.CONFIG.getBoolean(line + "Boolean", s.getDefaultBooleanValue());
                    }
                } else if (setting instanceof StringSetting) {
                    StringSetting s = (StringSetting)setting;
                    s.setValue(Frog.CONFIG.getString(line, s.getDefaultValue()));
                }
            }
            module.setState(Frog.CONFIG.getBoolean(module.getName() + "_state", module instanceof HUD));
        }
    }

    public void read() {
        this.load();
    }

    public void save() {
        PrintWriter printwriter = null;
        try {
            printwriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(options), StandardCharsets.UTF_8));
            for (Module module : Frog.MODULE.getModules()) {
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
                        printwriter.println(line + "Rainbow:" + s.rainbow);
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
            System.out.println("[Frog Client] Failed to save settings");
        }
        finally {
            IOUtils.closeQuietly(printwriter);
        }
    }

    public static File getCfgFolder() {
        File base = ConfigManager.getFolder();
        File folder = new File(base, "cfg");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public static File getCfgFile(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        File folder = ConfigManager.getCfgFolder();
        return new File(folder, name + ".cfg");
    }

    public static ArrayList<String> listCfgNames() {
        ArrayList<String> out = new ArrayList<String>();
        File folder = ConfigManager.getCfgFolder();
        if (folder == null) {
            return out;
        }
        File[] files = folder.listFiles();
        if (files == null) {
            return out;
        }
        for (File f : files) {
            if (f == null || !f.isFile()) continue;
            String n = f.getName();
            if (n == null) continue;
            String ln = n.toLowerCase();
            if (!ln.endsWith(".cfg")) continue;
            String base = n.substring(0, n.length() - 4);
            if (base.isEmpty()) continue;
            out.add(base);
        }
        out.sort(String::compareToIgnoreCase);
        return out;
    }

    public static String sanitizeCfgName(String name) {
        if (name == null) {
            return "";
        }
        String s = name.trim();
        if (s.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ' ') {
                out.append(c);
                continue;
            }
            out.append('_');
        }
        String r = out.toString().trim();
        if (r.length() > 32) {
            r = r.substring(0, 32);
        }
        return r;
    }

    public static String uniqueCfgName(String base) {
        String n = ConfigManager.sanitizeCfgName(base);
        if (n.isEmpty()) {
            return "";
        }
        File f = ConfigManager.getCfgFile(n);
        if (f != null && !f.exists()) {
            return n;
        }
        for (int i = 1; i < 1000; ++i) {
            String nn = n + "_" + i;
            File ff = ConfigManager.getCfgFile(nn);
            if (ff != null && !ff.exists()) {
                return nn;
            }
        }
        return n;
    }

    public static void writeDefaultCfg(File file) {
        if (file == null) {
            return;
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            for (Module module : Frog.MODULE.getModules()) {
                for (Setting setting : module.getSettings()) {
                    String line = module.getName() + "_" + setting.getName();
                    if (setting instanceof BooleanSetting) {
                        BooleanSetting s = (BooleanSetting)setting;
                        out.println(line + ":" + s.getDefaultValue());
                    } else if (setting instanceof SliderSetting) {
                        SliderSetting s = (SliderSetting)setting;
                        out.println(line + ":" + s.getDefaultValue());
                    } else if (setting instanceof BindSetting) {
                        BindSetting s = (BindSetting)setting;
                        out.println(line + ":" + s.getDefaultValue());
                        out.println(line + "_hold:" + false);
                    } else if (setting instanceof EnumSetting) {
                        EnumSetting s = (EnumSetting)setting;
                        Enum dv = (Enum)s.getDefaultValue();
                        out.println(line + ":" + dv.name());
                    } else if (setting instanceof ColorSetting) {
                        ColorSetting s = (ColorSetting)setting;
                        out.println(line + ":" + s.getDefaultValue().getRGB());
                        out.println(line + "Rainbow:" + s.getDefaultRainbow());
                        if (s.injectBoolean) {
                            out.println(line + "Boolean:" + s.getDefaultBooleanValue());
                        }
                    } else if (setting instanceof StringSetting) {
                        StringSetting s = (StringSetting)setting;
                        out.println(line + ":" + s.getDefaultValue());
                    }
                }
                out.println(module.getName() + "_state:" + (module instanceof HUD));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static String createDefaultCfg(String nameInput) {
        String base = ConfigManager.sanitizeCfgName(nameInput);
        if (base.isEmpty()) {
            return null;
        }
        String name = ConfigManager.uniqueCfgName(base);
        File file = ConfigManager.getCfgFile(name);
        if (file == null) {
            return null;
        }
        ConfigManager.writeDefaultCfg(file);
        return name;
    }

    public static String backupCfg(String fromName, String toNameInput) {
        if (fromName == null || fromName.isEmpty()) {
            return null;
        }
        File src = ConfigManager.getCfgFile(fromName);
        if (src == null || !src.exists()) {
            return null;
        }
        String base = ConfigManager.sanitizeCfgName(toNameInput);
        if (base.isEmpty()) {
            base = fromName + "_backup_" + System.currentTimeMillis();
        }
        String name = ConfigManager.uniqueCfgName(base);
        File dst = ConfigManager.getCfgFile(name);
        if (dst == null) {
            return null;
        }
        try {
            Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return name;
    }

    public static void deleteCfg(String name) {
        File f = ConfigManager.getCfgFile(name);
        if (f == null || !f.exists()) {
            return;
        }
        try {
            f.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String saveCfg(String nameInput) {
        String n = ConfigManager.sanitizeCfgName(nameInput);
        if (n.isEmpty()) {
            return null;
        }
        ConfigManager.getCfgFolder();
        try {
            ConfigManager.options = Manager.getFile("cfg" + File.separator + n + ".cfg");
            Frog.save();
            return n;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        finally {
            ConfigManager.options = Manager.getFile("options.txt");
        }
    }

    public static boolean loadCfg(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        try {
            ConfigManager.options = Manager.getFile("cfg" + File.separator + name + ".cfg");
            Frog.CONFIG = new ConfigManager();
            Frog.CONFIG.load();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            ConfigManager.options = Manager.getFile("options.txt");
            Frog.save();
            if (Fonts.INSTANCE != null) {
                Fonts.INSTANCE.refresh();
            }
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

