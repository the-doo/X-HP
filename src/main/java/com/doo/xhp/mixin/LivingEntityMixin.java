package com.doo.xhp.mixin;

import com.doo.xhp.XHP;
import com.doo.xhp.util.NetworkUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"), method = "applyDamage")
    private void sendDamagePacket(DamageSource source, float amount, CallbackInfo ci) {
        if (XHP.XOption.damage) {
            LivingEntity entity = (LivingEntity) (Object) this;
            NetworkUtil.packetSender(entity.getId(), amount, source.getAttacker())
                    .accept(entity.world.getEntitiesByClass(
                            ServerPlayerEntity.class,
                            entity.getBoundingBox().expand(XHP.XOption.distance),
                            l -> l.canSee(entity)));
        }
    }
}