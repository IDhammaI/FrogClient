/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.registry.SimpleRegistry
 *  net.minecraft.registry.entry.RegistryEntry$Reference
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.gzsakura_miitong.asm.accessors;

import java.util.Map;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={SimpleRegistry.class})
public interface ISimpleRegistry<T> {
    @Accessor(value="valueToEntry")
    public Map<T, RegistryEntry.Reference<T>> getValueToEntry();
}

