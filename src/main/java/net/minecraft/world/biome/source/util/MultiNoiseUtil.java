package net.minecraft.world.biome.source.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.Nullable;

public class MultiNoiseUtil {
   private static final boolean field_34477 = false;
   private static final float TO_LONG_FACTOR = 10000.0F;
   @VisibleForTesting
   protected static final int HYPERCUBE_DIMENSION = 7;

   public static NoiseValuePoint createNoiseValuePoint(float temperatureNoise, float humidityNoise, float continentalnessNoise, float erosionNoise, float depth, float weirdnessNoise) {
      return new NoiseValuePoint(toLong(temperatureNoise), toLong(humidityNoise), toLong(continentalnessNoise), toLong(erosionNoise), toLong(depth), toLong(weirdnessNoise));
   }

   public static NoiseHypercube createNoiseHypercube(float temperature, float humidity, float continentalness, float erosion, float depth, float weirdness, float offset) {
      return new NoiseHypercube(MultiNoiseUtil.ParameterRange.of(temperature), MultiNoiseUtil.ParameterRange.of(humidity), MultiNoiseUtil.ParameterRange.of(continentalness), MultiNoiseUtil.ParameterRange.of(erosion), MultiNoiseUtil.ParameterRange.of(depth), MultiNoiseUtil.ParameterRange.of(weirdness), toLong(offset));
   }

   public static NoiseHypercube createNoiseHypercube(ParameterRange temperature, ParameterRange humidity, ParameterRange continentalness, ParameterRange erosion, ParameterRange depth, ParameterRange weirdness, float offset) {
      return new NoiseHypercube(temperature, humidity, continentalness, erosion, depth, weirdness, toLong(offset));
   }

   public static long toLong(float value) {
      return (long)(value * 10000.0F);
   }

   public static float toFloat(long value) {
      return (float)value / 10000.0F;
   }

   public static MultiNoiseSampler createEmptyMultiNoiseSampler() {
      DensityFunction lv = DensityFunctionTypes.zero();
      return new MultiNoiseSampler(lv, lv, lv, lv, lv, lv, List.of());
   }

   public static BlockPos findFittestPosition(List noises, MultiNoiseSampler sampler) {
      return (new FittestPositionFinder(noises, sampler)).bestResult.location();
   }

   public static record NoiseValuePoint(long temperatureNoise, long humidityNoise, long continentalnessNoise, long erosionNoise, long depth, long weirdnessNoise) {
      final long temperatureNoise;
      final long humidityNoise;
      final long continentalnessNoise;
      final long erosionNoise;
      final long depth;
      final long weirdnessNoise;

      public NoiseValuePoint(long l, long m, long n, long o, long p, long q) {
         this.temperatureNoise = l;
         this.humidityNoise = m;
         this.continentalnessNoise = n;
         this.erosionNoise = o;
         this.depth = p;
         this.weirdnessNoise = q;
      }

      @VisibleForTesting
      protected long[] getNoiseValueList() {
         return new long[]{this.temperatureNoise, this.humidityNoise, this.continentalnessNoise, this.erosionNoise, this.depth, this.weirdnessNoise, 0L};
      }

      public long temperatureNoise() {
         return this.temperatureNoise;
      }

      public long humidityNoise() {
         return this.humidityNoise;
      }

      public long continentalnessNoise() {
         return this.continentalnessNoise;
      }

      public long erosionNoise() {
         return this.erosionNoise;
      }

      public long depth() {
         return this.depth;
      }

      public long weirdnessNoise() {
         return this.weirdnessNoise;
      }
   }

   public static record NoiseHypercube(ParameterRange temperature, ParameterRange humidity, ParameterRange continentalness, ParameterRange erosion, ParameterRange depth, ParameterRange weirdness, long offset) {
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(MultiNoiseUtil.ParameterRange.CODEC.fieldOf("temperature").forGetter((arg) -> {
            return arg.temperature;
         }), MultiNoiseUtil.ParameterRange.CODEC.fieldOf("humidity").forGetter((arg) -> {
            return arg.humidity;
         }), MultiNoiseUtil.ParameterRange.CODEC.fieldOf("continentalness").forGetter((arg) -> {
            return arg.continentalness;
         }), MultiNoiseUtil.ParameterRange.CODEC.fieldOf("erosion").forGetter((arg) -> {
            return arg.erosion;
         }), MultiNoiseUtil.ParameterRange.CODEC.fieldOf("depth").forGetter((arg) -> {
            return arg.depth;
         }), MultiNoiseUtil.ParameterRange.CODEC.fieldOf("weirdness").forGetter((arg) -> {
            return arg.weirdness;
         }), Codec.floatRange(0.0F, 1.0F).fieldOf("offset").xmap(MultiNoiseUtil::toLong, MultiNoiseUtil::toFloat).forGetter((arg) -> {
            return arg.offset;
         })).apply(instance, NoiseHypercube::new);
      });

      public NoiseHypercube(ParameterRange temperature, ParameterRange humidity, ParameterRange continentalness, ParameterRange erosion, ParameterRange depth, ParameterRange weirdness, long l) {
         this.temperature = temperature;
         this.humidity = humidity;
         this.continentalness = continentalness;
         this.erosion = erosion;
         this.depth = depth;
         this.weirdness = weirdness;
         this.offset = l;
      }

      long getSquaredDistance(NoiseValuePoint point) {
         return MathHelper.square(this.temperature.getDistance(point.temperatureNoise)) + MathHelper.square(this.humidity.getDistance(point.humidityNoise)) + MathHelper.square(this.continentalness.getDistance(point.continentalnessNoise)) + MathHelper.square(this.erosion.getDistance(point.erosionNoise)) + MathHelper.square(this.depth.getDistance(point.depth)) + MathHelper.square(this.weirdness.getDistance(point.weirdnessNoise)) + MathHelper.square(this.offset);
      }

      protected List getParameters() {
         return ImmutableList.of(this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, new ParameterRange(this.offset, this.offset));
      }

      public ParameterRange temperature() {
         return this.temperature;
      }

      public ParameterRange humidity() {
         return this.humidity;
      }

      public ParameterRange continentalness() {
         return this.continentalness;
      }

      public ParameterRange erosion() {
         return this.erosion;
      }

      public ParameterRange depth() {
         return this.depth;
      }

      public ParameterRange weirdness() {
         return this.weirdness;
      }

      public long offset() {
         return this.offset;
      }
   }

   public static record ParameterRange(long min, long max) {
      public static final Codec CODEC = Codecs.createCodecForPairObject(Codec.floatRange(-2.0F, 2.0F), "min", "max", (min, max) -> {
         return min.compareTo(max) > 0 ? DataResult.error(() -> {
            return "Cannon construct interval, min > max (" + min + " > " + max + ")";
         }) : DataResult.success(new ParameterRange(MultiNoiseUtil.toLong(min), MultiNoiseUtil.toLong(max)));
      }, (arg) -> {
         return MultiNoiseUtil.toFloat(arg.min());
      }, (arg) -> {
         return MultiNoiseUtil.toFloat(arg.max());
      });

      public ParameterRange(long l, long m) {
         this.min = l;
         this.max = m;
      }

      public static ParameterRange of(float point) {
         return of(point, point);
      }

      public static ParameterRange of(float min, float max) {
         if (min > max) {
            throw new IllegalArgumentException("min > max: " + min + " " + max);
         } else {
            return new ParameterRange(MultiNoiseUtil.toLong(min), MultiNoiseUtil.toLong(max));
         }
      }

      public static ParameterRange combine(ParameterRange min, ParameterRange max) {
         if (min.min() > max.max()) {
            throw new IllegalArgumentException("min > max: " + min + " " + max);
         } else {
            return new ParameterRange(min.min(), max.max());
         }
      }

      public String toString() {
         return this.min == this.max ? String.format(Locale.ROOT, "%d", this.min) : String.format(Locale.ROOT, "[%d-%d]", this.min, this.max);
      }

      public long getDistance(long noise) {
         long m = noise - this.max;
         long n = this.min - noise;
         return m > 0L ? m : Math.max(n, 0L);
      }

      public long getDistance(ParameterRange other) {
         long l = other.min() - this.max;
         long m = this.min - other.max();
         return l > 0L ? l : Math.max(m, 0L);
      }

      public ParameterRange combine(@Nullable ParameterRange other) {
         return other == null ? this : new ParameterRange(Math.min(this.min, other.min()), Math.max(this.max, other.max()));
      }

      public long min() {
         return this.min;
      }

      public long max() {
         return this.max;
      }
   }

   public static record MultiNoiseSampler(DensityFunction temperature, DensityFunction humidity, DensityFunction continentalness, DensityFunction erosion, DensityFunction depth, DensityFunction weirdness, List spawnTarget) {
      public MultiNoiseSampler(DensityFunction arg, DensityFunction arg2, DensityFunction arg3, DensityFunction arg4, DensityFunction arg5, DensityFunction arg6, List list) {
         this.temperature = arg;
         this.humidity = arg2;
         this.continentalness = arg3;
         this.erosion = arg4;
         this.depth = arg5;
         this.weirdness = arg6;
         this.spawnTarget = list;
      }

      public NoiseValuePoint sample(int x, int y, int z) {
         int l = BiomeCoords.toBlock(x);
         int m = BiomeCoords.toBlock(y);
         int n = BiomeCoords.toBlock(z);
         DensityFunction.UnblendedNoisePos lv = new DensityFunction.UnblendedNoisePos(l, m, n);
         return MultiNoiseUtil.createNoiseValuePoint((float)this.temperature.sample(lv), (float)this.humidity.sample(lv), (float)this.continentalness.sample(lv), (float)this.erosion.sample(lv), (float)this.depth.sample(lv), (float)this.weirdness.sample(lv));
      }

      public BlockPos findBestSpawnPosition() {
         return this.spawnTarget.isEmpty() ? BlockPos.ORIGIN : MultiNoiseUtil.findFittestPosition(this.spawnTarget, this);
      }

      public DensityFunction temperature() {
         return this.temperature;
      }

      public DensityFunction humidity() {
         return this.humidity;
      }

      public DensityFunction continentalness() {
         return this.continentalness;
      }

      public DensityFunction erosion() {
         return this.erosion;
      }

      public DensityFunction depth() {
         return this.depth;
      }

      public DensityFunction weirdness() {
         return this.weirdness;
      }

      public List spawnTarget() {
         return this.spawnTarget;
      }
   }

   private static class FittestPositionFinder {
      Result bestResult;

      FittestPositionFinder(List noises, MultiNoiseSampler sampler) {
         this.bestResult = calculateFitness(noises, sampler, 0, 0);
         this.findFittest(noises, sampler, 2048.0F, 512.0F);
         this.findFittest(noises, sampler, 512.0F, 32.0F);
      }

      private void findFittest(List noises, MultiNoiseSampler sampler, float maxDistance, float step) {
         float h = 0.0F;
         float i = step;
         BlockPos lv = this.bestResult.location();

         while(i <= maxDistance) {
            int j = lv.getX() + (int)(Math.sin((double)h) * (double)i);
            int k = lv.getZ() + (int)(Math.cos((double)h) * (double)i);
            Result lv2 = calculateFitness(noises, sampler, j, k);
            if (lv2.fitness() < this.bestResult.fitness()) {
               this.bestResult = lv2;
            }

            h += step / i;
            if ((double)h > 6.283185307179586) {
               h = 0.0F;
               i += step;
            }
         }

      }

      private static Result calculateFitness(List noises, MultiNoiseSampler sampler, int x, int z) {
         double d = MathHelper.square(2500.0);
         int k = true;
         long l = (long)((double)MathHelper.square(10000.0F) * Math.pow((double)(MathHelper.square((long)x) + MathHelper.square((long)z)) / d, 2.0));
         NoiseValuePoint lv = sampler.sample(BiomeCoords.fromBlock(x), 0, BiomeCoords.fromBlock(z));
         NoiseValuePoint lv2 = new NoiseValuePoint(lv.temperatureNoise(), lv.humidityNoise(), lv.continentalnessNoise(), lv.erosionNoise(), 0L, lv.weirdnessNoise());
         long m = Long.MAX_VALUE;

         NoiseHypercube lv3;
         for(Iterator var13 = noises.iterator(); var13.hasNext(); m = Math.min(m, lv3.getSquaredDistance(lv2))) {
            lv3 = (NoiseHypercube)var13.next();
         }

         return new Result(new BlockPos(x, 0, z), l + m);
      }

      private static record Result(BlockPos location, long fitness) {
         Result(BlockPos arg, long l) {
            this.location = arg;
            this.fitness = l;
         }

         public BlockPos location() {
            return this.location;
         }

         public long fitness() {
            return this.fitness;
         }
      }
   }

   public static class Entries {
      private final List entries;
      private final SearchTree tree;

      public static Codec createCodec(MapCodec entryCodec) {
         return Codecs.nonEmptyList(RecordCodecBuilder.create((instance) -> {
            return instance.group(MultiNoiseUtil.NoiseHypercube.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), entryCodec.forGetter(Pair::getSecond)).apply(instance, Pair::of);
         }).listOf()).xmap(Entries::new, Entries::getEntries);
      }

      public Entries(List entries) {
         this.entries = entries;
         this.tree = MultiNoiseUtil.SearchTree.create(entries);
      }

      public List getEntries() {
         return this.entries;
      }

      public Object get(NoiseValuePoint point) {
         return this.getValue(point);
      }

      @VisibleForTesting
      public Object getValueSimple(NoiseValuePoint point) {
         Iterator iterator = this.getEntries().iterator();
         Pair pair = (Pair)iterator.next();
         long l = ((NoiseHypercube)pair.getFirst()).getSquaredDistance(point);
         Object object = pair.getSecond();

         while(iterator.hasNext()) {
            Pair pair2 = (Pair)iterator.next();
            long m = ((NoiseHypercube)pair2.getFirst()).getSquaredDistance(point);
            if (m < l) {
               l = m;
               object = pair2.getSecond();
            }
         }

         return object;
      }

      public Object getValue(NoiseValuePoint point) {
         return this.getValue(point, SearchTree.TreeNode::getSquaredDistance);
      }

      protected Object getValue(NoiseValuePoint point, NodeDistanceFunction distanceFunction) {
         return this.tree.get(point, distanceFunction);
      }
   }

   protected static final class SearchTree {
      private static final int MAX_NODES_FOR_SIMPLE_TREE = 6;
      private final TreeNode firstNode;
      private final ThreadLocal previousResultNode = new ThreadLocal();

      private SearchTree(TreeNode firstNode) {
         this.firstNode = firstNode;
      }

      public static SearchTree create(List entries) {
         if (entries.isEmpty()) {
            throw new IllegalArgumentException("Need at least one value to build the search tree.");
         } else {
            int i = ((NoiseHypercube)((Pair)entries.get(0)).getFirst()).getParameters().size();
            if (i != 7) {
               throw new IllegalStateException("Expecting parameter space to be 7, got " + i);
            } else {
               List list2 = (List)entries.stream().map((entry) -> {
                  return new TreeLeafNode((NoiseHypercube)entry.getFirst(), entry.getSecond());
               }).collect(Collectors.toCollection(ArrayList::new));
               return new SearchTree(createNode(i, list2));
            }
         }
      }

      private static TreeNode createNode(int parameterNumber, List subTree) {
         if (subTree.isEmpty()) {
            throw new IllegalStateException("Need at least one child to build a node");
         } else if (subTree.size() == 1) {
            return (TreeNode)subTree.get(0);
         } else if (subTree.size() <= 6) {
            subTree.sort(Comparator.comparingLong((node) -> {
               long l = 0L;

               for(int j = 0; j < parameterNumber; ++j) {
                  ParameterRange lv = node.parameters[j];
                  l += Math.abs((lv.min() + lv.max()) / 2L);
               }

               return l;
            }));
            return new TreeBranchNode(subTree);
         } else {
            long l = Long.MAX_VALUE;
            int j = -1;
            List list2 = null;

            for(int k = 0; k < parameterNumber; ++k) {
               sortTree(subTree, parameterNumber, k, false);
               List list3 = getBatchedTree(subTree);
               long m = 0L;

               TreeBranchNode lv;
               for(Iterator var10 = list3.iterator(); var10.hasNext(); m += getRangeLengthSum(lv.parameters)) {
                  lv = (TreeBranchNode)var10.next();
               }

               if (l > m) {
                  l = m;
                  j = k;
                  list2 = list3;
               }
            }

            sortTree(list2, parameterNumber, j, true);
            return new TreeBranchNode((List)list2.stream().map((node) -> {
               return createNode(parameterNumber, Arrays.asList(node.subTree));
            }).collect(Collectors.toList()));
         }
      }

      private static void sortTree(List subTree, int parameterNumber, int currentParameter, boolean abs) {
         Comparator comparator = createNodeComparator(currentParameter, abs);

         for(int k = 1; k < parameterNumber; ++k) {
            comparator = comparator.thenComparing(createNodeComparator((currentParameter + k) % parameterNumber, abs));
         }

         subTree.sort(comparator);
      }

      private static Comparator createNodeComparator(int currentParameter, boolean abs) {
         return Comparator.comparingLong((arg) -> {
            ParameterRange lv = arg.parameters[currentParameter];
            long l = (lv.min() + lv.max()) / 2L;
            return abs ? Math.abs(l) : l;
         });
      }

      private static List getBatchedTree(List nodes) {
         List list2 = Lists.newArrayList();
         List list3 = Lists.newArrayList();
         int i = (int)Math.pow(6.0, Math.floor(Math.log((double)nodes.size() - 0.01) / Math.log(6.0)));
         Iterator var4 = nodes.iterator();

         while(var4.hasNext()) {
            TreeNode lv = (TreeNode)var4.next();
            list3.add(lv);
            if (list3.size() >= i) {
               list2.add(new TreeBranchNode(list3));
               list3 = Lists.newArrayList();
            }
         }

         if (!list3.isEmpty()) {
            list2.add(new TreeBranchNode(list3));
         }

         return list2;
      }

      private static long getRangeLengthSum(ParameterRange[] parameters) {
         long l = 0L;
         ParameterRange[] var3 = parameters;
         int var4 = parameters.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            ParameterRange lv = var3[var5];
            l += Math.abs(lv.max() - lv.min());
         }

         return l;
      }

      static List getEnclosingParameters(List subTree) {
         if (subTree.isEmpty()) {
            throw new IllegalArgumentException("SubTree needs at least one child");
         } else {
            int i = true;
            List list2 = Lists.newArrayList();

            for(int j = 0; j < 7; ++j) {
               list2.add((Object)null);
            }

            Iterator var6 = subTree.iterator();

            while(var6.hasNext()) {
               TreeNode lv = (TreeNode)var6.next();

               for(int k = 0; k < 7; ++k) {
                  list2.set(k, lv.parameters[k].combine((ParameterRange)list2.get(k)));
               }
            }

            return list2;
         }
      }

      public Object get(NoiseValuePoint point, NodeDistanceFunction distanceFunction) {
         long[] ls = point.getNoiseValueList();
         TreeLeafNode lv = this.firstNode.getResultingNode(ls, (TreeLeafNode)this.previousResultNode.get(), distanceFunction);
         this.previousResultNode.set(lv);
         return lv.value;
      }

      abstract static class TreeNode {
         protected final ParameterRange[] parameters;

         protected TreeNode(List parameters) {
            this.parameters = (ParameterRange[])parameters.toArray(new ParameterRange[0]);
         }

         protected abstract TreeLeafNode getResultingNode(long[] otherParameters, @Nullable TreeLeafNode alternative, NodeDistanceFunction distanceFunction);

         protected long getSquaredDistance(long[] otherParameters) {
            long l = 0L;

            for(int i = 0; i < 7; ++i) {
               l += MathHelper.square(this.parameters[i].getDistance(otherParameters[i]));
            }

            return l;
         }

         public String toString() {
            return Arrays.toString(this.parameters);
         }
      }

      static final class TreeBranchNode extends TreeNode {
         final TreeNode[] subTree;

         protected TreeBranchNode(List list) {
            this(MultiNoiseUtil.SearchTree.getEnclosingParameters(list), list);
         }

         protected TreeBranchNode(List parameters, List subTree) {
            super(parameters);
            this.subTree = (TreeNode[])subTree.toArray(new TreeNode[0]);
         }

         protected TreeLeafNode getResultingNode(long[] otherParameters, @Nullable TreeLeafNode alternative, NodeDistanceFunction distanceFunction) {
            long l = alternative == null ? Long.MAX_VALUE : distanceFunction.getDistance(alternative, otherParameters);
            TreeLeafNode lv = alternative;
            TreeNode[] var7 = this.subTree;
            int var8 = var7.length;

            for(int var9 = 0; var9 < var8; ++var9) {
               TreeNode lv2 = var7[var9];
               long m = distanceFunction.getDistance(lv2, otherParameters);
               if (l > m) {
                  TreeLeafNode lv3 = lv2.getResultingNode(otherParameters, lv, distanceFunction);
                  long n = lv2 == lv3 ? m : distanceFunction.getDistance(lv3, otherParameters);
                  if (l > n) {
                     l = n;
                     lv = lv3;
                  }
               }
            }

            return lv;
         }
      }

      private static final class TreeLeafNode extends TreeNode {
         final Object value;

         TreeLeafNode(NoiseHypercube parameters, Object value) {
            super(parameters.getParameters());
            this.value = value;
         }

         protected TreeLeafNode getResultingNode(long[] otherParameters, @Nullable TreeLeafNode alternative, NodeDistanceFunction distanceFunction) {
            return this;
         }
      }
   }

   interface NodeDistanceFunction {
      long getDistance(SearchTree.TreeNode node, long[] otherParameters);
   }
}
