/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.StringSetting;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Panic
extends Module {
    private final BooleanSetting restoreOnDisable;
    private final StringSetting whitelist;
    private final List<Module> disabledModules = new ArrayList<Module>();

    public Panic() {
        super("Panic", "\u7d27\u6025\u505c\u6b62\u6240\u6709\u529f\u80fd", Module.Category.Combat);
        this.setChinese("\u7d27\u6025\u505c\u6b62\u6240\u6709\u529f\u80fd");
        this.restoreOnDisable = this.add(new BooleanSetting("\u6062\u590d\u542f\u7528", true));
        this.whitelist = this.add(new StringSetting("\u767d\u540d\u5355", ""));
    }

    @Override
    public void onEnable() {
        this.disabledModules.clear();
        List<Module> allModules = this.getAllModules();
        String[] whitelistNames = this.whitelist.getValue().split(",");
        ArrayList<String> whitelistModules = new ArrayList<String>();
        for (String name : whitelistNames) {
            whitelistModules.add(name.trim().toLowerCase());
        }
        for (Module module : allModules) {
            if (module == this || this.shouldWhitelist(module, whitelistModules) || !module.isOn()) continue;
            this.disabledModules.add(module);
            module.disable();
        }
        this.sendMessage("\u00a7c\u5df2\u7d27\u6025\u505c\u6b62 " + this.disabledModules.size() + " \u4e2a\u529f\u80fd");
    }

    @Override
    public void onDisable() {
        if (this.restoreOnDisable.getValue() && !this.disabledModules.isEmpty()) {
            int restoredCount = 0;
            for (Module module : this.disabledModules) {
                if (!module.isOff()) continue;
                module.enable();
                ++restoredCount;
            }
            this.sendMessage("\u00a7a\u5df2\u6062\u590d " + restoredCount + " \u4e2a\u529f\u80fd");
        }
        this.disabledModules.clear();
    }

    @Override
    public String getInfo() {
        return this.disabledModules.isEmpty() ? "" : "" + this.disabledModules.size();
    }

    private List<Module> getAllModules() {
        String[] moduleNames;
        ArrayList<Module> modules = new ArrayList<Module>();
        for (String moduleName : moduleNames = new String[]{"AutoKit", "Fonts", "NoTerrainScreen", "AutoCrystal", "Ambience", "AntiHunger", "AntiVoid", "AutoWalk", "VClip", "ExtraTab", "AntiWeak", "BedCrafter", "Friend", "AspectRatio", "ChunkESP", "Aura", "PistonCrystal", "AutoAnchor", "PhaseESP", "AutoArmor", "Breaker", "AutoLog", "AutoEZ", "SelfTrap", "Sorter", "AutoMend", "AutoPot", "AutoPush", "Offhand", "Nuker", "AutoTrap", "AutoWeb", "Blink", "ChorusControl", "BlockStrafe", "FastSwim", "Blocker", "Quiver", "BowBomb", "BreakESP", "Burrow", "Punctuation", "MaceSpoof", "CameraClip", "ChatAppend", "ClickGui", "InfiniteTrident", "ColorsModule", "AutoRegear", "LavaFiller", "AntiPhase", "Clip", "AntiCheat", "IRC", "Fov", "Criticals", "CevBreaker", "Crosshair", "Chams", "AntiPacket", "AutoReconnect", "ESP", "HoleESP", "Tracers", "MovementSync", "ElytraFly", "PacketLogger", "TeleportLogger", "SkinFlicker", "EntityControl", "NameTags", "ShulkerViewer", "PingSpoof", "FakePlayer", "Spammer", "MotionCamera", "HighLight", "FastFall", "FastWeb", "Flatten", "Fly", "Yaw", "Freecam", "FreeLook", "TimerModule", "Tips", "ClientSetting", "TextRadar", "Info", "NoResourcePack", "RocketExtend", "HoleFiller", "HoleSnap", "LogoutSpots", "AutoTool", "Trajectories", "KillEffect", "AutoPearl", "AntiEffects", "NoFall", "NoRender", "NoSlow", "NoSound", "AirPlace", "Xray", "PacketEat", "PacketFly", "PacketMine", "PacketControl", "Phase", "PlaceRender", "InteractTweaks", "PopChams", "Replenish", "ServerLagger", "Scaffold", "ShaderModule", "AntiCrawl", "AntiRegear", "SafeWalk", "NoJumpDelay", "Speed", "Sprint", "Strafe", "Step", "Surround", "TotemParticle", "Velocity", "ViewModel", "XCarry", "Zoom"}) {
            Module module = this.getModuleInstance(moduleName);
            if (module == null) continue;
            modules.add(module);
        }
        return modules;
    }

    private Module getModuleInstance(String moduleName) {
        try {
            String[] possiblePackages;
            for (String packageName : possiblePackages = new String[]{"dev.luminous.mod.modules.impl.combat.", "dev.luminous.mod.modules.impl.movement.", "dev.luminous.mod.modules.impl.player.", "dev.luminous.mod.modules.impl.render.", "dev.luminous.mod.modules.impl.misc.", "dev.luminous.mod.modules.impl.client.", "dev.luminous.mod.modules.impl.exploit."}) {
                try {
                    String className = packageName + moduleName;
                    Class<?> clazz = Class.forName(className);
                    Field instanceField = clazz.getDeclaredField("INSTANCE");
                    instanceField.setAccessible(true);
                    Object instance = instanceField.get(null);
                    if (!(instance instanceof Module)) continue;
                    return (Module)instance;
                }
                catch (ClassNotFoundException | NoSuchFieldException e) {
                    // empty catch block
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private boolean shouldWhitelist(Module module, List<String> whitelistModules) {
        if (module == null) {
            return false;
        }
        String moduleName = module.getName().toLowerCase();
        for (String whitelistName : whitelistModules) {
            if (!moduleName.equals(whitelistName.toLowerCase())) continue;
            return true;
        }
        return false;
    }

    public void panicNow() {
        if (!this.isOn()) {
            this.enable();
        }
    }

    public void restoreNow() {
        if (this.isOn()) {
            this.disable();
        }
    }
}

