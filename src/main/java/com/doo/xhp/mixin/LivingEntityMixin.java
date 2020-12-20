package com.doo.xhp.mixin;

import com.doo.xhp.XHP;
import com.doo.xhp.util.HpUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {


    @Shadow
    protected float lastDamageTaken;

    @Shadow
    private long lastDamageTime;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At(value = "TAIL"), method = "damage", cancellable = true)
    private void setDamageT(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (XHP.option.damage && this.lastDamageTaken > 0) {
            HpUtil.set(getEntityId(), getWidth(), getHeight(), source.getAttacker(), this.lastDamageTaken, this.lastDamageTime);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "tick")
    private void tickH(CallbackInfo info) {
        if (XHP.option.damage && EntityPose.DYING.equals(this.getPose())) {
            HpUtil.remove(getEntityId());
        }
    }
}