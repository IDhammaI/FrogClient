/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.entity.projectile.thrown.EnderPearlEntity
 *  net.minecraft.entity.projectile.thrown.ExperienceBottleEntity
 *  net.minecraft.item.BowItem
 *  net.minecraft.item.CrossbowItem
 *  net.minecraft.item.EggItem
 *  net.minecraft.item.EnderPearlItem
 *  net.minecraft.item.ExperienceBottleItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.LingeringPotionItem
 *  net.minecraft.item.SnowballItem
 *  net.minecraft.item.SplashPotionItem
 *  net.minecraft.item.TridentItem
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package dev.idhammai.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.Frog;
import dev.idhammai.api.utils.math.MathUtil;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render3DUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.player.AutoPearl;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class Trajectories
extends Module {
    static MatrixStack matrixStack;
    private final ColorSetting hand = this.add(new ColorSetting("Hand", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting pearl = this.add(new ColorSetting("Pearl", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting arrow = this.add(new ColorSetting("Arrow", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting xp = this.add(new ColorSetting("XP", new Color(255, 255, 255, 255)).injectBoolean(true));

    public Trajectories() {
        super("Trajectories", Module.Category.Render);
        this.setChinese("\u629b\u7269\u7ebf\u9884\u6d4b");
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (Trajectories.nullCheck()) {
            return;
        }
        Trajectories.matrixStack = matrixStack;
        if (this.pearl.booleanValue || this.arrow.booleanValue || this.xp.booleanValue) {
            RenderSystem.disableDepthTest();
            for (Entity en : Frog.THREAD.getEntities()) {
                if (en instanceof EnderPearlEntity && this.pearl.booleanValue) {
                    this.calcTrajectory(en, this.pearl.getValue());
                }
                if (en instanceof ArrowEntity && this.arrow.booleanValue) {
                    this.calcTrajectory(en, this.arrow.getValue());
                }
                if (!(en instanceof ExperienceBottleEntity) || !this.xp.booleanValue) continue;
                this.calcTrajectory(en, this.xp.getValue());
            }
            RenderSystem.enableDepthTest();
        }
        if (this.hand.booleanValue) {
            Hand hand;
            if (!Trajectories.mc.options.getPerspective().isFirstPerson()) {
                return;
            }
            ItemStack mainHand = Trajectories.mc.player.getMainHandStack();
            ItemStack offHand = Trajectories.mc.player.getOffHandStack();
            if (mainHand.getItem() instanceof BowItem || mainHand.getItem() instanceof CrossbowItem || this.isThrowable(mainHand.getItem()) || AutoPearl.INSTANCE.isOn()) {
                hand = Hand.MAIN_HAND;
            } else if (offHand.getItem() instanceof BowItem || offHand.getItem() instanceof CrossbowItem || this.isThrowable(offHand.getItem())) {
                hand = Hand.OFF_HAND;
            } else {
                return;
            }
            RenderSystem.disableDepthTest();
            boolean prev_bob = (Boolean)Trajectories.mc.options.getBobView().getValue();
            Trajectories.mc.options.getBobView().setValue(false);
            double x = MathUtil.interpolate(Trajectories.mc.player.prevX, Trajectories.mc.player.getX(), (double)mc.getRenderTickCounter().getTickDelta(true));
            double y = MathUtil.interpolate(Trajectories.mc.player.prevY, Trajectories.mc.player.getY(), (double)mc.getRenderTickCounter().getTickDelta(true));
            double z = MathUtil.interpolate(Trajectories.mc.player.prevZ, Trajectories.mc.player.getZ(), (double)mc.getRenderTickCounter().getTickDelta(true));
            if (offHand.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel(Trajectories.mc.world.getRegistryManager().get(Enchantments.MULTISHOT.getRegistryRef()).getEntry(Enchantments.MULTISHOT).get(), offHand) != 0 || mainHand.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel(Trajectories.mc.world.getRegistryManager().get(Enchantments.MULTISHOT.getRegistryRef()).getEntry(Enchantments.MULTISHOT).get(), mainHand) != 0) {
                this.calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), Trajectories.mc.player.getYaw() - 10.0f, x, y, z);
                this.calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), Trajectories.mc.player.getYaw(), x, y, z);
                this.calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), Trajectories.mc.player.getYaw() + 10.0f, x, y, z);
            } else {
                this.calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), Trajectories.mc.player.getYaw(), x, y, z);
            }
            Trajectories.mc.options.getBobView().setValue(prev_bob);
            RenderSystem.enableDepthTest();
        }
    }

    private void calcTrajectory(Entity e, Color color) {
        double motionX = e.getVelocity().x;
        double motionY = e.getVelocity().y;
        double motionZ = e.getVelocity().z;
        if (motionX == 0.0 && motionY == 0.0 && motionZ == 0.0) {
            return;
        }
        double x = e.getX();
        double y = e.getY();
        double z = e.getZ();
        for (int i = 0; i < 300; ++i) {
            Vec3d lastPos = new Vec3d(x, y, z);
            if (Trajectories.mc.world.getBlockState(new BlockPos((int)(x += motionX), (int)(y += motionY), (int)(z += motionZ))).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }
            motionY = e instanceof ArrowEntity ? (motionY -= (double)0.05f) : (motionY -= (double)0.03f);
            Vec3d pos = new Vec3d(x, y, z);
            if (Trajectories.mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)Trajectories.mc.player)) != null && (Trajectories.mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)Trajectories.mc.player)).getType() == HitResult.Type.ENTITY || Trajectories.mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)Trajectories.mc.player)).getType() == HitResult.Type.BLOCK) || y <= -65.0) break;
            int alpha = (int)MathUtil.clamp(255.0f * ((float)(i + 1) / 10.0f), 0.0f, 255.0f);
            Render3DUtil.drawLine(lastPos, pos, ColorUtil.injectAlpha(color, alpha));
        }
    }

    private void calcTrajectory(Item item, float yaw, double x, double y, double z) {
        y = y + (double)Trajectories.mc.player.getEyeHeight(Trajectories.mc.player.getPose()) - 0.1000000014901161;
        if (item == Trajectories.mc.player.getMainHandStack().getItem()) {
            x -= (double)(MathHelper.cos((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
            z -= (double)(MathHelper.sin((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
        } else {
            x += (double)(MathHelper.cos((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
            z += (double)(MathHelper.sin((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
        }
        float maxDist = this.getDistance(item);
        double motionX = -MathHelper.sin((float)(yaw / 180.0f * (float)Math.PI)) * MathHelper.cos((float)(Trajectories.mc.player.getPitch() / 180.0f * (float)Math.PI)) * maxDist;
        double motionY = -MathHelper.sin((float)((Trajectories.mc.player.getPitch() - (float)this.getThrowPitch(item)) / 180.0f * 3.141593f)) * maxDist;
        double motionZ = MathHelper.cos((float)(yaw / 180.0f * (float)Math.PI)) * MathHelper.cos((float)(Trajectories.mc.player.getPitch() / 180.0f * (float)Math.PI)) * maxDist;
        float power = (float)Trajectories.mc.player.getItemUseTime() / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;
        if (power > 1.0f) {
            power = 1.0f;
        }
        float getDistance = MathHelper.sqrt((float)((float)(motionX * motionX + motionY * motionY + motionZ * motionZ)));
        motionX /= (double)getDistance;
        motionY /= (double)getDistance;
        motionZ /= (double)getDistance;
        float pow = (item instanceof BowItem ? power * 2.0f : (item instanceof CrossbowItem ? 2.2f : 1.0f)) * this.getThrowVelocity(item);
        motionX *= (double)pow;
        motionY *= (double)pow;
        motionZ *= (double)pow;
        motionX += Trajectories.mc.player.getVelocity().getX();
        motionY += Trajectories.mc.player.getVelocity().getY();
        motionZ += Trajectories.mc.player.getVelocity().getZ();
        for (int i = 0; i < 300; ++i) {
            BlockHitResult bhr;
            Vec3d lastPos = new Vec3d(x, y, z);
            if (Trajectories.mc.world.getBlockState(new BlockPos((int)(x += motionX), (int)(y += motionY), (int)(z += motionZ))).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }
            motionY = item instanceof BowItem ? (motionY -= (double)0.05f) : (Trajectories.mc.player.getMainHandStack().getItem() instanceof CrossbowItem ? (motionY -= (double)0.05f) : (motionY -= (double)0.03f));
            Vec3d pos = new Vec3d(x, y, z);
            for (Entity ent : Frog.THREAD.getEntities()) {
                if (ent instanceof ArrowEntity || ent.equals((Object)Trajectories.mc.player) || !ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.3))) continue;
                Render3DUtil.drawBox(matrixStack, ent.getBoundingBox(), this.hand.getValue());
                break;
            }
            if ((bhr = Trajectories.mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)Trajectories.mc.player))) != null && bhr.getType() == HitResult.Type.BLOCK) {
                Render3DUtil.drawBox(matrixStack, new Box(bhr.getBlockPos()), this.hand.getValue());
                break;
            }
            if (y <= -65.0) break;
            if (motionX == 0.0 && motionY == 0.0 && motionZ == 0.0) continue;
            Render3DUtil.drawLine(lastPos, pos, this.hand.getValue());
        }
    }

    private boolean isThrowable(Item item) {
        return item instanceof EnderPearlItem || item instanceof TridentItem || item instanceof ExperienceBottleItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
    }

    private float getDistance(Item item) {
        return item instanceof BowItem ? 1.0f : 0.4f;
    }

    private float getThrowVelocity(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
            return 0.5f;
        }
        if (item instanceof ExperienceBottleItem) {
            return 0.59f;
        }
        if (item instanceof TridentItem) {
            return 2.0f;
        }
        return 1.5f;
    }

    private int getThrowPitch(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof ExperienceBottleItem) {
            return 20;
        }
        return 0;
    }
}

