/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.PlayerListEntry
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.StringSetting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import net.minecraft.client.network.PlayerListEntry;

public class Spammer
extends Module {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public final BooleanSetting checkSelf = this.add(new BooleanSetting("CheckSelf", false));
    final StringSetting message = this.add(new StringSetting("Message", "\u6700\u5f3a\u5916\u6302Alien\u793e\u533a\u7248\u514d\u8d39\u8bd5\u7528 \u7fa4\ud835\udff1\ud835\udff4\ud835\udff5\ud835\udfed\ud835\udff5\ud835\udfed\ud835\udff1\ud835\udff2\ud835\udfed"));
    private final Random random = new Random();
    private final SliderSetting randoms = this.add(new SliderSetting("Random", 3.0, 0.0, 20.0, 1.0));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 5.0, 0.0, 60.0, 0.1).setSuffix("s"));
    private final BooleanSetting tellMode = this.add(new BooleanSetting("RandomWhisper", false));
    private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
    private final Timer timer = new Timer();

    public Spammer() {
        super("Spammer", Module.Category.Misc);
        this.setChinese("\u81ea\u52a8\u5237\u5c4f");
    }

    @Override
    public void onLogout() {
        if (this.autoDisable.getValue()) {
            this.disable();
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!this.timer.passedS(this.delay.getValue())) {
            return;
        }
        this.timer.reset();
        Object randomString = this.generateRandomString(this.randoms.getValueInt());
        if (!((String)randomString).isEmpty()) {
            randomString = " " + (String)randomString;
        }
        if (this.tellMode.getValue()) {
            Collection players = mc.getNetworkHandler().getPlayerList();
            ArrayList list = new ArrayList(players);
            int size = list.size();
            if (size == 0) {
                return;
            }
            PlayerListEntry playerListEntry = (PlayerListEntry)list.get(this.random.nextInt(size));
            int i = 0;
            while (this.checkSelf.getValue() && Objects.equals(playerListEntry.getProfile().getName(), Spammer.mc.player.getGameProfile().getName())) {
                if (i > 50) {
                    return;
                }
                ++i;
                playerListEntry = (PlayerListEntry)list.get(this.random.nextInt(size));
            }
            mc.getNetworkHandler().sendChatCommand("tell " + playerListEntry.getProfile().getName() + " " + this.message.getValue() + (String)randomString);
        } else if (this.message.getValue().startsWith("/")) {
            mc.getNetworkHandler().sendCommand(this.message.getValue().replaceFirst("/", "") + (String)randomString);
        } else {
            mc.getNetworkHandler().sendChatMessage(this.message.getValue() + (String)randomString);
        }
    }

    private String generateRandomString(int LENGTH) {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; ++i) {
            int index = this.random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}

