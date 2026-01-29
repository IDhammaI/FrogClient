/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Pair
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.DisconnectedScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.TitleScreen
 *  net.minecraft.client.gui.screen.multiplayer.ConnectScreen
 *  net.minecraft.client.gui.widget.ButtonWidget
 *  net.minecraft.client.gui.widget.ButtonWidget$Builder
 *  net.minecraft.client.gui.widget.DirectionalLayoutWidget
 *  net.minecraft.client.gui.widget.Widget
 *  net.minecraft.client.network.ServerAddress
 *  net.minecraft.client.network.ServerInfo
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.mod.modules.impl.misc.AutoReconnect;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={DisconnectedScreen.class})
public abstract class MixinDisconnectedScreen
extends Screen {
    @Shadow
    @Final
    private DirectionalLayoutWidget grid;
    @Unique
    private ButtonWidget reconnectBtn;
    @Unique
    private double time;

    protected MixinDisconnectedScreen(Text title) {
        super(title);
        this.time = AutoReconnect.INSTANCE.delay.getValue() * 20.0;
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V", shift=At.Shift.BEFORE)})
    private void addButtons(CallbackInfo ci) {
        if (AutoReconnect.INSTANCE.lastServerConnection != null) {
            this.reconnectBtn = new ButtonWidget.Builder((Text)Text.literal((String)this.getText()), button -> this.tryConnecting()).build();
            this.grid.add((Widget)this.reconnectBtn);
        }
    }

    public void tick() {
        if (!AutoReconnect.INSTANCE.rejoin() || AutoReconnect.INSTANCE.lastServerConnection == null) {
            return;
        }
        if (this.time <= 0.0) {
            this.tryConnecting();
        } else {
            this.time -= 1.0;
            if (this.reconnectBtn != null) {
                this.reconnectBtn.setMessage((Text)Text.literal((String)this.getText()));
            }
        }
    }

    @Unique
    private String getText() {
        Object reconnectText = "Reconnect";
        if (AutoReconnect.INSTANCE.rejoin()) {
            reconnectText = (String)reconnectText + " " + String.format("(%.1f)", this.time / 20.0);
        }
        return (String)reconnectText;
    }

    @Unique
    private void tryConnecting() {
        Pair<ServerAddress, ServerInfo> lastServer = AutoReconnect.INSTANCE.lastServerConnection;
        ConnectScreen.connect((Screen)new TitleScreen(), (MinecraftClient)Wrapper.mc, (ServerAddress)((ServerAddress)lastServer.left()), (ServerInfo)((ServerInfo)lastServer.right()), (boolean)false, null);
    }
}

