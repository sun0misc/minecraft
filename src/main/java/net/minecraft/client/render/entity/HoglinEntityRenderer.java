package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.HoglinEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class HoglinEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/hoglin/hoglin.png");

   public HoglinEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new HoglinEntityModel(arg.getPart(EntityModelLayers.HOGLIN)), 0.7F);
   }

   public Identifier getTexture(HoglinEntity arg) {
      return TEXTURE;
   }

   protected boolean isShaking(HoglinEntity arg) {
      return super.isShaking(arg) || arg.canConvert();
   }

   // $FF: synthetic method
   protected boolean isShaking(LivingEntity entity) {
      return this.isShaking((HoglinEntity)entity);
   }
}
