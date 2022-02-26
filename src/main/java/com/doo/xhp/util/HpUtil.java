package com.doo.xhp.util;

import com.doo.xhp.XHP;
import com.doo.xhp.config.XOption;
import com.doo.xhp.interfaces.Critable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;

public abstract class HpUtil {

    public static final DecimalFormat FORMATTER = new DecimalFormat("#.#");

    public record DamageR(float damage, long time, boolean isCrit, float x, float y) {

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

    public static boolean isCrit(DamageSource source) {
        // is critic
        boolean isCritic = false;

        if (source.getSource() instanceof PersistentProjectileEntity) {
            isCritic = ((PersistentProjectileEntity) source.getSource()).isCritical();
        } else if (source.getAttacker() instanceof Critable) {
            isCritic = ((Critable) source.getAttacker()).isCrit();
        }

        return isCritic;
    }

    public static boolean mustCheck(LivingEntity entity) {
        if (!XHP.XOption.enabled) {
            return false;
        }

        // option to if hidden hud
        if (XHP.XOption.syncWithHud && MinecraftClient.getInstance().options.hudHidden) {
            return false;
        }

        // todo filter list
        if (entity instanceof ArmorStandEntity && XHP.XOption.ignoreArmorStandEntity) {
            return false;
        }

        Entity camera = MinecraftClient.getInstance().cameraEntity;
        if (camera == null) {
            return false;
        }

        double distance = camera.getPos().distanceTo(entity.getPos());
        return !(distance > XHP.XOption.distance || distance < entity.getHeight() * 0.6);
    }

    public static boolean canRender(LivingEntity entity) {
        // option to if entity hidden
        if (XHP.XOption.syncWithHide && entity.isInvisible()) {
            return false;
        }

        Entity camera = MinecraftClient.getInstance().cameraEntity;
        if (camera == null) {
            return false;
        }

        // if FOCUS
        if (XHP.XOption.display == XOption.Display.FOCUS) {
            return HpUtil.focusTarget(camera) == entity;
        }

        return true;
    }

    /**
     * 如果不是敌对实体或可生气实体，且目标人物不是玩家，且玩家没有伤害过
     *
     * @param e      渲染实体
     * @param camera 当前摄像机对象
     * @return 是否
     */
    public static boolean isFriend(LivingEntity e, Entity camera) {
        if (e.isTeammate(camera)) {
            return true;
        }
        // is attacker
        if (e.getAttacker() == camera && e.age - e.getLastAttackedTime() < 800) {
            return false;
        }
        // monster or angerable
        return !(e instanceof Monster || e instanceof Angerable);
    }
}
