/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.gui.screen.recipebook.RecipeResultCollection
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.BedItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.recipe.RecipeEntry
 *  net.minecraft.registry.RegistryWrapper$WrapperLookup
 *  net.minecraft.screen.CraftingScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.player.InventoryUtil;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class BedCrafter
extends Module {
    public static BedCrafter INSTANCE;
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5, 0, 8));
    private final BooleanSetting disable = this.add(new BooleanSetting("Disable", true));

    public BedCrafter() {
        super("BedCrafter", Module.Category.Misc);
        this.setChinese("\u81ea\u52a8\u5236\u4f5c\u5e8a");
        INSTANCE = this;
    }

    public static int getEmptySlots() {
        int emptySlots = 0;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = BedCrafter.mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty()) continue;
            ++emptySlots;
        }
        return emptySlots;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (BedCrafter.getEmptySlots() == 0) {
            if (BedCrafter.mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
                BedCrafter.mc.player.closeHandledScreen();
            }
            if (this.disable.getValue()) {
                this.disable();
            }
            return;
        }
        if (BedCrafter.mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
            boolean craft = false;
            block0: for (RecipeResultCollection recipeResult : BedCrafter.mc.player.getRecipeBook().getOrderedResults()) {
                for (RecipeEntry recipe : recipeResult.getRecipes(true)) {
                    if (!(recipe.value().getResult((RegistryWrapper.WrapperLookup)BedCrafter.mc.world.getRegistryManager()).getItem() instanceof BedItem)) continue;
                    for (int i = 0; i < BedCrafter.getEmptySlots(); ++i) {
                        craft = true;
                        BedCrafter.mc.interactionManager.clickRecipe(BedCrafter.mc.player.currentScreenHandler.syncId, recipe, false);
                        BedCrafter.mc.interactionManager.clickSlot(BedCrafter.mc.player.currentScreenHandler.syncId, 0, 1, SlotActionType.QUICK_MOVE, (PlayerEntity)BedCrafter.mc.player);
                    }
                    continue block0;
                }
            }
            if (!craft) {
                if (BedCrafter.mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
                    BedCrafter.mc.player.closeHandledScreen();
                }
                if (this.disable.getValue()) {
                    this.disable();
                }
            }
        } else {
            this.doPlace();
        }
    }

    private void doPlace() {
        BlockPos bestPos = null;
        double getDistance = 100.0;
        boolean place = true;
        for (BlockPos pos : BlockUtil.getSphere(this.range.getValueFloat())) {
            if (BedCrafter.mc.world.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE && BlockUtil.getClickSideStrict(pos) != null) {
                place = false;
                bestPos = pos;
                break;
            }
            if (!BlockUtil.canPlace(pos) || bestPos != null && !((double)MathHelper.sqrt((float)((float)BedCrafter.mc.player.squaredDistanceTo(pos.toCenterPos()))) < getDistance)) continue;
            bestPos = pos;
            getDistance = MathHelper.sqrt((float)((float)BedCrafter.mc.player.squaredDistanceTo(pos.toCenterPos())));
        }
        if (bestPos != null) {
            if (!place) {
                BlockUtil.clickBlock(bestPos, BlockUtil.getClickSide(bestPos), this.rotate.getValue());
            } else {
                if (InventoryUtil.findItem(Items.CRAFTING_TABLE) == -1) {
                    return;
                }
                int old = BedCrafter.mc.player.getInventory().selectedSlot;
                InventoryUtil.switchToSlot(InventoryUtil.findItem(Items.CRAFTING_TABLE));
                BlockUtil.placeBlock(bestPos, this.rotate.getValue());
                InventoryUtil.switchToSlot(old);
            }
        }
    }
}

