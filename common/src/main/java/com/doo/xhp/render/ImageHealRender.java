package com.doo.xhp.render;

import com.doo.xhp.XHP;
import com.doo.xhp.enums.*;
import com.doo.xhp.interfaces.WithOption;
import com.doo.xhp.screen.MenuScreen;
import com.doo.xhp.util.ConfigUtil;
import com.google.gson.JsonArray;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ImageHealRender extends IconHealRender {

    private static final ResourceLocation BACK_ID = new ResourceLocation(XHP.MOD_ID, "textures/back_upload.png");
    private static final ResourceLocation FILL_ID = new ResourceLocation(XHP.MOD_ID, "textures/fill_upload.png");
    private static final Supplier<Stream<?>> STREAM_SUPPLIER = () -> {
        File file = FileSystems.getDefault().getPath("resourcepacks").toFile();
        if (!file.exists()) {
            return Stream.empty();
        }

        String[] list = file.list();
        if (list == null || list.length < 1) {
            return Stream.empty();
        }

        return Arrays.stream(list).filter(p -> p.endsWith(".png"));
    };

    private static final String BACK_FILE_KEY = "back_file";
    private static final String FILL_FILE_KEY = "fill_file";
    private static final String DRAW_KEY = "draw_key";

    private ImageDrawType drawType;
    private NativeImage back;
    private NativeImage fill;
    private float backXScala;
    private float backYScala;
    private float fillXScala;
    private float fillYScala;


    public ImageHealRender() {
        weight = 80;
        height = 16;

        drawType = ImageDrawType.COVER;
        position = HealthTextPosition.BOTTOM_RIGHT;

        options.addProperty(P_KEY, position.name());
        options.addProperty(TEXT_KEY, HealthTextGetters.CURRENT_AND_MAX.name());
        options.addProperty(DRAW_KEY, ImageDrawType.COVER.name());
        options.add(BACK_FILE_KEY, new JsonArray());
        options.add(FILL_FILE_KEY, new JsonArray());
    }

    @Override
    public void registerOpt() {
        super.registerOpt();

        String name = HealthRenders.name(this);
        MenuScreen.register(MenuOptType.LIST, name, BACK_FILE_KEY, STREAM_SUPPLIER);
        MenuScreen.register(MenuOptType.LIST, name, FILL_FILE_KEY, STREAM_SUPPLIER);
        MenuScreen.register(MenuOptType.ENUM, name, DRAW_KEY, ImageDrawType.class);
    }

    @Override
    public void reloadOpt() {
        super.reloadOpt();

        drawType = WithOption.enumV(options, DRAW_KEY, ImageDrawType.class)
                .orElse(ImageDrawType.COVER);

        // only first
        JsonArray array = options.get(BACK_FILE_KEY).getAsJsonArray();
        if (array.size() > 1) {
            for (int i = 1; i < array.size() - 1; i++) {
                array.remove(i);
            }
        }
        array = options.get(FILL_FILE_KEY).getAsJsonArray();
        if (array.size() > 1) {
            for (int i = 1; i < array.size() - 1; i++) {
                array.remove(i);
            }
        }
    }

    public void reloadImage(Minecraft client) {
        clear();

        NativeImage image = ConfigUtil.readImage(first(BACK_FILE_KEY));
        if (image != null) {
            back = image;
        }

        NativeImage image2 = ConfigUtil.readImage(first(FILL_FILE_KEY));
        if (image2 != null) {
            fill = image2;
        }

        resetSize(client);
    }

    private void clear() {
        back = null;
        fill = null;
        backXScala = 0;
        backYScala = 0;
        fillXScala = 0;
        fillYScala = 0;
    }

    private String first(String key) {
        JsonArray array = options.get(key).getAsJsonArray();
        if (array.isEmpty()) {
            return "";
        }

        return array.get(0).getAsString();
    }

    private void resetSize(Minecraft client) {
        TextureManager manager = client.getTextureManager();
        if (back != null) {
            backXScala = 1F * weight / back.getWidth();
            backYScala = 1F * height / back.getHeight();
            manager.release(BACK_ID);
            manager.register(BACK_ID, new DynamicTexture(back));
        }

        if (fill != null) {
            fillXScala = 1F * weight / fill.getWidth();
            fillYScala = 1F * height / fill.getHeight();
            manager.release(FILL_ID);
            manager.register(FILL_ID, new DynamicTexture(fill));
        }
    }

    protected void renderCurrent(PoseStack posed, double process, int endX, int endY, LivingEntity living) {
        RenderSystem.enableDepthTest();
        if (back != null && (drawType == ImageDrawType.COVER || process < 1)) {
            int startX = drawType == ImageDrawType.COVER ? 0 : (int) (process * back.getWidth());
            posed.pushPose();
            posed.scale(backXScala, backYScala, 1);
            RenderSystem.setShaderTexture(0, BACK_ID);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            GuiComponent.blit(posed, startX, 0, startX, 0, back.getWidth() - startX, back.getHeight(), back.getWidth(), back.getHeight());
            posed.popPose();
        }

        if (fill != null) {
            posed.pushPose();
            posed.scale(fillXScala, fillYScala, 1);
            RenderSystem.setShaderTexture(0, FILL_ID);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            GuiComponent.blit(posed, 0, 0, 0, 0, (int) (fill.getWidth() * process), fill.getHeight(), fill.getWidth(), fill.getHeight());
            posed.popPose();
        }
    }
}
