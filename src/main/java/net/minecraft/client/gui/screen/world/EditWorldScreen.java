package net.minecraft.client.gui.screen.world;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.PathUtil;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class EditWorldScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Text ENTER_NAME_TEXT = Text.translatable("selectWorld.enterName");
   private ButtonWidget saveButton;
   private final BooleanConsumer callback;
   private TextFieldWidget levelNameTextField;
   private final LevelStorage.Session storageSession;

   public EditWorldScreen(BooleanConsumer callback, LevelStorage.Session storageSession) {
      super(Text.translatable("selectWorld.edit.title"));
      this.callback = callback;
      this.storageSession = storageSession;
   }

   public void tick() {
      this.levelNameTextField.tick();
   }

   protected void init() {
      this.saveButton = ButtonWidget.builder(Text.translatable("selectWorld.edit.save"), (button) -> {
         this.commit();
      }).dimensions(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20).build();
      this.levelNameTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 38, 200, 20, Text.translatable("selectWorld.enterName"));
      LevelSummary lv = this.storageSession.getLevelSummary();
      String string = lv == null ? "" : lv.getDisplayName();
      this.levelNameTextField.setText(string);
      this.levelNameTextField.setChangedListener((levelName) -> {
         this.saveButton.active = !levelName.trim().isEmpty();
      });
      this.addSelectableChild(this.levelNameTextField);
      ButtonWidget lv2 = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.edit.resetIcon"), (button) -> {
         this.storageSession.getIconFile().ifPresent((path) -> {
            FileUtils.deleteQuietly(path.toFile());
         });
         button.active = false;
      }).dimensions(this.width / 2 - 100, this.height / 4 + 0 + 5, 200, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.edit.openFolder"), (button) -> {
         Util.getOperatingSystem().open(this.storageSession.getDirectory(WorldSavePath.ROOT).toFile());
      }).dimensions(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.edit.backup"), (button) -> {
         boolean bl = backupLevel(this.storageSession);
         this.callback.accept(!bl);
      }).dimensions(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.edit.backupFolder"), (button) -> {
         LevelStorage lv = this.client.getLevelStorage();
         Path path = lv.getBackupsDirectory();

         try {
            PathUtil.createDirectories(path);
         } catch (IOException var5) {
            throw new RuntimeException(var5);
         }

         Util.getOperatingSystem().open(path.toFile());
      }).dimensions(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.edit.optimize"), (button) -> {
         this.client.setScreen(new BackupPromptScreen(this, (backup, eraseCache) -> {
            if (backup) {
               backupLevel(this.storageSession);
            }

            this.client.setScreen(OptimizeWorldScreen.create(this.client, this.callback, this.client.getDataFixer(), this.storageSession, eraseCache));
         }, Text.translatable("optimizeWorld.confirm.title"), Text.translatable("optimizeWorld.confirm.description"), true));
      }).dimensions(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20).build());
      this.addDrawableChild(this.saveButton);
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.callback.accept(false);
      }).dimensions(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20).build());
      lv2.active = this.storageSession.getIconFile().filter((path) -> {
         return Files.isRegularFile(path, new LinkOption[0]);
      }).isPresent();
      this.setInitialFocus(this.levelNameTextField);
   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.levelNameTextField.getText();
      this.init(client, width, height);
      this.levelNameTextField.setText(string);
   }

   public void close() {
      this.callback.accept(false);
   }

   private void commit() {
      try {
         this.storageSession.save(this.levelNameTextField.getText().trim());
         this.callback.accept(true);
      } catch (IOException var2) {
         LOGGER.error("Failed to access world '{}'", this.storageSession.getDirectoryName(), var2);
         SystemToast.addWorldAccessFailureToast(this.client, this.storageSession.getDirectoryName());
         this.callback.accept(true);
      }

   }

   public static void onBackupConfirm(LevelStorage storage, String levelName) {
      boolean bl = false;

      try {
         LevelStorage.Session lv = storage.createSession(levelName);

         try {
            bl = true;
            backupLevel(lv);
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
      } catch (IOException var8) {
         if (!bl) {
            SystemToast.addWorldAccessFailureToast(MinecraftClient.getInstance(), levelName);
         }

         LOGGER.warn("Failed to create backup of level {}", levelName, var8);
      }

   }

   public static boolean backupLevel(LevelStorage.Session storageSession) {
      long l = 0L;
      IOException iOException = null;

      try {
         l = storageSession.createBackup();
      } catch (IOException var6) {
         iOException = var6;
      }

      MutableText lv;
      MutableText lv2;
      if (iOException != null) {
         lv = Text.translatable("selectWorld.edit.backupFailed");
         lv2 = Text.literal(iOException.getMessage());
         MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, lv, lv2));
         return false;
      } else {
         lv = Text.translatable("selectWorld.edit.backupCreated", storageSession.getDirectoryName());
         lv2 = Text.translatable("selectWorld.edit.backupSize", MathHelper.ceil((double)l / 1048576.0));
         MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, lv, lv2));
         return true;
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
      drawTextWithShadow(matrices, this.textRenderer, ENTER_NAME_TEXT, this.width / 2 - 100, 24, 10526880);
      this.levelNameTextField.render(matrices, mouseX, mouseY, delta);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
