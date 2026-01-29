/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BedBlock
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.LadderBlock
 *  net.minecraft.block.PistonBlock
 *  net.minecraft.block.ShulkerBoxBlock
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.screen.slot.SlotActionType
 *  org.apache.commons.io.IOUtils
 */
package dev.idhammai.mod.modules.impl.player;

import dev.idhammai.Frog;
import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.core.Manager;
import dev.idhammai.mod.gui.windows.WindowsScreen;
import dev.idhammai.mod.gui.windows.impl.ItemSelectWindow;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import dev.idhammai.mod.modules.settings.impl.StringSetting;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.io.IOUtils;

public class Sorter
extends Module {
    public static Sorter INSTANCE;
    final int[] stealCountList = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private final SliderSetting tasksPerTicks = this.add(new SliderSetting("TasksPerTick", 2, 1, 20));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 0.1, 0.0, 5.0, 0.01).setSuffix("s"));
    private final BooleanSetting stack = this.add(new BooleanSetting("Stack", true));
    private final EnumSetting<Mode> trashMode = this.add(new EnumSetting<Mode>("TrashMode", Mode.Whitelist));
    private final BooleanSetting edit = this.add(new BooleanSetting("EditTrash", false).injectTask(this::openGui));
    private final BooleanSetting sort = this.add(new BooleanSetting("Sort", true));
    private final BooleanSetting kit = this.add(new BooleanSetting("Kit", false).injectTask(this::onEnable));
    private final StringSetting kitName = this.add(new StringSetting("KitName", "kit1"));
    private final BooleanSetting drop = this.add(new BooleanSetting("Drop", true).setParent());
    private final BooleanSetting trash = this.add(new BooleanSetting("Trash", true, this.drop::isOpen));
    private final BooleanSetting rename = this.add(new BooleanSetting("Rename", true, this.drop::isOpen));
    private final BooleanSetting kitExceed = this.add(new BooleanSetting("KitExceed", true, this.drop::isOpen));
    private final BooleanSetting exceed = this.add(new BooleanSetting("Exceed", true, this.drop::isOpen));
    private final SliderSetting crystal = this.add(new SliderSetting("Crystal", 4, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting exp = this.add(new SliderSetting("Exp", 4, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting totem = this.add(new SliderSetting("Totem", 6, 0, 36, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting eGapple = this.add(new SliderSetting("EGapple", 2, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting gapple = this.add(new SliderSetting("Gapple", 2, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting obsidian = this.add(new SliderSetting("Obsidian", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting web = this.add(new SliderSetting("Web", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting glowstone = this.add(new SliderSetting("Glowstone", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting anchor = this.add(new SliderSetting("Anchor", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting pearl = this.add(new SliderSetting("Pearl", 1, 0, 8, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting piston = this.add(new SliderSetting("Piston", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting redstone = this.add(new SliderSetting("RedStone", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting ladder = this.add(new SliderSetting("Ladder", 2, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting bed = this.add(new SliderSetting("Bed", 4, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting speed = this.add(new SliderSetting("Speed", 1, 0, 8, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting turtle = this.add(new SliderSetting("Resistance", 1, 0, 8, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting strength = this.add(new SliderSetting("Strength", 1, 0, 8, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final Timer timer = new Timer();
    private final Map<Integer, String> kitMap = new ConcurrentHashMap<Integer, String>();

    public Sorter() {
        super("Sorter", Module.Category.Player);
        this.setChinese("\u80cc\u5305\u6574\u7406");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (Sorter.nullCheck()) {
            return;
        }
        Frog.THREAD.execute(() -> {
            this.kitMap.clear();
            try {
                File file = Manager.getFile(this.kitName.getValue() + ".kit");
                if (!file.exists()) {
                    return;
                }
                List<String> list = IOUtils.readLines((InputStream)new FileInputStream(file), (Charset)StandardCharsets.UTF_8);
                for (String s : list) {
                    String[] split = s.split(":");
                    if (split.length != 2) {
                        return;
                    }
                    this.kitMap.put(Integer.valueOf(split[0]), split[1]);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static int getItemCount(Item item) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().getItem() != item) continue;
            ++count;
        }
        if (Sorter.mc.player.getOffHandStack().getItem() == item) {
            ++count;
        }
        return count;
    }

    public static int getItemCount(Class<?> clazz) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (!(entry.getValue().getItem() instanceof BlockItem) || !clazz.isInstance(((BlockItem)entry.getValue().getItem()).getBlock())) continue;
            ++count;
        }
        return count;
    }

    private void openGui() {
        this.edit.setValueWithoutTask(false);
        if (!Sorter.nullCheck()) {
            mc.setScreen((Screen)new WindowsScreen(new ItemSelectWindow(Frog.CLEANER)));
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!this.timer.passedS(this.delay.getValue())) {
            return;
        }
        if (!EntityUtil.inInventory()) {
            return;
        }
        if (this.exceed.getValue()) {
            this.updateItem();
        }
        int i = 0;
        while ((double)i < this.tasksPerTicks.getValue()) {
            this.tweak();
            ++i;
        }
    }

    private void tweak() {
        ItemStack stack;
        int slot1;
        if (this.drop.getValue()) {
            for (slot1 = 35; slot1 >= 0; --slot1) {
                stack = Sorter.mc.player.getInventory().getStack(slot1);
                if (stack.isEmpty() || !this.shouldDrop(stack)) continue;
                this.timer.reset();
                Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.currentScreenHandler.syncId, slot1 < 9 ? slot1 + 36 : slot1, 1, SlotActionType.THROW, (PlayerEntity)Sorter.mc.player);
                return;
            }
        }
        if (this.stack.getValue()) {
            for (slot1 = 35; slot1 >= 9; --slot1) {
                stack = Sorter.mc.player.getInventory().getStack(slot1);
                if (stack.isEmpty() || !stack.isStackable() || stack.getCount() == stack.getMaxCount()) continue;
                for (int slot2 = 0; slot2 < 36; ++slot2) {
                    ItemStack stack2;
                    if (slot1 == slot2 || (stack2 = Sorter.mc.player.getInventory().getStack(slot2)).getCount() == stack2.getMaxCount() || !Sorter.canMerge(stack, stack2)) continue;
                    Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.playerScreenHandler.syncId, slot1, 0, SlotActionType.PICKUP, (PlayerEntity)Sorter.mc.player);
                    Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.playerScreenHandler.syncId, slot2 < 9 ? slot2 + 36 : slot2, 0, SlotActionType.PICKUP, (PlayerEntity)Sorter.mc.player);
                    Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.playerScreenHandler.syncId, slot1, 0, SlotActionType.PICKUP, (PlayerEntity)Sorter.mc.player);
                    this.timer.reset();
                    return;
                }
            }
        }
        if (this.drop.getValue()) {
            for (slot1 = 35; slot1 >= 0; --slot1) {
                stack = Sorter.mc.player.getInventory().getStack(slot1);
                if (stack.isEmpty() || !this.exceed.getValue() || !this.exceed(stack, false)) continue;
                this.timer.reset();
                Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.currentScreenHandler.syncId, slot1 < 9 ? slot1 + 36 : slot1, 1, SlotActionType.THROW, (PlayerEntity)Sorter.mc.player);
                return;
            }
        }
        if (this.sort.getValue()) {
            if (this.kit.getValue()) {
                for (slot1 = 0; slot1 < 36; ++slot1) {
                    if (!this.kitMap.containsKey(slot1)) continue;
                    String target = this.kitMap.get(slot1);
                    String name = Sorter.mc.player.getInventory().getStack(slot1).getItem().getTranslationKey();
                    if (name.equals(target)) continue;
                    for (int slot2 = 0; slot2 < 36; ++slot2) {
                        String itemID;
                        String slot2Target = this.kitMap.get(slot2);
                        ItemStack stack2 = Sorter.mc.player.getInventory().getStack(slot2);
                        if (stack2.isEmpty() || (itemID = stack2.getItem().getTranslationKey()).equals(slot2Target) || !itemID.equals(target)) continue;
                        Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.playerScreenHandler.syncId, slot1 < 9 ? slot1 + 36 : slot1, 0, SlotActionType.PICKUP, (PlayerEntity)Sorter.mc.player);
                        Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.playerScreenHandler.syncId, slot2 < 9 ? slot2 + 36 : slot2, 0, SlotActionType.PICKUP, (PlayerEntity)Sorter.mc.player);
                        Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.playerScreenHandler.syncId, slot1 < 9 ? slot1 + 36 : slot1, 0, SlotActionType.PICKUP, (PlayerEntity)Sorter.mc.player);
                        this.timer.reset();
                        return;
                    }
                }
            } else {
                for (slot1 = 9; slot1 < 36; ++slot1) {
                    int minId;
                    int id = Item.getRawId((Item)Sorter.mc.player.getInventory().getStack(slot1).getItem());
                    if (Sorter.mc.player.getInventory().getStack(slot1).isEmpty()) {
                        id = 114514;
                    }
                    if ((minId = this.getMinId(slot1, id)) >= id) continue;
                    for (int slot2 = 35; slot2 > slot1; --slot2) {
                        int itemID;
                        ItemStack stack3 = Sorter.mc.player.getInventory().getStack(slot2);
                        if (stack3.isEmpty() || (itemID = Item.getRawId((Item)stack3.getItem())) != minId) continue;
                        Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.playerScreenHandler.syncId, slot1, 0, SlotActionType.PICKUP, (PlayerEntity)Sorter.mc.player);
                        Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.playerScreenHandler.syncId, slot2, 0, SlotActionType.PICKUP, (PlayerEntity)Sorter.mc.player);
                        Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.playerScreenHandler.syncId, slot1, 0, SlotActionType.PICKUP, (PlayerEntity)Sorter.mc.player);
                        this.timer.reset();
                        return;
                    }
                }
            }
        }
        if (this.drop.getValue() && this.kitExceed.getValue()) {
            for (slot1 = 35; slot1 >= 0; --slot1) {
                BlockItem blockItem;
                Item item;
                if (!this.kitMap.containsKey(slot1)) continue;
                ItemStack stack4 = Sorter.mc.player.getInventory().getStack(slot1);
                if (this.exceed.getValue() && !this.exceed(stack4, true) || stack4.isEmpty() || stack4.getItem() instanceof ArmorItem || (item = stack4.getItem()) instanceof BlockItem && (blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock || stack4.getItem().getTranslationKey().equals(this.kitMap.get(slot1))) continue;
                this.timer.reset();
                Sorter.mc.interactionManager.clickSlot(Sorter.mc.player.currentScreenHandler.syncId, slot1 < 9 ? slot1 + 36 : slot1, 1, SlotActionType.THROW, (PlayerEntity)Sorter.mc.player);
                return;
            }
        }
    }

    private boolean shouldDrop(ItemStack stack) {
        boolean inList;
        BlockItem blockItem;
        Item item = stack.getItem();
        if (this.trash.getValue() && (!(item instanceof BlockItem) || !((blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock)) && (!(inList = Frog.CLEANER.inList(item.getTranslationKey())) && this.trashMode.is(Mode.Whitelist) || inList && this.trashMode.is(Mode.Blacklist))) {
            return true;
        }
        if (this.rename.getValue()) {
            return stack.isStackable() && !stack.getName().getString().equals(item.getName().getString());
        }
        return false;
    }

    private boolean exceed(ItemStack i, boolean dropOther) {
        if (i.getItem().equals(Items.END_CRYSTAL)) {
            if ((double)this.stealCountList[0] > this.crystal.getValue()) {
                this.stealCountList[0] = this.stealCountList[0] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem().equals(Items.EXPERIENCE_BOTTLE)) {
            if ((double)this.stealCountList[1] > this.exp.getValue()) {
                this.stealCountList[1] = this.stealCountList[1] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem().equals(Items.TOTEM_OF_UNDYING)) {
            if ((double)this.stealCountList[2] > this.totem.getValue()) {
                this.stealCountList[2] = this.stealCountList[2] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)) {
            if ((double)this.stealCountList[3] > this.eGapple.getValue()) {
                this.stealCountList[3] = this.stealCountList[3] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem().equals(Blocks.OBSIDIAN.asItem())) {
            if ((double)this.stealCountList[4] > this.obsidian.getValue()) {
                this.stealCountList[4] = this.stealCountList[4] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem().equals(Blocks.COBWEB.asItem())) {
            if ((double)this.stealCountList[5] > this.web.getValue()) {
                this.stealCountList[5] = this.stealCountList[5] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem().equals(Blocks.GLOWSTONE.asItem())) {
            if ((double)this.stealCountList[6] > this.glowstone.getValue()) {
                this.stealCountList[6] = this.stealCountList[6] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem().equals(Blocks.RESPAWN_ANCHOR.asItem())) {
            if ((double)this.stealCountList[7] > this.anchor.getValue()) {
                this.stealCountList[7] = this.stealCountList[7] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem().equals(Items.ENDER_PEARL)) {
            if ((double)this.stealCountList[8] > this.pearl.getValue()) {
                this.stealCountList[8] = this.stealCountList[8] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem() instanceof BlockItem && ((BlockItem)i.getItem()).getBlock() instanceof PistonBlock) {
            if ((double)this.stealCountList[9] > this.piston.getValue()) {
                this.stealCountList[9] = this.stealCountList[9] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem().equals(Blocks.REDSTONE_BLOCK.asItem())) {
            if ((double)this.stealCountList[10] > this.redstone.getValue()) {
                this.stealCountList[10] = this.stealCountList[10] - 1;
                return true;
            }
            return false;
        }
        if (i.getItem() instanceof BlockItem && ((BlockItem)i.getItem()).getBlock() instanceof BedBlock) {
            if ((double)this.stealCountList[11] > this.bed.getValue()) {
                this.stealCountList[11] = this.stealCountList[11] - 1;
                return true;
            }
            return false;
        }
        if (Item.getRawId((Item)i.getItem()) == Item.getRawId((Item)Items.SPLASH_POTION)) {
            PotionContentsComponent potionContentsComponent = (PotionContentsComponent)i.getOrDefault(DataComponentTypes.POTION_CONTENTS, (Object)PotionContentsComponent.DEFAULT);
            for (StatusEffectInstance effect : potionContentsComponent.getEffects()) {
                if (effect.getEffectType().value() == StatusEffects.SPEED.value()) {
                    if ((double)this.stealCountList[12] > this.speed.getValue()) {
                        this.stealCountList[12] = this.stealCountList[12] - 1;
                        return true;
                    }
                    return false;
                }
                if (effect.getEffectType().value() == StatusEffects.RESISTANCE.value()) {
                    if ((double)this.stealCountList[13] > this.turtle.getValue()) {
                        this.stealCountList[13] = this.stealCountList[13] - 1;
                        return true;
                    }
                    return false;
                }
                if (effect.getEffectType().value() != StatusEffects.STRENGTH.value()) continue;
                if ((double)this.stealCountList[16] > this.strength.getValue()) {
                    this.stealCountList[16] = this.stealCountList[16] - 1;
                    return true;
                }
                return false;
            }
        }
        if (i.getItem().equals(Items.GOLDEN_APPLE) && (double)this.stealCountList[14] > this.gapple.getValue()) {
            this.stealCountList[14] = this.stealCountList[14] - 1;
            return true;
        }
        if (i.getItem() instanceof BlockItem && ((BlockItem)i.getItem()).getBlock() instanceof LadderBlock && (double)this.stealCountList[15] > this.ladder.getValue()) {
            this.stealCountList[15] = this.stealCountList[15] - 1;
            return true;
        }
        return dropOther;
    }

    private void updateItem() {
        this.stealCountList[0] = Sorter.getItemCount(Items.END_CRYSTAL);
        this.stealCountList[1] = Sorter.getItemCount(Items.EXPERIENCE_BOTTLE);
        this.stealCountList[2] = Sorter.getItemCount(Items.TOTEM_OF_UNDYING);
        this.stealCountList[3] = Sorter.getItemCount(Items.ENCHANTED_GOLDEN_APPLE);
        this.stealCountList[4] = Sorter.getItemCount(Items.OBSIDIAN);
        this.stealCountList[5] = Sorter.getItemCount(Items.COBWEB);
        this.stealCountList[6] = Sorter.getItemCount(Items.GLOWSTONE);
        this.stealCountList[7] = Sorter.getItemCount(Items.RESPAWN_ANCHOR);
        this.stealCountList[8] = Sorter.getItemCount(Items.ENDER_PEARL);
        this.stealCountList[9] = Sorter.getItemCount(Items.PISTON) - Sorter.getItemCount(Items.STICKY_PISTON);
        this.stealCountList[10] = Sorter.getItemCount(Items.REDSTONE_BLOCK);
        this.stealCountList[11] = Sorter.getItemCount(BedBlock.class);
        this.stealCountList[12] = InventoryUtil.getPotionCount((StatusEffect)StatusEffects.SPEED.value());
        this.stealCountList[13] = InventoryUtil.getPotionCount((StatusEffect)StatusEffects.RESISTANCE.value());
        this.stealCountList[14] = Sorter.getItemCount(Items.GOLDEN_APPLE);
        this.stealCountList[15] = Sorter.getItemCount(LadderBlock.class);
        this.stealCountList[16] = InventoryUtil.getPotionCount((StatusEffect)StatusEffects.STRENGTH.value());
    }

    private int getMinId(int slot, int currentId) {
        int id = currentId;
        for (int slot1 = slot + 1; slot1 < 36; ++slot1) {
            int itemID;
            ItemStack stack = Sorter.mc.player.getInventory().getStack(slot1);
            if (stack.isEmpty() || (itemID = Item.getRawId((Item)stack.getItem())) >= id) continue;
            id = itemID;
        }
        return id;
    }

    public static boolean canMerge(ItemStack source, ItemStack stack) {
        return ItemStack.areItemsAndComponentsEqual((ItemStack)source, (ItemStack)stack);
    }

    private static enum Mode {
        Whitelist,
        Blacklist;

    }
}
