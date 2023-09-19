package com.doo.xhp.mixin;

import com.doo.xhp.interfaces.DamageAccessor;
import com.doo.xhp.interfaces.LivingEntityAccessor;
import com.doo.xhp.render.DamageRender;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Mixin(SynchedEntityData.class)
public abstract class SyncDataMixin implements DamageAccessor {

    @Shadow
    @Final
    private Entity entity;

    @Unique
    private Cache<Integer, Float> x_HP$lastDamageCached = CacheBuilder.newBuilder()
            .expireAfterWrite(800, TimeUnit.MILLISECONDS)
            .build();

    @Unique
    private float lastHealth;

    @Inject(method = "assignValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData$DataItem;setValue(Ljava/lang/Object;)V"))
    private <T> void injectSetHealT(SynchedEntityData.DataItem<T> dataItem, SynchedEntityData.DataItem<?> dataItem2, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        LivingEntity e = (LivingEntity) entity;
        if (LivingEntityAccessor.isPoseId(e, dataItem.getAccessor().getId())) {
            putDamage(e, -lastHealth);
        } else if (LivingEntityAccessor.isHealId(e, dataItem.getAccessor().getId())) {
            float change = (lastHealth = (Float) dataItem2.getValue()) - (Float) dataItem.getValue();
            putDamage(e, change);
        }
    }

    @Unique
    private void putDamage(LivingEntity e, float damage) {
        if (damage < 0.0001 && damage >= 0 || damage <= 0 && damage > -0.0001) {
            return;
        }

        x_HP$lastDamageCached.put(entity.tickCount, damage);
        DamageRender.put(e, damage);
    }

    @Override
    public Map<Integer, Float> x_HP$lastDamageMap() {
        return x_HP$lastDamageCached.asMap();
    }
}
