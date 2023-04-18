package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

public class CoralClawFeature extends CoralFeature {
   public CoralClawFeature(Codec codec) {
      super(codec);
   }

   protected boolean generateCoral(WorldAccess world, Random random, BlockPos pos, BlockState state) {
      if (!this.generateCoralPiece(world, random, pos, state)) {
         return false;
      } else {
         Direction lv = Direction.Type.HORIZONTAL.random(random);
         int i = random.nextInt(2) + 2;
         List list = Util.copyShuffled(Stream.of(lv, lv.rotateYClockwise(), lv.rotateYCounterclockwise()), random);
         List list2 = list.subList(0, i);
         Iterator var9 = list2.iterator();

         while(var9.hasNext()) {
            Direction lv2 = (Direction)var9.next();
            BlockPos.Mutable lv3 = pos.mutableCopy();
            int j = random.nextInt(2) + 1;
            lv3.move(lv2);
            int k;
            Direction lv4;
            if (lv2 == lv) {
               lv4 = lv;
               k = random.nextInt(3) + 2;
            } else {
               lv3.move(Direction.UP);
               Direction[] lvs = new Direction[]{lv2, Direction.UP};
               lv4 = (Direction)Util.getRandom((Object[])lvs, random);
               k = random.nextInt(3) + 3;
            }

            int l;
            for(l = 0; l < j && this.generateCoralPiece(world, random, lv3, state); ++l) {
               lv3.move(lv4);
            }

            lv3.move(lv4.getOpposite());
            lv3.move(Direction.UP);

            for(l = 0; l < k; ++l) {
               lv3.move(lv);
               if (!this.generateCoralPiece(world, random, lv3, state)) {
                  break;
               }

               if (random.nextFloat() < 0.25F) {
                  lv3.move(Direction.UP);
               }
            }
         }

         return true;
      }
   }
}
