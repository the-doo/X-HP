package com.doo.xhp.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class OptionScreen extends Screen {

    private final Screen prev;
    private Supplier<OptionInstance<?>[]> opsGetter;


    public OptionScreen(Screen prev) {
        super(Component.empty());

        this.prev = prev;
    }

    public OptionScreen(Screen prev, Supplier<OptionInstance<?>[]> opsGetter) {
        this(prev);
        setOpsGetter(opsGetter);
    }

    public void setOpsGetter(Supplier<OptionInstance<?>[]> opsGetter) {
        this.opsGetter = opsGetter;
    }

    @Override
    protected void init() {
        int w = this.width;
        OptionsList list = new OptionsList(minecraft, w, this.height, 32, this.height - 32, 25);

        list.addSmall(opsGetter.get());

        addRenderableWidget(list);
        addRenderableWidget(new Button.Builder(CommonComponents.GUI_BACK, b -> close()).bounds(this.width / 2 - 150 / 2, this.height - 28, 150, 20).build());
    }

    public void close() {
        minecraft.setScreen(prev);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(poseStack);

        super.render(poseStack, i, j, f);
    }
}
