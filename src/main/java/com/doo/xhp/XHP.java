package com.doo.xhp;

import com.doo.xhp.config.Config;
import com.doo.xhp.config.XOption;
import com.doo.xhp.renderer.TipsRenderer;
import com.doo.xhp.util.HpUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

public class XHP implements ClientModInitializer {

    public static final String ID = "xhp";

    public static XOption XOption = new XOption();

    @Override
    public void onInitializeClient() {
        // Loading config
        XOption = Config.read(ID, XOption.class, XOption);

//        // regis event
//        HudRenderCallback.EVENT.register(((matrixStack, tickDelta) -> {
//            if (!XOption.enabled || MinecraftClient.getInstance().cameraEntity == null) {
//                return;
//            }
//
//            LivingEntity target = HpUtil.focusTarget(MinecraftClient.getInstance().cameraEntity);
//            if (target == null) {
//                return;
//            }
//
//            if (HpUtil.mustCheck(target)) {
//                TipsRenderer.INSTANCE.tips(matrixStack, target);
//            }
//        }));
    }
}
