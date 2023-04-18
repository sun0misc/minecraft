package net.minecraft.client.render.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ChunkBorderDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;
   private static final int DARK_CYAN = ColorHelper.Argb.getArgb(255, 0, 155, 155);
   private static final int YELLOW = ColorHelper.Argb.getArgb(255, 255, 255, 0);

   public ChunkBorderDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      Entity lv = this.client.gameRenderer.getCamera().getFocusedEntity();
      float g = (float)((double)this.client.world.getBottomY() - cameraY);
      float h = (float)((double)this.client.world.getTopY() - cameraY);
      ChunkPos lv2 = lv.getChunkPos();
      float i = (float)((double)lv2.getStartX() - cameraX);
      float j = (float)((double)lv2.getStartZ() - cameraZ);
      VertexConsumer lv3 = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(1.0));
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();

      int k;
      int l;
      for(k = -16; k <= 32; k += 16) {
         for(l = -16; l <= 32; l += 16) {
            lv3.vertex(matrix4f, i + (float)k, g, j + (float)l).color(1.0F, 0.0F, 0.0F, 0.0F).next();
            lv3.vertex(matrix4f, i + (float)k, g, j + (float)l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
            lv3.vertex(matrix4f, i + (float)k, h, j + (float)l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
            lv3.vertex(matrix4f, i + (float)k, h, j + (float)l).color(1.0F, 0.0F, 0.0F, 0.0F).next();
         }
      }

      for(k = 2; k < 16; k += 2) {
         l = k % 4 == 0 ? DARK_CYAN : YELLOW;
         lv3.vertex(matrix4f, i + (float)k, g, j).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         lv3.vertex(matrix4f, i + (float)k, g, j).color(l).next();
         lv3.vertex(matrix4f, i + (float)k, h, j).color(l).next();
         lv3.vertex(matrix4f, i + (float)k, h, j).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         lv3.vertex(matrix4f, i + (float)k, g, j + 16.0F).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         lv3.vertex(matrix4f, i + (float)k, g, j + 16.0F).color(l).next();
         lv3.vertex(matrix4f, i + (float)k, h, j + 16.0F).color(l).next();
         lv3.vertex(matrix4f, i + (float)k, h, j + 16.0F).color(1.0F, 1.0F, 0.0F, 0.0F).next();
      }

      for(k = 2; k < 16; k += 2) {
         l = k % 4 == 0 ? DARK_CYAN : YELLOW;
         lv3.vertex(matrix4f, i, g, j + (float)k).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         lv3.vertex(matrix4f, i, g, j + (float)k).color(l).next();
         lv3.vertex(matrix4f, i, h, j + (float)k).color(l).next();
         lv3.vertex(matrix4f, i, h, j + (float)k).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         lv3.vertex(matrix4f, i + 16.0F, g, j + (float)k).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         lv3.vertex(matrix4f, i + 16.0F, g, j + (float)k).color(l).next();
         lv3.vertex(matrix4f, i + 16.0F, h, j + (float)k).color(l).next();
         lv3.vertex(matrix4f, i + 16.0F, h, j + (float)k).color(1.0F, 1.0F, 0.0F, 0.0F).next();
      }

      float m;
      for(k = this.client.world.getBottomY(); k <= this.client.world.getTopY(); k += 2) {
         m = (float)((double)k - cameraY);
         int n = k % 8 == 0 ? DARK_CYAN : YELLOW;
         lv3.vertex(matrix4f, i, m, j).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         lv3.vertex(matrix4f, i, m, j).color(n).next();
         lv3.vertex(matrix4f, i, m, j + 16.0F).color(n).next();
         lv3.vertex(matrix4f, i + 16.0F, m, j + 16.0F).color(n).next();
         lv3.vertex(matrix4f, i + 16.0F, m, j).color(n).next();
         lv3.vertex(matrix4f, i, m, j).color(n).next();
         lv3.vertex(matrix4f, i, m, j).color(1.0F, 1.0F, 0.0F, 0.0F).next();
      }

      lv3 = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(2.0));

      for(k = 0; k <= 16; k += 16) {
         for(l = 0; l <= 16; l += 16) {
            lv3.vertex(matrix4f, i + (float)k, g, j + (float)l).color(0.25F, 0.25F, 1.0F, 0.0F).next();
            lv3.vertex(matrix4f, i + (float)k, g, j + (float)l).color(0.25F, 0.25F, 1.0F, 1.0F).next();
            lv3.vertex(matrix4f, i + (float)k, h, j + (float)l).color(0.25F, 0.25F, 1.0F, 1.0F).next();
            lv3.vertex(matrix4f, i + (float)k, h, j + (float)l).color(0.25F, 0.25F, 1.0F, 0.0F).next();
         }
      }

      for(k = this.client.world.getBottomY(); k <= this.client.world.getTopY(); k += 16) {
         m = (float)((double)k - cameraY);
         lv3.vertex(matrix4f, i, m, j).color(0.25F, 0.25F, 1.0F, 0.0F).next();
         lv3.vertex(matrix4f, i, m, j).color(0.25F, 0.25F, 1.0F, 1.0F).next();
         lv3.vertex(matrix4f, i, m, j + 16.0F).color(0.25F, 0.25F, 1.0F, 1.0F).next();
         lv3.vertex(matrix4f, i + 16.0F, m, j + 16.0F).color(0.25F, 0.25F, 1.0F, 1.0F).next();
         lv3.vertex(matrix4f, i + 16.0F, m, j).color(0.25F, 0.25F, 1.0F, 1.0F).next();
         lv3.vertex(matrix4f, i, m, j).color(0.25F, 0.25F, 1.0F, 1.0F).next();
         lv3.vertex(matrix4f, i, m, j).color(0.25F, 0.25F, 1.0F, 0.0F).next();
      }

   }
}
