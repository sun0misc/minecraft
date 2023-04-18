package net.minecraft.client.gl;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Util;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public abstract class Framebuffer {
   private static final int field_31901 = 0;
   private static final int field_31902 = 1;
   private static final int field_31903 = 2;
   private static final int field_31904 = 3;
   public int textureWidth;
   public int textureHeight;
   public int viewportWidth;
   public int viewportHeight;
   public final boolean useDepthAttachment;
   public int fbo;
   protected int colorAttachment;
   protected int depthAttachment;
   private final float[] clearColor = (float[])Util.make(() -> {
      float[] fs = new float[]{1.0F, 1.0F, 1.0F, 0.0F};
      return fs;
   });
   public int texFilter;

   public Framebuffer(boolean useDepth) {
      this.useDepthAttachment = useDepth;
      this.fbo = -1;
      this.colorAttachment = -1;
      this.depthAttachment = -1;
   }

   public void resize(int width, int height, boolean getError) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this.resizeInternal(width, height, getError);
         });
      } else {
         this.resizeInternal(width, height, getError);
      }

   }

   private void resizeInternal(int width, int height, boolean getError) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._enableDepthTest();
      if (this.fbo >= 0) {
         this.delete();
      }

      this.initFbo(width, height, getError);
      GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
   }

   public void delete() {
      RenderSystem.assertOnRenderThreadOrInit();
      this.endRead();
      this.endWrite();
      if (this.depthAttachment > -1) {
         TextureUtil.releaseTextureId(this.depthAttachment);
         this.depthAttachment = -1;
      }

      if (this.colorAttachment > -1) {
         TextureUtil.releaseTextureId(this.colorAttachment);
         this.colorAttachment = -1;
      }

      if (this.fbo > -1) {
         GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
         GlStateManager._glDeleteFramebuffers(this.fbo);
         this.fbo = -1;
      }

   }

   public void copyDepthFrom(Framebuffer framebuffer) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, framebuffer.fbo);
      GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, this.fbo);
      GlStateManager._glBlitFrameBuffer(0, 0, framebuffer.textureWidth, framebuffer.textureHeight, 0, 0, this.textureWidth, this.textureHeight, 256, GlConst.GL_NEAREST);
      GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
   }

   public void initFbo(int width, int height, boolean getError) {
      RenderSystem.assertOnRenderThreadOrInit();
      int k = RenderSystem.maxSupportedTextureSize();
      if (width > 0 && width <= k && height > 0 && height <= k) {
         this.viewportWidth = width;
         this.viewportHeight = height;
         this.textureWidth = width;
         this.textureHeight = height;
         this.fbo = GlStateManager.glGenFramebuffers();
         this.colorAttachment = TextureUtil.generateTextureId();
         if (this.useDepthAttachment) {
            this.depthAttachment = TextureUtil.generateTextureId();
            GlStateManager._bindTexture(this.depthAttachment);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_COMPARE_MODE, 0);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
            GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GlConst.GL_DEPTH_COMPONENT, this.textureWidth, this.textureHeight, 0, GlConst.GL_DEPTH_COMPONENT, GlConst.GL_FLOAT, (IntBuffer)null);
         }

         this.setTexFilter(GlConst.GL_NEAREST);
         GlStateManager._bindTexture(this.colorAttachment);
         GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
         GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
         GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GlConst.GL_RGBA8, this.textureWidth, this.textureHeight, 0, GlConst.GL_RGBA, GlConst.GL_UNSIGNED_BYTE, (IntBuffer)null);
         GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this.fbo);
         GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, GlConst.GL_TEXTURE_2D, this.colorAttachment, 0);
         if (this.useDepthAttachment) {
            GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_DEPTH_ATTACHMENT, GlConst.GL_TEXTURE_2D, this.depthAttachment, 0);
         }

         this.checkFramebufferStatus();
         this.clear(getError);
         this.endRead();
      } else {
         throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + k + ")");
      }
   }

   public void setTexFilter(int texFilter) {
      RenderSystem.assertOnRenderThreadOrInit();
      this.texFilter = texFilter;
      GlStateManager._bindTexture(this.colorAttachment);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, texFilter);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, texFilter);
      GlStateManager._bindTexture(0);
   }

   public void checkFramebufferStatus() {
      RenderSystem.assertOnRenderThreadOrInit();
      int i = GlStateManager.glCheckFramebufferStatus(GlConst.GL_FRAMEBUFFER);
      if (i != GlConst.GL_FRAMEBUFFER_COMPLETE) {
         if (i == GlConst.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
         } else if (i == GlConst.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
         } else if (i == GlConst.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
         } else if (i == GlConst.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
         } else if (i == GlConst.GL_FRAMEBUFFER_UNSUPPORTED) {
            throw new RuntimeException("GL_FRAMEBUFFER_UNSUPPORTED");
         } else if (i == GlConst.GL_OUT_OF_MEMORY) {
            throw new RuntimeException("GL_OUT_OF_MEMORY");
         } else {
            throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
         }
      }
   }

   public void beginRead() {
      RenderSystem.assertOnRenderThread();
      GlStateManager._bindTexture(this.colorAttachment);
   }

   public void endRead() {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._bindTexture(0);
   }

   public void beginWrite(boolean setViewport) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this.bind(setViewport);
         });
      } else {
         this.bind(setViewport);
      }

   }

   private void bind(boolean updateViewport) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this.fbo);
      if (updateViewport) {
         GlStateManager._viewport(0, 0, this.viewportWidth, this.viewportHeight);
      }

   }

   public void endWrite() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
         });
      } else {
         GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
      }

   }

   public void setClearColor(float r, float g, float b, float a) {
      this.clearColor[0] = r;
      this.clearColor[1] = g;
      this.clearColor[2] = b;
      this.clearColor[3] = a;
   }

   public void draw(int width, int height) {
      this.draw(width, height, true);
   }

   public void draw(int width, int height, boolean disableBlend) {
      RenderSystem.assertOnGameThreadOrInit();
      if (!RenderSystem.isInInitPhase()) {
         RenderSystem.recordRenderCall(() -> {
            this.drawInternal(width, height, disableBlend);
         });
      } else {
         this.drawInternal(width, height, disableBlend);
      }

   }

   private void drawInternal(int width, int height, boolean disableBlend) {
      RenderSystem.assertOnRenderThread();
      GlStateManager._colorMask(true, true, true, false);
      GlStateManager._disableDepthTest();
      GlStateManager._depthMask(false);
      GlStateManager._viewport(0, 0, width, height);
      if (disableBlend) {
         GlStateManager._disableBlend();
      }

      MinecraftClient lv = MinecraftClient.getInstance();
      ShaderProgram lv2 = lv.gameRenderer.blitScreenProgram;
      lv2.addSampler("DiffuseSampler", this.colorAttachment);
      Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float)width, (float)height, 0.0F, 1000.0F, 3000.0F);
      RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_Z);
      if (lv2.modelViewMat != null) {
         lv2.modelViewMat.set((new Matrix4f()).translation(0.0F, 0.0F, -2000.0F));
      }

      if (lv2.projectionMat != null) {
         lv2.projectionMat.set(matrix4f);
      }

      lv2.bind();
      float f = (float)width;
      float g = (float)height;
      float h = (float)this.viewportWidth / (float)this.textureWidth;
      float k = (float)this.viewportHeight / (float)this.textureHeight;
      Tessellator lv3 = RenderSystem.renderThreadTesselator();
      BufferBuilder lv4 = lv3.getBuffer();
      lv4.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      lv4.vertex(0.0, (double)g, 0.0).texture(0.0F, 0.0F).color(255, 255, 255, 255).next();
      lv4.vertex((double)f, (double)g, 0.0).texture(h, 0.0F).color(255, 255, 255, 255).next();
      lv4.vertex((double)f, 0.0, 0.0).texture(h, k).color(255, 255, 255, 255).next();
      lv4.vertex(0.0, 0.0, 0.0).texture(0.0F, k).color(255, 255, 255, 255).next();
      BufferRenderer.draw(lv4.end());
      lv2.unbind();
      GlStateManager._depthMask(true);
      GlStateManager._colorMask(true, true, true, true);
   }

   public void clear(boolean getError) {
      RenderSystem.assertOnRenderThreadOrInit();
      this.beginWrite(true);
      GlStateManager._clearColor(this.clearColor[0], this.clearColor[1], this.clearColor[2], this.clearColor[3]);
      int i = 16384;
      if (this.useDepthAttachment) {
         GlStateManager._clearDepth(1.0);
         i |= 256;
      }

      GlStateManager._clear(i, getError);
      this.endWrite();
   }

   public int getColorAttachment() {
      return this.colorAttachment;
   }

   public int getDepthAttachment() {
      return this.depthAttachment;
   }
}
