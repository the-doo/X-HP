package com.doo.xhp.interfaces;

import com.doo.xhp.util.HpUtil;

import java.util.List;

/**
 * Damageable
 */
public interface Damageable {

    List<HpUtil.DamageR> getDamageList();
}
