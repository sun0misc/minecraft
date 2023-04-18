package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PosRuleTestType {
   PosRuleTestType ALWAYS_TRUE = register("always_true", AlwaysTruePosRuleTest.CODEC);
   PosRuleTestType LINEAR_POS = register("linear_pos", LinearPosRuleTest.CODEC);
   PosRuleTestType AXIS_ALIGNED_LINEAR_POS = register("axis_aligned_linear_pos", AxisAlignedLinearPosRuleTest.CODEC);

   Codec codec();

   static PosRuleTestType register(String id, Codec codec) {
      return (PosRuleTestType)Registry.register(Registries.POS_RULE_TEST, (String)id, () -> {
         return codec;
      });
   }
}
