package net.minecraft.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public interface EntityView {
   List getOtherEntities(@Nullable Entity except, Box box, Predicate predicate);

   List getEntitiesByType(TypeFilter filter, Box box, Predicate predicate);

   default List getEntitiesByClass(Class entityClass, Box box, Predicate predicate) {
      return this.getEntitiesByType(TypeFilter.instanceOf(entityClass), box, predicate);
   }

   List getPlayers();

   default List getOtherEntities(@Nullable Entity except, Box box) {
      return this.getOtherEntities(except, box, EntityPredicates.EXCEPT_SPECTATOR);
   }

   default boolean doesNotIntersectEntities(@Nullable Entity except, VoxelShape shape) {
      if (shape.isEmpty()) {
         return true;
      } else {
         Iterator var3 = this.getOtherEntities(except, shape.getBoundingBox()).iterator();

         Entity lv;
         do {
            do {
               do {
                  do {
                     if (!var3.hasNext()) {
                        return true;
                     }

                     lv = (Entity)var3.next();
                  } while(lv.isRemoved());
               } while(!lv.intersectionChecked);
            } while(except != null && lv.isConnectedThroughVehicle(except));
         } while(!VoxelShapes.matchesAnywhere(shape, VoxelShapes.cuboid(lv.getBoundingBox()), BooleanBiFunction.AND));

         return false;
      }
   }

   default List getNonSpectatingEntities(Class entityClass, Box box) {
      return this.getEntitiesByClass(entityClass, box, EntityPredicates.EXCEPT_SPECTATOR);
   }

   default List getEntityCollisions(@Nullable Entity entity, Box box) {
      if (box.getAverageSideLength() < 1.0E-7) {
         return List.of();
      } else {
         Predicate var10000;
         if (entity == null) {
            var10000 = EntityPredicates.CAN_COLLIDE;
         } else {
            var10000 = EntityPredicates.EXCEPT_SPECTATOR;
            Objects.requireNonNull(entity);
            var10000 = var10000.and(entity::collidesWith);
         }

         Predicate predicate = var10000;
         List list = this.getOtherEntities(entity, box.expand(1.0E-7), predicate);
         if (list.isEmpty()) {
            return List.of();
         } else {
            ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize(list.size());
            Iterator var6 = list.iterator();

            while(var6.hasNext()) {
               Entity lv = (Entity)var6.next();
               builder.add(VoxelShapes.cuboid(lv.getBoundingBox()));
            }

            return builder.build();
         }
      }
   }

   @Nullable
   default PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, @Nullable Predicate targetPredicate) {
      double h = -1.0;
      PlayerEntity lv = null;
      Iterator var13 = this.getPlayers().iterator();

      while(true) {
         PlayerEntity lv2;
         double i;
         do {
            do {
               do {
                  if (!var13.hasNext()) {
                     return lv;
                  }

                  lv2 = (PlayerEntity)var13.next();
               } while(targetPredicate != null && !targetPredicate.test(lv2));

               i = lv2.squaredDistanceTo(x, y, z);
            } while(!(maxDistance < 0.0) && !(i < maxDistance * maxDistance));
         } while(h != -1.0 && !(i < h));

         h = i;
         lv = lv2;
      }
   }

   @Nullable
   default PlayerEntity getClosestPlayer(Entity entity, double maxDistance) {
      return this.getClosestPlayer(entity.getX(), entity.getY(), entity.getZ(), maxDistance, false);
   }

   @Nullable
   default PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, boolean ignoreCreative) {
      Predicate predicate = ignoreCreative ? EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR : EntityPredicates.EXCEPT_SPECTATOR;
      return this.getClosestPlayer(x, y, z, maxDistance, predicate);
   }

   default boolean isPlayerInRange(double x, double y, double z, double range) {
      Iterator var9 = this.getPlayers().iterator();

      double h;
      do {
         PlayerEntity lv;
         do {
            do {
               if (!var9.hasNext()) {
                  return false;
               }

               lv = (PlayerEntity)var9.next();
            } while(!EntityPredicates.EXCEPT_SPECTATOR.test(lv));
         } while(!EntityPredicates.VALID_LIVING_ENTITY.test(lv));

         h = lv.squaredDistanceTo(x, y, z);
      } while(!(range < 0.0) && !(h < range * range));

      return true;
   }

   @Nullable
   default PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity) {
      return (PlayerEntity)this.getClosestEntity(this.getPlayers(), targetPredicate, entity, entity.getX(), entity.getY(), entity.getZ());
   }

   @Nullable
   default PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z) {
      return (PlayerEntity)this.getClosestEntity(this.getPlayers(), targetPredicate, entity, x, y, z);
   }

   @Nullable
   default PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, double x, double y, double z) {
      return (PlayerEntity)this.getClosestEntity(this.getPlayers(), targetPredicate, (LivingEntity)null, x, y, z);
   }

   @Nullable
   default LivingEntity getClosestEntity(Class entityClass, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z, Box box) {
      return this.getClosestEntity(this.getEntitiesByClass(entityClass, box, (entityx) -> {
         return true;
      }), targetPredicate, entity, x, y, z);
   }

   @Nullable
   default LivingEntity getClosestEntity(List entityList, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z) {
      double g = -1.0;
      LivingEntity lv = null;
      Iterator var13 = entityList.iterator();

      while(true) {
         LivingEntity lv2;
         double h;
         do {
            do {
               if (!var13.hasNext()) {
                  return lv;
               }

               lv2 = (LivingEntity)var13.next();
            } while(!targetPredicate.test(entity, lv2));

            h = lv2.squaredDistanceTo(x, y, z);
         } while(g != -1.0 && !(h < g));

         g = h;
         lv = lv2;
      }
   }

   default List getPlayers(TargetPredicate targetPredicate, LivingEntity entity, Box box) {
      List list = Lists.newArrayList();
      Iterator var5 = this.getPlayers().iterator();

      while(var5.hasNext()) {
         PlayerEntity lv = (PlayerEntity)var5.next();
         if (box.contains(lv.getX(), lv.getY(), lv.getZ()) && targetPredicate.test(entity, lv)) {
            list.add(lv);
         }
      }

      return list;
   }

   default List getTargets(Class entityClass, TargetPredicate targetPredicate, LivingEntity targetingEntity, Box box) {
      List list = this.getEntitiesByClass(entityClass, box, (arg) -> {
         return true;
      });
      List list2 = Lists.newArrayList();
      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         LivingEntity lv = (LivingEntity)var7.next();
         if (targetPredicate.test(targetingEntity, lv)) {
            list2.add(lv);
         }
      }

      return list2;
   }

   @Nullable
   default PlayerEntity getPlayerByUuid(UUID uuid) {
      for(int i = 0; i < this.getPlayers().size(); ++i) {
         PlayerEntity lv = (PlayerEntity)this.getPlayers().get(i);
         if (uuid.equals(lv.getUuid())) {
            return lv;
         }
      }

      return null;
   }
}
