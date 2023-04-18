package net.minecraft.world.gen.surfacebuilder;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.VerticalSurfaceType;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.Nullable;

public class MaterialRules {
   public static final MaterialCondition STONE_DEPTH_FLOOR;
   public static final MaterialCondition STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH;
   public static final MaterialCondition STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH_RANGE_6;
   public static final MaterialCondition STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH_RANGE_30;
   public static final MaterialCondition STONE_DEPTH_CEILING;
   public static final MaterialCondition STONE_DEPTH_CEILING_WITH_SURFACE_DEPTH;

   public static MaterialCondition stoneDepth(int offset, boolean addSurfaceDepth, VerticalSurfaceType verticalSurfaceType) {
      return new StoneDepthMaterialCondition(offset, addSurfaceDepth, 0, verticalSurfaceType);
   }

   public static MaterialCondition stoneDepth(int offset, boolean addSurfaceDepth, int secondaryDepthRange, VerticalSurfaceType verticalSurfaceType) {
      return new StoneDepthMaterialCondition(offset, addSurfaceDepth, secondaryDepthRange, verticalSurfaceType);
   }

   public static MaterialCondition not(MaterialCondition target) {
      return new NotMaterialCondition(target);
   }

   public static MaterialCondition aboveY(YOffset anchor, int runDepthMultiplier) {
      return new AboveYMaterialCondition(anchor, runDepthMultiplier, false);
   }

   public static MaterialCondition aboveYWithStoneDepth(YOffset anchor, int runDepthMultiplier) {
      return new AboveYMaterialCondition(anchor, runDepthMultiplier, true);
   }

   public static MaterialCondition water(int offset, int runDepthMultiplier) {
      return new WaterMaterialCondition(offset, runDepthMultiplier, false);
   }

   public static MaterialCondition waterWithStoneDepth(int offset, int runDepthMultiplier) {
      return new WaterMaterialCondition(offset, runDepthMultiplier, true);
   }

   @SafeVarargs
   public static MaterialCondition biome(RegistryKey... biomes) {
      return biome(List.of(biomes));
   }

   private static BiomeMaterialCondition biome(List biomes) {
      return new BiomeMaterialCondition(biomes);
   }

   public static MaterialCondition noiseThreshold(RegistryKey noise, double min) {
      return noiseThreshold(noise, min, Double.MAX_VALUE);
   }

   public static MaterialCondition noiseThreshold(RegistryKey noise, double min, double max) {
      return new NoiseThresholdMaterialCondition(noise, min, max);
   }

   public static MaterialCondition verticalGradient(String id, YOffset trueAtAndBelow, YOffset falseAtAndAbove) {
      return new VerticalGradientMaterialCondition(new Identifier(id), trueAtAndBelow, falseAtAndAbove);
   }

   public static MaterialCondition steepSlope() {
      return MaterialRules.SteepMaterialCondition.INSTANCE;
   }

   public static MaterialCondition hole() {
      return MaterialRules.HoleMaterialCondition.INSTANCE;
   }

   public static MaterialCondition surface() {
      return MaterialRules.SurfaceMaterialCondition.INSTANCE;
   }

   public static MaterialCondition temperature() {
      return MaterialRules.TemperatureMaterialCondition.INSTANCE;
   }

   public static MaterialRule condition(MaterialCondition condition, MaterialRule rule) {
      return new ConditionMaterialRule(condition, rule);
   }

   public static MaterialRule sequence(MaterialRule... rules) {
      if (rules.length == 0) {
         throw new IllegalArgumentException("Need at least 1 rule for a sequence");
      } else {
         return new SequenceMaterialRule(Arrays.asList(rules));
      }
   }

   public static MaterialRule block(BlockState state) {
      return new BlockMaterialRule(state);
   }

   public static MaterialRule terracottaBands() {
      return MaterialRules.TerracottaBandsMaterialRule.INSTANCE;
   }

   static Codec register(Registry registry, String id, CodecHolder codecHolder) {
      return (Codec)Registry.register(registry, (String)id, codecHolder.codec());
   }

   static {
      STONE_DEPTH_FLOOR = stoneDepth(0, false, VerticalSurfaceType.FLOOR);
      STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH = stoneDepth(0, true, VerticalSurfaceType.FLOOR);
      STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH_RANGE_6 = stoneDepth(0, true, 6, VerticalSurfaceType.FLOOR);
      STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH_RANGE_30 = stoneDepth(0, true, 30, VerticalSurfaceType.FLOOR);
      STONE_DEPTH_CEILING = stoneDepth(0, false, VerticalSurfaceType.CEILING);
      STONE_DEPTH_CEILING_WITH_SURFACE_DEPTH = stoneDepth(0, true, VerticalSurfaceType.CEILING);
   }

   private static record StoneDepthMaterialCondition(int offset, boolean addSurfaceDepth, int secondaryDepthRange, VerticalSurfaceType surfaceType) implements MaterialCondition {
      final int offset;
      final boolean addSurfaceDepth;
      final int secondaryDepthRange;
      static final CodecHolder CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(Codec.INT.fieldOf("offset").forGetter(StoneDepthMaterialCondition::offset), Codec.BOOL.fieldOf("add_surface_depth").forGetter(StoneDepthMaterialCondition::addSurfaceDepth), Codec.INT.fieldOf("secondary_depth_range").forGetter(StoneDepthMaterialCondition::secondaryDepthRange), VerticalSurfaceType.CODEC.fieldOf("surface_type").forGetter(StoneDepthMaterialCondition::surfaceType)).apply(instance, StoneDepthMaterialCondition::new);
      }));

      StoneDepthMaterialCondition(int i, boolean bl, int j, VerticalSurfaceType arg) {
         this.offset = i;
         this.addSurfaceDepth = bl;
         this.secondaryDepthRange = j;
         this.surfaceType = arg;
      }

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(final MaterialRuleContext arg) {
         final boolean bl = this.surfaceType == VerticalSurfaceType.CEILING;

         class StoneDepthPredicate extends FullLazyAbstractPredicate {
            StoneDepthPredicate() {
               super(arg);
            }

            protected boolean test() {
               int i = bl ? this.context.stoneDepthBelow : this.context.stoneDepthAbove;
               int j = StoneDepthMaterialCondition.this.addSurfaceDepth ? this.context.runDepth : 0;
               int k = StoneDepthMaterialCondition.this.secondaryDepthRange == 0 ? 0 : (int)MathHelper.map(this.context.method_39550(), -1.0, 1.0, 0.0, (double)StoneDepthMaterialCondition.this.secondaryDepthRange);
               return i <= 1 + StoneDepthMaterialCondition.this.offset + j + k;
            }
         }

         return new StoneDepthPredicate();
      }

      public int offset() {
         return this.offset;
      }

      public boolean addSurfaceDepth() {
         return this.addSurfaceDepth;
      }

      public int secondaryDepthRange() {
         return this.secondaryDepthRange;
      }

      public VerticalSurfaceType surfaceType() {
         return this.surfaceType;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }
   }

   private static record NotMaterialCondition(MaterialCondition target) implements MaterialCondition {
      static final CodecHolder CODEC;

      NotMaterialCondition(MaterialCondition arg) {
         this.target = arg;
      }

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(MaterialRuleContext arg) {
         return new InvertedBooleanSupplier((BooleanSupplier)this.target.apply(arg));
      }

      public MaterialCondition target() {
         return this.target;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }

      static {
         CODEC = CodecHolder.of(MaterialRules.MaterialCondition.CODEC.xmap(NotMaterialCondition::new, NotMaterialCondition::target).fieldOf("invert"));
      }
   }

   public interface MaterialCondition extends Function {
      Codec CODEC = Registries.MATERIAL_CONDITION.getCodec().dispatch((arg) -> {
         return arg.codec().codec();
      }, Function.identity());

      static Codec registerAndGetDefault(Registry registry) {
         MaterialRules.register(registry, "biome", MaterialRules.BiomeMaterialCondition.CODEC);
         MaterialRules.register(registry, "noise_threshold", MaterialRules.NoiseThresholdMaterialCondition.CODEC);
         MaterialRules.register(registry, "vertical_gradient", MaterialRules.VerticalGradientMaterialCondition.CODEC);
         MaterialRules.register(registry, "y_above", MaterialRules.AboveYMaterialCondition.CODEC);
         MaterialRules.register(registry, "water", MaterialRules.WaterMaterialCondition.CODEC);
         MaterialRules.register(registry, "temperature", MaterialRules.TemperatureMaterialCondition.CODEC);
         MaterialRules.register(registry, "steep", MaterialRules.SteepMaterialCondition.CODEC);
         MaterialRules.register(registry, "not", MaterialRules.NotMaterialCondition.CODEC);
         MaterialRules.register(registry, "hole", MaterialRules.HoleMaterialCondition.CODEC);
         MaterialRules.register(registry, "above_preliminary_surface", MaterialRules.SurfaceMaterialCondition.CODEC);
         return MaterialRules.register(registry, "stone_depth", MaterialRules.StoneDepthMaterialCondition.CODEC);
      }

      CodecHolder codec();
   }

   private static record AboveYMaterialCondition(YOffset anchor, int surfaceDepthMultiplier, boolean addStoneDepth) implements MaterialCondition {
      final YOffset anchor;
      final int surfaceDepthMultiplier;
      final boolean addStoneDepth;
      static final CodecHolder CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(YOffset.OFFSET_CODEC.fieldOf("anchor").forGetter(AboveYMaterialCondition::anchor), Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier").forGetter(AboveYMaterialCondition::surfaceDepthMultiplier), Codec.BOOL.fieldOf("add_stone_depth").forGetter(AboveYMaterialCondition::addStoneDepth)).apply(instance, AboveYMaterialCondition::new);
      }));

      AboveYMaterialCondition(YOffset arg, int i, boolean bl) {
         this.anchor = arg;
         this.surfaceDepthMultiplier = i;
         this.addStoneDepth = bl;
      }

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(final MaterialRuleContext arg) {
         class AboveYPredicate extends FullLazyAbstractPredicate {
            AboveYPredicate() {
               super(arg);
            }

            protected boolean test() {
               return this.context.blockY + (AboveYMaterialCondition.this.addStoneDepth ? this.context.stoneDepthAbove : 0) >= AboveYMaterialCondition.this.anchor.getY(this.context.heightContext) + this.context.runDepth * AboveYMaterialCondition.this.surfaceDepthMultiplier;
            }
         }

         return new AboveYPredicate();
      }

      public YOffset anchor() {
         return this.anchor;
      }

      public int surfaceDepthMultiplier() {
         return this.surfaceDepthMultiplier;
      }

      public boolean addStoneDepth() {
         return this.addStoneDepth;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }
   }

   private static record WaterMaterialCondition(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) implements MaterialCondition {
      final int offset;
      final int surfaceDepthMultiplier;
      final boolean addStoneDepth;
      static final CodecHolder CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(Codec.INT.fieldOf("offset").forGetter(WaterMaterialCondition::offset), Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier").forGetter(WaterMaterialCondition::surfaceDepthMultiplier), Codec.BOOL.fieldOf("add_stone_depth").forGetter(WaterMaterialCondition::addStoneDepth)).apply(instance, WaterMaterialCondition::new);
      }));

      WaterMaterialCondition(int i, int j, boolean bl) {
         this.offset = i;
         this.surfaceDepthMultiplier = j;
         this.addStoneDepth = bl;
      }

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(final MaterialRuleContext arg) {
         class WaterPredicate extends FullLazyAbstractPredicate {
            WaterPredicate() {
               super(arg);
            }

            protected boolean test() {
               return this.context.fluidHeight == Integer.MIN_VALUE || this.context.blockY + (WaterMaterialCondition.this.addStoneDepth ? this.context.stoneDepthAbove : 0) >= this.context.fluidHeight + WaterMaterialCondition.this.offset + this.context.runDepth * WaterMaterialCondition.this.surfaceDepthMultiplier;
            }
         }

         return new WaterPredicate();
      }

      public int offset() {
         return this.offset;
      }

      public int surfaceDepthMultiplier() {
         return this.surfaceDepthMultiplier;
      }

      public boolean addStoneDepth() {
         return this.addStoneDepth;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }
   }

   private static final class BiomeMaterialCondition implements MaterialCondition {
      static final CodecHolder CODEC;
      private final List biomes;
      final Predicate predicate;

      BiomeMaterialCondition(List biomes) {
         this.biomes = biomes;
         Set var10001 = Set.copyOf(biomes);
         Objects.requireNonNull(var10001);
         this.predicate = var10001::contains;
      }

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(final MaterialRuleContext arg) {
         class BiomePredicate extends FullLazyAbstractPredicate {
            BiomePredicate() {
               super(arg);
            }

            protected boolean test() {
               return ((RegistryEntry)this.context.biomeSupplier.get()).matches(BiomeMaterialCondition.this.predicate);
            }
         }

         return new BiomePredicate();
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object instanceof BiomeMaterialCondition) {
            BiomeMaterialCondition lv = (BiomeMaterialCondition)object;
            return this.biomes.equals(lv.biomes);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.biomes.hashCode();
      }

      public String toString() {
         return "BiomeConditionSource[biomes=" + this.biomes + "]";
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }

      static {
         CODEC = CodecHolder.of(RegistryKey.createCodec(RegistryKeys.BIOME).listOf().fieldOf("biome_is").xmap(MaterialRules::biome, (arg) -> {
            return arg.biomes;
         }));
      }
   }

   private static record NoiseThresholdMaterialCondition(RegistryKey noise, double minThreshold, double maxThreshold) implements MaterialCondition {
      final double minThreshold;
      final double maxThreshold;
      static final CodecHolder CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(RegistryKey.createCodec(RegistryKeys.NOISE_PARAMETERS).fieldOf("noise").forGetter(NoiseThresholdMaterialCondition::noise), Codec.DOUBLE.fieldOf("min_threshold").forGetter(NoiseThresholdMaterialCondition::minThreshold), Codec.DOUBLE.fieldOf("max_threshold").forGetter(NoiseThresholdMaterialCondition::maxThreshold)).apply(instance, NoiseThresholdMaterialCondition::new);
      }));

      NoiseThresholdMaterialCondition(RegistryKey arg, double d, double e) {
         this.noise = arg;
         this.minThreshold = d;
         this.maxThreshold = e;
      }

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(final MaterialRuleContext arg) {
         final DoublePerlinNoiseSampler lv = arg.noiseConfig.getOrCreateSampler(this.noise);

         class NoiseThresholdPredicate extends HorizontalLazyAbstractPredicate {
            NoiseThresholdPredicate() {
               super(arg);
            }

            protected boolean test() {
               double d = lv.sample((double)this.context.blockX, 0.0, (double)this.context.blockZ);
               return d >= NoiseThresholdMaterialCondition.this.minThreshold && d <= NoiseThresholdMaterialCondition.this.maxThreshold;
            }
         }

         return new NoiseThresholdPredicate();
      }

      public RegistryKey noise() {
         return this.noise;
      }

      public double minThreshold() {
         return this.minThreshold;
      }

      public double maxThreshold() {
         return this.maxThreshold;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }
   }

   private static record VerticalGradientMaterialCondition(Identifier randomName, YOffset trueAtAndBelow, YOffset falseAtAndAbove) implements MaterialCondition {
      static final CodecHolder CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(Identifier.CODEC.fieldOf("random_name").forGetter(VerticalGradientMaterialCondition::randomName), YOffset.OFFSET_CODEC.fieldOf("true_at_and_below").forGetter(VerticalGradientMaterialCondition::trueAtAndBelow), YOffset.OFFSET_CODEC.fieldOf("false_at_and_above").forGetter(VerticalGradientMaterialCondition::falseAtAndAbove)).apply(instance, VerticalGradientMaterialCondition::new);
      }));

      VerticalGradientMaterialCondition(Identifier arg, YOffset arg2, YOffset arg3) {
         this.randomName = arg;
         this.trueAtAndBelow = arg2;
         this.falseAtAndAbove = arg3;
      }

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(final MaterialRuleContext arg) {
         final int i = this.trueAtAndBelow().getY(arg.heightContext);
         final int j = this.falseAtAndAbove().getY(arg.heightContext);
         final RandomSplitter lv = arg.noiseConfig.getOrCreateRandomDeriver(this.randomName());

         class VerticalGradientPredicate extends FullLazyAbstractPredicate {
            VerticalGradientPredicate() {
               super(arg);
            }

            protected boolean test() {
               int ix = this.context.blockY;
               if (ix <= i) {
                  return true;
               } else if (ix >= j) {
                  return false;
               } else {
                  double d = MathHelper.map((double)ix, (double)i, (double)j, 1.0, 0.0);
                  Random lvx = lv.split(this.context.blockX, ix, this.context.blockZ);
                  return (double)lvx.nextFloat() < d;
               }
            }
         }

         return new VerticalGradientPredicate();
      }

      public Identifier randomName() {
         return this.randomName;
      }

      public YOffset trueAtAndBelow() {
         return this.trueAtAndBelow;
      }

      public YOffset falseAtAndAbove() {
         return this.falseAtAndAbove;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }
   }

   private static enum SteepMaterialCondition implements MaterialCondition {
      INSTANCE;

      static final CodecHolder CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(MaterialRuleContext arg) {
         return arg.steepSlopePredicate;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }

      // $FF: synthetic method
      private static SteepMaterialCondition[] method_39088() {
         return new SteepMaterialCondition[]{INSTANCE};
      }
   }

   private static enum HoleMaterialCondition implements MaterialCondition {
      INSTANCE;

      static final CodecHolder CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(MaterialRuleContext arg) {
         return arg.negativeRunDepthPredicate;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }

      // $FF: synthetic method
      private static HoleMaterialCondition[] method_39080() {
         return new HoleMaterialCondition[]{INSTANCE};
      }
   }

   private static enum SurfaceMaterialCondition implements MaterialCondition {
      INSTANCE;

      static final CodecHolder CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(MaterialRuleContext arg) {
         return arg.surfacePredicate;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }

      // $FF: synthetic method
      private static SurfaceMaterialCondition[] method_39475() {
         return new SurfaceMaterialCondition[]{INSTANCE};
      }
   }

   private static enum TemperatureMaterialCondition implements MaterialCondition {
      INSTANCE;

      static final CodecHolder CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));

      public CodecHolder codec() {
         return CODEC;
      }

      public BooleanSupplier apply(MaterialRuleContext arg) {
         return arg.biomeTemperaturePredicate;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }

      // $FF: synthetic method
      private static TemperatureMaterialCondition[] method_39093() {
         return new TemperatureMaterialCondition[]{INSTANCE};
      }
   }

   private static record ConditionMaterialRule(MaterialCondition ifTrue, MaterialRule thenRun) implements MaterialRule {
      static final CodecHolder CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(MaterialRules.MaterialCondition.CODEC.fieldOf("if_true").forGetter(ConditionMaterialRule::ifTrue), MaterialRules.MaterialRule.CODEC.fieldOf("then_run").forGetter(ConditionMaterialRule::thenRun)).apply(instance, ConditionMaterialRule::new);
      }));

      ConditionMaterialRule(MaterialCondition arg, MaterialRule arg2) {
         this.ifTrue = arg;
         this.thenRun = arg2;
      }

      public CodecHolder codec() {
         return CODEC;
      }

      public BlockStateRule apply(MaterialRuleContext arg) {
         return new ConditionalBlockStateRule((BooleanSupplier)this.ifTrue.apply(arg), (BlockStateRule)this.thenRun.apply(arg));
      }

      public MaterialCondition ifTrue() {
         return this.ifTrue;
      }

      public MaterialRule thenRun() {
         return this.thenRun;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }
   }

   public interface MaterialRule extends Function {
      Codec CODEC = Registries.MATERIAL_RULE.getCodec().dispatch((arg) -> {
         return arg.codec().codec();
      }, Function.identity());

      static Codec registerAndGetDefault(Registry registry) {
         MaterialRules.register(registry, "bandlands", MaterialRules.TerracottaBandsMaterialRule.CODEC);
         MaterialRules.register(registry, "block", MaterialRules.BlockMaterialRule.CODEC);
         MaterialRules.register(registry, "sequence", MaterialRules.SequenceMaterialRule.CODEC);
         return MaterialRules.register(registry, "condition", MaterialRules.ConditionMaterialRule.CODEC);
      }

      CodecHolder codec();
   }

   private static record SequenceMaterialRule(List sequence) implements MaterialRule {
      static final CodecHolder CODEC;

      SequenceMaterialRule(List list) {
         this.sequence = list;
      }

      public CodecHolder codec() {
         return CODEC;
      }

      public BlockStateRule apply(MaterialRuleContext arg) {
         if (this.sequence.size() == 1) {
            return (BlockStateRule)((MaterialRule)this.sequence.get(0)).apply(arg);
         } else {
            ImmutableList.Builder builder = ImmutableList.builder();
            Iterator var3 = this.sequence.iterator();

            while(var3.hasNext()) {
               MaterialRule lv = (MaterialRule)var3.next();
               builder.add((BlockStateRule)lv.apply(arg));
            }

            return new SequenceBlockStateRule(builder.build());
         }
      }

      public List sequence() {
         return this.sequence;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }

      static {
         CODEC = CodecHolder.of(MaterialRules.MaterialRule.CODEC.listOf().xmap(SequenceMaterialRule::new, SequenceMaterialRule::sequence).fieldOf("sequence"));
      }
   }

   static record BlockMaterialRule(BlockState resultState, SimpleBlockStateRule rule) implements MaterialRule {
      static final CodecHolder CODEC;

      BlockMaterialRule(BlockState resultState) {
         this(resultState, new SimpleBlockStateRule(resultState));
      }

      private BlockMaterialRule(BlockState arg, SimpleBlockStateRule arg2) {
         this.resultState = arg;
         this.rule = arg2;
      }

      public CodecHolder codec() {
         return CODEC;
      }

      public BlockStateRule apply(MaterialRuleContext arg) {
         return this.rule;
      }

      public BlockState resultState() {
         return this.resultState;
      }

      public SimpleBlockStateRule rule() {
         return this.rule;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }

      static {
         CODEC = CodecHolder.of(BlockState.CODEC.xmap(BlockMaterialRule::new, BlockMaterialRule::resultState).fieldOf("result_state"));
      }
   }

   static enum TerracottaBandsMaterialRule implements MaterialRule {
      INSTANCE;

      static final CodecHolder CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));

      public CodecHolder codec() {
         return CODEC;
      }

      public BlockStateRule apply(MaterialRuleContext arg) {
         SurfaceBuilder var10000 = arg.surfaceBuilder;
         Objects.requireNonNull(var10000);
         return var10000::getTerracottaBlock;
      }

      // $FF: synthetic method
      public Object apply(Object context) {
         return this.apply((MaterialRuleContext)context);
      }

      // $FF: synthetic method
      private static TerracottaBandsMaterialRule[] method_39063() {
         return new TerracottaBandsMaterialRule[]{INSTANCE};
      }
   }

   private static record SequenceBlockStateRule(List rules) implements BlockStateRule {
      SequenceBlockStateRule(List list) {
         this.rules = list;
      }

      @Nullable
      public BlockState tryApply(int i, int j, int k) {
         Iterator var4 = this.rules.iterator();

         BlockState lv2;
         do {
            if (!var4.hasNext()) {
               return null;
            }

            BlockStateRule lv = (BlockStateRule)var4.next();
            lv2 = lv.tryApply(i, j, k);
         } while(lv2 == null);

         return lv2;
      }

      public List rules() {
         return this.rules;
      }
   }

   private static record ConditionalBlockStateRule(BooleanSupplier condition, BlockStateRule followup) implements BlockStateRule {
      ConditionalBlockStateRule(BooleanSupplier arg, BlockStateRule arg2) {
         this.condition = arg;
         this.followup = arg2;
      }

      @Nullable
      public BlockState tryApply(int i, int j, int k) {
         return !this.condition.get() ? null : this.followup.tryApply(i, j, k);
      }

      public BooleanSupplier condition() {
         return this.condition;
      }

      public BlockStateRule followup() {
         return this.followup;
      }
   }

   private static record SimpleBlockStateRule(BlockState state) implements BlockStateRule {
      SimpleBlockStateRule(BlockState arg) {
         this.state = arg;
      }

      public BlockState tryApply(int i, int j, int k) {
         return this.state;
      }

      public BlockState state() {
         return this.state;
      }
   }

   protected interface BlockStateRule {
      @Nullable
      BlockState tryApply(int x, int y, int z);
   }

   static record InvertedBooleanSupplier(BooleanSupplier target) implements BooleanSupplier {
      InvertedBooleanSupplier(BooleanSupplier arg) {
         this.target = arg;
      }

      public boolean get() {
         return !this.target.get();
      }

      public BooleanSupplier target() {
         return this.target;
      }
   }

   private abstract static class FullLazyAbstractPredicate extends LazyAbstractPredicate {
      protected FullLazyAbstractPredicate(MaterialRuleContext arg) {
         super(arg);
      }

      protected long getCurrentUniqueValue() {
         return this.context.uniquePosValue;
      }
   }

   private abstract static class HorizontalLazyAbstractPredicate extends LazyAbstractPredicate {
      protected HorizontalLazyAbstractPredicate(MaterialRuleContext arg) {
         super(arg);
      }

      protected long getCurrentUniqueValue() {
         return this.context.uniqueHorizontalPosValue;
      }
   }

   private abstract static class LazyAbstractPredicate implements BooleanSupplier {
      protected final MaterialRuleContext context;
      private long uniqueValue;
      @Nullable
      Boolean result;

      protected LazyAbstractPredicate(MaterialRuleContext context) {
         this.context = context;
         this.uniqueValue = this.getCurrentUniqueValue() - 1L;
      }

      public boolean get() {
         long l = this.getCurrentUniqueValue();
         if (l == this.uniqueValue) {
            if (this.result == null) {
               throw new IllegalStateException("Update triggered but the result is null");
            } else {
               return this.result;
            }
         } else {
            this.uniqueValue = l;
            this.result = this.test();
            return this.result;
         }
      }

      protected abstract long getCurrentUniqueValue();

      protected abstract boolean test();
   }

   private interface BooleanSupplier {
      boolean get();
   }

   protected static final class MaterialRuleContext {
      private static final int field_36274 = 8;
      private static final int field_36275 = 4;
      private static final int field_36276 = 16;
      private static final int field_36277 = 15;
      final SurfaceBuilder surfaceBuilder;
      final BooleanSupplier biomeTemperaturePredicate = new BiomeTemperaturePredicate(this);
      final BooleanSupplier steepSlopePredicate = new SteepSlopePredicate(this);
      final BooleanSupplier negativeRunDepthPredicate = new NegativeRunDepthPredicate(this);
      final BooleanSupplier surfacePredicate = new SurfacePredicate();
      final NoiseConfig noiseConfig;
      final Chunk chunk;
      private final ChunkNoiseSampler chunkNoiseSampler;
      private final Function posToBiome;
      final HeightContext heightContext;
      private long field_36278 = Long.MAX_VALUE;
      private final int[] field_36279 = new int[4];
      long uniqueHorizontalPosValue = -9223372036854775807L;
      int blockX;
      int blockZ;
      int runDepth;
      private long field_35677;
      private double field_35678;
      private long field_35679;
      private int surfaceMinY;
      long uniquePosValue;
      final BlockPos.Mutable pos;
      Supplier biomeSupplier;
      int blockY;
      int fluidHeight;
      int stoneDepthBelow;
      int stoneDepthAbove;

      protected MaterialRuleContext(SurfaceBuilder surfaceBuilder, NoiseConfig noiseConfig, Chunk chunk, ChunkNoiseSampler chunkNoiseSampler, Function posToBiome, Registry arg5, HeightContext heightContext) {
         this.field_35677 = this.uniqueHorizontalPosValue - 1L;
         this.field_35679 = this.uniqueHorizontalPosValue - 1L;
         this.uniquePosValue = -9223372036854775807L;
         this.pos = new BlockPos.Mutable();
         this.surfaceBuilder = surfaceBuilder;
         this.noiseConfig = noiseConfig;
         this.chunk = chunk;
         this.chunkNoiseSampler = chunkNoiseSampler;
         this.posToBiome = posToBiome;
         this.heightContext = heightContext;
      }

      protected void initHorizontalContext(int blockX, int blockZ) {
         ++this.uniqueHorizontalPosValue;
         ++this.uniquePosValue;
         this.blockX = blockX;
         this.blockZ = blockZ;
         this.runDepth = this.surfaceBuilder.method_39552(blockX, blockZ);
      }

      protected void initVerticalContext(int stoneDepthAbove, int stoneDepthBelow, int fluidHeight, int blockX, int blockY, int blockZ) {
         ++this.uniquePosValue;
         this.biomeSupplier = Suppliers.memoize(() -> {
            return (RegistryEntry)this.posToBiome.apply(this.pos.set(blockX, blockY, blockZ));
         });
         this.blockY = blockY;
         this.fluidHeight = fluidHeight;
         this.stoneDepthBelow = stoneDepthBelow;
         this.stoneDepthAbove = stoneDepthAbove;
      }

      protected double method_39550() {
         if (this.field_35677 != this.uniqueHorizontalPosValue) {
            this.field_35677 = this.uniqueHorizontalPosValue;
            this.field_35678 = this.surfaceBuilder.method_39555(this.blockX, this.blockZ);
         }

         return this.field_35678;
      }

      private static int method_39903(int i) {
         return i >> 4;
      }

      private static int method_39904(int i) {
         return i << 4;
      }

      protected int method_39551() {
         if (this.field_35679 != this.uniqueHorizontalPosValue) {
            this.field_35679 = this.uniqueHorizontalPosValue;
            int i = method_39903(this.blockX);
            int j = method_39903(this.blockZ);
            long l = ChunkPos.toLong(i, j);
            if (this.field_36278 != l) {
               this.field_36278 = l;
               this.field_36279[0] = this.chunkNoiseSampler.estimateSurfaceHeight(method_39904(i), method_39904(j));
               this.field_36279[1] = this.chunkNoiseSampler.estimateSurfaceHeight(method_39904(i + 1), method_39904(j));
               this.field_36279[2] = this.chunkNoiseSampler.estimateSurfaceHeight(method_39904(i), method_39904(j + 1));
               this.field_36279[3] = this.chunkNoiseSampler.estimateSurfaceHeight(method_39904(i + 1), method_39904(j + 1));
            }

            int k = MathHelper.floor(MathHelper.lerp2((double)((float)(this.blockX & 15) / 16.0F), (double)((float)(this.blockZ & 15) / 16.0F), (double)this.field_36279[0], (double)this.field_36279[1], (double)this.field_36279[2], (double)this.field_36279[3]));
            this.surfaceMinY = k + this.runDepth - 8;
         }

         return this.surfaceMinY;
      }

      private static class BiomeTemperaturePredicate extends FullLazyAbstractPredicate {
         BiomeTemperaturePredicate(MaterialRuleContext arg) {
            super(arg);
         }

         protected boolean test() {
            return ((Biome)((RegistryEntry)this.context.biomeSupplier.get()).value()).isCold(this.context.pos.set(this.context.blockX, this.context.blockY, this.context.blockZ));
         }
      }

      private static class SteepSlopePredicate extends HorizontalLazyAbstractPredicate {
         SteepSlopePredicate(MaterialRuleContext arg) {
            super(arg);
         }

         protected boolean test() {
            int i = this.context.blockX & 15;
            int j = this.context.blockZ & 15;
            int k = Math.max(j - 1, 0);
            int l = Math.min(j + 1, 15);
            Chunk lv = this.context.chunk;
            int m = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, i, k);
            int n = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, i, l);
            if (n >= m + 4) {
               return true;
            } else {
               int o = Math.max(i - 1, 0);
               int p = Math.min(i + 1, 15);
               int q = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, o, j);
               int r = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, p, j);
               return q >= r + 4;
            }
         }
      }

      private static final class NegativeRunDepthPredicate extends HorizontalLazyAbstractPredicate {
         NegativeRunDepthPredicate(MaterialRuleContext arg) {
            super(arg);
         }

         protected boolean test() {
            return this.context.runDepth <= 0;
         }
      }

      private final class SurfacePredicate implements BooleanSupplier {
         SurfacePredicate() {
         }

         public boolean get() {
            return MaterialRuleContext.this.blockY >= MaterialRuleContext.this.method_39551();
         }
      }
   }
}
