package com.doo.xhp;

import com.doo.xhp.config.Config;
import com.doo.xhp.config.XOption;
import net.fabricmc.api.ModInitializer;

public class XHP implements ModInitializer {

    public static final String ID = "xhp";

    public static XOption XOption = new XOption();


    @Override
    public void onInitialize() {
        // 加载配置
        XOption = Config.read(ID, XOption.class, XOption);
    }
}
