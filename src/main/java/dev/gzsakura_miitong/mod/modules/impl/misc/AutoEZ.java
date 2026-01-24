/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.DeathEvent;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.StringSetting;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AutoEZ
extends Module {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public final List<String> sex = List.of("\u5450\u5450~\u6742\u9c7c\u54e5\u54e5\u4e0d\u4f1a\u8fd9\u6837\u5c31\u88ab\u6349\u5f04\u7684\u4e0d\u4f1a\u8bf4\u8bdd\u4e86\u5427\u2661", "\u563b\u563b~\u6742\u9c7c\u54e5\u54e5\u4e0d\u4f1a\u4ee5\u4e3a\u7ad6\u4e2a\u5927\u62c7\u54e5\u5c31\u80fd\u6b3a\u8d1f\u6211\u4e86\u5427~\u4e0d\u4f1a\u5427\u2661\u4e0d\u4f1a\u5427\u2661", "\u6742\u9c7c\u54e5\u54e5\u600e\u4e48\u53ef\u80fd\u6b3a\u8d1f\u5f97\u4e86\u522b\u4eba\u5462~\u53ea\u80fd\u6b3a\u8d1f\u81ea\u5df1\u54e6\u2661~", "\u54e5\u54e5\u771f\u662f\u597d\u6b3a\u8d1f\u554a\u2661\u563b\u563b~", "\u54ce\u2661~\u6742\u9c7c\u8bf4\u8bdd\u5c31\u662f\u65e0\u8da3\u5509~", "\u5450\u5450~\u6742\u9c7c\u54e5\u54e5\u53d1\u8fd9\u4e2a\u662f\u60f3\u6559\u80b2\u6211\u5417~\u563b\u563b~\u600e\u4e48\u53ef\u80fd\u554a\u2661", "\u4ec0\u4e48\u561b~\u5e9f\u67f4\u54e5\u54e5\u4f1a\u60f3\u8fd9\u79cd\u4e8b\u60c5\u554a~\u5514\u5443", "\u628a\u4f60\u80ae\u810f\u7684\u76ee\u5149\u62ff\u5f00\u5566~\u5f88\u6076\u5fc3\u54e6\u2661", "\u54b1\u7684\u671f\u5f85\u5c31\u662f\u88ab\u4f60\u8fd9\u6837\u7684\u7b28\u86cb\u7834\u574f\u4e86~\u2661");
    public final List<String> bot = List.of("\u9f20\u6807\u660e\u5929\u5230\uff0c\u89e6\u6478\u677f\u6253\u7684", "\u8f6c\u4eba\u5de5", "\u6536\u5f92", "\u4e0d\u6536\u5f92", "\u6709\u771f\u4eba\u5417", "\u58a8\u955c\u4e0a\u8f66", "\u7d20\u6750\u5c40", "\u4e0d\u63a5\u5355", "\u63a5\u5355", "\u5f81\u5a5a", "4399?", "\u6682\u65f6\u4e0d\u8003\u8651\u6253\u804c\u4e1a", "bot?", "\u53eb\u4f60\u5bb6\u5927\u4eba\u6765\u6253", "\u5047\u80a2\u4e0a\u95e8\u5b89\u88c5", "\u6d6a\u8d39\u6211\u7684\u7f51\u8d39", "\u4e0d\u6536\u6b8b\u75be\u4eba", "\u4e0b\u8bfe", "\u81ea\u5df1\u627e\u5dee\u8ddd", "\u4e0d\u63a5\u4ee3", "\u4ee3+", "\u8fd9\u6837\u7684\u6cbb\u597d\u4e86\u4e5f\u6d41\u53e3\u6c34", "\u4eba\u673a", "\u4eba\u673a\u600e\u4e48\u8c03\u96be\u5ea6\u554a", "\u53ea\u6536\u4e0d\u88ab0\u5c01\u7684", "Bot\u5417\u8fd9\u662f", "\u9886\u517b", "\u7eb3\u4eb2", "\u6b63\u89c6\u5dee\u8ddd", "\u8fd1\u4eb2\u7e41\u6b96?", "\u6211\u73a9\u7684\u662f\u65b0\u624b\u6559\u7a0b?", "\u6765\u8c03\u7075\u654f\u5ea6\u7684", "\u6765\u8c03\u53c2\u6570\u7684", "\u5c0f\u53f7", "\u4e0d\u662f\u672c\u4eba\u522b\u52a0", "\u4e0b\u6b21\u8bb0\u5f97\u665a\u70b9\u73a9", "\u968f\u4fbf\u73a9\u73a9,\u4e0d\u5e26\u59b9", "\u62631\u4e0a\u8f66");
    private final EnumSetting<Type> type = this.add(new EnumSetting<Type>("Type", Type.Bot));
    final StringSetting message = this.add(new StringSetting("Message", "EZ %player%", () -> this.type.getValue() == Type.Custom));
    final Random random = new Random();
    private final SliderSetting range = this.add(new SliderSetting("Range", 10.0, 0.0, 20.0, 0.1));
    private final SliderSetting randoms = this.add(new SliderSetting("Random", 3.0, 0.0, 20.0, 1.0));

    public AutoEZ() {
        super("AutoEZ", Module.Category.Misc);
        this.setChinese("\u81ea\u52a8\u5632\u8bbd");
    }

    @EventListener
    public void onDeath(DeathEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player != AutoEZ.mc.player && !Alien.FRIEND.isFriend(player)) {
            if (this.range.getValue() > 0.0 && (double)AutoEZ.mc.player.distanceTo((Entity)player) > this.range.getValue()) {
                return;
            }
            Object randomString = this.generateRandomString(this.randoms.getValueInt());
            if (!((String)randomString).isEmpty()) {
                randomString = " " + (String)randomString;
            }
            switch (this.type.getValue().ordinal()) {
                case 0: {
                    mc.getNetworkHandler().sendChatMessage(this.bot.get(this.random.nextInt(this.bot.size() - 1)) + " " + player.getName().getString() + (String)randomString);
                    break;
                }
                case 1: {
                    mc.getNetworkHandler().sendChatMessage(this.message.getValue().replaceAll("%player%", player.getName().getString()) + (String)randomString);
                    break;
                }
                case 2: {
                    mc.getNetworkHandler().sendChatMessage(this.sex.get(this.random.nextInt(this.sex.size() - 1)) + " " + player.getName().getString() + (String)randomString);
                }
            }
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

    public static enum Type {
        Bot,
        Custom,
        AutoSex;

    }
}

