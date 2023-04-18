package net.minecraft.world.gen.blockpredicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;

public class HasSturdyFacePredicate implements BlockPredicate {
   private final Vec3i offset;
   private final Direction face;
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Vec3i.createOffsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter((predicate) -> {
         return predicate.offset;
      }), Direction.CODEC.fieldOf("direction").forGetter((predicate) -> {
         return predicate.face;
      })).apply(instance, HasSturdyFacePredicate::new);
   });

   public HasSturdyFacePredicate(Vec3i offset, Direction face) {
      this.offset = offset;
      this.face = face;
   }

   public boolean test(StructureWorldAccess arg, BlockPos arg2) {
      BlockPos lv = arg2.add(this.offset);
      return arg.getBlockState(lv).isSideSolidFullSquare(arg, lv, this.face);
   }

   public BlockPredicateType getType() {
      return BlockPredicateType.HAS_STURDY_FACE;
   }

   // $FF: synthetic method
   public boolean test(Object world, Object pos) {
      return this.test((StructureWorldAccess)world, (BlockPos)pos);
   }
}
