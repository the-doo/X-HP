package com.doo.xhp.mixin;

import com.doo.xhp.interfaces.Critable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Attack crit log
 */
@Mixin(PlayerEntity.class)
public abstract class PlayEntityMixin implements Critable {

    private boolean isCrit;

    @ModifyVariable(at = @At(value = "STORE", ordinal = 2), method = "attack", ordinal = 0)
    private float setCrit(float f) {
        isCrit = true;
        return f;
    }

    @Inject(at = @At(value = "HEAD"), method = "attack")
    private void resetCrit(Entity target, CallbackInfo ci) {
        isCrit = false;
    }

    @Override
    public boolean isCrit() {
        return isCrit;
    }
}