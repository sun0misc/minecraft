package net.minecraft.client.render.debug;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class WorldGenAttemptDebugRenderer implements DebugRenderer.Renderer {
   private final List positions = Lists.newArrayList();
   private final List sizes = Lists.newArrayList();
   private final List alphas = Lists.newArrayList();
   private final List reds = Lists.newArrayList();
   private final List greens = Lists.newArrayList();
   private final List blues = Lists.newArrayList();

   public void addBox(BlockPos pos, float size, float red, float green, float blue, float alpha) {
      this.positions.add(pos);
      this.sizes.add(size);
      this.alphas.add(alpha);
      this.reds.add(red);
      this.greens.add(green);
      this.blues.add(blue);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());

      for(int i = 0; i < this.positions.size(); ++i) {
         BlockPos lv2 = (BlockPos)this.positions.get(i);
         Float float_ = (Float)this.sizes.get(i);
         float g = float_ / 2.0F;
         WorldRenderer.method_3258(matrices, lv, (double)((float)lv2.getX() + 0.5F - g) - cameraX, (double)((float)lv2.getY() + 0.5F - g) - cameraY, (double)((float)lv2.getZ() + 0.5F - g) - cameraZ, (double)((float)lv2.getX() + 0.5F + g) - cameraX, (double)((float)lv2.getY() + 0.5F + g) - cameraY, (double)((float)lv2.getZ() + 0.5F + g) - cameraZ, (Float)this.reds.get(i), (Float)this.greens.get(i), (Float)this.blues.get(i), (Float)this.alphas.get(i));
      }

   }
}
