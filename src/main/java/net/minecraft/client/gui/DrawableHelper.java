package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Divider;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public abstract class DrawableHelper {
   public static final Identifier OPTIONS_BACKGROUND_TEXTURE = new Identifier("textures/gui/options_background.png");
   public static final Identifier STATS_ICON_TEXTURE = new Identifier("textures/gui/container/stats_icons.png");
   public static final Identifier GUI_ICONS_TEXTURE = new Identifier("textures/gui/icons.png");
   public static final Identifier LIGHT_DIRT_BACKGROUND_TEXTURE = new Identifier("textures/gui/light_dirt_background.png");
   private static final ScissorStack SCISSOR_STACK = new ScissorStack();

   protected static void drawHorizontalLine(MatrixStack matrices, int x1, int x2, int y, int color) {
      if (x2 < x1) {
         int m = x1;
         x1 = x2;
         x2 = m;
      }

      fill(matrices, x1, y, x2 + 1, y + 1, color);
   }

   protected static void drawVerticalLine(MatrixStack matrices, int x, int y1, int y2, int color) {
      if (y2 < y1) {
         int m = y1;
         y1 = y2;
         y2 = m;
      }

      fill(matrices, x, y1 + 1, x + 1, y2, color);
   }

   public static void enableScissor(int x1, int y1, int x2, int y2) {
      setScissor(SCISSOR_STACK.push(new ScreenRect(x1, y1, x2 - x1, y2 - y1)));
   }

   public static void disableScissor() {
      setScissor(SCISSOR_STACK.pop());
   }

   private static void setScissor(@Nullable ScreenRect rect) {
      if (rect != null) {
         Window lv = MinecraftClient.getInstance().getWindow();
         int i = lv.getFramebufferHeight();
         double d = lv.getScaleFactor();
         double e = (double)rect.getLeft() * d;
         double f = (double)i - (double)rect.getBottom() * d;
         double g = (double)rect.width() * d;
         double h = (double)rect.height() * d;
         RenderSystem.enableScissor((int)e, (int)f, Math.max(0, (int)g), Math.max(0, (int)h));
      } else {
         RenderSystem.disableScissor();
      }

   }

   public static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
      fill(matrices, x1, y1, x2, y2, 0, color);
   }

   public static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int z, int color) {
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      int o;
      if (x1 < x2) {
         o = x1;
         x1 = x2;
         x2 = o;
      }

      if (y1 < y2) {
         o = y1;
         y1 = y2;
         y2 = o;
      }

      float f = (float)ColorHelper.Argb.getAlpha(color) / 255.0F;
      float g = (float)ColorHelper.Argb.getRed(color) / 255.0F;
      float h = (float)ColorHelper.Argb.getGreen(color) / 255.0F;
      float p = (float)ColorHelper.Argb.getBlue(color) / 255.0F;
      BufferBuilder lv = Tessellator.getInstance().getBuffer();
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      lv.vertex(matrix4f, (float)x1, (float)y1, (float)z).color(g, h, p, f).next();
      lv.vertex(matrix4f, (float)x1, (float)y2, (float)z).color(g, h, p, f).next();
      lv.vertex(matrix4f, (float)x2, (float)y2, (float)z).color(g, h, p, f).next();
      lv.vertex(matrix4f, (float)x2, (float)y1, (float)z).color(g, h, p, f).next();
      BufferRenderer.drawWithGlobalProgram(lv.end());
      RenderSystem.disableBlend();
   }

   protected static void fillGradient(MatrixStack matrices, int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
      fillGradient(matrices, startX, startY, endX, endY, colorStart, colorEnd, 0);
   }

   protected static void fillGradient(MatrixStack matrices, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z) {
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      Tessellator lv = Tessellator.getInstance();
      BufferBuilder lv2 = lv.getBuffer();
      lv2.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      fillGradient(matrices.peek().getPositionMatrix(), lv2, startX, startY, endX, endY, z, colorStart, colorEnd);
      lv.draw();
      RenderSystem.disableBlend();
   }

   protected static void fillGradient(Matrix4f matrix, BufferBuilder builder, int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd) {
      float f = (float)ColorHelper.Argb.getAlpha(colorStart) / 255.0F;
      float g = (float)ColorHelper.Argb.getRed(colorStart) / 255.0F;
      float h = (float)ColorHelper.Argb.getGreen(colorStart) / 255.0F;
      float p = (float)ColorHelper.Argb.getBlue(colorStart) / 255.0F;
      float q = (float)ColorHelper.Argb.getAlpha(colorEnd) / 255.0F;
      float r = (float)ColorHelper.Argb.getRed(colorEnd) / 255.0F;
      float s = (float)ColorHelper.Argb.getGreen(colorEnd) / 255.0F;
      float t = (float)ColorHelper.Argb.getBlue(colorEnd) / 255.0F;
      builder.vertex(matrix, (float)startX, (float)startY, (float)z).color(g, h, p, f).next();
      builder.vertex(matrix, (float)startX, (float)endY, (float)z).color(r, s, t, q).next();
      builder.vertex(matrix, (float)endX, (float)endY, (float)z).color(r, s, t, q).next();
      builder.vertex(matrix, (float)endX, (float)startY, (float)z).color(g, h, p, f).next();
   }

   public static void drawCenteredTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, String text, int centerX, int y, int color) {
      textRenderer.drawWithShadow(matrices, text, (float)(centerX - textRenderer.getWidth(text) / 2), (float)y, color);
   }

   public static void drawCenteredTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
      OrderedText lv = text.asOrderedText();
      textRenderer.drawWithShadow(matrices, lv, (float)(centerX - textRenderer.getWidth(lv) / 2), (float)y, color);
   }

   public static void drawCenteredTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, OrderedText text, int centerX, int y, int color) {
      textRenderer.drawWithShadow(matrices, text, (float)(centerX - textRenderer.getWidth(text) / 2), (float)y, color);
   }

   public static void drawTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, String text, int x, int y, int color) {
      textRenderer.drawWithShadow(matrices, text, (float)x, (float)y, color);
   }

   public static void drawTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
      textRenderer.drawWithShadow(matrices, text, (float)x, (float)y, color);
   }

   public static void drawTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, Text text, int x, int y, int color) {
      textRenderer.drawWithShadow(matrices, text, (float)x, (float)y, color);
   }

   public static void drawWithOutline(int x, int y, BiConsumer renderAction) {
      RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
      renderAction.accept(x + 1, y);
      renderAction.accept(x - 1, y);
      renderAction.accept(x, y + 1);
      renderAction.accept(x, y - 1);
      RenderSystem.defaultBlendFunc();
      renderAction.accept(x, y);
   }

   public static void drawSprite(MatrixStack matrices, int x, int y, int z, int width, int height, Sprite sprite) {
      drawTexturedQuad(matrices.peek().getPositionMatrix(), x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
   }

   public static void drawSprite(MatrixStack matrices, int x, int y, int z, int width, int height, Sprite sprite, float red, float green, float blue, float alpha) {
      drawTexturedQuad(matrices.peek().getPositionMatrix(), x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), red, green, blue, alpha);
   }

   public static void drawBorder(MatrixStack matrices, int x, int y, int width, int height, int color) {
      fill(matrices, x, y, x + width, y + 1, color);
      fill(matrices, x, y + height - 1, x + width, y + height, color);
      fill(matrices, x, y + 1, x + 1, y + height - 1, color);
      fill(matrices, x + width - 1, y + 1, x + width, y + height - 1, color);
   }

   public static void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
      drawTexture(matrices, x, y, 0, (float)u, (float)v, width, height, 256, 256);
   }

   public static void drawTexture(MatrixStack matrices, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
      drawTexture(matrices, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
   }

   public static void drawTexture(MatrixStack matrices, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
      drawTexture(matrices, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
   }

   public static void drawTexture(MatrixStack matrices, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
      drawTexture(matrices, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
   }

   private static void drawTexture(MatrixStack matrices, int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
      drawTexturedQuad(matrices.peek().getPositionMatrix(), x0, x1, y0, y1, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
   }

   private static void drawTexturedQuad(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      BufferBuilder lv = Tessellator.getInstance().getBuffer();
      lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      lv.vertex(matrix, (float)x0, (float)y0, (float)z).texture(u0, v0).next();
      lv.vertex(matrix, (float)x0, (float)y1, (float)z).texture(u0, v1).next();
      lv.vertex(matrix, (float)x1, (float)y1, (float)z).texture(u1, v1).next();
      lv.vertex(matrix, (float)x1, (float)y0, (float)z).texture(u1, v0).next();
      BufferRenderer.drawWithGlobalProgram(lv.end());
   }

   private static void drawTexturedQuad(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, float red, float green, float blue, float alpha) {
      RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
      RenderSystem.enableBlend();
      BufferBuilder lv = Tessellator.getInstance().getBuffer();
      lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
      lv.vertex(matrix, (float)x0, (float)y0, (float)z).color(red, green, blue, alpha).texture(u0, v0).next();
      lv.vertex(matrix, (float)x0, (float)y1, (float)z).color(red, green, blue, alpha).texture(u0, v1).next();
      lv.vertex(matrix, (float)x1, (float)y1, (float)z).color(red, green, blue, alpha).texture(u1, v1).next();
      lv.vertex(matrix, (float)x1, (float)y0, (float)z).color(red, green, blue, alpha).texture(u1, v0).next();
      BufferRenderer.drawWithGlobalProgram(lv.end());
      RenderSystem.disableBlend();
   }

   public static void drawNineSlicedTexture(MatrixStack matrices, int x, int y, int width, int height, int outerSliceSize, int centerSliceWidth, int centerSliceHeight, int u, int v) {
      drawNineSlicedTexture(matrices, x, y, width, height, outerSliceSize, outerSliceSize, outerSliceSize, outerSliceSize, centerSliceWidth, centerSliceHeight, u, v);
   }

   public static void drawNineSlicedTexture(MatrixStack matrices, int x, int y, int width, int height, int sideSliceWidth, int sideSliceHeight, int centerSliceWidth, int centerSliceHeight, int u, int v) {
      drawNineSlicedTexture(matrices, x, y, width, height, sideSliceWidth, sideSliceHeight, sideSliceWidth, sideSliceHeight, centerSliceWidth, centerSliceHeight, u, v);
   }

   public static void drawNineSlicedTexture(MatrixStack matrices, int x, int y, int width, int height, int leftSliceWidth, int topSliceHeight, int rightSliceWidth, int bottomSliceHeight, int centerSliceWidth, int centerSliceHeight, int u, int v) {
      leftSliceWidth = Math.min(leftSliceWidth, width / 2);
      rightSliceWidth = Math.min(rightSliceWidth, width / 2);
      topSliceHeight = Math.min(topSliceHeight, height / 2);
      bottomSliceHeight = Math.min(bottomSliceHeight, height / 2);
      if (width == centerSliceWidth && height == centerSliceHeight) {
         drawTexture(matrices, x, y, u, v, width, height);
      } else if (height == centerSliceHeight) {
         drawTexture(matrices, x, y, u, v, leftSliceWidth, height);
         drawRepeatingTexture(matrices, x + leftSliceWidth, y, width - rightSliceWidth - leftSliceWidth, height, u + leftSliceWidth, v, centerSliceWidth - rightSliceWidth - leftSliceWidth, centerSliceHeight);
         drawTexture(matrices, x + width - rightSliceWidth, y, u + centerSliceWidth - rightSliceWidth, v, rightSliceWidth, height);
      } else if (width == centerSliceWidth) {
         drawTexture(matrices, x, y, u, v, width, topSliceHeight);
         drawRepeatingTexture(matrices, x, y + topSliceHeight, width, height - bottomSliceHeight - topSliceHeight, u, v + topSliceHeight, centerSliceWidth, centerSliceHeight - bottomSliceHeight - topSliceHeight);
         drawTexture(matrices, x, y + height - bottomSliceHeight, u, v + centerSliceHeight - bottomSliceHeight, width, bottomSliceHeight);
      } else {
         drawTexture(matrices, x, y, u, v, leftSliceWidth, topSliceHeight);
         drawRepeatingTexture(matrices, x + leftSliceWidth, y, width - rightSliceWidth - leftSliceWidth, topSliceHeight, u + leftSliceWidth, v, centerSliceWidth - rightSliceWidth - leftSliceWidth, topSliceHeight);
         drawTexture(matrices, x + width - rightSliceWidth, y, u + centerSliceWidth - rightSliceWidth, v, rightSliceWidth, topSliceHeight);
         drawTexture(matrices, x, y + height - bottomSliceHeight, u, v + centerSliceHeight - bottomSliceHeight, leftSliceWidth, bottomSliceHeight);
         drawRepeatingTexture(matrices, x + leftSliceWidth, y + height - bottomSliceHeight, width - rightSliceWidth - leftSliceWidth, bottomSliceHeight, u + leftSliceWidth, v + centerSliceHeight - bottomSliceHeight, centerSliceWidth - rightSliceWidth - leftSliceWidth, bottomSliceHeight);
         drawTexture(matrices, x + width - rightSliceWidth, y + height - bottomSliceHeight, u + centerSliceWidth - rightSliceWidth, v + centerSliceHeight - bottomSliceHeight, rightSliceWidth, bottomSliceHeight);
         drawRepeatingTexture(matrices, x, y + topSliceHeight, leftSliceWidth, height - bottomSliceHeight - topSliceHeight, u, v + topSliceHeight, leftSliceWidth, centerSliceHeight - bottomSliceHeight - topSliceHeight);
         drawRepeatingTexture(matrices, x + leftSliceWidth, y + topSliceHeight, width - rightSliceWidth - leftSliceWidth, height - bottomSliceHeight - topSliceHeight, u + leftSliceWidth, v + topSliceHeight, centerSliceWidth - rightSliceWidth - leftSliceWidth, centerSliceHeight - bottomSliceHeight - topSliceHeight);
         drawRepeatingTexture(matrices, x + width - rightSliceWidth, y + topSliceHeight, leftSliceWidth, height - bottomSliceHeight - topSliceHeight, u + centerSliceWidth - rightSliceWidth, v + topSliceHeight, rightSliceWidth, centerSliceHeight - bottomSliceHeight - topSliceHeight);
      }
   }

   public static void drawRepeatingTexture(MatrixStack matrices, int x, int y, int width, int height, int u, int v, int textureWidth, int textureHeight) {
      int q = x;

      int r;
      for(IntIterator intIterator = createDivider(width, textureWidth); intIterator.hasNext(); q += r) {
         r = intIterator.nextInt();
         int s = (textureWidth - r) / 2;
         int t = y;

         int u;
         for(IntIterator intIterator2 = createDivider(height, textureHeight); intIterator2.hasNext(); t += u) {
            u = intIterator2.nextInt();
            int v = (textureHeight - u) / 2;
            drawTexture(matrices, q, t, u + s, v + v, r, u);
         }
      }

   }

   private static IntIterator createDivider(int sideLength, int textureSideLength) {
      int k = MathHelper.ceilDiv(sideLength, textureSideLength);
      return new Divider(sideLength, k);
   }

   @Environment(EnvType.CLIENT)
   private static class ScissorStack {
      private final Deque stack = new ArrayDeque();

      ScissorStack() {
      }

      public ScreenRect push(ScreenRect rect) {
         ScreenRect lv = (ScreenRect)this.stack.peekLast();
         if (lv != null) {
            ScreenRect lv2 = (ScreenRect)Objects.requireNonNullElse(rect.intersection(lv), ScreenRect.empty());
            this.stack.addLast(lv2);
            return lv2;
         } else {
            this.stack.addLast(rect);
            return rect;
         }
      }

      @Nullable
      public ScreenRect pop() {
         if (this.stack.isEmpty()) {
            throw new IllegalStateException("Scissor stack underflow");
         } else {
            this.stack.removeLast();
            return (ScreenRect)this.stack.peekLast();
         }
      }
   }
}
