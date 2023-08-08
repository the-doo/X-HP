package com.doo.xhp.interfaces;

public interface LivingEntityAccessor {

    static boolean isHealId(Object e, int id) {
        return ((LivingEntityAccessor) e).x_HP$healDataId() == id;
    }

    int x_HP$healDataId();
}
