package net.minecraft.entity.ai.goal;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

public class IronGolemWanderAroundGoal extends WanderAroundGoal {
   private static final int CHUNK_RANGE = 2;
   private static final int ENTITY_COLLISION_RANGE = 32;
   private static final int HORIZONTAL_RANGE = 10;
   private static final int VERTICAL_RANGE = 7;

   public IronGolemWanderAroundGoal(PathAwareEntity arg, double d) {
      super(arg, d, 240, false);
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      float f = this.mob.world.random.nextFloat();
      if (this.mob.world.random.nextFloat() < 0.3F) {
         return this.findRandomInRange();
      } else {
         Vec3d lv;
         if (f < 0.7F) {
            lv = this.findVillagerPos();
            if (lv == null) {
               lv = this.findRandomBlockPos();
            }
         } else {
            lv = this.findRandomBlockPos();
            if (lv == null) {
               lv = this.findVillagerPos();
            }
         }

         return lv == null ? this.findRandomInRange() : lv;
      }
   }

   @Nullable
   private Vec3d findRandomInRange() {
      return FuzzyTargeting.find(this.mob, 10, 7);
   }

   @Nullable
   private Vec3d findVillagerPos() {
      ServerWorld lv = (ServerWorld)this.mob.world;
      List list = lv.getEntitiesByType(EntityType.VILLAGER, this.mob.getBoundingBox().expand(32.0), this::canVillagerSummonGolem);
      if (list.isEmpty()) {
         return null;
      } else {
         VillagerEntity lv2 = (VillagerEntity)list.get(this.mob.world.random.nextInt(list.size()));
         Vec3d lv3 = lv2.getPos();
         return FuzzyTargeting.findTo(this.mob, 10, 7, lv3);
      }
   }

   @Nullable
   private Vec3d findRandomBlockPos() {
      ChunkSectionPos lv = this.findRandomChunkPos();
      if (lv == null) {
         return null;
      } else {
         BlockPos lv2 = this.findRandomPosInChunk(lv);
         return lv2 == null ? null : FuzzyTargeting.findTo(this.mob, 10, 7, Vec3d.ofBottomCenter(lv2));
      }
   }

   @Nullable
   private ChunkSectionPos findRandomChunkPos() {
      ServerWorld lv = (ServerWorld)this.mob.world;
      List list = (List)ChunkSectionPos.stream(ChunkSectionPos.from((EntityLike)this.mob), 2).filter((sectionPos) -> {
         return lv.getOccupiedPointOfInterestDistance(sectionPos) == 0;
      }).collect(Collectors.toList());
      return list.isEmpty() ? null : (ChunkSectionPos)list.get(lv.random.nextInt(list.size()));
   }

   @Nullable
   private BlockPos findRandomPosInChunk(ChunkSectionPos pos) {
      ServerWorld lv = (ServerWorld)this.mob.world;
      PointOfInterestStorage lv2 = lv.getPointOfInterestStorage();
      List list = (List)lv2.getInCircle((arg) -> {
         return true;
      }, pos.getCenterPos(), 8, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED).map(PointOfInterest::getPos).collect(Collectors.toList());
      return list.isEmpty() ? null : (BlockPos)list.get(lv.random.nextInt(list.size()));
   }

   private boolean canVillagerSummonGolem(VillagerEntity villager) {
      return villager.canSummonGolem(this.mob.world.getTime());
   }
}
