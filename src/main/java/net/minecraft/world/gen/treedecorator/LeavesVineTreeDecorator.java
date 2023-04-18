package net.minecraft.world.gen.treedecorator;

import com.mojang.serialization.Codec;
import net.minecraft.block.VineBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class LeavesVineTreeDecorator extends TreeDecorator {
   public static final Codec CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(LeavesVineTreeDecorator::new, (treeDecorator) -> {
      return treeDecorator.probability;
   }).codec();
   private final float probability;

   protected TreeDecoratorType getType() {
      return TreeDecoratorType.LEAVE_VINE;
   }

   public LeavesVineTreeDecorator(float probability) {
      this.probability = probability;
   }

   public void generate(TreeDecorator.Generator generator) {
      Random lv = generator.getRandom();
      generator.getLeavesPositions().forEach((pos) -> {
         BlockPos lvx;
         if (lv.nextFloat() < this.probability) {
            lvx = pos.west();
            if (generator.isAir(lvx)) {
               placeVines(lvx, VineBlock.EAST, generator);
            }
         }

         if (lv.nextFloat() < this.probability) {
            lvx = pos.east();
            if (generator.isAir(lvx)) {
               placeVines(lvx, VineBlock.WEST, generator);
            }
         }

         if (lv.nextFloat() < this.probability) {
            lvx = pos.north();
            if (generator.isAir(lvx)) {
               placeVines(lvx, VineBlock.SOUTH, generator);
            }
         }

         if (lv.nextFloat() < this.probability) {
            lvx = pos.south();
            if (generator.isAir(lvx)) {
               placeVines(lvx, VineBlock.NORTH, generator);
            }
         }

      });
   }

   private static void placeVines(BlockPos pos, BooleanProperty faceProperty, TreeDecorator.Generator generator) {
      generator.replaceWithVine(pos, faceProperty);
      int i = 4;

      for(pos = pos.down(); generator.isAir(pos) && i > 0; --i) {
         generator.replaceWithVine(pos, faceProperty);
         pos = pos.down();
      }

   }
}
