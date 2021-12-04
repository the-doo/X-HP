package com.doo.xhp.util;

import com.doo.xhp.XHP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public abstract class HpUtil {

    private static final Map<Integer, Deque<DamageTaken>> LAST_DAMAGE_TAKEN_MAP = new HashMap<>();

    private static final ConcurrentLinkedDeque<DamageTaken> EMPTY = new ConcurrentLinkedDeque<>();

    private static final Random random = new Random();

    public static final int BASE_HEIGHT = 15;
    public static final int HEALTH = 10;

    public static void set(int id, float width, float height, Entity attacker, float damage, long age) {
        // 设置显示位置
        int x = random.nextInt(BASE_HEIGHT) + (int) (width * BASE_HEIGHT);
        int y = getShowY(height, false);
        x = random.nextBoolean() ? -x : x;
        y = random.nextInt(y / 2) + y / 4;
        int attackerId = attacker == null ? -1 : attacker.getId();
        // 添加队列
        if (!LAST_DAMAGE_TAKEN_MAP.containsKey(id)) {
            LAST_DAMAGE_TAKEN_MAP.put(id, new ConcurrentLinkedDeque<>());
        }
        // 添加当前伤害对象
        Deque<DamageTaken> result = LAST_DAMAGE_TAKEN_MAP.get(id);
        // 如果是暴击
        boolean isCritic = attacker instanceof ServerPlayerEntity
                && damage > ((ServerPlayerEntity) attacker).getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        int rgb = isCritic ? XHP.XOption.criticDamageColor : XHP.XOption.damageColor;
        result.push(new DamageTaken(attackerId, rgb, damage, age, x, y));
        // 移除多余对象
        if (result.size() > 5) {
            result.pollLast();
        }
    }

    public static Deque<DamageTaken> get(int id) {
        return LAST_DAMAGE_TAKEN_MAP.getOrDefault(id, EMPTY);
    }

    public static void remove(int id) {
        ForkJoinPool.commonPool().execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LAST_DAMAGE_TAKEN_MAP.remove(id);
        });
    }

    public static boolean isAttacker(int id, int attacker, long now) {
        return LAST_DAMAGE_TAKEN_MAP.getOrDefault(id, new ConcurrentLinkedDeque<>())
                .stream().anyMatch(d -> attacker == d.attackerId && now - d.time <= 100);
    }

    public static int getShowY(float height, boolean isBaby) {
        return XHP.XOption.height + (int) ((height + 0.5) / getScale(isBaby));
    }

    public static float getScale(boolean isBaby) {
        return XHP.XOption.scale * (isBaby ? 1 : 2);
    }

    public record DamageTaken(int attackerId, int rgb, float damage, long time, int x, int y) {

    }
}
