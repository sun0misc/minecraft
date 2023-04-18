package net.minecraft.client.render.debug;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.WorldView;

@Environment(EnvType.CLIENT)
public class WaterDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;

   public WaterDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      BlockPos lv = this.client.player.getBlockPos();
      WorldView lv2 = this.client.player.world;
      Iterator var11 = BlockPos.iterate(lv.add(-10, -10, -10), lv.add(10, 10, 10)).iterator();

      BlockPos lv3;
      FluidState lv4;
      while(var11.hasNext()) {
         lv3 = (BlockPos)var11.next();
         lv4 = lv2.getFluidState(lv3);
         if (lv4.isIn(FluidTags.WATER)) {
            double g = (double)((float)lv3.getY() + lv4.getHeight(lv2, lv3));
            DebugRenderer.drawBox(matrices, vertexConsumers, (new Box((double)((float)lv3.getX() + 0.01F), (double)((float)lv3.getY() + 0.01F), (double)((float)lv3.getZ() + 0.01F), (double)((float)lv3.getX() + 0.99F), g, (double)((float)lv3.getZ() + 0.99F))).offset(-cameraX, -cameraY, -cameraZ), 0.0F, 1.0F, 0.0F, 0.15F);
         }
      }

      var11 = BlockPos.iterate(lv.add(-10, -10, -10), lv.add(10, 10, 10)).iterator();

      while(var11.hasNext()) {
         lv3 = (BlockPos)var11.next();
         lv4 = lv2.getFluidState(lv3);
         if (lv4.isIn(FluidTags.WATER)) {
            DebugRenderer.drawString(matrices, vertexConsumers, String.valueOf(lv4.getLevel()), (double)lv3.getX() + 0.5, (double)((float)lv3.getY() + lv4.getHeight(lv2, lv3)), (double)lv3.getZ() + 0.5, -16777216);
         }
      }

   }
}
