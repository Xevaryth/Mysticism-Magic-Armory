package com.xevaryth.mysticism.magicarmory.config;

import com.xevaryth.mysticism.magicarmory.focus.MagicFocusTier;
import java.util.EnumMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class MagicArmoryConfig {
    public static final ModConfigSpec SPEC;
    public static final EnchantmentRules ENCHANTMENT_RULES;
    public static final InvocationRules INVOCATION_RULES;

    private static final Map<MagicFocusTier, CastValues> STAFF_VALUES =
        new EnumMap<>(MagicFocusTier.class);
    private static final Map<MagicFocusTier, CastValues> TOME_VALUES =
        new EnumMap<>(MagicFocusTier.class);
    private static ModConfigSpec.DoubleValue TOME_RADIUS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment(
            "Mysticism: Magic Armory balance settings.",
            "Cooldowns and lifetimes are game ticks; 20 ticks = 1 second."
        );

        builder.push("staffs");
        for (MagicFocusTier tier : MagicFocusTier.values()) {
            builder.push(tier.key());
            STAFF_VALUES.put(tier, castValues(builder, tier.staffDefaults()));
            builder.pop();
        }
        builder.pop();

        builder.push("tomes");
        TOME_RADIUS = builder.comment(
            "Shared area radius for every tome tier."
        ).defineInRange("radius", 4.0D, 0.5D, 64.0D);
        for (MagicFocusTier tier : MagicFocusTier.values()) {
            builder.push(tier.key());
            TOME_VALUES.put(tier, castValues(builder, tier.tomeDefaults()));
            builder.pop();
        }

        builder.push("invocation");
        INVOCATION_RULES = new InvocationRules(
            builder.comment("Cooldown applied to every tome after Invocation.")
                .defineInRange("cooldownTicks", 1200, 20, 72000),
            builder.comment("Temporary arrows created by an arrow catalyst.")
                .defineInRange("arrowCount", 6, 1, 64),
            builder.comment("Maximum lifetime of an invocation arrow.")
                .defineInRange("arrowLifetimeTicks", 100, 10, 1200),
            builder.comment("Minimum height above the target area for arrow rain.")
                .defineInRange("arrowMinHeight", 8.0D, 2.0D, 64.0D),
            builder.comment("Maximum height above the target area for arrow rain.")
                .defineInRange("arrowMaxHeight", 12.0D, 2.0D, 96.0D),
            builder.comment("Number of rockets launched by one firework catalyst.")
                .defineInRange("fireworkCount", 3, 1, 16)
        );
        builder.pop();
        builder.pop();

        builder.push("enchantments");
        ENCHANTMENT_RULES = new EnchantmentRules(
            builder.comment("Casting range added by each Arcane Reach level.")
                .defineInRange("arcaneReachBlocksPerLevel", 4.0D, 0.0D, 64.0D),
            builder.comment("Flat melee damage added by Arcane Impact I.")
                .defineInRange("arcaneImpactBaseDamage", 1.0D, 0.0D, 100.0D),
            builder.comment("Additional flat melee damage for each level after Arcane Impact I.")
                .defineInRange("arcaneImpactDamagePerLevel", 0.5D, 0.0D, 100.0D)
        );
        builder.pop();

        SPEC = builder.build();
    }

    private MagicArmoryConfig() {}

    private static CastValues castValues(
        ModConfigSpec.Builder builder,
        MagicFocusTier.CastDefaults defaults
    ) {
        return new CastValues(
            builder.comment("Mana consumed by one successful cast.")
                .defineInRange("manaCost", defaults.manaCost(), 0, 100000),
            builder.comment("Magic damage dealt by the cast.")
                .defineInRange("damage", defaults.damage(), 0.0D, 100000.0D),
            builder.comment("Maximum casting range in blocks.")
                .defineInRange("range", defaults.range(), 1.0D, 256.0D),
            builder.comment("Cooldown after a successful cast.")
                .defineInRange("cooldownTicks", defaults.cooldownTicks(), 0, 12000),
            builder.comment("Maximum item durability.")
                .defineInRange("durability", defaults.durability(), 1, 1000000),
            builder.comment("Durability consumed by one successful cast.")
                .defineInRange("durabilityPerCast", 1, 0, 100000)
        );
    }

    public static CastValues staff(MagicFocusTier tier) {
        return STAFF_VALUES.get(tier);
    }

    public static TomeValues tome(MagicFocusTier tier) {
        return new TomeValues(TOME_VALUES.get(tier), TOME_RADIUS);
    }

    public record CastValues(
        ModConfigSpec.IntValue manaCostValue,
        ModConfigSpec.DoubleValue damageValue,
        ModConfigSpec.DoubleValue rangeValue,
        ModConfigSpec.IntValue cooldownTicksValue,
        ModConfigSpec.IntValue durabilityValue,
        ModConfigSpec.IntValue durabilityPerCastValue
    ) {
        public int manaCost() { return manaCostValue.get(); }
        public double damage() { return damageValue.get(); }
        public double range() { return rangeValue.get(); }
        public int cooldownTicks() { return cooldownTicksValue.get(); }
        public int durability() { return durabilityValue.get(); }
        public int durabilityPerCast() { return durabilityPerCastValue.get(); }
    }

    public record TomeValues(
        CastValues cast,
        ModConfigSpec.DoubleValue radiusValue
    ) {
        public double radius() { return radiusValue.get(); }
    }

    public record EnchantmentRules(
        ModConfigSpec.DoubleValue arcaneReachBlocksPerLevelValue,
        ModConfigSpec.DoubleValue arcaneImpactBaseDamageValue,
        ModConfigSpec.DoubleValue arcaneImpactDamagePerLevelValue
    ) {
        public double arcaneReachBlocksPerLevel() {
            return arcaneReachBlocksPerLevelValue.get();
        }
        public double arcaneImpactBaseDamage() {
            return arcaneImpactBaseDamageValue.get();
        }
        public double arcaneImpactDamagePerLevel() {
            return arcaneImpactDamagePerLevelValue.get();
        }
    }

    public record InvocationRules(
        ModConfigSpec.IntValue cooldownTicksValue,
        ModConfigSpec.IntValue arrowCountValue,
        ModConfigSpec.IntValue arrowLifetimeTicksValue,
        ModConfigSpec.DoubleValue arrowMinHeightValue,
        ModConfigSpec.DoubleValue arrowMaxHeightValue,
        ModConfigSpec.IntValue fireworkCountValue
    ) {
        public int cooldownTicks() { return cooldownTicksValue.get(); }
        public int arrowCount() { return arrowCountValue.get(); }
        public int arrowLifetimeTicks() { return arrowLifetimeTicksValue.get(); }
        public double arrowMinHeight() { return arrowMinHeightValue.get(); }
        public double arrowMaxHeight() { return arrowMaxHeightValue.get(); }
        public int fireworkCount() { return fireworkCountValue.get(); }
    }
}
