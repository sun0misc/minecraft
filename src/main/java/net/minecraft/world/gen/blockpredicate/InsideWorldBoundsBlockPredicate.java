package net.minecraft.world.gen.blockpredicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;

public class InsideWorldBoundsBlockPredicate implements BlockPredicate {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Vec3i.createOffsetCodec(16).optionalFieldOf("offset", BlockPos.ORIGIN).forGetter((predicate) -> {
         return predicate.offset;
      })).apply(instance, InsideWorldBoundsBlockPredicate::new);
   });
   private final Vec3i offset;

   public InsideWorldBoundsBlockPredicate(Vec3i offset) {
      this.offset = offset;
   }

   public boolean test(StructureWorldAccess arg, BlockPos arg2) {
      return !arg.isOutOfHeightLimit(arg2.add(this.offset));
   }

   public BlockPredicateType getType() {
      return BlockPredicateType.INSIDE_WORLD_BOUNDS;
   }

   // $FF: synthetic method
   public boolean test(Object world, Object pos) {
      return this.test((StructureWorldAccess)world, (BlockPos)pos);
   }
}
