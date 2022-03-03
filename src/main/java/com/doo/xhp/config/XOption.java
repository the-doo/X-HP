package com.doo.xhp.config;

import com.doo.xhp.renderer.HpRenderer;
import com.doo.xhp.util.HpUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;

import java.awt.*;
import java.util.function.Function;

/**
 * 设置选项
 */
public class XOption {

    /**
     * enabled
     */
    public boolean enabled = true;

    /**
     * sync with hud
     */
    public boolean syncWithHud = true;

    /**
     * sync with hide
     */
    public boolean syncWithHide = true;

    /**
     * display
     */
    public Display display = Display.FOCUS;

    /**
     * Display
     */
    public enum Display {
        FOCUS("xhp.menu.display.focus"),
        DISTANCE("xhp.menu.display.distance");

        public final String key;

        Display(String key) {
            this.key = key;
        }

        public static Display get(Integer index) {
            return values()[index % values().length];
        }
    }

    /**
     * disappear delay time on focus
     */
    public double focusDelay = 1.2;

    /**
     * tips
     */
    public boolean tips = true;

    /**
     * tips color
     */
    public int tipsColor = Color.BLUE.getRGB();

    /**
     * tips location x, y
     */
    public int[] tipsLocation = {0, 0};

    /**
     * see through
     */
    public boolean seeThrough = true;

    /**
     * 名字
     */
    public boolean name = true;

    /**
     * 血量
     */
    public boolean hp = true;

    /**
     * name and hp on one line
     */
    public boolean oneLine = true;

    /**
     * 血量可视化
     */
    public boolean visualization = true;

    /**
     * 血条还是图片
     */
    public HpRenderer.BarStyleEnum style = HpRenderer.BarStyleEnum.BAR;

    /**
     * 中立生物血条颜色
     */
    public Integer friendColor = Color.ORANGE.getRGB();

    /**
     * 敌对生物血条颜色
     */
    public Integer mobColor = Color.RED.getRGB();

    /**
     * EMPTY COLOR
     */
    public Integer emptyColor = Color.GRAY.getRGB();

    /**
     * 伤害颜色
     */
    public Integer damageColor = Color.RED.getRGB();

    /**
     * 暴击伤害颜色
     */
    public Integer criticDamageColor = Color.MAGENTA.getRGB();

    /**
     * 血条长度
     */
    public int barLength = 65;

    /**
     * 血条高度
     */
    public int barHeight = 6;

    /**
     * 伤害
     */
    public boolean damage = true;

    /**
     * damage follow display mode
     */
    public boolean damageFollow = false;

    /**
     * Damage From Middle
     */
    public boolean damageFromMiddle = true;

    /**
     * 距离
     */
    public int distance = 128;

    /**
     * 忽略盔甲架
     */
    public boolean ignoreArmorStandEntity = true;

    /**
     * 提示模板
     */
    public static final String DEFAULT_TIPS_TEMP = "#n#: #h# / #mh# - Armor(#a#)";

    /**
     * 提示模板
     */
    public String tipsTemplate = DEFAULT_TIPS_TEMP;

    public enum AttrKeyValue {
        NAME("#n#", e -> e.getDisplayName().getString(), "xhp.menu.option.tips_temp_name"),
        HEALTH("#h#", e -> HpUtil.FORMATTER.format(e.getHealth()), "xhp.menu.option.tips_temp_health"),
        MAX_HEALTH("#mh#", e -> HpUtil.FORMATTER.format(e.getMaxHealth()), "xhp.menu.option.tips_temp_max_health"),
        ARMOR("#a#", e -> HpUtil.FORMATTER.format(e.getAttributeValue(EntityAttributes.GENERIC_ARMOR)), "xhp.menu.option.tips_temp_armor"),
        TOUGHNESS("#t#", e -> HpUtil.FORMATTER.format(e.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)), "xhp.menu.option.tips_temp_toughness"),

        ;

        public final String key;
        public final Function<LivingEntity, String> valueGetter;
        public final String transactionKey;

        AttrKeyValue(String key, Function<LivingEntity, String> valueGetter, String transactionKey) {
            this.key = key;
            this.transactionKey = transactionKey;
            this.valueGetter = valueGetter;
        }
    }
}