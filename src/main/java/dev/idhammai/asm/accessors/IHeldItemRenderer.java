/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.item.HeldItemRenderer
 *  net.minecraft.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.idhammai.asm.accessors;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={HeldItemRenderer.class})
public interface IHeldItemRenderer {
    @Accessor(value="equipProgressMainHand")
    public float getEquippedProgressMainHand();

    @Accessor(value="equipProgressMainHand")
    public void setEquippedProgressMainHand(float var1);

    @Accessor(value="equipProgressOffHand")
    public float getEquippedProgressOffHand();

    @Accessor(value="equipProgressOffHand")
    public void setEquippedProgressOffHand(float var1);

    @Accessor(value="mainHand")
    public void setItemStackMainHand(ItemStack var1);

    @Accessor(value="offHand")
    public void setItemStackOffHand(ItemStack var1);
}

