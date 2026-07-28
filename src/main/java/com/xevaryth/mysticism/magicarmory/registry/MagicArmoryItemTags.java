package com.xevaryth.mysticism.magicarmory.registry;

import com.xevaryth.mysticism.magicarmory.MagicArmory;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class MagicArmoryItemTags {
    public static final TagKey<Item> ACRYL_EQUIPMENT = tag("acryl_equipment");
    public static final TagKey<Item> STAFFS = tag("staffs");
    public static final TagKey<Item> TOMES = tag("tomes");
    public static final TagKey<Item> FOCUSES = tag("focuses");
    public static final TagKey<Item> INVOCATION_CATALYSTS = tag("invocation_catalysts");

    private MagicArmoryItemTags() {}

    private static TagKey<Item> tag(String path) {
        return TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(MagicArmory.MOD_ID, path)
        );
    }
}
