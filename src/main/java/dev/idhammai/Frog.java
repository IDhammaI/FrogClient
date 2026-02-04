package dev.idhammai;

import dev.idhammai.api.events.eventbus.EventBus;
import dev.idhammai.api.events.impl.InitEvent;
import dev.idhammai.core.impl.BlurManager;
import dev.idhammai.core.impl.BreakManager;
import dev.idhammai.core.impl.CleanerManager;
import dev.idhammai.core.impl.CommandManager;
import dev.idhammai.core.impl.ConfigManager;
import dev.idhammai.core.impl.FPSManager;
import dev.idhammai.core.impl.FriendManager;
import dev.idhammai.core.impl.HoleManager;
import dev.idhammai.core.impl.ModuleManager;
import dev.idhammai.core.impl.PlayerManager;
import dev.idhammai.core.impl.PopManager;
import dev.idhammai.core.impl.RotationManager;
import dev.idhammai.core.impl.ServerManager;
import dev.idhammai.core.impl.ShaderManager;
import dev.idhammai.core.impl.ThreadManager;
import dev.idhammai.core.impl.TimerManager;
import dev.idhammai.core.impl.TradeManager;
import dev.idhammai.core.impl.XrayManager;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import java.io.File;
import java.lang.invoke.MethodHandles;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;

public class Frog implements ModInitializer {
    public static final String NAME = "Frog Client";
    public static final String VERSION = "1.04-beta";
    public static final String CONFIG_DIR = "frog";
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

    public static String getPrefix() {
        return ClientSetting.INSTANCE.prefix.getValue();
    }

    public static void save() {
        CONFIG.save();
        CLEANER.save();
        FRIEND.save();
        XRAY.save();
        TRADE.save();
        System.out.println("[Frog Client] Saved");
    }

    private void register() {
        EVENT_BUS.registerLambdaFactory((lookupInMethod, klass) -> (MethodHandles.Lookup)lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (loaded) {
                Frog.save();
            }
        }));
    }

    public void onInitialize() {
        this.register();
        MODULE = new ModuleManager();
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
        CONFIG.load();
        THREAD = new ThreadManager();

        initTime = System.currentTimeMillis();
        loaded = true;
        EVENT_BUS.post(new InitEvent());
        File folder = new File(MinecraftClient.getInstance().runDirectory.getPath() + File.separator + CONFIG_DIR + File.separator + "cfg");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    static {
        loaded = false;
    }
}

