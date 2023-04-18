package net.minecraft.world.gen.blockpredicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3i;

public class SolidBlockPredicate extends OffsetPredicate {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return registerOffsetField(instance).apply(instance, SolidBlockPredicate::new);
   });

   public SolidBlockPredicate(Vec3i arg) {
      super(arg);
   }

   protected boolean test(BlockState state) {
      return state.getMaterial().isSolid();
   }

   public BlockPredicateType getType() {
      return BlockPredicateType.SOLID;
   }
}
