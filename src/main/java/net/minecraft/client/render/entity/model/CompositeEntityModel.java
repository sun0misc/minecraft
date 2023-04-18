package net.minecraft.client.render.entity.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public abstract class CompositeEntityModel extends EntityModel {
   public CompositeEntityModel() {
      this(RenderLayer::getEntityCutoutNoCull);
   }

   public CompositeEntityModel(Function function) {
      super(function);
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.getParts().forEach((part) -> {
         part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
      });
   }

   public abstract Iterable getParts();
}
