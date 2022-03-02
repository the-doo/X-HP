package com.doo.xhp.mixin;

import com.doo.xhp.renderer.HpRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
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
@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class EntityRenderMixin<T extends Entity> {

    @Shadow
    @Final
    private TextRenderer textRenderer;

    @Shadow
    @Final
    protected EntityRenderDispatcher dispatcher;

    @Shadow
    protected abstract boolean hasLabel(T entity);

    @Inject(at = @At(value = "HEAD"), method = "render")
    private void renderH(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof LivingEntity) {
            HpRenderer.render(matrices, ((LivingEntity) entity), vertexConsumers, light, textRenderer, dispatcher, hasLabel(entity));
        }
    }
}
