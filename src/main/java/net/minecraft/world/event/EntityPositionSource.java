package net.minecraft.world.event;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;

public class EntityPositionSource implements PositionSource {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Uuids.INT_STREAM_CODEC.fieldOf("source_entity").forGetter(EntityPositionSource::getUuid), Codec.FLOAT.fieldOf("y_offset").orElse(0.0F).forGetter((entityPositionSource) -> {
         return entityPositionSource.yOffset;
      })).apply(instance, (uuid, yOffset) -> {
         return new EntityPositionSource(Either.right(Either.left(uuid)), yOffset);
      });
   });
   private Either source;
   final float yOffset;

   public EntityPositionSource(Entity entity, float yOffset) {
      this(Either.left(entity), yOffset);
   }

   EntityPositionSource(Either source, float yOffset) {
      this.source = source;
      this.yOffset = yOffset;
   }

   public Optional getPos(World world) {
      if (this.source.left().isEmpty()) {
         this.findEntityInWorld(world);
      }

      return this.source.left().map((entity) -> {
         return entity.getPos().add(0.0, (double)this.yOffset, 0.0);
      });
   }

   private void findEntityInWorld(World world) {
      ((Optional)this.source.map(Optional::of, (entityId) -> {
         Function var10001 = (uuid) -> {
            Entity var10000;
            if (world instanceof ServerWorld lv) {
               var10000 = lv.getEntity(uuid);
            } else {
               var10000 = null;
            }

            return var10000;
         };
         Objects.requireNonNull(world);
         return Optional.ofNullable((Entity)entityId.map(var10001, world::getEntityById));
      })).ifPresent((entity) -> {
         this.source = Either.left(entity);
      });
   }

   private UUID getUuid() {
      return (UUID)this.source.map(Entity::getUuid, (entityId) -> {
         return (UUID)entityId.map(Function.identity(), (entityIdx) -> {
            throw new RuntimeException("Unable to get entityId from uuid");
         });
      });
   }

   int getEntityId() {
      return (Integer)this.source.map(Entity::getId, (entityId) -> {
         return (Integer)entityId.map((uuid) -> {
            throw new IllegalStateException("Unable to get entityId from uuid");
         }, Function.identity());
      });
   }

   public PositionSourceType getType() {
      return PositionSourceType.ENTITY;
   }

   public static class Type implements PositionSourceType {
      public EntityPositionSource readFromBuf(PacketByteBuf arg) {
         return new EntityPositionSource(Either.right(Either.right(arg.readVarInt())), arg.readFloat());
      }

      public void writeToBuf(PacketByteBuf arg, EntityPositionSource arg2) {
         arg.writeVarInt(arg2.getEntityId());
         arg.writeFloat(arg2.yOffset);
      }

      public Codec getCodec() {
         return EntityPositionSource.CODEC;
      }

      // $FF: synthetic method
      public PositionSource readFromBuf(PacketByteBuf buf) {
         return this.readFromBuf(buf);
      }
   }
}
