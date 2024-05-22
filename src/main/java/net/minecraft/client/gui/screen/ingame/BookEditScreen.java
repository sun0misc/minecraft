/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BookEditScreen
extends Screen {
    private static final int MAX_TEXT_WIDTH = 114;
    private static final int MAX_TEXT_HEIGHT = 128;
    private static final int WIDTH = 192;
    private static final int HEIGHT = 192;
    private static final Text EDIT_TITLE_TEXT = Text.translatable("book.editTitle");
    private static final Text FINALIZE_WARNING_TEXT = Text.translatable("book.finalizeWarning");
    private static final OrderedText BLACK_CURSOR_TEXT = OrderedText.styledForwardsVisitedString("_", Style.EMPTY.withColor(Formatting.BLACK));
    private static final OrderedText GRAY_CURSOR_TEXT = OrderedText.styledForwardsVisitedString("_", Style.EMPTY.withColor(Formatting.GRAY));
    private final PlayerEntity player;
    private final ItemStack itemStack;
    private boolean dirty;
    private boolean signing;
    private int tickCounter;
    private int currentPage;
    private final List<String> pages = Lists.newArrayList();
    private String title = "";
    private final SelectionManager currentPageSelectionManager = new SelectionManager(this::getCurrentPageContent, this::setPageContent, this::getClipboard, this::setClipboard, string -> string.length() < 1024 && this.textRenderer.getWrappedLinesHeight((String)string, 114) <= 128);
    private final SelectionManager bookTitleSelectionManager = new SelectionManager(() -> this.title, title -> {
        this.title = title;
    }, this::getClipboard, this::setClipboard, string -> string.length() < 16);
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
    private PageContent pageContent = PageContent.EMPTY;
    private Text pageIndicatorText = ScreenTexts.EMPTY;
    private final Text signedByText;

    public BookEditScreen(PlayerEntity player, ItemStack itemStack, Hand hand) {
        super(NarratorManager.EMPTY);
        this.player = player;
        this.itemStack = itemStack;
        this.hand = hand;
        WritableBookContentComponent lv = itemStack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
        if (lv != null) {
            lv.stream(MinecraftClient.getInstance().shouldFilterText()).forEach(this.pages::add);
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

    @Override
    public void tick() {
        super.tick();
        ++this.tickCounter;
    }

    @Override
    protected void init() {
        this.invalidatePageContent();
        this.signButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("book.signButton"), button -> {
            this.signing = true;
            this.updateButtons();
        }).dimensions(this.width / 2 - 100, 196, 98, 20).build());
        this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            this.client.setScreen(null);
            this.finalizeBook(false);
        }).dimensions(this.width / 2 + 2, 196, 98, 20).build());
        this.finalizeButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("book.finalizeButton"), button -> {
            if (this.signing) {
                this.finalizeBook(true);
                this.client.setScreen(null);
            }
        }).dimensions(this.width / 2 - 100, 196, 98, 20).build());
        this.cancelButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            if (this.signing) {
                this.signing = false;
            }
            this.updateButtons();
        }).dimensions(this.width / 2 + 2, 196, 98, 20).build());
        int i = (this.width - 192) / 2;
        int j = 2;
        this.nextPageButton = this.addDrawableChild(new PageTurnWidget(i + 116, 159, true, button -> this.openNextPage(), true));
        this.previousPageButton = this.addDrawableChild(new PageTurnWidget(i + 43, 159, false, button -> this.openPreviousPage(), true));
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
        this.finalizeButton.active = !StringHelper.isBlank(this.title);
    }

    private void removeEmptyPages() {
        ListIterator<String> listIterator = this.pages.listIterator(this.pages.size());
        while (listIterator.hasPrevious() && listIterator.previous().isEmpty()) {
            listIterator.remove();
        }
    }

    private void finalizeBook(boolean signBook) {
        if (!this.dirty) {
            return;
        }
        this.removeEmptyPages();
        this.writeNbtData();
        int i = this.hand == Hand.MAIN_HAND ? this.player.getInventory().selectedSlot : 40;
        this.client.getNetworkHandler().sendPacket(new BookUpdateC2SPacket(i, this.pages, signBook ? Optional.of(this.title.trim()) : Optional.empty()));
    }

    private void writeNbtData() {
        this.itemStack.set(DataComponentTypes.WRITABLE_BOOK_CONTENT, new WritableBookContentComponent(this.pages.stream().map(RawFilteredPair::of).toList()));
    }

    private void appendNewPage() {
        if (this.countPages() >= 100) {
            return;
        }
        this.pages.add("");
        this.dirty = true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.signing) {
            return this.keyPressedSignMode(keyCode, scanCode, modifiers);
        }
        boolean bl = this.keyPressedEditMode(keyCode, scanCode, modifiers);
        if (bl) {
            this.invalidatePageContent();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (super.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.signing) {
            boolean bl = this.bookTitleSelectionManager.insert(chr);
            if (bl) {
                this.updateButtons();
                this.dirty = true;
                return true;
            }
            return false;
        }
        if (StringHelper.isValidChar(chr)) {
            this.currentPageSelectionManager.insert(Character.toString(chr));
            this.invalidatePageContent();
            return true;
        }
        return false;
    }

    private boolean keyPressedEditMode(int keyCode, int scanCode, int modifiers) {
        if (Screen.isSelectAll(keyCode)) {
            this.currentPageSelectionManager.selectAll();
            return true;
        }
        if (Screen.isCopy(keyCode)) {
            this.currentPageSelectionManager.copy();
            return true;
        }
        if (Screen.isPaste(keyCode)) {
            this.currentPageSelectionManager.paste();
            return true;
        }
        if (Screen.isCut(keyCode)) {
            this.currentPageSelectionManager.cut();
            return true;
        }
        SelectionManager.SelectionType lv = Screen.hasControlDown() ? SelectionManager.SelectionType.WORD : SelectionManager.SelectionType.CHARACTER;
        switch (keyCode) {
            case 259: {
                this.currentPageSelectionManager.delete(-1, lv);
                return true;
            }
            case 261: {
                this.currentPageSelectionManager.delete(1, lv);
                return true;
            }
            case 257: 
            case 335: {
                this.currentPageSelectionManager.insert("\n");
                return true;
            }
            case 263: {
                this.currentPageSelectionManager.moveCursor(-1, Screen.hasShiftDown(), lv);
                return true;
            }
            case 262: {
                this.currentPageSelectionManager.moveCursor(1, Screen.hasShiftDown(), lv);
                return true;
            }
            case 265: {
                this.moveUpLine();
                return true;
            }
            case 264: {
                this.moveDownLine();
                return true;
            }
            case 266: {
                this.previousPageButton.onPress();
                return true;
            }
            case 267: {
                this.nextPageButton.onPress();
                return true;
            }
            case 268: {
                this.moveToLineStart();
                return true;
            }
            case 269: {
                this.moveToLineEnd();
                return true;
            }
        }
        return false;
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
            case 259: {
                this.bookTitleSelectionManager.delete(-1);
                this.updateButtons();
                this.dirty = true;
                return true;
            }
            case 257: 
            case 335: {
                if (!this.title.isEmpty()) {
                    this.finalizeBook(true);
                    this.client.setScreen(null);
                }
                return true;
            }
        }
        return false;
    }

    private String getCurrentPageContent() {
        if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
            return this.pages.get(this.currentPage);
        }
        return "";
    }

    private void setPageContent(String newContent) {
        if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
            this.pages.set(this.currentPage, newContent);
            this.dirty = true;
            this.invalidatePageContent();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.setFocused(null);
        int k = (this.width - 192) / 2;
        int l = 2;
        if (this.signing) {
            boolean bl = this.tickCounter / 6 % 2 == 0;
            OrderedText lv = OrderedText.concat(OrderedText.styledForwardsVisitedString(this.title, Style.EMPTY), bl ? BLACK_CURSOR_TEXT : GRAY_CURSOR_TEXT);
            int m = this.textRenderer.getWidth(EDIT_TITLE_TEXT);
            context.drawText(this.textRenderer, EDIT_TITLE_TEXT, k + 36 + (114 - m) / 2, 34, 0, false);
            int n = this.textRenderer.getWidth(lv);
            context.drawText(this.textRenderer, lv, k + 36 + (114 - n) / 2, 50, 0, false);
            int o = this.textRenderer.getWidth(this.signedByText);
            context.drawText(this.textRenderer, this.signedByText, k + 36 + (114 - o) / 2, 60, 0, false);
            context.drawTextWrapped(this.textRenderer, FINALIZE_WARNING_TEXT, k + 36, 82, 114, 0);
        } else {
            int p = this.textRenderer.getWidth(this.pageIndicatorText);
            context.drawText(this.textRenderer, this.pageIndicatorText, k - p + 192 - 44, 18, 0, false);
            PageContent lv2 = this.getPageContent();
            for (Line lv3 : lv2.lines) {
                context.drawText(this.textRenderer, lv3.text, lv3.x, lv3.y, Colors.BLACK, false);
            }
            this.drawSelection(context, lv2.selectionRectangles);
            this.drawCursor(context, lv2.position, lv2.atEnd);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
        context.drawTexture(BookScreen.BOOK_TEXTURE, (this.width - 192) / 2, 2, 0, 0, 192, 192);
    }

    private void drawCursor(DrawContext context, Position position, boolean atEnd) {
        if (this.tickCounter / 6 % 2 == 0) {
            position = this.absolutePositionToScreenPosition(position);
            if (!atEnd) {
                context.fill(position.x, position.y - 1, position.x + 1, position.y + this.textRenderer.fontHeight, Colors.BLACK);
            } else {
                context.drawText(this.textRenderer, "_", position.x, position.y, 0, false);
            }
        }
    }

    private void drawSelection(DrawContext context, Rect2i[] selectionRectangles) {
        for (Rect2i lv : selectionRectangles) {
            int i = lv.getX();
            int j = lv.getY();
            int k = i + lv.getWidth();
            int l = j + lv.getHeight();
            context.fill(RenderLayer.getGuiTextHighlight(), i, j, k, l, -16776961);
        }
    }

    private Position screenPositionToAbsolutePosition(Position position) {
        return new Position(position.x - (this.width - 192) / 2 - 36, position.y - 32);
    }

    private Position absolutePositionToScreenPosition(Position position) {
        return new Position(position.x + (this.width - 192) / 2 + 36, position.y + 32);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
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

    private void selectCurrentWord(int cursor) {
        String string = this.getCurrentPageContent();
        this.currentPageSelectionManager.setSelection(TextHandler.moveCursorByWords(string, -1, cursor, false), TextHandler.moveCursorByWords(string, 1, cursor, false));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (button == 0) {
            PageContent lv = this.getPageContent();
            int j = lv.getCursorPosition(this.textRenderer, this.screenPositionToAbsolutePosition(new Position((int)mouseX, (int)mouseY)));
            this.currentPageSelectionManager.moveCursorTo(j, true);
            this.invalidatePageContent();
        }
        return true;
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
        int l;
        Position lv2;
        boolean bl;
        String string = this.getCurrentPageContent();
        if (string.isEmpty()) {
            return PageContent.EMPTY;
        }
        int i = this.currentPageSelectionManager.getSelectionStart();
        int j = this.currentPageSelectionManager.getSelectionEnd();
        IntArrayList intList = new IntArrayList();
        ArrayList list = Lists.newArrayList();
        MutableInt mutableInt = new MutableInt();
        MutableBoolean mutableBoolean = new MutableBoolean();
        TextHandler lv = this.textRenderer.getTextHandler();
        lv.wrapLines(string, 114, Style.EMPTY, true, (style, start, end) -> {
            int k = mutableInt.getAndIncrement();
            String string2 = string.substring(start, end);
            mutableBoolean.setValue(string2.endsWith("\n"));
            String string3 = StringUtils.stripEnd(string2, " \n");
            int l = k * this.textRenderer.fontHeight;
            Position lv = this.absolutePositionToScreenPosition(new Position(0, l));
            intList.add(start);
            list.add(new Line(style, string3, lv.x, lv.y));
        });
        int[] is = intList.toIntArray();
        boolean bl2 = bl = i == string.length();
        if (bl && mutableBoolean.isTrue()) {
            lv2 = new Position(0, list.size() * this.textRenderer.fontHeight);
        } else {
            int k = BookEditScreen.getLineFromOffset(is, i);
            l = this.textRenderer.getWidth(string.substring(is[k], i));
            lv2 = new Position(l, k * this.textRenderer.fontHeight);
        }
        ArrayList<Rect2i> list2 = Lists.newArrayList();
        if (i != j) {
            int o;
            l = Math.min(i, j);
            int m = Math.max(i, j);
            int n = BookEditScreen.getLineFromOffset(is, l);
            if (n == (o = BookEditScreen.getLineFromOffset(is, m))) {
                int p = n * this.textRenderer.fontHeight;
                int q = is[n];
                list2.add(this.getLineSelectionRectangle(string, lv, l, m, p, q));
            } else {
                int p = n + 1 > is.length ? string.length() : is[n + 1];
                list2.add(this.getLineSelectionRectangle(string, lv, l, p, n * this.textRenderer.fontHeight, is[n]));
                for (int q = n + 1; q < o; ++q) {
                    int r = q * this.textRenderer.fontHeight;
                    String string2 = string.substring(is[q], is[q + 1]);
                    int s = (int)lv.getWidth(string2);
                    list2.add(this.getRectFromCorners(new Position(0, r), new Position(s, r + this.textRenderer.fontHeight)));
                }
                list2.add(this.getLineSelectionRectangle(string, lv, is[o], m, o * this.textRenderer.fontHeight, is[o]));
            }
        }
        return new PageContent(string, lv2, bl, is, list.toArray(new Line[0]), list2.toArray(new Rect2i[0]));
    }

    static int getLineFromOffset(int[] lineStarts, int position) {
        int j = Arrays.binarySearch(lineStarts, position);
        if (j < 0) {
            return -(j + 2);
        }
        return j;
    }

    private Rect2i getLineSelectionRectangle(String string, TextHandler handler, int selectionStart, int selectionEnd, int lineY, int lineStart) {
        String string2 = string.substring(lineStart, selectionStart);
        String string3 = string.substring(lineStart, selectionEnd);
        Position lv = new Position((int)handler.getWidth(string2), lineY);
        Position lv2 = new Position((int)handler.getWidth(string3), lineY + this.textRenderer.fontHeight);
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

    @Environment(value=EnvType.CLIENT)
    static class PageContent {
        static final PageContent EMPTY = new PageContent("", new Position(0, 0), true, new int[]{0}, new Line[]{new Line(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
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
            int i = position.y / renderer.fontHeight;
            if (i < 0) {
                return 0;
            }
            if (i >= this.lines.length) {
                return this.pageContent.length();
            }
            Line lv = this.lines[i];
            return this.lineStarts[i] + renderer.getTextHandler().getTrimmedLength(lv.content, position.x, lv.style);
        }

        public int getVerticalOffset(int position, int lines) {
            int o;
            int k = BookEditScreen.getLineFromOffset(this.lineStarts, position);
            int l = k + lines;
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
    }

    @Environment(value=EnvType.CLIENT)
    static class Line {
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

    @Environment(value=EnvType.CLIENT)
    static class Position {
        public final int x;
        public final int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}

