package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class TooltipBackgroundRenderer {
   public static final int field_41688 = 12;
   private static final int field_41693 = 3;
   public static final int field_41689 = 3;
   public static final int field_41690 = 3;
   public static final int field_41691 = 3;
   public static final int field_41692 = 3;
   private static final int BACKGROUND_COLOR = -267386864;
   private static final int START_Y_BORDER_COLOR = 1347420415;
   private static final int END_Y_BORDER_COLOR = 1344798847;

   public static void render(RectangleRenderer renderer, Matrix4f matrix, BufferBuilder buffer, int x, int y, int width, int height, int z) {
      int n = x - 3;
      int o = y - 3;
      int p = width + 3 + 3;
      int q = height + 3 + 3;
      renderHorizontalLine(renderer, matrix, buffer, n, o - 1, p, z, -267386864);
      renderHorizontalLine(renderer, matrix, buffer, n, o + q, p, z, -267386864);
      renderRectangle(renderer, matrix, buffer, n, o, p, q, z, -267386864);
      renderVerticalLine(renderer, matrix, buffer, n - 1, o, q, z, -267386864);
      renderVerticalLine(renderer, matrix, buffer, n + p, o, q, z, -267386864);
      renderBorder(renderer, matrix, buffer, n, o + 1, p, q, z, 1347420415, 1344798847);
   }

   private static void renderBorder(RectangleRenderer renderer, Matrix4f matrix, BufferBuilder buffer, int x, int y, int width, int height, int z, int startYColor, int endYColor) {
      renderVerticalLine(renderer, matrix, buffer, x, y, height - 2, z, startYColor, endYColor);
      renderVerticalLine(renderer, matrix, buffer, x + width - 1, y, height - 2, z, startYColor, endYColor);
      renderHorizontalLine(renderer, matrix, buffer, x, y - 1, width, z, startYColor);
      renderHorizontalLine(renderer, matrix, buffer, x, y - 1 + height - 1, width, z, endYColor);
   }

   private static void renderVerticalLine(RectangleRenderer renderer, Matrix4f matrix, BufferBuilder buffer, int x, int y, int height, int z, int color) {
      renderer.blit(matrix, buffer, x, y, x + 1, y + height, z, color, color);
   }

   private static void renderVerticalLine(RectangleRenderer renderer, Matrix4f matrix, BufferBuilder buffer, int x, int y, int height, int z, int startColor, int endColor) {
      renderer.blit(matrix, buffer, x, y, x + 1, y + height, z, startColor, endColor);
   }

   private static void renderHorizontalLine(RectangleRenderer renderer, Matrix4f matrix, BufferBuilder buffer, int x, int y, int width, int z, int color) {
      renderer.blit(matrix, buffer, x, y, x + width, y + 1, z, color, color);
   }

   private static void renderRectangle(RectangleRenderer renderer, Matrix4f matrix, BufferBuilder buffer, int x, int y, int width, int height, int z, int color) {
      renderer.blit(matrix, buffer, x, y, x + width, y + height, z, color, color);
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface RectangleRenderer {
      void blit(Matrix4f matrix, BufferBuilder buffer, int startX, int startY, int endX, int endY, int z, int startColor, int endColor);
   }
}
