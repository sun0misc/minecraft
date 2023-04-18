package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class IllusionerEntityRenderer extends IllagerEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/illager/illusioner.png");

   public IllusionerEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new IllagerEntityModel(arg.getPart(EntityModelLayers.ILLUSIONER)), 0.5F);
      this.addFeature(new HeldItemFeatureRenderer(this, arg.getHeldItemRenderer()) {
         public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, IllusionerEntity arg3, float f, float g, float h, float j, float k, float l) {
            if (arg3.isSpellcasting() || arg3.isAttacking()) {
               super.render(arg, arg2, i, (LivingEntity)arg3, f, g, h, j, k, l);
            }

         }
      });
      ((IllagerEntityModel)this.model).getHat().visible = true;
   }

   public Identifier getTexture(IllusionerEntity arg) {
      return TEXTURE;
   }

   public void render(IllusionerEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      if (arg.isInvisible()) {
         Vec3d[] lvs = arg.getMirrorCopyOffsets(g);
         float h = this.getAnimationProgress(arg, g);

         for(int j = 0; j < lvs.length; ++j) {
            arg2.push();
            arg2.translate(lvs[j].x + (double)MathHelper.cos((float)j + h * 0.5F) * 0.025, lvs[j].y + (double)MathHelper.cos((float)j + h * 0.75F) * 0.0125, lvs[j].z + (double)MathHelper.cos((float)j + h * 0.7F) * 0.025);
            super.render(arg, f, g, arg2, arg3, i);
            arg2.pop();
         }
      } else {
         super.render(arg, f, g, arg2, arg3, i);
      }

   }

   protected boolean isVisible(IllusionerEntity arg) {
      return true;
   }

   // $FF: synthetic method
   protected boolean isVisible(LivingEntity entity) {
      return this.isVisible((IllusionerEntity)entity);
   }
}
