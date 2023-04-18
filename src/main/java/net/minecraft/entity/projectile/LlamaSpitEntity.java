package net.minecraft.entity.projectile;

import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LlamaSpitEntity extends ProjectileEntity {
   public LlamaSpitEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public LlamaSpitEntity(World world, LlamaEntity owner) {
      this(EntityType.LLAMA_SPIT, world);
      this.setOwner(owner);
      this.setPosition(owner.getX() - (double)(owner.getWidth() + 1.0F) * 0.5 * (double)MathHelper.sin(owner.bodyYaw * 0.017453292F), owner.getEyeY() - 0.10000000149011612, owner.getZ() + (double)(owner.getWidth() + 1.0F) * 0.5 * (double)MathHelper.cos(owner.bodyYaw * 0.017453292F));
   }

   public void tick() {
      super.tick();
      Vec3d lv = this.getVelocity();
      HitResult lv2 = ProjectileUtil.getCollision(this, this::canHit);
      this.onCollision(lv2);
      double d = this.getX() + lv.x;
      double e = this.getY() + lv.y;
      double f = this.getZ() + lv.z;
      this.updateRotation();
      float g = 0.99F;
      float h = 0.06F;
      if (this.world.getStatesInBox(this.getBoundingBox()).noneMatch(AbstractBlock.AbstractBlockState::isAir)) {
         this.discard();
      } else if (this.isInsideWaterOrBubbleColumn()) {
         this.discard();
      } else {
         this.setVelocity(lv.multiply(0.9900000095367432));
         if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.05999999865889549, 0.0));
         }

         this.setPosition(d, e, f);
      }
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      Entity var3 = this.getOwner();
      if (var3 instanceof LivingEntity lv) {
         entityHitResult.getEntity().damage(this.getDamageSources().mobProjectile(this, lv), 1.0F);
      }

   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      super.onBlockHit(blockHitResult);
      if (!this.world.isClient) {
         this.discard();
      }

   }

   protected void initDataTracker() {
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      super.onSpawnPacket(packet);
      double d = packet.getVelocityX();
      double e = packet.getVelocityY();
      double f = packet.getVelocityZ();

      for(int i = 0; i < 7; ++i) {
         double g = 0.4 + 0.1 * (double)i;
         this.world.addParticle(ParticleTypes.SPIT, this.getX(), this.getY(), this.getZ(), d * g, e, f * g);
      }

      this.setVelocity(d, e, f);
   }
}
