package com.xevaryth.mysticism.magicarmory.item;

import com.xevaryth.mysticism.api.ManaApi;
import com.xevaryth.mysticism.api.ManaContext;
import com.xevaryth.mysticism.magicarmory.ArmoryManaContexts;
import com.xevaryth.mysticism.magicarmory.config.MagicArmoryConfig;
import com.xevaryth.mysticism.magicarmory.config.MagicArmoryConfig.CastValues;
import com.xevaryth.mysticism.magicarmory.focus.MagicFocusTier;
import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryEnchantments;
import com.xevaryth.mysticism.magicarmory.registry.MagicArmoryItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MagicTomeItem extends ManaCastingItem {
    private final MagicFocusTier tier;

    public MagicTomeItem(
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
        return MagicArmoryConfig.tome(tier).cast();
    }

    @Override
    protected ManaContext manaContext() {
        return ArmoryManaContexts.TOME_CAST;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
        Level level,
        Player player,
        InteractionHand hand
    ) {
        ItemStack tome = player.getItemInHand(hand);
        ItemStack catalyst = player.getOffhandItem();
        boolean invocationAttempt = hand == InteractionHand.MAIN_HAND
            && player.isShiftKeyDown()
            && MagicArmoryEnchantments.level(
                tome,
                MagicArmoryEnchantments.INVOCATION
            ) > 0;

        if (!invocationAttempt) {
            return super.use(level, player, hand);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(tome, true);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(tome);
        }
        if (!isSupportedCatalyst(catalyst)) {
            player.displayClientMessage(
                Component.translatable(
                    "message.mysticism_magic_armory.unsupported_invocation_catalyst"
                ),
                true
            );
            failSound(level, player);
            return InteractionResultHolder.fail(tome);
        }
        return invoke(serverLevel, player, hand, tome, catalyst)
            ? InteractionResultHolder.sidedSuccess(tome, false)
            : InteractionResultHolder.fail(tome);
    }

    private boolean invoke(
        ServerLevel level,
        Player player,
        InteractionHand hand,
        ItemStack tome,
        ItemStack catalyst
    ) {
        if (ManaApi.getMana(player) <= 0) {
            player.displayClientMessage(
                Component.translatable(
                    "message.mysticism_magic_armory.invocation_requires_mana"
                ),
                true
            );
            failSound(level, player);
            return false;
        }

        CastValues values = values();
        Vec3 center = targetPoint(level, player, tome, values);
        Optional<Vec3> safeDestination = catalyst.is(Items.ENDER_PEARL)
            ? findSafeTeleport(level, player, center)
            : Optional.empty();
        if (catalyst.is(Items.ENDER_PEARL) && safeDestination.isEmpty()) {
            player.displayClientMessage(
                Component.translatable(
                    "message.mysticism_magic_armory.no_safe_invocation_target"
                ),
                true
            );
            failSound(level, player);
            return false;
        }

        int mana = ManaApi.getMana(player);
        ManaApi.drainMana(player, mana, ArmoryManaContexts.INVOCATION);
        if (ManaApi.getMana(player) > 0) {
            ManaApi.setMana(player, 0);
        }

        Item remainder = invocationRemainder(catalyst);
        if (!performInvocation(
            level,
            player,
            catalyst.copyWithCount(1),
            center,
            safeDestination
        )) {
            return false;
        }

        consumeCatalyst(player, catalyst, remainder);
        damageFocus(player, hand, tome, values.durabilityPerCast());
        addInvocationCooldown(player);

        level.playSound(
            null,
            player.blockPosition(),
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.PLAYERS,
            1.25F,
            0.65F
        );
        level.sendParticles(
            ParticleTypes.ENCHANT,
            center.x,
            center.y + 0.35D,
            center.z,
            48,
            tomeRadius() * 0.65D,
            tomeRadius() * 0.35D,
            tomeRadius() * 0.65D,
            0.12D
        );
        return true;
    }

    private boolean performInvocation(
        ServerLevel level,
        Player player,
        ItemStack catalyst,
        Vec3 center,
        Optional<Vec3> safeDestination
    ) {
        if (isPotion(catalyst)) {
            invokePotion(level, player, catalyst, center);
            return true;
        }
        if (catalyst.is(Items.ENDER_PEARL)) {
            teleportPlayer(level, player, safeDestination.orElseThrow());
            return true;
        }
        if (catalyst.is(Items.TNT)) {
            PrimedTnt tnt = new PrimedTnt(
                level,
                center.x,
                center.y + 0.15D,
                center.z,
                player
            );
            level.addFreshEntity(tnt);
            return true;
        }
        if (isArrow(catalyst)) {
            invokeArrowRain(level, player, catalyst, center);
            return true;
        }
        if (catalyst.is(Items.FIREWORK_ROCKET)) {
            invokeFireworks(level, player, catalyst, center);
            return true;
        }
        if (catalyst.is(Items.MILK_BUCKET)) {
            invokeMilk(level, center);
            return true;
        }
        return false;
    }

    @Override
    protected void cast(
        Level level,
        Player player,
        ItemStack stack,
        CastValues values
    ) {
        Vec3 center = targetPoint(level, player, stack, values);
        double radius = tomeRadius();
        AABB area = new AABB(center, center).inflate(radius);
        List<LivingEntity> targets = new ArrayList<>(
            level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                    && !entity.isSpectator()
                    && entity.distanceToSqr(center) <= radius * radius
            )
        );
        if (player.isAlive()
            && player.distanceToSqr(center) <= radius * radius
            && !targets.contains(player)) {
            targets.add(player);
        }

        int flame = MagicArmoryEnchantments.level(stack, Enchantments.FLAME);
        int windBurst = MagicArmoryEnchantments.level(
            stack,
            Enchantments.WIND_BURST
        );

        for (LivingEntity target : targets) {
            boolean hostileTarget = target != player && !player.isAlliedTo(target);
            if (hostileTarget) {
                // Ignite before damage so a lethal hit produces cooked drops.
                if (flame > 0) {
                    target.setRemainingFireTicks(Math.max(
                        target.getRemainingFireTicks(),
                        80 * flame
                    ));
                }
                target.hurt(
                    level.damageSources().indirectMagic(player, player),
                    (float) values.damage()
                );
            }
            // Match the original tome behavior: Wind Burst moves every living
            // entity in the radius, including the caster and allies.
            if (windBurst > 0) {
                pushFromCenter(target, center, windBurst);
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                center.x,
                center.y + 0.35D,
                center.z,
                Math.max(32, (int) Math.ceil(radius * radius * 10.0D)),
                radius * 0.65D,
                radius * 0.35D,
                radius * 0.65D,
                0.08D
            );
            serverLevel.sendParticles(
                ParticleTypes.WITCH,
                center.x,
                center.y + 0.2D,
                center.z,
                Math.max(12, (int) Math.ceil(radius * 4.0D)),
                radius * 0.35D,
                radius * 0.2D,
                radius * 0.35D,
                0.03D
            );
            spawnEnchantmentParticles(
                serverLevel,
                center,
                radius,
                flame,
                windBurst
            );
        }

        BlockPos soundPos = BlockPos.containing(center);
        if (flame > 0) {
            level.playSound(
                null,
                soundPos,
                SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS,
                0.8F,
                1.0F
            );
        }
        if (windBurst > 0) {
            level.playSound(
                null,
                soundPos,
                SoundEvents.BREEZE_WHIRL,
                SoundSource.PLAYERS,
                0.9F,
                1.0F + windBurst * 0.08F
            );
        }

        level.playSound(
            null,
            player.blockPosition(),
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.PLAYERS,
            1.0F,
            1.15F
        );
        level.playSound(
            null,
            BlockPos.containing(center),
            SoundEvents.AMETHYST_BLOCK_CHIME,
            SoundSource.PLAYERS,
            1.0F,
            0.9F
        );
    }

    private static void spawnEnchantmentParticles(
        ServerLevel level,
        Vec3 center,
        double radius,
        int flameLevel,
        int windBurstLevel
    ) {
        if (flameLevel > 0) {
            level.sendParticles(
                ParticleTypes.FLAME,
                center.x,
                center.y + 0.15D,
                center.z,
                18 + flameLevel * 8,
                radius * 0.65D,
                radius * 0.2D,
                radius * 0.65D,
                0.025D
            );
            level.sendParticles(
                ParticleTypes.SMALL_FLAME,
                center.x,
                center.y + 0.35D,
                center.z,
                12 + flameLevel * 5,
                radius * 0.45D,
                radius * 0.35D,
                radius * 0.45D,
                0.04D
            );
        }

        if (windBurstLevel > 0) {
            level.sendParticles(
                windBurstLevel >= 3
                    ? ParticleTypes.GUST_EMITTER_LARGE
                    : ParticleTypes.GUST_EMITTER_SMALL,
                center.x,
                center.y + 0.1D,
                center.z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
            );
            level.sendParticles(
                ParticleTypes.GUST,
                center.x,
                center.y + 0.25D,
                center.z,
                8 + windBurstLevel * 6,
                radius * 0.7D,
                radius * 0.3D,
                radius * 0.7D,
                0.05D
            );
        }
    }

    private static void invokePotion(
        ServerLevel level,
        Player player,
        ItemStack potionStack,
        Vec3 center
    ) {
        ThrownPotion potion = new ThrownPotion(
            level,
            center.x,
            center.y + 0.9D,
            center.z
        );
        potion.setOwner(player);
        potion.setItem(potionStack);
        potion.setDeltaMovement(0.0D, -0.35D, 0.0D);
        level.addFreshEntity(potion);
    }

    private static void teleportPlayer(
        ServerLevel level,
        Player player,
        Vec3 destination
    ) {
        level.sendParticles(
            ParticleTypes.PORTAL,
            player.getX(),
            player.getY() + 1.0D,
            player.getZ(),
            32,
            0.45D,
            0.8D,
            0.45D,
            0.12D
        );
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(
                level,
                destination.x,
                destination.y,
                destination.z,
                serverPlayer.getYRot(),
                serverPlayer.getXRot()
            );
        } else {
            player.teleportTo(destination.x, destination.y, destination.z);
        }
        level.sendParticles(
            ParticleTypes.PORTAL,
            destination.x,
            destination.y + 1.0D,
            destination.z,
            32,
            0.45D,
            0.8D,
            0.45D,
            0.12D
        );
        level.playSound(
            null,
            BlockPos.containing(destination),
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.PLAYERS,
            1.0F,
            1.0F
        );
    }

    private static Optional<Vec3> findSafeTeleport(
        ServerLevel level,
        Player player,
        Vec3 desired
    ) {
        BlockPos origin = BlockPos.containing(desired);
        for (int vertical = 3; vertical >= -5; vertical--) {
            for (int radius = 0; radius <= 2; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (radius > 0
                            && Math.abs(dx) != radius
                            && Math.abs(dz) != radius) {
                            continue;
                        }
                        BlockPos feet = origin.offset(dx, vertical, dz);
                        if (feet.getY() <= level.getMinBuildHeight()
                            || feet.getY() + 2 >= level.getMaxBuildHeight()) {
                            continue;
                        }
                        BlockPos floorPos = feet.below();
                        BlockState floor = level.getBlockState(floorPos);
                        VoxelShape floorShape = floor.getCollisionShape(level, floorPos);
                        if (floorShape.isEmpty()
                            || !level.getFluidState(feet).isEmpty()
                            || !level.getFluidState(feet.above()).isEmpty()) {
                            continue;
                        }
                        double x = feet.getX() + 0.5D;
                        double y = feet.getY();
                        double z = feet.getZ() + 0.5D;
                        AABB moved = player.getBoundingBox().move(
                            x - player.getX(),
                            y - player.getY(),
                            z - player.getZ()
                        );
                        if (level.noCollision(player, moved)) {
                            return Optional.of(new Vec3(x, y, z));
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static void invokeArrowRain(
        ServerLevel level,
        Player player,
        ItemStack catalyst,
        Vec3 center
    ) {
        RandomSource random = level.random;
        double radius = MagicArmoryConfig.tome(MagicFocusTier.FLINT_LEATHER).radius();
        int count = MagicArmoryConfig.INVOCATION_RULES.arrowCount();
        double minHeight = MagicArmoryConfig.INVOCATION_RULES.arrowMinHeight();
        double maxHeight = Math.max(
            minHeight,
            MagicArmoryConfig.INVOCATION_RULES.arrowMaxHeight()
        );
        int lifetime = MagicArmoryConfig.INVOCATION_RULES.arrowLifetimeTicks();

        for (int index = 0; index < count; index++) {
            Vec3 spawn = randomPoint(center, radius * 0.85D, random).add(
                0.0D,
                Mth.lerp(random.nextDouble(), minHeight, maxHeight),
                0.0D
            );
            Vec3 target = randomPoint(center, radius, random).add(
                0.0D,
                0.15D,
                0.0D
            );
            AbstractArrow arrow;
            if (catalyst.is(Items.SPECTRAL_ARROW)) {
                arrow = new InvocationSpectralArrow(
                    level,
                    spawn.x,
                    spawn.y,
                    spawn.z,
                    catalyst.copyWithCount(1),
                    lifetime
                );
            } else {
                arrow = new InvocationArrow(
                    level,
                    spawn.x,
                    spawn.y,
                    spawn.z,
                    catalyst.copyWithCount(1),
                    lifetime
                );
            }
            arrow.setOwner(player);
            Vec3 direction = target.subtract(spawn).normalize();
            arrow.shoot(
                direction.x,
                direction.y,
                direction.z,
                1.75F,
                2.5F
            );
            level.addFreshEntity(arrow);
        }
    }

    private static Vec3 randomPoint(
        Vec3 center,
        double radius,
        RandomSource random
    ) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = Math.sqrt(random.nextDouble()) * radius;
        return new Vec3(
            center.x + Math.cos(angle) * distance,
            center.y,
            center.z + Math.sin(angle) * distance
        );
    }

    private static void invokeFireworks(
        ServerLevel level,
        Player player,
        ItemStack rocket,
        Vec3 center
    ) {
        RandomSource random = level.random;
        int count = MagicArmoryConfig.INVOCATION_RULES.fireworkCount();
        for (int index = 0; index < count; index++) {
            double x = center.x + (random.nextDouble() - 0.5D) * 1.5D;
            double z = center.z + (random.nextDouble() - 0.5D) * 1.5D;
            FireworkRocketEntity firework = new FireworkRocketEntity(
                level,
                rocket.copyWithCount(1),
                player,
                x,
                center.y + 0.25D,
                z,
                true
            );
            firework.setDeltaMovement(
                (random.nextDouble() - 0.5D) * 0.12D,
                0.45D + random.nextDouble() * 0.18D,
                (random.nextDouble() - 0.5D) * 0.12D
            );
            level.addFreshEntity(firework);
        }
    }

    private static void invokeMilk(ServerLevel level, Vec3 center) {
        double radius = MagicArmoryConfig.tome(MagicFocusTier.FLINT_LEATHER).radius();
        AABB area = new AABB(center, center).inflate(radius);
        for (LivingEntity entity : level.getEntitiesOfClass(
            LivingEntity.class,
            area,
            entity -> entity.isAlive()
                && entity.distanceToSqr(center) <= radius * radius
        )) {
            entity.removeAllEffects();
        }
        level.sendParticles(
            ParticleTypes.WHITE_SMOKE,
            center.x,
            center.y + 0.3D,
            center.z,
            42,
            radius * 0.6D,
            radius * 0.3D,
            radius * 0.6D,
            0.04D
        );
        level.playSound(
            null,
            BlockPos.containing(center),
            SoundEvents.COW_MILK,
            SoundSource.PLAYERS,
            1.0F,
            0.85F
        );
    }

    private static void pushFromCenter(
        LivingEntity target,
        Vec3 center,
        int level
    ) {
        Vec3 direction = target.position().subtract(center);
        if (direction.lengthSqr() < 0.0001D) {
            direction = new Vec3(0.0D, 1.0D, 0.0D);
        } else {
            direction = direction.normalize();
        }
        double horizontal = 0.65D + level * 0.30D;
        double vertical = 0.20D + level * 0.12D;
        target.push(
            direction.x * horizontal,
            Math.max(vertical, direction.y * horizontal),
            direction.z * horizontal
        );
        target.hurtMarked = true;
    }

    private static void consumeCatalyst(
        Player player,
        ItemStack catalyst,
        Item remainder
    ) {
        if (player.getAbilities().instabuild) {
            return;
        }
        catalyst.shrink(1);
        if (remainder == Items.AIR) {
            return;
        }
        ItemStack result = new ItemStack(remainder);
        if (catalyst.isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, result);
        } else if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
    }

    private static Item invocationRemainder(ItemStack catalyst) {
        if (catalyst.is(Items.POTION)) {
            return Items.GLASS_BOTTLE;
        }
        if (catalyst.is(Items.MILK_BUCKET)) {
            return Items.BUCKET;
        }
        return Items.AIR;
    }

    private static void addInvocationCooldown(Player player) {
        int ticks = MagicArmoryConfig.INVOCATION_RULES.cooldownTicks();
        for (MagicTomeItem tome : MagicArmoryItems.tomes()) {
            player.getCooldowns().addCooldown(tome, ticks);
        }
    }

    private static boolean isSupportedCatalyst(ItemStack stack) {
        return !stack.isEmpty()
            && (isPotion(stack)
                || stack.is(Items.ENDER_PEARL)
                || stack.is(Items.TNT)
                || isArrow(stack)
                || stack.is(Items.FIREWORK_ROCKET)
                || stack.is(Items.MILK_BUCKET));
    }

    private static boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION)
            || stack.is(Items.SPLASH_POTION)
            || stack.is(Items.LINGERING_POTION);
    }

    private static boolean isArrow(ItemStack stack) {
        return stack.is(Items.ARROW)
            || stack.is(Items.SPECTRAL_ARROW)
            || stack.is(Items.TIPPED_ARROW);
    }

    private Vec3 targetPoint(
        Level level,
        Player player,
        ItemStack stack,
        CastValues values
    ) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(
            player.getLookAngle().scale(castRange(stack, values))
        );
        BlockHitResult hit = level.clip(new ClipContext(
            start,
            end,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));
        return hit.getType() == HitResult.Type.MISS
            ? end
            : hit.getLocation();
    }

    private double tomeRadius() {
        return MagicArmoryConfig.tome(tier).radius();
    }

    @Override
    protected void appendSpecialTooltip(
        ItemStack stack,
        List<Component> tooltip,
        CastValues values
    ) {
        tooltip.add(line(
            "tooltip.mysticism_magic_armory.radius",
            format(tomeRadius()),
            ChatFormatting.DARK_PURPLE
        ));
    }

    @Override
    public boolean supportsEnchantment(
        ItemStack stack,
        Holder<Enchantment> enchantment
    ) {
        return enchantment.is(Enchantments.FLAME)
            || enchantment.is(Enchantments.WIND_BURST)
            || enchantment.is(Enchantments.BREACH)
            || MagicArmoryEnchantments.is(
                enchantment,
                MagicArmoryEnchantments.INVOCATION
            )
            || super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean isPrimaryItemFor(
        ItemStack stack,
        Holder<Enchantment> enchantment
    ) {
        return enchantment.is(Enchantments.FLAME)
            || enchantment.is(Enchantments.WIND_BURST)
            || enchantment.is(Enchantments.BREACH)
            || MagicArmoryEnchantments.is(
                enchantment,
                MagicArmoryEnchantments.INVOCATION
            )
            || super.isPrimaryItemFor(stack, enchantment);
    }
}
