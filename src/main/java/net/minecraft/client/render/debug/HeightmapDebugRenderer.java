package net.minecraft.client.render.debug;

import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class HeightmapDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;
   private static final int CHUNK_RANGE = 2;
   private static final float BOX_HEIGHT = 0.09375F;

   public HeightmapDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      WorldAccess lv = this.client.world;
      VertexConsumer lv2 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
      BlockPos lv3 = BlockPos.ofFloored(cameraX, 0.0, cameraZ);

      for(int i = -2; i <= 2; ++i) {
         for(int j = -2; j <= 2; ++j) {
            Chunk lv4 = lv.getChunk(lv3.add(i * 16, 0, j * 16));
            Iterator var15 = lv4.getHeightmaps().iterator();

            while(var15.hasNext()) {
               Map.Entry entry = (Map.Entry)var15.next();
               Heightmap.Type lv5 = (Heightmap.Type)entry.getKey();
               ChunkPos lv6 = lv4.getPos();
               Vector3f vector3f = this.getColorForHeightmapType(lv5);

               for(int k = 0; k < 16; ++k) {
                  for(int l = 0; l < 16; ++l) {
                     int m = ChunkSectionPos.getOffsetPos(lv6.x, k);
                     int n = ChunkSectionPos.getOffsetPos(lv6.z, l);
                     float g = (float)((double)((float)lv.getTopY(lv5, m, n) + (float)lv5.ordinal() * 0.09375F) - cameraY);
                     WorldRenderer.method_3258(matrices, lv2, (double)((float)m + 0.25F) - cameraX, (double)g, (double)((float)n + 0.25F) - cameraZ, (double)((float)m + 0.75F) - cameraX, (double)(g + 0.09375F), (double)((float)n + 0.75F) - cameraZ, vector3f.x(), vector3f.y(), vector3f.z(), 1.0F);
                  }
               }
            }
         }
      }

   }

   private Vector3f getColorForHeightmapType(Heightmap.Type type) {
      Vector3f var10000;
      switch (type) {
         case WORLD_SURFACE_WG:
            var10000 = new Vector3f(1.0F, 1.0F, 0.0F);
            break;
         case OCEAN_FLOOR_WG:
            var10000 = new Vector3f(1.0F, 0.0F, 1.0F);
            break;
         case WORLD_SURFACE:
            var10000 = new Vector3f(0.0F, 0.7F, 0.0F);
            break;
         case OCEAN_FLOOR:
            var10000 = new Vector3f(0.0F, 0.0F, 0.5F);
            break;
         case MOTION_BLOCKING:
            var10000 = new Vector3f(0.0F, 0.3F, 0.3F);
            break;
         case MOTION_BLOCKING_NO_LEAVES:
            var10000 = new Vector3f(0.0F, 0.5F, 0.5F);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }
}
