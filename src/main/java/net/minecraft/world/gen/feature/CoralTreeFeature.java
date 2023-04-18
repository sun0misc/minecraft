package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

public class CoralTreeFeature extends CoralFeature {
   public CoralTreeFeature(Codec codec) {
      super(codec);
   }

   protected boolean generateCoral(WorldAccess world, Random random, BlockPos pos, BlockState state) {
      BlockPos.Mutable lv = pos.mutableCopy();
      int i = random.nextInt(3) + 1;

      for(int j = 0; j < i; ++j) {
         if (!this.generateCoralPiece(world, random, lv, state)) {
            return true;
         }

         lv.move(Direction.UP);
      }

      BlockPos lv2 = lv.toImmutable();
      int k = random.nextInt(3) + 2;
      List list = Direction.Type.HORIZONTAL.getShuffled(random);
      List list2 = list.subList(0, k);
      Iterator var11 = list2.iterator();

      while(var11.hasNext()) {
         Direction lv3 = (Direction)var11.next();
         lv.set(lv2);
         lv.move(lv3);
         int l = random.nextInt(5) + 2;
         int m = 0;

         for(int n = 0; n < l && this.generateCoralPiece(world, random, lv, state); ++n) {
            ++m;
            lv.move(Direction.UP);
            if (n == 0 || m >= 2 && random.nextFloat() < 0.25F) {
               lv.move(lv3);
               m = 0;
            }
         }
      }

      return true;
   }
}
