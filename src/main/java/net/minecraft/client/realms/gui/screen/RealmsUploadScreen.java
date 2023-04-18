package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.FileUpload;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.SizeUnit;
import net.minecraft.client.realms.UploadStatus;
import net.minecraft.client.realms.dto.UploadInfo;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.exception.RetryCallException;
import net.minecraft.client.realms.util.UploadTokenCache;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsUploadScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ReentrantLock UPLOAD_LOCK = new ReentrantLock();
   private static final int field_41776 = 200;
   private static final int field_41773 = 80;
   private static final int field_41774 = 95;
   private static final int field_41775 = 1;
   private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
   private static final Text VERIFYING_TEXT = Text.translatable("mco.upload.verifying");
   private final RealmsResetWorldScreen parent;
   private final LevelSummary selectedLevel;
   private final long worldId;
   private final int slotId;
   private final UploadStatus uploadStatus;
   private final RateLimiter narrationRateLimiter;
   @Nullable
   private volatile Text[] statusTexts;
   private volatile Text status = Text.translatable("mco.upload.preparing");
   private volatile String progress;
   private volatile boolean cancelled;
   private volatile boolean uploadFinished;
   private volatile boolean showDots = true;
   private volatile boolean uploadStarted;
   private ButtonWidget backButton;
   private ButtonWidget cancelButton;
   private int animTick;
   @Nullable
   private Long previousWrittenBytes;
   @Nullable
   private Long previousTimeSnapshot;
   private long bytesPerSecond;
   private final Runnable onBack;

   public RealmsUploadScreen(long worldId, int slotId, RealmsResetWorldScreen parent, LevelSummary selectedLevel, Runnable onBack) {
      super(NarratorManager.EMPTY);
      this.worldId = worldId;
      this.slotId = slotId;
      this.parent = parent;
      this.selectedLevel = selectedLevel;
      this.uploadStatus = new UploadStatus();
      this.narrationRateLimiter = RateLimiter.create(0.10000000149011612);
      this.onBack = onBack;
   }

   public void init() {
      this.backButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.onBack();
      }).dimensions((this.width - 200) / 2, this.height - 42, 200, 20).build());
      this.backButton.visible = false;
      this.cancelButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.onCancel();
      }).dimensions((this.width - 200) / 2, this.height - 42, 200, 20).build());
      if (!this.uploadStarted) {
         if (this.parent.slot == -1) {
            this.upload();
         } else {
            this.parent.switchSlot(() -> {
               if (!this.uploadStarted) {
                  this.uploadStarted = true;
                  this.client.setScreen(this);
                  this.upload();
               }

            });
         }
      }

   }

   private void onBack() {
      this.onBack.run();
   }

   private void onCancel() {
      this.cancelled = true;
      this.client.setScreen(this.parent);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         if (this.showDots) {
            this.onCancel();
         } else {
            this.onBack();
         }

         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      if (!this.uploadFinished && this.uploadStatus.bytesWritten != 0L && this.uploadStatus.bytesWritten == this.uploadStatus.totalBytes) {
         this.status = VERIFYING_TEXT;
         this.cancelButton.active = false;
      }

      drawCenteredTextWithShadow(matrices, this.textRenderer, this.status, this.width / 2, 50, 16777215);
      if (this.showDots) {
         this.drawDots(matrices);
      }

      if (this.uploadStatus.bytesWritten != 0L && !this.cancelled) {
         this.drawProgressBar(matrices);
         this.drawUploadSpeed(matrices);
      }

      if (this.statusTexts != null) {
         for(int k = 0; k < this.statusTexts.length; ++k) {
            drawCenteredTextWithShadow(matrices, this.textRenderer, this.statusTexts[k], this.width / 2, 110 + 12 * k, 16711680);
         }
      }

      super.render(matrices, mouseX, mouseY, delta);
   }

   private void drawDots(MatrixStack matrices) {
      int i = this.textRenderer.getWidth((StringVisitable)this.status);
      this.textRenderer.draw(matrices, DOTS[this.animTick / 10 % DOTS.length], (float)(this.width / 2 + i / 2 + 5), 50.0F, 16777215);
   }

   private void drawProgressBar(MatrixStack matrices) {
      double d = Math.min((double)this.uploadStatus.bytesWritten / (double)this.uploadStatus.totalBytes, 1.0);
      this.progress = String.format(Locale.ROOT, "%.1f", d * 100.0);
      int i = (this.width - 200) / 2;
      int j = i + (int)Math.round(200.0 * d);
      fill(matrices, i - 1, 79, j + 1, 96, -2501934);
      fill(matrices, i, 80, j, 95, -8355712);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.progress + " %", this.width / 2, 84, 16777215);
   }

   private void drawUploadSpeed(MatrixStack matrices) {
      if (this.animTick % 20 == 0) {
         if (this.previousWrittenBytes != null) {
            long l = Util.getMeasuringTimeMs() - this.previousTimeSnapshot;
            if (l == 0L) {
               l = 1L;
            }

            this.bytesPerSecond = 1000L * (this.uploadStatus.bytesWritten - this.previousWrittenBytes) / l;
            this.drawUploadSpeed0(matrices, this.bytesPerSecond);
         }

         this.previousWrittenBytes = this.uploadStatus.bytesWritten;
         this.previousTimeSnapshot = Util.getMeasuringTimeMs();
      } else {
         this.drawUploadSpeed0(matrices, this.bytesPerSecond);
      }

   }

   private void drawUploadSpeed0(MatrixStack matrices, long bytesPerSecond) {
      if (bytesPerSecond > 0L) {
         int i = this.textRenderer.getWidth(this.progress);
         String string = "(" + SizeUnit.getUserFriendlyString(bytesPerSecond) + "/s)";
         this.textRenderer.draw(matrices, string, (float)(this.width / 2 + i / 2 + 15), 84.0F, 16777215);
      }

   }

   public void tick() {
      super.tick();
      ++this.animTick;
      if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
         Text lv = this.getNarration();
         this.client.getNarratorManager().narrate(lv);
      }

   }

   private Text getNarration() {
      List list = Lists.newArrayList();
      list.add(this.status);
      if (this.progress != null) {
         list.add(Text.literal(this.progress + "%"));
      }

      if (this.statusTexts != null) {
         list.addAll(Arrays.asList(this.statusTexts));
      }

      return ScreenTexts.joinLines((Collection)list);
   }

   private void upload() {
      this.uploadStarted = true;
      (new Thread(() -> {
         File file = null;
         RealmsClient lv = RealmsClient.create();
         long l = this.worldId;

         try {
            try {
               if (!UPLOAD_LOCK.tryLock(1L, TimeUnit.SECONDS)) {
                  this.status = Text.translatable("mco.upload.close.failure");
                  return;
               }

               UploadInfo lv2 = null;

               for(int i = 0; i < 20; ++i) {
                  try {
                     if (this.cancelled) {
                        this.uploadCancelled();
                        return;
                     }

                     lv2 = lv.upload(l, UploadTokenCache.get(l));
                     if (lv2 != null) {
                        break;
                     }
                  } catch (RetryCallException var20) {
                     Thread.sleep((long)(var20.delaySeconds * 1000));
                  }
               }

               if (lv2 == null) {
                  this.status = Text.translatable("mco.upload.close.failure");
                  return;
               }

               UploadTokenCache.put(l, lv2.getToken());
               if (!lv2.isWorldClosed()) {
                  this.status = Text.translatable("mco.upload.close.failure");
                  return;
               }

               if (this.cancelled) {
                  this.uploadCancelled();
                  return;
               }

               File file2 = new File(this.client.runDirectory.getAbsolutePath(), "saves");
               file = this.tarGzipArchive(new File(file2, this.selectedLevel.getName()));
               if (this.cancelled) {
                  this.uploadCancelled();
                  return;
               }

               if (!this.verify(file)) {
                  long m = file.length();
                  SizeUnit lv4 = SizeUnit.getLargestUnit(m);
                  SizeUnit lv5 = SizeUnit.getLargestUnit(5368709120L);
                  if (SizeUnit.humanReadableSize(m, lv4).equals(SizeUnit.humanReadableSize(5368709120L, lv5)) && lv4 != SizeUnit.B) {
                     SizeUnit lv6 = SizeUnit.values()[lv4.ordinal() - 1];
                     this.setStatusTexts(Text.translatable("mco.upload.size.failure.line1", this.selectedLevel.getDisplayName()), Text.translatable("mco.upload.size.failure.line2", SizeUnit.humanReadableSize(m, lv6), SizeUnit.humanReadableSize(5368709120L, lv6)));
                     return;
                  }

                  this.setStatusTexts(Text.translatable("mco.upload.size.failure.line1", this.selectedLevel.getDisplayName()), Text.translatable("mco.upload.size.failure.line2", SizeUnit.humanReadableSize(m, lv4), SizeUnit.humanReadableSize(5368709120L, lv5)));
                  return;
               }

               this.status = Text.translatable("mco.upload.uploading", this.selectedLevel.getDisplayName());
               FileUpload lv7 = new FileUpload(file, this.worldId, this.slotId, lv2, this.client.getSession(), SharedConstants.getGameVersion().getName(), this.uploadStatus);
               lv7.upload((result) -> {
                  if (result.statusCode >= 200 && result.statusCode < 300) {
                     this.uploadFinished = true;
                     this.status = Text.translatable("mco.upload.done");
                     this.backButton.setMessage(ScreenTexts.DONE);
                     UploadTokenCache.invalidate(l);
                  } else if (result.statusCode == 400 && result.errorMessage != null) {
                     this.setStatusTexts(Text.translatable("mco.upload.failed", result.errorMessage));
                  } else {
                     this.setStatusTexts(Text.translatable("mco.upload.failed", result.statusCode));
                  }

               });

               while(!lv7.isFinished()) {
                  if (this.cancelled) {
                     lv7.cancel();
                     this.uploadCancelled();
                     return;
                  }

                  try {
                     Thread.sleep(500L);
                  } catch (InterruptedException var19) {
                     LOGGER.error("Failed to check Realms file upload status");
                  }
               }

               return;
            } catch (IOException var21) {
               this.setStatusTexts(Text.translatable("mco.upload.failed", var21.getMessage()));
            } catch (RealmsServiceException var22) {
               this.setStatusTexts(Text.translatable("mco.upload.failed", var22.toString()));
            } catch (InterruptedException var23) {
               LOGGER.error("Could not acquire upload lock");
            }

         } finally {
            this.uploadFinished = true;
            if (UPLOAD_LOCK.isHeldByCurrentThread()) {
               UPLOAD_LOCK.unlock();
               this.showDots = false;
               this.backButton.visible = true;
               this.cancelButton.visible = false;
               if (file != null) {
                  LOGGER.debug("Deleting file {}", file.getAbsolutePath());
                  file.delete();
               }

            } else {
               return;
            }
         }
      })).start();
   }

   private void setStatusTexts(Text... statusTexts) {
      this.statusTexts = statusTexts;
   }

   private void uploadCancelled() {
      this.status = Text.translatable("mco.upload.cancelled");
      LOGGER.debug("Upload was cancelled");
   }

   private boolean verify(File archive) {
      return archive.length() < 5368709120L;
   }

   private File tarGzipArchive(File pathToDirectoryFile) throws IOException {
      TarArchiveOutputStream tarArchiveOutputStream = null;

      File var4;
      try {
         File file2 = File.createTempFile("realms-upload-file", ".tar.gz");
         tarArchiveOutputStream = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(file2)));
         tarArchiveOutputStream.setLongFileMode(3);
         this.addFileToTarGz(tarArchiveOutputStream, pathToDirectoryFile.getAbsolutePath(), "world", true);
         tarArchiveOutputStream.finish();
         var4 = file2;
      } finally {
         if (tarArchiveOutputStream != null) {
            tarArchiveOutputStream.close();
         }

      }

      return var4;
   }

   private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base, boolean root) throws IOException {
      if (!this.cancelled) {
         File file = new File(path);
         String string3 = root ? base : base + file.getName();
         TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, string3);
         tOut.putArchiveEntry(tarArchiveEntry);
         if (file.isFile()) {
            IOUtils.copy(new FileInputStream(file), tOut);
            tOut.closeArchiveEntry();
         } else {
            tOut.closeArchiveEntry();
            File[] files = file.listFiles();
            if (files != null) {
               File[] var9 = files;
               int var10 = files.length;

               for(int var11 = 0; var11 < var10; ++var11) {
                  File file2 = var9[var11];
                  this.addFileToTarGz(tOut, file2.getAbsolutePath(), string3 + "/", false);
               }
            }
         }

      }
   }
}
