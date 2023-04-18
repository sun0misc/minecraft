package net.minecraft.client.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public final class ModelPart {
   public static final float field_37937 = 1.0F;
   public float pivotX;
   public float pivotY;
   public float pivotZ;
   public float pitch;
   public float yaw;
   public float roll;
   public float xScale = 1.0F;
   public float yScale = 1.0F;
   public float zScale = 1.0F;
   public boolean visible = true;
   public boolean hidden;
   private final List cuboids;
   private final Map children;
   private ModelTransform defaultTransform;

   public ModelPart(List cuboids, Map children) {
      this.defaultTransform = ModelTransform.NONE;
      this.cuboids = cuboids;
      this.children = children;
   }

   public ModelTransform getTransform() {
      return ModelTransform.of(this.pivotX, this.pivotY, this.pivotZ, this.pitch, this.yaw, this.roll);
   }

   public ModelTransform getDefaultTransform() {
      return this.defaultTransform;
   }

   public void setDefaultTransform(ModelTransform transform) {
      this.defaultTransform = transform;
   }

   public void resetTransform() {
      this.setTransform(this.defaultTransform);
   }

   public void setTransform(ModelTransform rotationData) {
      this.pivotX = rotationData.pivotX;
      this.pivotY = rotationData.pivotY;
      this.pivotZ = rotationData.pivotZ;
      this.pitch = rotationData.pitch;
      this.yaw = rotationData.yaw;
      this.roll = rotationData.roll;
      this.xScale = 1.0F;
      this.yScale = 1.0F;
      this.zScale = 1.0F;
   }

   public void copyTransform(ModelPart part) {
      this.xScale = part.xScale;
      this.yScale = part.yScale;
      this.zScale = part.zScale;
      this.pitch = part.pitch;
      this.yaw = part.yaw;
      this.roll = part.roll;
      this.pivotX = part.pivotX;
      this.pivotY = part.pivotY;
      this.pivotZ = part.pivotZ;
   }

   public boolean hasChild(String child) {
      return this.children.containsKey(child);
   }

   public ModelPart getChild(String name) {
      ModelPart lv = (ModelPart)this.children.get(name);
      if (lv == null) {
         throw new NoSuchElementException("Can't find part " + name);
      } else {
         return lv;
      }
   }

   public void setPivot(float x, float y, float z) {
      this.pivotX = x;
      this.pivotY = y;
      this.pivotZ = z;
   }

   public void setAngles(float pitch, float yaw, float roll) {
      this.pitch = pitch;
      this.yaw = yaw;
      this.roll = roll;
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
      this.render(matrices, vertices, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      if (this.visible) {
         if (!this.cuboids.isEmpty() || !this.children.isEmpty()) {
            matrices.push();
            this.rotate(matrices);
            if (!this.hidden) {
               this.renderCuboids(matrices.peek(), vertices, light, overlay, red, green, blue, alpha);
            }

            Iterator var9 = this.children.values().iterator();

            while(var9.hasNext()) {
               ModelPart lv = (ModelPart)var9.next();
               lv.render(matrices, vertices, light, overlay, red, green, blue, alpha);
            }

            matrices.pop();
         }
      }
   }

   public void forEachCuboid(MatrixStack matrices, CuboidConsumer consumer) {
      this.forEachCuboid(matrices, consumer, "");
   }

   private void forEachCuboid(MatrixStack matrices, CuboidConsumer consumer, String path) {
      if (!this.cuboids.isEmpty() || !this.children.isEmpty()) {
         matrices.push();
         this.rotate(matrices);
         MatrixStack.Entry lv = matrices.peek();

         for(int i = 0; i < this.cuboids.size(); ++i) {
            consumer.accept(lv, path, i, (Cuboid)this.cuboids.get(i));
         }

         String string2 = path + "/";
         this.children.forEach((name, part) -> {
            part.forEachCuboid(matrices, consumer, string2 + name);
         });
         matrices.pop();
      }
   }

   public void rotate(MatrixStack matrices) {
      matrices.translate(this.pivotX / 16.0F, this.pivotY / 16.0F, this.pivotZ / 16.0F);
      if (this.pitch != 0.0F || this.yaw != 0.0F || this.roll != 0.0F) {
         matrices.multiply((new Quaternionf()).rotationZYX(this.roll, this.yaw, this.pitch));
      }

      if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
         matrices.scale(this.xScale, this.yScale, this.zScale);
      }

   }

   private void renderCuboids(MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
      Iterator var9 = this.cuboids.iterator();

      while(var9.hasNext()) {
         Cuboid lv = (Cuboid)var9.next();
         lv.renderCuboid(entry, vertexConsumer, light, overlay, red, green, blue, alpha);
      }

   }

   public Cuboid getRandomCuboid(Random random) {
      return (Cuboid)this.cuboids.get(random.nextInt(this.cuboids.size()));
   }

   public boolean isEmpty() {
      return this.cuboids.isEmpty();
   }

   public void translate(Vector3f vec3f) {
      this.pivotX += vec3f.x();
      this.pivotY += vec3f.y();
      this.pivotZ += vec3f.z();
   }

   public void rotate(Vector3f vec3f) {
      this.pitch += vec3f.x();
      this.yaw += vec3f.y();
      this.roll += vec3f.z();
   }

   public void scale(Vector3f vec3f) {
      this.xScale += vec3f.x();
      this.yScale += vec3f.y();
      this.zScale += vec3f.z();
   }

   public Stream traverse() {
      return Stream.concat(Stream.of(this), this.children.values().stream().flatMap(ModelPart::traverse));
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface CuboidConsumer {
      void accept(MatrixStack.Entry matrix, String path, int index, Cuboid cuboid);
   }

   @Environment(EnvType.CLIENT)
   public static class Cuboid {
      private final Quad[] sides;
      public final float minX;
      public final float minY;
      public final float minZ;
      public final float maxX;
      public final float maxY;
      public final float maxZ;

      public Cuboid(int u, int v, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float extraX, float extraY, float extraZ, boolean mirror, float textureWidth, float textureHeight, Set set) {
         this.minX = x;
         this.minY = y;
         this.minZ = z;
         this.maxX = x + sizeX;
         this.maxY = y + sizeY;
         this.maxZ = z + sizeZ;
         this.sides = new Quad[set.size()];
         float s = x + sizeX;
         float t = y + sizeY;
         float u = z + sizeZ;
         x -= extraX;
         y -= extraY;
         z -= extraZ;
         s += extraX;
         t += extraY;
         u += extraZ;
         if (mirror) {
            float v = s;
            s = x;
            x = v;
         }

         Vertex lv = new Vertex(x, y, z, 0.0F, 0.0F);
         Vertex lv2 = new Vertex(s, y, z, 0.0F, 8.0F);
         Vertex lv3 = new Vertex(s, t, z, 8.0F, 8.0F);
         Vertex lv4 = new Vertex(x, t, z, 8.0F, 0.0F);
         Vertex lv5 = new Vertex(x, y, u, 0.0F, 0.0F);
         Vertex lv6 = new Vertex(s, y, u, 0.0F, 8.0F);
         Vertex lv7 = new Vertex(s, t, u, 8.0F, 8.0F);
         Vertex lv8 = new Vertex(x, t, u, 8.0F, 0.0F);
         float w = (float)u;
         float x = (float)u + sizeZ;
         float y = (float)u + sizeZ + sizeX;
         float z = (float)u + sizeZ + sizeX + sizeX;
         float aa = (float)u + sizeZ + sizeX + sizeZ;
         float ab = (float)u + sizeZ + sizeX + sizeZ + sizeX;
         float ac = (float)v;
         float ad = (float)v + sizeZ;
         float ae = (float)v + sizeZ + sizeY;
         int af = 0;
         if (set.contains(Direction.DOWN)) {
            this.sides[af++] = new Quad(new Vertex[]{lv6, lv5, lv, lv2}, x, ac, y, ad, textureWidth, textureHeight, mirror, Direction.DOWN);
         }

         if (set.contains(Direction.UP)) {
            this.sides[af++] = new Quad(new Vertex[]{lv3, lv4, lv8, lv7}, y, ad, z, ac, textureWidth, textureHeight, mirror, Direction.UP);
         }

         if (set.contains(Direction.WEST)) {
            this.sides[af++] = new Quad(new Vertex[]{lv, lv5, lv8, lv4}, w, ad, x, ae, textureWidth, textureHeight, mirror, Direction.WEST);
         }

         if (set.contains(Direction.NORTH)) {
            this.sides[af++] = new Quad(new Vertex[]{lv2, lv, lv4, lv3}, x, ad, y, ae, textureWidth, textureHeight, mirror, Direction.NORTH);
         }

         if (set.contains(Direction.EAST)) {
            this.sides[af++] = new Quad(new Vertex[]{lv6, lv2, lv3, lv7}, y, ad, aa, ae, textureWidth, textureHeight, mirror, Direction.EAST);
         }

         if (set.contains(Direction.SOUTH)) {
            this.sides[af] = new Quad(new Vertex[]{lv5, lv6, lv7, lv8}, aa, ad, ab, ae, textureWidth, textureHeight, mirror, Direction.SOUTH);
         }

      }

      public void renderCuboid(MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
         Matrix4f matrix4f = entry.getPositionMatrix();
         Matrix3f matrix3f = entry.getNormalMatrix();
         Quad[] var11 = this.sides;
         int var12 = var11.length;

         for(int var13 = 0; var13 < var12; ++var13) {
            Quad lv = var11[var13];
            Vector3f vector3f = matrix3f.transform(new Vector3f(lv.direction));
            float l = vector3f.x();
            float m = vector3f.y();
            float n = vector3f.z();
            Vertex[] var19 = lv.vertices;
            int var20 = var19.length;

            for(int var21 = 0; var21 < var20; ++var21) {
               Vertex lv2 = var19[var21];
               float o = lv2.pos.x() / 16.0F;
               float p = lv2.pos.y() / 16.0F;
               float q = lv2.pos.z() / 16.0F;
               Vector4f vector4f = matrix4f.transform(new Vector4f(o, p, q, 1.0F));
               vertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha, lv2.u, lv2.v, overlay, light, l, m, n);
            }
         }

      }
   }

   @Environment(EnvType.CLIENT)
   static class Vertex {
      public final Vector3f pos;
      public final float u;
      public final float v;

      public Vertex(float x, float y, float z, float u, float v) {
         this(new Vector3f(x, y, z), u, v);
      }

      public Vertex remap(float u, float v) {
         return new Vertex(this.pos, u, v);
      }

      public Vertex(Vector3f pos, float u, float v) {
         this.pos = pos;
         this.u = u;
         this.v = v;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class Quad {
      public final Vertex[] vertices;
      public final Vector3f direction;

      public Quad(Vertex[] vertices, float u1, float v1, float u2, float v2, float squishU, float squishV, boolean flip, Direction direction) {
         this.vertices = vertices;
         float l = 0.0F / squishU;
         float m = 0.0F / squishV;
         vertices[0] = vertices[0].remap(u2 / squishU - l, v1 / squishV + m);
         vertices[1] = vertices[1].remap(u1 / squishU + l, v1 / squishV + m);
         vertices[2] = vertices[2].remap(u1 / squishU + l, v2 / squishV - m);
         vertices[3] = vertices[3].remap(u2 / squishU - l, v2 / squishV - m);
         if (flip) {
            int n = vertices.length;

            for(int o = 0; o < n / 2; ++o) {
               Vertex lv = vertices[o];
               vertices[o] = vertices[n - 1 - o];
               vertices[n - 1 - o] = lv;
            }
         }

         this.direction = direction.getUnitVector();
         if (flip) {
            this.direction.mul(-1.0F, 1.0F, 1.0F);
         }

      }
   }
}
