package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record BlockColumnFeatureConfig(List layers, Direction direction, BlockPredicate allowedPlacement, boolean prioritizeTip) implements FeatureConfig {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockColumnFeatureConfig.Layer.CODEC.listOf().fieldOf("layers").forGetter(BlockColumnFeatureConfig::layers), Direction.CODEC.fieldOf("direction").forGetter(BlockColumnFeatureConfig::direction), BlockPredicate.BASE_CODEC.fieldOf("allowed_placement").forGetter(BlockColumnFeatureConfig::allowedPlacement), Codec.BOOL.fieldOf("prioritize_tip").forGetter(BlockColumnFeatureConfig::prioritizeTip)).apply(instance, BlockColumnFeatureConfig::new);
   });

   public BlockColumnFeatureConfig(List list, Direction arg, BlockPredicate arg2, boolean bl) {
      this.layers = list;
      this.direction = arg;
      this.allowedPlacement = arg2;
      this.prioritizeTip = bl;
   }

   public static Layer createLayer(IntProvider height, BlockStateProvider state) {
      return new Layer(height, state);
   }

   public static BlockColumnFeatureConfig create(IntProvider height, BlockStateProvider state) {
      return new BlockColumnFeatureConfig(List.of(createLayer(height, state)), Direction.UP, BlockPredicate.IS_AIR, false);
   }

   public List layers() {
      return this.layers;
   }

   public Direction direction() {
      return this.direction;
   }

   public BlockPredicate allowedPlacement() {
      return this.allowedPlacement;
   }

   public boolean prioritizeTip() {
      return this.prioritizeTip;
   }

   public static record Layer(IntProvider height, BlockStateProvider state) {
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(IntProvider.NON_NEGATIVE_CODEC.fieldOf("height").forGetter(Layer::height), BlockStateProvider.TYPE_CODEC.fieldOf("provider").forGetter(Layer::state)).apply(instance, Layer::new);
      });

      public Layer(IntProvider arg, BlockStateProvider arg2) {
         this.height = arg;
         this.state = arg2;
      }

      public IntProvider height() {
         return this.height;
      }

      public BlockStateProvider state() {
         return this.state;
      }
   }
}
