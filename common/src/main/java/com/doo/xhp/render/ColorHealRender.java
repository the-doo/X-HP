package com.doo.xhp.render;

import com.doo.xhp.enums.HealthRenders;
import com.doo.xhp.enums.MenuOptType;
import com.doo.xhp.interfaces.WithOption;
import com.doo.xhp.screen.MenuScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;

public class ColorHealRender extends HealRender {

    private static final String BAR_MONSTER_KEY = "bar_monster";
    private static final String BAR_FRIENDLY_KEY = "bar_friendly";
    private int monster;
    private int friend;

    public ColorHealRender() {
        options.addProperty(BAR_MONSTER_KEY, RED_COLOR);
        options.addProperty(BAR_FRIENDLY_KEY, GREEN_COLOR);
    }

    @Override
    public void registerOpt() {
        super.registerOpt();

        String name = HealthRenders.COLOR.name();
        MenuScreen.register(MenuOptType.COLOR, name, BAR_MONSTER_KEY, 1);
        MenuScreen.register(MenuOptType.COLOR, name, BAR_FRIENDLY_KEY, 1);
    }

    @Override
    public void reloadOpt() {
        super.reloadOpt();

        monster = (int) WithOption.doubleV(options, BAR_MONSTER_KEY);
        friend = (int) WithOption.doubleV(options, BAR_FRIENDLY_KEY);
    }

    protected void renderCurrent(GuiGraphics graphics, double process, int endX, int endY, LivingEntity living) {
        graphics.fill(0, 0, endX, endY, friendly(living) ? friend : monster);
    }

    protected void renderLost(GuiGraphics graphics, int startX, int endX, int endY, LivingEntity living) {
        graphics.fill(startX, 0, endX, endY, FastColor.ARGB32.color(200, 255, 255, 255));
    }

    protected void renderBack(GuiGraphics graphics, int startX, int endX, int endY) {
        graphics.fill(startX, 0, endX, endY, FastColor.ARGB32.color(200, 0, 0, 0));
    }
}
