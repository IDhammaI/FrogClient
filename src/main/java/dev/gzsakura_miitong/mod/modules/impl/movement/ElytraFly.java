/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.MovementType
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.FireworkRocketEntity
 *  net.minecraft.item.ElytraItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package dev.gzsakura_miitong.mod.modules.impl.movement;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.ClientTickEvent;
import dev.gzsakura_miitong.api.events.impl.MoveEvent;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.TravelEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateRotateEvent;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.player.EntityUtil;
import dev.gzsakura_miitong.api.utils.player.InventoryUtil;
import dev.gzsakura_miitong.api.utils.player.MovementUtil;
import dev.gzsakura_miitong.asm.accessors.IFireworkRocketEntity;
import dev.gzsakura_miitong.asm.accessors.ILivingEntity;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import dev.gzsakura_miitong.mod.modules.settings.impl.BindSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ElytraFly
extends Module {
    public static ElytraFly INSTANCE;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Control));
    public final BooleanSetting infiniteDura = this.add(new BooleanSetting("InfiniteDura", false));
    public final BooleanSetting packet = this.add(new BooleanSetting("Packet", false).setParent());
    private final SliderSetting packetDelay = this.add(new SliderSetting("PacketDelay", 0.0, 0.0, 20.0, 1.0, this.packet::isOpen));
    private final BooleanSetting setFlag = this.add(new BooleanSetting("SetFlag", false, () -> !this.mode.is(Mode.Bounce)));
    private final BooleanSetting firework = this.add(new BooleanSetting("Firework", false).setParent());
    public final BindSetting fireWork = this.add(new BindSetting("FireWorkBind", -1, this.firework::isOpen));
    public final BooleanSetting packetInteract = this.add(new BooleanSetting("PacketInteract", true, this.firework::isOpen));
    public final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true, this.firework::isOpen));
    public final BooleanSetting onlyOne = this.add(new BooleanSetting("OnlyOne", true, this.firework::isOpen));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true, this.firework::isOpen));
    public final BooleanSetting autoJump = this.add(new BooleanSetting("AutoJump", true, () -> this.mode.is(Mode.Bounce)));
    public final SliderSetting upPitch = this.add(new SliderSetting("UpPitch", 0.0, 0.0, 90.0, () -> this.mode.getValue() == Mode.Control));
    public final SliderSetting upFactor = this.add(new SliderSetting("UpFactor", 1.0, 0.0, 10.0, () -> this.mode.getValue() == Mode.Control));
    public final SliderSetting downFactor = this.add(new SliderSetting("FallSpeed", 1.0, 0.0, 10.0, () -> this.mode.getValue() == Mode.Control));
    public final SliderSetting speed = this.add(new SliderSetting("Speed", 1.0, (double)0.1f, 10.0, () -> this.mode.getValue() == Mode.Control));
    public final BooleanSetting speedLimit = this.add(new BooleanSetting("SpeedLimit", true, () -> this.mode.getValue() == Mode.Control));
    public final SliderSetting maxSpeed = this.add(new SliderSetting("MaxSpeed", 2.5, (double)0.1f, 10.0, () -> this.speedLimit.getValue() && this.mode.getValue() == Mode.Control));
    public final BooleanSetting noDrag = this.add(new BooleanSetting("NoDrag", false, () -> this.mode.getValue() == Mode.Control));
    public final Timer fireworkTimer = new Timer();
    private final BooleanSetting autoStop = this.add(new BooleanSetting("AutoStop", true));
    private final BooleanSetting sprint = this.add(new BooleanSetting("Sprint", true, () -> this.mode.is(Mode.Bounce)));
    private final SliderSetting pitch = this.add(new SliderSetting("Pitch", 88.0, -90.0, 90.0, 0.1, () -> this.mode.is(Mode.Bounce)));
    private final BooleanSetting instantFly = this.add(new BooleanSetting("AutoStart", true, () -> !this.mode.is(Mode.Bounce)));
    private final BooleanSetting checkSpeed = this.add(new BooleanSetting("CheckSpeed", false, () -> !this.mode.is(Mode.Bounce)));
    public final SliderSetting minSpeed = this.add(new SliderSetting("MinSpeed", 70.0, 0.1, 200.0, () -> !this.mode.is(Mode.Bounce)));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 1000.0, 0.0, 20000.0, 50.0, () -> !this.mode.is(Mode.Bounce)));
    private final SliderSetting timeout = this.add(new SliderSetting("Timeout", 0.0, 0.1, 1.0, 0.1, () -> !this.mode.is(Mode.Bounce)));
    private final SliderSetting sneakDownSpeed = this.add(new SliderSetting("DownSpeed", 1.0, 0.1, 10.0, () -> this.mode.getValue() == Mode.Control));
    private final SliderSetting boost = this.add(new SliderSetting("Boost", 1.0, 0.1, 4.0, () -> this.mode.getValue() == Mode.Boost));
    private final BooleanSetting freeze = this.add(new BooleanSetting("Freeze", false, () -> this.mode.is(Mode.Rotation)));
    private final BooleanSetting motionStop = this.add(new BooleanSetting("MotionStop", false, () -> this.mode.is(Mode.Rotation)));
    private final SliderSetting infiniteMaxSpeed = this.add(new SliderSetting("InfiniteMaxSpeed", 150.0, 50.0, 170.0, () -> this.mode.getValue() == Mode.Pitch));
    private final SliderSetting infiniteMinSpeed = this.add(new SliderSetting("InfiniteMinSpeed", 25.0, 10.0, 70.0, () -> this.mode.getValue() == Mode.Pitch));
    private final SliderSetting infiniteMaxHeight = this.add(new SliderSetting("InfiniteMaxHeight", 200, -50, 360, () -> this.mode.getValue() == Mode.Pitch));
    public final BooleanSetting releaseSneak = this.add(new BooleanSetting("ReleaseSneak", false));
    private final Timer instantFlyTimer = new Timer();
    boolean prev;
    float prePitch;
    private boolean hasElytra = false;
    float yaw = 0.0f;
    float rotationPitch = 0.0f;
    boolean flying = false;
    int packetDelayInt = 0;
    private boolean down;
    private float lastInfinitePitch;
    private float infinitePitch;

    public ElytraFly() {
        super("ElytraFly", Module.Category.Movement);
        this.setChinese("\u9798\u7fc5\u98de\u884c");
        INSTANCE = this;
        Alien.EVENT_BUS.subscribe(new FireWorkTweak());
    }

    public void off() {
        int firework;
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (this.onlyOne.getValue()) {
            for (Entity entity : Alien.THREAD.getEntities()) {
                FireworkRocketEntity fireworkRocketEntity;
                if (!(entity instanceof FireworkRocketEntity) || ((IFireworkRocketEntity)(fireworkRocketEntity = (FireworkRocketEntity)entity)).getShooter() != ElytraFly.mc.player) continue;
                return;
            }
        }
        ElytraFly.INSTANCE.fireworkTimer.reset();
        if (ElytraFly.mc.player.getMainHandStack().getItem() == Items.FIREWORK_ROCKET) {
            if (this.packetInteract.getValue()) {
                ElytraFly.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Alien.ROTATION.getLastYaw(), Alien.ROTATION.getLastPitch()));
            } else {
                ElytraFly.mc.interactionManager.interactItem((PlayerEntity)ElytraFly.mc.player, Hand.MAIN_HAND);
            }
        } else if (this.inventory.getValue() && (firework = InventoryUtil.findItemInventorySlot(Items.FIREWORK_ROCKET)) != -1) {
            InventoryUtil.inventorySwap(firework, ElytraFly.mc.player.getInventory().selectedSlot);
            if (this.packetInteract.getValue()) {
                ElytraFly.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Alien.ROTATION.getLastYaw(), Alien.ROTATION.getLastPitch()));
            } else {
                ElytraFly.mc.interactionManager.interactItem((PlayerEntity)ElytraFly.mc.player, Hand.MAIN_HAND);
            }
            InventoryUtil.inventorySwap(firework, ElytraFly.mc.player.getInventory().selectedSlot);
            EntityUtil.syncInventory();
        } else {
            firework = InventoryUtil.findItem(Items.FIREWORK_ROCKET);
            if (firework != -1) {
                int old = ElytraFly.mc.player.getInventory().selectedSlot;
                InventoryUtil.switchToSlot(firework);
                if (this.packetInteract.getValue()) {
                    ElytraFly.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Alien.ROTATION.getLastYaw(), Alien.ROTATION.getLastPitch()));
                } else {
                    ElytraFly.mc.interactionManager.interactItem((PlayerEntity)ElytraFly.mc.player, Hand.MAIN_HAND);
                }
                InventoryUtil.switchToSlot(old);
            }
        }
    }

    public static boolean recastElytra(ClientPlayerEntity player) {
        if (ElytraFly.checkConditions(player) && ElytraFly.ignoreGround(player)) {
            player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            if (ElytraFly.INSTANCE.setFlag.getValue()) {
                ElytraFly.mc.player.startFallFlying();
            }
            return true;
        }
        return false;
    }

    public static boolean checkConditions(ClientPlayerEntity player) {
        ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
        return !player.getAbilities().flying && !player.hasVehicle() && !player.isClimbing() && itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable((ItemStack)itemStack);
    }

    private static boolean ignoreGround(ClientPlayerEntity player) {
        if (!player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable((ItemStack)itemStack)) {
                player.startFallFlying();
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @Override
    public void onEnable() {
        if (ElytraFly.nullCheck()) {
            return;
        }
        this.hasElytra = false;
        this.yaw = ElytraFly.mc.player.getYaw();
        this.rotationPitch = ElytraFly.mc.player.getPitch();
    }

    @Override
    public void onDisable() {
        if (ElytraFly.nullCheck()) {
            return;
        }
        if (this.releaseSneak.getValue()) {
            mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraFly.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
    }

    private void boost() {
        if (this.hasElytra) {
            if (!this.isFallFlying()) {
                return;
            }
            float yaw = (float)Math.toRadians(ElytraFly.mc.player.getYaw());
            if (ElytraFly.mc.options.forwardKey.isPressed()) {
                ElytraFly.mc.player.addVelocity((double)(-MathHelper.sin((float)yaw) * this.boost.getValueFloat() / 10.0f), 0.0, (double)(MathHelper.cos((float)yaw) * this.boost.getValueFloat() / 10.0f));
            }
        }
    }

    @EventListener(priority=-9999)
    public void onRotation(UpdateRotateEvent event) {
        if (ElytraFly.nullCheck()) {
            return;
        }
        if (this.mode.is(Mode.Rotation)) {
            if (this.isFallFlying()) {
                if (MovementUtil.isMoving()) {
                    if (ElytraFly.mc.options.jumpKey.isPressed()) {
                        this.rotationPitch = -45.0f;
                    } else if (ElytraFly.mc.options.sneakKey.isPressed()) {
                        this.rotationPitch = 45.0f;
                    } else {
                        this.rotationPitch = -1.9f;
                        if (this.motionStop.getValue()) {
                            this.setY(0.0);
                        }
                    }
                } else if (ElytraFly.mc.options.jumpKey.isPressed()) {
                    this.rotationPitch = -89.0f;
                } else if (ElytraFly.mc.options.sneakKey.isPressed()) {
                    this.rotationPitch = 89.0f;
                } else if (this.motionStop.getValue()) {
                    this.setY(0.0);
                }
                if (ElytraFly.mc.options.forwardKey.isPressed() || ElytraFly.mc.options.backKey.isPressed() || ElytraFly.mc.options.leftKey.isPressed() || ElytraFly.mc.options.rightKey.isPressed()) {
                    this.yaw = Sprint.getSprintYaw(ElytraFly.mc.player.getYaw());
                } else if (this.motionStop.getValue()) {
                    this.setX(0.0);
                    this.setZ(0.0);
                }
                event.setYaw(this.yaw);
                event.setPitch(this.rotationPitch);
            }
        } else if (this.mode.is(Mode.Pitch)) {
            if (this.isFallFlying()) {
                event.setPitch(this.infinitePitch);
            }
        } else if (this.mode.is(Mode.Bounce) && this.isFallFlying()) {
            event.setPitch(this.pitch.getValueFloat());
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.getInfinitePitch();
        this.flying = false;
        if (this.packet.getValue()) {
            this.hasElytra = InventoryUtil.findItemInventorySlot(Items.ELYTRA) != -1;
        } else {
            this.hasElytra = false;
            for (ItemStack is : ElytraFly.mc.player.getArmorItems()) {
                if (!(is.getItem() instanceof ElytraItem)) continue;
                this.hasElytra = true;
                break;
            }
            if (this.infiniteDura.getValue() && !ElytraFly.mc.player.isOnGround() && this.hasElytra) {
                this.flying = true;
                ElytraFly.mc.interactionManager.clickSlot(ElytraFly.mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, (PlayerEntity)ElytraFly.mc.player);
                ElytraFly.mc.interactionManager.clickSlot(ElytraFly.mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, (PlayerEntity)ElytraFly.mc.player);
                mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraFly.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                if (this.setFlag.getValue()) {
                    ElytraFly.mc.player.startFallFlying();
                }
            }
            if (this.mode.is(Mode.Bounce)) {
                ((ILivingEntity)ElytraFly.mc.player).setLastJumpCooldown(0);
                return;
            }
        }
        double x = ElytraFly.mc.player.getX() - ElytraFly.mc.player.prevX;
        double y = ElytraFly.mc.player.getY() - ElytraFly.mc.player.prevY;
        double z = ElytraFly.mc.player.getZ() - ElytraFly.mc.player.prevZ;
        double dist = Math.sqrt(x * x + z * z + y * y) / 1000.0;
        double div = 1.388888888888889E-5;
        float timer = Alien.TIMER.get();
        double speed = dist / div * (double)timer;
        if (this.mode.getValue() == Mode.Boost) {
            this.boost();
        }
        if (this.packet.getValue()) {
            if (ElytraFly.mc.player.isOnGround()) {
                return;
            }
            ++this.packetDelayInt;
            if ((double)this.packetDelayInt <= this.packetDelay.getValue()) {
                return;
            }
            int elytra = InventoryUtil.findItem(Items.ELYTRA);
            if (elytra != -1) {
                ElytraFly.mc.interactionManager.clickSlot(ElytraFly.mc.player.currentScreenHandler.syncId, 6, elytra, SlotActionType.SWAP, (PlayerEntity)ElytraFly.mc.player);
                mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraFly.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                ElytraFly.mc.player.startFallFlying();
                if ((!this.checkSpeed.getValue() || speed <= this.minSpeed.getValue()) && this.firework.getValue() && this.fireworkTimer.passed(this.delay.getValueInt()) && (MovementUtil.isMoving() || this.mode.is(Mode.Rotation) && ElytraFly.mc.options.jumpKey.isPressed()) && (!ElytraFly.mc.player.isUsingItem() || !this.usingPause.getValue()) && this.isFallFlying()) {
                    this.off();
                    this.fireworkTimer.reset();
                }
                ElytraFly.mc.interactionManager.clickSlot(ElytraFly.mc.player.currentScreenHandler.syncId, 6, elytra, SlotActionType.SWAP, (PlayerEntity)ElytraFly.mc.player);
                this.packetDelayInt = 0;
            } else {
                elytra = InventoryUtil.findItemInventorySlot(Items.ELYTRA);
                if (elytra != -1) {
                    ElytraFly.mc.interactionManager.clickSlot(ElytraFly.mc.player.currentScreenHandler.syncId, elytra, 0, SlotActionType.PICKUP, (PlayerEntity)ElytraFly.mc.player);
                    ElytraFly.mc.interactionManager.clickSlot(ElytraFly.mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, (PlayerEntity)ElytraFly.mc.player);
                    mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraFly.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    ElytraFly.mc.player.startFallFlying();
                    if ((!this.checkSpeed.getValue() || speed <= this.minSpeed.getValue()) && this.firework.getValue() && this.fireworkTimer.passed(this.delay.getValueInt()) && (MovementUtil.isMoving() || this.mode.is(Mode.Rotation) && ElytraFly.mc.options.jumpKey.isPressed()) && (!ElytraFly.mc.player.isUsingItem() || !this.usingPause.getValue()) && this.isFallFlying()) {
                        this.off();
                        this.fireworkTimer.reset();
                    }
                    ElytraFly.mc.interactionManager.clickSlot(ElytraFly.mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, (PlayerEntity)ElytraFly.mc.player);
                    ElytraFly.mc.interactionManager.clickSlot(ElytraFly.mc.player.currentScreenHandler.syncId, elytra, 0, SlotActionType.PICKUP, (PlayerEntity)ElytraFly.mc.player);
                    this.packetDelayInt = 0;
                }
            }
            return;
        }
        if ((!this.checkSpeed.getValue() || speed <= this.minSpeed.getValue()) && this.firework.getValue() && this.fireworkTimer.passed(this.delay.getValueInt()) && (MovementUtil.isMoving() || this.mode.is(Mode.Rotation) && ElytraFly.mc.options.jumpKey.isPressed()) && (!ElytraFly.mc.player.isUsingItem() || !this.usingPause.getValue()) && this.isFallFlying()) {
            this.off();
            this.fireworkTimer.reset();
        }
        if (!this.isFallFlying() && this.hasElytra) {
            this.fireworkTimer.setMs(99999999L);
            if (!ElytraFly.mc.player.isOnGround() && this.instantFly.getValue() && ElytraFly.mc.player.getVelocity().getY() < 0.0 && !this.infiniteDura.getValue()) {
                if (!this.instantFlyTimer.passed((long)(1000.0 * this.timeout.getValue()))) {
                    return;
                }
                this.instantFlyTimer.reset();
                mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraFly.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                if (this.setFlag.getValue()) {
                    ElytraFly.mc.player.startFallFlying();
                }
            }
        }
    }

    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180);
        float g = -yaw * ((float)Math.PI / 180);
        float h = MathHelper.cos((float)g);
        float i = MathHelper.sin((float)g);
        float j = MathHelper.cos((float)f);
        float k = MathHelper.sin((float)f);
        return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
    }

    public Vec3d getRotationVec(float tickDelta) {
        return this.getRotationVector(-this.upPitch.getValueFloat(), ElytraFly.mc.player.getYaw(tickDelta));
    }

    @EventListener
    private void onPlayerMove(MoveEvent event) {
        if (this.autoStop.getValue() && this.isFallFlying()) {
            int chunkX = (int)(ElytraFly.mc.player.getX() / 16.0);
            int chunkZ = (int)(ElytraFly.mc.player.getZ() / 16.0);
            if (!ElytraFly.mc.world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
                event.cancel();
            }
        }
    }

    @EventListener
    private void onTick(ClientTickEvent event) {
        if (ElytraFly.nullCheck()) {
            return;
        }
        if (this.mode.is(Mode.Bounce) && this.hasElytra) {
            if (this.autoJump.getValue()) {
                ElytraFly.mc.options.jumpKey.setPressed(true);
            }
            if (event.isPost()) {
                if (!this.isFallFlying()) {
                    mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraFly.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
                if (ElytraFly.checkConditions(ElytraFly.mc.player) && !this.sprint.getValue()) {
                    if (this.isFallFlying()) {
                        ElytraFly.mc.player.setSprinting(ElytraFly.mc.player.isOnGround());
                    } else {
                        ElytraFly.mc.player.setSprinting(true);
                    }
                }
            } else if (ElytraFly.checkConditions(ElytraFly.mc.player) && this.sprint.getValue()) {
                ElytraFly.mc.player.setSprinting(true);
            }
        }
    }

    @EventListener
    private void onPacketSend(PacketEvent.Send event) {
        if (ElytraFly.nullCheck()) {
            return;
        }
        if (this.mode.is(Mode.Bounce) && this.hasElytra && event.getPacket() instanceof ClientCommandC2SPacket && ((ClientCommandC2SPacket)event.getPacket()).getMode().equals((Object)ClientCommandC2SPacket.Mode.START_FALL_FLYING) && !this.sprint.getValue()) {
            ElytraFly.mc.player.setSprinting(true);
        }
    }

    @EventListener
    private void onPacketReceive(PacketEvent.Receive event) {
        if (ElytraFly.nullCheck()) {
            return;
        }
        if (this.mode.is(Mode.Bounce) && this.hasElytra && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            ElytraFly.mc.player.stopFallFlying();
        }
    }

    @EventListener
    public void travel(TravelEvent event) {
        if (ElytraFly.nullCheck()) {
            return;
        }
        if (!AntiCheat.INSTANCE.movementSync()) {
            if (this.mode.is(Mode.Bounce) && this.hasElytra) {
                if (event.isPre()) {
                    this.prev = true;
                    this.prePitch = ElytraFly.mc.player.getPitch();
                    ElytraFly.mc.player.setPitch(this.pitch.getValueFloat());
                } else if (this.prev) {
                    this.prev = false;
                    ElytraFly.mc.player.setPitch(this.prePitch);
                }
            } else if (this.mode.is(Mode.Pitch) && this.isFallFlying()) {
                if (event.isPre()) {
                    this.prev = true;
                    this.prePitch = ElytraFly.mc.player.getPitch();
                    ElytraFly.mc.player.setPitch(this.lastInfinitePitch);
                } else if (this.prev) {
                    this.prev = false;
                    ElytraFly.mc.player.setPitch(this.prePitch);
                }
            }
        }
    }

    @EventListener
    public void onMove(TravelEvent event) {
        if (ElytraFly.nullCheck() || !this.hasElytra || !this.isFallFlying() || event.isPost()) {
            return;
        }
        if ((this.mode.is(Mode.Freeze) || this.mode.is(Mode.Rotation) && this.freeze.getValue()) && !MovementUtil.isMoving() && !ElytraFly.mc.options.jumpKey.isPressed() && !ElytraFly.mc.options.sneakKey.isPressed()) {
            event.cancel();
            return;
        }
        if (this.mode.getValue() == Mode.Control) {
            if (this.firework.getValue()) {
                if (!ElytraFly.mc.options.sneakKey.isPressed() || !ElytraFly.mc.player.input.jumping) {
                    if (ElytraFly.mc.options.sneakKey.isPressed()) {
                        this.setY(-this.sneakDownSpeed.getValue());
                    } else if (ElytraFly.mc.player.input.jumping) {
                        this.setY(this.upFactor.getValue());
                    } else {
                        this.setY(-3.0E-11 * this.downFactor.getValue());
                    }
                } else {
                    this.setY(0.0);
                }
                double[] dir = MovementUtil.directionSpeed(this.speed.getValue());
                this.setX(dir[0]);
                this.setZ(dir[1]);
            } else {
                Vec3d lookVec = this.getRotationVec(mc.getRenderTickCounter().getTickDelta(true));
                double lookDist = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
                double motionDist = Math.sqrt(this.getX() * this.getX() + this.getZ() * this.getZ());
                if (ElytraFly.mc.player.input.sneaking) {
                    this.setY(-this.sneakDownSpeed.getValue());
                } else if (!ElytraFly.mc.player.input.jumping) {
                    this.setY(-3.0E-11 * this.downFactor.getValue());
                }
                if (ElytraFly.mc.player.input.jumping) {
                    if (motionDist > this.upFactor.getValue() / this.upFactor.getMax()) {
                        double rawUpSpeed = motionDist * 0.01325;
                        this.setY(this.getY() + rawUpSpeed * 3.2);
                        this.setX(this.getX() - lookVec.x * rawUpSpeed / lookDist);
                        this.setZ(this.getZ() - lookVec.z * rawUpSpeed / lookDist);
                    } else {
                        double[] dir = MovementUtil.directionSpeed(this.speed.getValue());
                        this.setX(dir[0]);
                        this.setZ(dir[1]);
                    }
                }
                if (lookDist > 0.0) {
                    this.setX(this.getX() + (lookVec.x / lookDist * motionDist - this.getX()) * 0.1);
                    this.setZ(this.getZ() + (lookVec.z / lookDist * motionDist - this.getZ()) * 0.1);
                }
                if (!ElytraFly.mc.player.input.jumping) {
                    double[] dir = MovementUtil.directionSpeed(this.speed.getValue());
                    this.setX(dir[0]);
                    this.setZ(dir[1]);
                }
                if (!this.noDrag.getValue()) {
                    this.setY(this.getY() * (double)0.99f);
                    this.setX(this.getX() * (double)0.98f);
                    this.setZ(this.getZ() * (double)0.99f);
                }
                double finalDist = Math.sqrt(this.getX() * this.getX() + this.getZ() * this.getZ());
                if (this.speedLimit.getValue() && finalDist > this.maxSpeed.getValue()) {
                    this.setX(this.getX() * this.maxSpeed.getValue() / finalDist);
                    this.setZ(this.getZ() * this.maxSpeed.getValue() / finalDist);
                }
                event.cancel();
                ElytraFly.mc.player.move(MovementType.SELF, ElytraFly.mc.player.getVelocity());
            }
        }
    }

    private double getX() {
        return MovementUtil.getMotionX();
    }

    private void setX(double f) {
        MovementUtil.setMotionX(f);
    }

    private double getY() {
        return MovementUtil.getMotionY();
    }

    private void setY(double f) {
        MovementUtil.setMotionY(f);
    }

    private double getZ() {
        return MovementUtil.getMotionZ();
    }

    private void setZ(double f) {
        MovementUtil.setMotionZ(f);
    }

    private void getInfinitePitch() {
        this.lastInfinitePitch = this.infinitePitch;
        double currentPlayerSpeed = Math.hypot(ElytraFly.mc.player.getX() - ElytraFly.mc.player.prevX, ElytraFly.mc.player.getZ() - ElytraFly.mc.player.prevZ);
        if (ElytraFly.mc.player.getY() < this.infiniteMaxHeight.getValue()) {
            if (currentPlayerSpeed * 72.0 < this.infiniteMinSpeed.getValue() && !this.down) {
                this.down = true;
            }
            if (currentPlayerSpeed * 72.0 > this.infiniteMaxSpeed.getValue() && this.down) {
                this.down = false;
            }
        } else {
            this.down = true;
        }
        this.infinitePitch = this.down ? (this.infinitePitch += 3.0f) : (this.infinitePitch -= 3.0f);
        this.infinitePitch = MathUtil.clamp(this.infinitePitch, -40.0f, 40.0f);
    }

    public boolean isFallFlying() {
        return ElytraFly.mc.player.isFallFlying() || this.packet.getValue() && this.hasElytra && !ElytraFly.mc.player.isOnGround() || this.flying;
    }

    public static enum Mode {
        Control,
        Boost,
        Bounce,
        Freeze,
        None,
        Rotation,
        Pitch;

    }

    public class FireWorkTweak {
        boolean press;

        @EventListener
        public void onTick(ClientTickEvent event) {
            if (Module.nullCheck() || event.isPost()) {
                return;
            }
            if (ElytraFly.this.inventory.getValue() && !EntityUtil.inInventory()) {
                return;
            }
            if (Wrapper.mc.currentScreen == null) {
                if (ElytraFly.this.fireWork.isPressed()) {
                    if (!(this.press || !ElytraFly.this.fireworkTimer.passed(ElytraFly.this.delay.getValueInt()) || Wrapper.mc.player.isUsingItem() && ElytraFly.this.usingPause.getValue() || !ElytraFly.this.isFallFlying())) {
                        ElytraFly.this.off();
                        ElytraFly.this.fireworkTimer.reset();
                    }
                    this.press = true;
                } else {
                    this.press = false;
                }
            } else {
                this.press = false;
            }
        }
    }
}

