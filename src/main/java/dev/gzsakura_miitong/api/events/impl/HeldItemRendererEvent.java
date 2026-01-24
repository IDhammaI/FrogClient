/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.Hand
 */
package dev.gzsakura_miitong.api.events.impl;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class HeldItemRendererEvent {
    private static final HeldItemRendererEvent INSTANCE = new HeldItemRendererEvent();
    private Hand hand;
    private ItemStack item;
    private float ep;
    private MatrixStack stack;

    private HeldItemRendererEvent() {
    }

    public static HeldItemRendererEvent get(Hand hand, ItemStack item, float equipProgress, MatrixStack stack) {
        HeldItemRendererEvent.INSTANCE.hand = hand;
        HeldItemRendererEvent.INSTANCE.item = item;
        HeldItemRendererEvent.INSTANCE.ep = equipProgress;
        HeldItemRendererEvent.INSTANCE.stack = stack;
        return INSTANCE;
    }

    public Hand getHand() {
        return this.hand;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public float getEp() {
        return this.ep;
    }

    public MatrixStack getStack() {
        return this.stack;
    }
}

