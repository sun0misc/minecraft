package net.minecraft.client.render;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexConsumers {
   public static VertexConsumer union() {
      throw new IllegalArgumentException();
   }

   public static VertexConsumer union(VertexConsumer first) {
      return first;
   }

   public static VertexConsumer union(VertexConsumer first, VertexConsumer second) {
      return new Dual(first, second);
   }

   public static VertexConsumer union(VertexConsumer... delegates) {
      return new Union(delegates);
   }

   @Environment(EnvType.CLIENT)
   static class Dual implements VertexConsumer {
      private final VertexConsumer first;
      private final VertexConsumer second;

      public Dual(VertexConsumer first, VertexConsumer second) {
         if (first == second) {
            throw new IllegalArgumentException("Duplicate delegates");
         } else {
            this.first = first;
            this.second = second;
         }
      }

      public VertexConsumer vertex(double x, double y, double z) {
         this.first.vertex(x, y, z);
         this.second.vertex(x, y, z);
         return this;
      }

      public VertexConsumer color(int red, int green, int blue, int alpha) {
         this.first.color(red, green, blue, alpha);
         this.second.color(red, green, blue, alpha);
         return this;
      }

      public VertexConsumer texture(float u, float v) {
         this.first.texture(u, v);
         this.second.texture(u, v);
         return this;
      }

      public VertexConsumer overlay(int u, int v) {
         this.first.overlay(u, v);
         this.second.overlay(u, v);
         return this;
      }

      public VertexConsumer light(int u, int v) {
         this.first.light(u, v);
         this.second.light(u, v);
         return this;
      }

      public VertexConsumer normal(float x, float y, float z) {
         this.first.normal(x, y, z);
         this.second.normal(x, y, z);
         return this;
      }

      public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
         this.first.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
         this.second.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
      }

      public void next() {
         this.first.next();
         this.second.next();
      }

      public void fixedColor(int red, int green, int blue, int alpha) {
         this.first.fixedColor(red, green, blue, alpha);
         this.second.fixedColor(red, green, blue, alpha);
      }

      public void unfixColor() {
         this.first.unfixColor();
         this.second.unfixColor();
      }
   }

   @Environment(EnvType.CLIENT)
   private static class Union implements VertexConsumer {
      private final VertexConsumer[] delegates;

      public Union(VertexConsumer[] delegates) {
         for(int i = 0; i < delegates.length; ++i) {
            for(int j = i + 1; j < delegates.length; ++j) {
               if (delegates[i] == delegates[j]) {
                  throw new IllegalArgumentException("Duplicate delegates");
               }
            }
         }

         this.delegates = delegates;
      }

      private void delegate(Consumer action) {
         VertexConsumer[] var2 = this.delegates;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            VertexConsumer lv = var2[var4];
            action.accept(lv);
         }

      }

      public VertexConsumer vertex(double x, double y, double z) {
         this.delegate((vertexConsumer) -> {
            vertexConsumer.vertex(x, y, z);
         });
         return this;
      }

      public VertexConsumer color(int red, int green, int blue, int alpha) {
         this.delegate((vertexConsumer) -> {
            vertexConsumer.color(red, green, blue, alpha);
         });
         return this;
      }

      public VertexConsumer texture(float u, float v) {
         this.delegate((vertexConsumer) -> {
            vertexConsumer.texture(u, v);
         });
         return this;
      }

      public VertexConsumer overlay(int u, int v) {
         this.delegate((vertexConsumer) -> {
            vertexConsumer.overlay(u, v);
         });
         return this;
      }

      public VertexConsumer light(int u, int v) {
         this.delegate((vertexConsumer) -> {
            vertexConsumer.light(u, v);
         });
         return this;
      }

      public VertexConsumer normal(float x, float y, float z) {
         this.delegate((vertexConsumer) -> {
            vertexConsumer.normal(x, y, z);
         });
         return this;
      }

      public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
         this.delegate((vertexConsumer) -> {
            vertexConsumer.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
         });
      }

      public void next() {
         this.delegate(VertexConsumer::next);
      }

      public void fixedColor(int red, int green, int blue, int alpha) {
         this.delegate((vertexConsumer) -> {
            vertexConsumer.fixedColor(red, green, blue, alpha);
         });
      }

      public void unfixColor() {
         this.delegate(VertexConsumer::unfixColor);
      }
   }
}
