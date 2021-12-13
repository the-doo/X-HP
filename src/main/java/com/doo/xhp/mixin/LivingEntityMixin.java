package com.doo.xhp.mixin;

import com.doo.xhp.XHP;
import com.doo.xhp.util.NetworkUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    protected float lastDamageTaken;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At(value = "TAIL"), method = "damage")
    private void setDamageT(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        float damage = this.lastDamageTaken;
        if (XHP.XOption.damage && damage > 0) {
            NetworkUtil.packetSender(getId(), damage, source.getAttacker()).accept(PlayerLookup.tracking((ServerWorld) world, getBlockPos()));
        }
    }
}