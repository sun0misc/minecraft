package net.minecraft.entity.projectile.thrown;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnderPearlEntity extends ThrownItemEntity {
   public EnderPearlEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public EnderPearlEntity(World world, LivingEntity owner) {
      super(EntityType.ENDER_PEARL, owner, world);
   }

   protected Item getDefaultItem() {
      return Items.ENDER_PEARL;
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      entityHitResult.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), 0.0F);
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);

      for(int i = 0; i < 32; ++i) {
         this.world.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian());
      }

      if (!this.world.isClient && !this.isRemoved()) {
         Entity lv = this.getOwner();
         if (lv instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv2 = (ServerPlayerEntity)lv;
            if (lv2.networkHandler.isConnectionOpen() && lv2.world == this.world && !lv2.isSleeping()) {
               if (this.random.nextFloat() < 0.05F && this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
                  EndermiteEntity lv3 = (EndermiteEntity)EntityType.ENDERMITE.create(this.world);
                  if (lv3 != null) {
                     lv3.refreshPositionAndAngles(lv.getX(), lv.getY(), lv.getZ(), lv.getYaw(), lv.getPitch());
                     this.world.spawnEntity(lv3);
                  }
               }

               if (lv.hasVehicle()) {
                  lv2.requestTeleportAndDismount(this.getX(), this.getY(), this.getZ());
               } else {
                  lv.requestTeleport(this.getX(), this.getY(), this.getZ());
               }

               lv.onLanding();
               lv.damage(this.getDamageSources().fall(), 5.0F);
            }
         } else if (lv != null) {
            lv.requestTeleport(this.getX(), this.getY(), this.getZ());
            lv.onLanding();
         }

         this.discard();
      }

   }

   public void tick() {
      Entity lv = this.getOwner();
      if (lv instanceof PlayerEntity && !lv.isAlive()) {
         this.discard();
      } else {
         super.tick();
      }

   }

   @Nullable
   public Entity moveToWorld(ServerWorld destination) {
      Entity lv = this.getOwner();
      if (lv != null && lv.world.getRegistryKey() != destination.getRegistryKey()) {
         this.setOwner((Entity)null);
      }

      return super.moveToWorld(destination);
   }
}
