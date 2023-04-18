package net.minecraft.block.sapling;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import org.jetbrains.annotations.Nullable;

public class DarkOakSaplingGenerator extends LargeTreeSaplingGenerator {
   @Nullable
   protected RegistryKey getTreeFeature(Random random, boolean bees) {
      return null;
   }

   @Nullable
   protected RegistryKey getLargeTreeFeature(Random random) {
      return TreeConfiguredFeatures.DARK_OAK;
   }
}
