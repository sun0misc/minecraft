package net.minecraft.client.gui.screen;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkStatus;

@Environment(EnvType.CLIENT)
public class LevelLoadingScreen extends Screen {
   private static final long NARRATION_DELAY = 2000L;
   private final WorldGenerationProgressTracker progressProvider;
   private long lastNarrationTime = -1L;
   private boolean done;
   private static final Object2IntMap STATUS_TO_COLOR = (Object2IntMap)Util.make(new Object2IntOpenHashMap(), (map) -> {
      map.defaultReturnValue(0);
      map.put(ChunkStatus.EMPTY, 5526612);
      map.put(ChunkStatus.STRUCTURE_STARTS, 10066329);
      map.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
      map.put(ChunkStatus.BIOMES, 8434258);
      map.put(ChunkStatus.NOISE, 13750737);
      map.put(ChunkStatus.SURFACE, 7497737);
      map.put(ChunkStatus.CARVERS, 7169628);
      map.put(ChunkStatus.LIQUID_CARVERS, 3159410);
      map.put(ChunkStatus.FEATURES, 2213376);
      map.put(ChunkStatus.LIGHT, 13421772);
      map.put(ChunkStatus.SPAWN, 15884384);
      map.put(ChunkStatus.HEIGHTMAPS, 15658734);
      map.put(ChunkStatus.FULL, 16777215);
   });

   public LevelLoadingScreen(WorldGenerationProgressTracker progressProvider) {
      super(NarratorManager.EMPTY);
      this.progressProvider = progressProvider;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected boolean hasUsageText() {
      return false;
   }

   public void removed() {
      this.done = true;
      this.narrateScreenIfNarrationEnabled(true);
   }

   protected void addElementNarrations(NarrationMessageBuilder builder) {
      if (this.done) {
         builder.put(NarrationPart.TITLE, (Text)Text.translatable("narrator.loading.done"));
      } else {
         String string = this.getPercentage();
         builder.put(NarrationPart.TITLE, string);
      }

   }

   private String getPercentage() {
      int var10000 = this.progressProvider.getProgressPercentage();
      return MathHelper.clamp(var10000, 0, 100) + "%";
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      long l = Util.getMeasuringTimeMs();
      if (l - this.lastNarrationTime > 2000L) {
         this.lastNarrationTime = l;
         this.narrateScreenIfNarrationEnabled(true);
      }

      int k = this.width / 2;
      int m = this.height / 2;
      int n = true;
      drawChunkMap(matrices, this.progressProvider, k, m + 30, 2, 0);
      TextRenderer var10001 = this.textRenderer;
      String var10002 = this.getPercentage();
      Objects.requireNonNull(this.textRenderer);
      drawCenteredTextWithShadow(matrices, var10001, var10002, k, m - 9 / 2 - 30, 16777215);
   }

   public static void drawChunkMap(MatrixStack matrices, WorldGenerationProgressTracker progressProvider, int centerX, int centerY, int pixelSize, int pixelMargin) {
      int m = pixelSize + pixelMargin;
      int n = progressProvider.getCenterSize();
      int o = n * m - pixelMargin;
      int p = progressProvider.getSize();
      int q = p * m - pixelMargin;
      int r = centerX - q / 2;
      int s = centerY - q / 2;
      int t = o / 2 + 1;
      int u = -16772609;
      if (pixelMargin != 0) {
         fill(matrices, centerX - t, centerY - t, centerX - t + 1, centerY + t, -16772609);
         fill(matrices, centerX + t - 1, centerY - t, centerX + t, centerY + t, -16772609);
         fill(matrices, centerX - t, centerY - t, centerX + t, centerY - t + 1, -16772609);
         fill(matrices, centerX - t, centerY + t - 1, centerX + t, centerY + t, -16772609);
      }

      for(int v = 0; v < p; ++v) {
         for(int w = 0; w < p; ++w) {
            ChunkStatus lv = progressProvider.getChunkStatus(v, w);
            int x = r + v * m;
            int y = s + w * m;
            fill(matrices, x, y, x + pixelSize, y + pixelSize, STATUS_TO_COLOR.getInt(lv) | -16777216);
         }
      }

   }
}
