package net.minecraft.block.sapling;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;

public class CherrySaplingGenerator extends SaplingGenerator {
   protected RegistryKey getTreeFeature(Random random, boolean bees) {
      return bees ? TreeConfiguredFeatures.CHERRY_BEES_005 : TreeConfiguredFeatures.CHERRY;
   }
}
