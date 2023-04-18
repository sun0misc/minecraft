package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SilverfishEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SilverfishEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/silverfish.png");

   public SilverfishEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new SilverfishEntityModel(arg.getPart(EntityModelLayers.SILVERFISH)), 0.3F);
   }

   protected float getLyingAngle(SilverfishEntity arg) {
      return 180.0F;
   }

   public Identifier getTexture(SilverfishEntity arg) {
      return TEXTURE;
   }

   // $FF: synthetic method
   protected float getLyingAngle(LivingEntity entity) {
      return this.getLyingAngle((SilverfishEntity)entity);
   }

   // $FF: synthetic method
   public Identifier getTexture(Entity entity) {
      return this.getTexture((SilverfishEntity)entity);
   }
}
