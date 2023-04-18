package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;

public class BlockState extends AbstractBlock.AbstractBlockState {
   public static final Codec CODEC;

   public BlockState(Block arg, ImmutableMap immutableMap, MapCodec mapCodec) {
      super(arg, immutableMap, mapCodec);
   }

   protected BlockState asBlockState() {
      return this;
   }

   static {
      CODEC = createCodec(Registries.BLOCK.getCodec(), Block::getDefaultState).stable();
   }
}
