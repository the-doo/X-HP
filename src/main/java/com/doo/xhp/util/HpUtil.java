package com.doo.xhp.util;

import com.doo.xhp.XHP;
import com.doo.xhp.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public abstract class HpUtil {

    public static final DecimalFormat FORMATTER = new DecimalFormat("#.#");

    public record DamageTaken(int attackerId, int rgb, float damage, long time, int x, int y) {

    }

    private static final Map<Integer, Deque<DamageTaken>> LAST_DAMAGE_TAKEN_MAP = new HashMap<>();

    private static final ConcurrentLinkedDeque<DamageTaken> EMPTY = new ConcurrentLinkedDeque<>();

    private static final Random random = new Random();

    public static final int BASE_HEIGHT = 15;
    public static final int HEALTH = 10;

    public static void set(int id, float width, float height, int attacker, boolean isCritic, float damage, long age) {
        // 设置显示位置
        int x = random.nextInt(BASE_HEIGHT) + (int) (width * BASE_HEIGHT);
        int y = getShowY(height, false);
        x = random.nextBoolean() ? -x : x;
        y = random.nextInt(y / 2) + y / 4;

        // 添加队列
        if (!LAST_DAMAGE_TAKEN_MAP.containsKey(id)) {
            LAST_DAMAGE_TAKEN_MAP.put(id, new ConcurrentLinkedDeque<>());
        }
        // 添加当前伤害对象
        Deque<DamageTaken> result = LAST_DAMAGE_TAKEN_MAP.get(id);
        int rgb = isCritic ? XHP.XOption.criticDamageColor : XHP.XOption.damageColor;
        result.push(new DamageTaken(attacker, rgb, damage, age, x, y));
        // 移除多余对象
        if (result.size() > 5) {
            result.pollLast();
        }
    }

    public static Deque<DamageTaken> get(int id) {
        return LAST_DAMAGE_TAKEN_MAP.getOrDefault(id, EMPTY);
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

    /**
     * 定时任务循环运行去除map过期数据
     */
    private static final Timer JOB = new Timer("Clear Damage Data", true);

    /*
      10秒一次
     */
    static {
        JOB.schedule(new TimerTask() {
            @Override
            public void run() {
                int count = HpUtil.removeNotExists();
                Config.LOGGER.log(Level.DEBUG, "clear data end --- remove count: {}", count);
            }
        }, 0, 10000);
    }

    /**
     * 移除所有不存在对象（过期数据）
     */
    private static int removeNotExists() {
        if (LAST_DAMAGE_TAKEN_MAP.isEmpty()) {
            return 0;
        }
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return 0;
        }
        // 过滤不存在的实体
        Set<Integer> removeIds = LAST_DAMAGE_TAKEN_MAP.keySet().stream()
                .filter(id -> world.getEntityById(id) == null).collect(Collectors.toSet());
        removeIds.forEach(LAST_DAMAGE_TAKEN_MAP::remove);
        return removeIds.size();
    }

    /**
     * 是否是暴击
     * <p>
     * 判断是否是跳劈 || 伤害 > 基础伤害
     *
     * @return true false
     * @see PlayerEntity#attack(net.minecraft.entity.Entity)
     */
    public static boolean isCritic(Entity attacker, float damage) {
        if (!(attacker instanceof ServerPlayerEntity player)) {
            return false;
        }
        if (player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) < damage) {
            return true;
        }
        // 原版暴击判定，参考：PlayerEntity#attack(net.minecraft.entity.Entity)
        return player.getAttackCooldownProgress(0.5f) > 0.9 &&
                player.fallDistance > 0.0f && !player.isOnGround() &&
                !player.isClimbing() && !player.isTouchingWater() &&
                !player.hasStatusEffect(StatusEffects.BLINDNESS) && !player.hasVehicle();
    }


    public static LivingEntity focusTarget(Entity entity) {
        // see net.minecraft.client.render.GameRenderer.updateTargetedEntity
        Vec3d v1 = entity.getCameraPosVec(0);
        Vec3d v2 = entity.getRotationVec(0);
        Vec3d v3 = v1.add(v2.multiply(XHP.XOption.distance));

        Box box = entity.getBoundingBox().stretch(v2.multiply(XHP.XOption.distance)).expand(1);

        EntityHitResult result = ProjectileUtil.raycast(entity, v1, v3, box, t -> true, XHP.XOption.distance * XHP.XOption.distance);

        return HpUtil.focusResult(entity, result);
    }


    /**
     * log Player
     */
    private static Entity looker = null;

    /**
     * log time
     */
    private static long focusTime = -1;

    /**
     * log target
     */
    private static LivingEntity focusTarget = null;

    /**
     * Check focus result
     */
    public static LivingEntity focusResult(Entity entity, EntityHitResult result) {
        // entity maybe change
        if (entity != looker) {
            looker = entity;
            focusTime = -1;
            focusTarget = null;
        }

        LivingEntity target = null;
        // default prev target in 2s
        if (entity.age - focusTime <= 20 * XHP.XOption.focusDelay) {
            target = focusTarget;
        } else {
            focusTarget = null;
            focusTime = -1;
        }

        // if hit result now
        if (result != null && result.getEntity() instanceof LivingEntity) {
            target = (LivingEntity) result.getEntity();
            focusTarget = target;
            focusTime = entity.age;
        }

        return target;
    }
}
