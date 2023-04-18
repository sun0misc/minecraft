package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.PaintingManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class PaintingEntityRenderer extends EntityRenderer {
   public PaintingEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
   }

   public void render(PaintingEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      arg2.push();
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - f));
      PaintingVariant lv = (PaintingVariant)arg.getVariant().value();
      float h = 0.0625F;
      arg2.scale(0.0625F, 0.0625F, 0.0625F);
      VertexConsumer lv2 = arg3.getBuffer(RenderLayer.getEntitySolid(this.getTexture(arg)));
      PaintingManager lv3 = MinecraftClient.getInstance().getPaintingManager();
      this.renderPainting(arg2, lv2, arg, lv.getWidth(), lv.getHeight(), lv3.getPaintingSprite(lv), lv3.getBackSprite());
      arg2.pop();
      super.render(arg, f, g, arg2, arg3, i);
   }

   public Identifier getTexture(PaintingEntity arg) {
      return MinecraftClient.getInstance().getPaintingManager().getBackSprite().getAtlasId();
   }

   private void renderPainting(MatrixStack matrices, VertexConsumer vertexConsumer, PaintingEntity entity, int width, int height, Sprite paintingSprite, Sprite backSprite) {
      MatrixStack.Entry lv = matrices.peek();
      Matrix4f matrix4f = lv.getPositionMatrix();
      Matrix3f matrix3f = lv.getNormalMatrix();
      float f = (float)(-width) / 2.0F;
      float g = (float)(-height) / 2.0F;
      float h = 0.5F;
      float k = backSprite.getMinU();
      float l = backSprite.getMaxU();
      float m = backSprite.getMinV();
      float n = backSprite.getMaxV();
      float o = backSprite.getMinU();
      float p = backSprite.getMaxU();
      float q = backSprite.getMinV();
      float r = backSprite.getFrameV(1.0);
      float s = backSprite.getMinU();
      float t = backSprite.getFrameU(1.0);
      float u = backSprite.getMinV();
      float v = backSprite.getMaxV();
      int w = width / 16;
      int x = height / 16;
      double d = 16.0 / (double)w;
      double e = 16.0 / (double)x;

      for(int y = 0; y < w; ++y) {
         for(int z = 0; z < x; ++z) {
            float aa = f + (float)((y + 1) * 16);
            float ab = f + (float)(y * 16);
            float ac = g + (float)((z + 1) * 16);
            float ad = g + (float)(z * 16);
            int ae = entity.getBlockX();
            int af = MathHelper.floor(entity.getY() + (double)((ac + ad) / 2.0F / 16.0F));
            int ag = entity.getBlockZ();
            Direction lv2 = entity.getHorizontalFacing();
            if (lv2 == Direction.NORTH) {
               ae = MathHelper.floor(entity.getX() + (double)((aa + ab) / 2.0F / 16.0F));
            }

            if (lv2 == Direction.WEST) {
               ag = MathHelper.floor(entity.getZ() - (double)((aa + ab) / 2.0F / 16.0F));
            }

            if (lv2 == Direction.SOUTH) {
               ae = MathHelper.floor(entity.getX() - (double)((aa + ab) / 2.0F / 16.0F));
            }

            if (lv2 == Direction.EAST) {
               ag = MathHelper.floor(entity.getZ() + (double)((aa + ab) / 2.0F / 16.0F));
            }

            int ah = WorldRenderer.getLightmapCoordinates(entity.world, new BlockPos(ae, af, ag));
            float ai = paintingSprite.getFrameU(d * (double)(w - y));
            float aj = paintingSprite.getFrameU(d * (double)(w - (y + 1)));
            float ak = paintingSprite.getFrameV(e * (double)(x - z));
            float al = paintingSprite.getFrameV(e * (double)(x - (z + 1)));
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, aj, ak, -0.5F, 0, 0, -1, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, ai, ak, -0.5F, 0, 0, -1, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, ai, al, -0.5F, 0, 0, -1, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, aj, al, -0.5F, 0, 0, -1, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, l, m, 0.5F, 0, 0, 1, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, k, m, 0.5F, 0, 0, 1, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, k, n, 0.5F, 0, 0, 1, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, l, n, 0.5F, 0, 0, 1, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, o, q, -0.5F, 0, 1, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, p, q, -0.5F, 0, 1, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, p, r, 0.5F, 0, 1, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, o, r, 0.5F, 0, 1, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, o, q, 0.5F, 0, -1, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, p, q, 0.5F, 0, -1, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, p, r, -0.5F, 0, -1, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, o, r, -0.5F, 0, -1, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, t, u, 0.5F, -1, 0, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, t, v, 0.5F, -1, 0, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, s, v, -0.5F, -1, 0, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, s, u, -0.5F, -1, 0, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, t, u, -0.5F, 1, 0, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, t, v, -0.5F, 1, 0, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, s, v, 0.5F, 1, 0, 0, ah);
            this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, s, u, 0.5F, 1, 0, 0, ah);
         }
      }

   }

   private void vertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int light) {
      vertexConsumer.vertex(positionMatrix, x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, (float)normalX, (float)normalY, (float)normalZ).next();
   }
}
