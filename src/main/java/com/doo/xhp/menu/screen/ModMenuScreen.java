package com.doo.xhp.menu.screen;

import com.doo.xhp.XHP;
import com.doo.xhp.config.Config;
import com.doo.xhp.config.XOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.CyclingOption;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

/**
 * mod menu 配置界面
 */
public class ModMenuScreen extends Screen {

    private static final Option ENABLED = CyclingOption.create(
            "xhp.menu.option.enabled",
            o -> XHP.XOption.enabled,
            (g, o, v) -> XHP.XOption.enabled = v);

    private static final Option DISPLAY = CyclingOption.create(
            "xhp.menu.option.display", XOption.Display.values(),
            v -> new TranslatableText(v.key),
            o -> XHP.XOption.display, (g, o, v) -> XHP.XOption.display = v);

    private static final Option FOCUS_DELAY = new DoubleOption("xhp.menu.option.focus_delay", 0, 10, 0.1F,
            v -> XHP.XOption.focusDelay,
            (o, d) -> XHP.XOption.focusDelay = d,
            (g, o) -> new TranslatableText("xhp.menu.option.focus_delay", XHP.XOption.focusDelay));

    private static final Option TIPS = CyclingOption.create(
            "xhp.menu.option.tips",
            o -> XHP.XOption.tips,
            (g, o, v) -> XHP.XOption.tips = v);

    private static final Option TIPS_COLOR = new Option("xhp.menu.option.tips_color") {
        @Override
        public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
            return new ButtonWidget(x, y, width, 20, getDisplayPrefix(), b -> {
                if (INSTANCE.client != null) {
                    INSTANCE.client.setScreen(new ColorScreen(v -> XHP.XOption.tipsColor = v, XHP.XOption.tipsColor, INSTANCE));
                }
            });
        }
    };

    private static final Option TIPS_X = new DoubleOption("xhp.menu.option.tips_x", 0, MinecraftClient.getInstance().getWindow().getScaledWidth(), 1,
            v -> (double) XHP.XOption.tipsLocation[0],
            (o, d) -> XHP.XOption.tipsLocation[0] = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.tips_x", XHP.XOption.tipsLocation[0]));

    private static final Option TIPS_Y = new DoubleOption("xhp.menu.option.tips_y", 0, MinecraftClient.getInstance().getWindow().getScaledHeight(), 1,
            v -> (double) XHP.XOption.tipsLocation[1],
            (o, d) -> XHP.XOption.tipsLocation[1] = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.tips_y", XHP.XOption.tipsLocation[1]));

    private static final Option NAME = CyclingOption.create(
            "xhp.menu.option.name",
            o -> XHP.XOption.name,
            (g, o, v) -> XHP.XOption.name = v);

    private static final Option HP = CyclingOption.create("xhp.menu.option.hp",
            o -> XHP.XOption.hp, (g, o, v) -> XHP.XOption.hp = v);

    private static final Option VISUALIZATION = CyclingOption.create("xhp.menu.option.visualization",
            o -> XHP.XOption.visualization, (g, o, v) -> XHP.XOption.visualization = v);

    private static final Option DAMAGE = CyclingOption.create("xhp.menu.option.damage",
            o -> XHP.XOption.damage, (g, o, v) -> XHP.XOption.damage = v);

    private static final Option DISTANCE = new DoubleOption("xhp.menu.option.distance", 2, 128, 1,
            v -> (double) XHP.XOption.distance,
            (o, d) -> XHP.XOption.distance = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.distance", XHP.XOption.distance));

    private static final Option SCALE = new DoubleOption("xhp.menu.option.scale", 10, 40, 1,
            v -> (double) XHP.XOption.scale * 1000,
            (o, d) -> XHP.XOption.scale = d.intValue() / 1000F,
            (g, o) -> new TranslatableText("xhp.menu.option.scale", XHP.XOption.scale * 1000));

    private static final Option HEIGHT = new DoubleOption("xhp.menu.option.height", 0, 20, 1,
            v -> (double) XHP.XOption.height,
            (o, d) -> XHP.XOption.height = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.height", XHP.XOption.height));

    private static final Option STYLE = CyclingOption.create(
            "xhp.menu.option.style", XOption.StyleEnum.values(),
            v -> new TranslatableText(v.key),
            o -> XHP.XOption.style,
            (g, o, v) -> XHP.XOption.style = v);

    private static final Option BAR_LENGTH = new DoubleOption("xhp.menu.option.bar_length", 1, 20, 1,
            v -> (double) XHP.XOption.barLength,
            (o, d) -> XHP.XOption.barLength = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.bar_length", XHP.XOption.barLength));

    private static final Option BAR_HEIGHT = new DoubleOption("xhp.menu.option.bar_height", 1, 20, 1,
            v -> (double) XHP.XOption.barHeight,
            (o, d) -> XHP.XOption.barHeight = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.bar_height", XHP.XOption.barHeight));

    private static final Option FRIEND_COLOR = new Option("xhp.menu.option.friend_color") {
        @Override
        public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
            return new ButtonWidget(x, y, width, 20, getDisplayPrefix(), b -> {
                if (INSTANCE.client != null) {
                    INSTANCE.client.setScreen(new ColorScreen(v -> XHP.XOption.friendColor = v, XHP.XOption.friendColor, INSTANCE));
                }
            });
        }
    };

    private static final Option MOB_COLOR = new Option("xhp.menu.option.mob_color") {
        @Override
        public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
            return new ButtonWidget(x, y, width, 20, getDisplayPrefix(), b -> {
                if (INSTANCE.client != null) {
                    INSTANCE.client.setScreen(new ColorScreen(v -> XHP.XOption.mobColor = v, XHP.XOption.mobColor, INSTANCE));
                }
            });
        }
    };

    private static final Option EMPTY_COLOR = new Option("xhp.menu.option.empty_color") {
        @Override
        public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
            return new ButtonWidget(x, y, width, 20, getDisplayPrefix(), b -> {
                if (INSTANCE.client != null) {
                    INSTANCE.client.setScreen(new ColorScreen(v -> XHP.XOption.emptyColor = v, XHP.XOption.emptyColor, INSTANCE));
                }
            });
        }
    };

    private static final Option DAMAGE_COLOR = new Option("xhp.menu.option.damage_color") {
        @Override
        public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
            return new ButtonWidget(x, y, width, 20, getDisplayPrefix(), b -> {
                if (INSTANCE.client != null) {
                    INSTANCE.client.setScreen(new ColorScreen(v -> XHP.XOption.damageColor = v, XHP.XOption.damageColor, INSTANCE));
                }
            });
        }
    };

    private static final Option CRITIC_DAMAGE_COLOR = new Option("xhp.menu.option.critic_damage_color") {
        @Override
        public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
            return new ButtonWidget(x, y, width, 20, getDisplayPrefix(), b -> {
                if (INSTANCE.client != null) {
                    INSTANCE.client.setScreen(new ColorScreen(v -> XHP.XOption.criticDamageColor = v, XHP.XOption.criticDamageColor, INSTANCE));
                }
            });
        }
    };

    private static final Option IGNORE_ARMOR_STAND_ENTITY = CyclingOption.create("xhp.menu.option.ignore_armor_stand_entity",
            o -> XHP.XOption.ignoreArmorStandEntity, (g, o, v) -> XHP.XOption.ignoreArmorStandEntity = v);

    private static final ModMenuScreen INSTANCE = new ModMenuScreen();

    private ButtonListWidget list;

    private Screen pre;

    private ModMenuScreen() {
        super(new LiteralText(XHP.ID));
    }

    @Override
    protected void init() {
        Option[] options = {
                ENABLED, DISPLAY, FOCUS_DELAY, TIPS, TIPS_COLOR, TIPS_X, TIPS_Y,
                NAME, HP, VISUALIZATION, DAMAGE, BAR_LENGTH, BAR_HEIGHT, DISTANCE, SCALE, HEIGHT,
                STYLE, FRIEND_COLOR, MOB_COLOR, EMPTY_COLOR, DAMAGE_COLOR, CRITIC_DAMAGE_COLOR, IGNORE_ARMOR_STAND_ENTITY
        };
        list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        // 显示基础高度
        list.addAll(options);
        this.addSelectableChild(list);
        // 返回按钮
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 150 / 2, this.height - 28, 150, 20,
                ScreenTexts.BACK, b -> INSTANCE.close()));
    }

    public static ModMenuScreen get(Screen pre) {
        INSTANCE.width = pre.width;
        INSTANCE.height = pre.height;
        INSTANCE.pre = pre;
        return INSTANCE;
    }

    private void close() {
        if (client != null) {
            // 返回上个页面
            client.currentScreen = this.pre;
            // 保存设置的配置
            Config.write(XHP.ID, XHP.XOption);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // 画背景
        super.renderBackground(matrices);
        // 画其他
        list.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
