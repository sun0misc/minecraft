package net.minecraft.block.sapling;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;

public class JungleSaplingGenerator extends LargeTreeSaplingGenerator {
   protected RegistryKey getTreeFeature(Random random, boolean bees) {
      return TreeConfiguredFeatures.JUNGLE_TREE_NO_VINE;
   }

   protected RegistryKey getLargeTreeFeature(Random random) {
      return TreeConfiguredFeatures.MEGA_JUNGLE_TREE;
   }
}
