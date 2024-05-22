/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class EntryListWidget<E extends Entry<E>>
extends ContainerWidget {
    protected static final int field_45909 = 6;
    private static final Identifier SCROLLER_TEXTURE = Identifier.method_60656("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_TEXTURE = Identifier.method_60656("widget/scroller_background");
    private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = Identifier.method_60656("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND_TEXTURE = Identifier.method_60656("textures/gui/inworld_menu_list_background.png");
    protected final MinecraftClient client;
    protected final int itemHeight;
    private final List<E> children = new Entries();
    protected boolean centerListVertically = true;
    private double scrollAmount;
    private boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;
    @Nullable
    private E selected;
    @Nullable
    private E hoveredEntry;

    public EntryListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(0, y, width, height, ScreenTexts.EMPTY);
        this.client = client;
        this.itemHeight = itemHeight;
    }

    protected void setRenderHeader(boolean renderHeader, int headerHeight) {
        this.renderHeader = renderHeader;
        this.headerHeight = headerHeight;
        if (!renderHeader) {
            this.headerHeight = 0;
        }
    }

    public int getRowWidth() {
        return 220;
    }

    @Nullable
    public E getSelectedOrNull() {
        return this.selected;
    }

    public void setSelected(@Nullable E entry) {
        this.selected = entry;
    }

    public E getFirst() {
        return (E)((Entry)this.children.get(0));
    }

    @Nullable
    public E getFocused() {
        return (E)((Entry)super.getFocused());
    }

    public final List<E> children() {
        return this.children;
    }

    protected void clearEntries() {
        this.children.clear();
        this.selected = null;
    }

    protected void replaceEntries(Collection<E> newEntries) {
        this.clearEntries();
        this.children.addAll(newEntries);
    }

    protected E getEntry(int index) {
        return (E)((Entry)this.children().get(index));
    }

    protected int addEntry(E entry) {
        this.children.add(entry);
        return this.children.size() - 1;
    }

    protected void addEntryToTop(E entry) {
        double d = (double)this.getMaxScroll() - this.getScrollAmount();
        this.children.add(0, entry);
        this.setScrollAmount((double)this.getMaxScroll() - d);
    }

    protected boolean removeEntryWithoutScrolling(E entry) {
        double d = (double)this.getMaxScroll() - this.getScrollAmount();
        boolean bl = this.removeEntry(entry);
        this.setScrollAmount((double)this.getMaxScroll() - d);
        return bl;
    }

    protected int getEntryCount() {
        return this.children().size();
    }

    protected boolean isSelectedEntry(int index) {
        return Objects.equals(this.getSelectedOrNull(), this.children().get(index));
    }

    @Nullable
    protected final E getEntryAtPosition(double x, double y) {
        int i = this.getRowWidth() / 2;
        int j = this.getX() + this.width / 2;
        int k = j - i;
        int l = j + i;
        int m = MathHelper.floor(y - (double)this.getY()) - this.headerHeight + (int)this.getScrollAmount() - 4;
        int n = m / this.itemHeight;
        if (x >= (double)k && x <= (double)l && n >= 0 && m >= 0 && n < this.getEntryCount()) {
            return (E)((Entry)this.children().get(n));
        }
        return null;
    }

    public void position(int width, ThreePartsLayoutWidget layout) {
        this.position(width, layout.getContentHeight(), layout.getHeaderHeight());
    }

    public void position(int width, int height, int y) {
        this.setDimensions(width, height);
        this.setPosition(0, y);
        this.refreshScroll();
    }

    protected int getMaxPosition() {
        return this.getEntryCount() * this.itemHeight + this.headerHeight;
    }

    protected boolean clickedHeader(int x, int y) {
        return false;
    }

    protected void renderHeader(DrawContext context, int x, int y) {
    }

    protected void renderDecorations(DrawContext context, int mouseX, int mouseY) {
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int l;
        int k;
        this.hoveredEntry = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;
        this.drawMenuListBackground(context);
        this.enableScissor(context);
        if (this.renderHeader) {
            k = this.getRowLeft();
            l = this.getY() + 4 - (int)this.getScrollAmount();
            this.renderHeader(context, k, l);
        }
        this.renderList(context, mouseX, mouseY, delta);
        context.disableScissor();
        this.drawHeaderAndFooterSeparators(context);
        if (this.isScrollbarVisible()) {
            k = this.getScrollbarX();
            l = (int)((float)(this.height * this.height) / (float)this.getMaxPosition());
            l = MathHelper.clamp(l, 32, this.height - 8);
            int m = (int)this.getScrollAmount() * (this.height - l) / this.getMaxScroll() + this.getY();
            if (m < this.getY()) {
                m = this.getY();
            }
            RenderSystem.enableBlend();
            context.drawGuiTexture(SCROLLER_BACKGROUND_TEXTURE, k, this.getY(), 6, this.getHeight());
            context.drawGuiTexture(SCROLLER_TEXTURE, k, m, 6, l);
            RenderSystem.disableBlend();
        }
        this.renderDecorations(context, mouseX, mouseY);
        RenderSystem.disableBlend();
    }

    protected boolean isScrollbarVisible() {
        return this.getMaxScroll() > 0;
    }

    protected void drawHeaderAndFooterSeparators(DrawContext context) {
        RenderSystem.enableBlend();
        Identifier lv = this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
        Identifier lv2 = this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
        context.drawTexture(lv, this.getX(), this.getY() - 2, 0.0f, 0.0f, this.getWidth(), 2, 32, 2);
        context.drawTexture(lv2, this.getX(), this.getBottom(), 0.0f, 0.0f, this.getWidth(), 2, 32, 2);
        RenderSystem.disableBlend();
    }

    protected void drawMenuListBackground(DrawContext context) {
        RenderSystem.enableBlend();
        Identifier lv = this.client.world == null ? MENU_LIST_BACKGROUND_TEXTURE : INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
        context.drawTexture(lv, this.getX(), this.getY(), this.getRight(), this.getBottom() + (int)this.getScrollAmount(), this.getWidth(), this.getHeight(), 32, 32);
        RenderSystem.disableBlend();
    }

    protected void enableScissor(DrawContext context) {
        context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
    }

    protected void centerScrollOn(E entry) {
        this.setScrollAmount(this.children().indexOf(entry) * this.itemHeight + this.itemHeight / 2 - this.height / 2);
    }

    protected void ensureVisible(E entry) {
        int k;
        int i = this.getRowTop(this.children().indexOf(entry));
        int j = i - this.getY() - 4 - this.itemHeight;
        if (j < 0) {
            this.scroll(j);
        }
        if ((k = this.getBottom() - i - this.itemHeight - this.itemHeight) < 0) {
            this.scroll(-k);
        }
    }

    private void scroll(int amount) {
        this.setScrollAmount(this.getScrollAmount() + (double)amount);
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmountOnly(double amount) {
        this.scrollAmount = MathHelper.clamp(amount, 0.0, (double)this.getMaxScroll());
    }

    public void setScrollAmount(double amount) {
        this.setScrollAmountOnly(amount);
    }

    public void refreshScroll() {
        this.setScrollAmountOnly(this.getScrollAmount());
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.height - 4));
    }

    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarX() && mouseX < (double)(this.getScrollbarX() + 6);
    }

    protected int getScrollbarX() {
        return this.getDefaultScrollbarX();
    }

    protected int getDefaultScrollbarX() {
        return this.getBorderBoxRight() + this.getScrollbarMarginX();
    }

    private int getScrollbarMarginX() {
        return 10;
    }

    protected boolean isSelectButton(int button) {
        return button == 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isSelectButton(button)) {
            return false;
        }
        this.updateScrollingState(mouseX, mouseY, button);
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        E lv = this.getEntryAtPosition(mouseX, mouseY);
        if (lv != null) {
            if (lv.mouseClicked(mouseX, mouseY, button)) {
                Element lv2 = this.getFocused();
                if (lv2 != lv && lv2 instanceof ParentElement) {
                    ParentElement lv3 = (ParentElement)lv2;
                    lv3.setFocused(null);
                }
                this.setFocused((Element)lv);
                this.setDragging(true);
                return true;
            }
        } else if (this.clickedHeader((int)(mouseX - (double)(this.getX() + this.width / 2 - this.getRowWidth() / 2)), (int)(mouseY - (double)this.getY()) + (int)this.getScrollAmount() - 4)) {
            return true;
        }
        return this.scrolling;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.getFocused() != null) {
            return this.getFocused().mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (button != 0 || !this.scrolling) {
            return false;
        }
        if (mouseY < (double)this.getY()) {
            this.setScrollAmount(0.0);
        } else if (mouseY > (double)this.getBottom()) {
            this.setScrollAmount(this.getMaxScroll());
        } else {
            double h = Math.max(1, this.getMaxScroll());
            int j = this.height;
            int k = MathHelper.clamp((int)((float)(j * j) / (float)this.getMaxPosition()), 32, j - 8);
            double l = Math.max(1.0, h / (double)(j - k));
            this.setScrollAmount(this.getScrollAmount() + deltaY * l);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.setScrollAmount(this.getScrollAmount() - verticalAmount * (double)this.itemHeight / 2.0);
        return true;
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        super.setFocused(focused);
        int i = this.children.indexOf(focused);
        if (i >= 0) {
            Entry lv = (Entry)this.children.get(i);
            this.setSelected(lv);
            if (this.client.getNavigationType().isKeyboard()) {
                this.ensureVisible(lv);
            }
        }
    }

    @Nullable
    protected E getNeighboringEntry(NavigationDirection direction) {
        return (E)this.getNeighboringEntry(direction, entry -> true);
    }

    @Nullable
    protected E getNeighboringEntry(NavigationDirection direction, Predicate<E> predicate) {
        return this.getNeighboringEntry(direction, predicate, this.getSelectedOrNull());
    }

    @Nullable
    protected E getNeighboringEntry(NavigationDirection direction, Predicate<E> predicate, @Nullable E selected) {
        int i;
        switch (direction) {
            default: {
                throw new MatchException(null, null);
            }
            case RIGHT: 
            case LEFT: {
                int n = 0;
                break;
            }
            case UP: {
                int n = -1;
                break;
            }
            case DOWN: {
                int n = i = 1;
            }
        }
        if (!this.children().isEmpty() && i != 0) {
            int j = selected == null ? (i > 0 ? 0 : this.children().size() - 1) : this.children().indexOf(selected) + i;
            for (int k = j; k >= 0 && k < this.children.size(); k += i) {
                Entry lv = (Entry)this.children().get(k);
                if (!predicate.test(lv)) continue;
                return (E)lv;
            }
        }
        return null;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= (double)this.getY() && mouseY <= (double)this.getBottom() && mouseX >= (double)this.getX() && mouseX <= (double)this.getRight();
    }

    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        int k = this.getRowLeft();
        int l = this.getRowWidth();
        int m = this.itemHeight - 4;
        int n = this.getEntryCount();
        for (int o = 0; o < n; ++o) {
            int p = this.getRowTop(o);
            int q = this.getRowBottom(o);
            if (q < this.getY() || p > this.getBottom()) continue;
            this.renderEntry(context, mouseX, mouseY, delta, o, k, p, l, m);
        }
    }

    protected void renderEntry(DrawContext context, int mouseX, int mouseY, float delta, int index, int x, int y, int entryWidth, int entryHeight) {
        E lv = this.getEntry(index);
        ((Entry)lv).drawBorder(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, Objects.equals(this.hoveredEntry, lv), delta);
        if (this.isSelectedEntry(index)) {
            int p = this.isFocused() ? -1 : -8355712;
            this.drawSelectionHighlight(context, y, entryWidth, entryHeight, p, -16777216);
        }
        ((Entry)lv).render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, Objects.equals(this.hoveredEntry, lv), delta);
    }

    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        int n = this.getX() + (this.width - entryWidth) / 2;
        int o = this.getX() + (this.width + entryWidth) / 2;
        context.fill(n, y - 2, o, y + entryHeight + 2, borderColor);
        context.fill(n + 1, y - 1, o - 1, y + entryHeight + 1, fillColor);
    }

    public int getRowLeft() {
        return this.getX() + this.width / 2 - this.getRowWidth() / 2 + 2;
    }

    private int getBorderBoxLeft() {
        return this.getX() + this.width / 2 - this.getRowWidth() / 2;
    }

    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth();
    }

    private int getBorderBoxRight() {
        return this.getBorderBoxLeft() + this.getRowWidth();
    }

    protected int getRowTop(int index) {
        return this.getY() + 4 - (int)this.getScrollAmount() + index * this.itemHeight + this.headerHeight;
    }

    protected int getRowBottom(int index) {
        return this.getRowTop(index) + this.itemHeight;
    }

    @Override
    public Selectable.SelectionType getType() {
        if (this.isFocused()) {
            return Selectable.SelectionType.FOCUSED;
        }
        if (this.hoveredEntry != null) {
            return Selectable.SelectionType.HOVERED;
        }
        return Selectable.SelectionType.NONE;
    }

    @Nullable
    protected E remove(int index) {
        Entry lv = (Entry)this.children.get(index);
        if (this.removeEntry((Entry)this.children.get(index))) {
            return (E)lv;
        }
        return null;
    }

    protected boolean removeEntry(E entry) {
        boolean bl = this.children.remove(entry);
        if (bl && entry == this.getSelectedOrNull()) {
            this.setSelected(null);
        }
        return bl;
    }

    @Nullable
    protected E getHoveredEntry() {
        return this.hoveredEntry;
    }

    void setEntryParentList(Entry<E> entry) {
        entry.parentList = this;
    }

    protected void appendNarrations(NarrationMessageBuilder builder, E entry) {
        int i;
        List<E> list = this.children();
        if (list.size() > 1 && (i = list.indexOf(entry)) != -1) {
            builder.put(NarrationPart.POSITION, (Text)Text.translatable("narrator.position.list", i + 1, list.size()));
        }
    }

    @Override
    @Nullable
    public /* synthetic */ Element getFocused() {
        return this.getFocused();
    }

    @Environment(value=EnvType.CLIENT)
    class Entries
    extends AbstractList<E> {
        private final List<E> entries = Lists.newArrayList();

        Entries() {
        }

        @Override
        public E get(int i) {
            return (Entry)this.entries.get(i);
        }

        @Override
        public int size() {
            return this.entries.size();
        }

        @Override
        public E set(int i, E arg) {
            Entry lv = (Entry)this.entries.set(i, arg);
            EntryListWidget.this.setEntryParentList(arg);
            return lv;
        }

        @Override
        public void add(int i, E arg) {
            this.entries.add(i, arg);
            EntryListWidget.this.setEntryParentList(arg);
        }

        @Override
        public E remove(int i) {
            return (Entry)this.entries.remove(i);
        }

        @Override
        public /* synthetic */ Object remove(int index) {
            return this.remove(index);
        }

        @Override
        public /* synthetic */ void add(int index, Object entry) {
            this.add(index, (E)((Entry)entry));
        }

        @Override
        public /* synthetic */ Object set(int index, Object entry) {
            return this.set(index, (E)((Entry)entry));
        }

        @Override
        public /* synthetic */ Object get(int index) {
            return this.get(index);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static abstract class Entry<E extends Entry<E>>
    implements Element {
        @Deprecated
        EntryListWidget<E> parentList;

        protected Entry() {
        }

        @Override
        public void setFocused(boolean focused) {
        }

        @Override
        public boolean isFocused() {
            return this.parentList.getFocused() == this;
        }

        public abstract void render(DrawContext var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10);

        public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(this.parentList.getEntryAtPosition(mouseX, mouseY), this);
        }
    }
}

