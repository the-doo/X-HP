package com.doo.xhp.render;

import com.doo.xhp.enums.HealthTextGetters;
import com.doo.xhp.enums.MenuOptType;
import com.doo.xhp.interfaces.WithOption;
import com.doo.xhp.screen.MenuScreen;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.time.Duration;
import java.util.Random;

import static com.doo.xhp.render.HealRender.FONT_LIGHT;

public class DamageRender implements WithOption {

    public static final String ENABLED_KEY = "enabled";
    public static final String SCALE_KEY = "scale";
    public static final String DAMAGE_COLOR_KEY = "hit_color";
    public static final String HEAL_COLOR_KEY = "heal_color";
    public static final String CRIT_COLOR_KEY = "crit_color";

    private static final JsonObject options = new JsonObject();

    private static Cache<String, MutableDamage> CACHED = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(1200))
            .build();

    private static final int KILLED = 0xFFFFFF;

    private static int crit = 0xCC33FF;

    private static int hit = 0xFF0000;
    private static int heal = 0x00FF00;

    private static boolean enabled = true;

    private static float size = 0.8F;

    public DamageRender() {
        options.addProperty(ENABLED_KEY, enabled);
        options.addProperty(SCALE_KEY, size * 10);
        options.addProperty(DAMAGE_COLOR_KEY, hit);
        options.addProperty(HEAL_COLOR_KEY, heal);
        options.addProperty(CRIT_COLOR_KEY, crit);
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
    }

    @Override
    public void reloadOpt() {
        enabled = WithOption.boolV(options, ENABLED_KEY);
        size = (float) (WithOption.doubleV(options, SCALE_KEY) / 10);

        hit = (int) WithOption.doubleV(options, DAMAGE_COLOR_KEY);
        heal = (int) WithOption.doubleV(options, HEAL_COLOR_KEY);
        crit = (int) WithOption.doubleV(options, CRIT_COLOR_KEY);
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    public static void put(LivingEntity entity, float damage) {
        CACHED.put(entity.getId() + ":" + entity.tickCount, MutableDamage.random(entity, damage));
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
        Quaternion rotation = camera.rotation();
        CACHED.asMap().values().forEach(damage -> {
            stack.pushPose();
            transDamage(damage, stack, x, y, z, rotation);
            draw(damage, font, source, stack.last().pose());
            stack.popPose();
        });
    }

    private void transDamage(MutableDamage damage, PoseStack stack, double x, double y, double z, Quaternion rotation) {
        stack.translate(damage.x - x, damage.y - y, damage.z - z);
        stack.mulPose(rotation);
        stack.scale(size, size, size);
        stack.scale(-0.025f, -0.025f, 0.025f);
        stack.translate(0.0f, -1.501f, 0.0f);
    }

    public void draw(MutableDamage damage, Font font, MultiBufferSource.BufferSource source, Matrix4f pose) {
        font.drawInBatch(damage.damage, 0, 0, damage.color, false,
                pose, source, true, 0, FONT_LIGHT);
    }

    public static class MutableDamage {

        String damage;

        boolean isHeal;

        double x;

        double y;

        double z;

        int color = hit;

        int life = 22;

        public static MutableDamage random(LivingEntity entity, float damage) {
            Random random = entity.getRandom();
            MutableDamage d = new MutableDamage();
            d.x = entity.getX();
            d.y = entity.getY() + random.nextDouble() + (entity.isBaby() ? entity.getBbHeight() : entity.getEyeHeight()) / 2;
            d.z = entity.getZ();
            d.isHeal = damage > 0;
            d.damage = HealthTextGetters.formatNum(damage < 0 ? -damage : damage);

            d.color(entity.getMaxHealth(), damage);
            return d;
        }

        private void color(float max, float damage) {
            float process;
            if (isHeal) {
                color = heal;
            } else if ((process = -damage / max) >= 1) {
                color = KILLED;
            } else if (process >= 0.6) {
                color = crit;
            }
        }

        public void tick() {
            life--;

            if (life > 15) {
                y += 0.06;
            } else if (!isHeal && life < 10) {
                y -= 0.03;
            }
        }
    }
}
