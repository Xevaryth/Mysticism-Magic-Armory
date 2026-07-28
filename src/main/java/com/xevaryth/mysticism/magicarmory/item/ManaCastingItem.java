package com.xevaryth.mysticism.magicarmory.item;

import com.xevaryth.mysticism.api.ManaApi;
import com.xevaryth.mysticism.api.ManaContext;
import com.xevaryth.mysticism.api.ManaSpendFailureReason;
import com.xevaryth.mysticism.api.ManaSpendResult;
import com.xevaryth.mysticism.magicarmory.config.MagicArmoryConfig;
import com.xevaryth.mysticism.magicarmory.config.MagicArmoryConfig.CastValues;
import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryEnchantments;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public abstract class ManaCastingItem extends Item {
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    protected ManaCastingItem(
        Properties properties,
        int enchantmentValue,
        Supplier<Ingredient> repairIngredient
    ) {
        super(properties);
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    protected abstract CastValues values();
    protected abstract ManaContext manaContext();
    protected abstract void cast(Level level, Player player, ItemStack stack, CastValues values);

    protected Optional<Component> validateCast(
        Level level,
        Player player,
        ItemStack stack,
        CastValues values
    ) {
        return Optional.empty();
    }

    protected void appendSpecialTooltip(
        ItemStack stack,
        List<Component> tooltip,
        CastValues values
    ) {}

    @Override
    public InteractionResultHolder<ItemStack> use(
        Level level,
        Player player,
        InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        CastValues values = values();
        Optional<Component> failure = validateCast(level, player, stack, values);
        if (failure.isPresent()) {
            player.displayClientMessage(failure.get(), true);
            failSound(level, player);
            return InteractionResultHolder.fail(stack);
        }

        ManaSpendResult payment = ManaApi.spendMana(
            player,
            values.manaCost(),
            manaContext()
        );
        if (!payment.success()) {
            if (payment.failureReason() == ManaSpendFailureReason.INSUFFICIENT_MANA) {
                player.displayClientMessage(
                    Component.translatable(
                        "message.mysticism_magic_armory.not_enough_mana",
                        payment.finalCost()
                    ),
                    true
                );
            }
            failSound(level, player);
            return InteractionResultHolder.fail(stack);
        }

        completeCast(player, hand, stack, values, () -> cast(level, player, stack, values));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    protected final void completeCast(
        Player player,
        InteractionHand hand,
        ItemStack stack,
        CastValues values,
        Runnable action
    ) {
        action.run();
        damageFocus(player, hand, stack, values.durabilityPerCast());
        player.getCooldowns().addCooldown(this, values.cooldownTicks());
    }

    protected final void damageFocus(
        Player player,
        InteractionHand hand,
        ItemStack stack,
        int amount
    ) {
        if (amount <= 0) {
            return;
        }
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
            ? EquipmentSlot.MAINHAND
            : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(amount, player, slot);
    }

    protected static void failSound(Level level, Player player) {
        level.playSound(
            null,
            player.blockPosition(),
            SoundEvents.DISPENSER_FAIL,
            SoundSource.PLAYERS,
            0.5F,
            1.0F
        );
    }

    protected final double castRange(ItemStack stack, CastValues values) {
        int reach = MagicArmoryEnchantments.level(
            stack,
            MagicArmoryEnchantments.ARCANE_REACH
        );
        return values.range()
            + reach * MagicArmoryConfig.ENCHANTMENT_RULES.arcaneReachBlocksPerLevel();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return repairIngredient.get().test(ingredient)
            || super.isValidRepairItem(stack, ingredient);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return enchantmentValue;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return values().durability();
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        Item.TooltipContext context,
        List<Component> tooltip,
        TooltipFlag flag
    ) {
        CastValues values = values();
        tooltip.add(line(
            "tooltip.mysticism_magic_armory.damage",
            format(values.damage()),
            ChatFormatting.LIGHT_PURPLE
        ));
        tooltip.add(line(
            "tooltip.mysticism_magic_armory.mana_cost",
            values.manaCost(),
            ChatFormatting.AQUA
        ));
        appendSpecialTooltip(stack, tooltip, values);
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public boolean supportsEnchantment(
        ItemStack stack,
        Holder<Enchantment> enchantment
    ) {
        return enchantment.is(Enchantments.UNBREAKING)
            || enchantment.is(Enchantments.MENDING)
            || enchantment.is(Enchantments.VANISHING_CURSE)
            || MagicArmoryEnchantments.is(
                enchantment,
                MagicArmoryEnchantments.ARCANE_REACH
            )
            || super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean isPrimaryItemFor(
        ItemStack stack,
        Holder<Enchantment> enchantment
    ) {
        return MagicArmoryEnchantments.is(
                enchantment,
                MagicArmoryEnchantments.ARCANE_REACH
            )
            || super.isPrimaryItemFor(stack, enchantment);
    }

    protected static Component line(
        String key,
        Object value,
        ChatFormatting formatting
    ) {
        return Component.translatable(key, value).withStyle(formatting);
    }

    protected static String format(double value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
