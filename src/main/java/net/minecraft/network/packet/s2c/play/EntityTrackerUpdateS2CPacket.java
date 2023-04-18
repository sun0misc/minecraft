package net.minecraft.network.packet.s2c.play;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record EntityTrackerUpdateS2CPacket(int id, List trackedValues) implements Packet {
   public static final int MARKER_ID = 255;

   public EntityTrackerUpdateS2CPacket(PacketByteBuf buf) {
      this(buf.readVarInt(), read(buf));
   }

   public EntityTrackerUpdateS2CPacket(int id, List list) {
      this.id = id;
      this.trackedValues = list;
   }

   private static void write(List trackedValues, PacketByteBuf buf) {
      Iterator var2 = trackedValues.iterator();

      while(var2.hasNext()) {
         DataTracker.SerializedEntry lv = (DataTracker.SerializedEntry)var2.next();
         lv.write(buf);
      }

      buf.writeByte(255);
   }

   private static List read(PacketByteBuf buf) {
      List list = new ArrayList();

      short i;
      while((i = buf.readUnsignedByte()) != 255) {
         list.add(DataTracker.SerializedEntry.fromBuf(buf, i));
      }

      return list;
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.id);
      write(this.trackedValues, buf);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onEntityTrackerUpdate(this);
   }

   public int id() {
      return this.id;
   }

   public List trackedValues() {
      return this.trackedValues;
   }
}
