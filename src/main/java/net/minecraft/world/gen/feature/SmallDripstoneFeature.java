package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.util.DripstoneHelper;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SmallDripstoneFeature extends Feature {
   public SmallDripstoneFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      WorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      Random lv3 = context.getRandom();
      SmallDripstoneFeatureConfig lv4 = (SmallDripstoneFeatureConfig)context.getConfig();
      Optional optional = getDirection(lv, lv2, lv3);
      if (optional.isEmpty()) {
         return false;
      } else {
         BlockPos lv5 = lv2.offset(((Direction)optional.get()).getOpposite());
         generateDripstoneBlocks(lv, lv3, lv5, lv4);
         int i = lv3.nextFloat() < lv4.chanceOfTallerDripstone && DripstoneHelper.canGenerate(lv.getBlockState(lv2.offset((Direction)optional.get()))) ? 2 : 1;
         DripstoneHelper.generatePointedDripstone(lv, lv2, (Direction)optional.get(), i, false);
         return true;
      }
   }

   private static Optional getDirection(WorldAccess world, BlockPos pos, Random random) {
      boolean bl = DripstoneHelper.canReplace(world.getBlockState(pos.up()));
      boolean bl2 = DripstoneHelper.canReplace(world.getBlockState(pos.down()));
      if (bl && bl2) {
         return Optional.of(random.nextBoolean() ? Direction.DOWN : Direction.UP);
      } else if (bl) {
         return Optional.of(Direction.DOWN);
      } else {
         return bl2 ? Optional.of(Direction.UP) : Optional.empty();
      }
   }

   private static void generateDripstoneBlocks(WorldAccess world, Random random, BlockPos pos, SmallDripstoneFeatureConfig config) {
      DripstoneHelper.generateDripstoneBlock(world, pos);
      Iterator var4 = Direction.Type.HORIZONTAL.iterator();

      while(var4.hasNext()) {
         Direction lv = (Direction)var4.next();
         if (!(random.nextFloat() > config.chanceOfDirectionalSpread)) {
            BlockPos lv2 = pos.offset(lv);
            DripstoneHelper.generateDripstoneBlock(world, lv2);
            if (!(random.nextFloat() > config.chanceOfSpreadRadius2)) {
               BlockPos lv3 = lv2.offset(Direction.random(random));
               DripstoneHelper.generateDripstoneBlock(world, lv3);
               if (!(random.nextFloat() > config.chanceOfSpreadRadius3)) {
                  BlockPos lv4 = lv3.offset(Direction.random(random));
                  DripstoneHelper.generateDripstoneBlock(world, lv4);
               }
            }
         }
      }

   }
}
