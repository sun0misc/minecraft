package net.minecraft.client.gui.screen.advancement;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AdvancementWidget extends DrawableHelper {
   private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");
   private static final int field_32286 = 26;
   private static final int field_32287 = 0;
   private static final int field_32288 = 200;
   private static final int field_32289 = 26;
   private static final int ICON_OFFSET_X = 8;
   private static final int ICON_OFFSET_Y = 5;
   private static final int ICON_SIZE = 26;
   private static final int field_32293 = 3;
   private static final int field_32294 = 5;
   private static final int TITLE_OFFSET_X = 32;
   private static final int TITLE_OFFSET_Y = 9;
   private static final int TITLE_MAX_WIDTH = 163;
   private static final int[] SPLIT_OFFSET_CANDIDATES = new int[]{0, 10, -10, 25, -25};
   private final AdvancementTab tab;
   private final Advancement advancement;
   private final AdvancementDisplay display;
   private final OrderedText title;
   private final int width;
   private final List description;
   private final MinecraftClient client;
   @Nullable
   private AdvancementWidget parent;
   private final List children = Lists.newArrayList();
   @Nullable
   private AdvancementProgress progress;
   private final int x;
   private final int y;

   public AdvancementWidget(AdvancementTab tab, MinecraftClient client, Advancement advancement, AdvancementDisplay display) {
      this.tab = tab;
      this.advancement = advancement;
      this.display = display;
      this.client = client;
      this.title = Language.getInstance().reorder(client.textRenderer.trimToWidth((StringVisitable)display.getTitle(), 163));
      this.x = MathHelper.floor(display.getX() * 28.0F);
      this.y = MathHelper.floor(display.getY() * 27.0F);
      int i = advancement.getRequirementCount();
      int j = String.valueOf(i).length();
      int k = i > 1 ? client.textRenderer.getWidth("  ") + client.textRenderer.getWidth("0") * j * 2 + client.textRenderer.getWidth("/") : 0;
      int l = 29 + client.textRenderer.getWidth(this.title) + k;
      this.description = Language.getInstance().reorder(this.wrapDescription(Texts.setStyleIfAbsent(display.getDescription().copy(), Style.EMPTY.withColor(display.getFrame().getTitleFormat())), l));

      OrderedText lv;
      for(Iterator var9 = this.description.iterator(); var9.hasNext(); l = Math.max(l, client.textRenderer.getWidth(lv))) {
         lv = (OrderedText)var9.next();
      }

      this.width = l + 3 + 5;
   }

   private static float getMaxWidth(TextHandler textHandler, List lines) {
      Stream var10000 = lines.stream();
      Objects.requireNonNull(textHandler);
      return (float)var10000.mapToDouble(textHandler::getWidth).max().orElse(0.0);
   }

   private List wrapDescription(Text text, int width) {
      TextHandler lv = this.client.textRenderer.getTextHandler();
      List list = null;
      float f = Float.MAX_VALUE;
      int[] var6 = SPLIT_OFFSET_CANDIDATES;
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         int j = var6[var8];
         List list2 = lv.wrapLines((StringVisitable)text, width - j, Style.EMPTY);
         float g = Math.abs(getMaxWidth(lv, list2) - (float)width);
         if (g <= 10.0F) {
            return list2;
         }

         if (g < f) {
            f = g;
            list = list2;
         }
      }

      return list;
   }

   @Nullable
   private AdvancementWidget getParent(Advancement advancement) {
      do {
         advancement = advancement.getParent();
      } while(advancement != null && advancement.getDisplay() == null);

      if (advancement != null && advancement.getDisplay() != null) {
         return this.tab.getWidget(advancement);
      } else {
         return null;
      }
   }

   public void renderLines(MatrixStack matrices, int x, int y, boolean border) {
      if (this.parent != null) {
         int k = x + this.parent.x + 13;
         int l = x + this.parent.x + 26 + 4;
         int m = y + this.parent.y + 13;
         int n = x + this.x + 13;
         int o = y + this.y + 13;
         int p = border ? -16777216 : -1;
         if (border) {
            drawHorizontalLine(matrices, l, k, m - 1, p);
            drawHorizontalLine(matrices, l + 1, k, m, p);
            drawHorizontalLine(matrices, l, k, m + 1, p);
            drawHorizontalLine(matrices, n, l - 1, o - 1, p);
            drawHorizontalLine(matrices, n, l - 1, o, p);
            drawHorizontalLine(matrices, n, l - 1, o + 1, p);
            drawVerticalLine(matrices, l - 1, o, m, p);
            drawVerticalLine(matrices, l + 1, o, m, p);
         } else {
            drawHorizontalLine(matrices, l, k, m, p);
            drawHorizontalLine(matrices, n, l, o, p);
            drawVerticalLine(matrices, l, o, m, p);
         }
      }

      Iterator var11 = this.children.iterator();

      while(var11.hasNext()) {
         AdvancementWidget lv = (AdvancementWidget)var11.next();
         lv.renderLines(matrices, x, y, border);
      }

   }

   public void renderWidgets(MatrixStack matrices, int x, int y) {
      if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
         float f = this.progress == null ? 0.0F : this.progress.getProgressBarPercentage();
         AdvancementObtainedStatus lv;
         if (f >= 1.0F) {
            lv = AdvancementObtainedStatus.OBTAINED;
         } else {
            lv = AdvancementObtainedStatus.UNOBTAINED;
         }

         RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
         drawTexture(matrices, x + this.x + 3, y + this.y, this.display.getFrame().getTextureV(), 128 + lv.getSpriteIndex() * 26, 26, 26);
         this.client.getItemRenderer().renderInGui(matrices, this.display.getIcon(), x + this.x + 8, y + this.y + 5);
      }

      Iterator var6 = this.children.iterator();

      while(var6.hasNext()) {
         AdvancementWidget lv2 = (AdvancementWidget)var6.next();
         lv2.renderWidgets(matrices, x, y);
      }

   }

   public int getWidth() {
      return this.width;
   }

   public void setProgress(AdvancementProgress progress) {
      this.progress = progress;
   }

   public void addChild(AdvancementWidget widget) {
      this.children.add(widget);
   }

   public void drawTooltip(MatrixStack matrices, int originX, int originY, float alpha, int x, int y) {
      boolean bl = x + originX + this.x + this.width + 26 >= this.tab.getScreen().width;
      String string = this.progress == null ? null : this.progress.getProgressBarFraction();
      int m = string == null ? 0 : this.client.textRenderer.getWidth(string);
      int var10000 = 113 - originY - this.y - 26;
      int var10002 = this.description.size();
      Objects.requireNonNull(this.client.textRenderer);
      boolean bl2 = var10000 <= 6 + var10002 * 9;
      float g = this.progress == null ? 0.0F : this.progress.getProgressBarPercentage();
      int n = MathHelper.floor(g * (float)this.width);
      AdvancementObtainedStatus lv;
      AdvancementObtainedStatus lv2;
      AdvancementObtainedStatus lv3;
      if (g >= 1.0F) {
         n = this.width / 2;
         lv = AdvancementObtainedStatus.OBTAINED;
         lv2 = AdvancementObtainedStatus.OBTAINED;
         lv3 = AdvancementObtainedStatus.OBTAINED;
      } else if (n < 2) {
         n = this.width / 2;
         lv = AdvancementObtainedStatus.UNOBTAINED;
         lv2 = AdvancementObtainedStatus.UNOBTAINED;
         lv3 = AdvancementObtainedStatus.UNOBTAINED;
      } else if (n > this.width - 2) {
         n = this.width / 2;
         lv = AdvancementObtainedStatus.OBTAINED;
         lv2 = AdvancementObtainedStatus.OBTAINED;
         lv3 = AdvancementObtainedStatus.UNOBTAINED;
      } else {
         lv = AdvancementObtainedStatus.OBTAINED;
         lv2 = AdvancementObtainedStatus.UNOBTAINED;
         lv3 = AdvancementObtainedStatus.UNOBTAINED;
      }

      int o = this.width - n;
      RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
      RenderSystem.enableBlend();
      int p = originY + this.y;
      int q;
      if (bl) {
         q = originX + this.x - this.width + 26 + 6;
      } else {
         q = originX + this.x;
      }

      int var10001 = this.description.size();
      Objects.requireNonNull(this.client.textRenderer);
      int r = 32 + var10001 * 9;
      if (!this.description.isEmpty()) {
         if (bl2) {
            drawNineSlicedTexture(matrices, q, p + 26 - r, this.width, r, 10, 200, 26, 0, 52);
         } else {
            drawNineSlicedTexture(matrices, q, p, this.width, r, 10, 200, 26, 0, 52);
         }
      }

      drawTexture(matrices, q, p, 0, lv.getSpriteIndex() * 26, n, 26);
      drawTexture(matrices, q + n, p, 200 - o, lv2.getSpriteIndex() * 26, o, 26);
      drawTexture(matrices, originX + this.x + 3, originY + this.y, this.display.getFrame().getTextureV(), 128 + lv3.getSpriteIndex() * 26, 26, 26);
      if (bl) {
         this.client.textRenderer.drawWithShadow(matrices, (OrderedText)this.title, (float)(q + 5), (float)(originY + this.y + 9), -1);
         if (string != null) {
            this.client.textRenderer.drawWithShadow(matrices, (String)string, (float)(originX + this.x - m), (float)(originY + this.y + 9), -1);
         }
      } else {
         this.client.textRenderer.drawWithShadow(matrices, (OrderedText)this.title, (float)(originX + this.x + 32), (float)(originY + this.y + 9), -1);
         if (string != null) {
            this.client.textRenderer.drawWithShadow(matrices, (String)string, (float)(originX + this.x + this.width - m - 5), (float)(originY + this.y + 9), -1);
         }
      }

      float var10003;
      int s;
      int var10004;
      TextRenderer var21;
      OrderedText var22;
      if (bl2) {
         for(s = 0; s < this.description.size(); ++s) {
            var21 = this.client.textRenderer;
            var22 = (OrderedText)this.description.get(s);
            var10003 = (float)(q + 5);
            var10004 = p + 26 - r + 7;
            Objects.requireNonNull(this.client.textRenderer);
            var21.draw(matrices, var22, var10003, (float)(var10004 + s * 9), -5592406);
         }
      } else {
         for(s = 0; s < this.description.size(); ++s) {
            var21 = this.client.textRenderer;
            var22 = (OrderedText)this.description.get(s);
            var10003 = (float)(q + 5);
            var10004 = originY + this.y + 9 + 17;
            Objects.requireNonNull(this.client.textRenderer);
            var21.draw(matrices, var22, var10003, (float)(var10004 + s * 9), -5592406);
         }
      }

      this.client.getItemRenderer().renderInGui(matrices, this.display.getIcon(), originX + this.x + 8, originY + this.y + 5);
   }

   public boolean shouldRender(int originX, int originY, int mouseX, int mouseY) {
      if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
         int m = originX + this.x;
         int n = m + 26;
         int o = originY + this.y;
         int p = o + 26;
         return mouseX >= m && mouseX <= n && mouseY >= o && mouseY <= p;
      } else {
         return false;
      }
   }

   public void addToTree() {
      if (this.parent == null && this.advancement.getParent() != null) {
         this.parent = this.getParent(this.advancement);
         if (this.parent != null) {
            this.parent.addChild(this);
         }
      }

   }

   public int getY() {
      return this.y;
   }

   public int getX() {
      return this.x;
   }
}
