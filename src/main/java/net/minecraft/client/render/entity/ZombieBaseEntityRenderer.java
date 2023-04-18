package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class ZombieBaseEntityRenderer extends BipedEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/zombie/zombie.png");

   protected ZombieBaseEntityRenderer(EntityRendererFactory.Context ctx, ZombieEntityModel bodyModel, ZombieEntityModel legsArmorModel, ZombieEntityModel bodyArmorModel) {
      super(ctx, bodyModel, 0.5F);
      this.addFeature(new ArmorFeatureRenderer(this, legsArmorModel, bodyArmorModel, ctx.getModelManager()));
   }

   public Identifier getTexture(ZombieEntity arg) {
      return TEXTURE;
   }

   protected boolean isShaking(ZombieEntity arg) {
      return super.isShaking(arg) || arg.isConvertingInWater();
   }

   // $FF: synthetic method
   protected boolean isShaking(LivingEntity entity) {
      return this.isShaking((ZombieEntity)entity);
   }
}
