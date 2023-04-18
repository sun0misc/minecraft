package net.minecraft.world.gen.treedecorator;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class CocoaBeansTreeDecorator extends TreeDecorator {
   public static final Codec CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(CocoaBeansTreeDecorator::new, (decorator) -> {
      return decorator.probability;
   }).codec();
   private final float probability;

   public CocoaBeansTreeDecorator(float probability) {
      this.probability = probability;
   }

   protected TreeDecoratorType getType() {
      return TreeDecoratorType.COCOA;
   }

   public void generate(TreeDecorator.Generator generator) {
      Random lv = generator.getRandom();
      if (!(lv.nextFloat() >= this.probability)) {
         List list = generator.getLogPositions();
         int i = ((BlockPos)list.get(0)).getY();
         list.stream().filter((pos) -> {
            return pos.getY() - i <= 2;
         }).forEach((pos) -> {
            Iterator var3 = Direction.Type.HORIZONTAL.iterator();

            while(var3.hasNext()) {
               Direction lvx = (Direction)var3.next();
               if (lv.nextFloat() <= 0.25F) {
                  Direction lv2 = lvx.getOpposite();
                  BlockPos lv3 = pos.add(lv2.getOffsetX(), 0, lv2.getOffsetZ());
                  if (generator.isAir(lv3)) {
                     generator.replace(lv3, (BlockState)((BlockState)Blocks.COCOA.getDefaultState().with(CocoaBlock.AGE, lv.nextInt(3))).with(CocoaBlock.FACING, lvx));
                  }
               }
            }

         });
      }
   }
}
