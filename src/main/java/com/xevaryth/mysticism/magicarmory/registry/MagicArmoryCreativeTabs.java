package com.xevaryth.mysticism.magicarmory.registry;

import com.xevaryth.mysticism.magicarmory.MagicArmory;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MagicArmoryCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MagicArmory.MOD_ID);
    public static final Supplier<CreativeModeTab> MAGIC_ARMORY_TAB = TABS.register(
        "magic_armory", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.mysticism_magic_armory.magic_armory"))
            .icon(() -> new ItemStack(MagicArmoryItems.ACRYL.get()))
            .displayItems((parameters, output) -> {
                output.accept(MagicArmoryItems.ACRYL.get());
                output.accept(MagicArmoryItems.ACRYL_HELMET.get());
                output.accept(MagicArmoryItems.ACRYL_CHESTPLATE.get());
                output.accept(MagicArmoryItems.ACRYL_LEGGINGS.get());
                output.accept(MagicArmoryItems.ACRYL_BOOTS.get());
                output.accept(MagicArmoryItems.ACRYL_SWORD.get());
                output.accept(MagicArmoryItems.ACRYL_PICKAXE.get());
                output.accept(MagicArmoryItems.ACRYL_AXE.get());
                output.accept(MagicArmoryItems.ACRYL_SHOVEL.get());
                output.accept(MagicArmoryItems.ACRYL_HOE.get());
                output.accept(MagicArmoryItems.FLINT_STAFF.get());
                output.accept(MagicArmoryItems.AMETHYST_STAFF.get());
                output.accept(MagicArmoryItems.DIAMOND_STAFF.get());
                output.accept(MagicArmoryItems.EMERALD_STAFF.get());
                output.accept(MagicArmoryItems.NETHERITE_STAFF.get());
                output.accept(MagicArmoryItems.ACRYL_STAFF.get());
                output.accept(MagicArmoryItems.LEATHER_TOME.get());
                output.accept(MagicArmoryItems.AMETHYST_TOME.get());
                output.accept(MagicArmoryItems.DIAMOND_TOME.get());
                output.accept(MagicArmoryItems.EMERALD_TOME.get());
                output.accept(MagicArmoryItems.NETHERITE_TOME.get());
                output.accept(MagicArmoryItems.ACRYL_TOME.get());
                for (MagicArmoryBlocks.Entry entry : MagicArmoryBlocks.entries()) {
                    output.accept(MagicArmoryItems.BLOCK_ITEMS.get(entry.id()).get());
                }
                addEnchantedBook(parameters, output, MagicArmoryEnchantments.MANA_RESERVOIR);
                addEnchantedBook(parameters, output, MagicArmoryEnchantments.MAGIC_PROTECTION);
                addEnchantedBook(parameters, output, MagicArmoryEnchantments.MANA_FLOW);
            }).build());

    private MagicArmoryCreativeTabs() {}
    public static void register(IEventBus modBus) { TABS.register(modBus); }
    private static void addEnchantedBook(CreativeModeTab.ItemDisplayParameters parameters,
        CreativeModeTab.Output output, ResourceKey<Enchantment> key) {
        parameters.holders().lookupOrThrow(Registries.ENCHANTMENT).get(key).ifPresent(holder ->
            output.accept(EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(holder, holder.value().getMaxLevel()))));
    }
}
