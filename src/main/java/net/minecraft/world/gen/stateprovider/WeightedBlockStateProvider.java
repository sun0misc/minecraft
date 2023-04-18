package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class WeightedBlockStateProvider extends BlockStateProvider {
   public static final Codec CODEC;
   private final DataPool states;

   private static DataResult wrap(DataPool states) {
      return states.isEmpty() ? DataResult.error(() -> {
         return "WeightedStateProvider with no states";
      }) : DataResult.success(new WeightedBlockStateProvider(states));
   }

   public WeightedBlockStateProvider(DataPool states) {
      this.states = states;
   }

   public WeightedBlockStateProvider(DataPool.Builder states) {
      this(states.build());
   }

   protected BlockStateProviderType getType() {
      return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
   }

   public BlockState get(Random random, BlockPos pos) {
      return (BlockState)this.states.getDataOrEmpty(random).orElseThrow(IllegalStateException::new);
   }

   static {
      CODEC = DataPool.createCodec(BlockState.CODEC).comapFlatMap(WeightedBlockStateProvider::wrap, (arg) -> {
         return arg.states;
      }).fieldOf("entries").codec();
   }
}
