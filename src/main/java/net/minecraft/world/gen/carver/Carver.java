package net.minecraft.world.gen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

public abstract class Carver {
   public static final Carver CAVE;
   public static final Carver NETHER_CAVE;
   public static final Carver RAVINE;
   protected static final BlockState AIR;
   protected static final BlockState CAVE_AIR;
   protected static final FluidState WATER;
   protected static final FluidState LAVA;
   protected Set carvableFluids;
   private final Codec codec;

   private static Carver register(String name, Carver carver) {
      return (Carver)Registry.register(Registries.CARVER, (String)name, carver);
   }

   public Carver(Codec configCodec) {
      this.carvableFluids = ImmutableSet.of(Fluids.WATER);
      this.codec = configCodec.fieldOf("config").xmap(this::configure, ConfiguredCarver::config).codec();
   }

   public ConfiguredCarver configure(CarverConfig config) {
      return new ConfiguredCarver(this, config);
   }

   public Codec getCodec() {
      return this.codec;
   }

   public int getBranchFactor() {
      return 4;
   }

   protected boolean carveRegion(CarverContext context, CarverConfig config, Chunk chunk, Function posToBiome, AquiferSampler aquiferSampler, double d, double e, double f, double g, double h, CarvingMask mask, SkipPredicate skipPredicate) {
      ChunkPos lv = chunk.getPos();
      double i = (double)lv.getCenterX();
      double j = (double)lv.getCenterZ();
      double k = 16.0 + g * 2.0;
      if (!(Math.abs(d - i) > k) && !(Math.abs(f - j) > k)) {
         int l = lv.getStartX();
         int m = lv.getStartZ();
         int n = Math.max(MathHelper.floor(d - g) - l - 1, 0);
         int o = Math.min(MathHelper.floor(d + g) - l, 15);
         int p = Math.max(MathHelper.floor(e - h) - 1, context.getMinY() + 1);
         int q = chunk.hasBelowZeroRetrogen() ? 0 : 7;
         int r = Math.min(MathHelper.floor(e + h) + 1, context.getMinY() + context.getHeight() - 1 - q);
         int s = Math.max(MathHelper.floor(f - g) - m - 1, 0);
         int t = Math.min(MathHelper.floor(f + g) - m, 15);
         boolean bl = false;
         BlockPos.Mutable lv2 = new BlockPos.Mutable();
         BlockPos.Mutable lv3 = new BlockPos.Mutable();

         for(int u = n; u <= o; ++u) {
            int v = lv.getOffsetX(u);
            double w = ((double)v + 0.5 - d) / g;

            for(int x = s; x <= t; ++x) {
               int y = lv.getOffsetZ(x);
               double z = ((double)y + 0.5 - f) / g;
               if (!(w * w + z * z >= 1.0)) {
                  MutableBoolean mutableBoolean = new MutableBoolean(false);

                  for(int aa = r; aa > p; --aa) {
                     double ab = ((double)aa - 0.5 - e) / h;
                     if (!skipPredicate.shouldSkip(context, w, ab, z, aa) && (!mask.get(u, aa, x) || isDebug(config))) {
                        mask.set(u, aa, x);
                        lv2.set(v, aa, y);
                        bl |= this.carveAtPoint(context, config, chunk, posToBiome, mask, lv2, lv3, aquiferSampler, mutableBoolean);
                     }
                  }
               }
            }
         }

         return bl;
      } else {
         return false;
      }
   }

   protected boolean carveAtPoint(CarverContext context, CarverConfig config, Chunk chunk, Function posToBiome, CarvingMask mask, BlockPos.Mutable arg5, BlockPos.Mutable arg6, AquiferSampler aquiferSampler, MutableBoolean mutableBoolean) {
      BlockState lv = chunk.getBlockState(arg5);
      if (lv.isOf(Blocks.GRASS_BLOCK) || lv.isOf(Blocks.MYCELIUM)) {
         mutableBoolean.setTrue();
      }

      if (!this.canAlwaysCarveBlock(config, lv) && !isDebug(config)) {
         return false;
      } else {
         BlockState lv2 = this.getState(context, config, arg5, aquiferSampler);
         if (lv2 == null) {
            return false;
         } else {
            chunk.setBlockState(arg5, lv2, false);
            if (aquiferSampler.needsFluidTick() && !lv2.getFluidState().isEmpty()) {
               chunk.markBlockForPostProcessing(arg5);
            }

            if (mutableBoolean.isTrue()) {
               arg6.set(arg5, (Direction)Direction.DOWN);
               if (chunk.getBlockState(arg6).isOf(Blocks.DIRT)) {
                  context.applyMaterialRule(posToBiome, chunk, arg6, !lv2.getFluidState().isEmpty()).ifPresent((state) -> {
                     chunk.setBlockState(arg6, state, false);
                     if (!state.getFluidState().isEmpty()) {
                        chunk.markBlockForPostProcessing(arg6);
                     }

                  });
               }
            }

            return true;
         }
      }
   }

   @Nullable
   private BlockState getState(CarverContext context, CarverConfig config, BlockPos pos, AquiferSampler sampler) {
      if (pos.getY() <= config.lavaLevel.getY(context)) {
         return LAVA.getBlockState();
      } else {
         BlockState lv = sampler.apply(new DensityFunction.UnblendedNoisePos(pos.getX(), pos.getY(), pos.getZ()), 0.0);
         if (lv == null) {
            return isDebug(config) ? config.debugConfig.getBarrierState() : null;
         } else {
            return isDebug(config) ? getDebugState(config, lv) : lv;
         }
      }
   }

   private static BlockState getDebugState(CarverConfig config, BlockState state) {
      if (state.isOf(Blocks.AIR)) {
         return config.debugConfig.getAirState();
      } else if (state.isOf(Blocks.WATER)) {
         BlockState lv = config.debugConfig.getWaterState();
         return lv.contains(Properties.WATERLOGGED) ? (BlockState)lv.with(Properties.WATERLOGGED, true) : lv;
      } else {
         return state.isOf(Blocks.LAVA) ? config.debugConfig.getLavaState() : state;
      }
   }

   public abstract boolean carve(CarverContext context, CarverConfig config, Chunk chunk, Function posToBiome, Random random, AquiferSampler aquiferSampler, ChunkPos pos, CarvingMask mask);

   public abstract boolean shouldCarve(CarverConfig config, Random random);

   protected boolean canAlwaysCarveBlock(CarverConfig config, BlockState state) {
      return state.isIn(config.replaceable);
   }

   protected static boolean canCarveBranch(ChunkPos pos, double x, double z, int branchIndex, int branchCount, float baseWidth) {
      double g = (double)pos.getCenterX();
      double h = (double)pos.getCenterZ();
      double k = x - g;
      double l = z - h;
      double m = (double)(branchCount - branchIndex);
      double n = (double)(baseWidth + 2.0F + 16.0F);
      return k * k + l * l - m * m <= n * n;
   }

   private static boolean isDebug(CarverConfig config) {
      return config.debugConfig.isDebugMode();
   }

   static {
      CAVE = register("cave", new CaveCarver(CaveCarverConfig.CAVE_CODEC));
      NETHER_CAVE = register("nether_cave", new NetherCaveCarver(CaveCarverConfig.CAVE_CODEC));
      RAVINE = register("canyon", new RavineCarver(RavineCarverConfig.RAVINE_CODEC));
      AIR = Blocks.AIR.getDefaultState();
      CAVE_AIR = Blocks.CAVE_AIR.getDefaultState();
      WATER = Fluids.WATER.getDefaultState();
      LAVA = Fluids.LAVA.getDefaultState();
   }

   public interface SkipPredicate {
      boolean shouldSkip(CarverContext context, double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y);
   }
}
