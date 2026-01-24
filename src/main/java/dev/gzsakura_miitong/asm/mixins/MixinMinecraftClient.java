/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.SharedConstants
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.RunArgs
 *  net.minecraft.client.gui.hud.InGameHud
 *  net.minecraft.client.gui.screen.DownloadingTerrainScreen
 *  net.minecraft.client.gui.screen.ProgressScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.TitleScreen
 *  net.minecraft.client.gui.screen.multiplayer.ConnectScreen
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.network.ClientPlayerInteractionManager
 *  net.minecraft.client.network.ServerInfo
 *  net.minecraft.client.particle.ParticleManager
 *  net.minecraft.client.resource.language.I18n
 *  net.minecraft.client.util.Window
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.server.integrated.IntegratedServer
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.thread.ReentrantThreadExecutor
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
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
import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.events.Event;
import dev.gzsakura_miitong.api.events.impl.ClientTickEvent;
import dev.gzsakura_miitong.api.events.impl.DoAttackEvent;
import dev.gzsakura_miitong.api.events.impl.GameLeftEvent;
import dev.gzsakura_miitong.api.events.impl.OpenScreenEvent;
import dev.gzsakura_miitong.api.events.impl.ResizeEvent;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.core.impl.CommandManager;
import dev.gzsakura_miitong.core.impl.FontManager;
import dev.gzsakura_miitong.core.impl.ShaderManager;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import dev.gzsakura_miitong.mod.modules.impl.player.InteractTweaks;
import java.awt.Color;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;

@Mixin(value={MinecraftClient.class})
public abstract class MixinMinecraftClient
extends ReentrantThreadExecutor<Runnable> {
    @Shadow
    @Final
    public InGameHud inGameHud;
    @Shadow
    public int attackCooldown;
    @Shadow
    public ClientPlayerEntity player;
    @Shadow
    public HitResult crosshairTarget;
    @Shadow
    public ClientPlayerInteractionManager interactionManager;
    @Final
    @Shadow
    public ParticleManager particleManager;
    @Shadow
    public ClientWorld world;
    @Shadow
    private IntegratedServer server;
    @Shadow
    public Screen currentScreen;
    @Shadow
    @Final
    private Window window;
    @Unique
    private static long alienStartTs = 0L;

    public MixinMinecraftClient(String string) {
        super(string);
    }

    @Inject(method={"onResolutionChanged"}, at={@At(value="TAIL")})
    private void captureResize(CallbackInfo ci) {
        Vitality.EVENT_BUS.post(new ResizeEvent(this.window));
    }

    @Redirect(method={"render"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/RenderSystem;limitDisplayFPS(I)V"), require=0)
    public void fpsHook(int fps) {
        if (!ClientSetting.INSTANCE.fuckFPSLimit.getValue()) {
            RenderSystem.limitDisplayFPS((int)fps);
        }
    }

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    void postWindowInit(RunArgs args, CallbackInfo ci) {
        FontManager.init();
        alienStartTs = System.currentTimeMillis();
    }

    @Inject(method={"setScreen"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSetScreen(Screen screen, CallbackInfo info) {
        if (Alien.EVENT_BUS != null) {
            OpenScreenEvent event = OpenScreenEvent.get(screen);
            Alien.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                info.cancel();
            }
        }
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void vitalityMagentaOverlay(CallbackInfo ci) {
        float ny;
        float nx;
        float x0;
        float i;
        boolean isLoading;
        Screen screen = this.currentScreen;
        if (screen == null) {
            return;
        }
        boolean isMainMenu = screen instanceof TitleScreen;
        boolean bl = isLoading = screen instanceof ProgressScreen || screen instanceof DownloadingTerrainScreen || screen instanceof ConnectScreen;
        if (!isMainMenu && !isLoading) {
            return;
        }
        if (Vitality.SHADER.fullNullCheck()) {
            return;
        }
        float w = this.window.getScaledWidth();
        float h = this.window.getScaledHeight();
        float t = (float)(System.currentTimeMillis() % 100000L) / 1000.0f;
        ManagedShaderEffect gradient = Vitality.SHADER.getShader(ShaderManager.Shader.Gradient);
        gradient.setUniformValue("alpha2", 0.85f);
        gradient.setUniformValue("rgb", 0.78f, 0.05f, 0.59f);
        gradient.setUniformValue("rgb1", 0.56f, 0.06f, 0.68f);
        gradient.setUniformValue("rgb2", 0.93f, 0.12f, 0.63f);
        gradient.setUniformValue("rgb3", 0.64f, 0.0f, 0.64f);
        gradient.setUniformValue("step", 180.0f);
        gradient.setUniformValue("radius", 2.0f);
        gradient.setUniformValue("quality", 1.0f);
        gradient.setUniformValue("divider", 150.0f);
        gradient.setUniformValue("maxSample", 10.0f);
        gradient.setUniformValue("resolution", w, h);
        gradient.setUniformValue("time", t * 300.0f);
        gradient.render(((MinecraftClient)(Object)this).getRenderTickCounter().getTickDelta(true));
        ManagedShaderEffect pulse = Vitality.SHADER.getShader(ShaderManager.Shader.Pulse);
        pulse.setUniformValue("mixFactor", 0.65f);
        pulse.setUniformValue("minAlpha", 0.35f);
        pulse.setUniformValue("radius", 2.0f);
        pulse.setUniformValue("quality", 1.0f);
        pulse.setUniformValue("divider", 150.0f);
        pulse.setUniformValue("maxSample", 10.0f);
        pulse.setUniformValue("color", 0.85f, 0.05f, 0.66f);
        pulse.setUniformValue("color2", 0.56f, 0.06f, 0.68f);
        pulse.setUniformValue("time", t);
        pulse.setUniformValue("size", 12.0f);
        pulse.setUniformValue("resolution", w, h);
        pulse.render(((MinecraftClient)(Object)this).getRenderTickCounter().getTickDelta(true));
        MatrixStack m = new MatrixStack();
        w = this.window.getScaledWidth();
        h = this.window.getScaledHeight();
        long now = System.currentTimeMillis();
        long elapsed = now - alienStartTs;
        if (elapsed < 2400L) {
            float p = Math.max(0.0f, Math.min(1.0f, (float)elapsed / 2400.0f));
            int aTop = (int)(180.0 * Math.sin((double)p * Math.PI));
            Color c1 = new Color(28, 60, 110, aTop);
            Color c2 = new Color(190, 50, 160, aTop);
            Render2DUtil.verticalGradient(m, 0.0f, 0.0f, w, h, c1, c2);
            float r = Math.min(w, h) * (0.12f + 0.18f * p);
            Render2DUtil.drawCircle(m, w / 2.0f, h / 2.0f, r, new Color(255, 255, 255, (int)(70.0f * p)), 80);
            Render2DUtil.drawCircle(m, w / 2.0f, h / 2.0f, r * 1.2f, new Color(120, 220, 255, (int)(50.0f * p)), 80);
            return;
        }
        float phase = (float)(now % 6000L) / 6000.0f;
        float angle = 0.523599f;
        float dx = (float)Math.tan(angle) * h;
        float base = -h;
        float spacing = 28.0f;
        float shift = phase * spacing * 5.2f;
        int cA1 = new Color(230, 60, 170, 64).getRGB();
        int cA2 = new Color(160, 40, 130, 48).getRGB();
        int nodeC = new Color(255, 255, 255, 42).getRGB();
        for (i = base; i < w; i += spacing) {
            x0 = i + shift;
            Render2DUtil.drawLine(m, x0, 0.0f, x0 + dx, h, cA1);
            nx = x0 + dx * 0.25f;
            ny = h * 0.25f;
            Render2DUtil.drawCircle(m, nx, ny, 2.5f, new Color(nodeC, true), 32);
        }
        for (i = base + spacing / 2.0f; i < w; i += spacing) {
            x0 = i + shift * 0.85f;
            Render2DUtil.drawLine(m, x0, 0.0f, x0 + dx, h, cA2);
            nx = x0 + dx * 0.65f;
            ny = h * 0.6f;
            Render2DUtil.drawCircle(m, nx, ny, 2.0f, new Color(nodeC, true), 28);
        }
    }

    @Inject(method={"doAttack"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/hit/HitResult;getType()Lnet/minecraft/util/hit/HitResult$Type;", shift=At.Shift.BEFORE)})
    public void onAttack(CallbackInfoReturnable<Boolean> cir) {
        Vitality.EVENT_BUS.post(DoAttackEvent.getPre());
    }

    @Inject(method={"doAttack"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V", shift=At.Shift.AFTER)})
    public void onAttackPost(CallbackInfoReturnable<Boolean> cir) {
        if (Alien.EVENT_BUS != null) {
            Alien.EVENT_BUS.post(DoAttackEvent.getPost());
        }
    }

    @Inject(method={"doAttack"}, at={@At(value="HEAD")}, cancellable=true)
    public void doAttackHook(CallbackInfoReturnable<Boolean> cir) {
        if (Alien.EVENT_BUS != null) {
            DoAttackEvent event = new DoAttackEvent();
            Alien.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method={"disconnect(Lnet/minecraft/client/gui/screen/Screen;)V"}, at={@At(value="HEAD")})
    private void onDisconnect(Screen screen, CallbackInfo info) {
        if (Alien.EVENT_BUS != null) {
            Alien.EVENT_BUS.post(GameLeftEvent.INSTANCE);
        }
        if (Alien.MODULE != null) {
            Alien.MODULE.onLogout();
        }
    }

    @Inject(method={"disconnect(Lnet/minecraft/client/gui/screen/Screen;)V"}, at={@At(value="HEAD")})
    private void clearTitleMixin(Screen screen, CallbackInfo info) {
        if (ClientSetting.INSTANCE.titleFix.getValue()) {
            this.inGameHud.clearTitle();
            this.inGameHud.setDefaultTitleFade();
        }
    }

    @Inject(method={"handleBlockBreaking"}, at={@At(value="HEAD")}, cancellable=true)
    private void handleBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (this.attackCooldown <= 0 && this.player.isUsingItem() && InteractTweaks.INSTANCE.multiTask()) {
            if (breaking && this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                Direction direction;
                BlockHitResult blockHitResult = (BlockHitResult)this.crosshairTarget;
                BlockPos blockPos = blockHitResult.getBlockPos();
                if (!this.world.getBlockState(blockPos).isAir() && this.interactionManager.updateBlockBreakingProgress(blockPos, direction = blockHitResult.getSide())) {
                    this.particleManager.addBlockBreakingParticles(blockPos, direction);
                    this.player.swingHand(Hand.MAIN_HAND);
                }
            } else {
                this.interactionManager.cancelBlockBreaking();
            }
            ci.cancel();
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"tick()V"})
    public void tickHead(CallbackInfo info) {
        block2: {
            try {
                if (Alien.EVENT_BUS != null) {
                    Alien.EVENT_BUS.post(ClientTickEvent.get(Event.Stage.Pre));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block2;
                CommandManager.sendMessage("\u00a74An error has occurred (MinecraftClient.tick() [HEAD]) Message: [" + e.getMessage() + "]");
            }
        }
    }

    @Inject(at={@At(value="TAIL")}, method={"tick()V"})
    public void tickTail(CallbackInfo info) {
        block2: {
            try {
                if (Alien.EVENT_BUS != null) {
                    Alien.EVENT_BUS.post(ClientTickEvent.get(Event.Stage.Post));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block2;
                CommandManager.sendMessage("\u00a74An error has occurred (MinecraftClient.tick() [TAIL]) Message: [" + e.getMessage() + "]");
            }
        }
    }

    /**
     * @author Alien
     * @reason Custom Window Title
     */
    @Overwrite
    private String getWindowTitle() {
        if (ClientSetting.INSTANCE == null) {
            return Alien.NAME + ": Loading..";
        }
        if (ClientSetting.INSTANCE.titleOverride.getValue()) {
            return ClientSetting.INSTANCE.windowTitle.getValue();
        }
        StringBuilder stringBuilder = new StringBuilder(ClientSetting.INSTANCE.windowTitle.getValue());
        stringBuilder.append(" ");
        stringBuilder.append(SharedConstants.getGameVersion().getName());
        ClientPlayNetworkHandler clientPlayNetworkHandler = this.getNetworkHandler();
        if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isOpen()) {
            stringBuilder.append(" - ");
            ServerInfo serverInfo = this.getCurrentServerEntry();
            if (this.server != null && !this.server.isRemote()) {
                stringBuilder.append(I18n.translate((String)"title.singleplayer", (Object[])new Object[0]));
            } else if (serverInfo != null && serverInfo.isRealm()) {
                stringBuilder.append(I18n.translate((String)"title.multiplayer.realms", (Object[])new Object[0]));
            } else if (!(server != null || serverInfo != null && serverInfo.isLocal())) {
                stringBuilder.append(I18n.translate((String)"title.multiplayer.other", (Object[])new Object[0]));
            } else {
                stringBuilder.append(I18n.translate((String)"title.multiplayer.lan", (Object[])new Object[0]));
            }
        }
        return stringBuilder.toString();
    }

    @Shadow
    public ClientPlayNetworkHandler getNetworkHandler() {
        return null;
    }

    @Shadow
    public ServerInfo getCurrentServerEntry() {
        return null;
    }

}

