package com.xevaryth.mysticism.magicarmory;

import com.xevaryth.mysticism.api.ManaChangeReason;
import com.xevaryth.mysticism.api.ManaContext;
import net.minecraft.resources.ResourceLocation;

public final class ArmoryManaContexts {
    public static final ManaContext STAFF_CAST = context("staff_cast", ManaChangeReason.CAST);
    public static final ManaContext TOME_CAST = context("tome_cast", ManaChangeReason.CAST);
    public static final ManaContext INVOCATION = context("invocation", ManaChangeReason.ENCHANTMENT);

    private ArmoryManaContexts() {}

    private static ManaContext context(String path, ManaChangeReason reason) {
        return new ManaContext(
            ResourceLocation.fromNamespaceAndPath(MagicArmory.MOD_ID, path),
            reason
        );
    }
}
