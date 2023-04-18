package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class BoatItem extends Item {
   private static final Predicate RIDERS;
   private final BoatEntity.Type type;
   private final boolean chest;

   public BoatItem(boolean chest, BoatEntity.Type type, Item.Settings settings) {
      super(settings);
      this.chest = chest;
      this.type = type;
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      HitResult lv2 = raycast(world, user, RaycastContext.FluidHandling.ANY);
      if (lv2.getType() == HitResult.Type.MISS) {
         return TypedActionResult.pass(lv);
      } else {
         Vec3d lv3 = user.getRotationVec(1.0F);
         double d = 5.0;
         List list = world.getOtherEntities(user, user.getBoundingBox().stretch(lv3.multiply(5.0)).expand(1.0), RIDERS);
         if (!list.isEmpty()) {
            Vec3d lv4 = user.getEyePos();
            Iterator var11 = list.iterator();

            while(var11.hasNext()) {
               Entity lv5 = (Entity)var11.next();
               Box lv6 = lv5.getBoundingBox().expand((double)lv5.getTargetingMargin());
               if (lv6.contains(lv4)) {
                  return TypedActionResult.pass(lv);
               }
            }
         }

         if (lv2.getType() == HitResult.Type.BLOCK) {
            BoatEntity lv7 = this.createEntity(world, lv2);
            lv7.setVariant(this.type);
            lv7.setYaw(user.getYaw());
            if (!world.isSpaceEmpty(lv7, lv7.getBoundingBox())) {
               return TypedActionResult.fail(lv);
            } else {
               if (!world.isClient) {
                  world.spawnEntity(lv7);
                  world.emitGameEvent(user, GameEvent.ENTITY_PLACE, lv2.getPos());
                  if (!user.getAbilities().creativeMode) {
                     lv.decrement(1);
                  }
               }

               user.incrementStat(Stats.USED.getOrCreateStat(this));
               return TypedActionResult.success(lv, world.isClient());
            }
         } else {
            return TypedActionResult.pass(lv);
         }
      }
   }

   private BoatEntity createEntity(World world, HitResult hitResult) {
      return (BoatEntity)(this.chest ? new ChestBoatEntity(world, hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z) : new BoatEntity(world, hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z));
   }

   static {
      RIDERS = EntityPredicates.EXCEPT_SPECTATOR.and(Entity::canHit);
   }
}
