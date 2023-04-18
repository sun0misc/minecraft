package net.minecraft.client.render.entity;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.CatCollarFeatureRenderer;
import net.minecraft.client.render.entity.model.CatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class CatEntityRenderer extends MobEntityRenderer {
   public CatEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new CatEntityModel(arg.getPart(EntityModelLayers.CAT)), 0.4F);
      this.addFeature(new CatCollarFeatureRenderer(this, arg.getModelLoader()));
   }

   public Identifier getTexture(CatEntity arg) {
      return arg.getTexture();
   }

   protected void scale(CatEntity arg, MatrixStack arg2, float f) {
      super.scale(arg, arg2, f);
      arg2.scale(0.8F, 0.8F, 0.8F);
   }

   protected void setupTransforms(CatEntity arg, MatrixStack arg2, float f, float g, float h) {
      super.setupTransforms(arg, arg2, f, g, h);
      float i = arg.getSleepAnimation(h);
      if (i > 0.0F) {
         arg2.translate(0.4F * i, 0.15F * i, 0.1F * i);
         arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerpAngleDegrees(i, 0.0F, 90.0F)));
         BlockPos lv = arg.getBlockPos();
         List list = arg.world.getNonSpectatingEntities(PlayerEntity.class, (new Box(lv)).expand(2.0, 2.0, 2.0));
         Iterator var9 = list.iterator();

         while(var9.hasNext()) {
            PlayerEntity lv2 = (PlayerEntity)var9.next();
            if (lv2.isSleeping()) {
               arg2.translate(0.15F * i, 0.0F, 0.0F);
               break;
            }
         }
      }

   }
}
