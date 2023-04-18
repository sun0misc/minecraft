package net.minecraft.client.gui.screen.pack;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

@Environment(EnvType.CLIENT)
public class ExperimentalWarningScreen extends Screen {
   private static final Text TITLE = Text.translatable("selectWorld.experimental.title");
   private static final Text MESSAGE = Text.translatable("selectWorld.experimental.message");
   private static final Text DETAILS = Text.translatable("selectWorld.experimental.details");
   private static final int field_42498 = 10;
   private static final int field_42499 = 100;
   private final BooleanConsumer callback;
   final Collection enabledProfiles;
   private final GridWidget grid = (new GridWidget()).setColumnSpacing(10).setRowSpacing(20);

   public ExperimentalWarningScreen(Collection enabledProfiles, BooleanConsumer callback) {
      super(TITLE);
      this.enabledProfiles = enabledProfiles;
      this.callback = callback;
   }

   public Text getNarratedTitle() {
      return ScreenTexts.joinSentences(super.getNarratedTitle(), MESSAGE);
   }

   protected void init() {
      super.init();
      GridWidget.Adder lv = this.grid.createAdder(2);
      Positioner lv2 = lv.copyPositioner().alignHorizontalCenter();
      lv.add(new TextWidget(this.title, this.textRenderer), 2, lv2);
      MultilineTextWidget lv3 = (MultilineTextWidget)lv.add((new MultilineTextWidget(MESSAGE, this.textRenderer)).setCentered(true), 2, lv2);
      lv3.setMaxWidth(310);
      lv.add(ButtonWidget.builder(DETAILS, (button) -> {
         this.client.setScreen(new DetailsScreen());
      }).width(100).build(), 2, lv2);
      lv.add(ButtonWidget.builder(ScreenTexts.PROCEED, (button) -> {
         this.callback.accept(true);
      }).build());
      lv.add(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.callback.accept(false);
      }).build());
      this.grid.forEachChild((child) -> {
         ClickableWidget var10000 = (ClickableWidget)this.addDrawableChild(child);
      });
      this.grid.refreshPositions();
      this.initTabNavigation();
   }

   protected void initTabNavigation() {
      SimplePositioningWidget.setPos(this.grid, 0, 0, this.width, this.height, 0.5F, 0.5F);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public void close() {
      this.callback.accept(false);
   }

   @Environment(EnvType.CLIENT)
   private class DetailsScreen extends Screen {
      private PackListWidget packList;

      DetailsScreen() {
         super(Text.translatable("selectWorld.experimental.details.title"));
      }

      public void close() {
         this.client.setScreen(ExperimentalWarningScreen.this);
      }

      protected void init() {
         super.init();
         this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
            this.close();
         }).dimensions(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20).build());
         this.packList = new PackListWidget(this.client, ExperimentalWarningScreen.this.enabledProfiles);
         this.addSelectableChild(this.packList);
      }

      public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         this.renderBackground(matrices);
         this.packList.render(matrices, mouseX, mouseY, delta);
         drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);
         super.render(matrices, mouseX, mouseY, delta);
      }

      @Environment(EnvType.CLIENT)
      class PackListWidget extends AlwaysSelectedEntryListWidget {
         public PackListWidget(MinecraftClient client, Collection enabledProfiles) {
            int var10002 = DetailsScreen.this.width;
            int var10003 = DetailsScreen.this.height;
            int var10005 = DetailsScreen.this.height - 64;
            Objects.requireNonNull(client.textRenderer);
            super(client, var10002, var10003, 32, var10005, (9 + 2) * 3);
            Iterator var4 = enabledProfiles.iterator();

            while(var4.hasNext()) {
               ResourcePackProfile lv = (ResourcePackProfile)var4.next();
               String string = FeatureFlags.printMissingFlags(FeatureFlags.VANILLA_FEATURES, lv.getRequestedFeatures());
               if (!string.isEmpty()) {
                  Text lv2 = Texts.setStyleIfAbsent(lv.getDisplayName().copy(), Style.EMPTY.withBold(true));
                  Text lv3 = Text.translatable("selectWorld.experimental.details.entry", string);
                  this.addEntry(DetailsScreen.this.new PackListWidgetEntry(lv2, lv3, MultilineText.create(DetailsScreen.this.textRenderer, lv3, this.getRowWidth())));
               }
            }

         }

         public int getRowWidth() {
            return this.width * 3 / 4;
         }
      }

      @Environment(EnvType.CLIENT)
      private class PackListWidgetEntry extends AlwaysSelectedEntryListWidget.Entry {
         private final Text displayName;
         private final Text details;
         private final MultilineText multilineDetails;

         PackListWidgetEntry(Text displayName, Text details, MultilineText multilineDetails) {
            this.displayName = displayName;
            this.details = details;
            this.multilineDetails = multilineDetails;
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            DrawableHelper.drawTextWithShadow(matrices, DetailsScreen.this.client.textRenderer, this.displayName, x, y, 16777215);
            MultilineText var10000 = this.multilineDetails;
            int var10003 = y + 12;
            Objects.requireNonNull(DetailsScreen.this.textRenderer);
            var10000.drawWithShadow(matrices, x, var10003, 9, 16777215);
         }

         public Text getNarration() {
            return Text.translatable("narrator.select", ScreenTexts.joinSentences(this.displayName, this.details));
         }
      }
   }
}
