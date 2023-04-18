package net.minecraft.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.VertexBuffer;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BufferRenderer {
   @Nullable
   private static VertexBuffer currentVertexBuffer;

   public static void reset() {
      if (currentVertexBuffer != null) {
         resetCurrentVertexBuffer();
         VertexBuffer.unbind();
      }

   }

   public static void resetCurrentVertexBuffer() {
      currentVertexBuffer = null;
   }

   public static void drawWithGlobalProgram(BufferBuilder.BuiltBuffer buffer) {
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> {
            drawWithGlobalProgramInternal(buffer);
         });
      } else {
         drawWithGlobalProgramInternal(buffer);
      }

   }

   private static void drawWithGlobalProgramInternal(BufferBuilder.BuiltBuffer buffer) {
      VertexBuffer lv = upload(buffer);
      if (lv != null) {
         lv.draw(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
      }

   }

   public static void draw(BufferBuilder.BuiltBuffer buffer) {
      VertexBuffer lv = upload(buffer);
      if (lv != null) {
         lv.draw();
      }

   }

   @Nullable
   private static VertexBuffer upload(BufferBuilder.BuiltBuffer buffer) {
      RenderSystem.assertOnRenderThread();
      if (buffer.isEmpty()) {
         buffer.release();
         return null;
      } else {
         VertexBuffer lv = bind(buffer.getParameters().format());
         lv.upload(buffer);
         return lv;
      }
   }

   private static VertexBuffer bind(VertexFormat vertexFormat) {
      VertexBuffer lv = vertexFormat.getBuffer();
      bind(lv);
      return lv;
   }

   private static void bind(VertexBuffer vertexBuffer) {
      if (vertexBuffer != currentVertexBuffer) {
         vertexBuffer.bind();
         currentVertexBuffer = vertexBuffer;
      }

   }
}
