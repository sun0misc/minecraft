package net.minecraft.server.world;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

public interface EntityTrackingListener {
   ServerPlayerEntity getPlayer();

   void sendPacket(Packet packet);
}
