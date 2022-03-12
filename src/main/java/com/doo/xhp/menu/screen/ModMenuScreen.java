package com.doo.xhp.menu.screen;

import com.doo.xhp.XHP;
import com.doo.xhp.config.Config;
import com.doo.xhp.config.XOption;
import com.doo.xhp.renderer.HpRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * mod menu 配置界面
 */
public class ModMenuScreen extends Screen {

    private static final Option ENABLED =
//            CyclingOption.create("xhp.menu.option.enabled",
//                    o -> XHP.XOption.enabled, (g, o, v) -> XHP.XOption.enabled = v);
            new BooleanOption("xhp.menu.option.enabled",
                    o -> XHP.XOption.enabled, (g, v) -> XHP.XOption.enabled = v);

    private static final Option DISPLAY =
//            CyclingOption.create(
//                    "xhp.menu.option.display", XOption.Display.values(),
//                    v -> new TranslatableText(v.key),
//                    o -> XHP.XOption.display, (g, o, v) -> XHP.XOption.display = v);
            new CyclingOption("xhp.menu.option.display",
                    (o, v) -> XHP.XOption.display = XOption.Display.get(XHP.XOption.display.ordinal() + 1),
                    (g, o) -> new TranslatableText(XHP.XOption.display.key));

    private static final Option SYNC_WITH_HUD =
//            CyclingOption.create(
//                    "xhp.menu.option.sync_with_hud",
//                    o -> XHP.XOption.syncWithHud,
//                    (g, o, v) -> XHP.XOption.syncWithHud = v);
            new BooleanOption("xhp.menu.option.sync_with_hud",
                    o -> XHP.XOption.syncWithHud, (g, v) -> XHP.XOption.syncWithHud = v);

    private static final Option SYNC_WITH_HIDE =
//            CyclingOption.create(
//                    "xhp.menu.option.sync_with_hide",
//                    o -> XHP.XOption.syncWithHide,
//                    (g, o, v) -> XHP.XOption.syncWithHide = v);
            new BooleanOption("xhp.menu.option.sync_with_hide",
                    o -> XHP.XOption.syncWithHide, (g, v) -> XHP.XOption.syncWithHide = v);

    private static final Option FOCUS_DELAY =
            new DoubleOption("xhp.menu.option.focus_delay", 0, 10, 0.1F,
                    v -> XHP.XOption.focusDelay,
                    (o, d) -> XHP.XOption.focusDelay = d,
                    (g, o) -> new TranslatableText("xhp.menu.option.focus_delay", XHP.XOption.focusDelay));


    private static final Option TIPS_SETTINGS =
//            new Option("xhp.menu.option.tips_settings") {
//                @Override
//                public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
//                    return new ButtonWidget(x, y, width, 20, getDisplayPrefix(), b -> {
//                        if (INSTANCE.client != null) {
//                            INSTANCE.client.setScreen(TipsScreen.get(INSTANCE));
//                        }
//                    });
//                }
//            };
            new Option("xhp.menu.option.tips_settings") {
                @Override
                public AbstractButtonWidget createButton(GameOptions options, int x, int y, int width) {
                    return new ButtonWidget(x, y, width, 20, getDisplayPrefix(), b -> {
                        if (INSTANCE.client != null) {
                            INSTANCE.client.currentScreen = TipsScreen.get(INSTANCE);
                        }
                    });
                }
            };

    private static final Option SEE_THROUGH =
//            CyclingOption.create(
//                    "xhp.menu.option.see_through",
//                    o -> XHP.XOption.seeThrough,
//                    (g, o, v) -> XHP.XOption.seeThrough = v);
            new BooleanOption("xhp.menu.option.see_through",
                    o -> XHP.XOption.seeThrough, (g, v) -> XHP.XOption.seeThrough = v);

    private static final Option NAME =
//            CyclingOption.create(
//                    "xhp.menu.option.name",
//                    o -> XHP.XOption.name,
//                    (g, o, v) -> XHP.XOption.name = v);
            new BooleanOption("xhp.menu.option.name",
                    o -> XHP.XOption.name, (g, v) -> XHP.XOption.name = v);

    private static final Option HP =
//            CyclingOption.create("xhp.menu.option.hp",
//                    o -> XHP.XOption.hp, (g, o, v) -> XHP.XOption.hp = v);
            new BooleanOption("xhp.menu.option.hp",
                    o -> XHP.XOption.hp, (g, v) -> XHP.XOption.hp = v);

    private static final Option VISUALIZATION =
//            CyclingOption.create("xhp.menu.option.visualization",
//                    o -> XHP.XOption.visualization, (g, o, v) -> XHP.XOption.visualization = v);
            new BooleanOption("xhp.menu.option.visualization",
                    o -> XHP.XOption.visualization, (g, v) -> XHP.XOption.visualization = v);

    private static final Option DAMAGE =
//            CyclingOption.create("xhp.menu.option.damage",
//                    o -> XHP.XOption.damage, (g, o, v) -> XHP.XOption.damage = v);
            new BooleanOption("xhp.menu.option.damage",
                    o -> XHP.XOption.damage, (g, v) -> XHP.XOption.damage = v);

    private static final Option DAMAGE_FOLLOW =
//            CyclingOption.create("xhp.menu.option.damage_follow",
//                    o -> XHP.XOption.damageFollow, (g, o, v) -> XHP.XOption.damageFollow = v);
            new BooleanOption("xhp.menu.option.damage_follow",
                    o -> XHP.XOption.damageFollow, (g, v) -> XHP.XOption.damageFollow = v);

    private static final Option DAMAGE_FROM_MIDDLE =
//            CyclingOption.create("xhp.menu.option.damage_from_middle",
//                    o -> XHP.XOption.damageFromMiddle, (g, o, v) -> XHP.XOption.damageFromMiddle = v);
            new BooleanOption("xhp.menu.option.damage_from_middle",
                    o -> XHP.XOption.damageFromMiddle, (g, v) -> XHP.XOption.damageFromMiddle = v);

    private static final Option DAMAGE_SCALE = new DoubleOption("xhp.menu.option.damage_scale", 1, 30, 1,
            v -> (double) XHP.XOption.damageScale, (o, d) -> XHP.XOption.damageScale = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.damage_scale", XHP.XOption.damageScale));

    private static final Option ONE_LINE =
//            CyclingOption.create("xhp.menu.option.one_line",
//                    o -> XHP.XOption.oneLine, (g, o, v) -> XHP.XOption.oneLine = v);
            new BooleanOption("xhp.menu.option.one_line",
                    o -> XHP.XOption.oneLine, (g, v) -> XHP.XOption.oneLine = v);

    private static final Option DISTANCE = new DoubleOption("xhp.menu.option.distance", 2, 128, 1,
            v -> (double) XHP.XOption.distance,
            (o, d) -> XHP.XOption.distance = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.distance", XHP.XOption.distance));

    private static final Option STYLE =
//            CyclingOption.create("xhp.menu.option.style", HpRenderer.BarStyleEnum.values(),
//                    v -> new TranslatableText(v.key), o -> XHP.XOption.style,
//                    (g, o, v) -> XHP.XOption.style = v);
            new CyclingOption("xhp.menu.option.style",
                    (o, v) -> XHP.XOption.style = HpRenderer.BarStyleEnum.get(XHP.XOption.style.ordinal() + 1),
                    (g, o) -> new TranslatableText(XHP.XOption.style.key));

    private static final Option BAR_LENGTH = new DoubleOption("xhp.menu.option.bar_length", 1, 100, 1,
            v -> (double) XHP.XOption.barLength,
            (o, d) -> XHP.XOption.barLength = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.bar_length", XHP.XOption.barLength));

    private static final Option BAR_HEIGHT = new DoubleOption("xhp.menu.option.bar_height", 1, 40, 1,
            v -> (double) XHP.XOption.barHeight,
            (o, d) -> XHP.XOption.barHeight = d.intValue(),
            (g, o) -> new TranslatableText("xhp.menu.option.bar_height", XHP.XOption.barHeight));

    private static final Option FRIEND_COLOR = new ColorOption("xhp.menu.option.friend_color",
            v -> XHP.XOption.friendColor = v, () -> XHP.XOption.friendColor);

    private static final Option MOB_COLOR = new ColorOption("xhp.menu.option.mob_color",
            v -> XHP.XOption.mobColor = v, () -> XHP.XOption.mobColor);

    private static final Option EMPTY_COLOR = new ColorOption("xhp.menu.option.empty_color",
            v -> XHP.XOption.emptyColor = v, () -> XHP.XOption.emptyColor);

    private static final Option DAMAGE_COLOR = new ColorOption("xhp.menu.option.damage_color",
            v -> XHP.XOption.damageColor = v, () -> XHP.XOption.damageColor);

    private static final Option CRITIC_DAMAGE_COLOR = new ColorOption("xhp.menu.option.critic_damage_color",
            v -> XHP.XOption.criticDamageColor = v, () -> XHP.XOption.criticDamageColor);

    private static final Option IGNORE_ARMOR_STAND_ENTITY =
//            CyclingOption.create("xhp.menu.option.ignore_armor_stand_entity",
//                    o -> XHP.XOption.ignoreArmorStandEntity, (g, o, v) -> XHP.XOption.ignoreArmorStandEntity = v);
            new BooleanOption("xhp.menu.option.ignore_armor_stand_entity",
                    o -> XHP.XOption.ignoreArmorStandEntity, (g, v) -> XHP.XOption.ignoreArmorStandEntity = v);

    private static final Option ENABLED_FTB_TEAM =
//            CyclingOption.create("xhp.menu.option.enabled_ftb_team",
//                    o -> XHP.XOption.enableFTBTeam, (g, o, v) -> XHP.XOption.enableFTBTeam = v);
            new BooleanOption("xhp.menu.option.enabled_ftb_team",
                    o -> XHP.XOption.enableFTBTeam, (g, v) -> XHP.XOption.enableFTBTeam = v);

    private static final ModMenuScreen INSTANCE = new ModMenuScreen();

    private ButtonListWidget list;

    private Screen pre;

    private ModMenuScreen() {
        super(new LiteralText(XHP.ID));
    }

    @Override
    protected void init() {
        Option[] base = {
                ENABLED, DISPLAY,
                SYNC_WITH_HUD, SYNC_WITH_HIDE,
                FOCUS_DELAY, TIPS_SETTINGS,
                SEE_THROUGH, NAME,
                HP, VISUALIZATION,
                DAMAGE, DAMAGE_FOLLOW,
                ONE_LINE, DISTANCE,
                DAMAGE_FROM_MIDDLE, DAMAGE_SCALE
        };
        Option[] icon = {
                STYLE, BAR_LENGTH,
                BAR_HEIGHT, FRIEND_COLOR,
                MOB_COLOR, EMPTY_COLOR,
                DAMAGE_COLOR, CRITIC_DAMAGE_COLOR
        };
        Option[] other = {
                IGNORE_ARMOR_STAND_ENTITY
        };
        list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);

        list.addAll(base);
        list.addAll(icon);
        list.addAll(other);
        if (XHP.hasFTBTeam) {
            list.addSingleOptionEntry(ENABLED_FTB_TEAM);
        }

//        this.addSelectableChild(list);
//
//        this.addDrawableChild(new ButtonWidget(this.width / 2 - 150 / 2, this.height - 28, 150, 20,
//                ScreenTexts.BACK, b -> INSTANCE.close()));

        this.addChild(list);
        this.addButton(new ButtonWidget(this.width / 2 - 150 / 2, this.height - 28, 150, 20,
                ScreenTexts.BACK, b -> INSTANCE.close()));
    }

    public static ModMenuScreen get(Screen pre) {
        INSTANCE.width = pre.width;
        INSTANCE.height = pre.height;
        INSTANCE.pre = pre;
        return INSTANCE;
    }

    public void close() {
        if (client != null) {
            client.currentScreen = this.pre;
            Config.write(XHP.ID, XHP.XOption);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        list.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public static final class ColorOption extends Option {

        private final Function<Integer, Integer> setter;
        private final Supplier<Integer> getter;

        public ColorOption(String key, Function<Integer, Integer> colorSetter, Supplier<Integer> colorGetter) {
            super(key);

            setter = colorSetter;
            getter = colorGetter;
        }

        //        @Override
//        public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
        @Override
        public AbstractButtonWidget createButton(GameOptions options, int x, int y, int width) {
            return new ButtonWidget(x, y, width, 20, getDisplayPrefix(), b -> {
                if (INSTANCE.client != null) {
                    INSTANCE.client.currentScreen = new ColorScreen(setter, getter.get(), INSTANCE);
//                    INSTANCE.client.setScreen(new ColorScreen(setter, getter.get(), INSTANCE));
                }
            });
        }
    }
}
