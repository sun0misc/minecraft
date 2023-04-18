package net.minecraft.world.biome.source;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;

public abstract class BiomeSource implements BiomeSupplier {
   public static final Codec CODEC;
   private final Supplier biomes = Suppliers.memoize(() -> {
      return (Set)this.biomeStream().distinct().collect(ImmutableSet.toImmutableSet());
   });

   protected BiomeSource() {
   }

   protected abstract Codec getCodec();

   protected abstract Stream biomeStream();

   public Set getBiomes() {
      return (Set)this.biomes.get();
   }

   public Set getBiomesInArea(int x, int y, int z, int radius, MultiNoiseUtil.MultiNoiseSampler sampler) {
      int m = BiomeCoords.fromBlock(x - radius);
      int n = BiomeCoords.fromBlock(y - radius);
      int o = BiomeCoords.fromBlock(z - radius);
      int p = BiomeCoords.fromBlock(x + radius);
      int q = BiomeCoords.fromBlock(y + radius);
      int r = BiomeCoords.fromBlock(z + radius);
      int s = p - m + 1;
      int t = q - n + 1;
      int u = r - o + 1;
      Set set = Sets.newHashSet();

      for(int v = 0; v < u; ++v) {
         for(int w = 0; w < s; ++w) {
            for(int x = 0; x < t; ++x) {
               int y = m + w;
               int z = n + x;
               int aa = o + v;
               set.add(this.getBiome(y, z, aa, sampler));
            }
         }
      }

      return set;
   }

   @Nullable
   public Pair locateBiome(int x, int y, int z, int radius, Predicate predicate, Random random, MultiNoiseUtil.MultiNoiseSampler noiseSampler) {
      return this.locateBiome(x, y, z, radius, 1, predicate, random, false, noiseSampler);
   }

   @Nullable
   public Pair locateBiome(BlockPos origin, int radius, int horizontalBlockCheckInterval, int verticalBlockCheckInterval, Predicate predicate, MultiNoiseUtil.MultiNoiseSampler noiseSampler, WorldView world) {
      Set set = (Set)this.getBiomes().stream().filter(predicate).collect(Collectors.toUnmodifiableSet());
      if (set.isEmpty()) {
         return null;
      } else {
         int l = Math.floorDiv(radius, horizontalBlockCheckInterval);
         int[] is = MathHelper.stream(origin.getY(), world.getBottomY() + 1, world.getTopY(), verticalBlockCheckInterval).toArray();
         Iterator var11 = BlockPos.iterateInSquare(BlockPos.ORIGIN, l, Direction.EAST, Direction.SOUTH).iterator();

         while(var11.hasNext()) {
            BlockPos.Mutable lv = (BlockPos.Mutable)var11.next();
            int m = origin.getX() + lv.getX() * horizontalBlockCheckInterval;
            int n = origin.getZ() + lv.getZ() * horizontalBlockCheckInterval;
            int o = BiomeCoords.fromBlock(m);
            int p = BiomeCoords.fromBlock(n);
            int[] var17 = is;
            int var18 = is.length;

            for(int var19 = 0; var19 < var18; ++var19) {
               int q = var17[var19];
               int r = BiomeCoords.fromBlock(q);
               RegistryEntry lv2 = this.getBiome(o, r, p, noiseSampler);
               if (set.contains(lv2)) {
                  return Pair.of(new BlockPos(m, q, n), lv2);
               }
            }
         }

         return null;
      }
   }

   @Nullable
   public Pair locateBiome(int x, int y, int z, int radius, int blockCheckInterval, Predicate predicate, Random random, boolean bl, MultiNoiseUtil.MultiNoiseSampler noiseSampler) {
      int n = BiomeCoords.fromBlock(x);
      int o = BiomeCoords.fromBlock(z);
      int p = BiomeCoords.fromBlock(radius);
      int q = BiomeCoords.fromBlock(y);
      Pair pair = null;
      int r = 0;
      int s = bl ? 0 : p;

      for(int t = s; t <= p; t += blockCheckInterval) {
         for(int u = SharedConstants.DEBUG_BIOME_SOURCE ? 0 : -t; u <= t; u += blockCheckInterval) {
            boolean bl2 = Math.abs(u) == t;

            for(int v = -t; v <= t; v += blockCheckInterval) {
               if (bl) {
                  boolean bl3 = Math.abs(v) == t;
                  if (!bl3 && !bl2) {
                     continue;
                  }
               }

               int w = n + v;
               int x = o + u;
               RegistryEntry lv = this.getBiome(w, q, x, noiseSampler);
               if (predicate.test(lv)) {
                  if (pair == null || random.nextInt(r + 1) == 0) {
                     BlockPos lv2 = new BlockPos(BiomeCoords.toBlock(w), y, BiomeCoords.toBlock(x));
                     if (bl) {
                        return Pair.of(lv2, lv);
                     }

                     pair = Pair.of(lv2, lv);
                  }

                  ++r;
               }
            }
         }
      }

      return pair;
   }

   public abstract RegistryEntry getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise);

   public void addDebugInfo(List info, BlockPos pos, MultiNoiseUtil.MultiNoiseSampler noiseSampler) {
   }

   static {
      CODEC = Registries.BIOME_SOURCE.getCodec().dispatchStable(BiomeSource::getCodec, Function.identity());
   }
}
