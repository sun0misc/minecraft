package net.minecraft.network.packet.s2c.play;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record FeaturesS2CPacket(Set features) implements Packet {
   public FeaturesS2CPacket(PacketByteBuf buf) {
      this((Set)buf.readCollection(HashSet::new, PacketByteBuf::readIdentifier));
   }

   public FeaturesS2CPacket(Set set) {
      this.features = set;
   }

   public void write(PacketByteBuf buf) {
      buf.writeCollection(this.features, PacketByteBuf::writeIdentifier);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onFeatures(this);
   }

   public Set features() {
      return this.features;
   }
}
