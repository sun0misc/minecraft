package net.minecraft.world.gen.treedecorator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class AttachedToLeavesTreeDecorator extends TreeDecorator {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((treeDecorator) -> {
         return treeDecorator.probability;
      }), Codec.intRange(0, 16).fieldOf("exclusion_radius_xz").forGetter((treeDecorator) -> {
         return treeDecorator.exclusionRadiusXZ;
      }), Codec.intRange(0, 16).fieldOf("exclusion_radius_y").forGetter((treeDecorator) -> {
         return treeDecorator.exclusionRadiusY;
      }), BlockStateProvider.TYPE_CODEC.fieldOf("block_provider").forGetter((treeDecorator) -> {
         return treeDecorator.blockProvider;
      }), Codec.intRange(1, 16).fieldOf("required_empty_blocks").forGetter((treeDecorator) -> {
         return treeDecorator.requiredEmptyBlocks;
      }), Codecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter((treeDecorator) -> {
         return treeDecorator.directions;
      })).apply(instance, AttachedToLeavesTreeDecorator::new);
   });
   protected final float probability;
   protected final int exclusionRadiusXZ;
   protected final int exclusionRadiusY;
   protected final BlockStateProvider blockProvider;
   protected final int requiredEmptyBlocks;
   protected final List directions;

   public AttachedToLeavesTreeDecorator(float probability, int exclusionRadiusXZ, int exclusionRadiusY, BlockStateProvider blockProvider, int requiredEmptyBlocks, List directions) {
      this.probability = probability;
      this.exclusionRadiusXZ = exclusionRadiusXZ;
      this.exclusionRadiusY = exclusionRadiusY;
      this.blockProvider = blockProvider;
      this.requiredEmptyBlocks = requiredEmptyBlocks;
      this.directions = directions;
   }

   public void generate(TreeDecorator.Generator generator) {
      Set set = new HashSet();
      Random lv = generator.getRandom();
      Iterator var4 = Util.copyShuffled(generator.getLeavesPositions(), lv).iterator();

      while(true) {
         BlockPos lv2;
         Direction lv3;
         BlockPos lv4;
         do {
            do {
               do {
                  if (!var4.hasNext()) {
                     return;
                  }

                  lv2 = (BlockPos)var4.next();
                  lv3 = (Direction)Util.getRandom(this.directions, lv);
                  lv4 = lv2.offset(lv3);
               } while(set.contains(lv4));
            } while(!(lv.nextFloat() < this.probability));
         } while(!this.meetsRequiredEmptyBlocks(generator, lv2, lv3));

         BlockPos lv5 = lv4.add(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
         BlockPos lv6 = lv4.add(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);
         Iterator var10 = BlockPos.iterate(lv5, lv6).iterator();

         while(var10.hasNext()) {
            BlockPos lv7 = (BlockPos)var10.next();
            set.add(lv7.toImmutable());
         }

         generator.replace(lv4, this.blockProvider.get(lv, lv4));
      }
   }

   private boolean meetsRequiredEmptyBlocks(TreeDecorator.Generator generator, BlockPos pos, Direction direction) {
      for(int i = 1; i <= this.requiredEmptyBlocks; ++i) {
         BlockPos lv = pos.offset(direction, i);
         if (!generator.isAir(lv)) {
            return false;
         }
      }

      return true;
   }

   protected TreeDecoratorType getType() {
      return TreeDecoratorType.ATTACHED_TO_LEAVES;
   }
}
