/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.util.math.BlockPos
 */
package dev.idhammai.api.utils.path;

import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.mod.modules.impl.client.BaritoneModule;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class BaritoneUtil
implements Wrapper {
    public static boolean loaded;

    public static void gotoPos(BlockPos pos) {
        if (loaded) {
            BaritoneModule.gotoPos(pos);
        }
    }

    public static void forward() {
        if (loaded) {
            BaritoneModule.forward();
        }
    }

    public static void mine(Block block) {
        if (loaded) {
            BaritoneModule.mine(block);
        }
    }

    public static boolean isPathing() {
        if (loaded) {
            return BaritoneModule.isPathing();
        }
        return false;
    }

    public static void cancelEverything() {
        if (loaded) {
            BaritoneModule.cancelEverything();
        }
    }

    public static boolean isActive() {
        if (loaded) {
            return BaritoneModule.isActive();
        }
        return false;
    }

    static {
        Package[] packages;
        for (Package pkg : packages = Package.getPackages()) {
            if (!pkg.getName().contains("baritone.api")) continue;
            loaded = true;
            break;
        }
    }
}

