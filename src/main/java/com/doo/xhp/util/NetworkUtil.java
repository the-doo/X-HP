package com.doo.xhp.util;

import com.doo.xhp.XHP;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.function.Consumer;

public abstract class NetworkUtil {

    /**
     * 获取数据发送器
     */
    public static Consumer<Collection<ServerPlayerEntity>> packetSender(int id, float damage, DamageSource source) {
        return players -> {
            if (players == null || players.isEmpty()) {
                return;
            }

            Entity attacker = source.getAttacker();

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(attacker == null ? 0 : attacker.getId());
            buf.writeInt(id);
            buf.writeFloat(damage);

            // is critic
            boolean isCritic;
            // if is persistentProjectileEntity
            Entity entity = source.getSource();
            if (entity instanceof PersistentProjectileEntity) {
                isCritic = ((PersistentProjectileEntity) entity).isCritical();
            } else {
                isCritic = HpUtil.isCritic(attacker, damage);
            }

            buf.writeBoolean(isCritic);

            players.forEach(p -> ServerPlayNetworking.send(p, XHP.ON_DAMAGE_PACKET, buf));
        };
    }

    /**
     * 数据包处理器
     */
    public static void registerPacketAcceptor() {
        ClientPlayNetworking.registerGlobalReceiver(XHP.ON_DAMAGE_PACKET, (client, handler, buf, responseSender) -> {
            // if offline
            ClientWorld world = MinecraftClient.getInstance().world;
            if (world == null) {
                return;
            }

            // 读取数据
            int attacker = buf.readInt();
            int id = buf.readInt();
            float damage = buf.readFloat();
            boolean isCritic = buf.readBoolean();

            // 获取当前id的对象
            Entity entity = world.getEntityById(id);
            if (entity == null) {
                return;
            }
            client.execute(() -> HpUtil.set(id, entity.getWidth(), entity.getHeight(), attacker, isCritic, damage, world.getTime()));
        });
    }
}
