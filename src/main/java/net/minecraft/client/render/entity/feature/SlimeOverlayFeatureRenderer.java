package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class SlimeOverlayFeatureRenderer extends FeatureRenderer {
   private final EntityModel model;

   public SlimeOverlayFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.model = new SlimeEntityModel(loader.getModelPart(EntityModelLayers.SLIME_OUTER));
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LivingEntity arg3, float f, float g, float h, float j, float k, float l) {
      MinecraftClient lv = MinecraftClient.getInstance();
      boolean bl = lv.hasOutline(arg3) && arg3.isInvisible();
      if (!arg3.isInvisible() || bl) {
         VertexConsumer lv2;
         if (bl) {
            lv2 = arg2.getBuffer(RenderLayer.getOutline(this.getTexture(arg3)));
         } else {
            lv2 = arg2.getBuffer(RenderLayer.getEntityTranslucent(this.getTexture(arg3)));
         }

         ((SlimeEntityModel)this.getContextModel()).copyStateTo(this.model);
         this.model.animateModel(arg3, f, g, h);
         this.model.setAngles(arg3, f, g, j, k, l);
         this.model.render(arg, lv2, i, LivingEntityRenderer.getOverlay(arg3, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
