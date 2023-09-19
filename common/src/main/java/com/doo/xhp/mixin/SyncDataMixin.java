package com.doo.xhp.mixin;

import com.doo.xhp.interfaces.LivingEntityAccessor;
import com.doo.xhp.render.DamageRender;
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

@Mixin(SynchedEntityData.class)
public abstract class SyncDataMixin {

    @Shadow
    @Final
    private Entity entity;

    @Unique
    private float lastHealth;

    @Inject(method = "assignValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData$DataItem;setValue(Ljava/lang/Object;)V"))
    private <T> void injectSetHealT(SynchedEntityData.DataItem<T> dataItem, SynchedEntityData.DataValue<?> dataValue, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity e)) {
            return;
        }

        if (LivingEntityAccessor.isPoseId(e, dataValue.id())) {
            putDamage(e, -lastHealth);
        } else if (LivingEntityAccessor.isHealId(e, dataValue.id())) {
            float change = (lastHealth = (Float) dataValue.value()) - (Float) dataItem.value().value();
            putDamage(e, change);
        }
    }

    @Unique
    private void putDamage(LivingEntity e, float damage) {
        if (damage < 0.0001 && damage >= 0 || damage <= 0 && damage > -0.0001) {
            return;
        }

        DamageRender.put(e, damage);
    }
}
