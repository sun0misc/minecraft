package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BookEditScreen extends Screen {
   private static final int MAX_TEXT_WIDTH = 114;
   private static final int MAX_TEXT_HEIGHT = 128;
   private static final int WIDTH = 192;
   private static final int HEIGHT = 192;
   private static final Text EDIT_TITLE_TEXT = Text.translatable("book.editTitle");
   private static final Text FINALIZE_WARNING_TEXT = Text.translatable("book.finalizeWarning");
   private static final OrderedText BLACK_CURSOR_TEXT;
   private static final OrderedText GRAY_CURSOR_TEXT;
   private final PlayerEntity player;
   private final ItemStack itemStack;
   private boolean dirty;
   private boolean signing;
   private int tickCounter;
   private int currentPage;
   private final List pages = Lists.newArrayList();
   private String title = "";
   private final SelectionManager currentPageSelectionManager = new SelectionManager(this::getCurrentPageContent, this::setPageContent, this::getClipboard, this::setClipboard, (string) -> {
      return string.length() < 1024 && this.textRenderer.getWrappedLinesHeight((String)string, 114) <= 128;
   });
   private final SelectionManager bookTitleSelectionManager = new SelectionManager(() -> {
      return this.title;
   }, (title) -> {
      this.title = title;
   }, this::getClipboard, this::setClipboard, (string) -> {
      return string.length() < 16;
   });
   private long lastClickTime;
   private int lastClickIndex = -1;
   private PageTurnWidget nextPageButton;
   private PageTurnWidget previousPageButton;
   private ButtonWidget doneButton;
   private ButtonWidget signButton;
   private ButtonWidget finalizeButton;
   private ButtonWidget cancelButton;
   private final Hand hand;
   @Nullable
   private PageContent pageContent;
   private Text pageIndicatorText;
   private final Text signedByText;

   public BookEditScreen(PlayerEntity player, ItemStack itemStack, Hand hand) {
      super(NarratorManager.EMPTY);
      this.pageContent = BookEditScreen.PageContent.EMPTY;
      this.pageIndicatorText = ScreenTexts.EMPTY;
      this.player = player;
      this.itemStack = itemStack;
      this.hand = hand;
      NbtCompound lv = itemStack.getNbt();
      if (lv != null) {
         List var10001 = this.pages;
         Objects.requireNonNull(var10001);
         BookScreen.filterPages(lv, var10001::add);
      }

      if (this.pages.isEmpty()) {
         this.pages.add("");
      }

      this.signedByText = Text.translatable("book.byAuthor", player.getName()).formatted(Formatting.DARK_GRAY);
   }

   private void setClipboard(String clipboard) {
      if (this.client != null) {
         SelectionManager.setClipboard(this.client, clipboard);
      }

   }

   private String getClipboard() {
      return this.client != null ? SelectionManager.getClipboard(this.client) : "";
   }

   private int countPages() {
      return this.pages.size();
   }

   public void tick() {
      super.tick();
      ++this.tickCounter;
   }

   protected void init() {
      this.invalidatePageContent();
      this.signButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("book.signButton"), (button) -> {
         this.signing = true;
         this.updateButtons();
      }).dimensions(this.width / 2 - 100, 196, 98, 20).build());
      this.doneButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.client.setScreen((Screen)null);
         this.finalizeBook(false);
      }).dimensions(this.width / 2 + 2, 196, 98, 20).build());
      this.finalizeButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("book.finalizeButton"), (button) -> {
         if (this.signing) {
            this.finalizeBook(true);
            this.client.setScreen((Screen)null);
         }

      }).dimensions(this.width / 2 - 100, 196, 98, 20).build());
      this.cancelButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         if (this.signing) {
            this.signing = false;
         }

         this.updateButtons();
      }).dimensions(this.width / 2 + 2, 196, 98, 20).build());
      int i = (this.width - 192) / 2;
      int j = true;
      this.nextPageButton = (PageTurnWidget)this.addDrawableChild(new PageTurnWidget(i + 116, 159, true, (button) -> {
         this.openNextPage();
      }, true));
      this.previousPageButton = (PageTurnWidget)this.addDrawableChild(new PageTurnWidget(i + 43, 159, false, (button) -> {
         this.openPreviousPage();
      }, true));
      this.updateButtons();
   }

   private void openPreviousPage() {
      if (this.currentPage > 0) {
         --this.currentPage;
      }

      this.updateButtons();
      this.changePage();
   }

   private void openNextPage() {
      if (this.currentPage < this.countPages() - 1) {
         ++this.currentPage;
      } else {
         this.appendNewPage();
         if (this.currentPage < this.countPages() - 1) {
            ++this.currentPage;
         }
      }

      this.updateButtons();
      this.changePage();
   }

   private void updateButtons() {
      this.previousPageButton.visible = !this.signing && this.currentPage > 0;
      this.nextPageButton.visible = !this.signing;
      this.doneButton.visible = !this.signing;
      this.signButton.visible = !this.signing;
      this.cancelButton.visible = this.signing;
      this.finalizeButton.visible = this.signing;
      this.finalizeButton.active = !this.title.trim().isEmpty();
   }

   private void removeEmptyPages() {
      ListIterator listIterator = this.pages.listIterator(this.pages.size());

      while(listIterator.hasPrevious() && ((String)listIterator.previous()).isEmpty()) {
         listIterator.remove();
      }

   }

   private void finalizeBook(boolean signBook) {
      if (this.dirty) {
         this.removeEmptyPages();
         this.writeNbtData(signBook);
         int i = this.hand == Hand.MAIN_HAND ? this.player.getInventory().selectedSlot : 40;
         this.client.getNetworkHandler().sendPacket(new BookUpdateC2SPacket(i, this.pages, signBook ? Optional.of(this.title.trim()) : Optional.empty()));
      }
   }

   private void writeNbtData(boolean signBook) {
      NbtList lv = new NbtList();
      Stream var10000 = this.pages.stream().map(NbtString::of);
      Objects.requireNonNull(lv);
      var10000.forEach(lv::add);
      if (!this.pages.isEmpty()) {
         this.itemStack.setSubNbt("pages", lv);
      }

      if (signBook) {
         this.itemStack.setSubNbt("author", NbtString.of(this.player.getGameProfile().getName()));
         this.itemStack.setSubNbt("title", NbtString.of(this.title.trim()));
      }

   }

   private void appendNewPage() {
      if (this.countPages() < 100) {
         this.pages.add("");
         this.dirty = true;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (this.signing) {
         return this.keyPressedSignMode(keyCode, scanCode, modifiers);
      } else {
         boolean bl = this.keyPressedEditMode(keyCode, scanCode, modifiers);
         if (bl) {
            this.invalidatePageContent();
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      if (super.charTyped(chr, modifiers)) {
         return true;
      } else if (this.signing) {
         boolean bl = this.bookTitleSelectionManager.insert(chr);
         if (bl) {
            this.updateButtons();
            this.dirty = true;
            return true;
         } else {
            return false;
         }
      } else if (SharedConstants.isValidChar(chr)) {
         this.currentPageSelectionManager.insert(Character.toString(chr));
         this.invalidatePageContent();
         return true;
      } else {
         return false;
      }
   }

   private boolean keyPressedEditMode(int keyCode, int scanCode, int modifiers) {
      if (Screen.isSelectAll(keyCode)) {
         this.currentPageSelectionManager.selectAll();
         return true;
      } else if (Screen.isCopy(keyCode)) {
         this.currentPageSelectionManager.copy();
         return true;
      } else if (Screen.isPaste(keyCode)) {
         this.currentPageSelectionManager.paste();
         return true;
      } else if (Screen.isCut(keyCode)) {
         this.currentPageSelectionManager.cut();
         return true;
      } else {
         SelectionManager.SelectionType lv = Screen.hasControlDown() ? SelectionManager.SelectionType.WORD : SelectionManager.SelectionType.CHARACTER;
         switch (keyCode) {
            case 257:
            case 335:
               this.currentPageSelectionManager.insert("\n");
               return true;
            case 259:
               this.currentPageSelectionManager.delete(-1, lv);
               return true;
            case 261:
               this.currentPageSelectionManager.delete(1, lv);
               return true;
            case 262:
               this.currentPageSelectionManager.moveCursor(1, Screen.hasShiftDown(), lv);
               return true;
            case 263:
               this.currentPageSelectionManager.moveCursor(-1, Screen.hasShiftDown(), lv);
               return true;
            case 264:
               this.moveDownLine();
               return true;
            case 265:
               this.moveUpLine();
               return true;
            case 266:
               this.previousPageButton.onPress();
               return true;
            case 267:
               this.nextPageButton.onPress();
               return true;
            case 268:
               this.moveToLineStart();
               return true;
            case 269:
               this.moveToLineEnd();
               return true;
            default:
               return false;
         }
      }
   }

   private void moveUpLine() {
      this.moveVertically(-1);
   }

   private void moveDownLine() {
      this.moveVertically(1);
   }

   private void moveVertically(int lines) {
      int j = this.currentPageSelectionManager.getSelectionStart();
      int k = this.getPageContent().getVerticalOffset(j, lines);
      this.currentPageSelectionManager.moveCursorTo(k, Screen.hasShiftDown());
   }

   private void moveToLineStart() {
      if (Screen.hasControlDown()) {
         this.currentPageSelectionManager.moveCursorToStart(Screen.hasShiftDown());
      } else {
         int i = this.currentPageSelectionManager.getSelectionStart();
         int j = this.getPageContent().getLineStart(i);
         this.currentPageSelectionManager.moveCursorTo(j, Screen.hasShiftDown());
      }

   }

   private void moveToLineEnd() {
      if (Screen.hasControlDown()) {
         this.currentPageSelectionManager.moveCursorToEnd(Screen.hasShiftDown());
      } else {
         PageContent lv = this.getPageContent();
         int i = this.currentPageSelectionManager.getSelectionStart();
         int j = lv.getLineEnd(i);
         this.currentPageSelectionManager.moveCursorTo(j, Screen.hasShiftDown());
      }

   }

   private boolean keyPressedSignMode(int keyCode, int scanCode, int modifiers) {
      switch (keyCode) {
         case 257:
         case 335:
            if (!this.title.isEmpty()) {
               this.finalizeBook(true);
               this.client.setScreen((Screen)null);
            }

            return true;
         case 259:
            this.bookTitleSelectionManager.delete(-1);
            this.updateButtons();
            this.dirty = true;
            return true;
         default:
            return false;
      }
   }

   private String getCurrentPageContent() {
      return this.currentPage >= 0 && this.currentPage < this.pages.size() ? (String)this.pages.get(this.currentPage) : "";
   }

   private void setPageContent(String newContent) {
      if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
         this.pages.set(this.currentPage, newContent);
         this.dirty = true;
         this.invalidatePageContent();
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.setFocused((Element)null);
      RenderSystem.setShaderTexture(0, BookScreen.BOOK_TEXTURE);
      int k = (this.width - 192) / 2;
      int l = true;
      drawTexture(matrices, k, 2, 0, 0, 192, 192);
      int n;
      int o;
      if (this.signing) {
         boolean bl = this.tickCounter / 6 % 2 == 0;
         OrderedText lv = OrderedText.concat(OrderedText.styledForwardsVisitedString(this.title, Style.EMPTY), bl ? BLACK_CURSOR_TEXT : GRAY_CURSOR_TEXT);
         int m = this.textRenderer.getWidth((StringVisitable)EDIT_TITLE_TEXT);
         this.textRenderer.draw(matrices, (Text)EDIT_TITLE_TEXT, (float)(k + 36 + (114 - m) / 2), 34.0F, 0);
         n = this.textRenderer.getWidth(lv);
         this.textRenderer.draw(matrices, (OrderedText)lv, (float)(k + 36 + (114 - n) / 2), 50.0F, 0);
         o = this.textRenderer.getWidth((StringVisitable)this.signedByText);
         this.textRenderer.draw(matrices, (Text)this.signedByText, (float)(k + 36 + (114 - o) / 2), 60.0F, 0);
         this.textRenderer.drawTrimmed(matrices, FINALIZE_WARNING_TEXT, k + 36, 82, 114, 0);
      } else {
         int p = this.textRenderer.getWidth((StringVisitable)this.pageIndicatorText);
         this.textRenderer.draw(matrices, (Text)this.pageIndicatorText, (float)(k - p + 192 - 44), 18.0F, 0);
         PageContent lv2 = this.getPageContent();
         Line[] var15 = lv2.lines;
         n = var15.length;

         for(o = 0; o < n; ++o) {
            Line lv3 = var15[o];
            this.textRenderer.draw(matrices, lv3.text, (float)lv3.x, (float)lv3.y, -16777216);
         }

         this.drawSelection(matrices, lv2.selectionRectangles);
         this.drawCursor(matrices, lv2.position, lv2.atEnd);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }

   private void drawCursor(MatrixStack matrices, Position position, boolean atEnd) {
      if (this.tickCounter / 6 % 2 == 0) {
         position = this.absolutePositionToScreenPosition(position);
         if (!atEnd) {
            int var10001 = position.x;
            int var10002 = position.y - 1;
            int var10003 = position.x + 1;
            int var10004 = position.y;
            Objects.requireNonNull(this.textRenderer);
            DrawableHelper.fill(matrices, var10001, var10002, var10003, var10004 + 9, -16777216);
         } else {
            this.textRenderer.draw(matrices, (String)"_", (float)position.x, (float)position.y, 0);
         }
      }

   }

   private void drawSelection(MatrixStack matrices, Rect2i[] selectionRectangles) {
      RenderSystem.enableColorLogicOp();
      RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
      Rect2i[] var3 = selectionRectangles;
      int var4 = selectionRectangles.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Rect2i lv = var3[var5];
         int i = lv.getX();
         int j = lv.getY();
         int k = i + lv.getWidth();
         int l = j + lv.getHeight();
         fill(matrices, i, j, k, l, -16776961);
      }

      RenderSystem.disableColorLogicOp();
   }

   private Position screenPositionToAbsolutePosition(Position position) {
      return new Position(position.x - (this.width - 192) / 2 - 36, position.y - 32);
   }

   private Position absolutePositionToScreenPosition(Position position) {
      return new Position(position.x + (this.width - 192) / 2 + 36, position.y + 32);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         if (button == 0) {
            long l = Util.getMeasuringTimeMs();
            PageContent lv = this.getPageContent();
            int j = lv.getCursorPosition(this.textRenderer, this.screenPositionToAbsolutePosition(new Position((int)mouseX, (int)mouseY)));
            if (j >= 0) {
               if (j == this.lastClickIndex && l - this.lastClickTime < 250L) {
                  if (!this.currentPageSelectionManager.isSelecting()) {
                     this.selectCurrentWord(j);
                  } else {
                     this.currentPageSelectionManager.selectAll();
                  }
               } else {
                  this.currentPageSelectionManager.moveCursorTo(j, Screen.hasShiftDown());
               }

               this.invalidatePageContent();
            }

            this.lastClickIndex = j;
            this.lastClickTime = l;
         }

         return true;
      }
   }

   private void selectCurrentWord(int cursor) {
      String string = this.getCurrentPageContent();
      this.currentPageSelectionManager.setSelection(TextHandler.moveCursorByWords(string, -1, cursor, false), TextHandler.moveCursorByWords(string, 1, cursor, false));
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      } else {
         if (button == 0) {
            PageContent lv = this.getPageContent();
            int j = lv.getCursorPosition(this.textRenderer, this.screenPositionToAbsolutePosition(new Position((int)mouseX, (int)mouseY)));
            this.currentPageSelectionManager.moveCursorTo(j, true);
            this.invalidatePageContent();
         }

         return true;
      }
   }

   private PageContent getPageContent() {
      if (this.pageContent == null) {
         this.pageContent = this.createPageContent();
         this.pageIndicatorText = Text.translatable("book.pageIndicator", this.currentPage + 1, this.countPages());
      }

      return this.pageContent;
   }

   private void invalidatePageContent() {
      this.pageContent = null;
   }

   private void changePage() {
      this.currentPageSelectionManager.putCursorAtEnd();
      this.invalidatePageContent();
   }

   private PageContent createPageContent() {
      String string = this.getCurrentPageContent();
      if (string.isEmpty()) {
         return BookEditScreen.PageContent.EMPTY;
      } else {
         int i = this.currentPageSelectionManager.getSelectionStart();
         int j = this.currentPageSelectionManager.getSelectionEnd();
         IntList intList = new IntArrayList();
         List list = Lists.newArrayList();
         MutableInt mutableInt = new MutableInt();
         MutableBoolean mutableBoolean = new MutableBoolean();
         TextHandler lv = this.textRenderer.getTextHandler();
         lv.wrapLines(string, 114, Style.EMPTY, true, (style, start, end) -> {
            int k = mutableInt.getAndIncrement();
            String string2 = string.substring(start, end);
            mutableBoolean.setValue(string2.endsWith("\n"));
            String string3 = StringUtils.stripEnd(string2, " \n");
            Objects.requireNonNull(this.textRenderer);
            int l = k * 9;
            Position lv = this.absolutePositionToScreenPosition(new Position(0, l));
            intList.add(start);
            list.add(new Line(style, string3, lv.x, lv.y));
         });
         int[] is = intList.toIntArray();
         boolean bl = i == string.length();
         Position lv2;
         int l;
         if (bl && mutableBoolean.isTrue()) {
            int var10003 = list.size();
            Objects.requireNonNull(this.textRenderer);
            lv2 = new Position(0, var10003 * 9);
         } else {
            int k = getLineFromOffset(is, i);
            l = this.textRenderer.getWidth(string.substring(is[k], i));
            Objects.requireNonNull(this.textRenderer);
            lv2 = new Position(l, k * 9);
         }

         List list2 = Lists.newArrayList();
         if (i != j) {
            l = Math.min(i, j);
            int m = Math.max(i, j);
            int n = getLineFromOffset(is, l);
            int o = getLineFromOffset(is, m);
            int p;
            int q;
            if (n == o) {
               Objects.requireNonNull(this.textRenderer);
               p = n * 9;
               q = is[n];
               list2.add(this.getLineSelectionRectangle(string, lv, l, m, p, q));
            } else {
               p = n + 1 > is.length ? string.length() : is[n + 1];
               Objects.requireNonNull(this.textRenderer);
               list2.add(this.getLineSelectionRectangle(string, lv, l, p, n * 9, is[n]));

               for(q = n + 1; q < o; ++q) {
                  Objects.requireNonNull(this.textRenderer);
                  int r = q * 9;
                  String string2 = string.substring(is[q], is[q + 1]);
                  int s = (int)lv.getWidth(string2);
                  Position var10002 = new Position(0, r);
                  Objects.requireNonNull(this.textRenderer);
                  list2.add(this.getRectFromCorners(var10002, new Position(s, r + 9)));
               }

               int var10004 = is[o];
               Objects.requireNonNull(this.textRenderer);
               list2.add(this.getLineSelectionRectangle(string, lv, var10004, m, o * 9, is[o]));
            }
         }

         return new PageContent(string, lv2, bl, is, (Line[])list.toArray(new Line[0]), (Rect2i[])list2.toArray(new Rect2i[0]));
      }
   }

   static int getLineFromOffset(int[] lineStarts, int position) {
      int j = Arrays.binarySearch(lineStarts, position);
      return j < 0 ? -(j + 2) : j;
   }

   private Rect2i getLineSelectionRectangle(String string, TextHandler handler, int selectionStart, int selectionEnd, int lineY, int lineStart) {
      String string2 = string.substring(lineStart, selectionStart);
      String string3 = string.substring(lineStart, selectionEnd);
      Position lv = new Position((int)handler.getWidth(string2), lineY);
      int var10002 = (int)handler.getWidth(string3);
      Objects.requireNonNull(this.textRenderer);
      Position lv2 = new Position(var10002, lineY + 9);
      return this.getRectFromCorners(lv, lv2);
   }

   private Rect2i getRectFromCorners(Position start, Position end) {
      Position lv = this.absolutePositionToScreenPosition(start);
      Position lv2 = this.absolutePositionToScreenPosition(end);
      int i = Math.min(lv.x, lv2.x);
      int j = Math.max(lv.x, lv2.x);
      int k = Math.min(lv.y, lv2.y);
      int l = Math.max(lv.y, lv2.y);
      return new Rect2i(i, k, j - i, l - k);
   }

   static {
      BLACK_CURSOR_TEXT = OrderedText.styledForwardsVisitedString("_", Style.EMPTY.withColor(Formatting.BLACK));
      GRAY_CURSOR_TEXT = OrderedText.styledForwardsVisitedString("_", Style.EMPTY.withColor(Formatting.GRAY));
   }

   @Environment(EnvType.CLIENT)
   private static class PageContent {
      static final PageContent EMPTY;
      private final String pageContent;
      final Position position;
      final boolean atEnd;
      private final int[] lineStarts;
      final Line[] lines;
      final Rect2i[] selectionRectangles;

      public PageContent(String pageContent, Position position, boolean atEnd, int[] lineStarts, Line[] lines, Rect2i[] selectionRectangles) {
         this.pageContent = pageContent;
         this.position = position;
         this.atEnd = atEnd;
         this.lineStarts = lineStarts;
         this.lines = lines;
         this.selectionRectangles = selectionRectangles;
      }

      public int getCursorPosition(TextRenderer renderer, Position position) {
         int var10000 = position.y;
         Objects.requireNonNull(renderer);
         int i = var10000 / 9;
         if (i < 0) {
            return 0;
         } else if (i >= this.lines.length) {
            return this.pageContent.length();
         } else {
            Line lv = this.lines[i];
            return this.lineStarts[i] + renderer.getTextHandler().getTrimmedLength(lv.content, position.x, lv.style);
         }
      }

      public int getVerticalOffset(int position, int lines) {
         int k = BookEditScreen.getLineFromOffset(this.lineStarts, position);
         int l = k + lines;
         int o;
         if (0 <= l && l < this.lineStarts.length) {
            int m = position - this.lineStarts[k];
            int n = this.lines[l].content.length();
            o = this.lineStarts[l] + Math.min(m, n);
         } else {
            o = position;
         }

         return o;
      }

      public int getLineStart(int position) {
         int j = BookEditScreen.getLineFromOffset(this.lineStarts, position);
         return this.lineStarts[j];
      }

      public int getLineEnd(int position) {
         int j = BookEditScreen.getLineFromOffset(this.lineStarts, position);
         return this.lineStarts[j] + this.lines[j].content.length();
      }

      static {
         EMPTY = new PageContent("", new Position(0, 0), true, new int[]{0}, new Line[]{new Line(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
      }
   }

   @Environment(EnvType.CLIENT)
   private static class Line {
      final Style style;
      final String content;
      final Text text;
      final int x;
      final int y;

      public Line(Style style, String content, int x, int y) {
         this.style = style;
         this.content = content;
         this.x = x;
         this.y = y;
         this.text = Text.literal(content).setStyle(style);
      }
   }

   @Environment(EnvType.CLIENT)
   static class Position {
      public final int x;
      public final int y;

      Position(int x, int y) {
         this.x = x;
         this.y = y;
      }
   }
}
