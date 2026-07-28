package com.xevaryth.mysticism.magicarmory.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class AcrylGlassStairBlock extends StairBlock implements BeaconBeamBlock {
    private final DyeColor color;

    public AcrylGlassStairBlock(BlockState baseState, DyeColor color,
                                BlockBehaviour.Properties properties) {
        super(baseState, properties);
        this.color = color;
    }

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
}
