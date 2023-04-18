package net.minecraft.client.gui;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

@Environment(EnvType.CLIENT)
public interface ParentElement extends Element {
   List children();

   default Optional hoveredElement(double mouseX, double mouseY) {
      Iterator var5 = this.children().iterator();

      Element lv;
      do {
         if (!var5.hasNext()) {
            return Optional.empty();
         }

         lv = (Element)var5.next();
      } while(!lv.isMouseOver(mouseX, mouseY));

      return Optional.of(lv);
   }

   default boolean mouseClicked(double mouseX, double mouseY, int button) {
      Iterator var6 = this.children().iterator();

      Element lv;
      do {
         if (!var6.hasNext()) {
            return false;
         }

         lv = (Element)var6.next();
      } while(!lv.mouseClicked(mouseX, mouseY, button));

      this.setFocused(lv);
      if (button == 0) {
         this.setDragging(true);
      }

      return true;
   }

   default boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.setDragging(false);
      return this.hoveredElement(mouseX, mouseY).filter((element) -> {
         return element.mouseReleased(mouseX, mouseY, button);
      }).isPresent();
   }

   default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.getFocused() != null && this.isDragging() && button == 0 ? this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY) : false;
   }

   boolean isDragging();

   void setDragging(boolean dragging);

   default boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.hoveredElement(mouseX, mouseY).filter((element) -> {
         return element.mouseScrolled(mouseX, mouseY, amount);
      }).isPresent();
   }

   default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers);
   }

   default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      return this.getFocused() != null && this.getFocused().keyReleased(keyCode, scanCode, modifiers);
   }

   default boolean charTyped(char chr, int modifiers) {
      return this.getFocused() != null && this.getFocused().charTyped(chr, modifiers);
   }

   @Nullable
   Element getFocused();

   void setFocused(@Nullable Element focused);

   default void setFocused(boolean focused) {
   }

   default boolean isFocused() {
      return this.getFocused() != null;
   }

   @Nullable
   default GuiNavigationPath getFocusedPath() {
      Element lv = this.getFocused();
      return lv != null ? GuiNavigationPath.of(this, lv.getFocusedPath()) : null;
   }

   default void focusOn(@Nullable Element element) {
      this.setFocused(element);
   }

   @Nullable
   default GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
      Element lv = this.getFocused();
      if (lv != null) {
         GuiNavigationPath lv2 = lv.getNavigationPath(navigation);
         if (lv2 != null) {
            return GuiNavigationPath.of(this, lv2);
         }
      }

      if (navigation instanceof GuiNavigation.Tab lv3) {
         return this.computeNavigationPath(lv3);
      } else if (navigation instanceof GuiNavigation.Arrow lv4) {
         return this.computeNavigationPath(lv4);
      } else {
         return null;
      }
   }

   @Nullable
   private GuiNavigationPath computeNavigationPath(GuiNavigation.Tab navigation) {
      boolean bl = navigation.forward();
      Element lv = this.getFocused();
      List list = new ArrayList(this.children());
      Collections.sort(list, Comparator.comparingInt((arg) -> {
         return arg.getNavigationOrder();
      }));
      int i = list.indexOf(lv);
      int j;
      if (lv != null && i >= 0) {
         j = i + (bl ? 1 : 0);
      } else if (bl) {
         j = 0;
      } else {
         j = list.size();
      }

      ListIterator listIterator = list.listIterator(j);
      BooleanSupplier var10000;
      if (bl) {
         Objects.requireNonNull(listIterator);
         var10000 = listIterator::hasNext;
      } else {
         Objects.requireNonNull(listIterator);
         var10000 = listIterator::hasPrevious;
      }

      BooleanSupplier booleanSupplier = var10000;
      Supplier var12;
      if (bl) {
         Objects.requireNonNull(listIterator);
         var12 = listIterator::next;
      } else {
         Objects.requireNonNull(listIterator);
         var12 = listIterator::previous;
      }

      Supplier supplier = var12;

      GuiNavigationPath lv3;
      do {
         if (!booleanSupplier.getAsBoolean()) {
            return null;
         }

         Element lv2 = (Element)supplier.get();
         lv3 = lv2.getNavigationPath(navigation);
      } while(lv3 == null);

      return GuiNavigationPath.of(this, lv3);
   }

   @Nullable
   private GuiNavigationPath computeNavigationPath(GuiNavigation.Arrow navigation) {
      Element lv = this.getFocused();
      if (lv == null) {
         NavigationDirection lv2 = navigation.direction();
         ScreenRect lv3 = this.getNavigationFocus().getBorder(lv2.getOpposite());
         return GuiNavigationPath.of(this, this.computeChildPath(lv3, lv2, (Element)null, navigation));
      } else {
         ScreenRect lv4 = lv.getNavigationFocus();
         return GuiNavigationPath.of(this, this.computeChildPath(lv4, navigation.direction(), lv, navigation));
      }
   }

   @Nullable
   private GuiNavigationPath computeChildPath(ScreenRect focus, NavigationDirection direction, @Nullable Element focused, GuiNavigation navigation) {
      NavigationAxis lv = direction.getAxis();
      NavigationAxis lv2 = lv.getOther();
      NavigationDirection lv3 = lv2.getPositiveDirection();
      int i = focus.getBoundingCoordinate(direction.getOpposite());
      List list = new ArrayList();
      Iterator var10 = this.children().iterator();

      while(var10.hasNext()) {
         Element lv4 = (Element)var10.next();
         if (lv4 != focused) {
            ScreenRect lv5 = lv4.getNavigationFocus();
            if (lv5.overlaps(focus, lv2)) {
               int j = lv5.getBoundingCoordinate(direction.getOpposite());
               if (direction.isAfter(j, i)) {
                  list.add(lv4);
               } else if (j == i && direction.isAfter(lv5.getBoundingCoordinate(direction), focus.getBoundingCoordinate(direction))) {
                  list.add(lv4);
               }
            }
         }
      }

      Comparator comparator = Comparator.comparing((element) -> {
         return element.getNavigationFocus().getBoundingCoordinate(direction.getOpposite());
      }, direction.getComparator());
      Comparator comparator2 = Comparator.comparing((element) -> {
         return element.getNavigationFocus().getBoundingCoordinate(lv3.getOpposite());
      }, lv3.getComparator());
      list.sort(comparator.thenComparing(comparator2));
      Iterator var17 = list.iterator();

      GuiNavigationPath lv7;
      do {
         if (!var17.hasNext()) {
            return this.computeInitialChildPath(focus, direction, focused, navigation);
         }

         Element lv6 = (Element)var17.next();
         lv7 = lv6.getNavigationPath(navigation);
      } while(lv7 == null);

      return lv7;
   }

   @Nullable
   private GuiNavigationPath computeInitialChildPath(ScreenRect focus, NavigationDirection direction, @Nullable Element focused, GuiNavigation navigation) {
      NavigationAxis lv = direction.getAxis();
      NavigationAxis lv2 = lv.getOther();
      List list = new ArrayList();
      ScreenPos lv3 = ScreenPos.of(lv, focus.getBoundingCoordinate(direction), focus.getCenter(lv2));
      Iterator var9 = this.children().iterator();

      while(var9.hasNext()) {
         Element lv4 = (Element)var9.next();
         if (lv4 != focused) {
            ScreenRect lv5 = lv4.getNavigationFocus();
            ScreenPos lv6 = ScreenPos.of(lv, lv5.getBoundingCoordinate(direction.getOpposite()), lv5.getCenter(lv2));
            if (direction.isAfter(lv6.getComponent(lv), lv3.getComponent(lv))) {
               long l = Vector2i.distanceSquared(lv3.x(), lv3.y(), lv6.x(), lv6.y());
               list.add(Pair.of(lv4, l));
            }
         }
      }

      list.sort(Comparator.comparingDouble(Pair::getSecond));
      var9 = list.iterator();

      GuiNavigationPath lv7;
      do {
         if (!var9.hasNext()) {
            return null;
         }

         Pair pair = (Pair)var9.next();
         lv7 = ((Element)pair.getFirst()).getNavigationPath(navigation);
      } while(lv7 == null);

      return lv7;
   }
}
