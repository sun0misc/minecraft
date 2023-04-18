package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.realms.dto.Backup;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsWorldOptions;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.task.DownloadTask;
import net.minecraft.client.realms.task.RestoreTask;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsBackupScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   static final Identifier PLUS_ICON = new Identifier("realms", "textures/gui/realms/plus_icon.png");
   static final Identifier RESTORE_ICON = new Identifier("realms", "textures/gui/realms/restore_icon.png");
   static final Text RESTORE_TEXT = Text.translatable("mco.backup.button.restore");
   static final Text CHANGES_TOOLTIP = Text.translatable("mco.backup.changes.tooltip");
   private static final Text BACKUPS_TEXT = Text.translatable("mco.configure.world.backup");
   private static final Text NO_BACKUPS_TEXT = Text.translatable("mco.backup.nobackups");
   private final RealmsConfigureWorldScreen parent;
   List backups = Collections.emptyList();
   BackupObjectSelectionList backupObjectSelectionList;
   int selectedBackup = -1;
   private final int slotId;
   private ButtonWidget downloadButton;
   private ButtonWidget restoreButton;
   private ButtonWidget changesButton;
   Boolean noBackups = false;
   final RealmsServer serverData;
   private static final String UPLOADED = "Uploaded";

   public RealmsBackupScreen(RealmsConfigureWorldScreen parent, RealmsServer serverData, int slotId) {
      super(Text.translatable("mco.configure.world.backup"));
      this.parent = parent;
      this.serverData = serverData;
      this.slotId = slotId;
   }

   public void init() {
      this.backupObjectSelectionList = new BackupObjectSelectionList();
      (new Thread("Realms-fetch-backups") {
         public void run() {
            RealmsClient lv = RealmsClient.create();

            try {
               List list = lv.backupsFor(RealmsBackupScreen.this.serverData.id).backups;
               RealmsBackupScreen.this.client.execute(() -> {
                  RealmsBackupScreen.this.backups = list;
                  RealmsBackupScreen.this.noBackups = RealmsBackupScreen.this.backups.isEmpty();
                  RealmsBackupScreen.this.backupObjectSelectionList.clear();
                  Iterator var2 = RealmsBackupScreen.this.backups.iterator();

                  while(var2.hasNext()) {
                     Backup lv = (Backup)var2.next();
                     RealmsBackupScreen.this.backupObjectSelectionList.addEntry(lv);
                  }

               });
            } catch (RealmsServiceException var3) {
               RealmsBackupScreen.LOGGER.error("Couldn't request backups", var3);
            }

         }
      }).start();
      this.downloadButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.backup.button.download"), (button) -> {
         this.downloadClicked();
      }).dimensions(this.width - 135, row(1), 120, 20).build());
      this.restoreButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.backup.button.restore"), (button) -> {
         this.restoreClicked(this.selectedBackup);
      }).dimensions(this.width - 135, row(3), 120, 20).build());
      this.changesButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.backup.changes.tooltip"), (button) -> {
         this.client.setScreen(new RealmsBackupInfoScreen(this, (Backup)this.backups.get(this.selectedBackup)));
         this.selectedBackup = -1;
      }).dimensions(this.width - 135, row(5), 120, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width - 100, this.height - 35, 85, 20).build());
      this.addSelectableChild(this.backupObjectSelectionList);
      this.focusOn(this.backupObjectSelectionList);
      this.updateButtonStates();
   }

   void updateButtonStates() {
      this.restoreButton.visible = this.shouldRestoreButtonBeVisible();
      this.changesButton.visible = this.shouldChangesButtonBeVisible();
   }

   private boolean shouldChangesButtonBeVisible() {
      if (this.selectedBackup == -1) {
         return false;
      } else {
         return !((Backup)this.backups.get(this.selectedBackup)).changeList.isEmpty();
      }
   }

   private boolean shouldRestoreButtonBeVisible() {
      if (this.selectedBackup == -1) {
         return false;
      } else {
         return !this.serverData.expired;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   void restoreClicked(int selectedBackup) {
      if (selectedBackup >= 0 && selectedBackup < this.backups.size() && !this.serverData.expired) {
         this.selectedBackup = selectedBackup;
         Date date = ((Backup)this.backups.get(selectedBackup)).lastModifiedDate;
         String string = DateFormat.getDateTimeInstance(3, 3).format(date);
         String string2 = RealmsUtil.convertToAgePresentation(date);
         Text lv = Text.translatable("mco.configure.world.restore.question.line1", string, string2);
         Text lv2 = Text.translatable("mco.configure.world.restore.question.line2");
         this.client.setScreen(new RealmsLongConfirmationScreen((confirmed) -> {
            if (confirmed) {
               this.restore();
            } else {
               this.selectedBackup = -1;
               this.client.setScreen(this);
            }

         }, RealmsLongConfirmationScreen.Type.WARNING, lv, lv2, true));
      }

   }

   private void downloadClicked() {
      Text lv = Text.translatable("mco.configure.world.restore.download.question.line1");
      Text lv2 = Text.translatable("mco.configure.world.restore.download.question.line2");
      this.client.setScreen(new RealmsLongConfirmationScreen((confirmed) -> {
         if (confirmed) {
            this.downloadWorldData();
         } else {
            this.client.setScreen(this);
         }

      }, RealmsLongConfirmationScreen.Type.INFO, lv, lv2, true));
   }

   private void downloadWorldData() {
      MinecraftClient var10000 = this.client;
      RealmsConfigureWorldScreen var10003 = this.parent.getNewScreen();
      String var10008 = this.serverData.name;
      var10000.setScreen(new RealmsLongRunningMcoTaskScreen(var10003, new DownloadTask(this.serverData.id, this.slotId, var10008 + " (" + ((RealmsWorldOptions)this.serverData.slots.get(this.serverData.activeSlot)).getSlotName(this.serverData.activeSlot) + ")", this)));
   }

   private void restore() {
      Backup lv = (Backup)this.backups.get(this.selectedBackup);
      this.selectedBackup = -1;
      this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent.getNewScreen(), new RestoreTask(lv, this.serverData.id, this.parent)));
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.backupObjectSelectionList.render(matrices, mouseX, mouseY, delta);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 12, 16777215);
      this.textRenderer.draw(matrices, BACKUPS_TEXT, (float)((this.width - 150) / 2 - 90), 20.0F, 10526880);
      if (this.noBackups) {
         this.textRenderer.draw(matrices, NO_BACKUPS_TEXT, 20.0F, (float)(this.height / 2 - 10), 16777215);
      }

      this.downloadButton.active = !this.noBackups;
      super.render(matrices, mouseX, mouseY, delta);
   }

   @Environment(EnvType.CLIENT)
   private class BackupObjectSelectionList extends RealmsObjectSelectionList {
      public BackupObjectSelectionList() {
         super(RealmsBackupScreen.this.width - 150, RealmsBackupScreen.this.height, 32, RealmsBackupScreen.this.height - 15, 36);
      }

      public void addEntry(Backup backup) {
         this.addEntry(RealmsBackupScreen.this.new BackupObjectSelectionListEntry(backup));
      }

      public int getRowWidth() {
         return (int)((double)this.width * 0.93);
      }

      public int getMaxPosition() {
         return this.getEntryCount() * 36;
      }

      public void renderBackground(MatrixStack matrices) {
         RealmsBackupScreen.this.renderBackground(matrices);
      }

      public int getScrollbarPositionX() {
         return this.width - 5;
      }

      public void setSelected(int index) {
         super.setSelected(index);
         this.selectInviteListItem(index);
      }

      public void selectInviteListItem(int item) {
         RealmsBackupScreen.this.selectedBackup = item;
         RealmsBackupScreen.this.updateButtonStates();
      }

      public void setSelected(@Nullable BackupObjectSelectionListEntry arg) {
         super.setSelected(arg);
         RealmsBackupScreen.this.selectedBackup = this.children().indexOf(arg);
         RealmsBackupScreen.this.updateButtonStates();
      }
   }

   @Environment(EnvType.CLIENT)
   private class BackupObjectSelectionListEntry extends AlwaysSelectedEntryListWidget.Entry {
      private static final int field_44525 = 2;
      private static final int field_44526 = 7;
      private final Backup mBackup;
      private final List buttons = new ArrayList();
      @Nullable
      private TexturedButtonWidget restoreButton;
      @Nullable
      private TexturedButtonWidget infoButton;

      public BackupObjectSelectionListEntry(Backup backup) {
         this.mBackup = backup;
         this.updateChangeList(backup);
         if (!backup.changeList.isEmpty()) {
            this.addInfoButton();
         }

         if (!RealmsBackupScreen.this.serverData.expired) {
            this.addRestoreButton();
         }

      }

      private void updateChangeList(Backup backup) {
         int i = RealmsBackupScreen.this.backups.indexOf(backup);
         if (i != RealmsBackupScreen.this.backups.size() - 1) {
            Backup lv = (Backup)RealmsBackupScreen.this.backups.get(i + 1);
            Iterator var4 = backup.metadata.keySet().iterator();

            while(true) {
               while(var4.hasNext()) {
                  String string = (String)var4.next();
                  if (!string.contains("Uploaded") && lv.metadata.containsKey(string)) {
                     if (!((String)backup.metadata.get(string)).equals(lv.metadata.get(string))) {
                        this.addChange(string);
                     }
                  } else {
                     this.addChange(string);
                  }
               }

               return;
            }
         }
      }

      private void addChange(String metadataKey) {
         if (metadataKey.contains("Uploaded")) {
            String string2 = DateFormat.getDateTimeInstance(3, 3).format(this.mBackup.lastModifiedDate);
            this.mBackup.changeList.put(metadataKey, string2);
            this.mBackup.setUploadedVersion(true);
         } else {
            this.mBackup.changeList.put(metadataKey, (String)this.mBackup.metadata.get(metadataKey));
         }

      }

      private void addInfoButton() {
         int i = true;
         int j = true;
         int k = RealmsBackupScreen.this.backupObjectSelectionList.getRowRight() - 9 - 28;
         int l = RealmsBackupScreen.this.backupObjectSelectionList.getRowTop(RealmsBackupScreen.this.backups.indexOf(this.mBackup)) + 2;
         this.infoButton = new TexturedButtonWidget(k, l, 9, 9, 0, 0, 9, RealmsBackupScreen.PLUS_ICON, 9, 18, (button) -> {
            RealmsBackupScreen.this.client.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, this.mBackup));
         });
         this.infoButton.setTooltip(Tooltip.of(RealmsBackupScreen.CHANGES_TOOLTIP));
         this.buttons.add(this.infoButton);
      }

      private void addRestoreButton() {
         int i = true;
         int j = true;
         int k = RealmsBackupScreen.this.backupObjectSelectionList.getRowRight() - 17 - 7;
         int l = RealmsBackupScreen.this.backupObjectSelectionList.getRowTop(RealmsBackupScreen.this.backups.indexOf(this.mBackup)) + 2;
         this.restoreButton = new TexturedButtonWidget(k, l, 17, 10, 0, 0, 10, RealmsBackupScreen.RESTORE_ICON, 17, 20, (button) -> {
            RealmsBackupScreen.this.restoreClicked(RealmsBackupScreen.this.backups.indexOf(this.mBackup));
         });
         this.restoreButton.setTooltip(Tooltip.of(RealmsBackupScreen.RESTORE_TEXT));
         this.buttons.add(this.restoreButton);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (this.restoreButton != null) {
            this.restoreButton.mouseClicked(mouseX, mouseY, button);
         }

         if (this.infoButton != null) {
            this.infoButton.mouseClicked(mouseX, mouseY, button);
         }

         return true;
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         int p = this.mBackup.isUploadedVersion() ? -8388737 : 16777215;
         RealmsBackupScreen.this.textRenderer.draw(matrices, "Backup (" + RealmsUtil.convertToAgePresentation(this.mBackup.lastModifiedDate) + ")", (float)x, (float)(y + 1), p);
         RealmsBackupScreen.this.textRenderer.draw(matrices, this.getMediumDatePresentation(this.mBackup.lastModifiedDate), (float)x, (float)(y + 12), 5000268);
         this.buttons.forEach((button) -> {
            button.setY(y + 2);
            button.render(matrices, mouseX, mouseY, tickDelta);
         });
      }

      private String getMediumDatePresentation(Date lastModifiedDate) {
         return DateFormat.getDateTimeInstance(3, 3).format(lastModifiedDate);
      }

      public Text getNarration() {
         return Text.translatable("narrator.select", this.mBackup.lastModifiedDate.toString());
      }
   }
}
