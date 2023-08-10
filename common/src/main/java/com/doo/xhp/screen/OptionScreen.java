package com.doo.xhp.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Option;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.Supplier;

public class OptionScreen extends Screen {

    private final Screen prev;
    private Supplier<Option[]> opsGetter;

    private OptionsList list;


    public OptionScreen(Screen prev) {
        super(TextComponent.EMPTY);

        this.prev = prev;
    }

    public OptionScreen(Screen prev, Supplier<Option[]> opsGetter) {
        this(prev);
        setOpsGetter(opsGetter);
    }

    public void setOpsGetter(Supplier<Option[]> opsGetter) {
        this.opsGetter = opsGetter;
    }

    @Override
    protected void init() {
        int w = this.width;
        list = new OptionsList(minecraft, w, this.height, 32, this.height - 32, 25);

        list.addSmall(opsGetter.get());

        addRenderableWidget(list);
        addRenderableWidget(new Button(this.width / 2 - 150 / 2, this.height - 28, 150, 20, CommonComponents.GUI_BACK, b -> close()));
    }

    public void close() {
        minecraft.setScreen(prev);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(0);

        super.render(poseStack, i, j, f);

        List<FormattedCharSequence> list = SimpleOptionsSubScreen.tooltipAt(this.list, i, j);
        this.renderTooltip(poseStack, list, i, j);
    }
}
