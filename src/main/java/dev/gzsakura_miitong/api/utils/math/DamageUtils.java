/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.AttributeModifiersComponent
 *  net.minecraft.component.type.ItemEnchantmentsComponent
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.DamageUtil
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.attribute.EntityAttributeInstance
 *  net.minecraft.entity.attribute.EntityAttributeModifier
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.damage.DamageSource
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.MaceItem
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.registry.tag.DamageTypeTags
 *  net.minecraft.registry.tag.EntityTypeTags
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.BlockView
 *  net.minecraft.world.GameMode
 *  net.minecraft.world.Heightmap$Type
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Math
 */
package dev.gzsakura_miitong.api.utils.math;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.combat.CombatUtil;
import dev.gzsakura_miitong.core.impl.PlayerManager;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import dev.gzsakura_miitong.mod.modules.impl.combat.Criticals;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public class DamageUtils {
    public static final RaycastFactory HIT_FACTORY = (context, blockPos) -> {
        BlockState blockState = Wrapper.mc.world.getBlockState(blockPos);
        if (blockState.getBlock().getBlastResistance() < 600.0f) {
            return null;
        }
        return blockState.getCollisionShape((BlockView)Wrapper.mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
    };

    public static float calculateDamage(BlockPos pos, LivingEntity entity) {
        return DamageUtils.explosionDamage(entity, null, new Vec3d((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5), 12.0f);
    }

    public static float calculateDamage(Vec3d pos, LivingEntity entity) {
        return DamageUtils.explosionDamage(entity, null, pos, 12.0f);
    }

    public static float explosionDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, float power, RaycastFactory raycastFactory) {
        double modDistance = DamageUtils.getDistance(targetPos.x, targetPos.y, targetPos.z, explosionPos.x, explosionPos.y, explosionPos.z);
        if (modDistance > (double)power) {
            return 0.0f;
        }
        double exposure = DamageUtils.getExposure(explosionPos, targetBox, raycastFactory);
        double impact = (1.0 - modDistance / (double)power) * exposure;
        float damage = (int)((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0);
        return DamageUtils.calculateReductionsExplosion(damage, target, Wrapper.mc.world.getDamageSources().explosion(null));
    }

    public static float anchorDamage(LivingEntity target, LivingEntity predict, Vec3d anchor) {
        return DamageUtils.overridingExplosionDamage(target, predict, anchor, 10.0f, BlockPos.ofFloored((Position)anchor), Blocks.AIR.getDefaultState());
    }

    public static float overridingExplosionDamage(LivingEntity target, LivingEntity predict, Vec3d explosionPos, float power, BlockPos overridePos, BlockState overrideState) {
        return DamageUtils.explosionDamage(target, predict, explosionPos, power, DamageUtils.getOverridingHitFactory(overridePos, overrideState));
    }

    private static float explosionDamage(LivingEntity target, LivingEntity predict, Vec3d explosionPos, float power, RaycastFactory raycastFactory) {
        PlayerEntity player;
        if (target == null) {
            return 0.0f;
        }
        if (target instanceof PlayerEntity && DamageUtils.getGameMode(player = (PlayerEntity)target) == GameMode.CREATIVE) {
            return 0.0f;
        }
        return DamageUtils.explosionDamage(target, predict != null ? predict.getPos() : target.getPos(), predict != null ? predict.getBoundingBox() : target.getBoundingBox(), explosionPos, power, raycastFactory);
    }

    public static float explosionDamage(LivingEntity target, LivingEntity predict, Vec3d explosionPos, float power) {
        PlayerEntity player;
        if (target == null) {
            return 0.0f;
        }
        if (target instanceof PlayerEntity && DamageUtils.getGameMode(player = (PlayerEntity)target) == GameMode.CREATIVE) {
            return 0.0f;
        }
        return DamageUtils.explosionDamage(target, predict != null ? predict.getPos() : target.getPos(), predict != null ? predict.getBoundingBox() : target.getBoundingBox(), explosionPos, power, HIT_FACTORY);
    }

    public static RaycastFactory getOverridingHitFactory(BlockPos overridePos, BlockState overrideState) {
        return (context, blockPos) -> {
            BlockState blockState;
            if (blockPos.equals((Object)overridePos)) {
                blockState = overrideState;
            } else {
                blockState = Wrapper.mc.world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600.0f) {
                    return null;
                }
            }
            return blockState.getCollisionShape((BlockView)Wrapper.mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
        };
    }

    public static float getAttackDamage(LivingEntity attacker, LivingEntity target) {
        DamageSource damageSource;
        float itemDamage = (float)attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attacker instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)attacker;
            damageSource = Wrapper.mc.world.getDamageSources().playerAttack(player);
        } else {
            damageSource = Wrapper.mc.world.getDamageSources().mobAttack(attacker);
        }
        DamageSource damageSource2 = damageSource;
        StatusEffectInstance effect = attacker.getStatusEffect(StatusEffects.STRENGTH);
        if (effect != null) {
            itemDamage += 3.0f * (float)(effect.getAmplifier() + 1);
        }
        float damage = DamageUtils.modifyAttackDamage(attacker, target, attacker.getWeaponStack(), damageSource2, itemDamage);
        return DamageUtils.calculateReductions(damage, target, damageSource2);
    }

    public static float getAttackDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon) {
        DamageSource damageSource;
        EntityAttributeInstance original = attacker.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        EntityAttributeInstance copy = new EntityAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE, o -> {});
        copy.setBaseValue(original.getBaseValue());
        for (EntityAttributeModifier modifier2 : original.getModifiers()) {
            copy.addTemporaryModifier(modifier2);
        }
        copy.removeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID);
        AttributeModifiersComponent attributeModifiers = (AttributeModifiersComponent)weapon.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            attributeModifiers.applyModifiers(EquipmentSlot.MAINHAND, (entry, modifier) -> {
                if (entry == EntityAttributes.GENERIC_ATTACK_DAMAGE) {
                    copy.updateModifier(modifier);
                }
            });
        }
        float itemDamage = (float)copy.getValue();
        if (attacker instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)attacker;
            damageSource = Wrapper.mc.world.getDamageSources().playerAttack(player);
        } else {
            damageSource = Wrapper.mc.world.getDamageSources().mobAttack(attacker);
        }
        DamageSource damageSource2 = damageSource;
        float damage = DamageUtils.modifyAttackDamage(attacker, target, weapon, damageSource2, itemDamage);
        return DamageUtils.calculateReductions(damage, target, damageSource2);
    }

    private static float modifyAttackDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon, DamageSource damageSource, float damage) {
        int smite;
        int impaling;
        int baneOfArthropods;
        Object2IntOpenHashMap enchantments = new Object2IntOpenHashMap();
        DamageUtils.getEnchantments(weapon, (Object2IntMap<RegistryEntry<Enchantment>>)enchantments);
        float enchantDamage = 0.0f;
        int sharpness = DamageUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.SHARPNESS);
        if (sharpness > 0) {
            enchantDamage += 1.0f + 0.5f * (float)(sharpness - 1);
        }
        if ((baneOfArthropods = DamageUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.BANE_OF_ARTHROPODS)) > 0 && target.getType().isIn(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
            enchantDamage += 2.5f * (float)baneOfArthropods;
        }
        if ((impaling = DamageUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.IMPALING)) > 0 && target.getType().isIn(EntityTypeTags.SENSITIVE_TO_IMPALING)) {
            enchantDamage += 2.5f * (float)impaling;
        }
        if ((smite = DamageUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.SMITE)) > 0 && target.getType().isIn(EntityTypeTags.SENSITIVE_TO_SMITE)) {
            enchantDamage += 2.5f * (float)smite;
        }
        if (attacker instanceof PlayerEntity) {
            MaceItem item;
            float bonusDamage;
            PlayerEntity playerEntity = (PlayerEntity)attacker;
            float charge = playerEntity.getAttackCooldownProgress(0.5f);
            damage *= 0.2f + charge * charge * 0.8f;
            enchantDamage *= charge;
            Item item2 = weapon.getItem();
            if (item2 instanceof MaceItem && (bonusDamage = (item = (MaceItem)item2).getBonusAttackDamage((Entity)target, damage, damageSource)) > 0.0f) {
                int density = DamageUtils.getEnchantmentLevel(weapon, (RegistryKey<Enchantment>)Enchantments.DENSITY);
                if (density > 0) {
                    bonusDamage += 0.5f * attacker.fallDistance;
                }
                damage += bonusDamage;
            }
            if (!(!(charge > 0.9f) || !(attacker.fallDistance > 0.0f) && (attacker != Wrapper.mc.player || !Criticals.INSTANCE.isOn() || Criticals.INSTANCE.mode.is(Criticals.Mode.Ground) || !Wrapper.mc.player.isOnGround() && Criticals.INSTANCE.onlyGround.getValue()) || attacker.isOnGround() && (attacker != Wrapper.mc.player || !Criticals.INSTANCE.isOn() || Criticals.INSTANCE.mode.is(Criticals.Mode.Ground)) || attacker.isClimbing() || attacker.isTouchingWater() || attacker.hasStatusEffect(StatusEffects.BLINDNESS) || attacker.hasVehicle())) {
                damage *= 1.5f;
            }
        }
        return damage + enchantDamage;
    }

    public static float fallDamage(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (player.getAbilities().flying) {
                return 0.0f;
            }
        }
        if (entity.hasStatusEffect(StatusEffects.SLOW_FALLING) || entity.hasStatusEffect(StatusEffects.LEVITATION)) {
            return 0.0f;
        }
        int surface = Wrapper.mc.world.getWorldChunk(entity.getBlockPos()).getHeightmap(Heightmap.Type.MOTION_BLOCKING).get(entity.getBlockX() & 0xF, entity.getBlockZ() & 0xF);
        if (entity.getBlockY() >= surface) {
            return DamageUtils.fallDamageReductions(entity, surface);
        }
        BlockHitResult raycastResult = Wrapper.mc.world.raycast(new RaycastContext(entity.getPos(), new Vec3d(entity.getX(), (double)Wrapper.mc.world.getBottomY(), entity.getZ()), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.WATER, (Entity)entity));
        if (raycastResult.getType() == HitResult.Type.MISS) {
            return 0.0f;
        }
        return DamageUtils.fallDamageReductions(entity, raycastResult.getBlockPos().getY());
    }

    private static float fallDamageReductions(LivingEntity entity, int surface) {
        int fallHeight = (int)(entity.getY() - (double)surface + (double)entity.fallDistance - 3.0);
        @Nullable StatusEffectInstance jumpBoostInstance = entity.getStatusEffect(StatusEffects.JUMP_BOOST);
        if (jumpBoostInstance != null) {
            fallHeight -= jumpBoostInstance.getAmplifier() + 1;
        }
        return DamageUtils.calculateReductions(fallHeight, entity, Wrapper.mc.world.getDamageSources().fall());
    }

    public static float calculateReductionsExplosion(float damage, LivingEntity entity, DamageSource damageSource) {
        if (damageSource.isScaledWithDifficulty()) {
            switch (Wrapper.mc.world.getDifficulty()) {
                case EASY: {
                    damage = Math.min(damage / 2.0f + 1.0f, damage);
                    break;
                }
                case HARD: {
                    damage *= 1.5f;
                }
            }
        }
        damage = DamageUtil.getDamageLeft((LivingEntity)entity, (float)damage, (DamageSource)damageSource, (float)DamageUtils.getArmor(entity), (float)((float)DamageUtils.getARMOR_TOUGHNESS(entity)));
        damage = DamageUtils.resistanceReduction(entity, damage);
        damage = DamageUtil.getInflictedDamage((float)damage, (float)DamageUtils.getProtectionAmount(entity.getArmorItems()));
        return Math.max(damage, 0.0f);
    }

    public static float calculateReductions(float damage, LivingEntity entity, DamageSource damageSource) {
        if (damageSource.isScaledWithDifficulty()) {
            switch (Wrapper.mc.world.getDifficulty()) {
                case EASY: {
                    damage = Math.min(damage / 2.0f + 1.0f, damage);
                    break;
                }
                case HARD: {
                    damage *= 1.5f;
                }
            }
        }
        damage = DamageUtil.getDamageLeft((LivingEntity)entity, (float)damage, (DamageSource)damageSource, (float)DamageUtils.getArmor(entity), (float)((float)DamageUtils.getARMOR_TOUGHNESS(entity)));
        damage = DamageUtils.resistanceReduction(entity, damage);
        damage = DamageUtils.protectionReduction(entity, damage, damageSource);
        return Math.max(damage, 0.0f);
    }

    public static double getARMOR_TOUGHNESS(LivingEntity entity) {
        PlayerEntity player;
        PlayerManager.EntityAttribute entityAttribute;
        if (entity instanceof PlayerEntity && (entityAttribute = Alien.PLAYER.map.get(player = (PlayerEntity)entity)) != null) {
            return entityAttribute.toughness();
        }
        return entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
    }

    private static float getArmor(LivingEntity entity) {
        PlayerEntity player;
        PlayerManager.EntityAttribute entityAttribute;
        if (entity instanceof PlayerEntity && (entityAttribute = Alien.PLAYER.map.get(player = (PlayerEntity)entity)) != null) {
            return entityAttribute.armor();
        }
        return (float)Math.floor(entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR));
    }

    private static float protectionReduction(LivingEntity player, float damage, DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return damage;
        }
        int damageProtection = 0;
        for (ItemStack stack : player.getAllArmorItems()) {
            int featherFalling;
            int projectileProtection;
            int blastProtection;
            int fireProtection;
            Object2IntOpenHashMap enchantments = new Object2IntOpenHashMap();
            DamageUtils.getEnchantments(stack, (Object2IntMap<RegistryEntry<Enchantment>>)enchantments);
            int protection = DamageUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.PROTECTION);
            if (protection > 0) {
                damageProtection += protection;
            }
            if ((fireProtection = DamageUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.FIRE_PROTECTION)) > 0 && source.isIn(DamageTypeTags.IS_FIRE)) {
                damageProtection += 2 * fireProtection;
            }
            if ((blastProtection = DamageUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.BLAST_PROTECTION)) > 0 && source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                damageProtection += 2 * blastProtection;
            }
            if ((projectileProtection = DamageUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.PROJECTILE_PROTECTION)) > 0 && source.isIn(DamageTypeTags.IS_PROJECTILE)) {
                damageProtection += 2 * projectileProtection;
            }
            if ((featherFalling = DamageUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.FEATHER_FALLING)) <= 0 || !source.isIn(DamageTypeTags.IS_FALL)) continue;
            damageProtection += 3 * featherFalling;
        }
        return DamageUtil.getInflictedDamage((float)damage, (float)damageProtection);
    }

    public static int getProtectionAmount(Iterable<ItemStack> equipment) {
        MutableInt mutableInt = new MutableInt();
        equipment.forEach(i -> mutableInt.add(DamageUtils.getProtectionAmount(i)));
        return mutableInt.intValue();
    }

    public static int getProtectionAmount(ItemStack stack) {
        int modifierBlast = EnchantmentHelper.getLevel((RegistryEntry)((RegistryEntry)Wrapper.mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get()), (ItemStack)stack);
        int modifier = EnchantmentHelper.getLevel((RegistryEntry)((RegistryEntry)Wrapper.mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.PROTECTION).get()), (ItemStack)stack);
        return modifierBlast * 2 + modifier;
    }

    private static float resistanceReduction(LivingEntity player, float damage) {
        StatusEffectInstance resistance = player.getStatusEffect(StatusEffects.RESISTANCE);
        if (resistance != null) {
            int lvl = resistance.getAmplifier() + 1;
            damage *= 1.0f - (float)lvl * 0.2f;
        }
        return Math.max(damage, 0.0f);
    }

    private static float getExposure(Vec3d source, Box box, RaycastFactory raycastFactory) {
        if (ClientSetting.INSTANCE.optimizedCalc.getValue()) {
            int miss = 0;
            int hit = 0;
            for (int k = 0; k <= 1; ++k) {
                for (int l = 0; l <= 1; ++l) {
                    for (int m = 0; m <= 1; ++m) {
                        double p;
                        double o;
                        double n = MathHelper.lerp((double)k, (double)box.minX, (double)box.maxX);
                        Vec3d vec3d = new Vec3d(n, o = MathHelper.lerp((double)l, (double)box.minY, (double)box.maxY), p = MathHelper.lerp((double)m, (double)box.minZ, (double)box.maxZ));
                        if (DamageUtils.raycast(vec3d, source, CombatUtil.terrainIgnore) == HitResult.Type.MISS) {
                            ++miss;
                        }
                        ++hit;
                    }
                }
            }
            return (float)miss / (float)hit;
        }
        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;
        double xStep = 1.0 / (xDiff * 2.0 + 1.0);
        double yStep = 1.0 / (yDiff * 2.0 + 1.0);
        double zStep = 1.0 / (zDiff * 2.0 + 1.0);
        if (xStep > 0.0 && yStep > 0.0 && zStep > 0.0) {
            int misses = 0;
            int hits = 0;
            double xOffset = (1.0 - Math.floor(1.0 / xStep) * xStep) * 0.5;
            double zOffset = (1.0 - Math.floor(1.0 / zStep) * zStep) * 0.5;
            xStep *= xDiff;
            yStep *= yDiff;
            zStep *= zDiff;
            double startX = box.minX + xOffset;
            double startY = box.minY;
            double startZ = box.minZ + zOffset;
            double endX = box.maxX + xOffset;
            double endY = box.maxY;
            double endZ = box.maxZ + zOffset;
            for (double x = startX; x <= endX; x += xStep) {
                for (double y = startY; y <= endY; y += yStep) {
                    for (double z = startZ; z <= endZ; z += zStep) {
                        Vec3d position = new Vec3d(x, y, z);
                        if (DamageUtils.raycast(new ExposureRaycastContext(position, source), raycastFactory) == null) {
                            ++misses;
                        }
                        ++hits;
                    }
                }
            }
            return (float)misses / (float)hits;
        }
        return 0.0f;
    }

    public static HitResult.Type raycast(Vec3d start, Vec3d end, boolean ignoreTerrain) {
        return (HitResult.Type)BlockView.raycast((Vec3d)start, (Vec3d)end, null, (innerContext, blockPos) -> {
            BlockState blockState = Wrapper.mc.world.getBlockState(blockPos);
            if (blockState.getBlock().getBlastResistance() < 600.0f && ignoreTerrain) {
                return null;
            }
            BlockHitResult hitResult = blockState.getCollisionShape((BlockView)Wrapper.mc.world, blockPos).raycast(start, end, blockPos);
            return hitResult == null ? null : hitResult.getType();
        }, innerContext -> HitResult.Type.MISS);
    }

    public static BlockHitResult raycast(ExposureRaycastContext context, RaycastFactory raycastFactory) {
        return (BlockHitResult)BlockView.raycast((Vec3d)context.start(), (Vec3d)context.end(), (Object)context, (BiFunction)raycastFactory, ctx -> null);
    }

    public static int getEnchantmentLevel(ItemStack itemStack, RegistryKey<Enchantment> enchantment) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        Object2IntArrayMap itemEnchantments = new Object2IntArrayMap();
        DamageUtils.getEnchantments(itemStack, (Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments);
        return DamageUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments, enchantment);
    }

    public static int getEnchantmentLevel(Object2IntMap<RegistryEntry<Enchantment>> itemEnchantments, RegistryKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(itemEnchantments)) {
            if (!((RegistryEntry)entry.getKey()).matchesKey(enchantment)) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(DamageUtils.squaredDistance(x1, y1, z1, x2, y2, z2));
    }

    public static GameMode getGameMode(PlayerEntity player) {
        if (player == null) {
            return null;
        }
        PlayerListEntry playerListEntry = Wrapper.mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (playerListEntry == null) {
            return null;
        }
        return playerListEntry.getGameMode();
    }

    public static double squaredDistanceTo(Entity entity) {
        return DamageUtils.squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ());
    }

    public static double squaredDistanceTo(BlockPos blockPos) {
        return DamageUtils.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static double squaredDistanceTo(double x, double y, double z) {
        return DamageUtils.squaredDistance(Wrapper.mc.player.getX(), Wrapper.mc.player.getY(), Wrapper.mc.player.getZ(), x, y, z);
    }

    public static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double f = x1 - x2;
        double g = y1 - y2;
        double h = z1 - z2;
        return org.joml.Math.fma((double)f, (double)f, (double)org.joml.Math.fma((double)g, (double)g, (double)(h * h)));
    }

    public static void getEnchantments(ItemStack itemStack, Object2IntMap<RegistryEntry<Enchantment>> enchantments) {
        enchantments.clear();
        if (!itemStack.isEmpty()) {
            Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> itemEnchantments = itemStack.getItem() == Items.ENCHANTED_BOOK ? ((ItemEnchantmentsComponent)itemStack.get(DataComponentTypes.STORED_ENCHANTMENTS)).getEnchantmentEntries() : itemStack.getEnchantments().getEnchantmentEntries();
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantments) {
                enchantments.put(entry.getKey(), entry.getIntValue());
            }
        }
    }

    @FunctionalInterface
    public static interface RaycastFactory
    extends BiFunction<ExposureRaycastContext, BlockPos, BlockHitResult> {
    }

    public record ExposureRaycastContext(Vec3d start, Vec3d end) {
    }
}

