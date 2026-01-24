/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AirBlock
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.projectile.FishingBobberEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.math.BlockPos
 */
package dev.gzsakura_miitong.mod.modules.impl.player;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.PlaySoundEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.asm.accessors.IMinecraftClient;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.event.KeyEvent;
import net.minecraft.block.AirBlock;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class AutoTool
extends Module {
    private final BooleanSetting mine = this.add(new BooleanSetting("Mine", true));
    private final BooleanSetting fish = this.add(new BooleanSetting("Fish", true).setParent());
    private final BooleanSetting autoCast = this.add(new BooleanSetting("AutoCast", true, this.fish::isOpen));
    private final SliderSetting ticksAutoCast = this.add(new SliderSetting("TicksAutoCast", 10, 0, 60, this.fish::isOpen));
    private final SliderSetting ticksCatch = this.add(new SliderSetting("TicksCatch", 6, 0, 60, this.fish::isOpen));
    private final SliderSetting ticksThrow = this.add(new SliderSetting("TicksThrow", 14, 0, 60, this.fish::isOpen));
    private final BooleanSetting splashDetection = this.add(new BooleanSetting("SplashDetection", false, this.fish::isOpen));
    private final SliderSetting splashDetectionRange = this.add(new SliderSetting("DetectionRange", 10, 0, 60, this.fish::isOpen));
    private boolean ticksEnabled;
    private int ticksToRightClick;
    private int ticksData;
    private int autoCastTimer;
    private boolean autoCastEnabled;
    private int autoCastCheckTimer;

    public AutoTool() {
        super("AutoTool", Module.Category.Player);
        this.setChinese("\u81ea\u52a8\u5de5\u5177");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.autoFish();
        this.autoTool();
    }

    public void autoTool() {
        if (!this.mine.getValue()) {
            return;
        }
        HitResult hitResult = AutoTool.mc.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult result = (BlockHitResult)hitResult;
        BlockPos pos = result.getBlockPos();
        if (AutoTool.mc.world.isAir(pos)) {
            return;
        }
        int tool = AutoTool.getTool(pos);
        if (tool != -1 && AutoTool.mc.options.attackKey.isPressed()) {
            AutoTool.mc.player.getInventory().selectedSlot = tool;
        }
    }

    @Override
    public void onEnable() {
        this.ticksEnabled = false;
        this.autoCastEnabled = false;
        this.autoCastCheckTimer = 0;
    }

    @EventListener
    private void onPlaySound(PlaySoundEvent event) {
        if (AutoTool.nullCheck()) {
            return;
        }
        if (!this.fish.getValue()) {
            return;
        }
        SoundInstance p = event.sound;
        FishingBobberEntity elementCodec = AutoTool.mc.player.fishHook;
        if (elementCodec == null) {
            return;
        }
        if (p.getId().getPath().equals("entity.fishing_bobber.splash") && (!this.splashDetection.getValue() || MathUtil.getDistance(elementCodec.getX(), elementCodec.getY(), elementCodec.getZ(), p.getX(), p.getY(), p.getZ()) <= this.splashDetectionRange.getValue())) {
            this.ticksEnabled = true;
            this.ticksToRightClick = this.ticksCatch.getValueInt();
            this.ticksData = 0;
        }
    }

    public void autoFish() {
        if (!this.fish.getValue()) {
            return;
        }
        if (this.autoCastCheckTimer <= 0) {
            this.autoCastCheckTimer = 30;
            if (this.autoCast.getValue() && !this.ticksEnabled && !this.autoCastEnabled && AutoTool.mc.player.fishHook == null && AutoTool.mc.player.getMainHandStack().getItem() == Items.FISHING_ROD) {
                this.autoCastTimer = 0;
                this.autoCastEnabled = true;
            }
        } else {
            --this.autoCastCheckTimer;
        }
        if (this.autoCastEnabled) {
            ++this.autoCastTimer;
            if ((double)this.autoCastTimer > this.ticksAutoCast.getValue()) {
                this.autoCastEnabled = false;
                ((IMinecraftClient)mc).invokeDoItemUse();
            }
        }
        if (this.ticksEnabled && this.ticksToRightClick <= 0) {
            if (this.ticksData == 0) {
                ((IMinecraftClient)mc).invokeDoItemUse();
                this.ticksToRightClick = this.ticksThrow.getValueInt();
                this.ticksData = 1;
            } else if (this.ticksData == 1) {
                ((IMinecraftClient)mc).invokeDoItemUse();
                this.ticksEnabled = false;
            }
        }
        --this.ticksToRightClick;
    }

    @EventListener
    private void onKey(KeyEvent event) {
        if (AutoTool.mc.options.useKey.isPressed()) {
            this.ticksEnabled = false;
        }
    }

    public static int getTool(BlockPos pos) {
        int index = -1;
        float CurrentFastest = 1.0f;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoTool.mc.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY) continue;
            float digSpeed = EnchantmentHelper.getLevel(AutoTool.mc.world.getRegistryManager().get(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY).get(), stack);
            float destroySpeed = stack.getMiningSpeedMultiplier(AutoTool.mc.world.getBlockState(pos));
            if (AutoTool.mc.world.getBlockState(pos).getBlock() instanceof AirBlock) {
                return -1;
            }
            if (!(digSpeed + destroySpeed > CurrentFastest)) continue;
            CurrentFastest = digSpeed + destroySpeed;
            index = i;
        }
        return index;
    }
}

