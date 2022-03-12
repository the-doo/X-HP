package com.doo.xhp.util;

import com.doo.xhp.XHP;
import com.doo.xhp.config.XOption;
import com.doo.xhp.interfaces.Critable;
import com.google.common.collect.Multimap;
import dev.ftb.mods.ftbteams.data.ClientTeam;
import dev.ftb.mods.ftbteams.data.ClientTeamManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.text.DecimalFormat;

public abstract class HpUtil {

    public static final DecimalFormat FORMATTER = new DecimalFormat("#.#");

    public record DamageR(float damage, long time, boolean isCrit, float x, float y) {

    }

    public static boolean isStaring(LivingEntity target, Entity entity) {
        // see net.minecraft.entity.mob.EndermanEntity.isPlayerStaring
        Vec3d vec3d = entity.getRotationVec(1.0f).normalize();
        Vec3d vec3d2 = new Vec3d(target.getX() - entity.getX(), target.getEyeY() - entity.getEyeY(), target.getZ() - entity.getZ());
        double d = vec3d2.length();
        double e = vec3d.dotProduct(vec3d2.normalize());
        if (e > 1 - 0.03 / d) {
            focusResult(entity, target);
            return true;
        }
        return false;
    }

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
    public static LivingEntity focusResult(Entity entity, LivingEntity target) {
        // default prev target in 2s
        if (target == null && entity.age - focusTime <= 20 * XHP.XOption.focusDelay) {
            target = focusTarget;
        } else {
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

    public static float stackDamage(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        if (modifiers.isEmpty()) {
            return 0;
        }
        MutableFloat value = new MutableFloat();
        modifiers.forEach((a, m) -> {
            if (a == EntityAttributes.GENERIC_ATTACK_DAMAGE) {
                if (m.getOperation() == EntityAttributeModifier.Operation.ADDITION) {
                    value.add(m.getValue());
                }
            }
        });
        return value.floatValue();
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
        return !(distance > XHP.XOption.distance || distance < 0.8);
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
            return isStaring(entity, camera) || focusResult(camera, null) == entity;
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
    public static int getColor(LivingEntity e, Entity camera) {
        if (e.isTeammate(camera)) {
            return XHP.XOption.friendColor;
        }
        // is attacker or hostileEntity
        if (e instanceof HostileEntity || e.getAttacker() == camera && e.age - e.getLastAttackedTime() < 100) {
            return XHP.XOption.mobColor;
        }
        if (e instanceof Angerable) {
            return ((Angerable) e).getAngerTime() < 1 ? XHP.XOption.friendColor : XHP.XOption.mobColor;
        }

        // support FTB Team
        if (XHP.hasFTBTeam && XHP.XOption.enableFTBTeam && camera instanceof PlayerEntity && ClientTeamManager.INSTANCE.selfTeam != null) {
            ClientTeam team;
            if (e instanceof PlayerEntity && (team = ClientTeamManager.INSTANCE.getTeam(e.getUuid())) != null) {
                return team.getColor();
            }
            if (e instanceof Tameable && (team = ClientTeamManager.INSTANCE.getTeam(((Tameable) e).getOwnerUuid())) != null) {
                return team.getColor();
            }
        }

        // monster or angerable
        return !(e instanceof Monster) ? XHP.XOption.friendColor : XHP.XOption.mobColor;
    }
}
