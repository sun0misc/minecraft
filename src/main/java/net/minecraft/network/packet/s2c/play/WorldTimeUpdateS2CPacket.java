package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class WorldTimeUpdateS2CPacket implements Packet {
   private final long time;
   private final long timeOfDay;

   public WorldTimeUpdateS2CPacket(long time, long timeOfDay, boolean doDaylightCycle) {
      this.time = time;
      long n = timeOfDay;
      if (!doDaylightCycle) {
         n = -timeOfDay;
         if (n == 0L) {
            n = -1L;
         }
      }

      this.timeOfDay = n;
   }

   public WorldTimeUpdateS2CPacket(PacketByteBuf buf) {
      this.time = buf.readLong();
      this.timeOfDay = buf.readLong();
   }

   public void write(PacketByteBuf buf) {
      buf.writeLong(this.time);
      buf.writeLong(this.timeOfDay);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onWorldTimeUpdate(this);
   }

   public long getTime() {
      return this.time;
   }

   public long getTimeOfDay() {
      return this.timeOfDay;
   }
}
