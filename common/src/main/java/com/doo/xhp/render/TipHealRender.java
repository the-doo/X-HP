package com.doo.xhp.render;

import com.doo.xhp.XHP;
import com.doo.xhp.enums.HealthTextGetters;
import com.doo.xhp.interfaces.WithOption;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;

import static com.doo.xhp.XHP.ENABLED_KEY;

public class TipHealRender implements WithOption {

    private static final String NAME_KEY = "name";
    private static final String ARMOR_KEY = "armor";
    private static final String INTERVAL_KEY = "interval";
    private static final String Y_DIST_KEY = "y_dirt";
    private static final String Y_KEY = "y";

    private static final JsonObject options = new JsonObject();

    private int y = 0;

    public TipHealRender() {
        options.addProperty(XHP.ENABLED_KEY, false);
        options.addProperty(NAME_KEY, true);
        options.addProperty(ARMOR_KEY, true);
        options.addProperty(INTERVAL_KEY, 4);
        options.addProperty(Y_DIST_KEY, false);
        options.addProperty(Y_KEY, 20);
    }

    @Override
    public JsonObject opt() {
        return options;
    }

    @Override
    public void reloadOpt() {
        y = (int) ((WithOption.boolV(options, Y_DIST_KEY) ? -1 : 1) * WithOption.doubleV(options, Y_KEY));
    }

    @Override
    public boolean enabled() {
        return options.get(ENABLED_KEY).getAsBoolean();
    }

    public void render(GuiGraphics graphics, Font font, LivingEntity living, int centerX, int centerY) {
        String interval = " ".repeat((int) (WithOption.doubleV(options, INTERVAL_KEY)));

        StringBuilder sb = new StringBuilder();
        if (WithOption.boolV(options, NAME_KEY)) {
            sb.append(interval).append(living.getName().getString()).append(interval);
        }
        sb.append(HealthTextGetters.formatNum("❤%s", living.getHealth()));
        if (WithOption.boolV(options, ARMOR_KEY)) {
            sb.append(interval).append(HealthTextGetters.formatNum("🛡%s", living.getArmorValue()));
        }

        String string = sb.toString();
        int startY = centerY - 4 + y;
        graphics.drawCenteredString(font, string, centerX, startY, -1);
    }
}
