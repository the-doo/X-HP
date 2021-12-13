package com.doo.xhp.util;

import com.doo.xhp.XHP;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class NetworkUtil {

    /**
     * 获取数据发送器
     */
    public static Consumer<Collection<ServerPlayerEntity>> packetSender(int id, float damage, Entity attacker) {
        return players -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(id);
            buf.writeFloat(damage);
            buf.writeBoolean(HpUtil.isCritic(attacker, damage));
            buf.writeLong(NumberUtils.LONG_ZERO);
            Optional.ofNullable(attacker).ifPresent(e -> buf.writeLong(e.getId()));

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
            int id = buf.readInt();
            float damage = buf.readFloat();
            boolean isCritic = buf.readBoolean();
            int attacker = (int) buf.readLong();

            // 获取当前id的对象
            Entity entity = world.getEntityById(id);
            if (entity == null) {
                return;
            }
            client.execute(() -> HpUtil.set(id, entity.getWidth(), entity.getHeight(), attacker, isCritic, damage, world.getTime()));
        });
    }
}
