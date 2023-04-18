package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record DamageTiltS2CPacket(int id, float yaw) implements Packet {
   public DamageTiltS2CPacket(LivingEntity entity) {
      this(entity.getId(), entity.getDamageTiltYaw());
   }

   public DamageTiltS2CPacket(PacketByteBuf buf) {
      this(buf.readVarInt(), buf.readFloat());
   }

   public DamageTiltS2CPacket(int i, float f) {
      this.id = i;
      this.yaw = f;
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.id);
      buf.writeFloat(this.yaw);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onDamageTilt(this);
   }

   public int id() {
      return this.id;
   }

   public float yaw() {
      return this.yaw;
   }
}
