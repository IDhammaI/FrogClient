/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  net.minecraft.client.render.VertexFormat
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.resource.ResourceFactory
 *  net.minecraft.util.Identifier
 */
package satin.impl;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.ResizeEvent;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import satin.api.managed.ManagedCoreShader;
import satin.api.managed.ManagedShaderEffect;
import satin.api.managed.ShaderEffectManager;
import satin.impl.ResettableManagedCoreShader;
import satin.impl.ResettableManagedShaderBase;
import satin.impl.ResettableManagedShaderEffect;

public final class ReloadableShaderEffectManager
implements ShaderEffectManager {
    public static final ReloadableShaderEffectManager INSTANCE = new ReloadableShaderEffectManager();
    private final Set<ResettableManagedShaderBase<?>> managedShaders = new ReferenceOpenHashSet();

    public ReloadableShaderEffectManager() {
        Frog.EVENT_BUS.subscribe(this);
    }

    @EventListener
    public void onResize(ResizeEvent event) {
        this.onResolutionChanged(event.window().getFramebufferWidth(), event.window().getFramebufferHeight());
    }

    @Override
    public ManagedShaderEffect manage(Identifier location) {
        return this.manage(location, s -> {});
    }

    @Override
    public ManagedShaderEffect manage(Identifier location, Consumer<ManagedShaderEffect> initCallback) {
        ResettableManagedShaderEffect ret = new ResettableManagedShaderEffect(location, initCallback);
        this.managedShaders.add(ret);
        return ret;
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location) {
        return this.manageCoreShader(location, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location, VertexFormat vertexFormat) {
        return this.manageCoreShader(location, vertexFormat, s -> {});
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location, VertexFormat vertexFormat, Consumer<ManagedCoreShader> initCallback) {
        ResettableManagedCoreShader ret = new ResettableManagedCoreShader(location, vertexFormat, initCallback);
        this.managedShaders.add(ret);
        return ret;
    }

    public void reload(ResourceFactory shaderResources) {
        for (ResettableManagedShaderBase<?> ss : this.managedShaders) {
            ss.initializeOrLog(shaderResources);
        }
    }

    public void onResolutionChanged(int newWidth, int newHeight) {
        this.runShaderSetup(newWidth, newHeight);
    }

    private void runShaderSetup(int newWidth, int newHeight) {
        if (!this.managedShaders.isEmpty()) {
            for (ResettableManagedShaderBase<?> ss : this.managedShaders) {
                if (!ss.isInitialized()) continue;
                ss.setup(newWidth, newHeight);
            }
        }
    }
}
