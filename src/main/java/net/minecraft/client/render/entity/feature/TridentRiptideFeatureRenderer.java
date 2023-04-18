package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class TridentRiptideFeatureRenderer extends FeatureRenderer {
   public static final Identifier TEXTURE = new Identifier("textures/entity/trident_riptide.png");
   public static final String BOX = "box";
   private final ModelPart aura;

   public TridentRiptideFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      ModelPart lv = loader.getModelPart(EntityModelLayers.SPIN_ATTACK);
      this.aura = lv.getChild("box");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("box", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -16.0F, -8.0F, 16.0F, 32.0F, 16.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 64);
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LivingEntity arg3, float f, float g, float h, float j, float k, float l) {
      if (arg3.isUsingRiptide()) {
         VertexConsumer lv = arg2.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));

         for(int m = 0; m < 3; ++m) {
            arg.push();
            float n = j * (float)(-(45 + m * 5));
            arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n));
            float o = 0.75F * (float)m;
            arg.scale(o, o, o);
            arg.translate(0.0F, -0.2F + 0.6F * (float)m, 0.0F);
            this.aura.render(arg, lv, i, OverlayTexture.DEFAULT_UV);
            arg.pop();
         }

      }
   }
}
