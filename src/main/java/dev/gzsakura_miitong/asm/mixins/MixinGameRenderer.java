/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.block.enums.CameraSubmersionType
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.RenderTickCounter
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.projectile.ProjectileUtil
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.gzsakura_miitong.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.utils.render.TextUtil;
import dev.gzsakura_miitong.api.utils.world.InteractUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.player.Freecam;
import dev.gzsakura_miitong.mod.modules.impl.player.InteractTweaks;
import dev.gzsakura_miitong.mod.modules.impl.render.AspectRatio;
import dev.gzsakura_miitong.mod.modules.impl.render.Chams;
import dev.gzsakura_miitong.mod.modules.impl.render.Fov;
import dev.gzsakura_miitong.mod.modules.impl.render.HighLight;
import dev.gzsakura_miitong.mod.modules.impl.render.NoRender;
import dev.gzsakura_miitong.mod.modules.impl.render.Zoom;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={GameRenderer.class})
public class MixinGameRenderer {
    @Shadow
    @Final
    MinecraftClient client;
    @Shadow
    private float fovMultiplier;
    @Shadow
    private float lastFovMultiplier;
    @Shadow
    private boolean renderingPanorama;
    @Shadow
    private float zoom;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;
    @Shadow
    private float viewDistance;
    @Unique
    private Entity cameraEntity;
    @Unique
    private float originalYaw;
    @Unique
    private float originalPitch;

    @Shadow
    private static HitResult ensureTargetInRange(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
        Vec3d vec3d = hitResult.getPos();
        if (!vec3d.isInRange((Position)cameraPos, interactionRange)) {
            Vec3d vec3d2 = hitResult.getPos();
            Direction direction = Direction.getFacing((double)(vec3d2.x - cameraPos.x), (double)(vec3d2.y - cameraPos.y), (double)(vec3d2.z - cameraPos.z));
            return BlockHitResult.createMissed((Vec3d)vec3d2, (Direction)direction, (BlockPos)BlockPos.ofFloored((Position)vec3d2));
        }
        return hitResult;
    }

    @Inject(method={"showFloatingItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (floatingItem.getItem() == Items.TOTEM_OF_UNDYING && NoRender.INSTANCE.isOn() && NoRender.INSTANCE.totem.getValue()) {
            info.cancel();
        }
    }

    @Redirect(method={"renderWorld"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"), require=0)
    private float applyCameraTransformationsMathHelperLerpProxy(float delta, float first, float second) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.nausea.getValue()) {
            return 0.0f;
        }
        return MathHelper.lerp((float)delta, (float)first, (float)second);
    }

    @Inject(method={"tiltViewWhenHurt"}, at={@At(value="HEAD")}, cancellable=true)
    private void tiltViewWhenHurtHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.hurtCam.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"shouldRenderBlockOutline"}, at={@At(value="HEAD")}, cancellable=true)
    public void hookOutline(CallbackInfoReturnable<Boolean> cir) {
        if (HighLight.INSTANCE.isOn()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at={@At(value="FIELD", target="Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode=180, ordinal=0)}, method={"renderWorld"})
    void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Module.nullCheck()) {
            return;
        }
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        TextUtil.lastProjMat.set((Matrix4fc)RenderSystem.getProjectionMatrix());
        TextUtil.lastModMat.set((Matrix4fc)RenderSystem.getModelViewMatrix());
        TextUtil.lastWorldSpaceMatrix.set((Matrix4fc)matrixStack.peek().getPositionMatrix());
        Alien.FPS.record();
        Alien.MODULE.render3D(matrixStack);
    }

    @Inject(at={@At(value="TAIL")}, method={"renderWorld"})
    void render3dTail(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Chams.INSTANCE.isOn() && Chams.INSTANCE.hand.booleanValue) {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    @Inject(method={"getFov(Lnet/minecraft/client/render/Camera;FZ)D"}, at={@At(value="HEAD")}, cancellable=true)
    public void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> ci) {
        if (!this.renderingPanorama && (Fov.INSTANCE.isOn() || Zoom.on)) {
            CameraSubmersionType cameraSubmersionType;
            double d = 70.0;
            if (changingFov) {
                if (Fov.INSTANCE.isOn()) {
                    double fov = Fov.INSTANCE.fov.getValue();
                    if (Zoom.on) {
                        ci.setReturnValue(Math.min(Math.max(fov - Zoom.INSTANCE.currentFov, 1.0), 177.0));
                    } else {
                        ci.setReturnValue(fov);
                    }
                    return;
                }
                d = ((Integer)this.client.options.getFov().getValue()).intValue();
                d *= (double)MathHelper.lerp((float)tickDelta, (float)this.lastFovMultiplier, (float)this.fovMultiplier);
                if (Zoom.on) {
                    d = Math.min(Math.max(d - Zoom.INSTANCE.currentFov, 1.0), 177.0);
                }
            } else if (Fov.INSTANCE.isOn()) {
                ci.setReturnValue(Fov.INSTANCE.itemFov.getValue());
                return;
            }
            if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity)camera.getFocusedEntity()).isDead()) {
                float f = Math.min((float)((LivingEntity)camera.getFocusedEntity()).deathTime + tickDelta, 20.0f);
                d /= (double)((1.0f - 500.0f / (f + 500.0f)) * 2.0f + 1.0f);
            }
            if ((cameraSubmersionType = camera.getSubmersionType()) == CameraSubmersionType.LAVA || cameraSubmersionType == CameraSubmersionType.WATER) {
                d *= MathHelper.lerp((double)((Double)this.client.options.getFovEffectScale().getValue()), (double)1.0, (double)0.8571428656578064);
            }
            ci.setReturnValue(d);
        }
    }

    @Inject(method={"getNightVisionStrength"}, at={@At(value="HEAD")}, cancellable=true)
    private static void getNightVisionStrengthHook(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        StatusEffectInstance statusEffectInstance = entity.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (statusEffectInstance == null) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method={"renderWorld"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V", shift=At.Shift.AFTER)})
    public void postRender3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        Alien.SHADER.renderShaders();
    }

    @Inject(method={"getBasicProjectionMatrix"}, at={@At(value="TAIL")}, cancellable=true)
    public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (AspectRatio.INSTANCE.isOn()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (this.zoom != 1.0f) {
                matrixStack.translate(this.zoomX, -this.zoomY, 0.0f);
                matrixStack.scale(this.zoom, this.zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul((Matrix4fc)new Matrix4f().setPerspective((float)(fov * 0.01745329238474369), AspectRatio.INSTANCE.ratio.getValueFloat(), 0.05f, this.viewDistance * 4.0f));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }

    @Inject(method={"findCrosshairTarget"}, at={@At(value="HEAD")}, cancellable=true)
    private void findCrosshairTarget(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta, CallbackInfoReturnable<HitResult> cir) {
        if (Freecam.INSTANCE.isOn()) {
            cir.setReturnValue(InteractUtil.getRtxTarget(Freecam.INSTANCE.getFakeYaw(), Freecam.INSTANCE.getFakePitch(), Freecam.INSTANCE.getFakeX(), Freecam.INSTANCE.getFakeY(), Freecam.INSTANCE.getFakeZ()));
            return;
        }
        double d = Math.max(blockInteractionRange, entityInteractionRange);
        double e = MathHelper.square((double)d);
        Vec3d vec3d = camera.getCameraPosVec(tickDelta);
        InteractTweaks.INSTANCE.isActive = InteractTweaks.INSTANCE.ghostHand();
        HitResult hitResult = camera.raycast(d, tickDelta, false);
        InteractTweaks.INSTANCE.isActive = false;
        double f = hitResult.getPos().squaredDistanceTo(vec3d);
        if (hitResult.getType() != HitResult.Type.MISS) {
            e = f;
            d = Math.sqrt(f);
        }
        Vec3d vec3d2 = camera.getRotationVec(tickDelta);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
        Box box = camera.getBoundingBox().stretch(vec3d2.multiply(d)).expand(1.0, 1.0, 1.0);
        if (!InteractTweaks.INSTANCE.noEntityTrace()) {
            EntityHitResult entityHitResult = ProjectileUtil.raycast((Entity)camera, (Vec3d)vec3d, (Vec3d)vec3d3, (Box)box, entity -> !entity.isSpectator() && entity.canHit(), (double)e);
            cir.setReturnValue(entityHitResult != null && entityHitResult.getPos().squaredDistanceTo(vec3d) < f ? MixinGameRenderer.ensureTargetInRange((HitResult)entityHitResult, vec3d, entityInteractionRange) : MixinGameRenderer.ensureTargetInRange(hitResult, vec3d, blockInteractionRange));
        } else {
            cir.setReturnValue(MixinGameRenderer.ensureTargetInRange(hitResult, vec3d, blockInteractionRange));
        }
    }
}

