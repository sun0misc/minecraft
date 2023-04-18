package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class EntityEquipmentUpdateS2CPacket implements Packet {
   private static final byte field_33342 = -128;
   private final int id;
   private final List equipmentList;

   public EntityEquipmentUpdateS2CPacket(int id, List equipmentList) {
      this.id = id;
      this.equipmentList = equipmentList;
   }

   public EntityEquipmentUpdateS2CPacket(PacketByteBuf buf) {
      this.id = buf.readVarInt();
      EquipmentSlot[] lvs = EquipmentSlot.values();
      this.equipmentList = Lists.newArrayList();

      byte i;
      do {
         i = buf.readByte();
         EquipmentSlot lv = lvs[i & 127];
         ItemStack lv2 = buf.readItemStack();
         this.equipmentList.add(Pair.of(lv, lv2));
      } while((i & -128) != 0);

   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.id);
      int i = this.equipmentList.size();

      for(int j = 0; j < i; ++j) {
         Pair pair = (Pair)this.equipmentList.get(j);
         EquipmentSlot lv = (EquipmentSlot)pair.getFirst();
         boolean bl = j != i - 1;
         int k = lv.ordinal();
         buf.writeByte(bl ? k | -128 : k);
         buf.writeItemStack((ItemStack)pair.getSecond());
      }

   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onEntityEquipmentUpdate(this);
   }

   public int getId() {
      return this.id;
   }

   public List getEquipmentList() {
      return this.equipmentList;
   }
}
