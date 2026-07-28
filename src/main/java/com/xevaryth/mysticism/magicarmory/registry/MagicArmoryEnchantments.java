package com.xevaryth.mysticism.magicarmory.registry;

import com.xevaryth.mysticism.magicarmory.MagicArmory;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class MagicArmoryEnchantments {
    public static final ResourceKey<Enchantment> MANA_RESERVOIR = key("mana_reservoir");
    public static final ResourceKey<Enchantment> MAGIC_PROTECTION = key("magic_protection");
    public static final ResourceKey<Enchantment> MANA_FLOW = key("mana_flow");
    public static final ResourceKey<Enchantment> ARCANE_REACH = key("arcane_reach");
    public static final ResourceKey<Enchantment> ARCANE_IMPACT = key("arcane_impact");
    public static final ResourceKey<Enchantment> INVOCATION = key("invocation");

    private MagicArmoryEnchantments() {}

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(MagicArmory.MOD_ID, path)
        );
    }

    /** Returns the effective gameplay level, including Acryl's +1 level rule. */
    public static int level(ItemStack stack, ResourceKey<Enchantment> key) {
        ItemEnchantments enchantments = stack.getOrDefault(
            DataComponents.ENCHANTMENTS,
            ItemEnchantments.EMPTY
        );
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (enchantment.is(key)) {
                return stack.getEnchantmentLevel(enchantment);
            }
        }
        return 0;
    }

    public static boolean is(Holder<Enchantment> enchantment, ResourceKey<Enchantment> key) {
        return enchantment.is(key);
    }
}
