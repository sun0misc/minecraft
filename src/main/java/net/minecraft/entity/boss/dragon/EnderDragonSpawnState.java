package net.minecraft.entity.boss.dragon;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public enum EnderDragonSpawnState {
   START {
      public void run(ServerWorld world, EnderDragonFight fight, List crystals, int tick, BlockPos pos) {
         BlockPos lv = new BlockPos(0, 128, 0);
         Iterator var7 = crystals.iterator();

         while(var7.hasNext()) {
            EndCrystalEntity lv2 = (EndCrystalEntity)var7.next();
            lv2.setBeamTarget(lv);
         }

         fight.setSpawnState(PREPARING_TO_SUMMON_PILLARS);
      }
   },
   PREPARING_TO_SUMMON_PILLARS {
      public void run(ServerWorld world, EnderDragonFight fight, List crystals, int tick, BlockPos pos) {
         if (tick < 100) {
            if (tick == 0 || tick == 50 || tick == 51 || tick == 52 || tick >= 95) {
               world.syncWorldEvent(WorldEvents.ENDER_DRAGON_RESURRECTED, new BlockPos(0, 128, 0), 0);
            }
         } else {
            fight.setSpawnState(SUMMONING_PILLARS);
         }

      }
   },
   SUMMONING_PILLARS {
      public void run(ServerWorld world, EnderDragonFight fight, List crystals, int tick, BlockPos pos) {
         int j = true;
         boolean bl = tick % 40 == 0;
         boolean bl2 = tick % 40 == 39;
         if (bl || bl2) {
            List list2 = EndSpikeFeature.getSpikes(world);
            int k = tick / 40;
            if (k < list2.size()) {
               EndSpikeFeature.Spike lv = (EndSpikeFeature.Spike)list2.get(k);
               if (bl) {
                  Iterator var12 = crystals.iterator();

                  while(var12.hasNext()) {
                     EndCrystalEntity lv2 = (EndCrystalEntity)var12.next();
                     lv2.setBeamTarget(new BlockPos(lv.getCenterX(), lv.getHeight() + 1, lv.getCenterZ()));
                  }
               } else {
                  int l = true;
                  Iterator var16 = BlockPos.iterate(new BlockPos(lv.getCenterX() - 10, lv.getHeight() - 10, lv.getCenterZ() - 10), new BlockPos(lv.getCenterX() + 10, lv.getHeight() + 10, lv.getCenterZ() + 10)).iterator();

                  while(var16.hasNext()) {
                     BlockPos lv3 = (BlockPos)var16.next();
                     world.removeBlock(lv3, false);
                  }

                  world.createExplosion((Entity)null, (double)((float)lv.getCenterX() + 0.5F), (double)lv.getHeight(), (double)((float)lv.getCenterZ() + 0.5F), 5.0F, World.ExplosionSourceType.BLOCK);
                  EndSpikeFeatureConfig lv4 = new EndSpikeFeatureConfig(true, ImmutableList.of(lv), new BlockPos(0, 128, 0));
                  Feature.END_SPIKE.generateIfValid(lv4, world, world.getChunkManager().getChunkGenerator(), Random.create(), new BlockPos(lv.getCenterX(), 45, lv.getCenterZ()));
               }
            } else if (bl) {
               fight.setSpawnState(SUMMONING_DRAGON);
            }
         }

      }
   },
   SUMMONING_DRAGON {
      public void run(ServerWorld world, EnderDragonFight fight, List crystals, int tick, BlockPos pos) {
         Iterator var6;
         EndCrystalEntity lv;
         if (tick >= 100) {
            fight.setSpawnState(END);
            fight.resetEndCrystals();
            var6 = crystals.iterator();

            while(var6.hasNext()) {
               lv = (EndCrystalEntity)var6.next();
               lv.setBeamTarget((BlockPos)null);
               world.createExplosion(lv, lv.getX(), lv.getY(), lv.getZ(), 6.0F, World.ExplosionSourceType.NONE);
               lv.discard();
            }
         } else if (tick >= 80) {
            world.syncWorldEvent(WorldEvents.ENDER_DRAGON_RESURRECTED, new BlockPos(0, 128, 0), 0);
         } else if (tick == 0) {
            var6 = crystals.iterator();

            while(var6.hasNext()) {
               lv = (EndCrystalEntity)var6.next();
               lv.setBeamTarget(new BlockPos(0, 128, 0));
            }
         } else if (tick < 5) {
            world.syncWorldEvent(WorldEvents.ENDER_DRAGON_RESURRECTED, new BlockPos(0, 128, 0), 0);
         }

      }
   },
   END {
      public void run(ServerWorld world, EnderDragonFight fight, List crystals, int tick, BlockPos pos) {
      }
   };

   public abstract void run(ServerWorld world, EnderDragonFight fight, List crystals, int tick, BlockPos pos);

   // $FF: synthetic method
   private static EnderDragonSpawnState[] method_36745() {
      return new EnderDragonSpawnState[]{START, PREPARING_TO_SUMMON_PILLARS, SUMMONING_PILLARS, SUMMONING_DRAGON, END};
   }
}
