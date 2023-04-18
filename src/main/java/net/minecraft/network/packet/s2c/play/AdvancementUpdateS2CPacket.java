package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class AdvancementUpdateS2CPacket implements Packet {
   private final boolean clearCurrent;
   private final Map toEarn;
   private final Set toRemove;
   private final Map toSetProgress;

   public AdvancementUpdateS2CPacket(boolean clearCurrent, Collection toEarn, Set toRemove, Map toSetProgress) {
      this.clearCurrent = clearCurrent;
      ImmutableMap.Builder builder = ImmutableMap.builder();
      Iterator var6 = toEarn.iterator();

      while(var6.hasNext()) {
         Advancement lv = (Advancement)var6.next();
         builder.put(lv.getId(), lv.createTask());
      }

      this.toEarn = builder.build();
      this.toRemove = ImmutableSet.copyOf(toRemove);
      this.toSetProgress = ImmutableMap.copyOf(toSetProgress);
   }

   public AdvancementUpdateS2CPacket(PacketByteBuf buf) {
      this.clearCurrent = buf.readBoolean();
      this.toEarn = buf.readMap(PacketByteBuf::readIdentifier, Advancement.Builder::fromPacket);
      this.toRemove = (Set)buf.readCollection(Sets::newLinkedHashSetWithExpectedSize, PacketByteBuf::readIdentifier);
      this.toSetProgress = buf.readMap(PacketByteBuf::readIdentifier, AdvancementProgress::fromPacket);
   }

   public void write(PacketByteBuf buf) {
      buf.writeBoolean(this.clearCurrent);
      buf.writeMap(this.toEarn, PacketByteBuf::writeIdentifier, (buf2, task) -> {
         task.toPacket(buf2);
      });
      buf.writeCollection(this.toRemove, PacketByteBuf::writeIdentifier);
      buf.writeMap(this.toSetProgress, PacketByteBuf::writeIdentifier, (buf2, progress) -> {
         progress.toPacket(buf2);
      });
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onAdvancements(this);
   }

   public Map getAdvancementsToEarn() {
      return this.toEarn;
   }

   public Set getAdvancementIdsToRemove() {
      return this.toRemove;
   }

   public Map getAdvancementsToProgress() {
      return this.toSetProgress;
   }

   public boolean shouldClearCurrent() {
      return this.clearCurrent;
   }
}
