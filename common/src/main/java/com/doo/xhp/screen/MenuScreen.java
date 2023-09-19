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
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.network.chat.*;
import net.minecraft.util.FastColor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
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

        addRenderableWidget(list);
        addRenderableWidget(new Button(this.width / 2 - 150 / 2, this.height - 28, 150, 20, CommonComponents.GUI_BACK, b -> close()));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(0);

        super.render(poseStack, i, j, f);

        if (list != null) {
            renderTooltip(poseStack, SimpleOptionsSubScreen.tooltipAt(list, i, j), i, j);
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
        return CycleOption.createOnOff(
                menuName,
                new TranslatableComponent(WithOption.menuTip(menuName)),
                o -> withOption.enabled(),
                (g, o, b) -> minecraft.setScreen(opt));
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
            return new ProgressOption(nameKey, 0, 100, 0.5F,
                    v -> value,
                    (o, d) -> entry.setValue(new JsonPrimitive(d)),
                    (g, o) -> new TranslatableComponent(nameKey).append(": ").append(String.valueOf(entry.getValue().getAsDouble())),
                    m -> Collections.singletonList(tooltip.getVisualOrderText())
            );
        } catch (NumberFormatException ex) {
            return CycleOption.createOnOff(
                    nameKey,
                    tooltip,
                    o -> json.getAsBoolean(),
                    (g, o, b) -> entry.setValue(new JsonPrimitive(b)));
        }
    }

    @NotNull
    private static <T extends Enum<T>> Option getEnumBtn(Map.Entry<String, JsonElement> entry, String key, String nameKey, Component tooltip) {
        Class<T> opt = opt(MenuOptType.ENUM, key);
        return CycleOption.create(
                        nameKey,
                        opt.getEnumConstants(),
                        arg -> new TranslatableComponent(WithOption.menuName(arg.name())),
                        arg -> WithOption.enumV(entry, opt),
                        (arg, arg2, arg3) -> entry.setValue(new JsonPrimitive(arg3.name())))
                .setTooltip(m -> arg -> {
                    MutableComponent tip = tooltip.copy().append(" - ")
                            .append(new TranslatableComponent(WithOption.menuNameTip(arg.name())));
                    return m.font.split(tip, 200);
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
        return "%s.%s".formatted(prefix, key);
    }

    @NotNull
    private Option getListBtn(Screen prev, String key, String nameKey, Component tooltip, JsonElement json) {
        Supplier<Stream<String>> supplier = opt(MenuOptType.LIST, key);
        JsonArray array = json.getAsJsonArray();

        return CycleOption.createOnOff(
                nameKey,
                tooltip,
                o -> supplier != null && !array.isEmpty(),
                (g, o, b) -> {
                    if (supplier == null) {
                        return;
                    }

                    // remove not exists element
                    List<String> list = supplier.get().toList();
                    Set<JsonElement> idx = new HashSet<>();
                    array.forEach(e -> {
                        if (!list.contains(e.getAsString())) {
                            idx.add(e);
                        }
                    });
                    idx.forEach(array::remove);

                    minecraft.setScreen(new OptionScreen(prev, () -> supplier.get()
                            .map(e -> CycleOption.createOnOff(
                                    e, TextComponent.EMPTY, o1 -> array.contains(new JsonPrimitive(e)),
                                    (g1, o1, b1) -> {
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
        return CycleOption.createOnOff(
                nameKey,
                tooltip.copy().withStyle(s -> s.withColor(json.getAsInt())),
                o -> true,
                (g, o, b) -> minecraft.setScreen(new OptionScreen(prev, () -> {
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
                        String name = formatted.formatted(i);
                        return new ProgressOption(name, 0, 255, 1F,
                                v -> Double.valueOf(colors[i]),
                                (o1, d) -> {
                                    colors[i] = d.intValue();
                                    kv.setValue(new JsonPrimitive(FastColor.ARGB32.color(colors[0], colors[1], colors[2], colors[3])));
                                },
                                (component, d) -> new TranslatableComponent(name).append(": ").append(String.valueOf(d)),
                                m -> Collections.singletonList(new TranslatableComponent(WithOption.menuTip(name)).getVisualOrderText())
                        );
                    }).toArray(Option[]::new);
                })));
    }
}
