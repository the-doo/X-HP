package com.doo.xhp.menu;

import com.doo.xhp.menu.screen.ModMenuScreen;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<ModMenuScreen> getModConfigScreenFactory() {
        return ModMenuScreen::get;
    }
}
