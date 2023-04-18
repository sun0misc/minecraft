package net.minecraft.advancement;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class AdvancementPositioner {
   private final Advancement advancement;
   @Nullable
   private final AdvancementPositioner parent;
   @Nullable
   private final AdvancementPositioner previousSibling;
   private final int childrenSize;
   private final List children = Lists.newArrayList();
   private AdvancementPositioner optionalLast;
   @Nullable
   private AdvancementPositioner substituteChild;
   private int depth;
   private float row;
   private float relativeRowInSiblings;
   private float field_1266;
   private float field_1265;

   public AdvancementPositioner(Advancement advancement, @Nullable AdvancementPositioner parent, @Nullable AdvancementPositioner previousSibling, int childrenSize, int depth) {
      if (advancement.getDisplay() == null) {
         throw new IllegalArgumentException("Can't position an invisible advancement!");
      } else {
         this.advancement = advancement;
         this.parent = parent;
         this.previousSibling = previousSibling;
         this.childrenSize = childrenSize;
         this.optionalLast = this;
         this.depth = depth;
         this.row = -1.0F;
         AdvancementPositioner lv = null;

         Advancement lv2;
         for(Iterator var7 = advancement.getChildren().iterator(); var7.hasNext(); lv = this.findChildrenRecursively(lv2, lv)) {
            lv2 = (Advancement)var7.next();
         }

      }
   }

   @Nullable
   private AdvancementPositioner findChildrenRecursively(Advancement advancement, @Nullable AdvancementPositioner lastChild) {
      Advancement lv;
      if (advancement.getDisplay() != null) {
         lastChild = new AdvancementPositioner(advancement, this, lastChild, this.children.size() + 1, this.depth + 1);
         this.children.add(lastChild);
      } else {
         for(Iterator var3 = advancement.getChildren().iterator(); var3.hasNext(); lastChild = this.findChildrenRecursively(lv, lastChild)) {
            lv = (Advancement)var3.next();
         }
      }

      return lastChild;
   }

   private void calculateRecursively() {
      if (this.children.isEmpty()) {
         if (this.previousSibling != null) {
            this.row = this.previousSibling.row + 1.0F;
         } else {
            this.row = 0.0F;
         }

      } else {
         AdvancementPositioner lv = null;

         AdvancementPositioner lv2;
         for(Iterator var2 = this.children.iterator(); var2.hasNext(); lv = lv2.onFinishCalculation(lv == null ? lv2 : lv)) {
            lv2 = (AdvancementPositioner)var2.next();
            lv2.calculateRecursively();
         }

         this.onFinishChildrenCalculation();
         float f = (((AdvancementPositioner)this.children.get(0)).row + ((AdvancementPositioner)this.children.get(this.children.size() - 1)).row) / 2.0F;
         if (this.previousSibling != null) {
            this.row = this.previousSibling.row + 1.0F;
            this.relativeRowInSiblings = this.row - f;
         } else {
            this.row = f;
         }

      }
   }

   private float findMinRowRecursively(float deltaRow, int depth, float minRow) {
      this.row += deltaRow;
      this.depth = depth;
      if (this.row < minRow) {
         minRow = this.row;
      }

      AdvancementPositioner lv;
      for(Iterator var4 = this.children.iterator(); var4.hasNext(); minRow = lv.findMinRowRecursively(deltaRow + this.relativeRowInSiblings, depth + 1, minRow)) {
         lv = (AdvancementPositioner)var4.next();
      }

      return minRow;
   }

   private void increaseRowRecursively(float deltaRow) {
      this.row += deltaRow;
      Iterator var2 = this.children.iterator();

      while(var2.hasNext()) {
         AdvancementPositioner lv = (AdvancementPositioner)var2.next();
         lv.increaseRowRecursively(deltaRow);
      }

   }

   private void onFinishChildrenCalculation() {
      float f = 0.0F;
      float g = 0.0F;

      for(int i = this.children.size() - 1; i >= 0; --i) {
         AdvancementPositioner lv = (AdvancementPositioner)this.children.get(i);
         lv.row += f;
         lv.relativeRowInSiblings += f;
         g += lv.field_1266;
         f += lv.field_1265 + g;
      }

   }

   @Nullable
   private AdvancementPositioner getFirstChild() {
      if (this.substituteChild != null) {
         return this.substituteChild;
      } else {
         return !this.children.isEmpty() ? (AdvancementPositioner)this.children.get(0) : null;
      }
   }

   @Nullable
   private AdvancementPositioner getLastChild() {
      if (this.substituteChild != null) {
         return this.substituteChild;
      } else {
         return !this.children.isEmpty() ? (AdvancementPositioner)this.children.get(this.children.size() - 1) : null;
      }
   }

   private AdvancementPositioner onFinishCalculation(AdvancementPositioner last) {
      if (this.previousSibling == null) {
         return last;
      } else {
         AdvancementPositioner lv = this;
         AdvancementPositioner lv2 = this;
         AdvancementPositioner lv3 = this.previousSibling;
         AdvancementPositioner lv4 = (AdvancementPositioner)this.parent.children.get(0);
         float f = this.relativeRowInSiblings;
         float g = this.relativeRowInSiblings;
         float h = lv3.relativeRowInSiblings;

         float i;
         for(i = lv4.relativeRowInSiblings; lv3.getLastChild() != null && lv.getFirstChild() != null; g += lv2.relativeRowInSiblings) {
            lv3 = lv3.getLastChild();
            lv = lv.getFirstChild();
            lv4 = lv4.getFirstChild();
            lv2 = lv2.getLastChild();
            lv2.optionalLast = this;
            float j = lv3.row + h - (lv.row + f) + 1.0F;
            if (j > 0.0F) {
               lv3.getLast(this, last).pushDown(this, j);
               f += j;
               g += j;
            }

            h += lv3.relativeRowInSiblings;
            f += lv.relativeRowInSiblings;
            i += lv4.relativeRowInSiblings;
         }

         if (lv3.getLastChild() != null && lv2.getLastChild() == null) {
            lv2.substituteChild = lv3.getLastChild();
            lv2.relativeRowInSiblings += h - g;
         } else {
            if (lv.getFirstChild() != null && lv4.getFirstChild() == null) {
               lv4.substituteChild = lv.getFirstChild();
               lv4.relativeRowInSiblings += f - i;
            }

            last = this;
         }

         return last;
      }
   }

   private void pushDown(AdvancementPositioner positioner, float extraRowDistance) {
      float g = (float)(positioner.childrenSize - this.childrenSize);
      if (g != 0.0F) {
         positioner.field_1266 -= extraRowDistance / g;
         this.field_1266 += extraRowDistance / g;
      }

      positioner.field_1265 += extraRowDistance;
      positioner.row += extraRowDistance;
      positioner.relativeRowInSiblings += extraRowDistance;
   }

   private AdvancementPositioner getLast(AdvancementPositioner arg, AdvancementPositioner arg2) {
      return this.optionalLast != null && arg.parent.children.contains(this.optionalLast) ? this.optionalLast : arg2;
   }

   private void apply() {
      if (this.advancement.getDisplay() != null) {
         this.advancement.getDisplay().setPos((float)this.depth, this.row);
      }

      if (!this.children.isEmpty()) {
         Iterator var1 = this.children.iterator();

         while(var1.hasNext()) {
            AdvancementPositioner lv = (AdvancementPositioner)var1.next();
            lv.apply();
         }
      }

   }

   public static void arrangeForTree(Advancement root) {
      if (root.getDisplay() == null) {
         throw new IllegalArgumentException("Can't position children of an invisible root!");
      } else {
         AdvancementPositioner lv = new AdvancementPositioner(root, (AdvancementPositioner)null, (AdvancementPositioner)null, 1, 0);
         lv.calculateRecursively();
         float f = lv.findMinRowRecursively(0.0F, 0, lv.row);
         if (f < 0.0F) {
            lv.increaseRowRecursively(-f);
         }

         lv.apply();
      }
   }
}
