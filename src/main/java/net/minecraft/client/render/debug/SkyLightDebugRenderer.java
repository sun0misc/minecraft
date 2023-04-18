package net.minecraft.client.render.debug;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class SkyLightDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;
   private static final int RANGE = 10;

   public SkyLightDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      World lv = this.client.world;
      BlockPos lv2 = BlockPos.ofFloored(cameraX, cameraY, cameraZ);
      LongSet longSet = new LongOpenHashSet();
      Iterator var12 = BlockPos.iterate(lv2.add(-10, -10, -10), lv2.add(10, 10, 10)).iterator();

      while(var12.hasNext()) {
         BlockPos lv3 = (BlockPos)var12.next();
         int i = lv.getLightLevel(LightType.SKY, lv3);
         float g = (float)(15 - i) / 15.0F * 0.5F + 0.16F;
         int j = MathHelper.hsvToRgb(g, 0.9F, 0.9F);
         long l = ChunkSectionPos.fromBlockPos(lv3.asLong());
         if (longSet.add(l)) {
            DebugRenderer.drawString(matrices, vertexConsumers, lv.getChunkManager().getLightingProvider().displaySectionLevel(LightType.SKY, ChunkSectionPos.from(l)), (double)ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackX(l), 8), (double)ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackY(l), 8), (double)ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackZ(l), 8), 16711680, 0.3F);
         }

         if (i != 15) {
            DebugRenderer.drawString(matrices, vertexConsumers, String.valueOf(i), (double)lv3.getX() + 0.5, (double)lv3.getY() + 0.25, (double)lv3.getZ() + 0.5, j);
         }
      }

   }
}
