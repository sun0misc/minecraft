package net.minecraft.world.gen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.AquiferSampler;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NetherCaveCarver extends CaveCarver {
   public NetherCaveCarver(Codec codec) {
      super(codec);
      this.carvableFluids = ImmutableSet.of(Fluids.LAVA, Fluids.WATER);
   }

   protected int getMaxCaveCount() {
      return 10;
   }

   protected float getTunnelSystemWidth(Random random) {
      return (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
   }

   protected double getTunnelSystemHeightWidthRatio() {
      return 5.0;
   }

   protected boolean carveAtPoint(CarverContext arg, CaveCarverConfig arg2, Chunk arg3, Function function, CarvingMask arg4, BlockPos.Mutable arg5, BlockPos.Mutable arg6, AquiferSampler arg7, MutableBoolean mutableBoolean) {
      if (this.canAlwaysCarveBlock(arg2, arg3.getBlockState(arg5))) {
         BlockState lv;
         if (arg5.getY() <= arg.getMinY() + 31) {
            lv = LAVA.getBlockState();
         } else {
            lv = CAVE_AIR;
         }

         arg3.setBlockState(arg5, lv, false);
         return true;
      } else {
         return false;
      }
   }
}
