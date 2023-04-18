package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.TurtleEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TurtleEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/turtle/big_sea_turtle.png");

   public TurtleEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new TurtleEntityModel(arg.getPart(EntityModelLayers.TURTLE)), 0.7F);
   }

   public void render(TurtleEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      if (arg.isBaby()) {
         this.shadowRadius *= 0.5F;
      }

      super.render((MobEntity)arg, f, g, arg2, arg3, i);
   }

   public Identifier getTexture(TurtleEntity arg) {
      return TEXTURE;
   }
}
