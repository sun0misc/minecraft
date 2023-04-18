package net.minecraft.client.gui.screen;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class TitleScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String DEMO_WORLD_NAME = "Demo_World";
   public static final Text COPYRIGHT = Text.literal("Copyright Mojang AB. Do not distribute!");
   public static final CubeMapRenderer PANORAMA_CUBE_MAP = new CubeMapRenderer(new Identifier("textures/gui/title/background/panorama"));
   private static final Identifier PANORAMA_OVERLAY = new Identifier("textures/gui/title/background/panorama_overlay.png");
   @Nullable
   private String splashText;
   private ButtonWidget buttonResetDemo;
   @Nullable
   private RealmsNotificationsScreen realmsNotificationGui;
   private final RotatingCubeMapRenderer backgroundRenderer;
   private final boolean doBackgroundFade;
   private long backgroundFadeStart;
   @Nullable
   private DeprecationNotice deprecationNotice;
   private final LogoDrawer logoDrawer;

   public TitleScreen() {
      this(false);
   }

   public TitleScreen(boolean doBackgroundFade) {
      this(doBackgroundFade, (LogoDrawer)null);
   }

   public TitleScreen(boolean doBackgroundFade, @Nullable LogoDrawer logoDrawer) {
      super(Text.translatable("narrator.screen.title"));
      this.backgroundRenderer = new RotatingCubeMapRenderer(PANORAMA_CUBE_MAP);
      this.doBackgroundFade = doBackgroundFade;
      this.logoDrawer = (LogoDrawer)Objects.requireNonNullElseGet(logoDrawer, () -> {
         return new LogoDrawer(false);
      });
   }

   private boolean isRealmsNotificationsGuiDisplayed() {
      return this.realmsNotificationGui != null;
   }

   public void tick() {
      if (this.isRealmsNotificationsGuiDisplayed()) {
         this.realmsNotificationGui.tick();
      }

      this.client.getRealms32BitWarningChecker().showWarningIfNeeded(this);
   }

   public static CompletableFuture loadTexturesAsync(TextureManager textureManager, Executor executor) {
      return CompletableFuture.allOf(textureManager.loadTextureAsync(LogoDrawer.LOGO_TEXTURE, executor), textureManager.loadTextureAsync(LogoDrawer.EDITION_TEXTURE, executor), textureManager.loadTextureAsync(PANORAMA_OVERLAY, executor), PANORAMA_CUBE_MAP.loadTexturesAsync(textureManager, executor));
   }

   public boolean shouldPause() {
      return false;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected void init() {
      if (this.splashText == null) {
         this.splashText = this.client.getSplashTextLoader().get();
      }

      int i = this.textRenderer.getWidth((StringVisitable)COPYRIGHT);
      int j = this.width - i - 2;
      int k = true;
      int l = this.height / 4 + 48;
      if (this.client.isDemo()) {
         this.initWidgetsDemo(l, 24);
      } else {
         this.initWidgetsNormal(l, 24);
      }

      this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 124, l + 72 + 12, 20, 20, 0, 106, 20, ButtonWidget.WIDGETS_TEXTURE, 256, 256, (button) -> {
         this.client.setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager()));
      }, Text.translatable("narrator.button.language")));
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.options"), (button) -> {
         this.client.setScreen(new OptionsScreen(this, this.client.options));
      }).dimensions(this.width / 2 - 100, l + 72 + 12, 98, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.quit"), (button) -> {
         this.client.scheduleStop();
      }).dimensions(this.width / 2 + 2, l + 72 + 12, 98, 20).build());
      this.addDrawableChild(new TexturedButtonWidget(this.width / 2 + 104, l + 72 + 12, 20, 20, 0, 0, 20, ButtonWidget.ACCESSIBILITY_TEXTURE, 32, 64, (button) -> {
         this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options));
      }, Text.translatable("narrator.button.accessibility")));
      this.addDrawableChild(new PressableTextWidget(j, this.height - 10, i, 10, COPYRIGHT, (button) -> {
         this.client.setScreen(new CreditsAndAttributionScreen(this));
      }, this.textRenderer));
      this.client.setConnectedToRealms(false);
      if (this.realmsNotificationGui == null) {
         this.realmsNotificationGui = new RealmsNotificationsScreen();
      }

      if (this.isRealmsNotificationsGuiDisplayed()) {
         this.realmsNotificationGui.init(this.client, this.width, this.height);
      }

      if (!this.client.is64Bit()) {
         this.deprecationNotice = new DeprecationNotice(this.textRenderer, MultilineText.create(this.textRenderer, Text.translatable("title.32bit.deprecation"), 350, 2), this.width / 2, l - 24);
      }

   }

   private void initWidgetsNormal(int y, int spacingY) {
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.singleplayer"), (button) -> {
         this.client.setScreen(new SelectWorldScreen(this));
      }).dimensions(this.width / 2 - 100, y, 200, 20).build());
      Text lv = this.getMultiplayerDisabledText();
      boolean bl = lv == null;
      Tooltip lv2 = lv != null ? Tooltip.of(lv) : null;
      ((ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.multiplayer"), (button) -> {
         Screen lv = this.client.options.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this);
         this.client.setScreen((Screen)lv);
      }).dimensions(this.width / 2 - 100, y + spacingY * 1, 200, 20).tooltip(lv2).build())).active = bl;
      ((ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.online"), (button) -> {
         this.switchToRealms();
      }).dimensions(this.width / 2 - 100, y + spacingY * 2, 200, 20).tooltip(lv2).build())).active = bl;
   }

   @Nullable
   private Text getMultiplayerDisabledText() {
      if (this.client.isMultiplayerEnabled()) {
         return null;
      } else {
         BanDetails banDetails = this.client.getMultiplayerBanDetails();
         if (banDetails != null) {
            return banDetails.expires() != null ? Text.translatable("title.multiplayer.disabled.banned.temporary") : Text.translatable("title.multiplayer.disabled.banned.permanent");
         } else {
            return Text.translatable("title.multiplayer.disabled");
         }
      }
   }

   private void initWidgetsDemo(int y, int spacingY) {
      boolean bl = this.canReadDemoWorldData();
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.playdemo"), (button) -> {
         if (bl) {
            this.client.createIntegratedServerLoader().start(this, "Demo_World");
         } else {
            this.client.createIntegratedServerLoader().createAndStart("Demo_World", MinecraftServer.DEMO_LEVEL_INFO, GeneratorOptions.DEMO_OPTIONS, WorldPresets::createDemoOptions);
         }

      }).dimensions(this.width / 2 - 100, y, 200, 20).build());
      this.buttonResetDemo = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.resetdemo"), (button) -> {
         LevelStorage lv = this.client.getLevelStorage();

         try {
            LevelStorage.Session lv2 = lv.createSession("Demo_World");

            try {
               LevelSummary lv3 = lv2.getLevelSummary();
               if (lv3 != null) {
                  this.client.setScreen(new ConfirmScreen(this::onDemoDeletionConfirmed, Text.translatable("selectWorld.deleteQuestion"), Text.translatable("selectWorld.deleteWarning", lv3.getDisplayName()), Text.translatable("selectWorld.deleteButton"), ScreenTexts.CANCEL));
               }
            } catch (Throwable var7) {
               if (lv2 != null) {
                  try {
                     lv2.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (lv2 != null) {
               lv2.close();
            }
         } catch (IOException var8) {
            SystemToast.addWorldAccessFailureToast(this.client, "Demo_World");
            LOGGER.warn("Failed to access demo world", var8);
         }

      }).dimensions(this.width / 2 - 100, y + spacingY * 1, 200, 20).build());
      this.buttonResetDemo.active = bl;
   }

   private boolean canReadDemoWorldData() {
      try {
         LevelStorage.Session lv = this.client.getLevelStorage().createSession("Demo_World");

         boolean var2;
         try {
            var2 = lv.getLevelSummary() != null;
         } catch (Throwable var5) {
            if (lv != null) {
               try {
                  lv.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (lv != null) {
            lv.close();
         }

         return var2;
      } catch (IOException var6) {
         SystemToast.addWorldAccessFailureToast(this.client, "Demo_World");
         LOGGER.warn("Failed to read demo world data", var6);
         return false;
      }
   }

   private void switchToRealms() {
      this.client.setScreen(new RealmsMainScreen(this));
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
         this.backgroundFadeStart = Util.getMeasuringTimeMs();
      }

      float g = this.doBackgroundFade ? (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
      this.backgroundRenderer.render(delta, MathHelper.clamp(g, 0.0F, 1.0F));
      RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.doBackgroundFade ? (float)MathHelper.ceil(MathHelper.clamp(g, 0.0F, 1.0F)) : 1.0F);
      drawTexture(matrices, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      float h = this.doBackgroundFade ? MathHelper.clamp(g - 1.0F, 0.0F, 1.0F) : 1.0F;
      this.logoDrawer.draw(matrices, this.width, h);
      int k = MathHelper.ceil(h * 255.0F) << 24;
      if ((k & -67108864) != 0) {
         if (this.deprecationNotice != null) {
            this.deprecationNotice.render(matrices, k);
         }

         if (this.splashText != null) {
            matrices.push();
            matrices.translate((float)(this.width / 2 + 90), 70.0F, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20.0F));
            float l = 1.8F - MathHelper.abs(MathHelper.sin((float)(Util.getMeasuringTimeMs() % 1000L) / 1000.0F * 6.2831855F) * 0.1F);
            l = l * 100.0F / (float)(this.textRenderer.getWidth(this.splashText) + 32);
            matrices.scale(l, l, l);
            drawCenteredTextWithShadow(matrices, this.textRenderer, this.splashText, 0, -8, 16776960 | k);
            matrices.pop();
         }

         String string = "Minecraft " + SharedConstants.getGameVersion().getName();
         if (this.client.isDemo()) {
            string = string + " Demo";
         } else {
            string = string + ("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType());
         }

         if (MinecraftClient.getModStatus().isModded()) {
            string = string + I18n.translate("menu.modded");
         }

         drawTextWithShadow(matrices, this.textRenderer, string, 2, this.height - 10, 16777215 | k);
         Iterator var9 = this.children().iterator();

         while(var9.hasNext()) {
            Element lv = (Element)var9.next();
            if (lv instanceof ClickableWidget) {
               ((ClickableWidget)lv).setAlpha(h);
            }
         }

         super.render(matrices, mouseX, mouseY, delta);
         if (this.isRealmsNotificationsGuiDisplayed() && h >= 1.0F) {
            RenderSystem.enableDepthTest();
            this.realmsNotificationGui.render(matrices, mouseX, mouseY, delta);
         }

      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         return this.isRealmsNotificationsGuiDisplayed() && this.realmsNotificationGui.mouseClicked(mouseX, mouseY, button);
      }
   }

   public void removed() {
      if (this.realmsNotificationGui != null) {
         this.realmsNotificationGui.removed();
      }

   }

   public void onDisplayed() {
      super.onDisplayed();
      if (this.realmsNotificationGui != null) {
         this.realmsNotificationGui.onDisplayed();
      }

   }

   private void onDemoDeletionConfirmed(boolean delete) {
      if (delete) {
         try {
            LevelStorage.Session lv = this.client.getLevelStorage().createSession("Demo_World");

            try {
               lv.deleteSessionLock();
            } catch (Throwable var6) {
               if (lv != null) {
                  try {
                     lv.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (lv != null) {
               lv.close();
            }
         } catch (IOException var7) {
            SystemToast.addWorldDeleteFailureToast(this.client, "Demo_World");
            LOGGER.warn("Failed to delete demo world", var7);
         }
      }

      this.client.setScreen(this);
   }

   @Environment(EnvType.CLIENT)
   private static record DeprecationNotice(TextRenderer textRenderer, MultilineText label, int x, int y) {
      DeprecationNotice(TextRenderer arg, MultilineText arg2, int i, int j) {
         this.textRenderer = arg;
         this.label = arg2;
         this.x = i;
         this.y = j;
      }

      public void render(MatrixStack matrices, int color) {
         MultilineText var10000 = this.label;
         int var10002 = this.x;
         int var10003 = this.y;
         Objects.requireNonNull(this.textRenderer);
         var10000.fillBackground(matrices, var10002, var10003, 9, 2, 2097152 | Math.min(color, 1426063360));
         var10000 = this.label;
         var10002 = this.x;
         var10003 = this.y;
         Objects.requireNonNull(this.textRenderer);
         var10000.drawCenterWithShadow(matrices, var10002, var10003, 9, 16777215 | color);
      }

      public TextRenderer textRenderer() {
         return this.textRenderer;
      }

      public MultilineText label() {
         return this.label;
      }

      public int x() {
         return this.x;
      }

      public int y() {
         return this.y;
      }
   }
}
