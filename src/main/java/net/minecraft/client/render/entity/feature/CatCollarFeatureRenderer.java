package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.CatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CatCollarFeatureRenderer extends FeatureRenderer {
   private static final Identifier SKIN = new Identifier("textures/entity/cat/cat_collar.png");
   private final CatEntityModel model;

   public CatCollarFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.model = new CatEntityModel(loader.getModelPart(EntityModelLayers.CAT_COLLAR));
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, CatEntity arg3, float f, float g, float h, float j, float k, float l) {
      if (arg3.isTamed()) {
         float[] fs = arg3.getCollarColor().getColorComponents();
         render(this.getContextModel(), this.model, SKIN, arg, arg2, i, arg3, f, g, j, k, l, h, fs[0], fs[1], fs[2]);
      }
   }
}
