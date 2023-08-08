package com.doo.xhp.interfaces;

import java.util.Map;
import java.util.function.BiConsumer;

public interface DamageAccessor {

    static double sum(Object e) {
        Map<Integer, Float> map = ((DamageAccessor) e).x_HP$lastDamageMap();
        if (map.isEmpty()) {
            return 0;
        }
        return map.values().stream().mapToDouble(Float::doubleValue).sum();
    }

    static void foreach(Object e, BiConsumer<Integer, Float> consumer) {
        ((DamageAccessor) e).x_HP$lastDamageMap().forEach(consumer);
    }

    Map<Integer, Float> x_HP$lastDamageMap();
}
