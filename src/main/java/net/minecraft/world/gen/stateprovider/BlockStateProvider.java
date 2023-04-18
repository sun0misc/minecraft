package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public abstract class BlockStateProvider {
   public static final Codec TYPE_CODEC;

   public static SimpleBlockStateProvider of(BlockState state) {
      return new SimpleBlockStateProvider(state);
   }

   public static SimpleBlockStateProvider of(Block block) {
      return new SimpleBlockStateProvider(block.getDefaultState());
   }

   protected abstract BlockStateProviderType getType();

   public abstract BlockState get(Random random, BlockPos pos);

   static {
      TYPE_CODEC = Registries.BLOCK_STATE_PROVIDER_TYPE.getCodec().dispatch(BlockStateProvider::getType, BlockStateProviderType::getCodec);
   }
}
