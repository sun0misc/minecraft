package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BookScreen extends Screen {
   public static final int field_32328 = 16;
   public static final int field_32329 = 36;
   public static final int field_32330 = 30;
   public static final Contents EMPTY_PROVIDER = new Contents() {
      public int getPageCount() {
         return 0;
      }

      public StringVisitable getPageUnchecked(int index) {
         return StringVisitable.EMPTY;
      }
   };
   public static final Identifier BOOK_TEXTURE = new Identifier("textures/gui/book.png");
   protected static final int MAX_TEXT_WIDTH = 114;
   protected static final int MAX_TEXT_HEIGHT = 128;
   protected static final int WIDTH = 192;
   protected static final int HEIGHT = 192;
   private Contents contents;
   private int pageIndex;
   private List cachedPage;
   private int cachedPageIndex;
   private Text pageIndexText;
   private PageTurnWidget nextPageButton;
   private PageTurnWidget previousPageButton;
   private final boolean pageTurnSound;

   public BookScreen(Contents pageProvider) {
      this(pageProvider, true);
   }

   public BookScreen() {
      this(EMPTY_PROVIDER, false);
   }

   private BookScreen(Contents contents, boolean playPageTurnSound) {
      super(NarratorManager.EMPTY);
      this.cachedPage = Collections.emptyList();
      this.cachedPageIndex = -1;
      this.pageIndexText = ScreenTexts.EMPTY;
      this.contents = contents;
      this.pageTurnSound = playPageTurnSound;
   }

   public void setPageProvider(Contents pageProvider) {
      this.contents = pageProvider;
      this.pageIndex = MathHelper.clamp(this.pageIndex, 0, pageProvider.getPageCount());
      this.updatePageButtons();
      this.cachedPageIndex = -1;
   }

   public boolean setPage(int index) {
      int j = MathHelper.clamp(index, 0, this.contents.getPageCount() - 1);
      if (j != this.pageIndex) {
         this.pageIndex = j;
         this.updatePageButtons();
         this.cachedPageIndex = -1;
         return true;
      } else {
         return false;
      }
   }

   protected boolean jumpToPage(int page) {
      return this.setPage(page);
   }

   protected void init() {
      this.addCloseButton();
      this.addPageButtons();
   }

   protected void addCloseButton() {
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.close();
      }).dimensions(this.width / 2 - 100, 196, 200, 20).build());
   }

   protected void addPageButtons() {
      int i = (this.width - 192) / 2;
      int j = true;
      this.nextPageButton = (PageTurnWidget)this.addDrawableChild(new PageTurnWidget(i + 116, 159, true, (button) -> {
         this.goToNextPage();
      }, this.pageTurnSound));
      this.previousPageButton = (PageTurnWidget)this.addDrawableChild(new PageTurnWidget(i + 43, 159, false, (button) -> {
         this.goToPreviousPage();
      }, this.pageTurnSound));
      this.updatePageButtons();
   }

   private int getPageCount() {
      return this.contents.getPageCount();
   }

   protected void goToPreviousPage() {
      if (this.pageIndex > 0) {
         --this.pageIndex;
      }

      this.updatePageButtons();
   }

   protected void goToNextPage() {
      if (this.pageIndex < this.getPageCount() - 1) {
         ++this.pageIndex;
      }

      this.updatePageButtons();
   }

   private void updatePageButtons() {
      this.nextPageButton.visible = this.pageIndex < this.getPageCount() - 1;
      this.previousPageButton.visible = this.pageIndex > 0;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else {
         switch (keyCode) {
            case 266:
               this.previousPageButton.onPress();
               return true;
            case 267:
               this.nextPageButton.onPress();
               return true;
            default:
               return false;
         }
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      RenderSystem.setShaderTexture(0, BOOK_TEXTURE);
      int k = (this.width - 192) / 2;
      int l = true;
      drawTexture(matrices, k, 2, 0, 0, 192, 192);
      if (this.cachedPageIndex != this.pageIndex) {
         StringVisitable lv = this.contents.getPage(this.pageIndex);
         this.cachedPage = this.textRenderer.wrapLines(lv, 114);
         this.pageIndexText = Text.translatable("book.pageIndicator", this.pageIndex + 1, Math.max(this.getPageCount(), 1));
      }

      this.cachedPageIndex = this.pageIndex;
      int m = this.textRenderer.getWidth((StringVisitable)this.pageIndexText);
      this.textRenderer.draw(matrices, (Text)this.pageIndexText, (float)(k - m + 192 - 44), 18.0F, 0);
      Objects.requireNonNull(this.textRenderer);
      int n = Math.min(128 / 9, this.cachedPage.size());

      for(int o = 0; o < n; ++o) {
         OrderedText lv2 = (OrderedText)this.cachedPage.get(o);
         TextRenderer var10000 = this.textRenderer;
         float var10003 = (float)(k + 36);
         Objects.requireNonNull(this.textRenderer);
         var10000.draw(matrices, (OrderedText)lv2, var10003, (float)(32 + o * 9), 0);
      }

      Style lv3 = this.getTextStyleAt((double)mouseX, (double)mouseY);
      if (lv3 != null) {
         this.renderTextHoverEffect(matrices, lv3, mouseX, mouseY);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         Style lv = this.getTextStyleAt(mouseX, mouseY);
         if (lv != null && this.handleTextClick(lv)) {
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean handleTextClick(Style style) {
      ClickEvent lv = style.getClickEvent();
      if (lv == null) {
         return false;
      } else if (lv.getAction() == ClickEvent.Action.CHANGE_PAGE) {
         String string = lv.getValue();

         try {
            int i = Integer.parseInt(string) - 1;
            return this.jumpToPage(i);
         } catch (Exception var5) {
            return false;
         }
      } else {
         boolean bl = super.handleTextClick(style);
         if (bl && lv.getAction() == ClickEvent.Action.RUN_COMMAND) {
            this.closeScreen();
         }

         return bl;
      }
   }

   protected void closeScreen() {
      this.client.setScreen((Screen)null);
   }

   @Nullable
   public Style getTextStyleAt(double x, double y) {
      if (this.cachedPage.isEmpty()) {
         return null;
      } else {
         int i = MathHelper.floor(x - (double)((this.width - 192) / 2) - 36.0);
         int j = MathHelper.floor(y - 2.0 - 30.0);
         if (i >= 0 && j >= 0) {
            Objects.requireNonNull(this.textRenderer);
            int k = Math.min(128 / 9, this.cachedPage.size());
            if (i <= 114) {
               Objects.requireNonNull(this.client.textRenderer);
               if (j < 9 * k + k) {
                  Objects.requireNonNull(this.client.textRenderer);
                  int l = j / 9;
                  if (l >= 0 && l < this.cachedPage.size()) {
                     OrderedText lv = (OrderedText)this.cachedPage.get(l);
                     return this.client.textRenderer.getTextHandler().getStyleAt(lv, i);
                  }

                  return null;
               }
            }

            return null;
         } else {
            return null;
         }
      }
   }

   static List readPages(NbtCompound nbt) {
      ImmutableList.Builder builder = ImmutableList.builder();
      Objects.requireNonNull(builder);
      filterPages(nbt, builder::add);
      return builder.build();
   }

   public static void filterPages(NbtCompound nbt, Consumer pageConsumer) {
      NbtList lv = nbt.getList("pages", NbtElement.STRING_TYPE).copy();
      IntFunction intFunction;
      if (MinecraftClient.getInstance().shouldFilterText() && nbt.contains("filtered_pages", NbtElement.COMPOUND_TYPE)) {
         NbtCompound lv2 = nbt.getCompound("filtered_pages");
         intFunction = (page) -> {
            String string = String.valueOf(page);
            return lv2.contains(string) ? lv2.getString(string) : lv.getString(page);
         };
      } else {
         Objects.requireNonNull(lv);
         intFunction = lv::getString;
      }

      for(int i = 0; i < lv.size(); ++i) {
         pageConsumer.accept((String)intFunction.apply(i));
      }

   }

   @Environment(EnvType.CLIENT)
   public interface Contents {
      int getPageCount();

      StringVisitable getPageUnchecked(int index);

      default StringVisitable getPage(int index) {
         return index >= 0 && index < this.getPageCount() ? this.getPageUnchecked(index) : StringVisitable.EMPTY;
      }

      static Contents create(ItemStack stack) {
         if (stack.isOf(Items.WRITTEN_BOOK)) {
            return new WrittenBookContents(stack);
         } else {
            return (Contents)(stack.isOf(Items.WRITABLE_BOOK) ? new WritableBookContents(stack) : BookScreen.EMPTY_PROVIDER);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static class WritableBookContents implements Contents {
      private final List pages;

      public WritableBookContents(ItemStack stack) {
         this.pages = getPages(stack);
      }

      private static List getPages(ItemStack stack) {
         NbtCompound lv = stack.getNbt();
         return (List)(lv != null ? BookScreen.readPages(lv) : ImmutableList.of());
      }

      public int getPageCount() {
         return this.pages.size();
      }

      public StringVisitable getPageUnchecked(int index) {
         return StringVisitable.plain((String)this.pages.get(index));
      }
   }

   @Environment(EnvType.CLIENT)
   public static class WrittenBookContents implements Contents {
      private final List pages;

      public WrittenBookContents(ItemStack stack) {
         this.pages = getPages(stack);
      }

      private static List getPages(ItemStack stack) {
         NbtCompound lv = stack.getNbt();
         return (List)(lv != null && WrittenBookItem.isValid(lv) ? BookScreen.readPages(lv) : ImmutableList.of(Text.Serializer.toJson(Text.translatable("book.invalid.tag").formatted(Formatting.DARK_RED))));
      }

      public int getPageCount() {
         return this.pages.size();
      }

      public StringVisitable getPageUnchecked(int index) {
         String string = (String)this.pages.get(index);

         try {
            StringVisitable lv = Text.Serializer.fromJson(string);
            if (lv != null) {
               return lv;
            }
         } catch (Exception var4) {
         }

         return StringVisitable.plain(string);
      }
   }
}
