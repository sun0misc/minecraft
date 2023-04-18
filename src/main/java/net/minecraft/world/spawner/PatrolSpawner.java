package net.minecraft.world.spawner;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;

public class PatrolSpawner implements Spawner {
   private int cooldown;

   public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
      if (!spawnMonsters) {
         return 0;
      } else if (!world.getGameRules().getBoolean(GameRules.DO_PATROL_SPAWNING)) {
         return 0;
      } else {
         Random lv = world.random;
         --this.cooldown;
         if (this.cooldown > 0) {
            return 0;
         } else {
            this.cooldown += 12000 + lv.nextInt(1200);
            long l = world.getTimeOfDay() / 24000L;
            if (l >= 5L && world.isDay()) {
               if (lv.nextInt(5) != 0) {
                  return 0;
               } else {
                  int i = world.getPlayers().size();
                  if (i < 1) {
                     return 0;
                  } else {
                     PlayerEntity lv2 = (PlayerEntity)world.getPlayers().get(lv.nextInt(i));
                     if (lv2.isSpectator()) {
                        return 0;
                     } else if (world.isNearOccupiedPointOfInterest(lv2.getBlockPos(), 2)) {
                        return 0;
                     } else {
                        int j = (24 + lv.nextInt(24)) * (lv.nextBoolean() ? -1 : 1);
                        int k = (24 + lv.nextInt(24)) * (lv.nextBoolean() ? -1 : 1);
                        BlockPos.Mutable lv3 = lv2.getBlockPos().mutableCopy().move(j, 0, k);
                        int m = true;
                        if (!world.isRegionLoaded(lv3.getX() - 10, lv3.getZ() - 10, lv3.getX() + 10, lv3.getZ() + 10)) {
                           return 0;
                        } else {
                           RegistryEntry lv4 = world.getBiome(lv3);
                           if (lv4.isIn(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
                              return 0;
                           } else {
                              int n = 0;
                              int o = (int)Math.ceil((double)world.getLocalDifficulty(lv3).getLocalDifficulty()) + 1;

                              for(int p = 0; p < o; ++p) {
                                 ++n;
                                 lv3.setY(world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv3).getY());
                                 if (p == 0) {
                                    if (!this.spawnPillager(world, lv3, lv, true)) {
                                       break;
                                    }
                                 } else {
                                    this.spawnPillager(world, lv3, lv, false);
                                 }

                                 lv3.setX(lv3.getX() + lv.nextInt(5) - lv.nextInt(5));
                                 lv3.setZ(lv3.getZ() + lv.nextInt(5) - lv.nextInt(5));
                              }

                              return n;
                           }
                        }
                     }
                  }
               }
            } else {
               return 0;
            }
         }
      }
   }

   private boolean spawnPillager(ServerWorld world, BlockPos pos, Random random, boolean captain) {
      BlockState lv = world.getBlockState(pos);
      if (!SpawnHelper.isClearForSpawn(world, pos, lv, lv.getFluidState(), EntityType.PILLAGER)) {
         return false;
      } else if (!PatrolEntity.canSpawn(EntityType.PILLAGER, world, SpawnReason.PATROL, pos, random)) {
         return false;
      } else {
         PatrolEntity lv2 = (PatrolEntity)EntityType.PILLAGER.create(world);
         if (lv2 != null) {
            if (captain) {
               lv2.setPatrolLeader(true);
               lv2.setRandomPatrolTarget();
            }

            lv2.setPosition((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            lv2.initialize(world, world.getLocalDifficulty(pos), SpawnReason.PATROL, (EntityData)null, (NbtCompound)null);
            world.spawnEntityAndPassengers(lv2);
            return true;
         } else {
            return false;
         }
      }
   }
}
