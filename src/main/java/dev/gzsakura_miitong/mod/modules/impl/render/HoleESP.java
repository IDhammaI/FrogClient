/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render3DUtil;
import dev.gzsakura_miitong.api.utils.world.BlockUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class HoleESP
extends Module {
    public static HoleESP INSTANCE;
    public final SliderSetting startFade = this.add(new SliderSetting("StartFade", 5.0, 1.0, 20.0));
    public final SliderSetting getDistance = this.add(new SliderSetting("Distance", 6.0, 1.0, 20.0));
    public final SliderSetting airHeight = this.add(new SliderSetting("AirHeight", 1.0, -3.0, 3.0, 0.01));
    public final BooleanSetting airYCheck = this.add(new BooleanSetting("AirYCheck", true));
    public final SliderSetting height = this.add(new SliderSetting("Height", 1.0, -3.0, 3.0, 0.1));
    public final SliderSetting wallHeight = this.add(new SliderSetting("WallHeight", 3.0, -3.0, 3.0, 0.1));
    public final BooleanSetting sideCheck = this.add(new BooleanSetting("SideCheck", true));
    private final ColorSetting airFill = this.add(new ColorSetting("AirFill", new Color(148, 0, 0, 100)).injectBoolean(true));
    private final ColorSetting airBox = this.add(new ColorSetting("AirBox", new Color(148, 0, 0, 100)).injectBoolean(true));
    private final ColorSetting airFade = this.add(new ColorSetting("AirFade", new Color(148, 0, 0, 0)).injectBoolean(true));
    private final ColorSetting normalFill = this.add(new ColorSetting("UnsafeFill", new Color(255, 0, 0, 50)).injectBoolean(true));
    private final ColorSetting normalBox = this.add(new ColorSetting("UnsafeBox", new Color(255, 0, 0, 100)).injectBoolean(true));
    private final ColorSetting normalFade = this.add(new ColorSetting("UnsafeFade", new Color(255, 0, 0, 0)).injectBoolean(true));
    private final ColorSetting bedrockFill = this.add(new ColorSetting("SafeFill", new Color(8, 255, 79, 50)).injectBoolean(true));
    private final ColorSetting bedrockBox = this.add(new ColorSetting("SafeBox", new Color(8, 255, 79, 100)).injectBoolean(true));
    private final ColorSetting bedrockFade = this.add(new ColorSetting("SafeFade", new Color(8, 255, 79, 100)).injectBoolean(true));
    private final ColorSetting wallFill = this.add(new ColorSetting("WallFill", new Color(0, 255, 255, 128)).injectBoolean(true));
    private final ColorSetting wallBox = this.add(new ColorSetting("WallBox", new Color(0, 225, 255, 255)).injectBoolean(true));
    private final ColorSetting wallFade = this.add(new ColorSetting("WallFade", new Color(0, 255, 255, 64)).injectBoolean(true));
    private final ColorSetting wallSideFill = this.add(new ColorSetting("WallSideFill", new Color(0, 255, 255, 128)).injectBoolean(true));
    private final ColorSetting wallSideBox = this.add(new ColorSetting("WallSideBox", new Color(0, 225, 255, 255)).injectBoolean(true));
    private final ColorSetting wallSideFade = this.add(new ColorSetting("WallSideFade", new Color(0, 255, 255, 64)).injectBoolean(true));
    private final SliderSetting updateDelay = this.add(new SliderSetting("UpdateDelay", 50, 0, 1000));
    private final List<BlockPos> tempNormalList = new ArrayList<BlockPos>();
    private final List<BlockPos> tempBedrockList = new ArrayList<BlockPos>();
    private final List<BlockPos> tempAirList = new ArrayList<BlockPos>();
    private final List<BlockPos> tempWallList = new ArrayList<BlockPos>();
    private final List<BlockPos> tempWallSideList = new ArrayList<BlockPos>();
    private final Timer timer = new Timer();
    boolean drawing = false;
    private List<BlockPos> normalList = new ArrayList<BlockPos>();
    private List<BlockPos> bedrockList = new ArrayList<BlockPos>();
    private List<BlockPos> airList = new ArrayList<BlockPos>();
    private List<BlockPos> wallList = new ArrayList<BlockPos>();
    private List<BlockPos> wallSideList = new ArrayList<BlockPos>();

    public HoleESP() {
        super("HoleESP", Module.Category.Render);
        this.setChinese("\u5751\u900f\u89c6");
        INSTANCE = this;
    }

    public void onThread() {
        if (HoleESP.nullCheck() || this.isOff()) {
            return;
        }
        if (!this.drawing && this.timer.passedMs(this.updateDelay.getValue())) {
            this.normalList = new ArrayList<BlockPos>(this.tempNormalList);
            this.bedrockList = new ArrayList<BlockPos>(this.tempBedrockList);
            this.airList = new ArrayList<BlockPos>(this.tempAirList);
            this.wallList = new ArrayList<BlockPos>(this.tempWallList);
            this.wallSideList = new ArrayList<BlockPos>(this.tempWallSideList);
            this.timer.reset();
            this.tempBedrockList.clear();
            this.tempNormalList.clear();
            this.tempAirList.clear();
            this.tempWallList.clear();
            this.tempWallSideList.clear();
            for (BlockPos pos : BlockUtil.getSphere(this.getDistance.getValueFloat(), HoleESP.mc.player.getPos())) {
                Type type;
                if (this.isBedrock(pos) && this.isBedrock(pos.up(2)) && this.isBedrock(pos.down())) {
                    Direction side = this.getWallSide(pos);
                    if (side != null || !this.sideCheck.getValue()) {
                        this.tempWallList.add(pos);
                    }
                    if (side != null) {
                        this.tempWallSideList.add(pos.offset(side));
                    }
                }
                if ((type = this.isHole(pos)) == Type.Bedrock) {
                    this.tempBedrockList.add(pos);
                    continue;
                }
                if (type == Type.Normal) {
                    this.tempNormalList.add(pos);
                    continue;
                }
                if (type != Type.Air) continue;
                this.tempAirList.add(pos);
            }
        }
    }

    private Direction getWallSide(BlockPos pos) {
        double getDistance = Double.MAX_VALUE;
        Direction side = null;
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos;
            if (direction == Direction.UP || direction == Direction.DOWN || !BlockUtil.canCollide(new Box((offsetPos = pos.offset(direction)).down())) || BlockUtil.canCollide(new Box(offsetPos)) || BlockUtil.canCollide(new Box(offsetPos.up()))) continue;
            if (side == null) {
                side = direction;
                getDistance = HoleESP.mc.player.getEyePos().distanceTo(offsetPos.toCenterPos());
                continue;
            }
            if (!(HoleESP.mc.player.getEyePos().distanceTo(offsetPos.toCenterPos()) < getDistance)) continue;
            side = direction;
            getDistance = HoleESP.mc.player.getEyePos().distanceTo(offsetPos.toCenterPos());
        }
        return side;
    }

    private boolean isBedrock(BlockPos pos) {
        return HoleESP.mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
    }

    Type isHole(BlockPos pos) {
        if (HoleESP.mc.world.isAir(pos) && (!this.airYCheck.getValue() || pos.getY() == HoleESP.mc.player.getBlockY() - 1 || pos.getY() == HoleESP.mc.player.getBlockY()) && Alien.HOLE.isHard(pos.up())) {
            return Type.Air;
        }
        int blockProgress = 0;
        boolean bedRock = true;
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || i == Direction.DOWN || !Alien.HOLE.isHard(pos.offset(i))) continue;
            if (HoleESP.mc.world.getBlockState(pos.offset(i)).getBlock() != Blocks.BEDROCK) {
                bedRock = false;
            }
            ++blockProgress;
        }
        if (HoleESP.mc.world.isAir(pos) && HoleESP.mc.world.isAir(pos.up()) && HoleESP.mc.world.isAir(pos.up(2)) && blockProgress > 3 && BlockUtil.canCollide((Entity)HoleESP.mc.player, new Box(pos.down()))) {
            if (bedRock) {
                return Type.Bedrock;
            }
            return Type.Normal;
        }
        if (Alien.HOLE.isDoubleHole(pos)) {
            return Type.Normal;
        }
        return Type.None;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        this.drawing = true;
        this.draw(matrixStack, this.bedrockList, this.bedrockFill, this.bedrockFade, this.bedrockBox, this.height.getValue());
        this.draw(matrixStack, this.airList, this.airFill, this.airFade, this.airBox, this.airHeight.getValue());
        this.draw(matrixStack, this.normalList, this.normalFill, this.normalFade, this.normalBox, this.height.getValue());
        this.draw(matrixStack, this.wallList, this.wallFill, this.wallFade, this.wallBox, this.wallHeight.getValue());
        this.draw(matrixStack, this.wallSideList, this.wallSideFill, this.wallSideFade, this.wallSideBox, this.height.getValue());
        this.drawing = false;
    }

    private void draw(MatrixStack matrixStack, List<BlockPos> list, ColorSetting fill, ColorSetting fade, ColorSetting box, double height) {
        for (BlockPos pos : list) {
            double getDistance = HoleESP.mc.player.getPos().distanceTo(pos.toCenterPos());
            double alpha = getDistance > this.startFade.getValue() ? Math.max(Math.min(1.0, 1.0 - (getDistance - this.startFade.getValue()) / (this.getDistance.getValue() - this.startFade.getValue())), 0.0) : 1.0;
            Box espBox = new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)pos.getY() + height, (double)(pos.getZ() + 1));
            if (fill.booleanValue) {
                if (fade.booleanValue) {
                    Render3DUtil.drawFadeFill(matrixStack, espBox, ColorUtil.injectAlpha(fill.getValue(), (int)((double)fill.getValue().getAlpha() * alpha)), ColorUtil.injectAlpha(fade.getValue(), (int)((double)fade.getValue().getAlpha() * alpha)));
                } else {
                    Render3DUtil.drawFill(matrixStack, espBox, ColorUtil.injectAlpha(fill.getValue(), (int)((double)fill.getValue().getAlpha() * alpha)));
                }
            }
            if (!box.booleanValue) continue;
            Render3DUtil.drawBox(matrixStack, espBox, ColorUtil.injectAlpha(box.getValue(), (int)((double)box.getValue().getAlpha() * alpha)));
        }
    }

    public static enum Type {
        None,
        Air,
        Normal,
        Bedrock;

    }
}

