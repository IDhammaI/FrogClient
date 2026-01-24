/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 */
package dev.gzsakura_miitong.api.utils.player;

import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class InventoryUtil
implements Wrapper {
    static int lastSlot = -1;
    static int lastSelect = -1;

    public static void inventorySwap(int slot, int selectedSlot) {
        if (slot == lastSlot) {
            InventoryUtil.switchToSlot(lastSelect);
            lastSlot = -1;
            lastSelect = -1;
            return;
        }
        if (slot - 36 == selectedSlot) {
            return;
        }
        if (!EntityUtil.inInventory()) {
            return;
        }
        if (AntiCheat.INSTANCE.invSwapBypass.getValue()) {
            if (slot - 36 >= 0) {
                lastSlot = slot;
                lastSelect = selectedSlot;
                InventoryUtil.switchToSlot(slot - 36);
                return;
            }
            mc.getNetworkHandler().sendPacket((Packet)new PickFromInventoryC2SPacket(slot));
        } else {
            InventoryUtil.mc.interactionManager.clickSlot(InventoryUtil.mc.player.currentScreenHandler.syncId, slot, selectedSlot, SlotActionType.SWAP, (PlayerEntity)InventoryUtil.mc.player);
            InventoryUtil.mc.player.getInventory().updateItems();
        }
    }

    public static void switchToSlot(int slot) {
        InventoryUtil.mc.player.getInventory().selectedSlot = slot;
        mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot));
    }

    public static int findItem(Item input) {
        for (int i = 0; i < 9; ++i) {
            Item item = InventoryUtil.mc.player.getInventory().getStack(i).getItem();
            if (Item.getRawId((Item)item) != Item.getRawId((Item)input)) continue;
            return i;
        }
        return -1;
    }

    public static int getFood() {
        for (int i = 0; i < 9; ++i) {
            if (!InventoryUtil.mc.player.getInventory().getStack(i).getComponents().contains(DataComponentTypes.FOOD)) continue;
            return i;
        }
        return -1;
    }

    public static int getPotionCount(StatusEffect targetEffect) {
        int count = 0;
        for (int i = 35; i >= 0; --i) {
            ItemStack itemStack = InventoryUtil.mc.player.getInventory().getStack(i);
            if (Item.getRawId((Item)itemStack.getItem()) != Item.getRawId((Item)Items.SPLASH_POTION)) continue;
            PotionContentsComponent potionContentsComponent = (PotionContentsComponent)itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, (Object)PotionContentsComponent.DEFAULT);
            for (StatusEffectInstance effect : potionContentsComponent.getEffects()) {
                if (effect.getEffectType().value() != targetEffect) continue;
                count += itemStack.getCount();
            }
        }
        return count;
    }

    public static int getItemCount(Class<?> clazz) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (!(entry.getValue().getItem() instanceof BlockItem) || !clazz.isInstance(((BlockItem)entry.getValue().getItem()).getBlock())) continue;
            count += entry.getValue().getCount();
        }
        return count;
    }

    public static int getItemCount(Item item) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().getItem() != item) continue;
            count += entry.getValue().getCount();
        }
        if (InventoryUtil.mc.player.getOffHandStack().getItem() == item) {
            count += InventoryUtil.mc.player.getOffHandStack().getCount();
        }
        return count;
    }

    public static int findClass(Class<?> clazz) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY) continue;
            if (clazz.isInstance(stack.getItem())) {
                return i;
            }
            if (!(stack.getItem() instanceof BlockItem) || !clazz.isInstance(((BlockItem)stack.getItem()).getBlock())) continue;
            return i;
        }
        return -1;
    }

    public static int findClassInventorySlot(Class<?> clazz) {
        if (AntiCheat.INSTANCE.priorHotbar.getValue()) {
            for (int i = 0; i < 36; ++i) {
                ItemStack stack = InventoryUtil.mc.player.getInventory().getStack(i);
                if (stack == ItemStack.EMPTY) continue;
                if (clazz.isInstance(stack.getItem())) {
                    return i < 9 ? i + 36 : i;
                }
                if (!(stack.getItem() instanceof BlockItem) || !clazz.isInstance(((BlockItem)stack.getItem()).getBlock())) continue;
                return i < 9 ? i + 36 : i;
            }
        } else {
            for (int i = 35; i >= 0; --i) {
                ItemStack stack = InventoryUtil.mc.player.getInventory().getStack(i);
                if (stack == ItemStack.EMPTY) continue;
                if (clazz.isInstance(stack.getItem())) {
                    return i < 9 ? i + 36 : i;
                }
                if (!(stack.getItem() instanceof BlockItem) || !clazz.isInstance(((BlockItem)stack.getItem()).getBlock())) continue;
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    public static int findBlock(Block blockIn) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof BlockItem) || ((BlockItem)stack.getItem()).getBlock() != blockIn) continue;
            return i;
        }
        return -1;
    }

    public static int findUnBlock() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem) continue;
            return i;
        }
        return -1;
    }

    public static int findBlock() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof BlockItem) || BlockUtil.isClickable(Block.getBlockFromItem((Item)stack.getItem())) || ((BlockItem)stack.getItem()).getBlock() == Blocks.COBWEB) continue;
            return i;
        }
        return -1;
    }

    public static int findBlockInventorySlot(Block block) {
        return InventoryUtil.findItemInventorySlot(block.asItem());
    }

    public static int findItemInventorySlot(Item item) {
        if (AntiCheat.INSTANCE.priorHotbar.getValue()) {
            return InventoryUtil.findItemInventorySlotFromZero(item.asItem());
        }
        for (int i = 35; i >= 0; --i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().getStack(i);
            if (stack.getItem() != item) continue;
            return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    public static int findItemInventorySlotFromZero(Item item) {
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().getStack(i);
            if (stack.getItem() != item) continue;
            return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
        HashMap<Integer, ItemStack> fullInventorySlots = new HashMap<Integer, ItemStack>();
        for (int current = 0; current <= 35; ++current) {
            fullInventorySlots.put(current, InventoryUtil.mc.player.getInventory().getStack(current));
        }
        return fullInventorySlots;
    }
}

