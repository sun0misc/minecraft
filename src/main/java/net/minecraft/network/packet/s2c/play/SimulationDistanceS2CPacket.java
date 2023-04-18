package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record SimulationDistanceS2CPacket(int simulationDistance) implements Packet {
   public SimulationDistanceS2CPacket(PacketByteBuf buf) {
      this(buf.readVarInt());
   }

   public SimulationDistanceS2CPacket(int i) {
      this.simulationDistance = i;
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.simulationDistance);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onSimulationDistance(this);
   }

   public int simulationDistance() {
      return this.simulationDistance;
   }
}
