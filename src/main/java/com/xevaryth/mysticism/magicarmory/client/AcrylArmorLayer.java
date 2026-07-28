package com.xevaryth.mysticism.magicarmory.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xevaryth.mysticism.magicarmory.MagicArmory;
import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryItemTags;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public final class AcrylArmorLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation OUTER = ResourceLocation.fromNamespaceAndPath(
        MagicArmory.MOD_ID, "textures/models/armor/acryl_layer_1.png");
    private static final ResourceLocation INNER = ResourceLocation.fromNamespaceAndPath(
        MagicArmory.MOD_ID, "textures/models/armor/acryl_layer_2.png");
    private static final int DEFAULT_COLOR = 0xE8A7DD;
    private final HumanoidModel<AbstractClientPlayer> innerModel;
    private final HumanoidModel<AbstractClientPlayer> outerModel;

    public AcrylArmorLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                           HumanoidModel<AbstractClientPlayer> innerModel,
                           HumanoidModel<AbstractClientPlayer> outerModel) {
        super(parent); this.innerModel = innerModel; this.outerModel = outerModel;
    }

    @Override public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
        AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick,
        float ageInTicks, float netHeadYaw, float headPitch) {
        renderPiece(poseStack, buffers, packedLight, player, EquipmentSlot.CHEST);
        renderPiece(poseStack, buffers, packedLight, player, EquipmentSlot.LEGS);
        renderPiece(poseStack, buffers, packedLight, player, EquipmentSlot.FEET);
        renderPiece(poseStack, buffers, packedLight, player, EquipmentSlot.HEAD);
    }

    private void renderPiece(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                             AbstractClientPlayer player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (!stack.is(MagicArmoryItemTags.ACRYL_EQUIPMENT)
            || !(stack.getItem() instanceof ArmorItem armor) || armor.getEquipmentSlot() != slot) return;
        boolean inner = slot == EquipmentSlot.LEGS;
        HumanoidModel<AbstractClientPlayer> model = inner ? innerModel : outerModel;
        getParentModel().copyPropertiesTo(model);
        setVisible(model, slot);
        int rgb = DyedItemColor.getOrDefault(stack, DEFAULT_COLOR);
        int color = FastColor.ARGB32.color(150, FastColor.ARGB32.red(rgb),
            FastColor.ARGB32.green(rgb), FastColor.ARGB32.blue(rgb));
        VertexConsumer buffer = ItemRenderer.getArmorFoilBuffer(
            buffers, RenderType.entityTranslucent(inner ? INNER : OUTER), stack.hasFoil());
        model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, color);
    }

    private static void setVisible(HumanoidModel<?> model, EquipmentSlot slot) {
        model.setAllVisible(false);
        switch (slot) {
            case HEAD -> { model.head.visible = true; model.hat.visible = true; }
            case CHEST -> { model.body.visible = true; model.rightArm.visible = true; model.leftArm.visible = true; }
            case LEGS -> { model.body.visible = true; model.rightLeg.visible = true; model.leftLeg.visible = true; }
            case FEET -> { model.rightLeg.visible = true; model.leftLeg.visible = true; }
            default -> { }
        }
    }
}
