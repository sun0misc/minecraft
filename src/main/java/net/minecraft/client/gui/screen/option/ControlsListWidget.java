package net.minecraft.client.gui.screen.option;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ControlsListWidget extends ElementListWidget {
   final KeybindsScreen parent;
   int maxKeyNameLength;

   public ControlsListWidget(KeybindsScreen parent, MinecraftClient client) {
      super(client, parent.width + 45, parent.height, 20, parent.height - 32, 20);
      this.parent = parent;
      KeyBinding[] lvs = (KeyBinding[])ArrayUtils.clone(client.options.allKeys);
      Arrays.sort(lvs);
      String string = null;
      KeyBinding[] var5 = lvs;
      int var6 = lvs.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         KeyBinding lv = var5[var7];
         String string2 = lv.getCategory();
         if (!string2.equals(string)) {
            string = string2;
            this.addEntry(new CategoryEntry(Text.translatable(string2)));
         }

         Text lv2 = Text.translatable(lv.getTranslationKey());
         int i = client.textRenderer.getWidth((StringVisitable)lv2);
         if (i > this.maxKeyNameLength) {
            this.maxKeyNameLength = i;
         }

         this.addEntry(new KeyBindingEntry(lv, lv2));
      }

   }

   public void update() {
      KeyBinding.updateKeysByCode();
      this.updateChildren();
   }

   public void updateChildren() {
      this.children().forEach(Entry::update);
   }

   protected int getScrollbarPositionX() {
      return super.getScrollbarPositionX() + 15;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 32;
   }

   @Environment(EnvType.CLIENT)
   public class CategoryEntry extends Entry {
      final Text text;
      private final int textWidth;

      public CategoryEntry(Text text) {
         this.text = text;
         this.textWidth = ControlsListWidget.this.client.textRenderer.getWidth((StringVisitable)this.text);
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         TextRenderer var10000 = ControlsListWidget.this.client.textRenderer;
         Text var10002 = this.text;
         float var10003 = (float)(ControlsListWidget.this.client.currentScreen.width / 2 - this.textWidth / 2);
         int var10004 = y + entryHeight;
         Objects.requireNonNull(ControlsListWidget.this.client.textRenderer);
         var10000.draw(matrices, var10002, var10003, (float)(var10004 - 9 - 1), 16777215);
      }

      @Nullable
      public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
         return null;
      }

      public List children() {
         return Collections.emptyList();
      }

      public List selectableChildren() {
         return ImmutableList.of(new Selectable() {
            public Selectable.SelectionType getType() {
               return Selectable.SelectionType.HOVERED;
            }

            public void appendNarrations(NarrationMessageBuilder builder) {
               builder.put(NarrationPart.TITLE, CategoryEntry.this.text);
            }
         });
      }

      protected void update() {
      }
   }

   @Environment(EnvType.CLIENT)
   public class KeyBindingEntry extends Entry {
      private final KeyBinding binding;
      private final Text bindingName;
      private final ButtonWidget editButton;
      private final ButtonWidget resetButton;
      private boolean duplicate = false;

      KeyBindingEntry(KeyBinding binding, Text bindingName) {
         this.binding = binding;
         this.bindingName = bindingName;
         this.editButton = ButtonWidget.builder(bindingName, (button) -> {
            ControlsListWidget.this.parent.selectedKeyBinding = binding;
            ControlsListWidget.this.update();
         }).dimensions(0, 0, 75, 20).narrationSupplier((textSupplier) -> {
            return binding.isUnbound() ? Text.translatable("narrator.controls.unbound", bindingName) : Text.translatable("narrator.controls.bound", bindingName, textSupplier.get());
         }).build();
         this.resetButton = ButtonWidget.builder(Text.translatable("controls.reset"), (button) -> {
            ControlsListWidget.this.client.options.setKeyCode(binding, binding.getDefaultKey());
            ControlsListWidget.this.update();
         }).dimensions(0, 0, 50, 20).narrationSupplier((textSupplier) -> {
            return Text.translatable("narrator.controls.reset", bindingName);
         }).build();
         this.update();
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         TextRenderer var10000 = ControlsListWidget.this.client.textRenderer;
         Text var10002 = this.bindingName;
         float var10003 = (float)(x + 90 - ControlsListWidget.this.maxKeyNameLength);
         int var10004 = y + entryHeight / 2;
         Objects.requireNonNull(ControlsListWidget.this.client.textRenderer);
         var10000.draw(matrices, var10002, var10003, (float)(var10004 - 9 / 2), 16777215);
         this.resetButton.setX(x + 190);
         this.resetButton.setY(y);
         this.resetButton.render(matrices, mouseX, mouseY, tickDelta);
         this.editButton.setX(x + 105);
         this.editButton.setY(y);
         if (this.duplicate) {
            int p = true;
            int q = this.editButton.getX() - 6;
            DrawableHelper.fill(matrices, q, y + 2, q + 3, y + entryHeight + 2, Formatting.RED.getColorValue() | -16777216);
         }

         this.editButton.render(matrices, mouseX, mouseY, tickDelta);
      }

      public List children() {
         return ImmutableList.of(this.editButton, this.resetButton);
      }

      public List selectableChildren() {
         return ImmutableList.of(this.editButton, this.resetButton);
      }

      protected void update() {
         this.editButton.setMessage(this.binding.getBoundKeyLocalizedText());
         this.resetButton.active = !this.binding.isDefault();
         this.duplicate = false;
         MutableText lv = Text.empty();
         if (!this.binding.isUnbound()) {
            KeyBinding[] var2 = ControlsListWidget.this.client.options.allKeys;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               KeyBinding lv2 = var2[var4];
               if (lv2 != this.binding && this.binding.equals(lv2)) {
                  if (this.duplicate) {
                     lv.append(", ");
                  }

                  this.duplicate = true;
                  lv.append((Text)Text.translatable(lv2.getTranslationKey()));
               }
            }
         }

         if (this.duplicate) {
            this.editButton.setMessage(Text.literal("[ ").append((Text)this.editButton.getMessage().copy().formatted(Formatting.WHITE)).append(" ]").formatted(Formatting.RED));
            this.editButton.setTooltip(Tooltip.of(Text.translatable("controls.keybinds.duplicateKeybinds", lv)));
         } else {
            this.editButton.setTooltip((Tooltip)null);
         }

         if (ControlsListWidget.this.parent.selectedKeyBinding == this.binding) {
            this.editButton.setMessage(Text.literal("> ").append((Text)this.editButton.getMessage().copy().formatted(Formatting.WHITE, Formatting.UNDERLINE)).append(" <").formatted(Formatting.YELLOW));
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry extends ElementListWidget.Entry {
      abstract void update();
   }
}
