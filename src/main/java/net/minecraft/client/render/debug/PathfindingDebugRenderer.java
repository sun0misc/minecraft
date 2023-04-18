package net.minecraft.client.render.debug;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PathfindingDebugRenderer implements DebugRenderer.Renderer {
   private final Map paths = Maps.newHashMap();
   private final Map nodeSizes = Maps.newHashMap();
   private final Map pathTimes = Maps.newHashMap();
   private static final long MAX_PATH_AGE = 5000L;
   private static final float RANGE = 80.0F;
   private static final boolean field_32908 = true;
   private static final boolean field_32909 = false;
   private static final boolean field_32910 = false;
   private static final boolean field_32911 = true;
   private static final boolean field_32912 = true;
   private static final float DRAWN_STRING_SIZE = 0.02F;

   public void addPath(int id, Path path, float nodeSize) {
      this.paths.put(id, path);
      this.pathTimes.put(id, Util.getMeasuringTimeMs());
      this.nodeSizes.put(id, nodeSize);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      if (!this.paths.isEmpty()) {
         long l = Util.getMeasuringTimeMs();
         Iterator var11 = this.paths.keySet().iterator();

         while(var11.hasNext()) {
            Integer integer = (Integer)var11.next();
            Path lv = (Path)this.paths.get(integer);
            float g = (Float)this.nodeSizes.get(integer);
            drawPath(matrices, vertexConsumers, lv, g, true, true, cameraX, cameraY, cameraZ);
         }

         Integer[] var15 = (Integer[])this.pathTimes.keySet().toArray(new Integer[0]);
         int var16 = var15.length;

         for(int var17 = 0; var17 < var16; ++var17) {
            Integer integer2 = var15[var17];
            if (l - (Long)this.pathTimes.get(integer2) > 5000L) {
               this.paths.remove(integer2);
               this.pathTimes.remove(integer2);
            }
         }

      }
   }

   public static void drawPath(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Path path, float nodeSize, boolean drawDebugNodes, boolean drawLabels, double cameraX, double cameraY, double cameraZ) {
      drawPathLines(matrices, vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(6.0)), path, cameraX, cameraY, cameraZ);
      BlockPos lv = path.getTarget();
      int i;
      PathNode lv2;
      if (getManhattanDistance(lv, cameraX, cameraY, cameraZ) <= 80.0F) {
         DebugRenderer.drawBox(matrices, vertexConsumers, (new Box((double)((float)lv.getX() + 0.25F), (double)((float)lv.getY() + 0.25F), (double)lv.getZ() + 0.25, (double)((float)lv.getX() + 0.75F), (double)((float)lv.getY() + 0.75F), (double)((float)lv.getZ() + 0.75F))).offset(-cameraX, -cameraY, -cameraZ), 0.0F, 1.0F, 0.0F, 0.5F);

         for(i = 0; i < path.getLength(); ++i) {
            lv2 = path.getNode(i);
            if (getManhattanDistance(lv2.getBlockPos(), cameraX, cameraY, cameraZ) <= 80.0F) {
               float h = i == path.getCurrentNodeIndex() ? 1.0F : 0.0F;
               float j = i == path.getCurrentNodeIndex() ? 0.0F : 1.0F;
               DebugRenderer.drawBox(matrices, vertexConsumers, (new Box((double)((float)lv2.x + 0.5F - nodeSize), (double)((float)lv2.y + 0.01F * (float)i), (double)((float)lv2.z + 0.5F - nodeSize), (double)((float)lv2.x + 0.5F + nodeSize), (double)((float)lv2.y + 0.25F + 0.01F * (float)i), (double)((float)lv2.z + 0.5F + nodeSize))).offset(-cameraX, -cameraY, -cameraZ), h, 0.0F, j, 0.5F);
            }
         }
      }

      if (drawDebugNodes) {
         PathNode[] var17 = path.getDebugSecondNodes();
         int var18 = var17.length;

         int var19;
         PathNode lv3;
         for(var19 = 0; var19 < var18; ++var19) {
            lv3 = var17[var19];
            if (getManhattanDistance(lv3.getBlockPos(), cameraX, cameraY, cameraZ) <= 80.0F) {
               DebugRenderer.drawBox(matrices, vertexConsumers, (new Box((double)((float)lv3.x + 0.5F - nodeSize / 2.0F), (double)((float)lv3.y + 0.01F), (double)((float)lv3.z + 0.5F - nodeSize / 2.0F), (double)((float)lv3.x + 0.5F + nodeSize / 2.0F), (double)lv3.y + 0.1, (double)((float)lv3.z + 0.5F + nodeSize / 2.0F))).offset(-cameraX, -cameraY, -cameraZ), 1.0F, 0.8F, 0.8F, 0.5F);
            }
         }

         var17 = path.getDebugNodes();
         var18 = var17.length;

         for(var19 = 0; var19 < var18; ++var19) {
            lv3 = var17[var19];
            if (getManhattanDistance(lv3.getBlockPos(), cameraX, cameraY, cameraZ) <= 80.0F) {
               DebugRenderer.drawBox(matrices, vertexConsumers, (new Box((double)((float)lv3.x + 0.5F - nodeSize / 2.0F), (double)((float)lv3.y + 0.01F), (double)((float)lv3.z + 0.5F - nodeSize / 2.0F), (double)((float)lv3.x + 0.5F + nodeSize / 2.0F), (double)lv3.y + 0.1, (double)((float)lv3.z + 0.5F + nodeSize / 2.0F))).offset(-cameraX, -cameraY, -cameraZ), 0.8F, 1.0F, 1.0F, 0.5F);
            }
         }
      }

      if (drawLabels) {
         for(i = 0; i < path.getLength(); ++i) {
            lv2 = path.getNode(i);
            if (getManhattanDistance(lv2.getBlockPos(), cameraX, cameraY, cameraZ) <= 80.0F) {
               DebugRenderer.drawString(matrices, vertexConsumers, String.valueOf(lv2.type), (double)lv2.x + 0.5, (double)lv2.y + 0.75, (double)lv2.z + 0.5, -1, 0.02F, true, 0.0F, true);
               DebugRenderer.drawString(matrices, vertexConsumers, String.format(Locale.ROOT, "%.2f", lv2.penalty), (double)lv2.x + 0.5, (double)lv2.y + 0.25, (double)lv2.z + 0.5, -1, 0.02F, true, 0.0F, true);
            }
         }
      }

   }

   public static void drawPathLines(MatrixStack matrices, VertexConsumer vertexConsumers, Path path, double cameraX, double cameraY, double cameraZ) {
      for(int i = 0; i < path.getLength(); ++i) {
         PathNode lv = path.getNode(i);
         if (!(getManhattanDistance(lv.getBlockPos(), cameraX, cameraY, cameraZ) > 80.0F)) {
            float g = (float)i / (float)path.getLength() * 0.33F;
            int j = i == 0 ? 0 : MathHelper.hsvToRgb(g, 0.9F, 0.9F);
            int k = j >> 16 & 255;
            int l = j >> 8 & 255;
            int m = j & 255;
            vertexConsumers.vertex(matrices.peek().getPositionMatrix(), (float)((double)lv.x - cameraX + 0.5), (float)((double)lv.y - cameraY + 0.5), (float)((double)lv.z - cameraZ + 0.5)).color(k, l, m, 255).next();
         }
      }

   }

   private static float getManhattanDistance(BlockPos pos, double x, double y, double z) {
      return (float)(Math.abs((double)pos.getX() - x) + Math.abs((double)pos.getY() - y) + Math.abs((double)pos.getZ() - z));
   }
}
