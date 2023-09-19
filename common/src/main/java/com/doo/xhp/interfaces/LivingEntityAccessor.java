package com.doo.xhp.interfaces;

public interface LivingEntityAccessor {

    static boolean isHealId(Object e, int id) {
        return ((LivingEntityAccessor) e).x_HP$healDataId() == id;
    }

    static boolean isPoseId(Object e, int id) {
        return ((LivingEntityAccessor) e).x_HP$poseData() == id;
    }

    int x_HP$healDataId();

    int x_HP$poseData();
}
