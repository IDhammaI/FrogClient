/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.BowItem
 *  net.minecraft.item.Items
 *  net.minecraft.util.Hand
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.events.impl.UpdateRotateEvent;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BindSetting;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class Quiver
extends Module {
    private final BooleanSetting instant = this.add(new BooleanSetting("InstantRotate", true));
    private final SliderSetting time = this.add(new SliderSetting("Time", (double)0.11f, 0.0, 1.0, 0.01));
    private final BooleanSetting onlyPress = this.add(new BooleanSetting("OnlyPress", false));
    private final BindSetting key = this.add(new BindSetting("ActiveKey", -1));
    boolean bow = false;
    boolean pressed = false;
    boolean switching = false;
    int startSlot;

    public Quiver() {
        super("Quiver", Module.Category.Combat);
        this.setChinese("\u5934\u9876\u5c04\u7bad");
    }

    @Override
    public void onEnable() {
        this.bow = false;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.key.isPressed()) {
            int bow;
            if (!this.pressed && !this.switching && (bow = InventoryUtil.findItem(Items.BOW)) != -1) {
                this.startSlot = Quiver.mc.player.getInventory().selectedSlot;
                InventoryUtil.switchToSlot(bow);
                Quiver.mc.options.useKey.setPressed(true);
                this.switching = true;
                this.pressed = true;
            }
        } else {
            this.pressed = false;
        }
        if (this.switching && (!Quiver.mc.options.useKey.isPressed() || Quiver.mc.player.isUsingItem() && Quiver.mc.player.getActiveItem().getItem() != Items.BOW)) {
            InventoryUtil.switchToSlot(this.startSlot);
            this.switching = false;
        }
        boolean bl = Quiver.mc.player.isUsingItem() && (Quiver.mc.player.getActiveHand() == Hand.MAIN_HAND ? Quiver.mc.player.getMainHandStack() : Quiver.mc.player.getOffHandStack()).getItem() instanceof BowItem ? true : (this.bow = false);
        if (this.bow && (!this.onlyPress.getValue() || this.switching) && (double)BowItem.getPullProgress((int)Quiver.mc.player.getItemUseTime()) >= this.time.getValue()) {
            if (this.instant.getValue()) {
                Frog.ROTATION.snapAt(Frog.ROTATION.rotationYaw, -90.0f);
            }
            Quiver.mc.options.useKey.setPressed(false);
            Quiver.mc.interactionManager.stopUsingItem((PlayerEntity)Quiver.mc.player);
            if (this.instant.getValue()) {
                Frog.ROTATION.snapBack();
            }
        }
    }

    @EventListener
    public void onRotate(UpdateRotateEvent event) {
        if (this.bow && !this.instant.getValue()) {
            event.setPitch(-90.0f);
        }
    }
}

