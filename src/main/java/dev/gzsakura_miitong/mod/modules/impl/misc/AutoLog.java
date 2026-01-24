/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.network.packet.s2c.common.DisconnectS2CPacket
 *  net.minecraft.text.Text
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.TotemEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.player.EntityUtil;
import dev.gzsakura_miitong.api.utils.player.InventoryUtil;
import dev.gzsakura_miitong.core.impl.CommandManager;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

public class AutoLog
extends Module {
    public static boolean loggedOut = false;
    private final BooleanSetting logOnEnable = this.add(new BooleanSetting("LogOnEnable", false));
    private final BooleanSetting onPop = this.add(new BooleanSetting("OnPop", true));
    private final BooleanSetting lowArmor = this.add(new BooleanSetting("LowArmor", true));
    private final BooleanSetting totemLess = this.add(new BooleanSetting("TotemLess", true).setParent());
    private final SliderSetting totems = this.add(new SliderSetting("Totems", 2.0, 0.0, 20.0, 1.0, this.totemLess::isOpen));
    private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
    private final BooleanSetting showReason = this.add(new BooleanSetting("ShowReason", false));

    public AutoLog() {
        super("AutoLog", Module.Category.Misc);
        this.setChinese("\u81ea\u52a8\u4e0b\u7ebf");
    }

    @Override
    public void onEnable() {
        if (this.logOnEnable.getValue()) {
            this.disconnect("Enabled");
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        int totem;
        if (this.totemLess.getValue() && (double)(totem = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING)) <= this.totems.getValue()) {
            this.disconnect("You have too few totems (" + totem + ").");
            return;
        }
        if (this.lowArmor.getValue()) {
            for (ItemStack armor : AutoLog.mc.player.getInventory().armor) {
                int damage;
                if (armor.isEmpty() || (damage = EntityUtil.getDamagePercent(armor)) >= 5) continue;
                this.disconnect("Your armor has keyCodec durability of less than 5%.");
                return;
            }
        }
    }

    @EventListener
    public void onPop(TotemEvent event) {
        if (this.onPop.getValue() && event.getPlayer() == AutoLog.mc.player) {
            this.disconnect("You poped 1 totem!");
        }
    }

    @Override
    public void onLogout() {
        if (this.autoDisable.getValue()) {
            this.disable();
        }
    }

    private void disconnect(String reason) {
        loggedOut = true;
        CommandManager.sendMessage("\u00a74[AutoLog] " + reason);
        mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(114514));
        if (this.showReason.getValue()) {
            AutoLog.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket((Text)Text.literal((String)("[AutoLog]" + reason))));
        }
    }
}

