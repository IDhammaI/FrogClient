/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.type.ContainerComponent
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.collection.DefaultedList
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.gzsakura_miitong.asm.accessors;

import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ContainerComponent.class})
public interface IContainerComponent {
    @Accessor
    public DefaultedList<ItemStack> getStacks();
}

