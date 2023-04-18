package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class CubeMapRenderer {
   private static final int FACES_COUNT = 6;
   private final Identifier[] faces = new Identifier[6];

   public CubeMapRenderer(Identifier faces) {
      for(int i = 0; i < 6; ++i) {
         Identifier[] var10000 = this.faces;
         String var10003 = faces.getPath();
         var10000[i] = faces.withPath(var10003 + "_" + i + ".png");
      }

   }

   public void draw(MinecraftClient client, float x, float y, float alpha) {
      Tessellator lv = Tessellator.getInstance();
      BufferBuilder lv2 = lv.getBuffer();
      Matrix4f matrix4f = (new Matrix4f()).setPerspective(1.4835298F, (float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight(), 0.05F, 10.0F);
      RenderSystem.backupProjectionMatrix();
      RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_DISTANCE);
      MatrixStack lv3 = RenderSystem.getModelViewStack();
      lv3.push();
      lv3.loadIdentity();
      lv3.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
      RenderSystem.applyModelViewMatrix();
      RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.depthMask(false);
      int i = true;

      for(int j = 0; j < 4; ++j) {
         lv3.push();
         float k = ((float)(j % 2) / 2.0F - 0.5F) / 256.0F;
         float l = ((float)(j / 2) / 2.0F - 0.5F) / 256.0F;
         float m = 0.0F;
         lv3.translate(k, l, 0.0F);
         lv3.multiply(RotationAxis.POSITIVE_X.rotationDegrees(x));
         lv3.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(y));
         RenderSystem.applyModelViewMatrix();

         for(int n = 0; n < 6; ++n) {
            RenderSystem.setShaderTexture(0, this.faces[n]);
            lv2.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            int o = Math.round(255.0F * alpha) / (j + 1);
            if (n == 0) {
               lv2.vertex(-1.0, -1.0, 1.0).texture(0.0F, 0.0F).color(255, 255, 255, o).next();
               lv2.vertex(-1.0, 1.0, 1.0).texture(0.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(1.0, 1.0, 1.0).texture(1.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(1.0, -1.0, 1.0).texture(1.0F, 0.0F).color(255, 255, 255, o).next();
            }

            if (n == 1) {
               lv2.vertex(1.0, -1.0, 1.0).texture(0.0F, 0.0F).color(255, 255, 255, o).next();
               lv2.vertex(1.0, 1.0, 1.0).texture(0.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(1.0, 1.0, -1.0).texture(1.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(1.0, -1.0, -1.0).texture(1.0F, 0.0F).color(255, 255, 255, o).next();
            }

            if (n == 2) {
               lv2.vertex(1.0, -1.0, -1.0).texture(0.0F, 0.0F).color(255, 255, 255, o).next();
               lv2.vertex(1.0, 1.0, -1.0).texture(0.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(-1.0, 1.0, -1.0).texture(1.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(-1.0, -1.0, -1.0).texture(1.0F, 0.0F).color(255, 255, 255, o).next();
            }

            if (n == 3) {
               lv2.vertex(-1.0, -1.0, -1.0).texture(0.0F, 0.0F).color(255, 255, 255, o).next();
               lv2.vertex(-1.0, 1.0, -1.0).texture(0.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(-1.0, 1.0, 1.0).texture(1.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(-1.0, -1.0, 1.0).texture(1.0F, 0.0F).color(255, 255, 255, o).next();
            }

            if (n == 4) {
               lv2.vertex(-1.0, -1.0, -1.0).texture(0.0F, 0.0F).color(255, 255, 255, o).next();
               lv2.vertex(-1.0, -1.0, 1.0).texture(0.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(1.0, -1.0, 1.0).texture(1.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(1.0, -1.0, -1.0).texture(1.0F, 0.0F).color(255, 255, 255, o).next();
            }

            if (n == 5) {
               lv2.vertex(-1.0, 1.0, 1.0).texture(0.0F, 0.0F).color(255, 255, 255, o).next();
               lv2.vertex(-1.0, 1.0, -1.0).texture(0.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(1.0, 1.0, -1.0).texture(1.0F, 1.0F).color(255, 255, 255, o).next();
               lv2.vertex(1.0, 1.0, 1.0).texture(1.0F, 0.0F).color(255, 255, 255, o).next();
            }

            lv.draw();
         }

         lv3.pop();
         RenderSystem.applyModelViewMatrix();
         RenderSystem.colorMask(true, true, true, false);
      }

      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.restoreProjectionMatrix();
      lv3.pop();
      RenderSystem.applyModelViewMatrix();
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
   }

   public CompletableFuture loadTexturesAsync(TextureManager textureManager, Executor executor) {
      CompletableFuture[] completableFutures = new CompletableFuture[6];

      for(int i = 0; i < completableFutures.length; ++i) {
         completableFutures[i] = textureManager.loadTextureAsync(this.faces[i], executor);
      }

      return CompletableFuture.allOf(completableFutures);
   }
}
