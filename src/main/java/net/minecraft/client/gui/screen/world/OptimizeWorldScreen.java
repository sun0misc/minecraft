package net.minecraft.client.gui.screen.world;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.util.Iterator;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.SaveLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.updater.WorldUpdater;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class OptimizeWorldScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Object2IntMap DIMENSION_COLORS = (Object2IntMap)Util.make(new Object2IntOpenCustomHashMap(Util.identityHashStrategy()), (colors) -> {
      colors.put(World.OVERWORLD, -13408734);
      colors.put(World.NETHER, -10075085);
      colors.put(World.END, -8943531);
      colors.defaultReturnValue(-2236963);
   });
   private final BooleanConsumer callback;
   private final WorldUpdater updater;

   @Nullable
   public static OptimizeWorldScreen create(MinecraftClient client, BooleanConsumer callback, DataFixer dataFixer, LevelStorage.Session storageSession, boolean eraseCache) {
      try {
         SaveLoader lv = client.createIntegratedServerLoader().createSaveLoader(storageSession, false);

         OptimizeWorldScreen var8;
         try {
            SaveProperties lv2 = lv.saveProperties();
            DynamicRegistryManager.Immutable lv3 = lv.combinedDynamicRegistries().getCombinedRegistryManager();
            storageSession.backupLevelDataFile(lv3, lv2);
            var8 = new OptimizeWorldScreen(callback, dataFixer, storageSession, lv2.getLevelInfo(), eraseCache, lv3.get(RegistryKeys.DIMENSION));
         } catch (Throwable var10) {
            if (lv != null) {
               try {
                  lv.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (lv != null) {
            lv.close();
         }

         return var8;
      } catch (Exception var11) {
         LOGGER.warn("Failed to load datapacks, can't optimize world", var11);
         return null;
      }
   }

   private OptimizeWorldScreen(BooleanConsumer callback, DataFixer dataFixer, LevelStorage.Session storageSession, LevelInfo levelInfo, boolean eraseCache, Registry dimensionOptionsRegistry) {
      super(Text.translatable("optimizeWorld.title", levelInfo.getLevelName()));
      this.callback = callback;
      this.updater = new WorldUpdater(storageSession, dataFixer, dimensionOptionsRegistry, eraseCache);
   }

   protected void init() {
      super.init();
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.updater.cancel();
         this.callback.accept(false);
      }).dimensions(this.width / 2 - 100, this.height / 4 + 150, 200, 20).build());
   }

   public void tick() {
      if (this.updater.isDone()) {
         this.callback.accept(true);
      }

   }

   public void close() {
      this.callback.accept(false);
   }

   public void removed() {
      this.updater.cancel();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
      int k = this.width / 2 - 150;
      int l = this.width / 2 + 150;
      int m = this.height / 4 + 100;
      int n = m + 10;
      TextRenderer var10001 = this.textRenderer;
      Text var10002 = this.updater.getStatus();
      int var10003 = this.width / 2;
      Objects.requireNonNull(this.textRenderer);
      drawCenteredTextWithShadow(matrices, var10001, var10002, var10003, m - 9 - 2, 10526880);
      if (this.updater.getTotalChunkCount() > 0) {
         fill(matrices, k - 1, m - 1, l + 1, n + 1, -16777216);
         drawTextWithShadow(matrices, this.textRenderer, Text.translatable("optimizeWorld.info.converted", this.updater.getUpgradedChunkCount()), k, 40, 10526880);
         var10001 = this.textRenderer;
         MutableText var14 = Text.translatable("optimizeWorld.info.skipped", this.updater.getSkippedChunkCount());
         Objects.requireNonNull(this.textRenderer);
         drawTextWithShadow(matrices, var10001, var14, k, 40 + 9 + 3, 10526880);
         var10001 = this.textRenderer;
         var14 = Text.translatable("optimizeWorld.info.total", this.updater.getTotalChunkCount());
         Objects.requireNonNull(this.textRenderer);
         drawTextWithShadow(matrices, var10001, var14, k, 40 + (9 + 3) * 2, 10526880);
         int o = 0;

         int p;
         for(Iterator var10 = this.updater.getWorlds().iterator(); var10.hasNext(); o += p) {
            RegistryKey lv = (RegistryKey)var10.next();
            p = MathHelper.floor(this.updater.getProgress(lv) * (float)(l - k));
            fill(matrices, k + o, m, k + o + p, n, DIMENSION_COLORS.getInt(lv));
         }

         int q = this.updater.getUpgradedChunkCount() + this.updater.getSkippedChunkCount();
         var10001 = this.textRenderer;
         String var15 = "" + q + " / " + this.updater.getTotalChunkCount();
         var10003 = this.width / 2;
         Objects.requireNonNull(this.textRenderer);
         drawCenteredTextWithShadow(matrices, var10001, var15, var10003, m + 2 * 9 + 2, 10526880);
         var10001 = this.textRenderer;
         var15 = MathHelper.floor(this.updater.getProgress() * 100.0F) + "%";
         var10003 = this.width / 2;
         int var10004 = m + (n - m) / 2;
         Objects.requireNonNull(this.textRenderer);
         drawCenteredTextWithShadow(matrices, var10001, var15, var10003, var10004 - 9 / 2, 10526880);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }
}
