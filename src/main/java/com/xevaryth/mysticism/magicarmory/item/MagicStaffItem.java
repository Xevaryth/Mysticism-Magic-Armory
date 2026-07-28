package com.xevaryth.mysticism.magicarmory.item;

import com.xevaryth.mysticism.api.ManaContext;
import com.xevaryth.mysticism.magicarmory.ArmoryManaContexts;
import com.xevaryth.mysticism.magicarmory.config.MagicArmoryConfig;
import com.xevaryth.mysticism.magicarmory.config.MagicArmoryConfig.CastValues;
import com.xevaryth.mysticism.magicarmory.focus.MagicFocusTier;
import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryEnchantments;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class MagicStaffItem extends BeamCastingItem {
    private static final int DEFAULT_ACRYL_COLOR = 0xE8A7DD;
    private static final double BASE_MELEE_DAMAGE = 5.0D;
    private static final double ATTACK_SPEED = -2.8D;

    private final MagicFocusTier tier;

    public MagicStaffItem(
        MagicFocusTier tier,
        Supplier<Ingredient> repairIngredient,
        Item.Properties properties
    ) {
        super(properties, tier.enchantability(), repairIngredient);
        this.tier = tier;
    }

    public MagicFocusTier tier() {
        return tier;
    }

    @Override
    protected CastValues values() {
        return MagicArmoryConfig.staff(tier);
    }

    @Override
    protected ManaContext manaContext() {
        return ArmoryManaContexts.STAFF_CAST;
    }

    @Override
    protected double particlesPerBlock() {
        return 2.0D;
    }

    @Override
    protected ParticleOptions trailParticle(ItemStack stack) {
        return ColorParticleOption.create(
            ParticleTypes.ENTITY_EFFECT,
            particleArgb(stack)
        );
    }

    @Override
    protected void cast(
        Level level,
        Player player,
        ItemStack stack,
        CastValues values
    ) {
        BeamTrace trace = traceBeam(level, player, stack, values);
        int piercing = MagicArmoryEnchantments.level(stack, Enchantments.PIERCING);
        int targetLimit = 1 + Math.max(0, piercing);
        List<BeamTarget> selected = trace.targets().stream()
            .limit(targetLimit)
            .toList();

        Vec3 visualEnd = trace.end();
        for (BeamTarget hit : selected) {
            LivingEntity target = hit.target();
            applyProjectileFireAspect(stack, target);
            damageTarget(level, player, target, (float) values.damage());
            applyProjectileKnockback(stack, player, target);
            tryChannelLightning(level, player, stack, target);
            visualEnd = hit.hitPoint();
        }

        if (level instanceof ServerLevel serverLevel) {
            ParticleOptions particle = trailParticle(stack);
            spawnTrail(
                serverLevel,
                trace.start(),
                visualEnd,
                particle,
                particlesPerBlock()
            );
            if (!selected.isEmpty()) {
                spawnImpact(serverLevel, visualEnd, particle);
            }
        }

        level.playSound(
            null,
            player.blockPosition(),
            SoundEvents.TRIDENT_THROW.value(),
            SoundSource.PLAYERS,
            0.9F,
            tier == MagicFocusTier.EMERALD ? 1.25F : 1.15F
        );
        level.playSound(
            null,
            player.blockPosition(),
            SoundEvents.AMETHYST_BLOCK_CHIME,
            SoundSource.PLAYERS,
            0.8F,
            tier == MagicFocusTier.EMERALD ? 1.65F : 1.45F
        );
    }

    private static void spawnImpact(
        ServerLevel level,
        Vec3 point,
        ParticleOptions particle
    ) {
        level.sendParticles(
            particle,
            point.x,
            point.y,
            point.z,
            10,
            0.18D,
            0.18D,
            0.18D,
            0.03D
        );
    }

    private int particleArgb(ItemStack stack) {
        int rgb = switch (tier) {
            case FLINT_LEATHER -> 0x787878;
            case AMETHYST -> 0xB77DDB;
            case DIAMOND -> 0x54DDE3;
            case EMERALD -> 0x48C96B;
            case NETHERITE -> 0x69535C;
            case ACRYL -> DyedItemColor.getOrDefault(stack, DEFAULT_ACRYL_COLOR);
        };
        int alpha = tier == MagicFocusTier.ACRYL ? 150 : 255;
        return FastColor.ARGB32.color(
            alpha,
            FastColor.ARGB32.red(rgb),
            FastColor.ARGB32.green(rgb),
            FastColor.ARGB32.blue(rgb)
        );
    }

    private static void applyProjectileFireAspect(
        ItemStack stack,
        LivingEntity target
    ) {
        int fireAspect = MagicArmoryEnchantments.level(
            stack,
            Enchantments.FIRE_ASPECT
        );
        if (fireAspect > 0) {
            target.setRemainingFireTicks(Math.max(
                target.getRemainingFireTicks(),
                80 * fireAspect
            ));
        }
    }

    private static void applyProjectileKnockback(
        ItemStack stack,
        Player player,
        LivingEntity target
    ) {
        int knockback = MagicArmoryEnchantments.level(
            stack,
            Enchantments.KNOCKBACK
        );
        if (knockback <= 0 || !target.isAlive()) {
            return;
        }
        target.knockback(
            0.5D * knockback,
            player.getX() - target.getX(),
            player.getZ() - target.getZ()
        );
        target.hurtMarked = true;
    }

    private static void tryChannelLightning(
        Level level,
        Player player,
        ItemStack stack,
        LivingEntity target
    ) {
        int channeling = MagicArmoryEnchantments.level(
            stack,
            Enchantments.CHANNELING
        );
        if (channeling <= 0
            || !(level instanceof ServerLevel serverLevel)
            || !serverLevel.isThundering()
            || !serverLevel.canSeeSky(target.blockPosition())) {
            return;
        }

        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (lightning == null) {
            return;
        }
        lightning.moveTo(target.getX(), target.getY(), target.getZ());
        if (player instanceof ServerPlayer serverPlayer) {
            lightning.setCause(serverPlayer);
        }
        serverLevel.addFreshEntity(lightning);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        double impactDamage = arcaneImpactDamage(stack);
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                    Item.BASE_ATTACK_DAMAGE_ID,
                    BASE_MELEE_DAMAGE + impactDamage,
                    AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
            )
            .add(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(
                    Item.BASE_ATTACK_SPEED_ID,
                    ATTACK_SPEED,
                    AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
            )
            .build();
    }

    private double arcaneImpactDamage(ItemStack stack) {
        int impact = MagicArmoryEnchantments.level(
            stack,
            MagicArmoryEnchantments.ARCANE_IMPACT
        );
        if (impact <= 0) {
            return 0.0D;
        }
        // Arcane Impact follows vanilla Sharpness-style additive scaling.
        // It improves the staff as a melee weapon without multiplying its
        // substantially larger spell damage into the attack attribute.
        return MagicArmoryConfig.ENCHANTMENT_RULES
            .arcaneImpactBaseDamage()
            + MagicArmoryConfig.ENCHANTMENT_RULES
                .arcaneImpactDamagePerLevel() * (impact - 1);
    }

    @Override
    public boolean supportsEnchantment(
        ItemStack stack,
        Holder<Enchantment> enchantment
    ) {
        return enchantment.is(Enchantments.CHANNELING)
            || enchantment.is(Enchantments.KNOCKBACK)
            || enchantment.is(Enchantments.FIRE_ASPECT)
            || enchantment.is(Enchantments.LOOTING)
            || enchantment.is(Enchantments.PIERCING)
            || MagicArmoryEnchantments.is(
                enchantment,
                MagicArmoryEnchantments.ARCANE_IMPACT
            )
            || super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean isPrimaryItemFor(
        ItemStack stack,
        Holder<Enchantment> enchantment
    ) {
        return enchantment.is(Enchantments.CHANNELING)
            || enchantment.is(Enchantments.KNOCKBACK)
            || enchantment.is(Enchantments.FIRE_ASPECT)
            || enchantment.is(Enchantments.LOOTING)
            || enchantment.is(Enchantments.PIERCING)
            || MagicArmoryEnchantments.is(
                enchantment,
                MagicArmoryEnchantments.ARCANE_IMPACT
            )
            || super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean hurtEnemy(
        ItemStack stack,
        LivingEntity target,
        LivingEntity attacker
    ) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
