/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.EnderChestBlock
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.GenericContainerScreen
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.inventory.Inventory
 *  net.minecraft.item.ItemStack
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.util.collection.DefaultedList
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 */
package dev.idhammai.core.impl;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.BlockActivateEvent;
import dev.idhammai.api.events.impl.GameLeftEvent;
import dev.idhammai.api.events.impl.OpenScreenEvent;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.world.BlockPosX;
import dev.idhammai.mod.modules.Module;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class PlayerManager
implements Wrapper {
    public static Screen screenToOpen;
    public final DefaultedList<ItemStack> ENDERCHEST_ITEM = DefaultedList.ofSize((int)27, ItemStack.EMPTY);
    public final Map<PlayerEntity, EntityAttribute> map = new ConcurrentHashMap<PlayerEntity, EntityAttribute>();
    public final CopyOnWriteArrayList<PlayerEntity> inWebPlayers = new CopyOnWriteArrayList();
    public boolean known = false;
    private int echestOpenedState;

    public PlayerManager() {
        Frog.EVENT_BUS.subscribe(this);
    }

    @EventListener
    public void onLogout(GameLeftEvent event) {
        this.inWebPlayers.clear();
        this.map.clear();
        this.ENDERCHEST_ITEM.clear();
        this.known = false;
    }

    @EventListener
    private void onBlockActivate(BlockActivateEvent event) {
        if (event.blockState.getBlock() instanceof EnderChestBlock && this.echestOpenedState == 0) {
            this.echestOpenedState = 1;
        }
    }

    @EventListener
    private void onOpenScreenEvent(OpenScreenEvent event) {
        if (this.echestOpenedState == 1 && event.screen instanceof GenericContainerScreen) {
            this.echestOpenedState = 2;
            return;
        }
        if (this.echestOpenedState == 0) {
            return;
        }
        if (!(PlayerManager.mc.currentScreen instanceof GenericContainerScreen)) {
            return;
        }
        GenericContainerScreenHandler container = (GenericContainerScreenHandler)((GenericContainerScreen)PlayerManager.mc.currentScreen).getScreenHandler();
        if (container == null) {
            return;
        }
        Inventory inv = container.getInventory();
        for (int i = 0; i < 27; ++i) {
            this.ENDERCHEST_ITEM.set(i, inv.getStack(i));
        }
        this.known = true;
        this.echestOpenedState = 0;
    }

    public void onUpdate() {
        if (Module.nullCheck()) {
            return;
        }
        if (screenToOpen != null && PlayerManager.mc.currentScreen == null) {
            mc.setScreen(screenToOpen);
            screenToOpen = null;
        }
        this.inWebPlayers.clear();
        for (PlayerEntity playerEntity : Frog.THREAD.getPlayers()) {
            this.map.put(playerEntity, new EntityAttribute(playerEntity.getArmor(), playerEntity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)));
            this.webUpdate(playerEntity);
        }
    }

    public boolean isInWeb(PlayerEntity player) {
        return this.inWebPlayers.contains(player);
    }

    private void webUpdate(PlayerEntity player) {
        for (float x : new float[]{0.0f, 0.3f, -0.3f}) {
            for (float z : new float[]{0.0f, 0.3f, -0.3f}) {
                for (int y : new int[]{-1, 0, 1, 2}) {
                    BlockPos pos = new BlockPosX(player.getX() + (double)x, player.getY(), player.getZ() + (double)z).up(y);
                    if (!new Box(pos).intersects(player.getBoundingBox()) || PlayerManager.mc.world.getBlockState(pos).getBlock() != Blocks.COBWEB) continue;
                    this.inWebPlayers.add(player);
                    return;
                }
            }
        }
    }

    public record EntityAttribute(int armor, double toughness) {
    }
}

