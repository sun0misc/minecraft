package net.minecraft.world.gen.blockpredicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

class NotBlockPredicate implements BlockPredicate {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockPredicate.BASE_CODEC.fieldOf("predicate").forGetter((predicate) -> {
         return predicate.predicate;
      })).apply(instance, NotBlockPredicate::new);
   });
   private final BlockPredicate predicate;

   public NotBlockPredicate(BlockPredicate predicate) {
      this.predicate = predicate;
   }

   public boolean test(StructureWorldAccess arg, BlockPos arg2) {
      return !this.predicate.test(arg, arg2);
   }

   public BlockPredicateType getType() {
      return BlockPredicateType.NOT;
   }

   // $FF: synthetic method
   public boolean test(Object world, Object pos) {
      return this.test((StructureWorldAccess)world, (BlockPos)pos);
   }
}
