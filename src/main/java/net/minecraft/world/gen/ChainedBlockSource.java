package net.minecraft.world.gen;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;

public record ChainedBlockSource(List samplers) implements ChunkNoiseSampler.BlockStateSampler {
   public ChainedBlockSource(List samplers) {
      this.samplers = samplers;
   }

   @Nullable
   public BlockState sample(DensityFunction.NoisePos pos) {
      Iterator var2 = this.samplers.iterator();

      BlockState lv2;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         ChunkNoiseSampler.BlockStateSampler lv = (ChunkNoiseSampler.BlockStateSampler)var2.next();
         lv2 = lv.sample(pos);
      } while(lv2 == null);

      return lv2;
   }

   public List samplers() {
      return this.samplers;
   }
}
