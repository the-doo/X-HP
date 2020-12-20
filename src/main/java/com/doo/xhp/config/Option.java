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

    public boolean clickHp() {
        return hp = !hp;
    }

    public boolean clickHeart() {
        return heart = !heart;
    }

    public boolean clickDamage() {
        return damage = !damage;
    }
}
