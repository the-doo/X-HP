package com.doo.xhp.mixin;

import com.doo.xhp.renderer.HpRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRenderMixin {

    @Inject(at = @At(value = "HEAD"), method = "render*")
    private void renderR(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack,
                         VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        if (HpRenderer.canRender(livingEntity)) {
            HpRenderer.render(matrixStack, livingEntity);
        }
    }
}
