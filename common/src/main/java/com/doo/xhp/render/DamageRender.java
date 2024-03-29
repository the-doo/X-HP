package com.doo.xhp.render;

import com.doo.xhp.enums.HealthTextGetters;
import com.doo.xhp.enums.MenuOptType;
import com.doo.xhp.interfaces.WithOption;
import com.doo.xhp.screen.MenuScreen;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.time.Duration;
import java.util.concurrent.ConcurrentMap;

import static com.doo.xhp.render.HealRender.FONT_LIGHT;

public class DamageRender implements WithOption {

    public static final String ENABLED_KEY = "enabled";
    public static final String SHADOW_KEY = "shadow";

    public static final String FROM_HEAD_KEY = "from_head";
    public static final String SCALE_KEY = "scale";
    public static final String UP_SPEED_KEY = "up_speed";
    public static final String DOWN_SPEED_KEY = "down_speed";
    public static final String DAMAGE_COLOR_KEY = "hit_color";
    public static final String HEAL_COLOR_KEY = "heal_color";
    public static final String CRIT_COLOR_KEY = "crit_color";
    public static final String KILLED_COLOR_KEY = "killed_color";

    private static final JsonObject options = new JsonObject();

    private static final Cache<String, MutableDamage> CACHED = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(1500))
            .build();

    private static int killed = 0xFFFFFF;

    private static int crit = 0xCC33FF;

    private static int hit = 0xFF0000;
    private static int heal = 0x00FF00;

    private static boolean enabled = true;
    private static boolean shadow = true;
    private static boolean fromHead = false;

    private static float size = 1.2F;

    private static float upSpeed = 0.08F;
    private static int upTick = 20;
    private static float downSpeed = 0.03F;
    private static int downTick = 8;


    public DamageRender() {
        options.addProperty(ENABLED_KEY, enabled);
        options.addProperty(SHADOW_KEY, shadow);
        options.addProperty(FROM_HEAD_KEY, fromHead);
        options.addProperty(SCALE_KEY, size * 10);
        options.addProperty(UP_SPEED_KEY, upSpeed * 100);
        options.addProperty(DOWN_SPEED_KEY, downSpeed * 100);
        options.addProperty(DAMAGE_COLOR_KEY, hit);
        options.addProperty(HEAL_COLOR_KEY, heal);
        options.addProperty(CRIT_COLOR_KEY, crit);
        options.addProperty(KILLED_COLOR_KEY, killed);

        size *= 0.025F;
    }

    @Override
    public JsonObject opt() {
        return options;
    }

    @Override
    public void registerOpt() {
        String name = "damage";
        MenuScreen.register(MenuOptType.COLOR, name, DAMAGE_COLOR_KEY, 1);
        MenuScreen.register(MenuOptType.COLOR, name, HEAL_COLOR_KEY, 1);
        MenuScreen.register(MenuOptType.COLOR, name, CRIT_COLOR_KEY, 1);
        MenuScreen.register(MenuOptType.COLOR, name, KILLED_COLOR_KEY, 1);
    }

    @Override
    public void reloadOpt() {
        enabled = WithOption.boolV(options, ENABLED_KEY);
        shadow = WithOption.boolV(options, SHADOW_KEY);
        fromHead = WithOption.boolV(options, FROM_HEAD_KEY);
        size = (float) (WithOption.doubleV(options, SCALE_KEY) / 10 * 0.025);
        upSpeed = (float) (WithOption.doubleV(options, UP_SPEED_KEY) / 100);
        downSpeed = (float) (WithOption.doubleV(options, DOWN_SPEED_KEY) / 100);

        hit = (int) WithOption.doubleV(options, DAMAGE_COLOR_KEY);
        heal = (int) WithOption.doubleV(options, HEAL_COLOR_KEY);
        crit = (int) WithOption.doubleV(options, CRIT_COLOR_KEY);
        killed = (int) WithOption.doubleV(options, KILLED_COLOR_KEY);
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    public static double sum(int id) {
        ConcurrentMap<String, MutableDamage> map = CACHED.asMap();
        if (map.isEmpty()) {
            return 0;
        }

        String idKey = id + ":";
        return map.entrySet().stream()
                .filter(e -> e.getKey().startsWith(idKey) && e.getValue().life > 15)
                .mapToDouble(e -> e.getValue().damage).sum();
    }

    public static void put(LivingEntity entity, float damage) {
        if (entity.getMaxHealth() >= entity.getHealth()) {
            CACHED.put(entity.getId() + ":" + entity.tickCount, MutableDamage.random(entity, damage));
        }
    }

    public static void tick() {
        if (enabled) {
            CACHED.asMap().values().forEach(MutableDamage::tick);
        }
    }

    public void render(PoseStack stack, Font font, MultiBufferSource.BufferSource source, Camera camera) {
        Vec3 vec3 = camera.getPosition();
        double x = vec3.x();
        double y = vec3.y();
        double z = vec3.z();
        Quaternionf rotation = camera.rotation();
        CACHED.asMap().values().forEach(damage -> {
            stack.pushPose();
            transDamage(damage, stack, x, y, z, rotation);
            draw(damage, font, source, stack.last().pose(), damage.isStop());
            stack.popPose();
        });
    }

    private void transDamage(MutableDamage damage, PoseStack stack, double x, double y, double z, Quaternionf rotation) {
        stack.translate(damage.x - x, damage.y - y, damage.z - z);
        stack.mulPose(rotation);
        stack.scale(-size, -size, size);
    }

    public void draw(MutableDamage damage, Font font, MultiBufferSource.BufferSource source, Matrix4f pose, boolean shadows) {
        font.drawInBatch(damage.damageStr, 0, 0, damage.color, false,
                pose, source, Font.DisplayMode.SEE_THROUGH, 0, FONT_LIGHT);
        font.drawInBatch(damage.damageStr, 0, 0, damage.color, shadow && shadows,
                pose, source, Font.DisplayMode.NORMAL, 0, FONT_LIGHT);
    }

    public static class MutableDamage {

        double damage;

        String damageStr;

        boolean isHeal;

        double x;

        double y;

        double z;

        int color = hit;

        int life = 25;

        public static MutableDamage random(LivingEntity entity, float damage) {
            RandomSource random = entity.getRandom();
            MutableDamage d = new MutableDamage();
            float bodyY = entity.isBaby() ? entity.getBbHeight() : entity.getEyeHeight();
            if (!fromHead) {
                bodyY /= 2;
            }

            d.x = entity.getX();
            d.y = entity.getY() + random.nextDouble() + bodyY;
            d.z = entity.getZ();
            d.isHeal = damage > 0;
            d.damage = damage;
            d.damageStr = HealthTextGetters.formatNum(damage < 0 ? -damage : damage);

            d.color(entity.getMaxHealth(), damage);
            return d;
        }

        private void color(float max, float damage) {
            float process;
            if (isHeal) {
                color = heal;
            } else if ((process = -damage / max) >= 1) {
                color = killed;
            } else if (process >= 0.6) {
                color = crit;
            }
        }

        public void tick() {
            life--;

            if (life >= upTick) {
                y += upSpeed;
            } else if (downSpeed > 0 && life <= downTick) {
                y -= downSpeed;
            }
        }

        public boolean isStop() {
            return life > downTick + 1 && life < upTick - 1;
        }
    }
}
