package com.doo.xhp.enums;

import org.apache.commons.lang3.mutable.MutableInt;

public enum HealthTextPosition {

    IGNORED,

    FOLLOW,

    TOP_LEFT,

    TOP_CENTER,

    TOP_RIGHT,

    LEFT,

    CENTER,

    RIGHT,

    BOTTOM_LEFT,

    BOTTOM_CENTER,

    BOTTOM_RIGHT,

    ;

    public void change(MutableInt fontX, MutableInt fontY, int w, int h, int processW) {
        switch (this) {
            case TOP_LEFT:
                fontY.subtract(h);
                fontX.setValue(0);
                return;
            case TOP_CENTER:
                fontY.subtract(h);
                fontX.setValue(w - fontX.intValue() / 2);
                return;
            case TOP_RIGHT:
                fontY.subtract(h);
                fontX.setValue(2 * w - fontX.intValue());
                return;
            case BOTTOM_LEFT:
                fontY.add(h);
                fontX.setValue(0);
                return;
            case BOTTOM_CENTER:
                fontY.add(h);
                fontX.setValue(w - fontX.intValue() / 2);
                return;
            case BOTTOM_RIGHT:
                fontY.add(h);
                fontX.setValue(2 * w - fontX.intValue());
                return;
            case LEFT:
                fontX.setValue(0);
                return;
            case CENTER:
                fontX.setValue(w - fontX.intValue() / 2);
                return;
            case RIGHT:
                fontX.setValue(2 * w - fontX.intValue());
                return;
            default:
                fontX.setValue(Math.max(processW - fontX.intValue(), 0));
        }
    }
}
