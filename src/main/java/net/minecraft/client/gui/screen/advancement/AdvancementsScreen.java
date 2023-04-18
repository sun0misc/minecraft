package net.minecraft.client.gui.screen.advancement;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AdvancementsScreen extends Screen implements ClientAdvancementManager.Listener {
   private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
   private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
   public static final int WINDOW_WIDTH = 252;
   public static final int WINDOW_HEIGHT = 140;
   private static final int PAGE_OFFSET_X = 9;
   private static final int PAGE_OFFSET_Y = 18;
   public static final int PAGE_WIDTH = 234;
   public static final int PAGE_HEIGHT = 113;
   private static final int TITLE_OFFSET_X = 8;
   private static final int TITLE_OFFSET_Y = 6;
   public static final int field_32302 = 16;
   public static final int field_32303 = 16;
   public static final int field_32304 = 14;
   public static final int field_32305 = 7;
   private static final Text SAD_LABEL_TEXT = Text.translatable("advancements.sad_label");
   private static final Text EMPTY_TEXT = Text.translatable("advancements.empty");
   private static final Text ADVANCEMENTS_TEXT = Text.translatable("gui.advancements");
   private final ClientAdvancementManager advancementHandler;
   private final Map tabs = Maps.newLinkedHashMap();
   @Nullable
   private AdvancementTab selectedTab;
   private boolean movingTab;

   public AdvancementsScreen(ClientAdvancementManager advancementHandler) {
      super(NarratorManager.EMPTY);
      this.advancementHandler = advancementHandler;
   }

   protected void init() {
      this.tabs.clear();
      this.selectedTab = null;
      this.advancementHandler.setListener(this);
      if (this.selectedTab == null && !this.tabs.isEmpty()) {
         this.advancementHandler.selectTab(((AdvancementTab)this.tabs.values().iterator().next()).getRoot(), true);
      } else {
         this.advancementHandler.selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot(), true);
      }

   }

   public void removed() {
      this.advancementHandler.setListener((ClientAdvancementManager.Listener)null);
      ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
      if (lv != null) {
         lv.sendPacket(AdvancementTabC2SPacket.close());
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         int j = (this.width - 252) / 2;
         int k = (this.height - 140) / 2;
         Iterator var8 = this.tabs.values().iterator();

         while(var8.hasNext()) {
            AdvancementTab lv = (AdvancementTab)var8.next();
            if (lv.isClickOnTab(j, k, mouseX, mouseY)) {
               this.advancementHandler.selectTab(lv.getRoot(), true);
               break;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.client.options.advancementsKey.matchesKey(keyCode, scanCode)) {
         this.client.setScreen((Screen)null);
         this.client.mouse.lockCursor();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      int k = (this.width - 252) / 2;
      int l = (this.height - 140) / 2;
      this.renderBackground(matrices);
      this.drawAdvancementTree(matrices, mouseX, mouseY, k, l);
      this.drawWindow(matrices, k, l);
      this.drawWidgetTooltip(matrices, mouseX, mouseY, k, l);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (button != 0) {
         this.movingTab = false;
         return false;
      } else {
         if (!this.movingTab) {
            this.movingTab = true;
         } else if (this.selectedTab != null) {
            this.selectedTab.move(deltaX, deltaY);
         }

         return true;
      }
   }

   private void drawAdvancementTree(MatrixStack matrices, int mouseX, int mouseY, int x, int y) {
      AdvancementTab lv = this.selectedTab;
      if (lv == null) {
         fill(matrices, x + 9, y + 18, x + 9 + 234, y + 18 + 113, -16777216);
         int m = x + 9 + 117;
         TextRenderer var10001 = this.textRenderer;
         Text var10002 = EMPTY_TEXT;
         int var10004 = y + 18 + 56;
         Objects.requireNonNull(this.textRenderer);
         drawCenteredTextWithShadow(matrices, var10001, var10002, m, var10004 - 9 / 2, -1);
         var10001 = this.textRenderer;
         var10002 = SAD_LABEL_TEXT;
         var10004 = y + 18 + 113;
         Objects.requireNonNull(this.textRenderer);
         drawCenteredTextWithShadow(matrices, var10001, var10002, m, var10004 - 9, -1);
      } else {
         lv.render(matrices, x + 9, y + 18);
      }
   }

   public void drawWindow(MatrixStack matrices, int x, int y) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
      drawTexture(matrices, x, y, 0, 0, 252, 140);
      if (this.tabs.size() > 1) {
         RenderSystem.setShaderTexture(0, TABS_TEXTURE);
         Iterator var4 = this.tabs.values().iterator();

         AdvancementTab lv;
         while(var4.hasNext()) {
            lv = (AdvancementTab)var4.next();
            lv.drawBackground(matrices, x, y, lv == this.selectedTab);
         }

         var4 = this.tabs.values().iterator();

         while(var4.hasNext()) {
            lv = (AdvancementTab)var4.next();
            lv.drawIcon(matrices, x, y, this.itemRenderer);
         }
      }

      this.textRenderer.draw(matrices, ADVANCEMENTS_TEXT, (float)(x + 8), (float)(y + 6), 4210752);
   }

   private void drawWidgetTooltip(MatrixStack matrices, int mouseX, int mouseY, int x, int y) {
      if (this.selectedTab != null) {
         matrices.push();
         matrices.translate((float)(x + 9), (float)(y + 18), 400.0F);
         RenderSystem.enableDepthTest();
         this.selectedTab.drawWidgetTooltip(matrices, mouseX - x - 9, mouseY - y - 18, x, y);
         RenderSystem.disableDepthTest();
         matrices.pop();
      }

      if (this.tabs.size() > 1) {
         Iterator var6 = this.tabs.values().iterator();

         while(var6.hasNext()) {
            AdvancementTab lv = (AdvancementTab)var6.next();
            if (lv.isClickOnTab(x, y, (double)mouseX, (double)mouseY)) {
               this.renderTooltip(matrices, lv.getTitle(), mouseX, mouseY);
            }
         }
      }

   }

   public void onRootAdded(Advancement root) {
      AdvancementTab lv = AdvancementTab.create(this.client, this, this.tabs.size(), root);
      if (lv != null) {
         this.tabs.put(root, lv);
      }
   }

   public void onRootRemoved(Advancement root) {
   }

   public void onDependentAdded(Advancement dependent) {
      AdvancementTab lv = this.getTab(dependent);
      if (lv != null) {
         lv.addAdvancement(dependent);
      }

   }

   public void onDependentRemoved(Advancement dependent) {
   }

   public void setProgress(Advancement advancement, AdvancementProgress progress) {
      AdvancementWidget lv = this.getAdvancementWidget(advancement);
      if (lv != null) {
         lv.setProgress(progress);
      }

   }

   public void selectTab(@Nullable Advancement advancement) {
      this.selectedTab = (AdvancementTab)this.tabs.get(advancement);
   }

   public void onClear() {
      this.tabs.clear();
      this.selectedTab = null;
   }

   @Nullable
   public AdvancementWidget getAdvancementWidget(Advancement advancement) {
      AdvancementTab lv = this.getTab(advancement);
      return lv == null ? null : lv.getWidget(advancement);
   }

   @Nullable
   private AdvancementTab getTab(Advancement advancement) {
      while(advancement.getParent() != null) {
         advancement = advancement.getParent();
      }

      return (AdvancementTab)this.tabs.get(advancement);
   }
}
