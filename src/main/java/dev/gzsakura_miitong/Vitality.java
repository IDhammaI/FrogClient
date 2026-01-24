/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  by.radioegor146.nativeobfuscator.Native
 *  net.fabricmc.api.ModInitializer
 *  net.minecraft.client.MinecraftClient
 *  oshi.SystemInfo
 *  oshi.hardware.ComputerSystem
 *  oshi.hardware.HWDiskStore
 *  oshi.hardware.HardwareAbstractionLayer
 */
package dev.gzsakura_miitong;

import dev.gzsakura_miitong.api.events.eventbus.EventBus;
import dev.gzsakura_miitong.api.events.impl.InitEvent;
import dev.gzsakura_miitong.core.impl.BlurManager;
import dev.gzsakura_miitong.core.impl.BreakManager;
import dev.gzsakura_miitong.core.impl.CleanerManager;
import dev.gzsakura_miitong.core.impl.CommandManager;
import dev.gzsakura_miitong.core.impl.ConfigManager;
import dev.gzsakura_miitong.core.impl.FPSManager;
import dev.gzsakura_miitong.core.impl.FriendManager;
import dev.gzsakura_miitong.core.impl.HoleManager;
import dev.gzsakura_miitong.core.impl.ModuleManager;
import dev.gzsakura_miitong.core.impl.PlayerManager;
import dev.gzsakura_miitong.core.impl.PopManager;
import dev.gzsakura_miitong.core.impl.RotationManager;
import dev.gzsakura_miitong.core.impl.ServerManager;
import dev.gzsakura_miitong.core.impl.ShaderManager;
import dev.gzsakura_miitong.core.impl.ThreadManager;
import dev.gzsakura_miitong.core.impl.TimerManager;
import dev.gzsakura_miitong.core.impl.TradeManager;
import dev.gzsakura_miitong.core.impl.XrayManager;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

public class Vitality
implements ModInitializer {
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int PBKDF2_ITERATIONS = 600000;
    private static final int SALT_LENGTH = 16;
    public static final String NAME = "Alien";
    public static final String VERSION = "4.2.1";
    public static final EventBus EVENT_BUS = new EventBus();
    public static HoleManager HOLE;
    public static PlayerManager PLAYER;
    public static TradeManager TRADE;
    public static CleanerManager CLEANER;
    public static XrayManager XRAY;
    public static ModuleManager MODULE;
    public static CommandManager COMMAND;
    public static ConfigManager CONFIG;
    public static RotationManager ROTATION;
    public static BreakManager BREAK;
    public static PopManager POP;
    public static FriendManager FRIEND;
    public static TimerManager TIMER;
    public static ShaderManager SHADER;
    public static BlurManager BLUR;
    public static FPSManager FPS;
    public static ServerManager SERVER;
    public static ThreadManager THREAD;
    public static boolean loaded;
    public static long initTime;
    public static String userId;
    private static final Set<String> LEGAL_HWIDS;
    public static final List<String> hwidCache;

    public static String getPrefix() {
        return ClientSetting.INSTANCE.prefix.getValue();
    }

    public static void save() {
        CONFIG.save();
        CLEANER.save();
        FRIEND.save();
        XRAY.save();
        TRADE.save();
        System.out.println("[Vitality] Saved");
    }

    private void register() {
        EVENT_BUS.registerLambdaFactory((lookupInMethod, klass) -> (MethodHandles.Lookup)lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (loaded) {
                Vitality.save();
            }
        }));
    }

    public void onInitialize() {
        Vitality.performHWIDCheck();
        this.register();
        MODULE = new ModuleManager();
        Alien.MODULE = MODULE;
        CONFIG = new ConfigManager();
        HOLE = new HoleManager();
        COMMAND = new CommandManager();
        FRIEND = new FriendManager();
        XRAY = new XrayManager();
        CLEANER = new CleanerManager();
        TRADE = new TradeManager();
        ROTATION = new RotationManager();
        BREAK = new BreakManager();
        PLAYER = new PlayerManager();
        POP = new PopManager();
        TIMER = new TimerManager();
        SHADER = new ShaderManager();
        BLUR = new BlurManager();
        FPS = new FPSManager();
        SERVER = new ServerManager();
        Alien.CONFIG = CONFIG;
        Alien.HOLE = HOLE;
        Alien.MODULE = MODULE;
        Alien.COMMAND = COMMAND;
        Alien.FRIEND = FRIEND;
        Alien.XRAY = XRAY;
        Alien.CLEANER = CLEANER;
        Alien.TRADE = TRADE;
        Alien.ROTATION = ROTATION;
        Alien.BREAK = BREAK;
        Alien.PLAYER = PLAYER;
        Alien.POP = POP;
        Alien.TIMER = TIMER;
        Alien.SHADER = SHADER;
        Alien.BLUR = BLUR;
        Alien.FPS = FPS;
        Alien.SERVER = SERVER;
        CONFIG.load();
        THREAD = new ThreadManager();
        Alien.THREAD = THREAD;
        Alien.initTime = initTime = System.currentTimeMillis();
        Alien.loaded = loaded = true;
        EVENT_BUS.post(new InitEvent());
        File folder = new File(MinecraftClient.getInstance().runDirectory.getPath() + File.separator + NAME.toLowerCase() + File.separator + "cfg");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private static void performHWIDCheck() {
        // HWID check disabled.
    }

    private static String collectHWID() {
        SystemInfo info = new SystemInfo();
        HardwareAbstractionLayer hardware = info.getHardware();
        ComputerSystem computerSystem = hardware.getComputerSystem();
        List<HWDiskStore> diskStores = hardware.getDiskStores();
        long total = 0L;
        for (HWDiskStore d : diskStores) {
            total += d.getSize();
        }
        String hwidTemp = String.valueOf(System.getenv("PROCESSOR_IDENTIFIER")) + String.valueOf(System.getenv("PROCESSOR_LEVEL")) + String.valueOf(System.getenv("PROCESSOR_ARCHITEW6432")) + String.valueOf(System.getenv("NUMBER_OF_PROCESSORS")) + String.valueOf(System.getenv("COMPUTERNAME")) + String.valueOf(System.getenv("PROCESSOR_REVISION")) + String.valueOf(System.getProperty("user.name")) + String.valueOf(System.getenv("PROCESSOR_ARCHITECTURE")) + String.valueOf(computerSystem.getManufacturer()) + String.valueOf(computerSystem.getModel()) + String.valueOf(computerSystem.getBaseboard().getManufacturer()) + String.valueOf(computerSystem.getBaseboard().getModel()) + String.valueOf(computerSystem.getBaseboard().getSerialNumber()) + String.valueOf(hardware.getProcessor().getProcessorIdentifier().getName()) + String.valueOf(hardware.getProcessor().getPhysicalProcessorCount()) + String.valueOf(hardware.getMemory().getTotal()) + String.valueOf(total);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(hwidTemp.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte aByteData : md.digest()) {
                String hex = Integer.toHexString(0xFF & aByteData);
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (Exception e) {
            return hwidTemp;
        }
    }

    static {
        loaded = false;
        LEGAL_HWIDS = new HashSet<String>(Arrays.asList("851f629e2ca821c20554b9e9c424a1b"));
        hwidCache = new ArrayList<String>();
    }

    public static class Frame
    extends JFrame {
        public Frame(String hwid) {
            Frame.copyToClipboard(hwid);
            String message = "Failed verification.HWID(" + hwid + ")\n(Copied to clipboard)";
            JOptionPane.showMessageDialog(this, message, "FAILED", 2, UIManager.getIcon("OptionPane.errorIcon"));
        }

        public static void copyToClipboard(String s) {
            StringSelection selection = new StringSelection(s);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        }
    }
}
