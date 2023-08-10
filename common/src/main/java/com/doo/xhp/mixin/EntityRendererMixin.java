package com.doo.xhp.mixin;

import com.doo.xhp.util.HealthRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    protected abstract boolean shouldShowName(Entity arg);

    @Inject(method = "render", at = @At("RETURN"))
    private void injectRenderT(Entity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (entity instanceof LivingEntity e && e.getType().getCategory() != MobCategory.MISC) {
            float baseY = e.getBbHeight() + 0.5F + (shouldShowName(e) ? 0.35F : 0);
            HealthRenderUtil.render(poseStack, entityRenderDispatcher, multiBufferSource, e, baseY, i);
        }
    }
}
