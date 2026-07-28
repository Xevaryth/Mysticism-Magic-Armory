package com.xevaryth.mysticism.magicarmory.block;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class AcrylGlassBlock extends TransparentBlock implements BeaconBeamBlock {
    private final DyeColor color;

    public AcrylGlassBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
    }

    public DyeColor color() { return color; }
    @Override public DyeColor getColor() { return color == null ? DyeColor.PINK : color; }

    @Override
    public Integer getBeaconColorMultiplier(
        BlockState state,
        LevelReader level,
        BlockPos pos,
        BlockPos beaconPos
    ) {
        return AcrylBeaconColors.beamColor(color);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (adjacentState.getBlock() instanceof AcrylGlassBlock adjacent
            && Objects.equals(color, adjacent.color)) {
            return true;
        }
        return super.skipRendering(state, adjacentState, direction);
    }
}
