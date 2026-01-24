/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class Friend
extends Module {
    public static Friend INSTANCE;

    public Friend() {
        super("Friend", Module.Category.Misc);
        this.setChinese("\u597d\u53cb");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        EntityHitResult entityHitResult;
        Entity entity;
        if (Friend.nullCheck()) {
            this.disable();
            return;
        }
        HitResult target = Friend.mc.crosshairTarget;
        if (target instanceof EntityHitResult && (entity = (entityHitResult = (EntityHitResult)target).getEntity()) instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            Alien.FRIEND.friend(player);
        }
        this.disable();
    }
}

