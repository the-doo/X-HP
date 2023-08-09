package com.doo.xhp;

import com.doo.xhp.enums.HealthRenders;
import com.doo.xhp.enums.MenuOptType;
import com.doo.xhp.interfaces.WithOption;
import com.doo.xhp.render.HealRender;
import com.doo.xhp.render.ImageHealRender;
import com.doo.xhp.screen.MenuScreen;
import com.doo.xhp.util.ConfigUtil;
import com.doo.xhp.util.HealthRenderUtil;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;

import java.util.List;
import java.util.function.Supplier;

public class XHP implements WithOption {
    public static final String MOD_ID = "x_hp";
    public static final String MOD_NAME = "X-HP";
    private static final List<EntityType<?>> BAN = Lists.newArrayList();
    private static final String RANGE_KEY = "range";
    private static final String FOCUS_KEY = "focus";
    private static final String BAN_KEY = "ban";
    private static final String TYPE_KEY = "type";

    public static final String ENABLED_KEY = "enabled";
    private static final String TIP_KEY = "TIP";

    private static final List<WithOption> OPTIONS = Lists.newArrayList();

    private XHP() {
    }

    public static void init() {
        CONFIG.addProperty(ENABLED_KEY, true);
        CONFIG.addProperty(RANGE_KEY, 32);
        CONFIG.addProperty(FOCUS_KEY, false);
        CONFIG.add(BAN_KEY, new JsonArray());
        CONFIG.addProperty(TYPE_KEY, HealthRenders.COLOR.name());

        OPTIONS.add(HealthRenderUtil.TIP_HEAL_RENDER);
        CONFIG.add(TIP_KEY, HealthRenderUtil.TIP_HEAL_RENDER.opt());
        for (HealthRenders value : HealthRenders.values()) {
            if (value.getRender() != null) {
                CONFIG.add(value.name(), value.getRender().opt());
                OPTIONS.add(value.getRender());
            }
        }

        registerSource();

        OPTIONS.forEach(WithOption::registerOpt);

        // load config
        ConfigUtil.copyTo(XHP.MOD_NAME, CONFIG);
        reloadConfig(true);
    }

    private static void registerSource() {
        MenuScreen.register(MenuOptType.LIST, null, BAN_KEY, (Supplier<?>) () -> BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(e -> e.getCategory() != MobCategory.MISC)
                .map(EntityType::getDescriptionId));
        MenuScreen.register(MenuOptType.ENUM, null, TYPE_KEY, HealthRenders.class);
    }

    public static void reloadConfig(boolean init) {
        OPTIONS.forEach(WithOption::reloadOpt);

        // reload ban
        BAN.clear();
        CONFIG.get(BAN_KEY).getAsJsonArray().forEach(id -> BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(e -> id.getAsString().equals(e.getDescriptionId()))
                .forEach(BAN::add));

        // reload render
        HealthRenderUtil.setRender(render());

        // reload file
        if (!init) {
            ((ImageHealRender) HealthRenders.IMAGE.getRender()).reloadImage(Minecraft.getInstance());
        }
    }

    public static HealRender render() {
        return WithOption.renderV(CONFIG, TYPE_KEY);
    }

    public static double distance() {
        double range = WithOption.doubleV(CONFIG, RANGE_KEY);
        return range * range;
    }

    public static boolean focus() {
        return WithOption.boolV(CONFIG, FOCUS_KEY);
    }

    public static boolean ignored(LivingEntity e) {
        return BAN.contains(e.getType());
    }

    public static boolean isTip(String key) {
        return TIP_KEY.equals(key);
    }

    public static boolean disabled() {
        return !CONFIG.get(ENABLED_KEY).getAsBoolean();
    }
}
