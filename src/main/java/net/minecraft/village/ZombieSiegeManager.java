package net.minecraft.village;

import com.mojang.logging.LogUtils;
import java.util.Iterator;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ZombieSiegeManager implements Spawner {
   private static final Logger LOGGER = LogUtils.getLogger();
   private boolean spawned;
   private State state;
   private int remaining;
   private int countdown;
   private int startX;
   private int startY;
   private int startZ;

   public ZombieSiegeManager() {
      this.state = ZombieSiegeManager.State.SIEGE_DONE;
   }

   public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
      if (!world.isDay() && spawnMonsters) {
         float f = world.getSkyAngle(0.0F);
         if ((double)f == 0.5) {
            this.state = world.random.nextInt(10) == 0 ? ZombieSiegeManager.State.SIEGE_TONIGHT : ZombieSiegeManager.State.SIEGE_DONE;
         }

         if (this.state == ZombieSiegeManager.State.SIEGE_DONE) {
            return 0;
         } else {
            if (!this.spawned) {
               if (!this.spawn(world)) {
                  return 0;
               }

               this.spawned = true;
            }

            if (this.countdown > 0) {
               --this.countdown;
               return 0;
            } else {
               this.countdown = 2;
               if (this.remaining > 0) {
                  this.trySpawnZombie(world);
                  --this.remaining;
               } else {
                  this.state = ZombieSiegeManager.State.SIEGE_DONE;
               }

               return 1;
            }
         }
      } else {
         this.state = ZombieSiegeManager.State.SIEGE_DONE;
         this.spawned = false;
         return 0;
      }
   }

   private boolean spawn(ServerWorld world) {
      Iterator var2 = world.getPlayers().iterator();

      while(var2.hasNext()) {
         PlayerEntity lv = (PlayerEntity)var2.next();
         if (!lv.isSpectator()) {
            BlockPos lv2 = lv.getBlockPos();
            if (world.isNearOccupiedPointOfInterest(lv2) && !world.getBiome(lv2).isIn(BiomeTags.WITHOUT_ZOMBIE_SIEGES)) {
               for(int i = 0; i < 10; ++i) {
                  float f = world.random.nextFloat() * 6.2831855F;
                  this.startX = lv2.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0F);
                  this.startY = lv2.getY();
                  this.startZ = lv2.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0F);
                  if (this.getSpawnVector(world, new BlockPos(this.startX, this.startY, this.startZ)) != null) {
                     this.countdown = 0;
                     this.remaining = 20;
                     break;
                  }
               }

               return true;
            }
         }
      }

      return false;
   }

   private void trySpawnZombie(ServerWorld world) {
      Vec3d lv = this.getSpawnVector(world, new BlockPos(this.startX, this.startY, this.startZ));
      if (lv != null) {
         ZombieEntity lv2;
         try {
            lv2 = new ZombieEntity(world);
            lv2.initialize(world, world.getLocalDifficulty(lv2.getBlockPos()), SpawnReason.EVENT, (EntityData)null, (NbtCompound)null);
         } catch (Exception var5) {
            LOGGER.warn("Failed to create zombie for village siege at {}", lv, var5);
            return;
         }

         lv2.refreshPositionAndAngles(lv.x, lv.y, lv.z, world.random.nextFloat() * 360.0F, 0.0F);
         world.spawnEntityAndPassengers(lv2);
      }
   }

   @Nullable
   private Vec3d getSpawnVector(ServerWorld world, BlockPos pos) {
      for(int i = 0; i < 10; ++i) {
         int j = pos.getX() + world.random.nextInt(16) - 8;
         int k = pos.getZ() + world.random.nextInt(16) - 8;
         int l = world.getTopY(Heightmap.Type.WORLD_SURFACE, j, k);
         BlockPos lv = new BlockPos(j, l, k);
         if (world.isNearOccupiedPointOfInterest(lv) && HostileEntity.canSpawnInDark(EntityType.ZOMBIE, world, SpawnReason.EVENT, lv, world.random)) {
            return Vec3d.ofBottomCenter(lv);
         }
      }

      return null;
   }

   private static enum State {
      SIEGE_CAN_ACTIVATE,
      SIEGE_TONIGHT,
      SIEGE_DONE;

      // $FF: synthetic method
      private static State[] method_36628() {
         return new State[]{SIEGE_CAN_ACTIVATE, SIEGE_TONIGHT, SIEGE_DONE};
      }
   }
}
