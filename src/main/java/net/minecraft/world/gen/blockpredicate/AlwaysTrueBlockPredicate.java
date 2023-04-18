package net.minecraft.world.gen.blockpredicate;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

class AlwaysTrueBlockPredicate implements BlockPredicate {
   public static AlwaysTrueBlockPredicate instance = new AlwaysTrueBlockPredicate();
   public static final Codec CODEC = Codec.unit(() -> {
      return instance;
   });

   private AlwaysTrueBlockPredicate() {
   }

   public boolean test(StructureWorldAccess arg, BlockPos arg2) {
      return true;
   }

   public BlockPredicateType getType() {
      return BlockPredicateType.TRUE;
   }

   // $FF: synthetic method
   public boolean test(Object world, Object pos) {
      return this.test((StructureWorldAccess)world, (BlockPos)pos);
   }
}
