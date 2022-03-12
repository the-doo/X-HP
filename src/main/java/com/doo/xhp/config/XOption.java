package com.doo.xhp.config;

import com.doo.xhp.interfaces.Damageable;
import com.doo.xhp.renderer.HpRenderer;
import com.doo.xhp.util.HpUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;

import java.awt.*;
import java.util.List;
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
     * FTB Team
     */
    public boolean enableFTBTeam = true;

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
    public double focusDelay = 0.8;

    /**
     * tips
     */
    public boolean tips = true;

    /**
     * tips color
     */
    public int tipsColor = Color.RED.darker().getRGB();

    /**
     * tips location x, y
     */
    public int[] tipsLocation = {0, 20};

    /**
     * tips location middle x, y
     */
    public boolean[] tipsMiddle = {true, false};

    /**
     * tips scale
     */
    public int tipsScale = 10;

    /**
     * see through
     */
    public boolean seeThrough = false;

    /**
     * name
     */
    public boolean name = true;

    /**
     * hp
     */
    public boolean hp = true;

    /**
     * name and hp on one line
     */
    public boolean oneLine = true;

    /**
     * visualization
     */
    public boolean visualization = true;

    /**
     * style
     */
    public HpRenderer.BarStyleEnum style = HpRenderer.BarStyleEnum.BAR;

    /**
     * friendColor
     */
    public Integer friendColor = Color.ORANGE.getRGB();

    /**
     * mobColor
     */
    public Integer mobColor = Color.RED.getRGB();

    /**
     * EMPTY COLOR
     */
    public Integer emptyColor = Color.GRAY.getRGB();

    /**
     * damageColor
     */
    public Integer damageColor = Color.RED.getRGB();

    /**
     * criticDamageColor
     */
    public Integer criticDamageColor = Color.MAGENTA.getRGB();

    /**
     * barLength
     */
    public int barLength = 65;

    /**
     * barHeight
     */
    public int barHeight = 6;

    /**
     * damage
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
     * damage scale
     */
    public int damageScale = 8;

    /**
     * distance
     */
    public int distance = 128;

    /**
     * ignoreArmorStandEntity
     */
    public boolean ignoreArmorStandEntity = true;

    /**
     * DEFAULT_TIPS_TEMP
     */
    public static final String DEFAULT_TIPS_TEMP = "#n#: #h# / #mh# - Armor(#a#)";

    /**
     * tipsTemplate
     */
    public String tipsTemplate = DEFAULT_TIPS_TEMP;

    /**
     * tipsTemplate2
     */
    public String tipsTemplate2 = AttrKeyValue.DAMAGE.key;

    public enum AttrKeyValue {
        NAME("#n#", e -> e.getDisplayName().getString(), "xhp.menu.option.tips_temp_name"),
        HEALTH("#h#", e -> HpUtil.FORMATTER.format(e.getHealth()), "xhp.menu.option.tips_temp_health"),
        MAX_HEALTH("#mh#", e -> HpUtil.FORMATTER.format(e.getMaxHealth()), "xhp.menu.option.tips_temp_max_health"),
        ARMOR("#a#", e -> HpUtil.FORMATTER.format(e.getAttributeValue(EntityAttributes.GENERIC_ARMOR)), "xhp.menu.option.tips_temp_armor"),
        TOUGHNESS("#t#", e -> HpUtil.FORMATTER.format(e.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)), "xhp.menu.option.tips_temp_toughness"),
        DAMAGE("#d#", e -> {
            List<HpUtil.DamageR> damageList = ((Damageable) e).getDamageList();
            HpUtil.DamageR last;
            if (damageList.isEmpty() || e.world.getTime() - (last = damageList.get(damageList.size() - 1)).time() > 10) {
                return "";
            }
            return HpUtil.FORMATTER.format(last.damage());
        }, "xhp.menu.option.tips_temp_damage"),

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