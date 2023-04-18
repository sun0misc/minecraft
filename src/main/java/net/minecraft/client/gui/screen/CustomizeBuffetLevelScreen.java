package net.minecraft.client.gui.screen;

import com.ibm.icu.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CustomizeBuffetLevelScreen extends Screen {
   private static final Text BUFFET_BIOME_TEXT = Text.translatable("createWorld.customize.buffet.biome");
   private final Screen parent;
   private final Consumer onDone;
   final Registry biomeRegistry;
   private BuffetBiomesListWidget biomeSelectionList;
   RegistryEntry biome;
   private ButtonWidget confirmButton;

   public CustomizeBuffetLevelScreen(Screen parent, GeneratorOptionsHolder generatorOptionsHolder, Consumer onDone) {
      super(Text.translatable("createWorld.customize.buffet.title"));
      this.parent = parent;
      this.onDone = onDone;
      this.biomeRegistry = generatorOptionsHolder.getCombinedRegistryManager().get(RegistryKeys.BIOME);
      RegistryEntry lv = (RegistryEntry)this.biomeRegistry.getEntry(BiomeKeys.PLAINS).or(() -> {
         return this.biomeRegistry.streamEntries().findAny();
      }).orElseThrow();
      this.biome = (RegistryEntry)generatorOptionsHolder.selectedDimensions().getChunkGenerator().getBiomeSource().getBiomes().stream().findFirst().orElse(lv);
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   protected void init() {
      this.biomeSelectionList = new BuffetBiomesListWidget();
      this.addSelectableChild(this.biomeSelectionList);
      this.confirmButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.onDone.accept(this.biome);
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 155, this.height - 28, 150, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 + 5, this.height - 28, 150, 20).build());
      this.biomeSelectionList.setSelected((BuffetBiomesListWidget.BuffetBiomeItem)this.biomeSelectionList.children().stream().filter((entry) -> {
         return Objects.equals(entry.biome, this.biome);
      }).findFirst().orElse((Object)null));
   }

   void refreshConfirmButton() {
      this.confirmButton.active = this.biomeSelectionList.getSelectedOrNull() != null;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackgroundTexture(matrices);
      this.biomeSelectionList.render(matrices, mouseX, mouseY, delta);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
      drawCenteredTextWithShadow(matrices, this.textRenderer, BUFFET_BIOME_TEXT, this.width / 2, 28, 10526880);
      super.render(matrices, mouseX, mouseY, delta);
   }

   @Environment(EnvType.CLIENT)
   class BuffetBiomesListWidget extends AlwaysSelectedEntryListWidget {
      BuffetBiomesListWidget() {
         super(CustomizeBuffetLevelScreen.this.client, CustomizeBuffetLevelScreen.this.width, CustomizeBuffetLevelScreen.this.height, 40, CustomizeBuffetLevelScreen.this.height - 37, 16);
         Collator collator = Collator.getInstance(Locale.getDefault());
         CustomizeBuffetLevelScreen.this.biomeRegistry.streamEntries().map((entry) -> {
            return new BuffetBiomeItem(entry);
         }).sorted(Comparator.comparing((biome) -> {
            return biome.text.getString();
         }, collator)).forEach((entry) -> {
            this.addEntry(entry);
         });
      }

      public void setSelected(@Nullable BuffetBiomeItem arg) {
         super.setSelected(arg);
         if (arg != null) {
            CustomizeBuffetLevelScreen.this.biome = arg.biome;
         }

         CustomizeBuffetLevelScreen.this.refreshConfirmButton();
      }

      @Environment(EnvType.CLIENT)
      private class BuffetBiomeItem extends AlwaysSelectedEntryListWidget.Entry {
         final RegistryEntry.Reference biome;
         final Text text;

         public BuffetBiomeItem(RegistryEntry.Reference biome) {
            this.biome = biome;
            Identifier lv = biome.registryKey().getValue();
            String string = lv.toTranslationKey("biome");
            if (Language.getInstance().hasTranslation(string)) {
               this.text = Text.translatable(string);
            } else {
               this.text = Text.literal(lv.toString());
            }

         }

         public Text getNarration() {
            return Text.translatable("narrator.select", this.text);
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            DrawableHelper.drawTextWithShadow(matrices, CustomizeBuffetLevelScreen.this.textRenderer, this.text, x + 5, y + 2, 16777215);
         }

         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
               BuffetBiomesListWidget.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }
      }
   }
}
