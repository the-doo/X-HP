package com.doo.xhp.screen;

import com.doo.xhp.XHP;
import com.doo.xhp.enums.HealthRenders;
import com.doo.xhp.enums.MenuOptType;
import com.doo.xhp.interfaces.WithOption;
import com.doo.xhp.render.HealRender;
import com.doo.xhp.util.ConfigUtil;
import com.doo.xhp.util.HealthRenderUtil;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MenuScreen extends Screen {

    private static final Map<MenuOptType, Map<String, Object>> OPT_TYPE_MAP = Maps.newHashMap();

    private final Screen prev;

    private OptionsList list;

    private MenuScreen(Screen prev) {
        super(TextComponent.EMPTY);

        this.prev = prev;
    }

    public static <T> void register(MenuOptType type, String title, String key, T value) {
        OPT_TYPE_MAP.compute(type, (k, v) -> {
            if (v == null) {
                v = Maps.newHashMap();
            }

            v.put(getNameKey(title, key), value);
            return v;
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> T opt(MenuOptType type, String key) {
        if (!OPT_TYPE_MAP.containsKey(type)) {
            return null;
        }
        return (T) OPT_TYPE_MAP.get(type).get(key);
    }

    public static MenuScreen get(Screen prev) {
        return new MenuScreen(prev);
    }

    public static void open(Minecraft minecraft) {
        minecraft.setScreen(get(minecraft.screen));
    }

    @Override
    protected void init() {
        int w = this.width;
        list = new OptionsList(minecraft, w, this.height, 32, this.height - 32, 25);

        Option[] opts = WithOption.CONFIG.entrySet().stream()
                .map(this::opt)
                .toArray(Option[]::new);
        list.addSmall(opts);

        children.add(list);
        addButton(new Button(this.width / 2 - 150 / 2, this.height - 28, 150, 20, CommonComponents.GUI_BACK, b -> close()));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(0);

        list.render(poseStack, i, j, f);

        super.render(poseStack, i, j, f);

        List<FormattedCharSequence> tooltip;
        if ((tooltip = OptionsSubScreen.tooltipAt(list, i, j)) != null) {
            renderTooltip(poseStack, tooltip, i, j);
        }
    }

    public void close() {
        minecraft.setScreen(prev);
        ConfigUtil.write(WithOption.CONFIG);
        XHP.reloadConfig(false);
    }

    private Option opt(Map.Entry<String, JsonElement> entry) {
        String key = entry.getKey();
        // With Option
        WithOption withOption = HealthRenders.name(key).getRender();
        if (withOption == null && XHP.isTip(key)) {
            withOption = HealthRenderUtil.TIP_HEAL_RENDER;
        }
        if (withOption == null && XHP.isDamage(key)) {
            withOption = HealthRenderUtil.DAMAGE_RENDER;
        }
        if (withOption != null) {
            String menuName = WithOption.menuName(key);
            return withOpsBtn(entry, menuName, withOption, key);
        }

        return getBaseOptionInstance(this, key, entry);
    }

    @NotNull
    private Option withOpsBtn(Map.Entry<String, JsonElement> entry, String menuName, WithOption withOption, String key) {
        OptionScreen opt = new OptionScreen(this);
        opt.setOpsGetter(() -> entry.getValue().getAsJsonObject().entrySet().stream()
                .map(e -> getBaseOptionInstance(opt, MenuScreen.getNameKey(key, e.getKey()), e))
                .toArray(Option[]::new));
        return new BooleanOption(
                menuName,
                new TranslatableComponent(WithOption.menuTip(menuName)),
                o -> withOption.enabled(),
                (g, b) -> minecraft.setScreen(opt));
    }

    @NotNull
    protected Option getBaseOptionInstance(Screen prev, String key, Map.Entry<String, JsonElement> entry) {
        // other value
        JsonElement json = entry.getValue();
        String nameKey = switchName(key);
        Component tooltip = new TranslatableComponent(WithOption.menuTip(nameKey));

        if (json.isJsonArray()) {
            return getListBtn(prev, key, nameKey, tooltip, json);
        }

        if (opt(MenuOptType.ENUM, key) != null) {
            return getEnumBtn(entry, key, nameKey, tooltip);
        }

        if (opt(MenuOptType.COLOR, key) != null) {
            return getColorBtn(prev, nameKey, tooltip, entry);
        }

        try {
            double value = json.getAsDouble();
            ProgressOption option = new ProgressOption(nameKey, 0, 100, 0.5F,
                    v -> value,
                    (o, d) -> entry.setValue(new JsonPrimitive(d)),
                    (g, o) -> new TranslatableComponent(nameKey).append(": ").append(entry.getValue().getAsString())
            );
            option.setTooltip(Collections.singletonList(tooltip.getVisualOrderText()));
            return option;
        } catch (NumberFormatException ex) {
            return new BooleanOption(
                    nameKey,
                    tooltip,
                    o -> entry.getValue().getAsBoolean(),
                    (g, b) -> entry.setValue(new JsonPrimitive(b)));
        }
    }

    @NotNull
    private static <T extends Enum<T>> Option getEnumBtn(Map.Entry<String, JsonElement> entry, String key, String nameKey, Component tooltip) {
        Class<T> opt = opt(MenuOptType.ENUM, key);
        T[] constants = opt.getEnumConstants();
        MutableInt idx = new MutableInt(WithOption.enumV(entry, opt).ordinal());
        return new CycleOption(
                nameKey,
                (a, b) -> {
                    idx.setValue(idx.incrementAndGet() % constants.length);
                    T constant = constants[idx.intValue()];
                    entry.setValue(new JsonPrimitive(constant.name()));
                },
                (o, b) -> {
                    T t = WithOption.enumV(entry, opt);
                    String name = WithOption.menuName(t.name());
                    MutableComponent tip = tooltip.copy().append(" - ")
                            .append(new TranslatableComponent(WithOption.menuTip(name)));
                    b.setTooltip(Minecraft.getInstance().font.split(tip, 200));
                    return new TranslatableComponent(nameKey).append(": ").append(new TranslatableComponent(name));
                });
    }

    private String switchName(String key) {
        if (key.endsWith(XHP.ENABLED_KEY)) {
            return WithOption.menuName(XHP.ENABLED_KEY);
        }
        if (key.endsWith(HealRender.BASE_SCALE_KEY)) {
            return WithOption.menuName(HealRender.BASE_SCALE_KEY);
        }
        if (key.endsWith(HealRender.BASE_Y_KEY)) {
            return WithOption.menuName(HealRender.BASE_Y_KEY);
        }
        if (key.endsWith(HealRender.WRAPPER_KEY)) {
            return WithOption.menuName(HealRender.WRAPPER_KEY);
        }
        if (key.endsWith(HealRender.TEXT_KEY)) {
            return WithOption.menuName(HealRender.TEXT_KEY);
        }
        if (key.endsWith(HealRender.TEXT_SEE_KEY)) {
            return WithOption.menuName(HealRender.TEXT_SEE_KEY);
        }
        if (key.endsWith(HealRender.TEXT_COLOR_KEY)) {
            return WithOption.menuName(HealRender.TEXT_COLOR_KEY);
        }
        if (key.endsWith(HealRender.P_KEY)) {
            return WithOption.menuName(HealRender.P_KEY);
        }

        return WithOption.menuName(key);
    }

    public static String getNameKey(String prefix, String key) {
        if (StringUtils.isEmpty(prefix)) {
            return key;
        }
        return String.format("%s.%s", prefix, key);
    }

    @NotNull
    private Option getListBtn(Screen prev, String key, String nameKey, Component tooltip, JsonElement json) {
        Supplier<Stream<String>> supplier = opt(MenuOptType.LIST, key);
        JsonArray array = json.getAsJsonArray();

        return new BooleanOption(
                nameKey,
                tooltip,
                o -> supplier != null && array.size() > 0,
                (g, b) -> {
                    if (supplier == null) {
                        return;
                    }

                    // remove not exists element
                    List<String> list = supplier.get().collect(Collectors.toList());
                    Set<JsonElement> idx = new HashSet<>();
                    array.forEach(e -> {
                        if (!list.contains(e.getAsString())) {
                            idx.add(e);
                        }
                    });
                    idx.forEach(array::remove);

                    minecraft.setScreen(new OptionScreen(prev, () -> supplier.get()
                            .map(e -> new BooleanOption(
                                    e, TextComponent.EMPTY, o1 -> array.contains(new JsonPrimitive(e)),
                                    (g1, b1) -> {
                                        array.remove(new JsonPrimitive(e));
                                        if (b1) {
                                            array.add(e);
                                        }
                                    }))
                            .toArray(Option[]::new)));
                });
    }

    @NotNull
    private Option getColorBtn(Screen prev, String nameKey, Component tooltip, Map.Entry<String, JsonElement> kv) {
        JsonElement json = kv.getValue();
        return new BooleanOption(
                nameKey,
                tooltip.copy().withStyle(s -> s.withColor(TextColor.fromRgb(json.getAsInt()))),
                o -> true,
                (g, b) -> minecraft.setScreen(new OptionScreen(prev, () -> {
                    int color = json.getAsInt();
                    String formatted = WithOption.menuName("color.%d");
                    MutableInt idx = new MutableInt();
                    Integer[] colors = {
                            FastColor.ARGB32.alpha(color),
                            FastColor.ARGB32.red(color),
                            FastColor.ARGB32.green(color),
                            FastColor.ARGB32.blue(color)
                    };
                    return Arrays.stream(colors).map(c -> {
                        int i = idx.getAndIncrement();
                        String name = String.format(formatted, i);
                        ProgressOption option = new ProgressOption(name, 0, 255, 1F,
                                v -> Double.valueOf(colors[i]),
                                (o1, d) -> {
                                    colors[i] = d.intValue();
                                    kv.setValue(new JsonPrimitive(FastColor.ARGB32.color(colors[0], colors[1], colors[2], colors[3])));
                                },
                                (g1, o1) -> new TranslatableComponent(name).append(": ").append(o1.toString())
                        );
                        option.setTooltip(Collections.singletonList(new TranslatableComponent(WithOption.menuTip(name)).getVisualOrderText()));
                        return option;
                    }).toArray(Option[]::new);
                })));
    }
}
