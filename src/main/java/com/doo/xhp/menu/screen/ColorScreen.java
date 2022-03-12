package com.doo.xhp.menu.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.options.DoubleOption;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.Objects;
import java.util.function.Function;

/**
 * mod menu COLOR SCREEN 配置界面
 */
public class ColorScreen extends Screen {

    private final Function<Integer, Integer> setter;

    private final Screen pre;

    private ButtonListWidget list;

    private Integer r, g, b;

    public ColorScreen(Function<Integer, Integer> setter, Integer value, Screen pre) {
        super(Text.of(""));
        this.setter = setter;
        this.r = (value >> 16) & 0xFF;
        this.g = (value >> 8) & 0xFF;
        this.b = value & 0xFF;
        this.pre = pre;
    }

    public int getValue() {
        return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    @Override
    protected void init() {
        list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        // 显示RBG
        String redKey = "xhp.menu.color.red";
        DoubleOption ro = new DoubleOption(redKey, 0, 255, 1,
                (o) -> Double.valueOf(r), (o, d) -> r = d.intValue(), (o, c) -> new TranslatableText(redKey, r));
        String greenKey = "xhp.menu.color.green";
        DoubleOption go = new DoubleOption(greenKey, 0, 255, 1,
                (o) -> Double.valueOf(g), (o, d) -> g = d.intValue(), (o, c) -> new TranslatableText(greenKey, g));
        String blueKey = "xhp.menu.color.blue";
        DoubleOption bo = new DoubleOption(blueKey, 0, 255, 1,
                (o) -> Double.valueOf(b), (o, d) -> b = d.intValue(), (o, c) -> new TranslatableText(blueKey, b));
        Option[] options = {ro, go, bo, new Option("") {
            @Override
            public AbstractButtonWidget createButton(GameOptions options, int x, int y, int width) {
//            public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
                TextFieldWidget color = new TextFieldWidget(Objects.requireNonNull(client).textRenderer, x, y, width, 20, Text.of("color")) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        super.render(matrices, mouseX, mouseY, delta);
                        setUneditableColor(getValue());
                    }
                };
                color.setEditable(false);
                color.setText("||||||||||||||||||||");
                color.setUneditableColor(getValue());
                return color;
            }
        }};
        list.addAll(options);
        this.addChild(list);
        // 返回按钮
        this.addButton(new ButtonWidget(this.width / 2 - 150 / 2, this.height - 28,
                150, 20, ScreenTexts.BACK, ignored -> close()));
    }

    public void close() {
        if (client != null) {
            setter.apply(getValue());
            client.currentScreen = pre;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        list.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
