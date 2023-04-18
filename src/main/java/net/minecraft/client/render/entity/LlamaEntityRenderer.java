package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.LlamaDecorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.LlamaEntityModel;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class LlamaEntityRenderer extends MobEntityRenderer {
   private static final Identifier CREAMY_TEXTURE = new Identifier("textures/entity/llama/creamy.png");
   private static final Identifier WHITE_TEXTURE = new Identifier("textures/entity/llama/white.png");
   private static final Identifier BROWN_TEXTURE = new Identifier("textures/entity/llama/brown.png");
   private static final Identifier GRAY_TEXTURE = new Identifier("textures/entity/llama/gray.png");

   public LlamaEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer) {
      super(ctx, new LlamaEntityModel(ctx.getPart(layer)), 0.7F);
      this.addFeature(new LlamaDecorFeatureRenderer(this, ctx.getModelLoader()));
   }

   public Identifier getTexture(LlamaEntity arg) {
      Identifier var10000;
      switch (arg.getVariant()) {
         case CREAMY:
            var10000 = CREAMY_TEXTURE;
            break;
         case WHITE:
            var10000 = WHITE_TEXTURE;
            break;
         case BROWN:
            var10000 = BROWN_TEXTURE;
            break;
         case GRAY:
            var10000 = GRAY_TEXTURE;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }
}
