package com.xevaryth.mysticism.magicarmory.client;

import com.xevaryth.mysticism.magicarmory.MagicArmory;
import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryItems;
import java.util.List;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@Mod(value = MagicArmory.MOD_ID, dist = Dist.CLIENT)
public final class MagicArmoryClient {
    private static final int DEFAULT_ACRYL_COLOR = 0xE8A7DD;
    public MagicArmoryClient(IEventBus modBus) {
        modBus.addListener(this::registerClientExtensions);
        modBus.addListener(this::registerAdditionalModels);
        modBus.addListener(this::registerItemColors);
        modBus.addListener(this::addRenderLayers);
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        TomeItemRenderer tomeRenderer = new TomeItemRenderer();
        IClientItemExtensions tomeExtensions = new IClientItemExtensions() {
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return tomeRenderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(
                LivingEntity entity,
                InteractionHand hand,
                ItemStack stack
            ) {
                // Raise the holding arm in front of the torso instead of
                // leaving an open tome hanging from the default item pose.
                return HumanoidModel.ArmPose.BLOCK;
            }
        };
        event.registerItem(tomeExtensions,
            MagicArmoryItems.LEATHER_TOME.get(), MagicArmoryItems.AMETHYST_TOME.get(),
            MagicArmoryItems.DIAMOND_TOME.get(), MagicArmoryItems.EMERALD_TOME.get(),
            MagicArmoryItems.NETHERITE_TOME.get(), MagicArmoryItems.ACRYL_TOME.get());

        IClientItemExtensions armorExtensions = new IClientItemExtensions() {
            @Override public int getDefaultDyeColor(ItemStack stack) {
                return FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(stack, DEFAULT_ACRYL_COLOR));
            }
            @Override public int getArmorLayerTintColor(ItemStack stack,
                net.minecraft.world.entity.LivingEntity entity,
                net.minecraft.world.item.ArmorMaterial.Layer layer, int layerIndex, int fallbackColor) {
                return entity instanceof Player ? 0 : fallbackColor;
            }
        };
        event.registerItem(armorExtensions, MagicArmoryItems.ACRYL_HELMET.get(),
            MagicArmoryItems.ACRYL_CHESTPLATE.get(), MagicArmoryItems.ACRYL_LEGGINGS.get(),
            MagicArmoryItems.ACRYL_BOOTS.get());
    }

    private void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (com.xevaryth.mysticism.magicarmory.focus.MagicFocusTier tier
            : com.xevaryth.mysticism.magicarmory.focus.MagicFocusTier.values()) {
            event.register(TomeItemRenderer.flatModel(tier));
        }
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        List<ItemLike> items = List.of(
            MagicArmoryItems.ACRYL_HELMET.get(), MagicArmoryItems.ACRYL_CHESTPLATE.get(),
            MagicArmoryItems.ACRYL_LEGGINGS.get(), MagicArmoryItems.ACRYL_BOOTS.get(),
            MagicArmoryItems.ACRYL_SWORD.get(), MagicArmoryItems.ACRYL_PICKAXE.get(),
            MagicArmoryItems.ACRYL_AXE.get(), MagicArmoryItems.ACRYL_SHOVEL.get(),
            MagicArmoryItems.ACRYL_HOE.get(), MagicArmoryItems.ACRYL_STAFF.get(),
            MagicArmoryItems.ACRYL_TOME.get());
        event.register((stack, tintIndex) -> {
            if (tintIndex != 0) return 0xFFFFFFFF;
            int rgb = DyedItemColor.getOrDefault(stack, DEFAULT_ACRYL_COLOR);
            return FastColor.ARGB32.color(190, FastColor.ARGB32.red(rgb),
                FastColor.ARGB32.green(rgb), FastColor.ARGB32.blue(rgb));
        }, items.toArray(ItemLike[]::new));
    }

    private void addRenderLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer == null) continue;
            boolean slim = skin == PlayerSkin.Model.SLIM;
            renderer.addLayer(new AcrylArmorLayer(renderer,
                new HumanoidArmorModel<>(event.getEntityModels().bakeLayer(
                    slim ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(event.getEntityModels().bakeLayer(
                    slim ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR))));
        }
    }
}
