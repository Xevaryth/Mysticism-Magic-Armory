package com.xevaryth.mysticism.magicarmory.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.xevaryth.mysticism.magicarmory.MagicArmory;
import com.xevaryth.mysticism.magicarmory.focus.MagicFocusTier;
import com.xevaryth.mysticism.magicarmory.item.MagicTomeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Keeps the existing flat tome model for GUI, ground, frame, and head contexts,
 * while using the animated enchanting-table book geometry only in a hand.
 */
public final class TomeItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final int DEFAULT_ACRYL_COLOR = 0xE8A7DD;
    private static final ResourceLocation ACRYL_PAGES = texture("acryl_tome_pages");
    private static final ResourceLocation ACRYL_COVER = texture("acryl_tome_cover");

    private final BookModel bookModel;

    public TomeItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());
        bookModel = new BookModel(BookModel.createBodyLayer().bakeRoot());
    }

    public static ModelResourceLocation flatModel(MagicFocusTier tier) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
            MagicArmory.MOD_ID, "item/" + tier.tomeId() + "_flat"));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
        PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!isHeld(displayContext)) {
            renderFlat(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }

        float time = (System.currentTimeMillis() % 120000L) / 50.0F;
        float page = (Mth.sin(time * 0.17F) + 1.0F) * 0.5F;

        poseStack.pushPose();
        // ItemRenderer translates custom models by -0.5 before invoking the BEWLR.
        poseStack.translate(0.5F, 0.5F, 0.5F);
        applyHeldTransform(displayContext, poseStack);

        // Broadly open covers with a slow alternating page turn.
        bookModel.setupAnim(time * 0.075F,
            Mth.clamp(page + 0.12F, 0.0F, 1.0F),
            Mth.clamp(1.12F - page, 0.0F, 1.0F),
            0.38F);

        MagicFocusTier tier = tier(stack);
        if (tier == MagicFocusTier.ACRYL) {
            renderAcryl(stack, poseStack, bufferSource, packedLight, packedOverlay);
        } else {
            VertexConsumer consumer = ItemRenderer.getFoilBuffer(
                bufferSource, RenderType.entityCutoutNoCull(texture(tier.tomeId())),
                false, stack.hasFoil());
            bookModel.render(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);
        }
        poseStack.popPose();
    }

    private static void renderFlat(ItemStack stack, ItemDisplayContext context,
        PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel flat = minecraft.getModelManager().getModel(flatModel(tier(stack)));
        boolean leftHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
            || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        poseStack.pushPose();
        // Undo the centering translation applied for the builtin/entity parent;
        // the nested flat-model render then applies the original 0.15.1 transforms.
        poseStack.translate(0.5F, 0.5F, 0.5F);
        minecraft.getItemRenderer().render(stack, context, leftHand, poseStack,
            bufferSource, packedLight, packedOverlay, flat);
        poseStack.popPose();
    }

    private static boolean isHeld(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
            || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
            || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
            || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    private static void applyHeldTransform(ItemDisplayContext context, PoseStack poseStack) {
        // BookModel is upright by default. The ~80 degree Z rotation used by the
        // enchanting table lays its spine horizontally so pages turn naturally.
        switch (context) {
            case FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(-0.10F, -0.20F, 0.10F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(8.0F));
                poseStack.scale(0.92F, 0.92F, 0.92F);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(0.10F, -0.20F, 0.10F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(8.0F));
                poseStack.scale(0.92F, 0.92F, 0.92F);
            }
            case THIRD_PERSON_RIGHT_HAND -> {
                // Move the book forward from the raised hand and turn the
                // open pages inward so the holder is visibly reading it.
                poseStack.translate(0.10F, -0.38F, -0.08F);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(6.0F));
                poseStack.scale(0.82F, 0.82F, 0.82F);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(-0.10F, -0.38F, -0.08F);
                poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(6.0F));
                poseStack.scale(0.82F, 0.82F, 0.82F);
            }
            default -> { }
        }
    }

    private void renderAcryl(ItemStack stack, PoseStack poseStack,
        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        VertexConsumer pages = bufferSource.getBuffer(RenderType.entityCutoutNoCull(ACRYL_PAGES));
        bookModel.render(poseStack, pages, packedLight, packedOverlay, 0xFFFFFFFF);

        int rgb = DyedItemColor.getOrDefault(stack, DEFAULT_ACRYL_COLOR);
        int tint = FastColor.ARGB32.color(205,
            FastColor.ARGB32.red(rgb), FastColor.ARGB32.green(rgb), FastColor.ARGB32.blue(rgb));
        VertexConsumer cover = ItemRenderer.getArmorFoilBuffer(
            bufferSource, RenderType.entityTranslucent(ACRYL_COVER), stack.hasFoil());
        bookModel.render(poseStack, cover, packedLight, packedOverlay, tint);
    }

    private static MagicFocusTier tier(ItemStack stack) {
        return stack.getItem() instanceof MagicTomeItem tome
            ? tome.tier() : MagicFocusTier.FLINT_LEATHER;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(
            MagicArmory.MOD_ID, "textures/entity/tome/" + name + ".png");
    }
}
