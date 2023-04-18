package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class DesertWellFeature extends Feature {
   private static final BlockStatePredicate CAN_GENERATE;
   private final BlockState sand;
   private final BlockState slab;
   private final BlockState wall;
   private final BlockState fluidInside;

   public DesertWellFeature(Codec codec) {
      super(codec);
      this.sand = Blocks.SAND.getDefaultState();
      this.slab = Blocks.SANDSTONE_SLAB.getDefaultState();
      this.wall = Blocks.SANDSTONE.getDefaultState();
      this.fluidInside = Blocks.WATER.getDefaultState();
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();

      for(lv2 = lv2.up(); lv.isAir(lv2) && lv2.getY() > lv.getBottomY() + 2; lv2 = lv2.down()) {
      }

      if (!CAN_GENERATE.test(lv.getBlockState(lv2))) {
         return false;
      } else {
         int i;
         int j;
         for(i = -2; i <= 2; ++i) {
            for(j = -2; j <= 2; ++j) {
               if (lv.isAir(lv2.add(i, -1, j)) && lv.isAir(lv2.add(i, -2, j))) {
                  return false;
               }
            }
         }

         int k;
         for(i = -2; i <= 0; ++i) {
            for(j = -2; j <= 2; ++j) {
               for(k = -2; k <= 2; ++k) {
                  lv.setBlockState(lv2.add(j, i, k), this.wall, Block.NOTIFY_LISTENERS);
               }
            }
         }

         lv.setBlockState(lv2, this.fluidInside, Block.NOTIFY_LISTENERS);
         Iterator var8 = Direction.Type.HORIZONTAL.iterator();

         while(var8.hasNext()) {
            Direction lv3 = (Direction)var8.next();
            lv.setBlockState(lv2.offset(lv3), this.fluidInside, Block.NOTIFY_LISTENERS);
         }

         BlockPos lv4 = lv2.down();
         lv.setBlockState(lv4, this.sand, Block.NOTIFY_LISTENERS);
         Iterator var11 = Direction.Type.HORIZONTAL.iterator();

         while(var11.hasNext()) {
            Direction lv5 = (Direction)var11.next();
            lv.setBlockState(lv4.offset(lv5), this.sand, Block.NOTIFY_LISTENERS);
         }

         for(j = -2; j <= 2; ++j) {
            for(k = -2; k <= 2; ++k) {
               if (j == -2 || j == 2 || k == -2 || k == 2) {
                  lv.setBlockState(lv2.add(j, 1, k), this.wall, Block.NOTIFY_LISTENERS);
               }
            }
         }

         lv.setBlockState(lv2.add(2, 1, 0), this.slab, Block.NOTIFY_LISTENERS);
         lv.setBlockState(lv2.add(-2, 1, 0), this.slab, Block.NOTIFY_LISTENERS);
         lv.setBlockState(lv2.add(0, 1, 2), this.slab, Block.NOTIFY_LISTENERS);
         lv.setBlockState(lv2.add(0, 1, -2), this.slab, Block.NOTIFY_LISTENERS);

         for(j = -1; j <= 1; ++j) {
            for(k = -1; k <= 1; ++k) {
               if (j == 0 && k == 0) {
                  lv.setBlockState(lv2.add(j, 4, k), this.wall, Block.NOTIFY_LISTENERS);
               } else {
                  lv.setBlockState(lv2.add(j, 4, k), this.slab, Block.NOTIFY_LISTENERS);
               }
            }
         }

         for(j = 1; j <= 3; ++j) {
            lv.setBlockState(lv2.add(-1, j, -1), this.wall, Block.NOTIFY_LISTENERS);
            lv.setBlockState(lv2.add(-1, j, 1), this.wall, Block.NOTIFY_LISTENERS);
            lv.setBlockState(lv2.add(1, j, -1), this.wall, Block.NOTIFY_LISTENERS);
            lv.setBlockState(lv2.add(1, j, 1), this.wall, Block.NOTIFY_LISTENERS);
         }

         List list = List.of(lv2, lv2.east(), lv2.south(), lv2.west(), lv2.north());
         Random lv7 = context.getRandom();
         generateSuspiciousSand(lv, ((BlockPos)Util.getRandom(list, lv7)).down(1));
         generateSuspiciousSand(lv, ((BlockPos)Util.getRandom(list, lv7)).down(2));
         return true;
      }
   }

   private static void generateSuspiciousSand(StructureWorldAccess world, BlockPos pos) {
      world.setBlockState(pos, Blocks.SUSPICIOUS_SAND.getDefaultState(), Block.NOTIFY_ALL);
      world.getBlockEntity(pos, BlockEntityType.BRUSHABLE_BLOCK).ifPresent((blockEntity) -> {
         blockEntity.setLootTable(LootTables.DESERT_WELL_ARCHAEOLOGY, pos.asLong());
      });
   }

   static {
      CAN_GENERATE = BlockStatePredicate.forBlock(Blocks.SAND);
   }
}
