package com.doo.xhp.mixin;

import com.doo.xhp.util.NetworkUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Angerable.class)
public interface AngerableMixin {

    @Shadow
    @Nullable LivingEntity getTarget();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/Angerable;setAngryAt(Ljava/util/UUID;)V"), method = "tickAngerLogic")
    private void setAnger(ServerWorld world, boolean angerPersistent, CallbackInfo ci) {
        LivingEntity attacker = getTarget();
        if (attacker instanceof ServerPlayerEntity) {
            NetworkUtil.angerAt((LivingEntity) this, (ServerPlayerEntity) attacker, true);
        }
    }

    @Inject(at = @At("HEAD"), method = "stopAnger")
    private void resetAnger(CallbackInfo ci) {
        LivingEntity attacker = getTarget();
        if (attacker instanceof ServerPlayerEntity) {
            NetworkUtil.angerAt((LivingEntity) this, (ServerPlayerEntity) attacker, false);
        }
    }
}
