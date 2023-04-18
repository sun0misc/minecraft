package net.minecraft.client.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CompassAnglePredicateProvider implements ClampedModelPredicateProvider {
   public static final int field_38798 = 0;
   private final AngleInterpolator aimedInterpolator = new AngleInterpolator();
   private final AngleInterpolator aimlessInterpolator = new AngleInterpolator();
   public final CompassTarget compassTarget;

   public CompassAnglePredicateProvider(CompassTarget compassTarget) {
      this.compassTarget = compassTarget;
   }

   public float unclampedCall(ItemStack arg, @Nullable ClientWorld arg2, @Nullable LivingEntity arg3, int i) {
      Entity lv = arg3 != null ? arg3 : arg.getHolder();
      if (lv == null) {
         return 0.0F;
      } else {
         arg2 = this.getClientWorld((Entity)lv, arg2);
         return arg2 == null ? 0.0F : this.getAngle(arg, arg2, i, (Entity)lv);
      }
   }

   private float getAngle(ItemStack stack, ClientWorld world, int seed, Entity entity) {
      GlobalPos lv = this.compassTarget.getPos(world, stack, entity);
      long l = world.getTime();
      return !this.canPointTo(entity, lv) ? this.getAimlessAngle(seed, l) : this.getAngleTo(entity, l, lv.getPos());
   }

   private float getAimlessAngle(int seed, long time) {
      if (this.aimlessInterpolator.shouldUpdate(time)) {
         this.aimlessInterpolator.update(time, Math.random());
      }

      double d = this.aimlessInterpolator.value + (double)((float)this.scatter(seed) / 2.1474836E9F);
      return MathHelper.floorMod((float)d, 1.0F);
   }

   private float getAngleTo(Entity entity, long time, BlockPos pos) {
      double d = this.getAngleTo(entity, pos);
      double e = this.getBodyYaw(entity);
      double f;
      if (entity instanceof PlayerEntity lv) {
         if (lv.isMainPlayer()) {
            if (this.aimedInterpolator.shouldUpdate(time)) {
               this.aimedInterpolator.update(time, 0.5 - (e - 0.25));
            }

            f = d + this.aimedInterpolator.value;
            return MathHelper.floorMod((float)f, 1.0F);
         }
      }

      f = 0.5 - (e - 0.25 - d);
      return MathHelper.floorMod((float)f, 1.0F);
   }

   @Nullable
   private ClientWorld getClientWorld(Entity entity, @Nullable ClientWorld world) {
      return world == null && entity.world instanceof ClientWorld ? (ClientWorld)entity.world : world;
   }

   private boolean canPointTo(Entity entity, @Nullable GlobalPos pos) {
      return pos != null && pos.getDimension() == entity.world.getRegistryKey() && !(pos.getPos().getSquaredDistance(entity.getPos()) < 9.999999747378752E-6);
   }

   private double getAngleTo(Entity entity, BlockPos pos) {
      Vec3d lv = Vec3d.ofCenter(pos);
      return Math.atan2(lv.getZ() - entity.getZ(), lv.getX() - entity.getX()) / 6.2831854820251465;
   }

   private double getBodyYaw(Entity entity) {
      return MathHelper.floorMod((double)(entity.getBodyYaw() / 360.0F), 1.0);
   }

   private int scatter(int seed) {
      return seed * 1327217883;
   }

   @Environment(EnvType.CLIENT)
   static class AngleInterpolator {
      double value;
      private double speed;
      private long lastUpdateTime;

      boolean shouldUpdate(long time) {
         return this.lastUpdateTime != time;
      }

      void update(long time, double target) {
         this.lastUpdateTime = time;
         double e = target - this.value;
         e = MathHelper.floorMod(e + 0.5, 1.0) - 0.5;
         this.speed += e * 0.1;
         this.speed *= 0.8;
         this.value = MathHelper.floorMod(this.value + this.speed, 1.0);
      }
   }

   @Environment(EnvType.CLIENT)
   public interface CompassTarget {
      @Nullable
      GlobalPos getPos(ClientWorld world, ItemStack stack, Entity entity);
   }
}
