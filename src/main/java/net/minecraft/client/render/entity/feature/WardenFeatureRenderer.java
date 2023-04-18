package net.minecraft.client.render.entity.feature;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.WardenEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class WardenFeatureRenderer extends FeatureRenderer {
   private final Identifier texture;
   private final AnimationAngleAdjuster animationAngleAdjuster;
   private final ModelPartVisibility modelPartVisibility;

   public WardenFeatureRenderer(FeatureRendererContext context, Identifier texture, AnimationAngleAdjuster animationAngleAdjuster, ModelPartVisibility modelPartVisibility) {
      super(context);
      this.texture = texture;
      this.animationAngleAdjuster = animationAngleAdjuster;
      this.modelPartVisibility = modelPartVisibility;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, WardenEntity arg3, float f, float g, float h, float j, float k, float l) {
      if (!arg3.isInvisible()) {
         this.updateModelPartVisibility();
         VertexConsumer lv = arg2.getBuffer(RenderLayer.getEntityTranslucentEmissive(this.texture));
         ((WardenEntityModel)this.getContextModel()).render(arg, lv, i, LivingEntityRenderer.getOverlay(arg3, 0.0F), 1.0F, 1.0F, 1.0F, this.animationAngleAdjuster.apply(arg3, h, j));
         this.unhideAllModelParts();
      }
   }

   private void updateModelPartVisibility() {
      List list = this.modelPartVisibility.getPartsToDraw((WardenEntityModel)this.getContextModel());
      ((WardenEntityModel)this.getContextModel()).getPart().traverse().forEach((part) -> {
         part.hidden = true;
      });
      list.forEach((part) -> {
         part.hidden = false;
      });
   }

   private void unhideAllModelParts() {
      ((WardenEntityModel)this.getContextModel()).getPart().traverse().forEach((part) -> {
         part.hidden = false;
      });
   }

   @Environment(EnvType.CLIENT)
   public interface AnimationAngleAdjuster {
      float apply(WardenEntity warden, float tickDelta, float animationProgress);
   }

   @Environment(EnvType.CLIENT)
   public interface ModelPartVisibility {
      List getPartsToDraw(EntityModel model);
   }
}
