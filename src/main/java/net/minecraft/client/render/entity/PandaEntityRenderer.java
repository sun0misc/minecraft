package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.PandaHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PandaEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class PandaEntityRenderer extends MobEntityRenderer {
   private static final Map TEXTURES = (Map)Util.make(Maps.newEnumMap(PandaEntity.Gene.class), (map) -> {
      map.put(PandaEntity.Gene.NORMAL, new Identifier("textures/entity/panda/panda.png"));
      map.put(PandaEntity.Gene.LAZY, new Identifier("textures/entity/panda/lazy_panda.png"));
      map.put(PandaEntity.Gene.WORRIED, new Identifier("textures/entity/panda/worried_panda.png"));
      map.put(PandaEntity.Gene.PLAYFUL, new Identifier("textures/entity/panda/playful_panda.png"));
      map.put(PandaEntity.Gene.BROWN, new Identifier("textures/entity/panda/brown_panda.png"));
      map.put(PandaEntity.Gene.WEAK, new Identifier("textures/entity/panda/weak_panda.png"));
      map.put(PandaEntity.Gene.AGGRESSIVE, new Identifier("textures/entity/panda/aggressive_panda.png"));
   });

   public PandaEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new PandaEntityModel(arg.getPart(EntityModelLayers.PANDA)), 0.9F);
      this.addFeature(new PandaHeldItemFeatureRenderer(this, arg.getHeldItemRenderer()));
   }

   public Identifier getTexture(PandaEntity arg) {
      return (Identifier)TEXTURES.getOrDefault(arg.getProductGene(), (Identifier)TEXTURES.get(PandaEntity.Gene.NORMAL));
   }

   protected void setupTransforms(PandaEntity arg, MatrixStack arg2, float f, float g, float h) {
      super.setupTransforms(arg, arg2, f, g, h);
      float k;
      if (arg.playingTicks > 0) {
         int i = arg.playingTicks;
         int j = i + 1;
         k = 7.0F;
         float l = arg.isBaby() ? 0.3F : 0.8F;
         float o;
         float m;
         float n;
         if (i < 8) {
            m = (float)(90 * i) / 7.0F;
            n = (float)(90 * j) / 7.0F;
            o = this.getAngle(m, n, j, h, 8.0F);
            arg2.translate(0.0F, (l + 0.2F) * (o / 90.0F), 0.0F);
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-o));
         } else {
            float p;
            if (i < 16) {
               m = ((float)i - 8.0F) / 7.0F;
               n = 90.0F + 90.0F * m;
               p = 90.0F + 90.0F * ((float)j - 8.0F) / 7.0F;
               o = this.getAngle(n, p, j, h, 16.0F);
               arg2.translate(0.0F, l + 0.2F + (l - 0.2F) * (o - 90.0F) / 90.0F, 0.0F);
               arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-o));
            } else if ((float)i < 24.0F) {
               m = ((float)i - 16.0F) / 7.0F;
               n = 180.0F + 90.0F * m;
               p = 180.0F + 90.0F * ((float)j - 16.0F) / 7.0F;
               o = this.getAngle(n, p, j, h, 24.0F);
               arg2.translate(0.0F, l + l * (270.0F - o) / 90.0F, 0.0F);
               arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-o));
            } else if (i < 32) {
               m = ((float)i - 24.0F) / 7.0F;
               n = 270.0F + 90.0F * m;
               p = 270.0F + 90.0F * ((float)j - 24.0F) / 7.0F;
               o = this.getAngle(n, p, j, h, 32.0F);
               arg2.translate(0.0F, l * ((360.0F - o) / 90.0F), 0.0F);
               arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-o));
            }
         }
      }

      float q = arg.getSittingAnimationProgress(h);
      float r;
      if (q > 0.0F) {
         arg2.translate(0.0F, 0.8F * q, 0.0F);
         arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.lerp(q, arg.getPitch(), arg.getPitch() + 90.0F)));
         arg2.translate(0.0F, -1.0F * q, 0.0F);
         if (arg.isScaredByThunderstorm()) {
            r = (float)(Math.cos((double)arg.age * 1.25) * Math.PI * 0.05000000074505806);
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(r));
            if (arg.isBaby()) {
               arg2.translate(0.0F, 0.8F, 0.55F);
            }
         }
      }

      r = arg.getLieOnBackAnimationProgress(h);
      if (r > 0.0F) {
         k = arg.isBaby() ? 0.5F : 1.3F;
         arg2.translate(0.0F, k * r, 0.0F);
         arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.lerp(r, arg.getPitch(), arg.getPitch() + 180.0F)));
      }

   }

   private float getAngle(float f, float g, int i, float h, float j) {
      return (float)i < j ? MathHelper.lerp(h, f, g) : f;
   }
}
