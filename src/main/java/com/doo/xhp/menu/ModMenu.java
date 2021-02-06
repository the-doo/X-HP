package com.doo.xhp.menu;

import com.doo.xhp.XHP;
import com.doo.xhp.menu.screen.ModMenuScreen;
import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.Map;

public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuScreen::get;
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return ImmutableMap.of(XHP.ID, getModConfigScreenFactory());
    }
}
