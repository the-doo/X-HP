package com.doo.xhp;

import com.doo.xhp.config.Config;
import com.doo.xhp.config.XOption;
import com.doo.xhp.renderer.HpRenderer;
import com.doo.xhp.util.HpUtil;
import com.doo.xhp.util.NetworkUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class XHP implements ModInitializer {

    public static final String ID = "xhp";

    public static final Identifier ON_DAMAGE_PACKET = new Identifier(ID + "_damage");

    public static final Identifier ANGER_PACKET = new Identifier(ID + "_anger");

    public static XOption XOption = new XOption();

    @Override
    public void onInitialize() {
        // Loading config
        XOption = Config.read(ID, XOption.class, XOption);

        // Regis Damage Pack
        NetworkUtil.registerDamagePacketAcceptor();
        // Regis Anger Pack
        NetworkUtil.registerAngerPacketAcceptor();


        // regis event
        HudRenderCallback.EVENT.register(((matrixStack, tickDelta) -> {
            if (!XOption.enabled || MinecraftClient.getInstance().cameraEntity == null) {
                return;
            }

            LivingEntity target = HpUtil.focusTarget(MinecraftClient.getInstance().cameraEntity);
            if (target == null) {
                return;
            }

            HpRenderer.renderTips(matrixStack, target);
        }));
    }
}
