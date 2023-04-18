package net.minecraft.world.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockPositionSource implements PositionSource {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockPos.CODEC.fieldOf("pos").forGetter((blockPositionSource) -> {
         return blockPositionSource.pos;
      })).apply(instance, BlockPositionSource::new);
   });
   final BlockPos pos;

   public BlockPositionSource(BlockPos pos) {
      this.pos = pos;
   }

   public Optional getPos(World world) {
      return Optional.of(Vec3d.ofCenter(this.pos));
   }

   public PositionSourceType getType() {
      return PositionSourceType.BLOCK;
   }

   public static class Type implements PositionSourceType {
      public BlockPositionSource readFromBuf(PacketByteBuf arg) {
         return new BlockPositionSource(arg.readBlockPos());
      }

      public void writeToBuf(PacketByteBuf arg, BlockPositionSource arg2) {
         arg.writeBlockPos(arg2.pos);
      }

      public Codec getCodec() {
         return BlockPositionSource.CODEC;
      }

      // $FF: synthetic method
      public PositionSource readFromBuf(PacketByteBuf buf) {
         return this.readFromBuf(buf);
      }
   }
}
