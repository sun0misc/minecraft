package net.minecraft.world.event;

import com.mojang.serialization.Codec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface PositionSourceType {
   PositionSourceType BLOCK = register("block", new BlockPositionSource.Type());
   PositionSourceType ENTITY = register("entity", new EntityPositionSource.Type());

   PositionSource readFromBuf(PacketByteBuf buf);

   void writeToBuf(PacketByteBuf buf, PositionSource positionSource);

   Codec getCodec();

   static PositionSourceType register(String id, PositionSourceType positionSourceType) {
      return (PositionSourceType)Registry.register(Registries.POSITION_SOURCE_TYPE, (String)id, positionSourceType);
   }

   static PositionSource read(PacketByteBuf buf) {
      Identifier lv = buf.readIdentifier();
      return ((PositionSourceType)Registries.POSITION_SOURCE_TYPE.getOrEmpty(lv).orElseThrow(() -> {
         return new IllegalArgumentException("Unknown position source type " + lv);
      })).readFromBuf(buf);
   }

   static void write(PositionSource positionSource, PacketByteBuf buf) {
      buf.writeIdentifier(Registries.POSITION_SOURCE_TYPE.getId(positionSource.getType()));
      positionSource.getType().writeToBuf(buf, positionSource);
   }
}
