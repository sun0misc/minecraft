package net.minecraft.predicate.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.AbstractTeam;
import org.jetbrains.annotations.Nullable;

public final class EntityPredicates {
   public static final Predicate VALID_ENTITY = Entity::isAlive;
   public static final Predicate VALID_LIVING_ENTITY = (entity) -> {
      return entity.isAlive() && entity instanceof LivingEntity;
   };
   public static final Predicate NOT_MOUNTED = (entity) -> {
      return entity.isAlive() && !entity.hasPassengers() && !entity.hasVehicle();
   };
   public static final Predicate VALID_INVENTORIES = (entity) -> {
      return entity instanceof Inventory && entity.isAlive();
   };
   public static final Predicate EXCEPT_CREATIVE_OR_SPECTATOR = (entity) -> {
      return !(entity instanceof PlayerEntity) || !entity.isSpectator() && !((PlayerEntity)entity).isCreative();
   };
   public static final Predicate EXCEPT_SPECTATOR = (entity) -> {
      return !entity.isSpectator();
   };
   public static final Predicate CAN_COLLIDE;

   private EntityPredicates() {
   }

   public static Predicate maxDistance(double x, double y, double z, double max) {
      double h = max * max;
      return (entity) -> {
         return entity != null && entity.squaredDistanceTo(x, y, z) <= h;
      };
   }

   public static Predicate canBePushedBy(Entity entity) {
      AbstractTeam lv = entity.getScoreboardTeam();
      AbstractTeam.CollisionRule lv2 = lv == null ? AbstractTeam.CollisionRule.ALWAYS : lv.getCollisionRule();
      return (Predicate)(lv2 == AbstractTeam.CollisionRule.NEVER ? Predicates.alwaysFalse() : EXCEPT_SPECTATOR.and((entityx) -> {
         if (!entityx.isPushable()) {
            return false;
         } else if (entity.world.isClient && (!(entityx instanceof PlayerEntity) || !((PlayerEntity)entityx).isMainPlayer())) {
            return false;
         } else {
            AbstractTeam lvx = entityx.getScoreboardTeam();
            AbstractTeam.CollisionRule lv2x = lvx == null ? AbstractTeam.CollisionRule.ALWAYS : lvx.getCollisionRule();
            if (lv2x == AbstractTeam.CollisionRule.NEVER) {
               return false;
            } else {
               boolean bl = lv != null && lv.isEqual(lvx);
               if ((lv2 == AbstractTeam.CollisionRule.PUSH_OWN_TEAM || lv2x == AbstractTeam.CollisionRule.PUSH_OWN_TEAM) && bl) {
                  return false;
               } else {
                  return lv2 != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS && lv2x != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS || bl;
               }
            }
         }
      }));
   }

   public static Predicate rides(Entity entity) {
      return (testedEntity) -> {
         while(true) {
            if (testedEntity.hasVehicle()) {
               testedEntity = testedEntity.getVehicle();
               if (testedEntity != entity) {
                  continue;
               }

               return false;
            }

            return true;
         }
      };
   }

   static {
      CAN_COLLIDE = EXCEPT_SPECTATOR.and(Entity::isCollidable);
   }

   public static class Equipable implements Predicate {
      private final ItemStack stack;

      public Equipable(ItemStack stack) {
         this.stack = stack;
      }

      public boolean test(@Nullable Entity arg) {
         if (!arg.isAlive()) {
            return false;
         } else if (!(arg instanceof LivingEntity)) {
            return false;
         } else {
            LivingEntity lv = (LivingEntity)arg;
            return lv.canEquip(this.stack);
         }
      }

      // $FF: synthetic method
      public boolean test(@Nullable Object context) {
         return this.test((Entity)context);
      }
   }
}
