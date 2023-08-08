package com.doo.xhp.interfaces;

import com.doo.xhp.XHP;
import com.doo.xhp.enums.HealthRenders;
import com.doo.xhp.render.HealRender;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Optional;

public interface WithOption {

    JsonObject CONFIG = new JsonObject();
    String MENU_NAME_FORMATTER = "%s.menu.option.%s";
    String MENU_TIP_FORMATTER = "%s.tip";

    default JsonObject opt() {
        return CONFIG;
    }

    default void registerOpt() {
    }

    default void reloadOpt() {
    }

    static String menuName(String name) {
        return MENU_NAME_FORMATTER.formatted(XHP.MOD_ID, name);
    }

    static String menuTip(String name) {
        return MENU_TIP_FORMATTER.formatted(name);
    }

    static String menuNameTip(String name) {
        return MENU_TIP_FORMATTER.formatted(menuName(name));
    }

    default boolean enabled() {
        return true;
    }

    static boolean boolV(JsonObject object, String key) {
        return object.get(key).getAsBoolean();
    }

    static double doubleV(JsonObject object, String key) {
        return object.get(key).getAsDouble();
    }

    static <T extends Enum<T>> Optional<T> enumV(JsonObject object, String key, Class<T> clazz) {
        String name = object.get(key).getAsString();
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }

        for (T t : clazz.getEnumConstants()) {
            if (t.name().equals(name)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    static <T extends Enum<T>> T enumV(Map.Entry<String, JsonElement> entry, Class<T> clazz) {
        String name = entry.getValue().getAsString();
        for (T t : clazz.getEnumConstants()) {
            if (t.name().equals(name)) {
                return t;
            }
        }
        return null;
    }

    static void addItem(JsonObject object, String key, String item) {
        object.get(key).getAsJsonArray().add(item);
    }

    static HealRender renderV(JsonObject config, String typeKey) {
        return HealthRenders.name(config.get(typeKey).getAsString()).getRender();
    }
}
