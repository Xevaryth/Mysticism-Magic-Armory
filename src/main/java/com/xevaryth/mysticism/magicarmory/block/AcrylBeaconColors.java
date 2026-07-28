package com.xevaryth.mysticism.magicarmory.block;

import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;

final class AcrylBeaconColors {
    private static final int NATURAL_ACRYL = 0xFFE8A7DD;
    private static final float DARKEN = 0.82F;

    private AcrylBeaconColors() {}

    static int beamColor(DyeColor color) {
        int source = color == null
            ? NATURAL_ACRYL
            : color.getTextureDiffuseColor();
        return FastColor.ARGB32.color(
            255,
            darken(FastColor.ARGB32.red(source)),
            darken(FastColor.ARGB32.green(source)),
            darken(FastColor.ARGB32.blue(source))
        );
    }

    private static int darken(int channel) {
        return Math.max(0, Math.min(255, Math.round(channel * DARKEN)));
    }
}
