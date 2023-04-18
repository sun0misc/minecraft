package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SplashOverlay extends Overlay {
   static final Identifier LOGO = new Identifier("textures/gui/title/mojangstudios.png");
   private static final int MOJANG_RED = ColorHelper.Argb.getArgb(255, 239, 50, 61);
   private static final int MONOCHROME_BLACK = ColorHelper.Argb.getArgb(255, 0, 0, 0);
   private static final IntSupplier BRAND_ARGB = () -> {
      return (Boolean)MinecraftClient.getInstance().options.getMonochromeLogo().getValue() ? MONOCHROME_BLACK : MOJANG_RED;
   };
   private static final int field_32251 = 240;
   private static final float LOGO_RIGHT_HALF_V = 60.0F;
   private static final int field_32253 = 60;
   private static final int field_32254 = 120;
   private static final float LOGO_OVERLAP = 0.0625F;
   private static final float PROGRESS_LERP_DELTA = 0.95F;
   public static final long RELOAD_COMPLETE_FADE_DURATION = 1000L;
   public static final long RELOAD_START_FADE_DURATION = 500L;
   private final MinecraftClient client;
   private final ResourceReload reload;
   private final Consumer exceptionHandler;
   private final boolean reloading;
   private float progress;
   private long reloadCompleteTime = -1L;
   private long reloadStartTime = -1L;

   public SplashOverlay(MinecraftClient client, ResourceReload monitor, Consumer exceptionHandler, boolean reloading) {
      this.client = client;
      this.reload = monitor;
      this.exceptionHandler = exceptionHandler;
      this.reloading = reloading;
   }

   public static void init(MinecraftClient client) {
      client.getTextureManager().registerTexture(LOGO, new LogoTexture());
   }

   private static int withAlpha(int color, int alpha) {
      return color & 16777215 | alpha << 24;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      int k = this.client.getWindow().getScaledWidth();
      int l = this.client.getWindow().getScaledHeight();
      long m = Util.getMeasuringTimeMs();
      if (this.reloading && this.reloadStartTime == -1L) {
         this.reloadStartTime = m;
      }

      float g = this.reloadCompleteTime > -1L ? (float)(m - this.reloadCompleteTime) / 1000.0F : -1.0F;
      float h = this.reloadStartTime > -1L ? (float)(m - this.reloadStartTime) / 500.0F : -1.0F;
      float o;
      int n;
      if (g >= 1.0F) {
         if (this.client.currentScreen != null) {
            this.client.currentScreen.render(matrices, 0, 0, delta);
         }

         n = MathHelper.ceil((1.0F - MathHelper.clamp(g - 1.0F, 0.0F, 1.0F)) * 255.0F);
         fill(matrices, 0, 0, k, l, withAlpha(BRAND_ARGB.getAsInt(), n));
         o = 1.0F - MathHelper.clamp(g - 1.0F, 0.0F, 1.0F);
      } else if (this.reloading) {
         if (this.client.currentScreen != null && h < 1.0F) {
            this.client.currentScreen.render(matrices, mouseX, mouseY, delta);
         }

         n = MathHelper.ceil(MathHelper.clamp((double)h, 0.15, 1.0) * 255.0);
         fill(matrices, 0, 0, k, l, withAlpha(BRAND_ARGB.getAsInt(), n));
         o = MathHelper.clamp(h, 0.0F, 1.0F);
      } else {
         n = BRAND_ARGB.getAsInt();
         float p = (float)(n >> 16 & 255) / 255.0F;
         float q = (float)(n >> 8 & 255) / 255.0F;
         float r = (float)(n & 255) / 255.0F;
         GlStateManager._clearColor(p, q, r, 1.0F);
         GlStateManager._clear(16384, MinecraftClient.IS_SYSTEM_MAC);
         o = 1.0F;
      }

      n = (int)((double)this.client.getWindow().getScaledWidth() * 0.5);
      int s = (int)((double)this.client.getWindow().getScaledHeight() * 0.5);
      double d = Math.min((double)this.client.getWindow().getScaledWidth() * 0.75, (double)this.client.getWindow().getScaledHeight()) * 0.25;
      int t = (int)(d * 0.5);
      double e = d * 4.0;
      int u = (int)(e * 0.5);
      RenderSystem.setShaderTexture(0, LOGO);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, o);
      drawTexture(matrices, n - u, s - t, u, (int)d, -0.0625F, 0.0F, 120, 60, 120, 120);
      drawTexture(matrices, n, s - t, u, (int)d, 0.0625F, 60.0F, 120, 60, 120, 120);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      int v = (int)((double)this.client.getWindow().getScaledHeight() * 0.8325);
      float w = this.reload.getProgress();
      this.progress = MathHelper.clamp(this.progress * 0.95F + w * 0.050000012F, 0.0F, 1.0F);
      if (g < 1.0F) {
         this.renderProgressBar(matrices, k / 2 - u, v - 5, k / 2 + u, v + 5, 1.0F - MathHelper.clamp(g, 0.0F, 1.0F));
      }

      if (g >= 2.0F) {
         this.client.setOverlay((Overlay)null);
      }

      if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || h >= 2.0F)) {
         try {
            this.reload.throwException();
            this.exceptionHandler.accept(Optional.empty());
         } catch (Throwable var23) {
            this.exceptionHandler.accept(Optional.of(var23));
         }

         this.reloadCompleteTime = Util.getMeasuringTimeMs();
         if (this.client.currentScreen != null) {
            this.client.currentScreen.init(this.client, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight());
         }
      }

   }

   private void renderProgressBar(MatrixStack matrices, int minX, int minY, int maxX, int maxY, float opacity) {
      int m = MathHelper.ceil((float)(maxX - minX - 2) * this.progress);
      int n = Math.round(opacity * 255.0F);
      int o = ColorHelper.Argb.getArgb(n, 255, 255, 255);
      fill(matrices, minX + 2, minY + 2, minX + m, maxY - 2, o);
      fill(matrices, minX + 1, minY, maxX - 1, minY + 1, o);
      fill(matrices, minX + 1, maxY, maxX - 1, maxY - 1, o);
      fill(matrices, minX, minY, minX + 1, maxY, o);
      fill(matrices, maxX, minY, maxX - 1, maxY, o);
   }

   public boolean pausesGame() {
      return true;
   }

   @Environment(EnvType.CLIENT)
   static class LogoTexture extends ResourceTexture {
      public LogoTexture() {
         super(SplashOverlay.LOGO);
      }

      protected ResourceTexture.TextureData loadTextureData(ResourceManager resourceManager) {
         DefaultResourcePack lv = MinecraftClient.getInstance().getDefaultResourcePack();
         InputSupplier lv2 = lv.open(ResourceType.CLIENT_RESOURCES, SplashOverlay.LOGO);
         if (lv2 == null) {
            return new ResourceTexture.TextureData(new FileNotFoundException(SplashOverlay.LOGO.toString()));
         } else {
            try {
               InputStream inputStream = (InputStream)lv2.get();

               ResourceTexture.TextureData var5;
               try {
                  var5 = new ResourceTexture.TextureData(new TextureResourceMetadata(true, true), NativeImage.read(inputStream));
               } catch (Throwable var8) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (inputStream != null) {
                  inputStream.close();
               }

               return var5;
            } catch (IOException var9) {
               return new ResourceTexture.TextureData(var9);
            }
         }
      }
   }
}
