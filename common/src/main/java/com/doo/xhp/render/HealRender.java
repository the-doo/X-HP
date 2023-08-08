package com.doo.xhp.render;

import com.doo.xhp.enums.HealthRenders;
import com.doo.xhp.enums.HealthTextGetters;
import com.doo.xhp.enums.MenuOptType;
import com.doo.xhp.interfaces.DamageAccessor;
import com.doo.xhp.interfaces.WithOption;
import com.doo.xhp.screen.MenuScreen;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.joml.Quaternionf;

import static com.doo.xhp.XHP.ENABLED_KEY;

public abstract class HealRender implements WithOption {

    protected static final int GREEN_COLOR = 0xFF00FF00;
    protected static final int RED_COLOR = 0xFFFF0000;
    public static final String DAMAGE_KEY = "damage";
    public static final String DAMAGE_SPEED_KEY = "damage_speed";
    public static final String DAMAGE_COLOR_KEY = "damage_color";
    public static final String HEAL_COLOR_KEY = "heal_color";
    public static final String WRAPPER_KEY = "wrapper";
    public static final int FONT_LIGHT = 0xFF00FF;

    public final JsonObject options = new JsonObject();

    protected HealRender() {
        options.addProperty(ENABLED_KEY, true);
        options.addProperty(WRAPPER_KEY, true);
        options.addProperty(DAMAGE_KEY, true);
        options.addProperty(DAMAGE_SPEED_KEY, 4);
        options.addProperty(DAMAGE_COLOR_KEY, RED_COLOR);
        options.addProperty(HEAL_COLOR_KEY, GREEN_COLOR);
    }

    @Override
    public void registerOpt() {
        String name = HealthRenders.name(this);

        MenuScreen.register(MenuOptType.COLOR, name, DAMAGE_COLOR_KEY, 1);
        MenuScreen.register(MenuOptType.COLOR, name, HEAL_COLOR_KEY, 1);
    }

    @Override
    public JsonObject opt() {
        return options;
    }

    @Override
    public boolean enabled() {
        return options.get(ENABLED_KEY).getAsBoolean();
    }

    public final void render(PoseStack poseStack, Quaternionf quaternionf, MultiBufferSource bufferSource, LivingEntity living, float baseH, int i) {
        poseStack.pushPose();
        poseStack.translate(0.0f, baseH, 0.0f);
        poseStack.mulPose(quaternionf);
        poseStack.scale(-0.025f, -0.025f, 0.025f);

        if (needMoveCenter()) {
            poseStack.translate(-width(living) / 2F, 0, 0);
        }

        GuiGraphics graphics = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
        PoseStack posed = graphics.pose();
        posed.mulPoseMatrix(poseStack.last().pose());

        int damageStartX = renderContent(graphics, living, bufferSource, i);

        if (needDamageText()) {
            renderDamage(graphics, bufferSource, living, damageStartX, i);
        }

        graphics.flush();
        poseStack.popPose();
    }

    protected boolean needMoveCenter() {
        return true;
    }

    protected int renderContent(GuiGraphics graphics, LivingEntity living, MultiBufferSource bufferSource, int i) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int backColor = (int) (minecraft.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;
        int w = width(living);
        int h = height(living);
        double process = living.getHealth() / living.getMaxHealth();
        int processW = (int) (process * w);
        boolean friendly = friendly(living);

        // container
        renderCurrent(graphics, friendly, process, processW, h);

        int inc = -(int) (DamageAccessor.sum(living.getEntityData()) / living.getMaxHealth() * w);
        if (inc > 0) {
            renderLost(graphics, processW, processW + inc, h, living);
        } else {
            inc = 0;
        }

        if (processW + inc < w) {
            renderBack(graphics, processW + inc, w, h);
        }

        // Wrapper
        if (needWrapper()) {
            int p = 2;
            graphics.fill(-p, h + p, w + p, h, backColor);
            graphics.fill(-p, h, 0, 0, backColor);
            graphics.fill(-p, 0, w + p, -p, backColor);
            graphics.fill(w, h, w + p, 0, backColor);
        }

        renderHealthText(graphics, friendly, living, font, bufferSource, i, processW);
        return processW;
    }

    protected boolean needWrapper() {
        return WithOption.boolV(options, WRAPPER_KEY);
    }

    protected boolean needDamageText() {
        return WithOption.boolV(options, DAMAGE_KEY);
    }

    protected void renderCurrent(GuiGraphics graphics, boolean friendly, double process, int endX, int endY) {

    }

    protected void renderLost(GuiGraphics graphics, int startX, int endX, int endY, LivingEntity living) {
    }

    protected void renderBack(GuiGraphics graphics, int startX, int endX, int endY) {

    }

    protected void renderHealthText(GuiGraphics graphics, boolean friendly, LivingEntity living, Font font, MultiBufferSource bufferSource, int i, int processW) {

    }

    protected void renderDamage(GuiGraphics graphics, MultiBufferSource bufferSource, LivingEntity living, int damageStartX, int i) {
        Minecraft minecraft = Minecraft.getInstance();
        int fps = minecraft.getFps();
        Font font = minecraft.font;
        int current = living.tickCount;
        PoseStack posed = graphics.pose();
        double speed = WithOption.doubleV(options, DAMAGE_SPEED_KEY);
        double height = height(living);
        double xSpeed = 200 * speed;
        double ySpeed = height * 20 * speed;
        int color1 = (int) WithOption.doubleV(options, DAMAGE_COLOR_KEY);
        int color2 = (int) WithOption.doubleV(options, HEAL_COLOR_KEY);
        boolean id = living.getId() % 2 == 0;

        DamageAccessor.foreach(living.getEntityData(), (tick, value) -> {
            posed.pushPose();
            float p = 1F * (current - tick) / fps;

            int color = color2;
            int fontX = damageStartX + ((int) ((tick % 2 == 0 && id ? 1 : -1) * p * xSpeed));
            int fontY = -(int) Math.min((p * ySpeed), 2 * height);
            if (value < 0) {
                color = color1;
                value = -value;
            }

            String damage = HealthTextGetters.formatNum(value);
            font.drawInBatch(Component.literal(damage), fontX, fontY, color, false,
                    posed.last().pose(), bufferSource, Font.DisplayMode.SEE_THROUGH, 0, FONT_LIGHT);

            posed.popPose();
        });
    }

    public int width(LivingEntity living) {
        return 0;
    }

    public int height(LivingEntity living) {
        return 0;
    }

    protected final boolean friendly(LivingEntity living) {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.isAlliedTo(living) || !(living instanceof Enemy);
    }
}
