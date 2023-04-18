package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.WorldEvents;

public class FarmerWorkTask extends VillagerWorkTask {
   private static final List COMPOSTABLES;

   protected void performAdditionalWork(ServerWorld world, VillagerEntity entity) {
      Optional optional = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
      if (optional.isPresent()) {
         GlobalPos lv = (GlobalPos)optional.get();
         BlockState lv2 = world.getBlockState(lv.getPos());
         if (lv2.isOf(Blocks.COMPOSTER)) {
            this.craftAndDropBread(entity);
            this.compostSeeds(world, entity, lv, lv2);
         }

      }
   }

   private void compostSeeds(ServerWorld world, VillagerEntity entity, GlobalPos pos, BlockState composterState) {
      BlockPos lv = pos.getPos();
      if ((Integer)composterState.get(ComposterBlock.LEVEL) == 8) {
         composterState = ComposterBlock.emptyFullComposter(entity, composterState, world, lv);
      }

      int i = 20;
      int j = true;
      int[] is = new int[COMPOSTABLES.size()];
      SimpleInventory lv2 = entity.getInventory();
      int k = lv2.size();
      BlockState lv3 = composterState;

      for(int l = k - 1; l >= 0 && i > 0; --l) {
         ItemStack lv4 = lv2.getStack(l);
         int m = COMPOSTABLES.indexOf(lv4.getItem());
         if (m != -1) {
            int n = lv4.getCount();
            int o = is[m] + n;
            is[m] = o;
            int p = Math.min(Math.min(o - 10, i), n);
            if (p > 0) {
               i -= p;

               for(int q = 0; q < p; ++q) {
                  lv3 = ComposterBlock.compost(entity, lv3, world, lv4, lv);
                  if ((Integer)lv3.get(ComposterBlock.LEVEL) == 7) {
                     this.syncComposterEvent(world, composterState, lv, lv3);
                     return;
                  }
               }
            }
         }
      }

      this.syncComposterEvent(world, composterState, lv, lv3);
   }

   private void syncComposterEvent(ServerWorld world, BlockState oldState, BlockPos pos, BlockState newState) {
      world.syncWorldEvent(WorldEvents.COMPOSTER_USED, pos, newState != oldState ? 1 : 0);
   }

   private void craftAndDropBread(VillagerEntity entity) {
      SimpleInventory lv = entity.getInventory();
      if (lv.count(Items.BREAD) <= 36) {
         int i = lv.count(Items.WHEAT);
         int j = true;
         int k = true;
         int l = Math.min(3, i / 3);
         if (l != 0) {
            int m = l * 3;
            lv.removeItem(Items.WHEAT, m);
            ItemStack lv2 = lv.addStack(new ItemStack(Items.BREAD, l));
            if (!lv2.isEmpty()) {
               entity.dropStack(lv2, 0.5F);
            }

         }
      }
   }

   static {
      COMPOSTABLES = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);
   }
}
