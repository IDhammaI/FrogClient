/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.PickaxeItem
 *  net.minecraft.item.SwordItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 */
package dev.idhammai.mod.modules.impl.combat;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.ClientTickEvent;
import dev.idhammai.api.events.impl.TotemEvent;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.enums.Timing;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Offhand
extends Module {
    private final EnumSetting<OffhandItem> item = this.add(new EnumSetting<OffhandItem>("Item", OffhandItem.Totem));
    private final BooleanSetting safe = this.add(new BooleanSetting("Safe", true).setParent());
    private final SliderSetting safeHealth = this.add(new SliderSetting("Health", 16.0, 0.0, 36.0, 0.1, this.safe::isOpen));
    private final BooleanSetting lethalCrystal = this.add(new BooleanSetting("LethalCrystal", true, this.safe::isOpen));
    private final BooleanSetting gapSwitch = this.add(new BooleanSetting("GapSwitch", true).setParent());
    private final BooleanSetting always = this.add(new BooleanSetting("Always", false, this.gapSwitch::isOpen));
    private final BooleanSetting gapOnTotem = this.add(new BooleanSetting("Gap-Totem", false, this.gapSwitch::isOpen));
    private final BooleanSetting gapOnSword = this.add(new BooleanSetting("Gap-Sword", true, this.gapSwitch::isOpen));
    private final BooleanSetting gapOnPick = this.add(new BooleanSetting("Gap-Pickaxe", false, this.gapSwitch::isOpen));
    private final BooleanSetting mainHandTotem = this.add(new BooleanSetting("MainHandTotem", false).setParent());
    private final SliderSetting slot = this.add(new SliderSetting("Slot", 1.0, 1.0, 9.0, 1.0, this.mainHandTotem::isOpen));
    private final BooleanSetting forceUpdate = this.add(new BooleanSetting("ForceUpdate", false, this.mainHandTotem::isOpen));
    private final BooleanSetting withOffhand = this.add(new BooleanSetting("WithOffhand", false, this.mainHandTotem::isOpen));
    private final EnumSetting<SwapMode> swapMode = this.add(new EnumSetting<SwapMode>("SwapMode", SwapMode.OffhandSwap));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 50.0, 0.0, 500.0, 1.0));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All));
    private final Timer timer = new Timer();

    public Offhand() {
        super("Offhand", Module.Category.Combat);
        this.setChinese("\u526f\u624b\u7269\u54c1");
    }

    @EventListener
    public void totem(TotemEvent event) {
        if (event.getPlayer() == Offhand.mc.player) {
            if (Offhand.mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
                Offhand.mc.player.getInventory().removeStack(0);
            } else if (Offhand.mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
                Offhand.mc.player.getInventory().offHand.set(0, ItemStack.EMPTY);
            }
        }
    }

    private boolean lethalCrystal() {
        if (!this.lethalCrystal.getValue()) {
            return false;
        }
        for (Entity entity : Frog.THREAD.getEntities()) {
            if (!(entity instanceof EndCrystalEntity) || !(Offhand.mc.player.distanceTo(entity) <= 12.0f)) continue;
            Vec3d vec3d = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
            if (!(AutoCrystal.INSTANCE.calculateDamage(vec3d, (PlayerEntity)Offhand.mc.player, (PlayerEntity)Offhand.mc.player) >= EntityUtil.getHealth((Entity)Offhand.mc.player))) continue;
            return true;
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     */
    @EventListener
    public void onTick(ClientTickEvent event) {
        block22: {
            int hotBarSlot;
            boolean switchMainHandTotem;
            block21: {
                int totemSlot;
                boolean unsafe;
                if (Offhand.nullCheck()) {
                    return;
                }
                if (this.timing.is(Timing.Pre) && event.isPost() || this.timing.is(Timing.Post) && event.isPre()) {
                    return;
                }
                if (!this.timer.passed(this.delay.getValueInt())) {
                    return;
                }
                if (!EntityUtil.inInventory()) {
                    return;
                }
                switchMainHandTotem = Offhand.mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING || this.withOffhand.getValue();
                boolean bl = unsafe = (double)EntityUtil.getHealth((Entity)Offhand.mc.player) < this.safeHealth.getValue() || this.lethalCrystal();
                if (this.mainHandTotem.getValue() && (totemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING)) != -1 && Offhand.mc.player.getInventory().getStack(this.slot.getValueInt() - 1).getItem() != Items.TOTEM_OF_UNDYING) {
                    switch (this.swapMode.getValue().ordinal()) {
                        case 0: {
                            Offhand.mc.interactionManager.clickSlot(Offhand.mc.player.currentScreenHandler.syncId, totemSlot, 0, SlotActionType.PICKUP, (PlayerEntity)Offhand.mc.player);
                            Offhand.mc.interactionManager.clickSlot(Offhand.mc.player.currentScreenHandler.syncId, this.slot.getValueInt() - 1 + 36, 0, SlotActionType.PICKUP, (PlayerEntity)Offhand.mc.player);
                            Offhand.mc.interactionManager.clickSlot(Offhand.mc.player.currentScreenHandler.syncId, totemSlot, 0, SlotActionType.PICKUP, (PlayerEntity)Offhand.mc.player);
                            EntityUtil.syncInventory();
                            break;
                        }
                        case 1: {
                            Offhand.mc.interactionManager.clickSlot(Offhand.mc.player.currentScreenHandler.syncId, totemSlot, this.slot.getValueInt() - 1, SlotActionType.SWAP, (PlayerEntity)Offhand.mc.player);
                            EntityUtil.syncInventory();
                            break;
                        }
                        case 2: {
                            int old = Offhand.mc.player.getInventory().selectedSlot;
                            InventoryUtil.switchToSlot(this.slot.getValueInt() - 1);
                            mc.getNetworkHandler().sendPacket((Packet)new PickFromInventoryC2SPacket(totemSlot));
                            InventoryUtil.switchToSlot(old);
                            break;
                        }
                    }
                    if (switchMainHandTotem && (!this.safe.getValue() || unsafe) && (this.slot.getValueInt() - 1 != Offhand.mc.player.getInventory().selectedSlot || this.forceUpdate.getValue())) {
                        InventoryUtil.switchToSlot(this.slot.getValueInt() - 1);
                    }
                    this.timer.reset();
                    return;
                }
                if (!this.safe.getValue()) break block21;
                if (!unsafe) break block22;
                if (this.mainHandTotem.getValue() && switchMainHandTotem) {
                    hotBarSlot = InventoryUtil.findItem(Items.TOTEM_OF_UNDYING);
                    if (hotBarSlot != -1 && (hotBarSlot != Offhand.mc.player.getInventory().selectedSlot || this.forceUpdate.getValue())) {
                        InventoryUtil.switchToSlot(hotBarSlot);
                    }
                    break block22;
                } else {
                    this.swap(Items.TOTEM_OF_UNDYING);
                    this.timer.reset();
                    return;
                }
            }
            if (this.mainHandTotem.getValue() && switchMainHandTotem && (hotBarSlot = InventoryUtil.findItem(Items.TOTEM_OF_UNDYING)) != -1 && (hotBarSlot != Offhand.mc.player.getInventory().selectedSlot || this.forceUpdate.getValue())) {
                InventoryUtil.switchToSlot(hotBarSlot);
            }
        }
        if ((this.gapOnSword.getValue() && Offhand.mc.player.getMainHandStack().getItem() instanceof SwordItem || this.always.getValue() && Offhand.mc.player.getMainHandStack().getItem() != Items.GOLDEN_APPLE && Offhand.mc.player.getMainHandStack().getItem() != Items.ENCHANTED_GOLDEN_APPLE || this.gapOnPick.getValue() && Offhand.mc.player.getMainHandStack().getItem() instanceof PickaxeItem || this.gapOnTotem.getValue() && Offhand.mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING) && Offhand.mc.options.useKey.isPressed() && this.gapSwitch.getValue()) {
            this.swap(Items.GOLDEN_APPLE);
            this.timer.reset();
            return;
        }
        EnumSetting<OffhandItem> item = this.item;
        OffhandItem i = item.getValue();
        if (i == OffhandItem.Shield) {
            this.swap(Items.SHIELD);
            this.timer.reset();
            return;
        }
        if (i == OffhandItem.Chorus) {
            this.swap(Items.CHORUS_FRUIT);
            this.timer.reset();
            return;
        }
        if (i == OffhandItem.Crystal) {
            this.swap(Items.END_CRYSTAL);
            this.timer.reset();
            return;
        }
        if (i == OffhandItem.Totem) {
            this.swap(Items.TOTEM_OF_UNDYING);
            this.timer.reset();
            return;
        }
        if (i == OffhandItem.Gapple) {
            this.swap(Items.GOLDEN_APPLE);
            this.timer.reset();
        }
    }

    private void swap(Item item) {
        int itemSlot;
        int n = itemSlot = item == Items.GOLDEN_APPLE ? this.getGAppleSlot() : this.findItemInventorySlot(item);
        if (itemSlot == -1) {
            return;
        }
        switch (this.swapMode.getValue().ordinal()) {
            case 1: {
                Offhand.mc.interactionManager.clickSlot(Offhand.mc.player.currentScreenHandler.syncId, itemSlot, 40, SlotActionType.SWAP, (PlayerEntity)Offhand.mc.player);
                EntityUtil.syncInventory();
                break;
            }
            case 2: {
                mc.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(itemSlot));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN, 0));
                mc.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(itemSlot));
                break;
            }
            case 0: {
                Offhand.mc.interactionManager.clickSlot(Offhand.mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, (PlayerEntity)Offhand.mc.player);
                Offhand.mc.interactionManager.clickSlot(Offhand.mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, (PlayerEntity)Offhand.mc.player);
                Offhand.mc.interactionManager.clickSlot(Offhand.mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, (PlayerEntity)Offhand.mc.player);
                EntityUtil.syncInventory();
            }
        }
    }

    private int getGAppleSlot() {
        return this.findItemInventorySlot(Items.ENCHANTED_GOLDEN_APPLE) != -1 ? this.findItemInventorySlot(Items.ENCHANTED_GOLDEN_APPLE) : this.findItemInventorySlot(Items.GOLDEN_APPLE);
    }

    @Override
    public String getInfo() {
        return this.item.getValue().name();
    }

    public int findItemInventorySlot(Item item) {
        if (Offhand.mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE && item == Items.GOLDEN_APPLE) {
            return -1;
        }
        if (Offhand.mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE && (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE)) {
            return -1;
        }
        if (item == Offhand.mc.player.getOffHandStack().getItem()) {
            return -1;
        }
        switch (this.swapMode.getValue().ordinal()) {
            case 2: {
                ItemStack s;
                int i;
                for (i = 9; i < Offhand.mc.player.getInventory().size() + 1; ++i) {
                    s = Offhand.mc.player.getInventory().getStack(i);
                    if (s.getItem() != item) continue;
                    return i;
                }
                for (i = 0; i < 9; ++i) {
                    s = Offhand.mc.player.getInventory().getStack(i);
                    if (s.getItem() != item) continue;
                    return i;
                }
                break;
            }
            case 0: 
            case 1: {
                for (int i = 44; i >= 0; --i) {
                    ItemStack stack = Offhand.mc.player.getInventory().getStack(i);
                    if (stack.getItem() != item) continue;
                    return i < 9 ? i + 36 : i;
                }
                break;
            }
        }
        return -1;
    }

    public static enum OffhandItem {
        None,
        Totem,
        Crystal,
        Gapple,
        Shield,
        Chorus;

    }

    public static enum SwapMode {
        ClickSlot,
        OffhandSwap,
        Pick;

    }
}

