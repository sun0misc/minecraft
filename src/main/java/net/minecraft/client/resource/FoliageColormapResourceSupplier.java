package net.minecraft.client.resource;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.util.RawTextureDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Environment(EnvType.CLIENT)
public class FoliageColormapResourceSupplier extends SinglePreparationResourceReloader {
   private static final Identifier FOLIAGE_COLORMAP = new Identifier("textures/colormap/foliage.png");

   protected int[] reload(ResourceManager resourceManager, Profiler profiler) {
      try {
         return RawTextureDataLoader.loadRawTextureData(resourceManager, FOLIAGE_COLORMAP);
      } catch (IOException var4) {
         throw new IllegalStateException("Failed to load foliage color texture", var4);
      }
   }

   protected void apply(int[] is, ResourceManager arg, Profiler arg2) {
      FoliageColors.setColorMap(is);
   }

   // $FF: synthetic method
   protected Object prepare(ResourceManager manager, Profiler profiler) {
      return this.reload(manager, profiler);
   }
}
