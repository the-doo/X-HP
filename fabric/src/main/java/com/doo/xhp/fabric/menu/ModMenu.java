package com.doo.xhp.fabric.menu;

import com.doo.xhp.XHP;
import com.doo.xhp.screen.MenuScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.Collections;
import java.util.Map;

public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MenuScreen::get;
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return Collections.singletonMap(XHP.MOD_ID, getModConfigScreenFactory());
    }
}
