package net.minecraft.network.packet.s2c.play;

import java.util.Map;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagPacketSerializer;

public class SynchronizeTagsS2CPacket implements Packet {
   private final Map groups;

   public SynchronizeTagsS2CPacket(Map groups) {
      this.groups = groups;
   }

   public SynchronizeTagsS2CPacket(PacketByteBuf buf) {
      this.groups = buf.readMap((bufx) -> {
         return RegistryKey.ofRegistry(bufx.readIdentifier());
      }, TagPacketSerializer.Serialized::fromBuf);
   }

   public void write(PacketByteBuf buf) {
      buf.writeMap(this.groups, (bufx, registryKey) -> {
         bufx.writeIdentifier(registryKey.getValue());
      }, (bufx, serializedGroup) -> {
         serializedGroup.writeBuf(bufx);
      });
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onSynchronizeTags(this);
   }

   public Map getGroups() {
      return this.groups;
   }
}
