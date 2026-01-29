/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.screen.slot.SlotActionType
 */
package dev.idhammai.mod.modules.impl.player;

import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class Replenish
extends Module {
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.QuickMove));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 2.0, 0.0, 5.0, 0.01).setSuffix("s"));
    private final SliderSetting min = this.add(new SliderSetting("Min", 50, 1, 100)).setSuffix("%");
    private final SliderSetting forceDelay = this.add(new SliderSetting("ForceDelay", 0.2, 0.0, 4.0, 0.01).setSuffix("s"));
    private final SliderSetting forceMin = this.add(new SliderSetting("ForceMin", 16, 1, 100)).setSuffix("%");
    private final Timer timer = new Timer();

    public Replenish() {
        super("Replenish", Module.Category.Player);
        this.setChinese("\u7269\u54c1\u680f\u8865\u5145");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        for (int i = 0; i < 9; ++i) {
            if (!this.replenish(i)) continue;
            this.timer.reset();
            return;
        }
    }

    private boolean replenish(int slot) {
        ItemStack stack = Replenish.mc.player.getInventory().getStack(slot);
        if (stack.isEmpty()) {
            return false;
        }
        if (!stack.isStackable()) {
            return false;
        }
        int percent = (int)((double)stack.getCount() / (double)stack.getMaxCount() * 100.0);
        if ((double)percent > this.min.getValue()) {
            return false;
        }
        for (int i = 9; i < 36; ++i) {
            ItemStack item = Replenish.mc.player.getInventory().getStack(i);
            if (item.isEmpty() || !Sorter.canMerge(stack, item)) continue;
            if ((float)percent > this.forceMin.getValueFloat() ? !this.timer.passedS(this.delay.getValue()) : !this.timer.passedS(this.forceDelay.getValue())) {
                return false;
            }
            switch (this.mode.getValue().ordinal()) {
                case 0: {
                    Replenish.mc.interactionManager.clickSlot(Replenish.mc.player.playerScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Replenish.mc.player);
                    break;
                }
                case 1: {
                    Replenish.mc.interactionManager.clickSlot(Replenish.mc.player.playerScreenHandler.syncId, i, 0, SlotActionType.PICKUP, (PlayerEntity)Replenish.mc.player);
                    Replenish.mc.interactionManager.clickSlot(Replenish.mc.player.playerScreenHandler.syncId, slot + 36, 0, SlotActionType.PICKUP, (PlayerEntity)Replenish.mc.player);
                    Replenish.mc.interactionManager.clickSlot(Replenish.mc.player.playerScreenHandler.syncId, i, 0, SlotActionType.PICKUP, (PlayerEntity)Replenish.mc.player);
                }
            }
            return true;
        }
        return false;
    }

    public static enum Mode {
        QuickMove,
        ClickSlot;

    }
}

