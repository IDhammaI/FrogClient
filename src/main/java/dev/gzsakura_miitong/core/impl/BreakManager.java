/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 */
package dev.gzsakura_miitong.core.impl;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.BlockBreakingProgressEvent;
import dev.gzsakura_miitong.api.events.impl.ClientTickEvent;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.ServerConnectBeginEvent;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.math.FadeUtils;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import dev.gzsakura_miitong.mod.modules.impl.player.PacketMine;
import dev.gzsakura_miitong.mod.modules.impl.render.BreakESP;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class BreakManager
implements Wrapper {
    public final ConcurrentHashMap<Integer, BreakData> breakMap = new ConcurrentHashMap();
    public final ConcurrentHashMap<Integer, BreakData> doubleMap = new ConcurrentHashMap();

    public BreakManager() {
        Alien.EVENT_BUS.subscribe(this);
    }

    @EventListener
    public void onServerConnectBegin(ServerConnectBeginEvent event) {
        this.breakMap.clear();
        this.doubleMap.clear();
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (Module.nullCheck()) {
            return;
        }
        if (AntiCheat.INSTANCE.detectDouble.getValue()) {
            Iterator<Object> iterator = ((ConcurrentHashMap.KeySetView)Alien.BREAK.doubleMap.keySet()).iterator();
            while (iterator.hasNext()) {
                int i = (Integer)iterator.next();
                BreakData breakData = Alien.BREAK.doubleMap.get(i);
                if (breakData != null && breakData.getEntity() != null && !BreakManager.mc.world.isAir(breakData.pos) && !breakData.timer.passedMs(Math.max(AntiCheat.INSTANCE.minTimeout.getValue() * 1000.0, breakData.breakTime * AntiCheat.INSTANCE.doubleMineTimeout.getValue()))) continue;
                Alien.BREAK.doubleMap.remove(i);
            }
        }
        for (BreakData breakData : this.breakMap.values()) {
            breakData.breakTime = Math.max(BreakESP.getBreakTime(breakData.pos, false), 50.0);
            if (PacketMine.unbreakable(breakData.pos)) {
                breakData.fade.setLength(0L);
                breakData.complete = false;
                breakData.failed = true;
                continue;
            }
            if (BreakManager.mc.world.isAir(breakData.pos)) {
                breakData.fade.setLength(0L);
                breakData.complete = true;
                breakData.failed = false;
                continue;
            }
            if (!breakData.complete && breakData.timer.passedMs(breakData.breakTime * AntiCheat.INSTANCE.breakTimeout.getValue())) {
                breakData.fade.setLength(0L);
                breakData.failed = true;
                continue;
            }
            breakData.fade.setLength((long)breakData.breakTime);
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Receive event) {
        if (Module.nullCheck()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof BlockBreakingProgressS2CPacket) {
            BreakData current;
            BlockBreakingProgressS2CPacket packet2 = (BlockBreakingProgressS2CPacket)packet;
            if (packet2.getPos() == null) {
                return;
            }
            BreakData breakData = new BreakData(packet2.getPos(), packet2.getEntityId(), false);
            if (breakData.getEntity() == null) {
                return;
            }
            if (MathHelper.sqrt((float)((float)breakData.getEntity().getEyePos().squaredDistanceTo(packet2.getPos().toCenterPos()))) > 8.0f) {
                return;
            }
            if (AntiCheat.INSTANCE.detectDouble.getValue() && packet2.getProgress() != 255) {
                if (packet2.getProgress() != 0) {
                    BreakData doublePos = this.doubleMap.get(packet2.getEntityId());
                    if (doublePos != null) {
                        doublePos.pos = packet2.getPos();
                        doublePos.timer.reset();
                    } else if (!PacketMine.unbreakable(packet2.getPos())) {
                        this.doubleMap.put(packet2.getEntityId(), new BreakData(packet2.getPos(), packet2.getEntityId(), true));
                    }
                    return;
                }
                BreakData doublePos = this.doubleMap.get(packet2.getEntityId());
                if (doublePos != null && doublePos.pos.equals((Object)packet2.getPos()) && !doublePos.timer.passedS(150.0)) {
                    return;
                }
            }
            if ((current = this.breakMap.get(packet2.getEntityId())) != null && !current.failed && current.pos.equals((Object)packet2.getPos())) {
                return;
            }
            this.breakMap.put(packet2.getEntityId(), breakData);
            Alien.EVENT_BUS.post(BlockBreakingProgressEvent.get(packet2.getPos(), packet2.getEntityId(), packet2.getProgress()));
            if (AntiCheat.INSTANCE.detectDouble.getValue() && !this.doubleMap.containsKey(packet2.getEntityId()) && !PacketMine.unbreakable(packet2.getPos())) {
                this.doubleMap.put(packet2.getEntityId(), new BreakData(packet2.getPos(), packet2.getEntityId(), true));
            }
        }
    }

    public boolean isMining(BlockPos pos) {
        return this.isMining(pos, true);
    }

    public boolean isMining(BlockPos pos, boolean self) {
        if (self && PacketMine.getBreakPos() != null && PacketMine.getBreakPos().equals((Object)pos)) {
            return true;
        }
        for (BreakData breakData : this.breakMap.values()) {
            if (breakData.getEntity() == null || breakData.getEntity().getEyePos().distanceTo(pos.toCenterPos()) > 7.0 || breakData.failed || !breakData.pos.equals((Object)pos)) continue;
            return true;
        }
        return false;
    }

    public static class BreakData {
        public BlockPos pos;
        private final int entityId;
        public final FadeUtils fade;
        public final Timer timer;
        public double breakTime;
        public boolean failed = false;
        public boolean complete = false;

        public BreakData(BlockPos pos, int entityId, boolean extraBreak) {
            this.pos = pos;
            this.entityId = entityId;
            this.breakTime = Math.max(BreakESP.getBreakTime(pos, extraBreak), 50.0);
            this.fade = new FadeUtils((long)this.breakTime);
            this.timer = new Timer();
        }

        public Entity getEntity() {
            if (Wrapper.mc.world == null) {
                return null;
            }
            Entity entity = Wrapper.mc.world.getEntityById(this.entityId);
            if (entity instanceof PlayerEntity) {
                return entity;
            }
            return null;
        }
    }
}

