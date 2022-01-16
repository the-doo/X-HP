package com.doo.xhp;

import com.doo.xhp.config.Config;
import com.doo.xhp.config.XOption;
import com.doo.xhp.renderer.HpRenderer;
import com.doo.xhp.util.NetworkUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class XHP implements ModInitializer {

    public static final String ID = "xhp";

    public static final Identifier ON_DAMAGE_PACKET = new Identifier(ID);

    public static XOption XOption = new XOption();

    @Override
    public void onInitialize() {
        // 加载配置
        XOption = Config.read(ID, XOption.class, XOption);
        // 注册监听
        NetworkUtil.registerPacketAcceptor();


        HudRenderCallback.EVENT.register(((matrixStack, tickDelta) -> {
            if (!XOption.enabled || MinecraftClient.getInstance().cameraEntity == null) {
                return;
            }

            LivingEntity target = HpRenderer.focusTarget(MinecraftClient.getInstance().cameraEntity);
            if (target == null) {
                return;
            }

            HpRenderer.renderTips(matrixStack, target);
        }));
    }
}
