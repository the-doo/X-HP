package com.doo.xhp.config;

/**
 * 设置选项
 */
public class Option {

    /**
     * 血量
     */
    public boolean hp = true;

    /**
     * 血量百分比
     */
    public boolean heart = true;

    /**
     * 伤害
     */
    public boolean damage = true;

    /**
     * 距离
     */
    public int distance = 128;

    /**
     * 缩放比例
     */
    public float scale = 0.02F;

    /**
     * 基础高度
     */
    public int height = 0;

    public boolean clickHp() {
        return hp = !hp;
    }

    public boolean clickHeart() {
        return heart = !heart;
    }

    public boolean clickDamage() {
        return damage = !damage;
    }

    public int clickDistance() {
        return (distance <<= 1) > 1024 ? distance = 8 : distance;
    }

    public float clickScale() {
        return (scale -= 0.001F) < 0.02F ? scale = 0.04F : scale;
    }

    public int clickHeight() {
        return (height += 1) > 20 ? height = 0 : height;
    }
}
