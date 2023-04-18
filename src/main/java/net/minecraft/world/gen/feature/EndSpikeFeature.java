package net.minecraft.world.gen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndSpikeFeature extends Feature {
   public static final int field_31516 = 10;
   private static final int field_31517 = 42;
   private static final LoadingCache CACHE;

   public EndSpikeFeature(Codec codec) {
      super(codec);
   }

   public static List getSpikes(StructureWorldAccess world) {
      Random lv = Random.create(world.getSeed());
      long l = lv.nextLong() & 65535L;
      return (List)CACHE.getUnchecked(l);
   }

   public boolean generate(FeatureContext context) {
      EndSpikeFeatureConfig lv = (EndSpikeFeatureConfig)context.getConfig();
      StructureWorldAccess lv2 = context.getWorld();
      Random lv3 = context.getRandom();
      BlockPos lv4 = context.getOrigin();
      List list = lv.getSpikes();
      if (list.isEmpty()) {
         list = getSpikes(lv2);
      }

      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         Spike lv5 = (Spike)var7.next();
         if (lv5.isInChunk(lv4)) {
            this.generateSpike(lv2, lv3, lv, lv5);
         }
      }

      return true;
   }

   private void generateSpike(ServerWorldAccess world, Random random, EndSpikeFeatureConfig config, Spike spike) {
      int i = spike.getRadius();
      Iterator var6 = BlockPos.iterate(new BlockPos(spike.getCenterX() - i, world.getBottomY(), spike.getCenterZ() - i), new BlockPos(spike.getCenterX() + i, spike.getHeight() + 10, spike.getCenterZ() + i)).iterator();

      while(true) {
         while(var6.hasNext()) {
            BlockPos lv = (BlockPos)var6.next();
            if (lv.getSquaredDistance((double)spike.getCenterX(), (double)lv.getY(), (double)spike.getCenterZ()) <= (double)(i * i + 1) && lv.getY() < spike.getHeight()) {
               this.setBlockState(world, lv, Blocks.OBSIDIAN.getDefaultState());
            } else if (lv.getY() > 65) {
               this.setBlockState(world, lv, Blocks.AIR.getDefaultState());
            }
         }

         if (spike.isGuarded()) {
            int j = true;
            int k = true;
            int l = true;
            BlockPos.Mutable lv2 = new BlockPos.Mutable();

            for(int m = -2; m <= 2; ++m) {
               for(int n = -2; n <= 2; ++n) {
                  for(int o = 0; o <= 3; ++o) {
                     boolean bl = MathHelper.abs(m) == 2;
                     boolean bl2 = MathHelper.abs(n) == 2;
                     boolean bl3 = o == 3;
                     if (bl || bl2 || bl3) {
                        boolean bl4 = m == -2 || m == 2 || bl3;
                        boolean bl5 = n == -2 || n == 2 || bl3;
                        BlockState lv3 = (BlockState)((BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, bl4 && n != -2)).with(PaneBlock.SOUTH, bl4 && n != 2)).with(PaneBlock.WEST, bl5 && m != -2)).with(PaneBlock.EAST, bl5 && m != 2);
                        this.setBlockState(world, lv2.set(spike.getCenterX() + m, spike.getHeight() + o, spike.getCenterZ() + n), lv3);
                     }
                  }
               }
            }
         }

         EndCrystalEntity lv4 = (EndCrystalEntity)EntityType.END_CRYSTAL.create(world.toServerWorld());
         if (lv4 != null) {
            lv4.setBeamTarget(config.getPos());
            lv4.setInvulnerable(config.isCrystalInvulnerable());
            lv4.refreshPositionAndAngles((double)spike.getCenterX() + 0.5, (double)(spike.getHeight() + 1), (double)spike.getCenterZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            world.spawnEntity(lv4);
            this.setBlockState(world, new BlockPos(spike.getCenterX(), spike.getHeight(), spike.getCenterZ()), Blocks.BEDROCK.getDefaultState());
         }

         return;
      }
   }

   static {
      CACHE = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build(new SpikeCache());
   }

   public static class Spike {
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.INT.fieldOf("centerX").orElse(0).forGetter((spike) -> {
            return spike.centerX;
         }), Codec.INT.fieldOf("centerZ").orElse(0).forGetter((spike) -> {
            return spike.centerZ;
         }), Codec.INT.fieldOf("radius").orElse(0).forGetter((spike) -> {
            return spike.radius;
         }), Codec.INT.fieldOf("height").orElse(0).forGetter((spike) -> {
            return spike.height;
         }), Codec.BOOL.fieldOf("guarded").orElse(false).forGetter((spike) -> {
            return spike.guarded;
         })).apply(instance, Spike::new);
      });
      private final int centerX;
      private final int centerZ;
      private final int radius;
      private final int height;
      private final boolean guarded;
      private final Box boundingBox;

      public Spike(int centerX, int centerZ, int radius, int height, boolean guarded) {
         this.centerX = centerX;
         this.centerZ = centerZ;
         this.radius = radius;
         this.height = height;
         this.guarded = guarded;
         this.boundingBox = new Box((double)(centerX - radius), (double)DimensionType.MIN_HEIGHT, (double)(centerZ - radius), (double)(centerX + radius), (double)DimensionType.MAX_COLUMN_HEIGHT, (double)(centerZ + radius));
      }

      public boolean isInChunk(BlockPos pos) {
         return ChunkSectionPos.getSectionCoord(pos.getX()) == ChunkSectionPos.getSectionCoord(this.centerX) && ChunkSectionPos.getSectionCoord(pos.getZ()) == ChunkSectionPos.getSectionCoord(this.centerZ);
      }

      public int getCenterX() {
         return this.centerX;
      }

      public int getCenterZ() {
         return this.centerZ;
      }

      public int getRadius() {
         return this.radius;
      }

      public int getHeight() {
         return this.height;
      }

      public boolean isGuarded() {
         return this.guarded;
      }

      public Box getBoundingBox() {
         return this.boundingBox;
      }
   }

   private static class SpikeCache extends CacheLoader {
      SpikeCache() {
      }

      public List load(Long long_) {
         IntArrayList intArrayList = Util.shuffle(IntStream.range(0, 10), Random.create(long_));
         List list = Lists.newArrayList();

         for(int i = 0; i < 10; ++i) {
            int j = MathHelper.floor(42.0 * Math.cos(2.0 * (-3.141592653589793 + 0.3141592653589793 * (double)i)));
            int k = MathHelper.floor(42.0 * Math.sin(2.0 * (-3.141592653589793 + 0.3141592653589793 * (double)i)));
            int l = intArrayList.get(i);
            int m = 2 + l / 3;
            int n = 76 + l * 3;
            boolean bl = l == 1 || l == 2;
            list.add(new Spike(j, k, m, n, bl));
         }

         return list;
      }

      // $FF: synthetic method
      public Object load(Object seed) throws Exception {
         return this.load((Long)seed);
      }
   }
}
