package com.doo.xhp.forge;

import com.doo.xhp.XHP;
import com.doo.xhp.screen.MenuScreen;
import com.doo.xhp.util.HealthRenderUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

@Mod(XHP.MOD_ID)
public class XHPForge {
    public XHPForge() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = XHP.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        private ClientModEvents() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            XHP.init();
            HealthRenderUtil.onClientStarted(Minecraft.getInstance());

            Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, EXAMPLE_MAPPING.get());
        }

        // Key mapping is lazily initialized so it doesn't exist until it is registered
        public static final Lazy<KeyMapping> EXAMPLE_MAPPING = Lazy.of(() -> new KeyMapping(
                "keybinding.key.x_hp.name",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "keybinding.category.x_hp.name"));
    }

    // Event is on the Forge event bus only on the physical client
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // Only call code once as the tick event is called twice every tick
            while (ClientModEvents.EXAMPLE_MAPPING.get().consumeClick()) {
                // Execute logic to perform on click here
                MenuScreen.open(Minecraft.getInstance());
            }

            HealthRenderUtil.onClientEndTick(Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public void onGui(RenderGameOverlayEvent.Post event) {
        HealthRenderUtil.renderTips(event.getMatrixStack());
    }
}