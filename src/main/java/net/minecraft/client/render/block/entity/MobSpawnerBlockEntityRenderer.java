package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.MobSpawnerLogic;

@Environment(EnvType.CLIENT)
public class MobSpawnerBlockEntityRenderer implements BlockEntityRenderer {
   private final EntityRenderDispatcher entityRenderDispatcher;

   public MobSpawnerBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      this.entityRenderDispatcher = ctx.getEntityRenderDispatcher();
   }

   public void render(MobSpawnerBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      arg2.push();
      arg2.translate(0.5F, 0.0F, 0.5F);
      MobSpawnerLogic lv = arg.getLogic();
      Entity lv2 = lv.getRenderedEntity(arg.getWorld(), arg.getWorld().getRandom(), arg.getPos());
      if (lv2 != null) {
         float g = 0.53125F;
         float h = Math.max(lv2.getWidth(), lv2.getHeight());
         if ((double)h > 1.0) {
            g /= h;
         }

         arg2.translate(0.0F, 0.4F, 0.0F);
         arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)MathHelper.lerp((double)f, lv.method_8279(), lv.method_8278()) * 10.0F));
         arg2.translate(0.0F, -0.2F, 0.0F);
         arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30.0F));
         arg2.scale(g, g, g);
         this.entityRenderDispatcher.render(lv2, 0.0, 0.0, 0.0, 0.0F, f, arg2, arg3, i);
      }

      arg2.pop();
   }
}
