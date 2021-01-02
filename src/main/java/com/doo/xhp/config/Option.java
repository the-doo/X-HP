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
     * 距离 --- 不保存此字段
     */
    public int distance = 128;

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
}
