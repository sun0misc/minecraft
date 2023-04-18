package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.EndermanBlockFeatureRenderer;
import net.minecraft.client.render.entity.feature.EndermanEyesFeatureRenderer;
import net.minecraft.client.render.entity.model.EndermanEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class EndermanEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/enderman/enderman.png");
   private final Random random = Random.create();

   public EndermanEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new EndermanEntityModel(arg.getPart(EntityModelLayers.ENDERMAN)), 0.5F);
      this.addFeature(new EndermanEyesFeatureRenderer(this));
      this.addFeature(new EndermanBlockFeatureRenderer(this, arg.getBlockRenderManager()));
   }

   public void render(EndermanEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      BlockState lv = arg.getCarriedBlock();
      EndermanEntityModel lv2 = (EndermanEntityModel)this.getModel();
      lv2.carryingBlock = lv != null;
      lv2.angry = arg.isAngry();
      super.render((MobEntity)arg, f, g, arg2, arg3, i);
   }

   public Vec3d getPositionOffset(EndermanEntity arg, float f) {
      if (arg.isAngry()) {
         double d = 0.02;
         return new Vec3d(this.random.nextGaussian() * 0.02, 0.0, this.random.nextGaussian() * 0.02);
      } else {
         return super.getPositionOffset(arg, f);
      }
   }

   public Identifier getTexture(EndermanEntity arg) {
      return TEXTURE;
   }
}
