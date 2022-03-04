package com.doo.xhp.mixin;

import com.doo.xhp.renderer.HpRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = EntityRenderDispatcher.class, priority = Integer.MAX_VALUE)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    @Final
    private TextRenderer textRenderer;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;doesRenderOnFire()Z"), method = "render")
    private <E extends Entity> void renderH(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof LivingEntity) {
            HpRenderer.render(matrices, ((LivingEntity) entity), vertexConsumers, light, textRenderer, (EntityRenderDispatcher) (Object) this, entity.hasCustomName() || entity.shouldRenderName());
        }
    }
}
