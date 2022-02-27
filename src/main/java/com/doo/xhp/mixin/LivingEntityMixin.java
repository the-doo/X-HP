package com.doo.xhp.mixin;

import com.doo.xhp.interfaces.Damageable;
import com.doo.xhp.util.HpUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Damageable {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    @Final
    private static TrackedData<Float> HEALTH;

    @Shadow
    public abstract void setAttacker(@Nullable LivingEntity attacker);

    private float preHealth = 0;
    private boolean damageIsCrit = false;
    private final List<HpUtil.DamageR> damages = new ArrayList<>();

    @Inject(at = @At(value = "HEAD"), method = "damage")
    private void logCrit(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient() || amount <= 0) {
            return;
        }
        damageIsCrit = HpUtil.isCrit(source);
        if (source.getAttacker() instanceof LivingEntity) {
            setAttacker((LivingEntity) source.getAttacker());
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "getHealth")
    private void getHealthH(CallbackInfoReturnable<Float> cir) {
        if (!world.isClient()) {
            return;
        }

        float f = dataTracker.get(HEALTH);
        if (f == preHealth) {
            return;
        }

        float damage = f - preHealth;
        preHealth = f;
        if (damage == 0 || f == 0) {
            return;
        }

        float x = (random.nextBoolean() ? 1 : -1) * random.nextFloat();
        float y = random.nextFloat();
        damages.add(new HpUtil.DamageR(damage, world.getTime(), damageIsCrit, x, y));

        damageIsCrit = false;
    }

    @Override
    public List<HpUtil.DamageR> getDamageList() {
        return damages;
    }
}