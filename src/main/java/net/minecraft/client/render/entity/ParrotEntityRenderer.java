package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ParrotEntityRenderer extends MobEntityRenderer {
   private static final Identifier RED_BLUE_TEXTURE = new Identifier("textures/entity/parrot/parrot_red_blue.png");
   private static final Identifier BLUE_TEXTURE = new Identifier("textures/entity/parrot/parrot_blue.png");
   private static final Identifier GREEN_TEXTURE = new Identifier("textures/entity/parrot/parrot_green.png");
   private static final Identifier YELLOW_TEXTURE = new Identifier("textures/entity/parrot/parrot_yellow_blue.png");
   private static final Identifier GREY_TEXTURE = new Identifier("textures/entity/parrot/parrot_grey.png");

   public ParrotEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new ParrotEntityModel(arg.getPart(EntityModelLayers.PARROT)), 0.3F);
   }

   public Identifier getTexture(ParrotEntity arg) {
      return getTexture(arg.getVariant());
   }

   public static Identifier getTexture(ParrotEntity.Variant variant) {
      Identifier var10000;
      switch (variant) {
         case RED_BLUE:
            var10000 = RED_BLUE_TEXTURE;
            break;
         case BLUE:
            var10000 = BLUE_TEXTURE;
            break;
         case GREEN:
            var10000 = GREEN_TEXTURE;
            break;
         case YELLOW_BLUE:
            var10000 = YELLOW_TEXTURE;
            break;
         case GRAY:
            var10000 = GREY_TEXTURE;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public float getAnimationProgress(ParrotEntity arg, float f) {
      float g = MathHelper.lerp(f, arg.prevFlapProgress, arg.flapProgress);
      float h = MathHelper.lerp(f, arg.prevMaxWingDeviation, arg.maxWingDeviation);
      return (MathHelper.sin(g) + 1.0F) * h;
   }
}
