package net.minecraft.block;

import java.util.Iterator;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class NetherrackBlock extends Block implements Fertilizable {
   public NetherrackBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      if (!world.getBlockState(pos.up()).isTransparent(world, pos)) {
         return false;
      } else {
         Iterator var5 = BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 1, 1)).iterator();

         BlockPos lv;
         do {
            if (!var5.hasNext()) {
               return false;
            }

            lv = (BlockPos)var5.next();
         } while(!world.getBlockState(lv).isIn(BlockTags.NYLIUM));

         return true;
      }
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      boolean bl = false;
      boolean bl2 = false;
      Iterator var7 = BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 1, 1)).iterator();

      while(var7.hasNext()) {
         BlockPos lv = (BlockPos)var7.next();
         BlockState lv2 = world.getBlockState(lv);
         if (lv2.isOf(Blocks.WARPED_NYLIUM)) {
            bl2 = true;
         }

         if (lv2.isOf(Blocks.CRIMSON_NYLIUM)) {
            bl = true;
         }

         if (bl2 && bl) {
            break;
         }
      }

      if (bl2 && bl) {
         world.setBlockState(pos, random.nextBoolean() ? Blocks.WARPED_NYLIUM.getDefaultState() : Blocks.CRIMSON_NYLIUM.getDefaultState(), Block.NOTIFY_ALL);
      } else if (bl2) {
         world.setBlockState(pos, Blocks.WARPED_NYLIUM.getDefaultState(), Block.NOTIFY_ALL);
      } else if (bl) {
         world.setBlockState(pos, Blocks.CRIMSON_NYLIUM.getDefaultState(), Block.NOTIFY_ALL);
      }

   }
}
