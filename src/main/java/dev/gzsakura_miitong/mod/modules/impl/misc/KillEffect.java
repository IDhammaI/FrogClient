/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.LightningEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.world.World
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.DeathEvent;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class KillEffect
extends Module {
    private final BooleanSetting lightning = this.add(new BooleanSetting("Lightning", true));
    private final BooleanSetting levelUp = this.add(new BooleanSetting("LevelUp", true).setParent());
    private final SliderSetting lMaxPitch = this.add(new SliderSetting("LMaxPitch", 1.0, 0.0, 2.0, 0.1, this.levelUp::isOpen));
    private final SliderSetting lMinPitch = this.add(new SliderSetting("LMinPitch", 1.0, 0.0, 2.0, 0.1, this.levelUp::isOpen));
    private final BooleanSetting trident = this.add(new BooleanSetting("Trident", false).setParent());
    private final SliderSetting tMaxPitch = this.add(new SliderSetting("TMaxPitch", 1.0, 0.0, 2.0, 0.1, this.trident::isOpen));
    private final SliderSetting tMinPitch = this.add(new SliderSetting("TMinPitch", 1.0, 0.0, 2.0, 0.1, this.trident::isOpen));
    private final SliderSetting factor = this.add(new SliderSetting("Factor", 1.0, 1.0, 10.0, 1.0));

    public KillEffect() {
        super("KillEffect", Module.Category.Misc);
        this.setChinese("\u51fb\u6740\u6548\u679c");
    }

    @EventListener
    public void onPlayerDeath(DeathEvent event) {
        if (KillEffect.nullCheck()) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        if (player == null) {
            return;
        }
        int i = 0;
        while ((double)i < this.factor.getValue()) {
            this.doEffect(player);
            ++i;
        }
    }

    private void doEffect(PlayerEntity player) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        if (this.lightning.getValue()) {
            LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, (World)KillEffect.mc.world);
            lightningEntity.updatePosition(x, y, z);
            lightningEntity.refreshPositionAfterTeleport(x, y, z);
            KillEffect.mc.world.addEntity((Entity)lightningEntity);
        }
        if (this.levelUp.getValue()) {
            KillEffect.mc.world.playSound((PlayerEntity)KillEffect.mc.player, x, y, z, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 100.0f, MathUtil.random(this.lMinPitch.getValueFloat(), this.lMaxPitch.getValueFloat()));
        }
        if (this.trident.getValue()) {
            KillEffect.mc.world.playSound((PlayerEntity)KillEffect.mc.player, x, y, z, SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.MASTER, 999.0f, MathUtil.random(this.tMinPitch.getValueFloat(), this.tMaxPitch.getValueFloat()));
        }
    }
}

