package net.minecraft.world.spawner;

import java.util.List;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;

public class CatSpawner implements Spawner {
   private static final int SPAWN_INTERVAL = 1200;
   private int cooldown;

   public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
      if (spawnAnimals && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
         --this.cooldown;
         if (this.cooldown > 0) {
            return 0;
         } else {
            this.cooldown = 1200;
            PlayerEntity lv = world.getRandomAlivePlayer();
            if (lv == null) {
               return 0;
            } else {
               Random lv2 = world.random;
               int i = (8 + lv2.nextInt(24)) * (lv2.nextBoolean() ? -1 : 1);
               int j = (8 + lv2.nextInt(24)) * (lv2.nextBoolean() ? -1 : 1);
               BlockPos lv3 = lv.getBlockPos().add(i, 0, j);
               int k = true;
               if (!world.isRegionLoaded(lv3.getX() - 10, lv3.getZ() - 10, lv3.getX() + 10, lv3.getZ() + 10)) {
                  return 0;
               } else {
                  if (SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, world, lv3, EntityType.CAT)) {
                     if (world.isNearOccupiedPointOfInterest(lv3, 2)) {
                        return this.spawnInHouse(world, lv3);
                     }

                     if (world.getStructureAccessor().getStructureContaining(lv3, StructureTags.CATS_SPAWN_IN).hasChildren()) {
                        return this.spawnInSwampHut(world, lv3);
                     }
                  }

                  return 0;
               }
            }
         }
      } else {
         return 0;
      }
   }

   private int spawnInHouse(ServerWorld world, BlockPos pos) {
      int i = true;
      if (world.getPointOfInterestStorage().count((entry) -> {
         return entry.matchesKey(PointOfInterestTypes.HOME);
      }, pos, 48, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED) > 4L) {
         List list = world.getNonSpectatingEntities(CatEntity.class, (new Box(pos)).expand(48.0, 8.0, 48.0));
         if (list.size() < 5) {
            return this.spawn(pos, world);
         }
      }

      return 0;
   }

   private int spawnInSwampHut(ServerWorld world, BlockPos pos) {
      int i = true;
      List list = world.getNonSpectatingEntities(CatEntity.class, (new Box(pos)).expand(16.0, 8.0, 16.0));
      return list.size() < 1 ? this.spawn(pos, world) : 0;
   }

   private int spawn(BlockPos pos, ServerWorld world) {
      CatEntity lv = (CatEntity)EntityType.CAT.create(world);
      if (lv == null) {
         return 0;
      } else {
         lv.initialize(world, world.getLocalDifficulty(pos), SpawnReason.NATURAL, (EntityData)null, (NbtCompound)null);
         lv.refreshPositionAndAngles(pos, 0.0F, 0.0F);
         world.spawnEntityAndPassengers(lv);
         return 1;
      }
   }
}
