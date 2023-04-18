package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.SlimeOverlayFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SlimeEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/slime/slime.png");

   public SlimeEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new SlimeEntityModel(arg.getPart(EntityModelLayers.SLIME)), 0.25F);
      this.addFeature(new SlimeOverlayFeatureRenderer(this, arg.getModelLoader()));
   }

   public void render(SlimeEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      this.shadowRadius = 0.25F * (float)arg.getSize();
      super.render((MobEntity)arg, f, g, arg2, arg3, i);
   }

   protected void scale(SlimeEntity arg, MatrixStack arg2, float f) {
      float g = 0.999F;
      arg2.scale(0.999F, 0.999F, 0.999F);
      arg2.translate(0.0F, 0.001F, 0.0F);
      float h = (float)arg.getSize();
      float i = MathHelper.lerp(f, arg.lastStretch, arg.stretch) / (h * 0.5F + 1.0F);
      float j = 1.0F / (i + 1.0F);
      arg2.scale(j * h, 1.0F / j * h, j * h);
   }

   public Identifier getTexture(SlimeEntity arg) {
      return TEXTURE;
   }
}
