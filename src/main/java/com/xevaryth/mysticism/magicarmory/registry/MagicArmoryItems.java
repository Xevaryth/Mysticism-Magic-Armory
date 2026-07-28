package com.xevaryth.mysticism.magicarmory.registry;

import com.xevaryth.mysticism.magicarmory.MagicArmory;
import com.xevaryth.mysticism.magicarmory.focus.MagicFocusTier;
import com.xevaryth.mysticism.magicarmory.item.MagicStaffItem;
import com.xevaryth.mysticism.magicarmory.item.MagicTomeItem;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MagicArmoryItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MagicArmory.MOD_ID);
    public static final DeferredItem<Item> ACRYL = ITEMS.registerSimpleItem("acryl");
    public static final Map<String, DeferredItem<BlockItem>> BLOCK_ITEMS = new LinkedHashMap<>();
    static {
        for (MagicArmoryBlocks.Entry entry : MagicArmoryBlocks.entries()) {
            BLOCK_ITEMS.put(entry.id(), ITEMS.registerSimpleBlockItem(entry.block()));
        }
    }

    public static final DeferredItem<ArmorItem> ACRYL_HELMET = armor("acryl_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<ArmorItem> ACRYL_CHESTPLATE = armor("acryl_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<ArmorItem> ACRYL_LEGGINGS = armor("acryl_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<ArmorItem> ACRYL_BOOTS = armor("acryl_boots", ArmorItem.Type.BOOTS);
    public static final DeferredItem<SwordItem> ACRYL_SWORD = ITEMS.registerItem(
        "acryl_sword", p -> new SwordItem(MagicArmoryTiers.ACRYL, p),
        toolProperties(SwordItem.createAttributes(MagicArmoryTiers.ACRYL, 3, -2.4F)));
    public static final DeferredItem<PickaxeItem> ACRYL_PICKAXE = ITEMS.registerItem(
        "acryl_pickaxe", p -> new PickaxeItem(MagicArmoryTiers.ACRYL, p),
        toolProperties(PickaxeItem.createAttributes(MagicArmoryTiers.ACRYL, 1.0F, -2.8F)));
    public static final DeferredItem<AxeItem> ACRYL_AXE = ITEMS.registerItem(
        "acryl_axe", p -> new AxeItem(MagicArmoryTiers.ACRYL, p),
        toolProperties(AxeItem.createAttributes(MagicArmoryTiers.ACRYL, 5.0F, -3.0F)));
    public static final DeferredItem<ShovelItem> ACRYL_SHOVEL = ITEMS.registerItem(
        "acryl_shovel", p -> new ShovelItem(MagicArmoryTiers.ACRYL, p),
        toolProperties(ShovelItem.createAttributes(MagicArmoryTiers.ACRYL, 1.5F, -3.0F)));
    public static final DeferredItem<HoeItem> ACRYL_HOE = ITEMS.registerItem(
        "acryl_hoe", p -> new HoeItem(MagicArmoryTiers.ACRYL, p),
        toolProperties(HoeItem.createAttributes(MagicArmoryTiers.ACRYL, -3.0F, 0.0F)));

    public static final DeferredItem<MagicStaffItem> FLINT_STAFF = staff(
        MagicFocusTier.FLINT_LEATHER, () -> Ingredient.of(Items.FLINT));
    public static final DeferredItem<MagicStaffItem> AMETHYST_STAFF = staff(
        MagicFocusTier.AMETHYST, () -> Ingredient.of(Items.AMETHYST_SHARD));
    public static final DeferredItem<MagicStaffItem> DIAMOND_STAFF = staff(
        MagicFocusTier.DIAMOND, () -> Ingredient.of(Items.DIAMOND));
    public static final DeferredItem<MagicStaffItem> EMERALD_STAFF = staff(
        MagicFocusTier.EMERALD, () -> Ingredient.of(Items.EMERALD));
    public static final DeferredItem<MagicStaffItem> NETHERITE_STAFF = staff(
        MagicFocusTier.NETHERITE, () -> Ingredient.of(Items.NETHERITE_INGOT));
    public static final DeferredItem<MagicStaffItem> ACRYL_STAFF = staff(
        MagicFocusTier.ACRYL, () -> Ingredient.of(ACRYL.get()));

    public static final DeferredItem<MagicTomeItem> LEATHER_TOME = tome(
        MagicFocusTier.FLINT_LEATHER, () -> Ingredient.of(Items.LEATHER));
    public static final DeferredItem<MagicTomeItem> AMETHYST_TOME = tome(
        MagicFocusTier.AMETHYST, () -> Ingredient.of(Items.AMETHYST_SHARD));
    public static final DeferredItem<MagicTomeItem> DIAMOND_TOME = tome(
        MagicFocusTier.DIAMOND, () -> Ingredient.of(Items.DIAMOND));
    public static final DeferredItem<MagicTomeItem> EMERALD_TOME = tome(
        MagicFocusTier.EMERALD, () -> Ingredient.of(Items.EMERALD));
    public static final DeferredItem<MagicTomeItem> NETHERITE_TOME = tome(
        MagicFocusTier.NETHERITE, () -> Ingredient.of(Items.NETHERITE_INGOT));
    public static final DeferredItem<MagicTomeItem> ACRYL_TOME = tome(
        MagicFocusTier.ACRYL, () -> Ingredient.of(ACRYL.get()));


    public static List<MagicTomeItem> tomes() {
        return List.of(
            LEATHER_TOME.get(), AMETHYST_TOME.get(), DIAMOND_TOME.get(),
            EMERALD_TOME.get(), NETHERITE_TOME.get(), ACRYL_TOME.get()
        );
    }

    private MagicArmoryItems() {}
    public static void register(IEventBus modBus) { ITEMS.register(modBus); }

    private static DeferredItem<ArmorItem> armor(String name, ArmorItem.Type type) {
        return ITEMS.registerItem(name, p -> new ArmorItem(MagicArmoryArmorMaterials.ACRYL, type, p),
            new Item.Properties().durability(type.getDurability(30)));
    }
    private static Item.Properties toolProperties(ItemAttributeModifiers attributes) {
        return new Item.Properties().durability(MagicArmoryTiers.ACRYL.getUses()).attributes(attributes);
    }
    private static DeferredItem<MagicStaffItem> staff(MagicFocusTier tier,
                                                       Supplier<Ingredient> repair) {
        Item.Properties properties = new Item.Properties()
            .durability(tier.staffDefaults().durability());
        if (tier.fireResistant()) properties = properties.fireResistant();
        Item.Properties finalProperties = properties;
        return ITEMS.registerItem(tier.staffId(),
            p -> new MagicStaffItem(tier, repair, p), finalProperties);
    }
    private static DeferredItem<MagicTomeItem> tome(MagicFocusTier tier,
                                                     Supplier<Ingredient> repair) {
        Item.Properties properties = new Item.Properties().durability(tier.tomeDefaults().durability());
        if (tier.fireResistant()) properties = properties.fireResistant();
        Item.Properties finalProperties = properties;
        return ITEMS.registerItem(tier.tomeId(),
            p -> new MagicTomeItem(tier, repair, p), finalProperties);
    }
}
