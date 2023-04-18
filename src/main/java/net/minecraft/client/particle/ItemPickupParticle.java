package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class ItemPickupParticle extends Particle {
   private static final int field_32656 = 3;
   private final BufferBuilderStorage bufferStorage;
   private final Entity itemEntity;
   private final Entity interactingEntity;
   private int ticksExisted;
   private final EntityRenderDispatcher dispatcher;

   public ItemPickupParticle(EntityRenderDispatcher dispatcher, BufferBuilderStorage bufferStorage, ClientWorld world, Entity itemEntity, Entity interactingEntity) {
      this(dispatcher, bufferStorage, world, itemEntity, interactingEntity, itemEntity.getVelocity());
   }

   private ItemPickupParticle(EntityRenderDispatcher dispatcher, BufferBuilderStorage bufferStorage, ClientWorld world, Entity itemEntity, Entity interactingEntity, Vec3d velocity) {
      super(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), velocity.x, velocity.y, velocity.z);
      this.bufferStorage = bufferStorage;
      this.itemEntity = this.getOrCopy(itemEntity);
      this.interactingEntity = interactingEntity;
      this.dispatcher = dispatcher;
   }

   private Entity getOrCopy(Entity entity) {
      return (Entity)(!(entity instanceof ItemEntity) ? entity : ((ItemEntity)entity).copy());
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.CUSTOM;
   }

   public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
      float g = ((float)this.ticksExisted + tickDelta) / 3.0F;
      g *= g;
      double d = MathHelper.lerp((double)tickDelta, this.interactingEntity.lastRenderX, this.interactingEntity.getX());
      double e = MathHelper.lerp((double)tickDelta, this.interactingEntity.lastRenderY, (this.interactingEntity.getY() + this.interactingEntity.getEyeY()) / 2.0);
      double h = MathHelper.lerp((double)tickDelta, this.interactingEntity.lastRenderZ, this.interactingEntity.getZ());
      double i = MathHelper.lerp((double)g, this.itemEntity.getX(), d);
      double j = MathHelper.lerp((double)g, this.itemEntity.getY(), e);
      double k = MathHelper.lerp((double)g, this.itemEntity.getZ(), h);
      VertexConsumerProvider.Immediate lv = this.bufferStorage.getEntityVertexConsumers();
      Vec3d lv2 = camera.getPos();
      this.dispatcher.render(this.itemEntity, i - lv2.getX(), j - lv2.getY(), k - lv2.getZ(), this.itemEntity.getYaw(), tickDelta, new MatrixStack(), lv, this.dispatcher.getLight(this.itemEntity, tickDelta));
      lv.draw();
   }

   public void tick() {
      ++this.ticksExisted;
      if (this.ticksExisted == 3) {
         this.markDead();
      }

   }
}
