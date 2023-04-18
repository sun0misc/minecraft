package net.minecraft.client.gui.widget;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

@Environment(EnvType.CLIENT)
public class ThreePartsLayoutWidget implements LayoutWidget {
   private static final int DEFAULT_HEADER_FOOTER_HEIGHT = 36;
   private static final int FOOTER_MARGIN_TOP = 30;
   private final SimplePositioningWidget header;
   private final SimplePositioningWidget body;
   private final SimplePositioningWidget footer;
   private final Screen screen;
   private int headerHeight;
   private int footerHeight;

   public ThreePartsLayoutWidget(Screen screen) {
      this(screen, 36);
   }

   public ThreePartsLayoutWidget(Screen screen, int headerFooterHeight) {
      this(screen, headerFooterHeight, headerFooterHeight);
   }

   public ThreePartsLayoutWidget(Screen screen, int headerHeight, int footerHeight) {
      this.header = new SimplePositioningWidget();
      this.body = new SimplePositioningWidget();
      this.footer = new SimplePositioningWidget();
      this.screen = screen;
      this.headerHeight = headerHeight;
      this.footerHeight = footerHeight;
      this.header.getMainPositioner().relative(0.5F, 0.5F);
      this.body.getMainPositioner().relative(0.5F, 0.5F);
      this.footer.getMainPositioner().relative(0.5F, 0.0F).marginTop(30);
   }

   public void setX(int x) {
   }

   public void setY(int y) {
   }

   public int getX() {
      return 0;
   }

   public int getY() {
      return 0;
   }

   public int getWidth() {
      return this.screen.width;
   }

   public int getHeight() {
      return this.screen.height;
   }

   public int getFooterHeight() {
      return this.footerHeight;
   }

   public void setFooterHeight(int footerHeight) {
      this.footerHeight = footerHeight;
   }

   public void setHeaderHeight(int headerHeight) {
      this.headerHeight = headerHeight;
   }

   public int getHeaderHeight() {
      return this.headerHeight;
   }

   public void forEachElement(Consumer consumer) {
      this.header.forEachElement(consumer);
      this.footer.forEachElement(consumer);
      this.body.forEachElement(consumer);
   }

   public void refreshPositions() {
      int i = this.getHeaderHeight();
      int j = this.getFooterHeight();
      this.header.setMinWidth(this.screen.width);
      this.header.setMinHeight(i);
      this.header.setPosition(0, 0);
      this.header.refreshPositions();
      this.body.setMinWidth(this.screen.width);
      this.body.setMinHeight(j);
      this.body.refreshPositions();
      this.body.setY(this.screen.height - j);
      this.footer.setMinWidth(this.screen.width);
      this.footer.setMinHeight(this.screen.height - i - j);
      this.footer.setPosition(0, i);
      this.footer.refreshPositions();
   }

   public Widget addHeader(Widget widget) {
      return this.header.add(widget);
   }

   public Widget addHeader(Widget widget, Positioner positioner) {
      return this.header.add(widget, positioner);
   }

   public Widget addBody(Widget widget) {
      return this.body.add(widget);
   }

   public Widget addBody(Widget widget, Positioner positioner) {
      return this.body.add(widget, positioner);
   }

   public Widget addFooter(Widget widget) {
      return this.footer.add(widget);
   }

   public Widget addFooter(Widget widget, Positioner positioner) {
      return this.footer.add(widget, positioner);
   }

   public Positioner copyHeaderPositioner() {
      return this.header.copyPositioner();
   }

   public Positioner copyFooterPositioner() {
      return this.footer.copyPositioner();
   }

   public Positioner copyBodyPositioner() {
      return this.body.copyPositioner();
   }
}
