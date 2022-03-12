package com.doo.xhp.menu.screen;

import com.doo.xhp.XHP;
import com.doo.xhp.config.XOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.options.BooleanOption;
import net.minecraft.client.options.DoubleOption;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.util.OrderableTooltip;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Tips Settings
 */
public class TipsScreen extends Screen {

    private static final Option TIPS =
//            CyclingOption.create("xhp.menu.option.tips",
//                    o -> XHP.XOption.tips, (g, o, v) -> XHP.XOption.tips = v);
            new BooleanOption("xhp.menu.option.tips",
                    o -> XHP.XOption.tips, (g, v) -> XHP.XOption.tips = v);

    private static final Option TIPS_COLOR = new ModMenuScreen.ColorOption("xhp.menu.option.tips_color",
            v -> XHP.XOption.tipsColor = v, () -> XHP.XOption.tipsColor);

    private static final Option TIPS_X = new DoubleOption("xhp.menu.option.tips_x", 0, MinecraftClient.getInstance().getWindow().getFramebufferWidth() / 2D, 1,
            v -> XHP.XOption.tipsMiddle[0] ? MinecraftClient.getInstance().getWindow().getFramebufferWidth() / 2 : (double) XHP.XOption.tipsLocation[0],
            (o, d) -> XHP.XOption.tipsLocation[0] = d.intValue(),
            (g, o) -> {
                if (Math.abs(MinecraftClient.getInstance().getWindow().getFramebufferWidth() / 2 - XHP.XOption.tipsLocation[0]) < 3) {
                    XHP.XOption.tipsMiddle[0] = true;
                    return new TranslatableText("options.fov.min");
                }
                XHP.XOption.tipsMiddle[0] = false;
                return new TranslatableText("xhp.menu.option.tips_x", XHP.XOption.tipsLocation[0]);
            });

    private static final Option TIPS_Y = new DoubleOption("xhp.menu.option.tips_y", 0, MinecraftClient.getInstance().getWindow().getFramebufferHeight() / 2D, 1,
            v -> XHP.XOption.tipsMiddle[1] ? MinecraftClient.getInstance().getWindow().getFramebufferHeight() / 2 : (double) XHP.XOption.tipsLocation[1],
            (o, d) -> XHP.XOption.tipsLocation[1] = d.intValue(),
            (g, o) -> {
                if (Math.abs(MinecraftClient.getInstance().getWindow().getFramebufferHeight() / 2 - XHP.XOption.tipsLocation[1]) < 3) {
                    XHP.XOption.tipsMiddle[1] = true;
                    return new TranslatableText("options.fov.min");
                }
                XHP.XOption.tipsMiddle[1] = false;
                return new TranslatableText("xhp.menu.option.tips_y", XHP.XOption.tipsLocation[1]);
            });

    private static final Option TIPS_SCALE = new DoubleOption("xhp.menu.option.tips_scale", 1, 40, 0.5F,
            v -> (double) XHP.XOption.tipsScale,
            (o, d) -> XHP.XOption.tipsScale = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.tips_scale", XHP.XOption.tipsScale));

    private static final Option TIPS_TEMPLATE = new Option("") {

        @Override
        public AbstractButtonWidget createButton(GameOptions options, int x, int y, int width) {
//        public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
            if (INSTANCE.client == null) {
                return null;
            }

            // tips
            List<OrderedText> tips = new ArrayList<>();
            tips.add(new TranslatableText("xhp.menu.option.tips_temp").asOrderedText());
            tips.add(new TranslatableText("xhp.menu.option.tips_temp_desc").append(":  ")
                    .append(XOption.DEFAULT_TIPS_TEMP).asOrderedText());
            Arrays.stream(XOption.AttrKeyValue.values()).forEach(kv ->
                    tips.add(new LiteralText(kv.key).append(": ").append(new TranslatableText(kv.transactionKey)).asOrderedText()));

            TextFieldWidget text = new InputWidget(INSTANCE.client.textRenderer, x + 2, y + 2, width - 4, 18, tips);

            text.setText(XHP.XOption.tipsTemplate);
            if (StringUtils.isEmpty(text.getText())) {
                text.setSuggestion(XOption.DEFAULT_TIPS_TEMP);
            }

            text.setChangedListener(v -> {
                XHP.XOption.tipsTemplate = StringUtils.defaultIfEmpty(v, XOption.DEFAULT_TIPS_TEMP);
                text.setSuggestion(StringUtils.isEmpty(v) ? XOption.DEFAULT_TIPS_TEMP : StringUtils.EMPTY);
            });

            return text;
        }
    };

    private static final Option TIPS_TEMPLATE_2 = new Option("") {

        @Override
//        public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
        public AbstractButtonWidget createButton(GameOptions options, int x, int y, int width) {
            if (INSTANCE.client == null) {
                return null;
            }

            TextFieldWidget text = new InputWidget(INSTANCE.client.textRenderer, x + 2, y + 2, width - 4, 18,
                    Collections.singletonList(new TranslatableText("xhp.menu.option.tips_temp_2").asOrderedText()));

            text.setText(XHP.XOption.tipsTemplate2);
            if (StringUtils.isEmpty(text.getText())) {
                text.setSuggestion(XOption.AttrKeyValue.DAMAGE.key);
            }

            text.setChangedListener(v -> {
                XHP.XOption.tipsTemplate2 = StringUtils.defaultIfEmpty(v, XOption.AttrKeyValue.DAMAGE.key);
                text.setSuggestion(StringUtils.isEmpty(v) ? XOption.AttrKeyValue.DAMAGE.key : StringUtils.EMPTY);
            });

            return text;
        }
    };

    private static final TipsScreen INSTANCE = new TipsScreen();

    private ButtonListWidget list;

    private Screen pre;

    private TipsScreen() {
        super(new LiteralText(XHP.ID));
    }

    @Override
    protected void init() {
        Option[] other = {
                TIPS, TIPS_COLOR,
                TIPS_X, TIPS_Y,
                TIPS_TEMPLATE, TIPS_TEMPLATE_2,
                TIPS_SCALE
        };

        list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        list.addAll(other);

//        this.addSelectableChild(list);
//        this.addDrawableChild(new ButtonWidget(this.width / 2 - 150 / 2, this.height - 28, 150, 20,
//                ScreenTexts.BACK, b -> Optional.ofNullable(client).ifPresent(c -> c.currentScreen = this.pre)));
        this.addChild(list);
        this.addButton(new ButtonWidget(this.width / 2 - 150 / 2, this.height - 28, 150, 20,
                ScreenTexts.BACK, b -> Optional.ofNullable(client).ifPresent(c -> c.currentScreen = this.pre)));
    }

    public static TipsScreen get(Screen pre) {
        INSTANCE.width = pre.width;
        INSTANCE.height = pre.height;
        INSTANCE.pre = pre;
        return INSTANCE;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        list.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);

        // see net.minecraft.client.gui.screen.option.NarratorOptionsScreen.render
//        Optional<ClickableWidget> optional = list.getHoveredButton(mouseX, mouseY);
        Optional<AbstractButtonWidget> optional = list.getHoveredButton(mouseX, mouseY);
//        if (optional.isPresent() && optional.get() instanceof OrderableTooltip) {
//            this.renderOrderedTooltip(matrices, ((OrderableTooltip) optional.get()).getOrderedTooltip(), mouseX, mouseY);
        if (optional.isPresent() && optional.get() instanceof OrderableTooltip && ((OrderableTooltip) optional.get()).getOrderedTooltip().isPresent()) {
            this.renderOrderedTooltip(matrices, ((OrderableTooltip) optional.get()).getOrderedTooltip().get(), mouseX, mouseY);
        }
    }

    private static class InputWidget extends TextFieldWidget implements OrderableTooltip {
        private final List<OrderedText> tips;

        public InputWidget(TextRenderer textRenderer, int x, int y, int width, int height, List<OrderedText> tips) {
            super(textRenderer, x, y, width, height, Text.of(StringUtils.EMPTY));

            this.tips = tips;
        }

        //        @Override
//        public List<OrderedText> getOrderedTooltip() {
//            return tips;
//        }
        @Override
        public Optional<List<OrderedText>> getOrderedTooltip() {
            return Optional.of(tips);
        }
    }
}
