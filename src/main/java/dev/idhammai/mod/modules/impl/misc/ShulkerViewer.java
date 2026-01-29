/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.block.ShulkerBoxBlock
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.render.DiffuseLighting
 *  net.minecraft.component.ComponentMap
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.NbtComponent
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.registry.RegistryWrapper$WrapperLookup
 *  net.minecraft.util.collection.DefaultedList
 *  org.lwjgl.opengl.GL11
 *  org.spongepowered.asm.mixin.Unique
 */
package dev.idhammai.mod.modules.impl.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.asm.accessors.IContainerComponent;
import dev.idhammai.core.impl.PlayerManager;
import dev.idhammai.mod.gui.PeekScreen;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Unique;

public class ShulkerViewer
extends Module {
    private static final ItemStack[] ITEMS = new ItemStack[27];
    public static ShulkerViewer INSTANCE;
    private static int offset;
    public final BooleanSetting toolTips = this.add(new BooleanSetting("ToolTips", true));
    public final BooleanSetting icon = this.add(new BooleanSetting("Icon", true));
    private final HashMap<UUID, Shulker> map = new HashMap();
    private final BooleanSetting peek = this.add(new BooleanSetting("Peek", false).setParent());
    private final SliderSetting renderTime = this.add(new SliderSetting("RenderTime", 10.0, 0.0, 100.0, 0.1, this.peek::isOpen).setSuffix("s"));
    private final SliderSetting xOffset = this.add(new SliderSetting("X", 0, 0, 1500, this.peek::isOpen));
    private final SliderSetting yOffset = this.add(new SliderSetting("Y", 120, 0, 1000, this.peek::isOpen));
    private final SliderSetting space = this.add(new SliderSetting("Space", 78.0, 0.0, 200.0, 1.0, this.peek::isOpen));

    public ShulkerViewer() {
        super("ShulkerViewer", Module.Category.Misc);
        this.setChinese("\u6f5c\u5f71\u76d2\u67e5\u770b");
        INSTANCE = this;
    }

    public static void renderShulkerToolTip(DrawContext context, int mouseX, int mouseY, ItemStack stack) {
        ShulkerViewer.getItemsInContainerItem(stack, ITEMS);
        ShulkerViewer.draw(context, mouseX, mouseY);
    }

    @Unique
    private static void draw(DrawContext context, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        GL11.glClear((int)256);
        Render2DUtil.drawRect(context.getMatrices(), (float)(mouseX += 8), (float)(mouseY -= 82), 176.0f, 67.0f, new Color(0, 0, 0, 120));
        DiffuseLighting.enableGuiDepthLighting();
        int row = 0;
        int i = 0;
        for (ItemStack itemStack : ITEMS) {
            context.drawItem(itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
            context.drawItemInSlot(ShulkerViewer.mc.textRenderer, itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
            if (++i < 9) continue;
            i = 0;
            ++row;
        }
        DiffuseLighting.disableGuiDepthLighting();
        RenderSystem.enableDepthTest();
    }

    public static boolean hasItems(ItemStack itemStack) {
        IContainerComponent container = (IContainerComponent)(Object)itemStack.get(DataComponentTypes.CONTAINER);
        if (container != null && !container.getStacks().isEmpty()) {
            return true;
        }
        NbtCompound compoundTag = ((NbtComponent)itemStack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, (Object)NbtComponent.DEFAULT)).getNbt();
        return compoundTag != null && compoundTag.contains("Items", 9);
    }

    public static void getItemsInContainerItem(ItemStack itemStack, ItemStack[] items) {
        block5: {
            NbtComponent nbt2;
            ComponentMap components;
            block4: {
                if (itemStack.getItem() == Items.ENDER_CHEST) {
                    for (int i = 0; i < Frog.PLAYER.ENDERCHEST_ITEM.size(); ++i) {
                        items[i] = (ItemStack)Frog.PLAYER.ENDERCHEST_ITEM.get(i);
                    }
                    return;
                }
                Arrays.fill(items, ItemStack.EMPTY);
                components = itemStack.getComponents();
                if (!components.contains(DataComponentTypes.CONTAINER)) break block4;
                IContainerComponent container = (IContainerComponent)(Object)components.get(DataComponentTypes.CONTAINER);
                DefaultedList<ItemStack> stacks = container.getStacks();
                for (int i = 0; i < stacks.size(); ++i) {
                    if (i < 0 || i >= items.length) continue;
                    items[i] = (ItemStack)stacks.get(i);
                }
                break block5;
            }
            if (!components.contains(DataComponentTypes.BLOCK_ENTITY_DATA) || !(nbt2 = (NbtComponent)components.get(DataComponentTypes.BLOCK_ENTITY_DATA)).contains("Items")) break block5;
            NbtList nbt3 = (NbtList)nbt2.getNbt().get("Items");
            for (int i = 0; i < nbt3.size(); ++i) {
                byte slot = nbt3.getCompound(i).getByte("Slot");
                if (slot < 0 || slot >= items.length) continue;
                items[slot] = ItemStack.fromNbtOrEmpty((RegistryWrapper.WrapperLookup)ShulkerViewer.mc.player.getRegistryManager(), (NbtCompound)nbt3.getCompound(i));
            }
        }
    }

    public static boolean openContainer(ItemStack itemStack, ItemStack[] contents, boolean pause) {
        if (ShulkerViewer.hasItems(itemStack) || itemStack.getItem() == Items.ENDER_CHEST) {
            ShulkerViewer.getItemsInContainerItem(itemStack, contents);
            if (pause) {
                PlayerManager.screenToOpen = new PeekScreen(itemStack, contents);
            } else {
                mc.setScreen((Screen)new PeekScreen(itemStack, contents));
            }
            return true;
        }
        return false;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.peek.getValue()) {
            for (AbstractClientPlayerEntity player : Frog.THREAD.getPlayers()) {
                BlockItem blockItem;
                ItemStack stack = player.getMainHandStack();
                Item item = stack.getItem();
                if (!(item instanceof BlockItem) || !((blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock)) {
                    stack = player.getOffHandStack();
                }
                if (!((item = stack.getItem()) instanceof BlockItem) || !((blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock)) continue;
                this.map.put(player.getGameProfile().getId(), new Shulker(stack, player.getGameProfile().getName()));
            }
        }
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (this.peek.getValue()) {
            offset = 0;
            this.map.values().removeIf(shulker -> shulker.draw(drawContext));
        }
    }

    class Shulker {
        final ItemStack itemStack;
        final String name;
        private final Timer timer;

        public Shulker(ItemStack itemStack, String name) {
            this.itemStack = itemStack;
            this.timer = new Timer();
            this.name = name;
        }

        public boolean draw(DrawContext context) {
            if (this.timer.passedS(ShulkerViewer.this.renderTime.getValue())) {
                return true;
            }
            ShulkerViewer.renderShulkerToolTip(context, ShulkerViewer.this.xOffset.getValueInt() - 8, ShulkerViewer.this.yOffset.getValueInt() + offset, this.itemStack);
            TextRenderer textRenderer = Wrapper.mc.textRenderer;
            int n = ShulkerViewer.this.xOffset.getValueInt();
            int n2 = ShulkerViewer.this.yOffset.getValueInt() + offset;
            Objects.requireNonNull(Wrapper.mc.textRenderer);
            context.drawText(textRenderer, this.name, n, n2 - 9 - 82, -1, true);
            offset += ShulkerViewer.this.space.getValueInt();
            return false;
        }
    }
}

