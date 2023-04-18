package net.minecraft.client.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class GlyphRenderer {
   private final RenderLayer textLayer;
   private final RenderLayer seeThroughTextLayer;
   private final RenderLayer polygonOffsetTextLayer;
   private final float minU;
   private final float maxU;
   private final float minV;
   private final float maxV;
   private final float minX;
   private final float maxX;
   private final float minY;
   private final float maxY;

   public GlyphRenderer(RenderLayer textLayer, RenderLayer seeThroughTextLayer, RenderLayer polygonOffsetTextLayer, float minU, float maxU, float minV, float maxV, float minX, float maxX, float minY, float maxY) {
      this.textLayer = textLayer;
      this.seeThroughTextLayer = seeThroughTextLayer;
      this.polygonOffsetTextLayer = polygonOffsetTextLayer;
      this.minU = minU;
      this.maxU = maxU;
      this.minV = minV;
      this.maxV = maxV;
      this.minX = minX;
      this.maxX = maxX;
      this.minY = minY;
      this.maxY = maxY;
   }

   public void draw(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light) {
      int m = true;
      float n = x + this.minX;
      float o = x + this.maxX;
      float p = this.minY - 3.0F;
      float q = this.maxY - 3.0F;
      float r = y + p;
      float s = y + q;
      float t = italic ? 1.0F - 0.25F * p : 0.0F;
      float u = italic ? 1.0F - 0.25F * q : 0.0F;
      vertexConsumer.vertex(matrix, n + t, r, 0.0F).color(red, green, blue, alpha).texture(this.minU, this.minV).light(light).next();
      vertexConsumer.vertex(matrix, n + u, s, 0.0F).color(red, green, blue, alpha).texture(this.minU, this.maxV).light(light).next();
      vertexConsumer.vertex(matrix, o + u, s, 0.0F).color(red, green, blue, alpha).texture(this.maxU, this.maxV).light(light).next();
      vertexConsumer.vertex(matrix, o + t, r, 0.0F).color(red, green, blue, alpha).texture(this.maxU, this.minV).light(light).next();
   }

   public void drawRectangle(Rectangle rectangle, Matrix4f matrix, VertexConsumer vertexConsumer, int light) {
      vertexConsumer.vertex(matrix, rectangle.minX, rectangle.minY, rectangle.zIndex).color(rectangle.red, rectangle.green, rectangle.blue, rectangle.alpha).texture(this.minU, this.minV).light(light).next();
      vertexConsumer.vertex(matrix, rectangle.maxX, rectangle.minY, rectangle.zIndex).color(rectangle.red, rectangle.green, rectangle.blue, rectangle.alpha).texture(this.minU, this.maxV).light(light).next();
      vertexConsumer.vertex(matrix, rectangle.maxX, rectangle.maxY, rectangle.zIndex).color(rectangle.red, rectangle.green, rectangle.blue, rectangle.alpha).texture(this.maxU, this.maxV).light(light).next();
      vertexConsumer.vertex(matrix, rectangle.minX, rectangle.maxY, rectangle.zIndex).color(rectangle.red, rectangle.green, rectangle.blue, rectangle.alpha).texture(this.maxU, this.minV).light(light).next();
   }

   public RenderLayer getLayer(TextRenderer.TextLayerType layerType) {
      RenderLayer var10000;
      switch (layerType) {
         case NORMAL:
            var10000 = this.textLayer;
            break;
         case SEE_THROUGH:
            var10000 = this.seeThroughTextLayer;
            break;
         case POLYGON_OFFSET:
            var10000 = this.polygonOffsetTextLayer;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   @Environment(EnvType.CLIENT)
   public static class Rectangle {
      protected final float minX;
      protected final float minY;
      protected final float maxX;
      protected final float maxY;
      protected final float zIndex;
      protected final float red;
      protected final float green;
      protected final float blue;
      protected final float alpha;

      public Rectangle(float minX, float minY, float maxX, float maxY, float zIndex, float red, float green, float blue, float alpha) {
         this.minX = minX;
         this.minY = minY;
         this.maxX = maxX;
         this.maxY = maxY;
         this.zIndex = zIndex;
         this.red = red;
         this.green = green;
         this.blue = blue;
         this.alpha = alpha;
      }
   }
}
