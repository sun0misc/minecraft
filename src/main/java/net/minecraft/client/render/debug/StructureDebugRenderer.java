package net.minecraft.client.render.debug;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;

@Environment(EnvType.CLIENT)
public class StructureDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;
   private final Map structureBoundingBoxes = Maps.newIdentityHashMap();
   private final Map structurePiecesBoundingBoxes = Maps.newIdentityHashMap();
   private final Map field_4625 = Maps.newIdentityHashMap();
   private static final int RANGE = 500;

   public StructureDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      Camera lv = this.client.gameRenderer.getCamera();
      WorldAccess lv2 = this.client.world;
      DimensionType lv3 = lv2.getDimension();
      BlockPos lv4 = BlockPos.ofFloored(lv.getPos().x, 0.0, lv.getPos().z);
      VertexConsumer lv5 = vertexConsumers.getBuffer(RenderLayer.getLines());
      Iterator var14;
      if (this.structureBoundingBoxes.containsKey(lv3)) {
         var14 = ((Map)this.structureBoundingBoxes.get(lv3)).values().iterator();

         while(var14.hasNext()) {
            BlockBox lv6 = (BlockBox)var14.next();
            if (lv4.isWithinDistance(lv6.getCenter(), 500.0)) {
               WorldRenderer.drawBox(matrices, lv5, (double)lv6.getMinX() - cameraX, (double)lv6.getMinY() - cameraY, (double)lv6.getMinZ() - cameraZ, (double)(lv6.getMaxX() + 1) - cameraX, (double)(lv6.getMaxY() + 1) - cameraY, (double)(lv6.getMaxZ() + 1) - cameraZ, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }

      if (this.structurePiecesBoundingBoxes.containsKey(lv3)) {
         var14 = ((Map)this.structurePiecesBoundingBoxes.get(lv3)).entrySet().iterator();

         while(var14.hasNext()) {
            Map.Entry entry = (Map.Entry)var14.next();
            String string = (String)entry.getKey();
            BlockBox lv7 = (BlockBox)entry.getValue();
            Boolean boolean_ = (Boolean)((Map)this.field_4625.get(lv3)).get(string);
            if (lv4.isWithinDistance(lv7.getCenter(), 500.0)) {
               if (boolean_) {
                  WorldRenderer.drawBox(matrices, lv5, (double)lv7.getMinX() - cameraX, (double)lv7.getMinY() - cameraY, (double)lv7.getMinZ() - cameraZ, (double)(lv7.getMaxX() + 1) - cameraX, (double)(lv7.getMaxY() + 1) - cameraY, (double)(lv7.getMaxZ() + 1) - cameraZ, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);
               } else {
                  WorldRenderer.drawBox(matrices, lv5, (double)lv7.getMinX() - cameraX, (double)lv7.getMinY() - cameraY, (double)lv7.getMinZ() - cameraZ, (double)(lv7.getMaxX() + 1) - cameraX, (double)(lv7.getMaxY() + 1) - cameraY, (double)(lv7.getMaxZ() + 1) - cameraZ, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
               }
            }
         }
      }

   }

   public void addStructure(BlockBox boundingBox, List piecesBoundingBoxes, List list2, DimensionType dimension) {
      if (!this.structureBoundingBoxes.containsKey(dimension)) {
         this.structureBoundingBoxes.put(dimension, Maps.newHashMap());
      }

      if (!this.structurePiecesBoundingBoxes.containsKey(dimension)) {
         this.structurePiecesBoundingBoxes.put(dimension, Maps.newHashMap());
         this.field_4625.put(dimension, Maps.newHashMap());
      }

      ((Map)this.structureBoundingBoxes.get(dimension)).put(boundingBox.toString(), boundingBox);

      for(int i = 0; i < piecesBoundingBoxes.size(); ++i) {
         BlockBox lv = (BlockBox)piecesBoundingBoxes.get(i);
         Boolean boolean_ = (Boolean)list2.get(i);
         ((Map)this.structurePiecesBoundingBoxes.get(dimension)).put(lv.toString(), lv);
         ((Map)this.field_4625.get(dimension)).put(lv.toString(), boolean_);
      }

   }

   public void clear() {
      this.structureBoundingBoxes.clear();
      this.structurePiecesBoundingBoxes.clear();
      this.field_4625.clear();
   }
}
