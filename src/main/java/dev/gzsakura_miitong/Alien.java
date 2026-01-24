/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong;

import dev.gzsakura_miitong.api.events.eventbus.EventBus;
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

public class Alien {
    public static final String NAME = Vitality.NAME;
    public static final String VERSION = Vitality.VERSION;
    public static final EventBus EVENT_BUS = Vitality.EVENT_BUS;
    public static HoleManager HOLE = Vitality.HOLE;
    public static PlayerManager PLAYER = Vitality.PLAYER;
    public static TradeManager TRADE = Vitality.TRADE;
    public static CleanerManager CLEANER = Vitality.CLEANER;
    public static XrayManager XRAY = Vitality.XRAY;
    public static ModuleManager MODULE = Vitality.MODULE;
    public static CommandManager COMMAND = Vitality.COMMAND;
    public static ConfigManager CONFIG = Vitality.CONFIG;
    public static RotationManager ROTATION = Vitality.ROTATION;
    public static BreakManager BREAK = Vitality.BREAK;
    public static PopManager POP = Vitality.POP;
    public static FriendManager FRIEND = Vitality.FRIEND;
    public static TimerManager TIMER = Vitality.TIMER;
    public static ShaderManager SHADER = Vitality.SHADER;
    public static BlurManager BLUR = Vitality.BLUR;
    public static FPSManager FPS = Vitality.FPS;
    public static ServerManager SERVER = Vitality.SERVER;
    public static ThreadManager THREAD = Vitality.THREAD;
    public static boolean loaded = Vitality.loaded;
    public static long initTime = Vitality.initTime;

    public static String getPrefix() {
        return Vitality.getPrefix();
    }

    public static void save() {
        Vitality.save();
    }
}

