/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.type.ItemEnchantmentsComponent
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.ElytraItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 */
package dev.idhammai.mod.modules.impl.player;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.api.utils.player.MovementUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.movement.ElytraFly;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoArmor
extends Module {
    public static AutoArmor INSTANCE;
    private final EnumSetting<EnchantPriority> head = this.add(new EnumSetting<EnchantPriority>("Head", EnchantPriority.Protection));
    private final EnumSetting<EnchantPriority> body = this.add(new EnumSetting<EnchantPriority>("Body", EnchantPriority.Protection));
    private final EnumSetting<EnchantPriority> tights = this.add(new EnumSetting<EnchantPriority>("Tights", EnchantPriority.Protection));
    private final EnumSetting<EnchantPriority> feet = this.add(new EnumSetting<EnchantPriority>("Feet", EnchantPriority.Protection));
    private final BooleanSetting ignoreCurse = this.add(new BooleanSetting("IgnoreCurse", true));
    private final BooleanSetting noMove = this.add(new BooleanSetting("NoMove", false));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 3.0, 0.0, 10.0, 1.0));
    private final BooleanSetting autoElytra = this.add(new BooleanSetting("AutoElytra", true));
    private final EnumSetting<HotbarSwapMode> hotbarSwap = this.add(new EnumSetting<HotbarSwapMode>("HotbarSwap", HotbarSwapMode.Swap));
    private final EnumSetting<InventorySwapMode> inventorySwap = this.add(new EnumSetting<InventorySwapMode>("InventorySwap", InventorySwapMode.ClickSlot));
    private int tickDelay = 0;

    public AutoArmor() {
        super("AutoArmor", Module.Category.Player);
        this.setChinese("\u81ea\u52a8\u7a7f\u7532");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!EntityUtil.inInventory()) {
            return;
        }
        if (AutoArmor.mc.player.playerScreenHandler != AutoArmor.mc.player.currentScreenHandler) {
            return;
        }
        if (MovementUtil.isMoving() && this.noMove.getValue()) {
            return;
        }
        if (this.tickDelay > 0) {
            --this.tickDelay;
            return;
        }
        this.tickDelay = this.delay.getValueInt();
        HashMap<EquipmentSlot, int[]> armorMap = new HashMap<EquipmentSlot, int[]>(4);
        armorMap.put(EquipmentSlot.FEET, new int[]{36, this.getProtection(AutoArmor.mc.player.getInventory().getStack(36)), -1, -1});
        armorMap.put(EquipmentSlot.LEGS, new int[]{37, this.getProtection(AutoArmor.mc.player.getInventory().getStack(37)), -1, -1});
        armorMap.put(EquipmentSlot.CHEST, new int[]{38, this.getProtection(AutoArmor.mc.player.getInventory().getStack(38)), -1, -1});
        armorMap.put(EquipmentSlot.HEAD, new int[]{39, this.getProtection(AutoArmor.mc.player.getInventory().getStack(39)), -1, -1});
        for (int s = 0; s < 36; ++s) {
            if (!(AutoArmor.mc.player.getInventory().getStack(s).getItem() instanceof ArmorItem) && AutoArmor.mc.player.getInventory().getStack(s).getItem() != Items.ELYTRA || AutoArmor.mc.player.getInventory().getStack(s).getItem() == Items.ELYTRA && (ElytraFly.INSTANCE.isOff() && this.autoElytra.getValue() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.packet.getValue())) continue;
            int protection = this.getProtection(AutoArmor.mc.player.getInventory().getStack(s));
            EquipmentSlot slot = AutoArmor.mc.player.getInventory().getStack(s).getItem() instanceof ElytraItem ? EquipmentSlot.CHEST : ((ArmorItem)AutoArmor.mc.player.getInventory().getStack(s).getItem()).getSlotType();
            for (Map.Entry e : armorMap.entrySet()) {
                if (this.autoElytra.getValue() && ElytraFly.INSTANCE.isOn() && e.getKey() == EquipmentSlot.CHEST) {
                    if (!AutoArmor.mc.player.getInventory().getStack(38).isEmpty() && AutoArmor.mc.player.getInventory().getStack(38).getItem() instanceof ElytraItem && ElytraItem.isUsable((ItemStack)AutoArmor.mc.player.getInventory().getStack(38)) || ((int[])e.getValue())[2] != -1 && !AutoArmor.mc.player.getInventory().getStack(((int[])e.getValue())[2]).isEmpty() && AutoArmor.mc.player.getInventory().getStack(((int[])e.getValue())[2]).getItem() instanceof ElytraItem && ElytraItem.isUsable((ItemStack)AutoArmor.mc.player.getInventory().getStack(((int[])e.getValue())[2])) || AutoArmor.mc.player.getInventory().getStack(s).isEmpty() || !(AutoArmor.mc.player.getInventory().getStack(s).getItem() instanceof ElytraItem) || !ElytraItem.isUsable((ItemStack)AutoArmor.mc.player.getInventory().getStack(s))) continue;
                    ((int[])e.getValue())[2] = s;
                    continue;
                }
                if (protection <= 0 || e.getKey() != slot || protection <= ((int[])e.getValue())[1] || protection <= ((int[])e.getValue())[3]) continue;
                ((int[])e.getValue())[2] = s;
                ((int[])e.getValue())[3] = protection;
            }
        }
        for (Map.Entry equipmentSlotEntry : armorMap.entrySet()) {
            if (((int[])equipmentSlotEntry.getValue())[2] == -1) continue;
            if (((int[])equipmentSlotEntry.getValue())[2] < 9) {
                switch (this.hotbarSwap.getValue().ordinal()) {
                    case 0: {
                        int armorSlot = 44 - ((int[])equipmentSlotEntry.getValue())[0];
                        AutoArmor.mc.interactionManager.clickSlot(AutoArmor.mc.player.currentScreenHandler.syncId, armorSlot, ((int[])equipmentSlotEntry.getValue())[2], SlotActionType.SWAP, (PlayerEntity)AutoArmor.mc.player);
                        EntityUtil.syncInventory();
                        break;
                    }
                    case 1: {
                        int old = AutoArmor.mc.player.getInventory().selectedSlot;
                        InventoryUtil.switchToSlot(((int[])equipmentSlotEntry.getValue())[2]);
                        AutoArmor.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Frog.ROTATION.getLastYaw(), Frog.ROTATION.getLastPitch()));
                        InventoryUtil.switchToSlot(old);
                    }
                }
            } else if (AutoArmor.mc.player.playerScreenHandler == AutoArmor.mc.player.currentScreenHandler) {
                int armorSlot = 44 - ((int[])equipmentSlotEntry.getValue())[0];
                int newArmorSlot = ((int[])equipmentSlotEntry.getValue())[2];
                switch (this.inventorySwap.getValue().ordinal()) {
                    case 1: {
                        mc.getNetworkHandler().sendPacket((Packet)new PickFromInventoryC2SPacket(newArmorSlot));
                        AutoArmor.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Frog.ROTATION.getLastYaw(), Frog.ROTATION.getLastPitch()));
                        mc.getNetworkHandler().sendPacket((Packet)new PickFromInventoryC2SPacket(newArmorSlot));
                        break;
                    }
                    case 0: {
                        AutoArmor.mc.interactionManager.clickSlot(AutoArmor.mc.player.currentScreenHandler.syncId, newArmorSlot, 0, SlotActionType.PICKUP, (PlayerEntity)AutoArmor.mc.player);
                        AutoArmor.mc.interactionManager.clickSlot(AutoArmor.mc.player.currentScreenHandler.syncId, armorSlot, 0, SlotActionType.PICKUP, (PlayerEntity)AutoArmor.mc.player);
                        if (((int[])equipmentSlotEntry.getValue())[1] == -1) break;
                        AutoArmor.mc.interactionManager.clickSlot(AutoArmor.mc.player.currentScreenHandler.syncId, newArmorSlot, 0, SlotActionType.PICKUP, (PlayerEntity)AutoArmor.mc.player);
                    }
                }
                EntityUtil.syncInventory();
            }
            return;
        }
    }

    private int getProtection(ItemStack is) {
        if (is.getItem() instanceof ArmorItem || is.getItem() == Items.ELYTRA) {
            EquipmentSlot slot;
            int prot = 0;
            Item item = is.getItem();
            if (item instanceof ArmorItem) {
                ArmorItem ai = (ArmorItem)item;
                slot = ai.getSlotType();
            } else {
                slot = EquipmentSlot.BODY;
            }
            if (is.getItem() instanceof ElytraItem) {
                if (!ElytraItem.isUsable((ItemStack)is)) {
                    return 0;
                }
                prot = 1;
            }
            int blastMultiplier = 1;
            int protectionMultiplier = 1;
            switch (slot) {
                case HEAD: {
                    if (this.head.is(EnchantPriority.Protection)) {
                        protectionMultiplier *= 2;
                        break;
                    }
                    blastMultiplier *= 2;
                    break;
                }
                case BODY: {
                    if (this.body.is(EnchantPriority.Protection)) {
                        protectionMultiplier *= 2;
                        break;
                    }
                    blastMultiplier *= 2;
                    break;
                }
                case LEGS: {
                    if (this.tights.is(EnchantPriority.Protection)) {
                        protectionMultiplier *= 2;
                        break;
                    }
                    blastMultiplier *= 2;
                    break;
                }
                case FEET: {
                    if (this.feet.is(EnchantPriority.Protection)) {
                        protectionMultiplier *= 2;
                        break;
                    }
                    blastMultiplier *= 2;
                }
            }
            if (is.hasEnchantments()) {
                ItemEnchantmentsComponent enchants = EnchantmentHelper.getEnchantments((ItemStack)is);
                if (enchants.getEnchantments().contains(AutoArmor.mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.PROTECTION).get())) {
                    prot += enchants.getLevel((RegistryEntry)AutoArmor.mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.PROTECTION).get()) * protectionMultiplier;
                }
                if (enchants.getEnchantments().contains(AutoArmor.mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get())) {
                    prot += enchants.getLevel((RegistryEntry)AutoArmor.mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get()) * blastMultiplier;
                }
                if (enchants.getEnchantments().contains(AutoArmor.mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BINDING_CURSE).get()) && this.ignoreCurse.getValue()) {
                    prot = -999;
                }
            }
            return (is.getItem() instanceof ArmorItem ? ((ArmorItem)is.getItem()).getProtection() : 0) + prot;
        }
        if (!is.isEmpty()) {
            return 0;
        }
        return -1;
    }

    private static enum EnchantPriority {
        Blast,
        Protection;

    }

    public static enum HotbarSwapMode {
        Swap,
        Switch;

    }

    public static enum InventorySwapMode {
        ClickSlot,
        Pick;

    }
}

