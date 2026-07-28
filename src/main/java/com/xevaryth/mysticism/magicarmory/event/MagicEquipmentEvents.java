package com.xevaryth.mysticism.magicarmory.event;

import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryItemTags;
import java.util.ArrayList;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

public final class MagicEquipmentEvents {
    private MagicEquipmentEvents() {}

    public static void onGetEnchantmentLevel(GetEnchantmentLevelEvent event) {
        if (!event.getStack().is(MagicArmoryItemTags.ACRYL_EQUIPMENT)) return;
        ItemEnchantments.Mutable enchantments = event.getEnchantments();
        Holder<Enchantment> target = event.getTargetEnchant();
        if (target != null) {
            amplify(enchantments, target);
            return;
        }
        for (Holder<Enchantment> enchantment : new ArrayList<>(enchantments.keySet())) {
            amplify(enchantments, enchantment);
        }
    }

    private static void amplify(ItemEnchantments.Mutable enchantments, Holder<Enchantment> enchantment) {
        int level = enchantments.getLevel(enchantment);
        if (level > 0 && enchantment.value().getMaxLevel() > 1) enchantments.set(enchantment, level + 1);
    }
}
