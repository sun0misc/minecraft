package net.minecraft.client.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class TextureManager implements ResourceReloader, TextureTickListener, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Identifier MISSING_IDENTIFIER = new Identifier("");
   private final Map textures = Maps.newHashMap();
   private final Set tickListeners = Sets.newHashSet();
   private final Map dynamicIdCounters = Maps.newHashMap();
   private final ResourceManager resourceContainer;

   public TextureManager(ResourceManager resourceManager) {
      this.resourceContainer = resourceManager;
   }

   public void bindTexture(Identifier id) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this.bindTextureInner(id);
         });
      } else {
         this.bindTextureInner(id);
      }

   }

   private void bindTextureInner(Identifier id) {
      AbstractTexture lv = (AbstractTexture)this.textures.get(id);
      if (lv == null) {
         lv = new ResourceTexture(id);
         this.registerTexture(id, (AbstractTexture)lv);
      }

      ((AbstractTexture)lv).bindTexture();
   }

   public void registerTexture(Identifier id, AbstractTexture texture) {
      texture = this.loadTexture(id, texture);
      AbstractTexture lv = (AbstractTexture)this.textures.put(id, texture);
      if (lv != texture) {
         if (lv != null && lv != MissingSprite.getMissingSpriteTexture()) {
            this.tickListeners.remove(lv);
            this.closeTexture(id, lv);
         }

         if (texture instanceof TextureTickListener) {
            this.tickListeners.add((TextureTickListener)texture);
         }
      }

   }

   private void closeTexture(Identifier id, AbstractTexture texture) {
      if (texture != MissingSprite.getMissingSpriteTexture()) {
         try {
            texture.close();
         } catch (Exception var4) {
            LOGGER.warn("Failed to close texture {}", id, var4);
         }
      }

      texture.clearGlId();
   }

   private AbstractTexture loadTexture(Identifier id, AbstractTexture texture) {
      try {
         texture.load(this.resourceContainer);
         return texture;
      } catch (IOException var6) {
         if (id != MISSING_IDENTIFIER) {
            LOGGER.warn("Failed to load texture: {}", id, var6);
         }

         return MissingSprite.getMissingSpriteTexture();
      } catch (Throwable var7) {
         CrashReport lv = CrashReport.create(var7, "Registering texture");
         CrashReportSection lv2 = lv.addElement("Resource location being registered");
         lv2.add("Resource location", (Object)id);
         lv2.add("Texture object class", () -> {
            return texture.getClass().getName();
         });
         throw new CrashException(lv);
      }
   }

   public AbstractTexture getTexture(Identifier id) {
      AbstractTexture lv = (AbstractTexture)this.textures.get(id);
      if (lv == null) {
         lv = new ResourceTexture(id);
         this.registerTexture(id, (AbstractTexture)lv);
      }

      return (AbstractTexture)lv;
   }

   public AbstractTexture getOrDefault(Identifier id, AbstractTexture fallback) {
      return (AbstractTexture)this.textures.getOrDefault(id, fallback);
   }

   public Identifier registerDynamicTexture(String prefix, NativeImageBackedTexture texture) {
      Integer integer = (Integer)this.dynamicIdCounters.get(prefix);
      if (integer == null) {
         integer = 1;
      } else {
         integer = integer + 1;
      }

      this.dynamicIdCounters.put(prefix, integer);
      Identifier lv = new Identifier(String.format(Locale.ROOT, "dynamic/%s_%d", prefix, integer));
      this.registerTexture(lv, texture);
      return lv;
   }

   public CompletableFuture loadTextureAsync(Identifier id, Executor executor) {
      if (!this.textures.containsKey(id)) {
         AsyncTexture lv = new AsyncTexture(this.resourceContainer, id, executor);
         this.textures.put(id, lv);
         return lv.getLoadCompleteFuture().thenRunAsync(() -> {
            this.registerTexture(id, lv);
         }, TextureManager::runOnRenderThread);
      } else {
         return CompletableFuture.completedFuture((Object)null);
      }
   }

   private static void runOnRenderThread(Runnable runnable) {
      MinecraftClient.getInstance().execute(() -> {
         Objects.requireNonNull(runnable);
         RenderSystem.recordRenderCall(runnable::run);
      });
   }

   public void tick() {
      Iterator var1 = this.tickListeners.iterator();

      while(var1.hasNext()) {
         TextureTickListener lv = (TextureTickListener)var1.next();
         lv.tick();
      }

   }

   public void destroyTexture(Identifier id) {
      AbstractTexture lv = this.getOrDefault(id, MissingSprite.getMissingSpriteTexture());
      if (lv != MissingSprite.getMissingSpriteTexture()) {
         TextureUtil.releaseTextureId(lv.getGlId());
      }

   }

   public void close() {
      this.textures.forEach(this::closeTexture);
      this.textures.clear();
      this.tickListeners.clear();
      this.dynamicIdCounters.clear();
   }

   public CompletableFuture reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      CompletableFuture completableFuture = new CompletableFuture();
      CompletableFuture var10000 = CompletableFuture.allOf(TitleScreen.loadTexturesAsync(this, prepareExecutor), this.loadTextureAsync(ClickableWidget.WIDGETS_TEXTURE, prepareExecutor));
      Objects.requireNonNull(synchronizer);
      var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((void_) -> {
         MissingSprite.getMissingSpriteTexture();
         RealmsMainScreen.loadImages(this.resourceContainer);
         Iterator iterator = this.textures.entrySet().iterator();

         while(true) {
            while(iterator.hasNext()) {
               Map.Entry entry = (Map.Entry)iterator.next();
               Identifier lv = (Identifier)entry.getKey();
               AbstractTexture lv2 = (AbstractTexture)entry.getValue();
               if (lv2 == MissingSprite.getMissingSpriteTexture() && !lv.equals(MissingSprite.getMissingSpriteId())) {
                  iterator.remove();
               } else {
                  lv2.registerTexture(this, manager, lv, applyExecutor);
               }
            }

            MinecraftClient.getInstance().send(() -> {
               completableFuture.complete((Object)null);
            });
            return;
         }
      }, (runnable) -> {
         Objects.requireNonNull(runnable);
         RenderSystem.recordRenderCall(runnable::run);
      });
      return completableFuture;
   }

   public void dumpDynamicTextures(Path path) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this.dumpDynamicTexturesInternal(path);
         });
      } else {
         this.dumpDynamicTexturesInternal(path);
      }

   }

   private void dumpDynamicTexturesInternal(Path path) {
      try {
         Files.createDirectories(path);
      } catch (IOException var3) {
         LOGGER.error("Failed to create directory {}", path, var3);
         return;
      }

      this.textures.forEach((id, texture) -> {
         if (texture instanceof DynamicTexture lv) {
            try {
               lv.save(id, path);
            } catch (IOException var5) {
               LOGGER.error("Failed to dump texture {}", id, var5);
            }
         }

      });
   }
}
