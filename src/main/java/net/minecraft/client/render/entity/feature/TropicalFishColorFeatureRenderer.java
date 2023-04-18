package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.LargeTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.SmallTropicalFishEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TropicalFishColorFeatureRenderer extends FeatureRenderer {
   private static final Identifier KOB_TEXTURE = new Identifier("textures/entity/fish/tropical_a_pattern_1.png");
   private static final Identifier SUNSTREAK_TEXTURE = new Identifier("textures/entity/fish/tropical_a_pattern_2.png");
   private static final Identifier SNOOPER_TEXTURE = new Identifier("textures/entity/fish/tropical_a_pattern_3.png");
   private static final Identifier DASHER_TEXTURE = new Identifier("textures/entity/fish/tropical_a_pattern_4.png");
   private static final Identifier BRINELY_TEXTURE = new Identifier("textures/entity/fish/tropical_a_pattern_5.png");
   private static final Identifier SPOTTY_TEXTURE = new Identifier("textures/entity/fish/tropical_a_pattern_6.png");
   private static final Identifier FLOPPER_TEXTURE = new Identifier("textures/entity/fish/tropical_b_pattern_1.png");
   private static final Identifier STRIPEY_TEXTURE = new Identifier("textures/entity/fish/tropical_b_pattern_2.png");
   private static final Identifier GLITTER_TEXTURE = new Identifier("textures/entity/fish/tropical_b_pattern_3.png");
   private static final Identifier BLOCKFISH_TEXTURE = new Identifier("textures/entity/fish/tropical_b_pattern_4.png");
   private static final Identifier BETTY_TEXTURE = new Identifier("textures/entity/fish/tropical_b_pattern_5.png");
   private static final Identifier CLAYFISH_TEXTURE = new Identifier("textures/entity/fish/tropical_b_pattern_6.png");
   private final SmallTropicalFishEntityModel smallModel;
   private final LargeTropicalFishEntityModel largeModel;

   public TropicalFishColorFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.smallModel = new SmallTropicalFishEntityModel(loader.getModelPart(EntityModelLayers.TROPICAL_FISH_SMALL_PATTERN));
      this.largeModel = new LargeTropicalFishEntityModel(loader.getModelPart(EntityModelLayers.TROPICAL_FISH_LARGE_PATTERN));
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, TropicalFishEntity arg3, float f, float g, float h, float j, float k, float l) {
      TropicalFishEntity.Variety lv = arg3.getVariant();
      Object var10000;
      switch (lv.getSize()) {
         case SMALL:
            var10000 = this.smallModel;
            break;
         case LARGE:
            var10000 = this.largeModel;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      EntityModel lv2 = var10000;
      Identifier var15;
      switch (lv) {
         case KOB:
            var15 = KOB_TEXTURE;
            break;
         case SUNSTREAK:
            var15 = SUNSTREAK_TEXTURE;
            break;
         case SNOOPER:
            var15 = SNOOPER_TEXTURE;
            break;
         case DASHER:
            var15 = DASHER_TEXTURE;
            break;
         case BRINELY:
            var15 = BRINELY_TEXTURE;
            break;
         case SPOTTY:
            var15 = SPOTTY_TEXTURE;
            break;
         case FLOPPER:
            var15 = FLOPPER_TEXTURE;
            break;
         case STRIPEY:
            var15 = STRIPEY_TEXTURE;
            break;
         case GLITTER:
            var15 = GLITTER_TEXTURE;
            break;
         case BLOCKFISH:
            var15 = BLOCKFISH_TEXTURE;
            break;
         case BETTY:
            var15 = BETTY_TEXTURE;
            break;
         case CLAYFISH:
            var15 = CLAYFISH_TEXTURE;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      Identifier lv3 = var15;
      float[] fs = arg3.getPatternColorComponents().getColorComponents();
      render(this.getContextModel(), (EntityModel)lv2, lv3, arg, arg2, i, arg3, f, g, j, k, l, h, fs[0], fs[1], fs[2]);
   }
}
