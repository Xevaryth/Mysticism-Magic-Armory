package com.xevaryth.mysticism.magicarmory.registry;

import com.xevaryth.mysticism.magicarmory.MagicArmory;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MagicArmoryArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
        DeferredRegister.create(Registries.ARMOR_MATERIAL, MagicArmory.MOD_ID);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ACRYL = MATERIALS.register(
        "acryl", () -> new ArmorMaterial(
            diamondDefense(), 25, SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(MagicArmoryItems.ACRYL.get()),
            List.of(new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(MagicArmory.MOD_ID, "acryl"), "", true)),
            2.0F, 0.0F));

    private MagicArmoryArmorMaterials() {}
    public static void register(IEventBus modBus) { MATERIALS.register(modBus); }
    private static EnumMap<ArmorItem.Type, Integer> diamondDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.HELMET, 3);
        defense.put(ArmorItem.Type.BODY, 8);
        return defense;
    }
}
