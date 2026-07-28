package com.xevaryth.mysticism.magicarmory.item;

import com.xevaryth.mysticism.magicarmory.config.MagicArmoryConfig.CastValues;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class BeamCastingItem extends ManaCastingItem {
    protected BeamCastingItem(
        Item.Properties properties,
        int enchantmentValue,
        Supplier<Ingredient> repairIngredient
    ) {
        super(properties, enchantmentValue, repairIngredient);
    }

    protected abstract double particlesPerBlock();

    @Override
    protected void cast(
        Level level,
        Player player,
        ItemStack stack,
        CastValues values
    ) {
        BeamTrace trace = traceBeam(level, player, stack, values);
        Vec3 visualEnd = trace.end();
        if (!trace.targets().isEmpty()) {
            BeamTarget target = trace.targets().getFirst();
            damageTarget(level, player, target.target(), (float) values.damage());
            visualEnd = target.hitPoint();
        }
        if (level instanceof ServerLevel serverLevel) {
            spawnTrail(
                serverLevel,
                trace.start(),
                visualEnd,
                trailParticle(stack),
                particlesPerBlock()
            );
        }
    }

    protected abstract ParticleOptions trailParticle(ItemStack stack);

    protected final BeamTrace traceBeam(
        Level level,
        Player player,
        ItemStack stack,
        CastValues values
    ) {
        return traceBeam(level, player, stack, values, player.getLookAngle());
    }

    protected final BeamTrace traceBeam(
        Level level,
        Player player,
        ItemStack stack,
        CastValues values,
        Vec3 castDirection
    ) {
        Vec3 start = player.getEyePosition();
        Vec3 direction = castDirection.normalize();
        double range = castRange(stack, values);
        Vec3 maximumEnd = start.add(direction.scale(range));
        BlockHitResult blockHit = level.clip(new ClipContext(
            start,
            maximumEnd,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS
            ? maximumEnd
            : blockHit.getLocation();

        AABB searchArea = player.getBoundingBox()
            .expandTowards(direction.scale(range))
            .inflate(1.5D);
        List<BeamTarget> targets = new ArrayList<>();
        for (LivingEntity target : level.getEntitiesOfClass(
            LivingEntity.class,
            searchArea,
            entity -> entity != player
                && entity.isAlive()
                && entity.isPickable()
                && !entity.isSpectator()
                && !player.isAlliedTo(entity)
        )) {
            Optional<Vec3> intersection = target.getBoundingBox()
                .inflate(0.3D)
                .clip(start, end);
            intersection.ifPresent(point -> targets.add(new BeamTarget(
                target,
                point,
                start.distanceToSqr(point)
            )));
        }
        targets.sort(Comparator.comparingDouble(BeamTarget::distanceSqr));
        return new BeamTrace(start, end, targets);
    }

    protected final void damageTarget(
        Level level,
        Player player,
        LivingEntity target,
        float damage
    ) {
        target.hurt(
            level.damageSources().indirectMagic(player, player),
            damage
        );
    }

    protected final void spawnTrail(
        ServerLevel level,
        Vec3 start,
        Vec3 end,
        ParticleOptions trailParticle,
        double density
    ) {
        Vec3 path = end.subtract(start);
        int steps = Math.max(1, (int) Math.ceil(path.length() * density));
        Vec3 step = path.scale(1.0D / steps);
        for (int index = 1; index <= steps; index++) {
            Vec3 point = start.add(step.scale(index));
            level.sendParticles(
                trailParticle,
                point.x,
                point.y,
                point.z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
            );
        }
    }

    protected record BeamTrace(
        Vec3 start,
        Vec3 end,
        List<BeamTarget> targets
    ) {}

    protected record BeamTarget(
        LivingEntity target,
        Vec3 hitPoint,
        double distanceSqr
    ) {}
}
