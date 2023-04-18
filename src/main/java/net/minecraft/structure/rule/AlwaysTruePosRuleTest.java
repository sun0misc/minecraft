package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class AlwaysTruePosRuleTest extends PosRuleTest {
   public static final Codec CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final AlwaysTruePosRuleTest INSTANCE = new AlwaysTruePosRuleTest();

   private AlwaysTruePosRuleTest() {
   }

   public boolean test(BlockPos originalPos, BlockPos currentPos, BlockPos pivot, Random random) {
      return true;
   }

   protected PosRuleTestType getType() {
      return PosRuleTestType.ALWAYS_TRUE;
   }
}
