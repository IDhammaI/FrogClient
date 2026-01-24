/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.AirBlockItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.world.BlockView
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.utils.math.Easing;
import dev.gzsakura_miitong.api.utils.player.InventoryUtil;
import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render3DUtil;
import dev.gzsakura_miitong.core.impl.BreakManager;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.BlockView;

public class BreakESP
extends Module {
    public static BreakESP INSTANCE;
    private final BooleanSetting progress = this.add(new BooleanSetting("Progress", true));
    private final SliderSetting damage = this.add(new SliderSetting("Damage", 1.0, 0.0, 2.0, 0.01));
    private final ColorSetting box = this.add(new ColorSetting("Box", new Color(198, 176, 12, 255)).injectBoolean(true));
    private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(198, 176, 12, 78)).injectBoolean(true));
    private final ColorSetting boxFriend = this.add(new ColorSetting("FriendBox", new Color(30, 45, 169, 255)).injectBoolean(true));
    private final ColorSetting fillFriend = this.add(new ColorSetting("FriendFill", new Color(30, 45, 169, 78)).injectBoolean(true));
    private final EnumSetting<Easing> ease = this.add(new EnumSetting<Easing>("Ease", Easing.CubicInOut));
    private final BooleanSetting second = this.add(new BooleanSetting("Second", true));
    private final ColorSetting secondBox = this.add(new ColorSetting("SecondBox", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting secondFill = this.add(new ColorSetting("SecondFill", new Color(255, 255, 255, 100)).injectBoolean(true));
    final DecimalFormat df = new DecimalFormat("0.0");
    final Color startColor = new Color(255, 6, 6);
    final Color endColor = new Color(0, 255, 12);
    final Color doubleColor = new Color(255, 179, 96);

    public BreakESP() {
        super("BreakESP", Module.Category.Render);
        this.setChinese("\u6316\u6398\u663e\u793a");
        INSTANCE = this;
    }

    private Color getFillColor(PlayerEntity player) {
        return Alien.FRIEND.isFriend(player) ? this.fillFriend.getValue() : this.fill.getValue();
    }

    private Color getBoxColor(PlayerEntity player) {
        return Alien.FRIEND.isFriend(player) ? this.boxFriend.getValue() : this.box.getValue();
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        for (BreakManager.BreakData breakData : Alien.BREAK.breakMap.values()) {
            if (breakData == null || breakData.getEntity() == null) continue;
            PlayerEntity player = (PlayerEntity)breakData.getEntity();
            double size = 0.5 * (1.0 - breakData.fade.ease(this.ease.getValue()));
            Box cbox = new Box(breakData.pos).shrink(size, size, size).shrink(-size, -size, -size);
            if (this.fill.booleanValue) {
                Render3DUtil.drawFill(matrixStack, cbox, this.getFillColor(player));
            }
            if (this.box.booleanValue) {
                Render3DUtil.drawBox(matrixStack, cbox, this.getBoxColor(player));
            }
            Render3DUtil.drawText3D(player.getName().getString(), breakData.pos.toCenterPos().add(0.0, this.progress.getValue() ? 0.15 : 0.0, 0.0), -1);
            if (!this.progress.getValue()) continue;
            Render3DUtil.drawText3D(Text.of((String)(breakData.failed ? "\u00a74Failed" : (breakData.complete ? "Broke" : this.df.format(Math.min(1.0, (double)breakData.timer.getMs() / breakData.breakTime) * 100.0)))), breakData.pos.toCenterPos().add(0.0, -0.15, 0.0), 0.0, 0.0, 1.0, breakData.complete ? (BreakESP.mc.world.isAir(breakData.pos) ? this.endColor : this.startColor) : ColorUtil.fadeColor(this.startColor, this.endColor, (double)breakData.timer.getMs() / breakData.breakTime));
        }
        if (this.second.getValue()) {
            Iterator<BreakManager.BreakData> iterator = ((ConcurrentHashMap.KeySetView)Alien.BREAK.doubleMap.keySet()).iterator();
            while (iterator.hasNext()) {
                int i = (Integer)((Object)iterator.next());
                BreakManager.BreakData breakData = Alien.BREAK.doubleMap.get(i);
                if (breakData == null || breakData.getEntity() == null || BreakESP.mc.world.isAir(breakData.pos)) {
                    Alien.BREAK.doubleMap.remove(i);
                    continue;
                }
                BreakManager.BreakData singleBreakData = Alien.BREAK.breakMap.get(i);
                if (singleBreakData != null && singleBreakData.pos.equals((Object)breakData.pos)) continue;
                double size = 0.5 * (1.0 - breakData.fade.ease(this.ease.getValue()));
                Box cbox = new Box(breakData.pos).shrink(size, size, size).shrink(-size, -size, -size);
                if (this.secondFill.booleanValue) {
                    Render3DUtil.drawFill(matrixStack, cbox, this.secondFill.getValue());
                }
                if (this.secondBox.booleanValue) {
                    Render3DUtil.drawBox(matrixStack, cbox, this.secondBox.getValue());
                }
                Render3DUtil.drawText3D(breakData.getEntity().getName().getString(), breakData.pos.toCenterPos().add(0.0, 0.15, 0.0), -1);
                Render3DUtil.drawText3D("Double", breakData.pos.toCenterPos().add(0.0, -0.15, 0.0), this.doubleColor.getRGB());
            }
        }
    }

    public static double getBreakTime(BlockPos pos, boolean extraBreak) {
        int slot = BreakESP.getTool(pos);
        if (slot == -1) {
            slot = BreakESP.mc.player.getInventory().selectedSlot;
        }
        return BreakESP.getBreakTime(pos, slot, extraBreak ? 1.0 : BreakESP.INSTANCE.damage.getValue());
    }

    static int getTool(BlockPos pos) {
        AtomicInteger slot = new AtomicInteger();
        slot.set(-1);
        float CurrentFastest = 1.0f;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            float destroySpeed;
            float digSpeed;
            if (entry.getValue().getItem() instanceof AirBlockItem || !((digSpeed = (float)EnchantmentHelper.getLevel((RegistryEntry)((RegistryEntry)BreakESP.mc.world.getRegistryManager().get(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY).get()), (ItemStack)entry.getValue())) + (destroySpeed = entry.getValue().getMiningSpeedMultiplier(BreakESP.mc.world.getBlockState(pos))) > CurrentFastest)) continue;
            CurrentFastest = digSpeed + destroySpeed;
            slot.set(entry.getKey());
        }
        return slot.get();
    }

    static double getBreakTime(BlockPos pos, int slot, double damage) {
        return (double)(1.0f / BreakESP.getBlockStrength(pos, BreakESP.mc.player.getInventory().getStack(slot)) / 20.0f * 1000.0f) * damage;
    }

    static float getBlockStrength(BlockPos position, ItemStack itemStack) {
        BlockState state = BreakESP.mc.world.getBlockState(position);
        float hardness = state.getHardness((BlockView)BreakESP.mc.world, position);
        if (hardness < 0.0f) {
            return 0.0f;
        }
        float i = !state.isToolRequired() || itemStack.isSuitableFor(state) ? 30.0f : 100.0f;
        return BreakESP.getDigSpeed(state, itemStack) / hardness / i;
    }

    static float getDigSpeed(BlockState state, ItemStack itemStack) {
        int efficiencyModifier;
        float digSpeed = BreakESP.getDestroySpeed(state, itemStack);
        if (digSpeed > 1.0f && (efficiencyModifier = EnchantmentHelper.getLevel((RegistryEntry)((RegistryEntry)BreakESP.mc.world.getRegistryManager().get(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY).get()), (ItemStack)itemStack)) > 0 && !itemStack.isEmpty()) {
            digSpeed += (float)(StrictMath.pow(efficiencyModifier, 2.0) + 1.0);
        }
        return digSpeed < 0.0f ? 0.0f : digSpeed;
    }

    static float getDestroySpeed(BlockState state, ItemStack itemStack) {
        float destroySpeed = 1.0f;
        if (itemStack != null && !itemStack.isEmpty()) {
            destroySpeed *= itemStack.getMiningSpeedMultiplier(state);
        }
        return destroySpeed;
    }
}

