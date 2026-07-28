package com.xevaryth.mysticism.magicarmory.item;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** A normal or tipped arrow that can never be collected or remain embedded. */
public final class InvocationArrow extends Arrow {
    private final int maximumLifetime;
    private int invocationAge;

    public InvocationArrow(
        Level level,
        double x,
        double y,
        double z,
        ItemStack projectile,
        int maximumLifetime
    ) {
        super(level, x, y, z, projectile, ItemStack.EMPTY);
        this.maximumLifetime = maximumLifetime;
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();
        invocationAge++;
        if (!level().isClientSide() && invocationAge >= maximumLifetime) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!level().isClientSide()) {
            discard();
        }
    }
}
