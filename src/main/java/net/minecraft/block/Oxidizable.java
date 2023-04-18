package net.minecraft.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;

public interface Oxidizable extends Degradable {
   Supplier OXIDATION_LEVEL_INCREASES = Suppliers.memoize(() -> {
      return ImmutableBiMap.builder().put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER).put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER).put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER).put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER).put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER).put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER).put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB).put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB).put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB).put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS).put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS).put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS).build();
   });
   Supplier OXIDATION_LEVEL_DECREASES = Suppliers.memoize(() -> {
      return ((BiMap)OXIDATION_LEVEL_INCREASES.get()).inverse();
   });

   static Optional getDecreasedOxidationBlock(Block block) {
      return Optional.ofNullable((Block)((BiMap)OXIDATION_LEVEL_DECREASES.get()).get(block));
   }

   static Block getUnaffectedOxidationBlock(Block block) {
      Block lv = block;

      for(Block lv2 = (Block)((BiMap)OXIDATION_LEVEL_DECREASES.get()).get(block); lv2 != null; lv2 = (Block)((BiMap)OXIDATION_LEVEL_DECREASES.get()).get(lv2)) {
         lv = lv2;
      }

      return lv;
   }

   static Optional getDecreasedOxidationState(BlockState state) {
      return getDecreasedOxidationBlock(state.getBlock()).map((block) -> {
         return block.getStateWithProperties(state);
      });
   }

   static Optional getIncreasedOxidationBlock(Block block) {
      return Optional.ofNullable((Block)((BiMap)OXIDATION_LEVEL_INCREASES.get()).get(block));
   }

   static BlockState getUnaffectedOxidationState(BlockState state) {
      return getUnaffectedOxidationBlock(state.getBlock()).getStateWithProperties(state);
   }

   default Optional getDegradationResult(BlockState state) {
      return getIncreasedOxidationBlock(state.getBlock()).map((block) -> {
         return block.getStateWithProperties(state);
      });
   }

   default float getDegradationChanceMultiplier() {
      return this.getDegradationLevel() == Oxidizable.OxidationLevel.UNAFFECTED ? 0.75F : 1.0F;
   }

   public static enum OxidationLevel {
      UNAFFECTED,
      EXPOSED,
      WEATHERED,
      OXIDIZED;

      // $FF: synthetic method
      private static OxidationLevel[] method_36712() {
         return new OxidationLevel[]{UNAFFECTED, EXPOSED, WEATHERED, OXIDIZED};
      }
   }
}
