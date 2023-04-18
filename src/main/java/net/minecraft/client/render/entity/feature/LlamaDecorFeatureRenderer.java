package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.LlamaEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class LlamaDecorFeatureRenderer extends FeatureRenderer {
   private static final Identifier[] LLAMA_DECOR = new Identifier[]{new Identifier("textures/entity/llama/decor/white.png"), new Identifier("textures/entity/llama/decor/orange.png"), new Identifier("textures/entity/llama/decor/magenta.png"), new Identifier("textures/entity/llama/decor/light_blue.png"), new Identifier("textures/entity/llama/decor/yellow.png"), new Identifier("textures/entity/llama/decor/lime.png"), new Identifier("textures/entity/llama/decor/pink.png"), new Identifier("textures/entity/llama/decor/gray.png"), new Identifier("textures/entity/llama/decor/light_gray.png"), new Identifier("textures/entity/llama/decor/cyan.png"), new Identifier("textures/entity/llama/decor/purple.png"), new Identifier("textures/entity/llama/decor/blue.png"), new Identifier("textures/entity/llama/decor/brown.png"), new Identifier("textures/entity/llama/decor/green.png"), new Identifier("textures/entity/llama/decor/red.png"), new Identifier("textures/entity/llama/decor/black.png")};
   private static final Identifier TRADER_LLAMA_DECOR = new Identifier("textures/entity/llama/decor/trader_llama.png");
   private final LlamaEntityModel model;

   public LlamaDecorFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.model = new LlamaEntityModel(loader.getModelPart(EntityModelLayers.LLAMA_DECOR));
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LlamaEntity arg3, float f, float g, float h, float j, float k, float l) {
      DyeColor lv = arg3.getCarpetColor();
      Identifier lv2;
      if (lv != null) {
         lv2 = LLAMA_DECOR[lv.getId()];
      } else {
         if (!arg3.isTrader()) {
            return;
         }

         lv2 = TRADER_LLAMA_DECOR;
      }

      ((LlamaEntityModel)this.getContextModel()).copyStateTo(this.model);
      this.model.setAngles((AbstractDonkeyEntity)arg3, f, g, j, k, l);
      VertexConsumer lv3 = arg2.getBuffer(RenderLayer.getEntityCutoutNoCull(lv2));
      this.model.render(arg, lv3, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
   }
}
