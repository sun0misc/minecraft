package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class RotatingCubeMapRenderer {
   private final MinecraftClient client;
   private final CubeMapRenderer cubeMap;
   private float pitch;
   private float yaw;

   public RotatingCubeMapRenderer(CubeMapRenderer cubeMap) {
      this.cubeMap = cubeMap;
      this.client = MinecraftClient.getInstance();
   }

   public void render(float delta, float alpha) {
      float h = (float)((double)delta * (Double)this.client.options.getPanoramaSpeed().getValue());
      this.pitch = wrapOnce(this.pitch + h * 0.1F, 360.0F);
      this.yaw = wrapOnce(this.yaw + h * 0.001F, 6.2831855F);
      this.cubeMap.draw(this.client, 10.0F, -this.pitch, alpha);
   }

   private static float wrapOnce(float a, float b) {
      return a > b ? a - b : a;
   }
}
