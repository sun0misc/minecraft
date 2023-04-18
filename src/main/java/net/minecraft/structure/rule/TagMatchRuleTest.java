package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.random.Random;

public class TagMatchRuleTest extends RuleTest {
   public static final Codec CODEC;
   private final TagKey tag;

   public TagMatchRuleTest(TagKey tag) {
      this.tag = tag;
   }

   public boolean test(BlockState state, Random random) {
      return state.isIn(this.tag);
   }

   protected RuleTestType getType() {
      return RuleTestType.TAG_MATCH;
   }

   static {
      CODEC = TagKey.unprefixedCodec(RegistryKeys.BLOCK).fieldOf("tag").xmap(TagMatchRuleTest::new, (ruleTest) -> {
         return ruleTest.tag;
      }).codec();
   }
}
