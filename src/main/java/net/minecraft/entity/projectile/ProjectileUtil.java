package net.minecraft.entity.projectile;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class ProjectileUtil {
   public static HitResult getCollision(Entity entity, Predicate predicate) {
      Vec3d lv = entity.getVelocity();
      World lv2 = entity.world;
      Vec3d lv3 = entity.getPos();
      return getCollision(lv3, entity, predicate, lv, lv2);
   }

   public static HitResult getCollision(Entity entity, Predicate predicate, double range) {
      Vec3d lv = entity.getRotationVec(0.0F).multiply(range);
      World lv2 = entity.world;
      Vec3d lv3 = entity.getEyePos();
      return getCollision(lv3, entity, predicate, lv, lv2);
   }

   private static HitResult getCollision(Vec3d pos, Entity entity, Predicate predicate, Vec3d velocity, World world) {
      Vec3d lv = pos.add(velocity);
      HitResult lv2 = world.raycast(new RaycastContext(pos, lv, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));
      if (((HitResult)lv2).getType() != HitResult.Type.MISS) {
         lv = ((HitResult)lv2).getPos();
      }

      HitResult lv3 = getEntityCollision(world, entity, pos, lv, entity.getBoundingBox().stretch(entity.getVelocity()).expand(1.0), predicate);
      if (lv3 != null) {
         lv2 = lv3;
      }

      return (HitResult)lv2;
   }

   @Nullable
   public static EntityHitResult raycast(Entity entity, Vec3d min, Vec3d max, Box box, Predicate predicate, double d) {
      World lv = entity.world;
      double e = d;
      Entity lv2 = null;
      Vec3d lv3 = null;
      Iterator var12 = lv.getOtherEntities(entity, box, predicate).iterator();

      while(true) {
         while(var12.hasNext()) {
            Entity lv4 = (Entity)var12.next();
            Box lv5 = lv4.getBoundingBox().expand((double)lv4.getTargetingMargin());
            Optional optional = lv5.raycast(min, max);
            if (lv5.contains(min)) {
               if (e >= 0.0) {
                  lv2 = lv4;
                  lv3 = (Vec3d)optional.orElse(min);
                  e = 0.0;
               }
            } else if (optional.isPresent()) {
               Vec3d lv6 = (Vec3d)optional.get();
               double f = min.squaredDistanceTo(lv6);
               if (f < e || e == 0.0) {
                  if (lv4.getRootVehicle() == entity.getRootVehicle()) {
                     if (e == 0.0) {
                        lv2 = lv4;
                        lv3 = lv6;
                     }
                  } else {
                     lv2 = lv4;
                     lv3 = lv6;
                     e = f;
                  }
               }
            }
         }

         if (lv2 == null) {
            return null;
         }

         return new EntityHitResult(lv2, lv3);
      }
   }

   @Nullable
   public static EntityHitResult getEntityCollision(World world, Entity entity, Vec3d min, Vec3d max, Box box, Predicate predicate) {
      return getEntityCollision(world, entity, min, max, box, predicate, 0.3F);
   }

   @Nullable
   public static EntityHitResult getEntityCollision(World world, Entity entity, Vec3d min, Vec3d max, Box box, Predicate predicate, float margin) {
      double d = Double.MAX_VALUE;
      Entity lv = null;
      Iterator var10 = world.getOtherEntities(entity, box, predicate).iterator();

      while(var10.hasNext()) {
         Entity lv2 = (Entity)var10.next();
         Box lv3 = lv2.getBoundingBox().expand((double)margin);
         Optional optional = lv3.raycast(min, max);
         if (optional.isPresent()) {
            double e = min.squaredDistanceTo((Vec3d)optional.get());
            if (e < d) {
               lv = lv2;
               d = e;
            }
         }
      }

      if (lv == null) {
         return null;
      } else {
         return new EntityHitResult(lv);
      }
   }

   public static void setRotationFromVelocity(Entity entity, float delta) {
      Vec3d lv = entity.getVelocity();
      if (lv.lengthSquared() != 0.0) {
         double d = lv.horizontalLength();
         entity.setYaw((float)(MathHelper.atan2(lv.z, lv.x) * 57.2957763671875) + 90.0F);
         entity.setPitch((float)(MathHelper.atan2(d, lv.y) * 57.2957763671875) - 90.0F);

         while(entity.getPitch() - entity.prevPitch < -180.0F) {
            entity.prevPitch -= 360.0F;
         }

         while(entity.getPitch() - entity.prevPitch >= 180.0F) {
            entity.prevPitch += 360.0F;
         }

         while(entity.getYaw() - entity.prevYaw < -180.0F) {
            entity.prevYaw -= 360.0F;
         }

         while(entity.getYaw() - entity.prevYaw >= 180.0F) {
            entity.prevYaw += 360.0F;
         }

         entity.setPitch(MathHelper.lerp(delta, entity.prevPitch, entity.getPitch()));
         entity.setYaw(MathHelper.lerp(delta, entity.prevYaw, entity.getYaw()));
      }
   }

   public static Hand getHandPossiblyHolding(LivingEntity entity, Item item) {
      return entity.getMainHandStack().isOf(item) ? Hand.MAIN_HAND : Hand.OFF_HAND;
   }

   public static PersistentProjectileEntity createArrowProjectile(LivingEntity entity, ItemStack stack, float damageModifier) {
      ArrowItem lv = (ArrowItem)(stack.getItem() instanceof ArrowItem ? stack.getItem() : Items.ARROW);
      PersistentProjectileEntity lv2 = lv.createArrow(entity.world, stack, entity);
      lv2.applyEnchantmentEffects(entity, damageModifier);
      if (stack.isOf(Items.TIPPED_ARROW) && lv2 instanceof ArrowEntity) {
         ((ArrowEntity)lv2).initFromStack(stack);
      }

      return lv2;
   }
}
