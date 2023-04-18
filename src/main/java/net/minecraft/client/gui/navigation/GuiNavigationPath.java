package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface GuiNavigationPath {
   static GuiNavigationPath of(Element leaf) {
      return new Leaf(leaf);
   }

   @Nullable
   static GuiNavigationPath of(ParentElement element, @Nullable GuiNavigationPath childPath) {
      return childPath == null ? null : new IntermediaryNode(element, childPath);
   }

   static GuiNavigationPath of(Element leaf, ParentElement... elements) {
      GuiNavigationPath lv = of(leaf);
      ParentElement[] var3 = elements;
      int var4 = elements.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ParentElement lv2 = var3[var5];
         lv = of(lv2, lv);
      }

      return lv;
   }

   Element component();

   void setFocused(boolean focused);

   @Environment(EnvType.CLIENT)
   public static record Leaf(Element component) implements GuiNavigationPath {
      public Leaf(Element arg) {
         this.component = arg;
      }

      public void setFocused(boolean focused) {
         this.component.setFocused(focused);
      }

      public Element component() {
         return this.component;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record IntermediaryNode(ParentElement component, GuiNavigationPath childPath) implements GuiNavigationPath {
      public IntermediaryNode(ParentElement arg, GuiNavigationPath arg2) {
         this.component = arg;
         this.childPath = arg2;
      }

      public void setFocused(boolean focused) {
         if (!focused) {
            this.component.setFocused((Element)null);
         } else {
            this.component.setFocused(this.childPath.component());
         }

         this.childPath.setFocused(focused);
      }

      public ParentElement component() {
         return this.component;
      }

      public GuiNavigationPath childPath() {
         return this.childPath;
      }

      // $FF: synthetic method
      public Element component() {
         return this.component();
      }
   }
}
