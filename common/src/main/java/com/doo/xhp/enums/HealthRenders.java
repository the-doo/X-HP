package com.doo.xhp.enums;

import com.doo.xhp.render.*;

public enum HealthRenders {

    IGNORED(null),
    COLOR(new ColorHealRender()),
    NAME_TAG(new NameTagLikeHealRender()),
    ICON(new IconHealRender()),
    FENCE(new FenceHealRender()),

    ;

    private final HealRender render;

    HealthRenders(HealRender render) {
        this.render = render;
    }

    public static HealthRenders name(String name) {
        for (HealthRenders value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }

        return IGNORED;
    }

    public static String name(HealRender render) {
        for (HealthRenders value : values()) {
            if (value.render == render) {
                return value.name();
            }
        }

        return null;
    }

    public HealRender getRender() {
        return render;
    }
}
