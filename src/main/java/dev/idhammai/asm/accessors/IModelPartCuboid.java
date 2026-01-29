package dev.idhammai.asm.accessors;

import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelPart.Cuboid.class)
public interface IModelPartCuboid {
    @Accessor("sides")
    ModelPart.Quad[] getSides();
}
