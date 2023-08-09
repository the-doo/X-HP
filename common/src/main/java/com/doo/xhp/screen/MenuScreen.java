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
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

    private MenuScreen(Screen prev) {
        super(Component.empty());

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
        OptionsList list = new OptionsList(minecraft, w, this.height, 32, this.height - 32, 25);

        OptionInstance<?>[] opts = WithOption.CONFIG.entrySet().stream()
                .map(this::opt)
                .toArray(OptionInstance<?>[]::new);
        list.addSmall(opts);

        addRenderableWidget(list);
        addRenderableWidget(new Button.Builder(CommonComponents.GUI_BACK, b -> close()).bounds(this.width / 2 - 150 / 2, this.height - 28, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        renderDirtBackground(guiGraphics);

        super.render(guiGraphics, i, j, f);
    }

    public void close() {
        minecraft.setScreen(prev);
        ConfigUtil.write(WithOption.CONFIG);
        XHP.reloadConfig(false);
    }

    private OptionInstance<?> opt(Map.Entry<String, JsonElement> entry) {
        String key = entry.getKey();
        // With Option
        WithOption withOption = HealthRenders.name(key).getRender();
        if (withOption == null && XHP.isTip(key)) {
            withOption = HealthRenderUtil.TIP_HEAL_RENDER;
        }
        if (withOption != null) {
            String menuName = WithOption.menuName(key);
            return withOpsBtn(entry, menuName, withOption, key);
        }

        return getBaseOptionInstance(this, key, entry);
    }

    @NotNull
    private OptionInstance<?> withOpsBtn(Map.Entry<String, JsonElement> entry, String menuName, WithOption withOption, String key) {
        OptionScreen opt = new OptionScreen(this);
        opt.setOpsGetter(() -> entry.getValue().getAsJsonObject().entrySet().stream()
                .map(e -> getBaseOptionInstance(opt, MenuScreen.getNameKey(key, e.getKey()), e))
                .toArray(OptionInstance[]::new));
        return OptionInstance.createBoolean(
                menuName,
                OptionInstance.cachedConstantTooltip(Component.translatable(WithOption.menuTip(menuName))),
                withOption.enabled(),
                b -> minecraft.setScreen(opt));
    }

    @NotNull
    protected OptionInstance<?> getBaseOptionInstance(Screen prev, String key, Map.Entry<String, JsonElement> entry) {
        // other value
        JsonElement json = entry.getValue();
        String nameKey = switchName(key);
        MutableComponent tooltip = Component.translatable(WithOption.menuTip(nameKey));

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

            return new OptionInstance<>(
                    nameKey,
                    OptionInstance.cachedConstantTooltip(tooltip),
                    (component, d) -> Component.translatable(nameKey).append(": ").append(String.valueOf(d)),
                    new OptionInstance.IntRange(0, 200).xmap(i -> i / 2D, d -> (int) (d * 2)),
                    value,
                    d -> entry.setValue(new JsonPrimitive(d))
            );
        } catch (NumberFormatException ex) {
            return OptionInstance.createBoolean(
                    nameKey,
                    OptionInstance.cachedConstantTooltip(tooltip),
                    json.getAsBoolean(),
                    b -> entry.setValue(new JsonPrimitive(b)));
        }
    }

    @NotNull
    private static <T extends Enum<T>> OptionInstance<?> getEnumBtn(Map.Entry<String, JsonElement> entry, String key, String nameKey, MutableComponent tooltip) {
        Class<T> opt = opt(MenuOptType.ENUM, key);
        return new OptionInstance<>(
                nameKey,
                t -> Tooltip.create(tooltip.copy().append(" - ").append(Component.translatable(WithOption.menuNameTip(t.name())))),
                (arg, arg2) -> Component.translatable(WithOption.menuName(arg2.name())),
                altEnum(opt.getEnumConstants()),
                WithOption.enumV(entry, opt),
                arg -> entry.setValue(new JsonPrimitive(arg.name())));
    }

    public static <T extends Enum<T>> OptionInstance.AltEnum<T> altEnum(T[] constants) {
        return new OptionInstance.AltEnum<>(
                Arrays.asList(constants),
                Stream.of(constants).toList(),
                () -> true,
                OptionInstance::set,
                Codec.INT.xmap(i -> constants[i], Enum::ordinal));
    }

    private String switchName(String key) {
        if (key.endsWith(XHP.ENABLED_KEY)) {
            return WithOption.menuName(XHP.ENABLED_KEY);
        }
        if (key.endsWith(HealRender.BASE_Y_KEY)) {
            return WithOption.menuName(HealRender.BASE_Y_KEY);
        }
        if (key.endsWith(HealRender.DAMAGE_KEY)) {
            return WithOption.menuName(HealRender.DAMAGE_KEY);
        }
        if (key.endsWith(HealRender.DAMAGE_SPEED_KEY)) {
            return WithOption.menuName(HealRender.DAMAGE_SPEED_KEY);
        }
        if (key.endsWith(HealRender.WRAPPER_KEY)) {
            return WithOption.menuName(HealRender.WRAPPER_KEY);
        }
        if (key.endsWith(HealRender.DAMAGE_COLOR_KEY)) {
            return WithOption.menuName(HealRender.DAMAGE_COLOR_KEY);
        }
        if (key.endsWith(HealRender.HEAL_COLOR_KEY)) {
            return WithOption.menuName(HealRender.HEAL_COLOR_KEY);
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
    private OptionInstance<Boolean> getListBtn(Screen prev, String key, String nameKey, MutableComponent tooltip, JsonElement json) {
        Supplier<Stream<String>> supplier = opt(MenuOptType.LIST, key);
        JsonArray array = json.getAsJsonArray();
        return OptionInstance.createBoolean(
                nameKey,
                OptionInstance.cachedConstantTooltip(tooltip),
                supplier != null && !array.isEmpty(),
                clicked -> {
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
                            .map(e -> OptionInstance.createBoolean(
                                    e, OptionInstance.noTooltip(), array.contains(new JsonPrimitive(e)),
                                    b -> {
                                        array.remove(new JsonPrimitive(e));
                                        if (b) {
                                            array.add(e);
                                        }
                                    }))
                            .toArray(OptionInstance[]::new)));
                });
    }

    @NotNull
    private OptionInstance<?> getColorBtn(Screen prev, String nameKey, MutableComponent tooltip, Map.Entry<String, JsonElement> kv) {
        JsonElement json = kv.getValue();
        return OptionInstance.createBoolean(
                nameKey,
                b -> Tooltip.create(tooltip.withStyle(s -> s.withColor(json.getAsInt()))),
                true,
                b -> minecraft.setScreen(new OptionScreen(prev, () -> {
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
                        return new OptionInstance<>(
                                name,
                                OptionInstance.cachedConstantTooltip(Component.translatable(WithOption.menuTip(name))),
                                (component, d) -> Component.translatable(name).append(": ").append(String.valueOf(d)),
                                new OptionInstance.IntRange(0, 255).xmap(v -> v, v -> v),
                                c,
                                d -> {
                                    colors[i] = d;
                                    kv.setValue(new JsonPrimitive(FastColor.ARGB32.color(colors[0], colors[1], colors[2], colors[3])));
                                });
                    }).toArray(OptionInstance<?>[]::new);
                })));
    }
}
