package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.block.SculkSpreadable;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SculkPatchFeature extends Feature {
   public SculkPatchFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      if (!this.canGenerate(lv, lv2)) {
         return false;
      } else {
         SculkPatchFeatureConfig lv3 = (SculkPatchFeatureConfig)context.getConfig();
         Random lv4 = context.getRandom();
         SculkSpreadManager lv5 = SculkSpreadManager.createWorldGen();
         int i = lv3.spreadRounds() + lv3.growthRounds();

         int k;
         int l;
         for(int j = 0; j < i; ++j) {
            for(k = 0; k < lv3.chargeCount(); ++k) {
               lv5.spread(lv2, lv3.amountPerCharge());
            }

            boolean bl = j < lv3.spreadRounds();

            for(l = 0; l < lv3.spreadAttempts(); ++l) {
               lv5.tick(lv, lv2, lv4, bl);
            }

            lv5.clearCursors();
         }

         BlockPos lv6 = lv2.down();
         if (lv4.nextFloat() <= lv3.catalystChance() && lv.getBlockState(lv6).isFullCube(lv, lv6)) {
            lv.setBlockState(lv2, Blocks.SCULK_CATALYST.getDefaultState(), Block.NOTIFY_ALL);
         }

         k = lv3.extraRareGrowths().get(lv4);

         for(l = 0; l < k; ++l) {
            BlockPos lv7 = lv2.add(lv4.nextInt(5) - 2, 0, lv4.nextInt(5) - 2);
            if (lv.getBlockState(lv7).isAir() && lv.getBlockState(lv7.down()).isSideSolidFullSquare(lv, lv7.down(), Direction.UP)) {
               lv.setBlockState(lv7, (BlockState)Blocks.SCULK_SHRIEKER.getDefaultState().with(SculkShriekerBlock.CAN_SUMMON, true), Block.NOTIFY_ALL);
            }
         }

         return true;
      }
   }

   private boolean canGenerate(WorldAccess world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      if (lv.getBlock() instanceof SculkSpreadable) {
         return true;
      } else if (!lv.isAir() && (!lv.isOf(Blocks.WATER) || !lv.getFluidState().isStill())) {
         return false;
      } else {
         Stream var10000 = Direction.stream();
         Objects.requireNonNull(pos);
         return var10000.map(pos::offset).anyMatch((pos2) -> {
            return world.getBlockState(pos2).isFullCube(world, pos2);
         });
      }
   }
}
