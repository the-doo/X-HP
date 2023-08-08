package com.doo.xhp.render;

import com.doo.xhp.enums.HealthRenders;
import com.doo.xhp.enums.HealthTextGetters;
import com.doo.xhp.enums.HealthTextPosition;
import com.doo.xhp.enums.MenuOptType;
import com.doo.xhp.interfaces.WithOption;
import com.doo.xhp.screen.MenuScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;

public class ColorHealRender extends HealRender {

    private static final String BAR_MONSTER_KEY = "bar_monster";
    private static final String BAR_FRIENDLY_KEY = "bar_friendly";
    private static final String TEXT_KEY = "text";
    private static final String TEXT_COLOR_KEY = "text_color";
    private static final String P_KEY = "position";
    private HealthTextPosition position;
    private int monster;
    private int friend;

    public ColorHealRender() {
        options.addProperty(BAR_MONSTER_KEY, RED_COLOR);
        options.addProperty(BAR_FRIENDLY_KEY, GREEN_COLOR);
        options.addProperty(TEXT_COLOR_KEY, -1);
        options.addProperty(TEXT_KEY, HealthTextGetters.ONLY_CURRENT.name());
        options.addProperty(P_KEY, HealthTextPosition.FOLLOW.name());
    }

    @Override
    public void registerOpt() {
        super.registerOpt();

        String name = HealthRenders.COLOR.name();
        MenuScreen.register(MenuOptType.COLOR, name, BAR_MONSTER_KEY, 1);
        MenuScreen.register(MenuOptType.COLOR, name, BAR_FRIENDLY_KEY, 1);
        MenuScreen.register(MenuOptType.COLOR, name, TEXT_COLOR_KEY, 1);
        MenuScreen.register(MenuOptType.ENUM, name, TEXT_KEY, HealthTextGetters.class);
        MenuScreen.register(MenuOptType.ENUM, name, P_KEY, HealthTextPosition.class);
    }

    @Override
    public void reloadOpt() {
        super.reloadOpt();

        position = WithOption.enumV(options, P_KEY, HealthTextPosition.class).orElse(HealthTextPosition.FOLLOW);
        monster = (int) WithOption.doubleV(options, BAR_MONSTER_KEY);
        friend = (int) WithOption.doubleV(options, BAR_FRIENDLY_KEY);
    }

    protected void renderCurrent(GuiGraphics graphics, boolean friendly, double process, int endX, int endY) {
        graphics.fill(0, 0, endX, endY, friendly ? friend : monster);
    }

    protected void renderLost(GuiGraphics graphics, int startX, int endX, int endY, LivingEntity living) {
        graphics.fill(startX, 0, endX, endY, FastColor.ARGB32.color(200, 255, 255, 255));
    }

    protected void renderBack(GuiGraphics graphics, int startX, int endX, int endY) {
        graphics.fill(startX, 0, endX, endY, FastColor.ARGB32.color(200, 0, 0, 0));
    }

    protected void renderHealthText(GuiGraphics graphics, boolean friendly, LivingEntity living, Font font, MultiBufferSource bufferSource, int i, int processW) {
        HealthTextGetters getters = WithOption.enumV(options, TEXT_KEY, HealthTextGetters.class)
                .orElse(HealthTextGetters.ONLY_CURRENT);
        String heal = getters.format(living, null);
        int w = width(living) / 2;
        int h = height(living);
        MutableInt fontW = new MutableInt(font.width(heal));
        MutableInt fontY = new MutableInt(1);

        position.change(fontW, fontY, w, h, processW);

        MutableComponent component = Component.literal(heal);
        Matrix4f pose = graphics.pose().last().pose();
        int x = fontW.intValue();
        int y = fontY.intValue();
        font.drawInBatch(component, x, y, -1, false,
                pose, bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, FONT_LIGHT);
        font.drawInBatch(component, x, y, -1, false,
                pose, bufferSource, Font.DisplayMode.NORMAL, 0, FONT_LIGHT);
    }

    public int width(LivingEntity living) {
        return 80;
    }

    public int height(LivingEntity living) {
        return 9;
    }
}
