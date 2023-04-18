package net.minecraft.world.gen.treedecorator;

import com.mojang.serialization.Codec;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class TrunkVineTreeDecorator extends TreeDecorator {
   public static final Codec CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final TrunkVineTreeDecorator INSTANCE = new TrunkVineTreeDecorator();

   protected TreeDecoratorType getType() {
      return TreeDecoratorType.TRUNK_VINE;
   }

   public void generate(TreeDecorator.Generator generator) {
      Random lv = generator.getRandom();
      generator.getLogPositions().forEach((pos) -> {
         BlockPos lvx;
         if (lv.nextInt(3) > 0) {
            lvx = pos.west();
            if (generator.isAir(lvx)) {
               generator.replaceWithVine(lvx, VineBlock.EAST);
            }
         }

         if (lv.nextInt(3) > 0) {
            lvx = pos.east();
            if (generator.isAir(lvx)) {
               generator.replaceWithVine(lvx, VineBlock.WEST);
            }
         }

         if (lv.nextInt(3) > 0) {
            lvx = pos.north();
            if (generator.isAir(lvx)) {
               generator.replaceWithVine(lvx, VineBlock.SOUTH);
            }
         }

         if (lv.nextInt(3) > 0) {
            lvx = pos.south();
            if (generator.isAir(lvx)) {
               generator.replaceWithVine(lvx, VineBlock.NORTH);
            }
         }

      });
   }
}
