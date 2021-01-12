package com.doo.xhp;

import com.doo.xhp.config.Config;
import com.doo.xhp.config.Option;
import net.fabricmc.api.ModInitializer;

public class XHP implements ModInitializer {

    public static final String ID = "xhp";

    public static Option option = new Option();


    @Override
    public void onInitialize() {
        // 加载配置
        option = Config.read(ID, Option.class, option);
    }
}
