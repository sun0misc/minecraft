package net.minecraft.client.gui.screen.pack;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

@Environment(EnvType.CLIENT)
public class PackListWidget extends AlwaysSelectedEntryListWidget {
   static final Identifier RESOURCE_PACKS_TEXTURE = new Identifier("textures/gui/resource_packs.png");
   static final Text INCOMPATIBLE = Text.translatable("pack.incompatible");
   static final Text INCOMPATIBLE_CONFIRM = Text.translatable("pack.incompatible.confirm.title");
   private final Text title;
   final PackScreen screen;

   public PackListWidget(MinecraftClient client, PackScreen screen, int width, int height, Text title) {
      super(client, width, height, 32, height - 55 + 4, 36);
      this.screen = screen;
      this.title = title;
      this.centerListVertically = false;
      Objects.requireNonNull(client.textRenderer);
      this.setRenderHeader(true, (int)(9.0F * 1.5F));
   }

   protected void renderHeader(MatrixStack matrices, int x, int y) {
      Text lv = Text.empty().append(this.title).formatted(Formatting.UNDERLINE, Formatting.BOLD);
      this.client.textRenderer.draw(matrices, (Text)lv, (float)(x + this.width / 2 - this.client.textRenderer.getWidth((StringVisitable)lv) / 2), (float)Math.min(this.top + 3, y), 16777215);
   }

   public int getRowWidth() {
      return this.width;
   }

   protected int getScrollbarPositionX() {
      return this.right - 6;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.getSelectedOrNull() != null) {
         switch (keyCode) {
            case 32:
            case 257:
               ((ResourcePackEntry)this.getSelectedOrNull()).toggle();
               return true;
            default:
               if (Screen.hasShiftDown()) {
                  switch (keyCode) {
                     case 264:
                        ((ResourcePackEntry)this.getSelectedOrNull()).moveTowardEnd();
                        return true;
                     case 265:
                        ((ResourcePackEntry)this.getSelectedOrNull()).moveTowardStart();
                        return true;
                  }
               }
         }
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Environment(EnvType.CLIENT)
   public static class ResourcePackEntry extends AlwaysSelectedEntryListWidget.Entry {
      private static final int field_32397 = 0;
      private static final int field_32398 = 32;
      private static final int field_32399 = 64;
      private static final int field_32400 = 96;
      private static final int field_32401 = 0;
      private static final int field_32402 = 32;
      private static final int field_32403 = 157;
      private static final int field_32404 = 157;
      private static final String ELLIPSIS = "...";
      private final PackListWidget widget;
      protected final MinecraftClient client;
      private final ResourcePackOrganizer.Pack pack;
      private final OrderedText displayName;
      private final MultilineText description;
      private final OrderedText incompatibleText;
      private final MultilineText compatibilityNotificationText;

      public ResourcePackEntry(MinecraftClient client, PackListWidget widget, ResourcePackOrganizer.Pack pack) {
         this.client = client;
         this.pack = pack;
         this.widget = widget;
         this.displayName = trimTextToWidth(client, pack.getDisplayName());
         this.description = createMultilineText(client, pack.getDecoratedDescription());
         this.incompatibleText = trimTextToWidth(client, PackListWidget.INCOMPATIBLE);
         this.compatibilityNotificationText = createMultilineText(client, pack.getCompatibility().getNotification());
      }

      private static OrderedText trimTextToWidth(MinecraftClient client, Text text) {
         int i = client.textRenderer.getWidth((StringVisitable)text);
         if (i > 157) {
            StringVisitable lv = StringVisitable.concat(client.textRenderer.trimToWidth((StringVisitable)text, 157 - client.textRenderer.getWidth("...")), StringVisitable.plain("..."));
            return Language.getInstance().reorder(lv);
         } else {
            return text.asOrderedText();
         }
      }

      private static MultilineText createMultilineText(MinecraftClient client, Text text) {
         return MultilineText.create(client.textRenderer, text, 157, 2);
      }

      public Text getNarration() {
         return Text.translatable("narrator.select", this.pack.getDisplayName());
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         ResourcePackCompatibility lv = this.pack.getCompatibility();
         if (!lv.isCompatible()) {
            DrawableHelper.fill(matrices, x - 1, y - 1, x + entryWidth - 9, y + entryHeight + 1, -8978432);
         }

         RenderSystem.setShaderTexture(0, this.pack.getIconId());
         DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
         OrderedText lv2 = this.displayName;
         MultilineText lv3 = this.description;
         if (this.isSelectable() && ((Boolean)this.client.options.getTouchscreen().getValue() || hovered || this.widget.getSelectedOrNull() == this && this.widget.isFocused())) {
            RenderSystem.setShaderTexture(0, PackListWidget.RESOURCE_PACKS_TEXTURE);
            DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
            int p = mouseX - x;
            int q = mouseY - y;
            if (!this.pack.getCompatibility().isCompatible()) {
               lv2 = this.incompatibleText;
               lv3 = this.compatibilityNotificationText;
            }

            if (this.pack.canBeEnabled()) {
               if (p < 32) {
                  DrawableHelper.drawTexture(matrices, x, y, 0.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 256, 256);
               }
            } else {
               if (this.pack.canBeDisabled()) {
                  if (p < 16) {
                     DrawableHelper.drawTexture(matrices, x, y, 32.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     DrawableHelper.drawTexture(matrices, x, y, 32.0F, 0.0F, 32, 32, 256, 256);
                  }
               }

               if (this.pack.canMoveTowardStart()) {
                  if (p < 32 && p > 16 && q < 16) {
                     DrawableHelper.drawTexture(matrices, x, y, 96.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     DrawableHelper.drawTexture(matrices, x, y, 96.0F, 0.0F, 32, 32, 256, 256);
                  }
               }

               if (this.pack.canMoveTowardEnd()) {
                  if (p < 32 && p > 16 && q > 16) {
                     DrawableHelper.drawTexture(matrices, x, y, 64.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     DrawableHelper.drawTexture(matrices, x, y, 64.0F, 0.0F, 32, 32, 256, 256);
                  }
               }
            }
         }

         this.client.textRenderer.drawWithShadow(matrices, lv2, (float)(x + 32 + 2), (float)(y + 1), 16777215);
         lv3.drawWithShadow(matrices, x + 32 + 2, y + 12, 10, 8421504);
      }

      public String getName() {
         return this.pack.getName();
      }

      private boolean isSelectable() {
         return !this.pack.isPinned() || !this.pack.isAlwaysEnabled();
      }

      public void toggle() {
         if (this.pack.canBeEnabled() && this.enable()) {
            this.widget.screen.switchFocusedList(this.widget);
         } else if (this.pack.canBeDisabled()) {
            this.pack.disable();
            this.widget.screen.switchFocusedList(this.widget);
         }

      }

      void moveTowardStart() {
         if (this.pack.canMoveTowardStart()) {
            this.pack.moveTowardStart();
         }

      }

      void moveTowardEnd() {
         if (this.pack.canMoveTowardEnd()) {
            this.pack.moveTowardEnd();
         }

      }

      private boolean enable() {
         if (this.pack.getCompatibility().isCompatible()) {
            this.pack.enable();
            return true;
         } else {
            Text lv = this.pack.getCompatibility().getConfirmMessage();
            this.client.setScreen(new ConfirmScreen((confirmed) -> {
               this.client.setScreen(this.widget.screen);
               if (confirmed) {
                  this.pack.enable();
               }

            }, PackListWidget.INCOMPATIBLE_CONFIRM, lv));
            return false;
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (button != 0) {
            return false;
         } else {
            double f = mouseX - (double)this.widget.getRowLeft();
            double g = mouseY - (double)this.widget.getRowTop(this.widget.children().indexOf(this));
            if (this.isSelectable() && f <= 32.0) {
               this.widget.screen.clearSelection();
               if (this.pack.canBeEnabled()) {
                  this.enable();
                  return true;
               }

               if (f < 16.0 && this.pack.canBeDisabled()) {
                  this.pack.disable();
                  return true;
               }

               if (f > 16.0 && g < 16.0 && this.pack.canMoveTowardStart()) {
                  this.pack.moveTowardStart();
                  return true;
               }

               if (f > 16.0 && g > 16.0 && this.pack.canMoveTowardEnd()) {
                  this.pack.moveTowardEnd();
                  return true;
               }
            }

            return false;
         }
      }
   }
}
