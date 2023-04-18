package net.minecraft.client.render.debug;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class BlockOutlineDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;

   public BlockOutlineDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      BlockView lv = this.client.player.world;
      BlockPos lv2 = BlockPos.ofFloored(cameraX, cameraY, cameraZ);
      Iterator var12 = BlockPos.iterate(lv2.add(-6, -6, -6), lv2.add(6, 6, 6)).iterator();

      while(true) {
         BlockPos lv3;
         BlockState lv4;
         do {
            if (!var12.hasNext()) {
               return;
            }

            lv3 = (BlockPos)var12.next();
            lv4 = lv.getBlockState(lv3);
         } while(lv4.isOf(Blocks.AIR));

         VoxelShape lv5 = lv4.getOutlineShape(lv, lv3);
         Iterator var16 = lv5.getBoundingBoxes().iterator();

         while(var16.hasNext()) {
            Box lv6 = (Box)var16.next();
            Box lv7 = lv6.offset(lv3).expand(0.002);
            float g = (float)(lv7.minX - cameraX);
            float h = (float)(lv7.minY - cameraY);
            float i = (float)(lv7.minZ - cameraZ);
            float j = (float)(lv7.maxX - cameraX);
            float k = (float)(lv7.maxY - cameraY);
            float l = (float)(lv7.maxZ - cameraZ);
            float m = 1.0F;
            float n = 0.0F;
            float o = 0.0F;
            float p = 0.5F;
            VertexConsumer lv8;
            if (lv4.isSideSolidFullSquare(lv, lv3, Direction.WEST)) {
               lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
               lv8.vertex(matrix4f, g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, g, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, g, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, g, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
            }

            if (lv4.isSideSolidFullSquare(lv, lv3, Direction.SOUTH)) {
               lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
               lv8.vertex(matrix4f, g, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, g, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, j, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, j, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
            }

            if (lv4.isSideSolidFullSquare(lv, lv3, Direction.EAST)) {
               lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
               lv8.vertex(matrix4f, j, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, j, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, j, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, j, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
            }

            if (lv4.isSideSolidFullSquare(lv, lv3, Direction.NORTH)) {
               lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
               lv8.vertex(matrix4f, j, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, j, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, g, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
            }

            if (lv4.isSideSolidFullSquare(lv, lv3, Direction.DOWN)) {
               lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
               lv8.vertex(matrix4f, g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, j, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, g, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, j, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
            }

            if (lv4.isSideSolidFullSquare(lv, lv3, Direction.UP)) {
               lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
               lv8.vertex(matrix4f, g, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, g, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, j, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               lv8.vertex(matrix4f, j, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).next();
            }
         }
      }
   }
}
