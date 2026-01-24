package dev.gzsakura_miitong.asm.accessors;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntityModel.class)
public interface IPlayerEntityModel {
    @Accessor("leftPants")
    ModelPart getLeftPants();

    @Accessor("rightPants")
    ModelPart getRightPants();

    @Accessor("leftSleeve")
    ModelPart getLeftSleeve();

    @Accessor("rightSleeve")
    ModelPart getRightSleeve();

    @Accessor("jacket")
    ModelPart getJacket();
}
