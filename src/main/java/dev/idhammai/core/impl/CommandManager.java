/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  by.radioegor146.nativeobfuscator.Native
 *  net.minecraft.text.Text
 */
package dev.idhammai.core.impl;

import dev.idhammai.Frog;
import dev.idhammai.api.interfaces.IChatHudHook;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.mod.commands.Command;
import dev.idhammai.mod.commands.impl.AimCommand;
import dev.idhammai.mod.commands.impl.BindCommand;
import dev.idhammai.mod.commands.impl.BindsCommand;
import dev.idhammai.mod.commands.impl.CleanerCommand;
import dev.idhammai.mod.commands.impl.ClipCommand;
import dev.idhammai.mod.commands.impl.EsuCommand;
import dev.idhammai.mod.commands.impl.FakePlayerCommand;
import dev.idhammai.mod.commands.impl.FriendCommand;
import dev.idhammai.mod.commands.impl.GamemodeCommand;
import dev.idhammai.mod.commands.impl.GcCommand;
import dev.idhammai.mod.commands.impl.KitCommand;
import dev.idhammai.mod.commands.impl.LoadCommand;
import dev.idhammai.mod.commands.impl.PeekCommand;
import dev.idhammai.mod.commands.impl.PingCommand;
import dev.idhammai.mod.commands.impl.PrefixCommand;
import dev.idhammai.mod.commands.impl.RejoinCommand;
import dev.idhammai.mod.commands.impl.ReloadCommand;
import dev.idhammai.mod.commands.impl.SaveCommand;
import dev.idhammai.mod.commands.impl.TCommand;
import dev.idhammai.mod.commands.impl.TeleportCommand;
import dev.idhammai.mod.commands.impl.ToggleCommand;
import dev.idhammai.mod.commands.impl.TradeCommand;
import dev.idhammai.mod.commands.impl.WatermarkCommand;
import dev.idhammai.mod.commands.impl.XrayCommand;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import java.util.HashMap;
import net.minecraft.text.Text;

public class CommandManager
implements Wrapper {
    private final HashMap<String, Command> commands = new HashMap();

    public CommandManager() {
        this.init();
    }

    public void init() {
        this.registerCommand(new AimCommand());
        this.registerCommand(new BindCommand());
        this.registerCommand(new BindsCommand());
        this.registerCommand(new CleanerCommand());
        this.registerCommand(new ClipCommand());
        this.registerCommand(new EsuCommand());
        this.registerCommand(new FakePlayerCommand());
        this.registerCommand(new FriendCommand());
        this.registerCommand(new XrayCommand());
        this.registerCommand(new GamemodeCommand());
        this.registerCommand(new KitCommand());
        this.registerCommand(new LoadCommand());
        this.registerCommand(new PingCommand());
        this.registerCommand(new PrefixCommand());
        this.registerCommand(new RejoinCommand());
        this.registerCommand(new ReloadCommand());
        this.registerCommand(new SaveCommand());
        this.registerCommand(new TeleportCommand());
        this.registerCommand(new TCommand());
        this.registerCommand(new ToggleCommand());
        this.registerCommand(new TradeCommand());
        this.registerCommand(new WatermarkCommand());
        this.registerCommand(new PeekCommand());
        this.registerCommand(new GcCommand());
    }

    public static void sendMessage(String message) {
        mc.execute(() -> {
            if (Module.nullCheck()) {
                return;
            }
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Earth) {
                CommandManager.mc.inGameHud.getChatHud().addMessage(Text.of((String)message));
                return;
            }
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
                CommandManager.mc.inGameHud.getChatHud().addMessage(Text.of((String)("\u00a7f[\u00a7b" + ClientSetting.INSTANCE.hackName.getValue() + "\u00a7f] " + message)));
                return;
            }
            ((IChatHudHook)CommandManager.mc.inGameHud.getChatHud()).frogClient$addMessage(Text.of((String)(ClientSetting.INSTANCE.hackName.getValue() + "\u00a7f " + message)));
        });
    }

    public static void sendMessageId(String message, int id) {
        mc.execute(() -> {
            if (Module.nullCheck()) {
                return;
            }
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Earth) {
                ((IChatHudHook)CommandManager.mc.inGameHud.getChatHud()).frogClient$addMessage(Text.of((String)message), id);
                return;
            }
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
                ((IChatHudHook)CommandManager.mc.inGameHud.getChatHud()).frogClient$addMessage(Text.of((String)("\u00a7f[\u00a7b" + ClientSetting.INSTANCE.hackName.getValue() + "\u00a7f] " + message)), id);
                return;
            }
            ((IChatHudHook)CommandManager.mc.inGameHud.getChatHud()).frogClient$addMessage(Text.of((String)(ClientSetting.INSTANCE.hackName.getValue() + "\u00a7f " + message)), id);
        });
    }

    public static void sendChatMessageWidthIdNoSync(String message, int id) {
        mc.execute(() -> {
            if (Module.nullCheck()) {
                return;
            }
            ((IChatHudHook)CommandManager.mc.inGameHud.getChatHud()).frogClient$addMessageOutSync(Text.of((String)("\u00a7f" + message)), id);
        });
    }

    private void registerCommand(Command command) {
        this.commands.put(command.getName(), command);
    }

    public Command getCommandBySyntax(String string) {
        return this.commands.get(string);
    }

    public HashMap<String, Command> getCommands() {
        return this.commands;
    }

    public void command(String[] commandIn) {
        Command command = this.commands.get(commandIn[0].substring(Frog.getPrefix().length()).toLowerCase());
        if (command == null) {
            CommandManager.sendMessage("\u00a74Invalid Command!");
        } else {
            String[] parameterList = new String[commandIn.length - 1];
            System.arraycopy(commandIn, 1, parameterList, 0, commandIn.length - 1);
            if (parameterList.length == 1 && parameterList[0].equals("help")) {
                command.sendUsage();
                return;
            }
            command.runCommand(parameterList);
        }
    }
}

