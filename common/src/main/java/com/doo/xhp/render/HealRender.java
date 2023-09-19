package com.doo.xhp.render;

import com.doo.xhp.enums.HealthRenders;
import com.doo.xhp.enums.HealthTextGetters;
import com.doo.xhp.enums.HealthTextPosition;
import com.doo.xhp.enums.MenuOptType;
import com.doo.xhp.interfaces.DamageAccessor;
import com.doo.xhp.interfaces.WithOption;
import com.doo.xhp.screen.MenuScreen;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.apache.commons.lang3.mutable.MutableInt;

import static com.doo.xhp.XHP.ENABLED_KEY;
import static com.doo.xhp.interfaces.WithOption.enumV;

public abstract class HealRender implements WithOption {

    protected static final int GREEN_COLOR = 0xFF00FF00;
    protected static final int RED_COLOR = 0xFFFF0000;
    public static final String BASE_SCALE_KEY = "scale";
    public static final String BASE_Y_KEY = "base_y";
    public static final String WRAPPER_KEY = "wrapper";
    public static final String TEXT_KEY = "text";
    public static final String TEXT_COLOR_KEY = "text_color";
    public static final String TEXT_SEE_KEY = "text_see";
    public static final String P_KEY = "position";
    public static final int FONT_LIGHT = 0xF000F0;

    protected final JsonObject options = new JsonObject();


    protected float scale = 0.8f;
    protected double baseY = 0;
    protected int weight = 80;
    protected int height = 9;
    protected HealthTextPosition position;

    protected HealRender() {
        options.addProperty(ENABLED_KEY, true);
        options.addProperty(BASE_SCALE_KEY, 8);
        options.addProperty(BASE_Y_KEY, 40);
        options.addProperty(WRAPPER_KEY, true);

        if (needHealthText()) {
            position = HealthTextPosition.FOLLOW;
            options.addProperty(TEXT_KEY, HealthTextGetters.ONLY_CURRENT.name());
            options.addProperty(TEXT_SEE_KEY, false);
            options.addProperty(TEXT_COLOR_KEY, -1);
            options.addProperty(P_KEY, position.name());
        }
    }

    @Override
    public void registerOpt() {
        String name = HealthRenders.name(this);

        if (needHealthText()) {
            MenuScreen.register(MenuOptType.COLOR, name, TEXT_COLOR_KEY, 1);
            MenuScreen.register(MenuOptType.ENUM, name, TEXT_KEY, HealthTextGetters.class);
            MenuScreen.register(MenuOptType.ENUM, name, P_KEY, HealthTextPosition.class);
        }
    }

    @Override
    public void reloadOpt() {
        scale = (float) (WithOption.doubleV(options, BASE_SCALE_KEY) / 10);
        baseY = -(WithOption.doubleV(options, BASE_Y_KEY) - 45);

        if (needHealthText()) {
            position = WithOption.enumV(options, P_KEY, HealthTextPosition.class).orElse(HealthTextPosition.FOLLOW);
        }
    }

    @Override
    public JsonObject opt() {
        return options;
    }

    @Override
    public boolean enabled() {
        return options.get(ENABLED_KEY).getAsBoolean();
    }

    public final void render(PoseStack graphics, MultiBufferSource bufferSource, LivingEntity living) {
        graphics.scale(scale, scale, scale);
        graphics.translate(needMoveCenter() ? -width() / 2F : 0, incY() - 10F, 0);

        renderContent(graphics, living, bufferSource);
    }

    protected boolean needMoveCenter() {
        return true;
    }

    protected int renderContent(PoseStack graphics, LivingEntity living, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int backColor = (int) (minecraft.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;
        int w = width();
        int h = height();
        double process = Math.min(living.getHealth() / living.getMaxHealth(), 1);
        int processW = (int) (process * w);

        // container
        renderCurrent(graphics, process, processW, h, living);

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
            GuiComponent.fill(graphics, -p, h + p, w + p, h, backColor);
            GuiComponent.fill(graphics, -p, h, 0, 0, backColor);
            GuiComponent.fill(graphics, -p, 0, w + p, -p, backColor);
            GuiComponent.fill(graphics, w, h, w + p, 0, backColor);
        }

        if (needHealthText() && enumV(options, P_KEY, HealthTextPosition.class)
                .map(e -> e != HealthTextPosition.IGNORED)
                .orElse(true)) {
            renderHealthText(graphics, living, font, bufferSource, processW);
        }
        return processW;
    }

    protected boolean needWrapper() {
        return WithOption.boolV(options, WRAPPER_KEY);
    }

    protected void renderCurrent(PoseStack graphics, double process, int endX, int endY, LivingEntity living) {

    }

    protected void renderLost(PoseStack graphics, int startX, int endX, int endY, LivingEntity living) {
    }

    protected void renderBack(PoseStack graphics, int startX, int endX, int endY) {

    }

    protected boolean needHealthText() {
        return true;
    }

    protected void renderHealthText(PoseStack graphics, LivingEntity living, Font font, MultiBufferSource bufferSource, int processW) {
        HealthTextGetters getters = WithOption.enumV(options, TEXT_KEY, HealthTextGetters.class)
                .orElse(HealthTextGetters.ONLY_CURRENT);
        String heal = getters.formatted(living, null);
        int w = width() / 2;
        int h = height();
        MutableInt fontW = new MutableInt(font.width(heal));
        MutableInt fontY = new MutableInt(1);

        position.change(fontW, fontY, w, h, processW);

        Matrix4f pose = graphics.last().pose();
        float x = fontW.intValue();
        float y = fontY.intValue();
        int color = (int) WithOption.doubleV(options, TEXT_COLOR_KEY);

        Component component = new TextComponent(heal);
        font.drawInBatch(component, x, y, color, false,
                pose, bufferSource, false, 0, FONT_LIGHT);

        if (WithOption.boolV(options, TEXT_SEE_KEY)) {
            font.drawInBatch(component, x, y, color, false,
                    pose, bufferSource, true, 0, FONT_LIGHT);
        }
    }

    public int width() {
        return weight;
    }

    public int height() {
        return height;
    }

    protected double incY() {
        return baseY;
    }

    protected final boolean friendly(LivingEntity living) {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.isAlliedTo(living) || !(living instanceof Enemy);
    }
}
