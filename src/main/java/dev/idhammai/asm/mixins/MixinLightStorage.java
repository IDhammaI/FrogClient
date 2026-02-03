package dev.idhammai.asm.mixins;

import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.light.LightStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={LightStorage.class})
public class MixinLightStorage {
    @Redirect(method={"get(Lnet/minecraft/world/chunk/ChunkNibbleArray;III)I"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/chunk/ChunkNibbleArray;get(III)I"), require=0)
    private static int redirectChunkNibbleGetDesc(ChunkNibbleArray chunkNibbleArray, int x, int y, int z) {
        if (chunkNibbleArray == null) {
            return 0;
        }
        return chunkNibbleArray.get(x, y, z);
    }

    @Redirect(method={"get"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/chunk/ChunkNibbleArray;get(III)I"), require=0)
    private static int redirectChunkNibbleGet(ChunkNibbleArray chunkNibbleArray, int x, int y, int z) {
        if (chunkNibbleArray == null) {
            return 0;
        }
        return chunkNibbleArray.get(x, y, z);
    }
}