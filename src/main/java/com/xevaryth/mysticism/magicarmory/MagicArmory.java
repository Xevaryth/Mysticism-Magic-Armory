package com.xevaryth.mysticism.magicarmory;

import com.xevaryth.mysticism.magicarmory.config.MagicArmoryConfig;
import com.xevaryth.mysticism.magicarmory.event.MagicEquipmentEvents;
import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryArmorMaterials;
import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryBlocks;
import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryCreativeTabs;
import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MagicArmory.MOD_ID)
public final class MagicArmory {
    public static final String MOD_ID = "mysticism_magic_armory";

    public MagicArmory(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, MagicArmoryConfig.SPEC);
        MagicArmoryArmorMaterials.register(modBus);
        MagicArmoryBlocks.register(modBus);
        MagicArmoryItems.register(modBus);
        MagicArmoryCreativeTabs.register(modBus);
        NeoForge.EVENT_BUS.addListener(MagicEquipmentEvents::onGetEnchantmentLevel);
    }
}
