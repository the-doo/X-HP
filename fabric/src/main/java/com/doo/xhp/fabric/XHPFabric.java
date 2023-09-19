package com.doo.xhp.fabric;

import com.doo.xhp.XHP;
import com.doo.xhp.util.HealthRenderUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class XHPFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        XHP.init();

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
                HealthRenderUtil.renderTips(drawContext));

        ClientLifecycleEvents.CLIENT_STARTED.register(HealthRenderUtil::onClientStarted);

        ClientTickEvents.END_CLIENT_TICK.register(HealthRenderUtil::onClientEndTick);
    }
}