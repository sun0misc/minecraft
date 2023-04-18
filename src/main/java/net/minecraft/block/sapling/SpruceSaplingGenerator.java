package net.minecraft.block.sapling;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;

public class SpruceSaplingGenerator extends LargeTreeSaplingGenerator {
   protected RegistryKey getTreeFeature(Random random, boolean bees) {
      return TreeConfiguredFeatures.SPRUCE;
   }

   protected RegistryKey getLargeTreeFeature(Random random) {
      return random.nextBoolean() ? TreeConfiguredFeatures.MEGA_SPRUCE : TreeConfiguredFeatures.MEGA_PINE;
   }
}
