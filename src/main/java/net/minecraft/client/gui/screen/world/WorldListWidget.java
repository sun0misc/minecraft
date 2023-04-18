package net.minecraft.client.gui.screen.world;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldListWidget extends AlwaysSelectedEntryListWidget {
   static final Logger LOGGER = LogUtils.getLogger();
   static final DateFormat DATE_FORMAT = new SimpleDateFormat();
   static final Identifier UNKNOWN_SERVER_LOCATION = new Identifier("textures/misc/unknown_server.png");
   static final Identifier WORLD_SELECTION_LOCATION = new Identifier("textures/gui/world_selection.png");
   static final Text FROM_NEWER_VERSION_FIRST_LINE;
   static final Text FROM_NEWER_VERSION_SECOND_LINE;
   static final Text SNAPSHOT_FIRST_LINE;
   static final Text SNAPSHOT_SECOND_LINE;
   static final Text LOCKED_TEXT;
   static final Text CONVERSION_TOOLTIP;
   private final SelectWorldScreen parent;
   private CompletableFuture levelsFuture;
   @Nullable
   private List levels;
   private String search;
   private final LoadingEntry loadingEntry;

   public WorldListWidget(SelectWorldScreen parent, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, String search, @Nullable WorldListWidget oldWidget) {
      super(client, width, height, top, bottom, itemHeight);
      this.parent = parent;
      this.loadingEntry = new LoadingEntry(client);
      this.search = search;
      if (oldWidget != null) {
         this.levelsFuture = oldWidget.levelsFuture;
      } else {
         this.levelsFuture = this.loadLevels();
      }

      this.show(this.tryGet());
   }

   @Nullable
   private List tryGet() {
      try {
         return (List)this.levelsFuture.getNow((Object)null);
      } catch (CancellationException | CompletionException var2) {
         return null;
      }
   }

   void load() {
      this.levelsFuture = this.loadLevels();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      List list = this.tryGet();
      if (list != this.levels) {
         this.show(list);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }

   private void show(@Nullable List levels) {
      if (levels == null) {
         this.showLoadingScreen();
      } else {
         this.showSummaries(this.search, levels);
      }

      this.levels = levels;
   }

   public void setSearch(String search) {
      if (this.levels != null && !search.equals(this.search)) {
         this.showSummaries(search, this.levels);
      }

      this.search = search;
   }

   private CompletableFuture loadLevels() {
      LevelStorage.LevelList lv;
      try {
         lv = this.client.getLevelStorage().getLevelList();
      } catch (LevelStorageException var3) {
         LOGGER.error("Couldn't load level list", var3);
         this.showUnableToLoadScreen(var3.getMessageText());
         return CompletableFuture.completedFuture(List.of());
      }

      if (lv.isEmpty()) {
         CreateWorldScreen.create(this.client, (Screen)null);
         return CompletableFuture.completedFuture(List.of());
      } else {
         return this.client.getLevelStorage().loadSummaries(lv).exceptionally((throwable) -> {
            this.client.setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Couldn't load level list"));
            return List.of();
         });
      }
   }

   private void showSummaries(String search, List summaries) {
      this.clearEntries();
      search = search.toLowerCase(Locale.ROOT);
      Iterator var3 = summaries.iterator();

      while(var3.hasNext()) {
         LevelSummary lv = (LevelSummary)var3.next();
         if (this.shouldShow(search, lv)) {
            this.addEntry(new WorldEntry(this, lv));
         }
      }

      this.narrateScreenIfNarrationEnabled();
   }

   private boolean shouldShow(String search, LevelSummary summary) {
      return summary.getDisplayName().toLowerCase(Locale.ROOT).contains(search) || summary.getName().toLowerCase(Locale.ROOT).contains(search);
   }

   private void showLoadingScreen() {
      this.clearEntries();
      this.addEntry(this.loadingEntry);
      this.narrateScreenIfNarrationEnabled();
   }

   private void narrateScreenIfNarrationEnabled() {
      this.parent.narrateScreenIfNarrationEnabled(true);
   }

   private void showUnableToLoadScreen(Text message) {
      this.client.setScreen(new FatalErrorScreen(Text.translatable("selectWorld.unable_to_load"), message));
   }

   protected int getScrollbarPositionX() {
      return super.getScrollbarPositionX() + 20;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 50;
   }

   public void setSelected(@Nullable Entry arg) {
      super.setSelected(arg);
      this.parent.worldSelected(arg != null && arg.isAvailable(), arg != null);
   }

   public Optional getSelectedAsOptional() {
      Entry lv = (Entry)this.getSelectedOrNull();
      if (lv instanceof WorldEntry lv2) {
         return Optional.of(lv2);
      } else {
         return Optional.empty();
      }
   }

   public SelectWorldScreen getParent() {
      return this.parent;
   }

   public void appendNarrations(NarrationMessageBuilder builder) {
      if (this.children().contains(this.loadingEntry)) {
         this.loadingEntry.appendNarrations(builder);
      } else {
         super.appendNarrations(builder);
      }
   }

   static {
      FROM_NEWER_VERSION_FIRST_LINE = Text.translatable("selectWorld.tooltip.fromNewerVersion1").formatted(Formatting.RED);
      FROM_NEWER_VERSION_SECOND_LINE = Text.translatable("selectWorld.tooltip.fromNewerVersion2").formatted(Formatting.RED);
      SNAPSHOT_FIRST_LINE = Text.translatable("selectWorld.tooltip.snapshot1").formatted(Formatting.GOLD);
      SNAPSHOT_SECOND_LINE = Text.translatable("selectWorld.tooltip.snapshot2").formatted(Formatting.GOLD);
      LOCKED_TEXT = Text.translatable("selectWorld.locked").formatted(Formatting.RED);
      CONVERSION_TOOLTIP = Text.translatable("selectWorld.conversion.tooltip").formatted(Formatting.RED);
   }

   @Environment(EnvType.CLIENT)
   public static class LoadingEntry extends Entry {
      private static final Text LOADING_LIST_TEXT = Text.translatable("selectWorld.loading_list");
      private final MinecraftClient client;

      public LoadingEntry(MinecraftClient client) {
         this.client = client;
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         int p = (this.client.currentScreen.width - this.client.textRenderer.getWidth((StringVisitable)LOADING_LIST_TEXT)) / 2;
         Objects.requireNonNull(this.client.textRenderer);
         int q = y + (entryHeight - 9) / 2;
         this.client.textRenderer.draw(matrices, LOADING_LIST_TEXT, (float)p, (float)q, 16777215);
         String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
         int r = (this.client.currentScreen.width - this.client.textRenderer.getWidth(string)) / 2;
         Objects.requireNonNull(this.client.textRenderer);
         int s = q + 9;
         this.client.textRenderer.draw(matrices, string, (float)r, (float)s, 8421504);
      }

      public Text getNarration() {
         return LOADING_LIST_TEXT;
      }

      public boolean isAvailable() {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   public final class WorldEntry extends Entry implements AutoCloseable {
      private static final int field_32435 = 32;
      private static final int field_32436 = 32;
      private static final int field_32437 = 0;
      private static final int field_32438 = 32;
      private static final int field_32439 = 64;
      private static final int field_32440 = 96;
      private static final int field_32441 = 0;
      private static final int field_32442 = 32;
      private final MinecraftClient client;
      private final SelectWorldScreen screen;
      private final LevelSummary level;
      private final Identifier iconLocation;
      @Nullable
      private Path iconPath;
      @Nullable
      private final NativeImageBackedTexture icon;
      private long time;

      public WorldEntry(WorldListWidget levelList, LevelSummary level) {
         this.client = levelList.client;
         this.screen = levelList.getParent();
         this.level = level;
         String string = level.getName();
         String var10004 = Util.replaceInvalidChars(string, Identifier::isPathCharacterValid);
         this.iconLocation = new Identifier("minecraft", "worlds/" + var10004 + "/" + Hashing.sha1().hashUnencodedChars(string) + "/icon");
         this.iconPath = level.getIconPath();
         if (!Files.isRegularFile(this.iconPath, new LinkOption[0])) {
            this.iconPath = null;
         }

         this.icon = this.getIconTexture();
      }

      public Text getNarration() {
         Text lv = Text.translatable("narrator.select.world", this.level.getDisplayName(), new Date(this.level.getLastPlayed()), this.level.isHardcore() ? Text.translatable("gameMode.hardcore") : Text.translatable("gameMode." + this.level.getGameMode().getName()), this.level.hasCheats() ? Text.translatable("selectWorld.cheats") : ScreenTexts.EMPTY, this.level.getVersion());
         MutableText lv2;
         if (this.level.isLocked()) {
            lv2 = ScreenTexts.joinSentences(lv, WorldListWidget.LOCKED_TEXT);
         } else {
            lv2 = lv;
         }

         return Text.translatable("narrator.select", lv2);
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         String string = this.level.getDisplayName();
         String var10000 = this.level.getName();
         String string2 = var10000 + " (" + WorldListWidget.DATE_FORMAT.format(new Date(this.level.getLastPlayed())) + ")";
         if (StringUtils.isEmpty(string)) {
            var10000 = I18n.translate("selectWorld.world");
            string = var10000 + " " + (index + 1);
         }

         Text lv = this.level.getDetails();
         this.client.textRenderer.draw(matrices, string, (float)(x + 32 + 3), (float)(y + 1), 16777215);
         TextRenderer var17 = this.client.textRenderer;
         float var10003 = (float)(x + 32 + 3);
         Objects.requireNonNull(this.client.textRenderer);
         var17.draw(matrices, string2, var10003, (float)(y + 9 + 3), 8421504);
         var17 = this.client.textRenderer;
         var10003 = (float)(x + 32 + 3);
         Objects.requireNonNull(this.client.textRenderer);
         int var10004 = y + 9;
         Objects.requireNonNull(this.client.textRenderer);
         var17.draw(matrices, lv, var10003, (float)(var10004 + 9 + 3), 8421504);
         RenderSystem.setShaderTexture(0, this.icon != null ? this.iconLocation : WorldListWidget.UNKNOWN_SERVER_LOCATION);
         RenderSystem.enableBlend();
         DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
         RenderSystem.disableBlend();
         if ((Boolean)this.client.options.getTouchscreen().getValue() || hovered) {
            RenderSystem.setShaderTexture(0, WorldListWidget.WORLD_SELECTION_LOCATION);
            DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
            int p = mouseX - x;
            boolean bl2 = p < 32;
            int q = bl2 ? 32 : 0;
            if (this.level.isLocked()) {
               DrawableHelper.drawTexture(matrices, x, y, 96.0F, (float)q, 32, 32, 256, 256);
               if (bl2) {
                  this.screen.setTooltip(this.client.textRenderer.wrapLines(WorldListWidget.LOCKED_TEXT, 175));
               }
            } else if (this.level.requiresConversion()) {
               DrawableHelper.drawTexture(matrices, x, y, 96.0F, (float)q, 32, 32, 256, 256);
               if (bl2) {
                  this.screen.setTooltip(this.client.textRenderer.wrapLines(WorldListWidget.CONVERSION_TOOLTIP, 175));
               }
            } else if (this.level.isDifferentVersion()) {
               DrawableHelper.drawTexture(matrices, x, y, 32.0F, (float)q, 32, 32, 256, 256);
               if (this.level.isFutureLevel()) {
                  DrawableHelper.drawTexture(matrices, x, y, 96.0F, (float)q, 32, 32, 256, 256);
                  if (bl2) {
                     this.screen.setTooltip(ImmutableList.of(WorldListWidget.FROM_NEWER_VERSION_FIRST_LINE.asOrderedText(), WorldListWidget.FROM_NEWER_VERSION_SECOND_LINE.asOrderedText()));
                  }
               } else if (!SharedConstants.getGameVersion().isStable()) {
                  DrawableHelper.drawTexture(matrices, x, y, 64.0F, (float)q, 32, 32, 256, 256);
                  if (bl2) {
                     this.screen.setTooltip(ImmutableList.of(WorldListWidget.SNAPSHOT_FIRST_LINE.asOrderedText(), WorldListWidget.SNAPSHOT_SECOND_LINE.asOrderedText()));
                  }
               }
            } else {
               DrawableHelper.drawTexture(matrices, x, y, 0.0F, (float)q, 32, 32, 256, 256);
            }
         }

      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (this.level.isUnavailable()) {
            return true;
         } else {
            WorldListWidget.this.setSelected((Entry)this);
            if (mouseX - (double)WorldListWidget.this.getRowLeft() <= 32.0) {
               this.play();
               return true;
            } else if (Util.getMeasuringTimeMs() - this.time < 250L) {
               this.play();
               return true;
            } else {
               this.time = Util.getMeasuringTimeMs();
               return true;
            }
         }
      }

      public void play() {
         if (!this.level.isUnavailable()) {
            LevelSummary.ConversionWarning lv = this.level.getConversionWarning();
            if (lv.promptsBackup()) {
               String string = "selectWorld.backupQuestion." + lv.getTranslationKeySuffix();
               String string2 = "selectWorld.backupWarning." + lv.getTranslationKeySuffix();
               MutableText lv2 = Text.translatable(string);
               if (lv.needsBoldRedFormatting()) {
                  lv2.formatted(Formatting.BOLD, Formatting.RED);
               }

               Text lv3 = Text.translatable(string2, this.level.getVersion(), SharedConstants.getGameVersion().getName());
               this.client.setScreen(new BackupPromptScreen(this.screen, (backup, eraseCache) -> {
                  if (backup) {
                     String string = this.level.getName();

                     try {
                        LevelStorage.Session lv = this.client.getLevelStorage().createSession(string);

                        try {
                           EditWorldScreen.backupLevel(lv);
                        } catch (Throwable var8) {
                           if (lv != null) {
                              try {
                                 lv.close();
                              } catch (Throwable var7) {
                                 var8.addSuppressed(var7);
                              }
                           }

                           throw var8;
                        }

                        if (lv != null) {
                           lv.close();
                        }
                     } catch (IOException var9) {
                        SystemToast.addWorldAccessFailureToast(this.client, string);
                        WorldListWidget.LOGGER.error("Failed to backup level {}", string, var9);
                     }
                  }

                  this.start();
               }, lv2, lv3, false));
            } else if (this.level.isFutureLevel()) {
               this.client.setScreen(new ConfirmScreen((confirmed) -> {
                  if (confirmed) {
                     try {
                        this.start();
                     } catch (Exception var3) {
                        WorldListWidget.LOGGER.error("Failure to open 'future world'", var3);
                        this.client.setScreen(new NoticeScreen(() -> {
                           this.client.setScreen(this.screen);
                        }, Text.translatable("selectWorld.futureworld.error.title"), Text.translatable("selectWorld.futureworld.error.text")));
                     }
                  } else {
                     this.client.setScreen(this.screen);
                  }

               }, Text.translatable("selectWorld.versionQuestion"), Text.translatable("selectWorld.versionWarning", this.level.getVersion()), Text.translatable("selectWorld.versionJoinButton"), ScreenTexts.CANCEL));
            } else {
               this.start();
            }

         }
      }

      public void deleteIfConfirmed() {
         this.client.setScreen(new ConfirmScreen((confirmed) -> {
            if (confirmed) {
               this.client.setScreen(new ProgressScreen(true));
               this.delete();
            }

            this.client.setScreen(this.screen);
         }, Text.translatable("selectWorld.deleteQuestion"), Text.translatable("selectWorld.deleteWarning", this.level.getDisplayName()), Text.translatable("selectWorld.deleteButton"), ScreenTexts.CANCEL));
      }

      public void delete() {
         LevelStorage lv = this.client.getLevelStorage();
         String string = this.level.getName();

         try {
            LevelStorage.Session lv2 = lv.createSession(string);

            try {
               lv2.deleteSessionLock();
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
            SystemToast.addWorldDeleteFailureToast(this.client, string);
            WorldListWidget.LOGGER.error("Failed to delete world {}", string, var8);
         }

         WorldListWidget.this.load();
      }

      public void edit() {
         this.openReadingWorldScreen();
         String string = this.level.getName();

         try {
            LevelStorage.Session lv = this.client.getLevelStorage().createSession(string);
            this.client.setScreen(new EditWorldScreen((edited) -> {
               try {
                  lv.close();
               } catch (IOException var5) {
                  WorldListWidget.LOGGER.error("Failed to unlock level {}", string, var5);
               }

               if (edited) {
                  WorldListWidget.this.load();
               }

               this.client.setScreen(this.screen);
            }, lv));
         } catch (IOException var3) {
            SystemToast.addWorldAccessFailureToast(this.client, string);
            WorldListWidget.LOGGER.error("Failed to access level {}", string, var3);
            WorldListWidget.this.load();
         }

      }

      public void recreate() {
         this.openReadingWorldScreen();

         try {
            LevelStorage.Session lv = this.client.getLevelStorage().createSession(this.level.getName());

            try {
               Pair pair = this.client.createIntegratedServerLoader().loadForRecreation(lv);
               LevelInfo lv2 = (LevelInfo)pair.getFirst();
               GeneratorOptionsHolder lv3 = (GeneratorOptionsHolder)pair.getSecond();
               Path path = CreateWorldScreen.copyDataPack(lv.getDirectory(WorldSavePath.DATAPACKS), this.client);
               if (lv3.generatorOptions().isLegacyCustomizedType()) {
                  this.client.setScreen(new ConfirmScreen((confirmed) -> {
                     this.client.setScreen((Screen)(confirmed ? CreateWorldScreen.create(this.client, this.screen, lv2, lv3, path) : this.screen));
                  }, Text.translatable("selectWorld.recreate.customized.title"), Text.translatable("selectWorld.recreate.customized.text"), ScreenTexts.PROCEED, ScreenTexts.CANCEL));
               } else {
                  this.client.setScreen(CreateWorldScreen.create(this.client, this.screen, lv2, lv3, path));
               }
            } catch (Throwable var7) {
               if (lv != null) {
                  try {
                     lv.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (lv != null) {
               lv.close();
            }
         } catch (Exception var8) {
            WorldListWidget.LOGGER.error("Unable to recreate world", var8);
            this.client.setScreen(new NoticeScreen(() -> {
               this.client.setScreen(this.screen);
            }, Text.translatable("selectWorld.recreate.error.title"), Text.translatable("selectWorld.recreate.error.text")));
         }

      }

      private void start() {
         this.client.getSoundManager().play(PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, 1.0F));
         if (this.client.getLevelStorage().levelExists(this.level.getName())) {
            this.openReadingWorldScreen();
            this.client.createIntegratedServerLoader().start(this.screen, this.level.getName());
         }

      }

      private void openReadingWorldScreen() {
         this.client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.data_read")));
      }

      @Nullable
      private NativeImageBackedTexture getIconTexture() {
         boolean bl = this.iconPath != null && Files.isRegularFile(this.iconPath, new LinkOption[0]);
         if (bl) {
            try {
               InputStream inputStream = Files.newInputStream(this.iconPath);

               NativeImageBackedTexture var5;
               try {
                  NativeImage lv = NativeImage.read(inputStream);
                  Preconditions.checkState(lv.getWidth() == 64, "Must be 64 pixels wide");
                  Preconditions.checkState(lv.getHeight() == 64, "Must be 64 pixels high");
                  NativeImageBackedTexture lv2 = new NativeImageBackedTexture(lv);
                  this.client.getTextureManager().registerTexture(this.iconLocation, lv2);
                  var5 = lv2;
               } catch (Throwable var7) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (inputStream != null) {
                  inputStream.close();
               }

               return var5;
            } catch (Throwable var8) {
               WorldListWidget.LOGGER.error("Invalid icon for world {}", this.level.getName(), var8);
               this.iconPath = null;
               return null;
            }
         } else {
            this.client.getTextureManager().destroyTexture(this.iconLocation);
            return null;
         }
      }

      public void close() {
         if (this.icon != null) {
            this.icon.close();
         }

      }

      public String getLevelDisplayName() {
         return this.level.getDisplayName();
      }

      public boolean isAvailable() {
         return !this.level.isUnavailable();
      }
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry implements AutoCloseable {
      public abstract boolean isAvailable();

      public void close() {
      }
   }
}
