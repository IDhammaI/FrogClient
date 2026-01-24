/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package dev.gzsakura_miitong.asm.mixins;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.impl.LookDirectionEvent;
import dev.gzsakura_miitong.api.events.impl.SprintEvent;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.mod.modules.impl.movement.Velocity;
import dev.gzsakura_miitong.mod.modules.impl.render.NoRender;
import dev.gzsakura_miitong.mod.modules.impl.render.ShaderModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Entity.class})
public abstract class MixinEntity {
    @Shadow
    private static final int SPRINTING_FLAG_INDEX = 3;

    @Inject(method={"changeLookDirection"}, at={@At(value="HEAD")}, cancellable=true)
    private void hookChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (Entity.class.cast(this) == Wrapper.mc.player) {
            LookDirectionEvent lookDirectionEvent = LookDirectionEvent.get((Entity)Entity.class.cast(this), cursorDeltaX, cursorDeltaY);
            Alien.EVENT_BUS.post(lookDirectionEvent);
            if (lookDirectionEvent.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"}, cancellable=true)
    private void onIsInvisibleCheck(PlayerEntity message, CallbackInfoReturnable<Boolean> cir) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.invisible.getValue()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"isGlowing"}, at={@At(value="HEAD")}, cancellable=true)
    void isGlowingHook(CallbackInfoReturnable<Boolean> cir) {
        if (ShaderModule.INSTANCE.isOn()) {
            cir.setReturnValue(ShaderModule.INSTANCE.shouldRender((Entity)Entity.class.cast(this)));
        }
    }

    @ModifyArgs(method={"pushAwayFrom"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void pushAwayFromHook(Args args) {
        if (Entity.class.cast(this) == MinecraftClient.getInstance().player && Velocity.INSTANCE.isOn() && Velocity.INSTANCE.entityPush.getValue()) {
            args.set(0, 0.0);
            args.set(1, 0.0);
            args.set(2, 0.0);
        }
    }

    @Inject(method={"isOnFire"}, at={@At(value="HEAD")}, cancellable=true)
    void isOnFireHook(CallbackInfoReturnable<Boolean> cir) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.fireEntity.getValue()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"setSprinting"}, at={@At(value="HEAD")}, cancellable=true)
    public void setSprintingHook(boolean sprinting, CallbackInfo ci) {
        if (Entity.class.cast(this) == MinecraftClient.getInstance().player) {
            SprintEvent event = SprintEvent.get();
            Alien.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
                sprinting = event.isSprint();
                this.setFlag(3, sprinting);
            }
        }
    }

    @Shadow
    protected void setFlag(int index, boolean value) {
    }
}

