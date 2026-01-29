/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.EntityPose
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.Vec3d
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.Frog;
import dev.idhammai.api.events.Event;
import dev.idhammai.api.events.impl.JumpEvent;
import dev.idhammai.api.events.impl.TravelEvent;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import dev.idhammai.mod.modules.impl.player.InteractTweaks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerEntity.class})
public class MixinPlayerEntity
implements Wrapper {
    @Inject(method={"canChangeIntoPose"}, at={@At(value="RETURN")}, cancellable=true)
    private void poseNotCollide(EntityPose pose, CallbackInfoReturnable<Boolean> cir) {
        if (PlayerEntity.class.cast(this) == MixinPlayerEntity.mc.player && !ClientSetting.INSTANCE.crawl.getValue() && pose == EntityPose.SWIMMING) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"getBlockInteractionRange"}, at={@At(value="HEAD")}, cancellable=true)
    public void getBlockInteractionRangeHook(CallbackInfoReturnable<Double> cir) {
        if (InteractTweaks.INSTANCE.reach()) {
            cir.setReturnValue(InteractTweaks.INSTANCE.blockRange.getValue());
        }
    }

    @Inject(method={"getEntityInteractionRange"}, at={@At(value="HEAD")}, cancellable=true)
    public void getEntityInteractionRangeHook(CallbackInfoReturnable<Double> cir) {
        if (InteractTweaks.INSTANCE.reach()) {
            cir.setReturnValue(InteractTweaks.INSTANCE.entityRange.getValue());
        }
    }

    @Inject(method={"jump"}, at={@At(value="HEAD")})
    private void onJumpPre(CallbackInfo ci) {
        Frog.EVENT_BUS.post(JumpEvent.get(Event.Stage.Pre));
    }

    @Inject(method={"jump"}, at={@At(value="RETURN")})
    private void onJumpPost(CallbackInfo ci) {
        Frog.EVENT_BUS.post(JumpEvent.get(Event.Stage.Post));
    }

    @Inject(method={"travel"}, at={@At(value="HEAD")}, cancellable=true)
    private void onTravelPre(Vec3d movementInput, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)PlayerEntity.class.cast(this);
        if (player != MixinPlayerEntity.mc.player) {
            return;
        }
        TravelEvent event = TravelEvent.get(Event.Stage.Pre, player);
        Frog.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
            event = TravelEvent.get(Event.Stage.Post, player);
            Frog.EVENT_BUS.post(event);
        }
    }

    @Inject(method={"travel"}, at={@At(value="RETURN")})
    private void onTravelPost(Vec3d movementInput, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)PlayerEntity.class.cast(this);
        if (player != MixinPlayerEntity.mc.player) {
            return;
        }
        TravelEvent event = TravelEvent.get(Event.Stage.Post, player);
        Frog.EVENT_BUS.post(event);
    }
}

