package com.doo.xhp.enums;

import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.function.BiFunction;

public enum HealthTextGetters {

    IGNORED(null),

    ONLY_CURRENT((living, formatter) -> formatNum(living.getHealth())),

    ONLY_MAX((living, formatter) -> formatNum(living.getMaxHealth())),
    CURRENT_AND_MAX((living, formatter) ->
            StringUtils.defaultString(formatter, "%s/%s").formatted(formatNum(living.getHealth()), formatNum(living.getMaxHealth()))),
    PERCENTAGE((living, formatter) ->
            formatNum(living.getHealth() / living.getMaxHealth() * 100) + "%"),

    ;

    private final BiFunction<LivingEntity, String, String> getter;

    public static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    HealthTextGetters(BiFunction<LivingEntity, String, String> getter) {
        this.getter = getter;
    }

    public String formatted(LivingEntity entity, String formatter) {
        if (this == IGNORED) {
            return "";
        }
        return getter.apply(entity, formatter);
    }

    public static String formatNum(float number) {
        return FORMAT.format(number);
    }

    public static String formatNum(String formatter, Object... numbers) {
        Object[] array = Arrays.stream(numbers).map(FORMAT::format).toArray();
        return formatter.formatted(array);
    }
}
