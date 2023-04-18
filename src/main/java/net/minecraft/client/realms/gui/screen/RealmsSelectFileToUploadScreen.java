package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.RealmsLabel;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSelectFileToUploadScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final Text WORLD_LANG = Text.translatable("selectWorld.world");
   static final Text HARDCORE_TEXT = Text.translatable("mco.upload.hardcore").styled((style) -> {
      return style.withColor(-65536);
   });
   static final Text CHEATS_TEXT = Text.translatable("selectWorld.cheats");
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
   private final RealmsResetWorldScreen parent;
   private final long worldId;
   private final int slotId;
   ButtonWidget uploadButton;
   List levelList = Lists.newArrayList();
   int selectedWorld = -1;
   WorldSelectionList worldSelectionList;
   private final Runnable onBack;

   public RealmsSelectFileToUploadScreen(long worldId, int slotId, RealmsResetWorldScreen parent, Runnable onBack) {
      super(Text.translatable("mco.upload.select.world.title"));
      this.parent = parent;
      this.worldId = worldId;
      this.slotId = slotId;
      this.onBack = onBack;
   }

   private void loadLevelList() throws Exception {
      LevelStorage.LevelList lv = this.client.getLevelStorage().getLevelList();
      this.levelList = (List)((List)this.client.getLevelStorage().loadSummaries(lv).join()).stream().filter((a) -> {
         return !a.requiresConversion() && !a.isLocked();
      }).collect(Collectors.toList());
      Iterator var2 = this.levelList.iterator();

      while(var2.hasNext()) {
         LevelSummary lv2 = (LevelSummary)var2.next();
         this.worldSelectionList.addEntry(lv2);
      }

   }

   public void init() {
      this.worldSelectionList = new WorldSelectionList();

      try {
         this.loadLevelList();
      } catch (Exception var2) {
         LOGGER.error("Couldn't load level list", var2);
         this.client.setScreen(new RealmsGenericErrorScreen(Text.literal("Unable to load worlds"), Text.of(var2.getMessage()), this.parent));
         return;
      }

      this.addSelectableChild(this.worldSelectionList);
      this.uploadButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.upload.button.name"), (button) -> {
         this.upload();
      }).dimensions(this.width / 2 - 154, this.height - 32, 153, 20).build());
      this.uploadButton.active = this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size();
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 + 6, this.height - 32, 153, 20).build());
      this.addLabel(new RealmsLabel(Text.translatable("mco.upload.select.world.subtitle"), this.width / 2, row(-1), 10526880));
      if (this.levelList.isEmpty()) {
         this.addLabel(new RealmsLabel(Text.translatable("mco.upload.select.world.none"), this.width / 2, this.height / 2 - 20, 16777215));
      }

   }

   public Text getNarratedTitle() {
      return ScreenTexts.joinSentences(this.getTitle(), this.narrateLabels());
   }

   private void upload() {
      if (this.selectedWorld != -1 && !((LevelSummary)this.levelList.get(this.selectedWorld)).isHardcore()) {
         LevelSummary lv = (LevelSummary)this.levelList.get(this.selectedWorld);
         this.client.setScreen(new RealmsUploadScreen(this.worldId, this.slotId, this.parent, lv, this.onBack));
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.worldSelectionList.render(matrices, mouseX, mouseY, delta);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 13, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   static Text getGameModeName(LevelSummary summary) {
      return summary.getGameMode().getTranslatableName();
   }

   static String getLastPlayed(LevelSummary summary) {
      return DATE_FORMAT.format(new Date(summary.getLastPlayed()));
   }

   @Environment(EnvType.CLIENT)
   class WorldSelectionList extends RealmsObjectSelectionList {
      public WorldSelectionList() {
         super(RealmsSelectFileToUploadScreen.this.width, RealmsSelectFileToUploadScreen.this.height, RealmsSelectFileToUploadScreen.row(0), RealmsSelectFileToUploadScreen.this.height - 40, 36);
      }

      public void addEntry(LevelSummary summary) {
         this.addEntry(RealmsSelectFileToUploadScreen.this.new WorldListEntry(summary));
      }

      public int getMaxPosition() {
         return RealmsSelectFileToUploadScreen.this.levelList.size() * 36;
      }

      public void renderBackground(MatrixStack matrices) {
         RealmsSelectFileToUploadScreen.this.renderBackground(matrices);
      }

      public void setSelected(@Nullable WorldListEntry arg) {
         super.setSelected(arg);
         RealmsSelectFileToUploadScreen.this.selectedWorld = this.children().indexOf(arg);
         RealmsSelectFileToUploadScreen.this.uploadButton.active = RealmsSelectFileToUploadScreen.this.selectedWorld >= 0 && RealmsSelectFileToUploadScreen.this.selectedWorld < this.getEntryCount() && !((LevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld)).isHardcore();
      }
   }

   @Environment(EnvType.CLIENT)
   private class WorldListEntry extends AlwaysSelectedEntryListWidget.Entry {
      private final LevelSummary summary;
      private final String displayName;
      private final String nameAndLastPlayed;
      private final Text details;

      public WorldListEntry(LevelSummary summary) {
         this.summary = summary;
         this.displayName = summary.getDisplayName();
         String var10001 = summary.getName();
         this.nameAndLastPlayed = var10001 + " (" + RealmsSelectFileToUploadScreen.getLastPlayed(summary) + ")";
         Object lv;
         if (summary.isHardcore()) {
            lv = RealmsSelectFileToUploadScreen.HARDCORE_TEXT;
         } else {
            lv = RealmsSelectFileToUploadScreen.getGameModeName(summary);
         }

         if (summary.hasCheats()) {
            lv = ((Text)lv).copy().append(", ").append(RealmsSelectFileToUploadScreen.CHEATS_TEXT);
         }

         this.details = (Text)lv;
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.renderItem(matrices, index, x, y);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         RealmsSelectFileToUploadScreen.this.worldSelectionList.setSelected(RealmsSelectFileToUploadScreen.this.levelList.indexOf(this.summary));
         return true;
      }

      protected void renderItem(MatrixStack matrices, int index, int x, int y) {
         String string;
         if (this.displayName.isEmpty()) {
            string = RealmsSelectFileToUploadScreen.WORLD_LANG + " " + (index + 1);
         } else {
            string = this.displayName;
         }

         RealmsSelectFileToUploadScreen.this.textRenderer.draw(matrices, string, (float)(x + 2), (float)(y + 1), 16777215);
         RealmsSelectFileToUploadScreen.this.textRenderer.draw(matrices, this.nameAndLastPlayed, (float)(x + 2), (float)(y + 12), 8421504);
         RealmsSelectFileToUploadScreen.this.textRenderer.draw(matrices, this.details, (float)(x + 2), (float)(y + 12 + 10), 8421504);
      }

      public Text getNarration() {
         Text lv = ScreenTexts.joinLines(Text.literal(this.summary.getDisplayName()), Text.literal(RealmsSelectFileToUploadScreen.getLastPlayed(this.summary)), RealmsSelectFileToUploadScreen.getGameModeName(this.summary));
         return Text.translatable("narrator.select", lv);
      }
   }
}
