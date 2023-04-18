package net.minecraft.entity.data;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;

public interface TrackedDataHandler {
   void write(PacketByteBuf buf, Object value);

   Object read(PacketByteBuf buf);

   default TrackedData create(int id) {
      return new TrackedData(id, this);
   }

   Object copy(Object value);

   static TrackedDataHandler of(final PacketByteBuf.PacketWriter writer, final PacketByteBuf.PacketReader reader) {
      return new ImmutableHandler() {
         public void write(PacketByteBuf buf, Object value) {
            writer.accept(buf, value);
         }

         public Object read(PacketByteBuf buf) {
            return reader.apply(buf);
         }
      };
   }

   static TrackedDataHandler ofOptional(PacketByteBuf.PacketWriter writer, PacketByteBuf.PacketReader reader) {
      return of(writer.asOptional(), reader.asOptional());
   }

   static TrackedDataHandler ofEnum(Class enum_) {
      return of(PacketByteBuf::writeEnumConstant, (buf) -> {
         return buf.readEnumConstant(enum_);
      });
   }

   static TrackedDataHandler of(IndexedIterable registry) {
      return of((buf, value) -> {
         buf.writeRegistryValue(registry, value);
      }, (buf) -> {
         return buf.readRegistryValue(registry);
      });
   }

   public interface ImmutableHandler extends TrackedDataHandler {
      default Object copy(Object value) {
         return value;
      }
   }
}
