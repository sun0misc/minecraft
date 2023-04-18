package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.WorldTemplate;
import net.minecraft.client.realms.dto.WorldTemplatePaginatedList;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.util.RealmsTextureManager;
import net.minecraft.client.realms.util.TextRenderingUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   static final Identifier LINK_ICONS = new Identifier("realms", "textures/gui/realms/link_icons.png");
   static final Identifier TRAILER_ICONS = new Identifier("realms", "textures/gui/realms/trailer_icons.png");
   static final Identifier SLOT_FRAME = new Identifier("realms", "textures/gui/realms/slot_frame.png");
   static final Text INFO_TOOLTIP = Text.translatable("mco.template.info.tooltip");
   static final Text TRAILER_TOOLTIP = Text.translatable("mco.template.trailer.tooltip");
   private final Consumer callback;
   WorldTemplateObjectSelectionList templateList;
   int selectedTemplate;
   private ButtonWidget selectButton;
   private ButtonWidget trailerButton;
   private ButtonWidget publisherButton;
   @Nullable
   Text tooltip;
   @Nullable
   String currentLink;
   private final RealmsServer.WorldType worldType;
   int clicks;
   @Nullable
   private Text[] warning;
   private String warningURL;
   boolean displayWarning;
   private boolean hoverWarning;
   @Nullable
   List noTemplatesMessage;

   public RealmsSelectWorldTemplateScreen(Text title, Consumer callback, RealmsServer.WorldType worldType) {
      this(title, callback, worldType, (WorldTemplatePaginatedList)null);
   }

   public RealmsSelectWorldTemplateScreen(Text title, Consumer callback, RealmsServer.WorldType worldType, @Nullable WorldTemplatePaginatedList templateList) {
      super(title);
      this.selectedTemplate = -1;
      this.callback = callback;
      this.worldType = worldType;
      if (templateList == null) {
         this.templateList = new WorldTemplateObjectSelectionList();
         this.setPagination(new WorldTemplatePaginatedList(10));
      } else {
         this.templateList = new WorldTemplateObjectSelectionList(Lists.newArrayList(templateList.templates));
         this.setPagination(templateList);
      }

   }

   public void setWarning(Text... warning) {
      this.warning = warning;
      this.displayWarning = true;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.hoverWarning && this.warningURL != null) {
         Util.getOperatingSystem().open("https://www.minecraft.net/realms/adventure-maps-in-1-9");
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public void init() {
      this.templateList = new WorldTemplateObjectSelectionList(this.templateList.getValues());
      this.trailerButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.template.button.trailer"), (button) -> {
         this.onTrailer();
      }).dimensions(this.width / 2 - 206, this.height - 32, 100, 20).build());
      this.selectButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.template.button.select"), (button) -> {
         this.selectTemplate();
      }).dimensions(this.width / 2 - 100, this.height - 32, 100, 20).build());
      Text lv = this.worldType == RealmsServer.WorldType.MINIGAME ? ScreenTexts.CANCEL : ScreenTexts.BACK;
      ButtonWidget lv2 = ButtonWidget.builder(lv, (button) -> {
         this.close();
      }).dimensions(this.width / 2 + 6, this.height - 32, 100, 20).build();
      this.addDrawableChild(lv2);
      this.publisherButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.template.button.publisher"), (button) -> {
         this.onPublish();
      }).dimensions(this.width / 2 + 112, this.height - 32, 100, 20).build());
      this.selectButton.active = false;
      this.trailerButton.visible = false;
      this.publisherButton.visible = false;
      this.addSelectableChild(this.templateList);
      this.focusOn(this.templateList);
   }

   public Text getNarratedTitle() {
      List list = Lists.newArrayListWithCapacity(2);
      if (this.title != null) {
         list.add(this.title);
      }

      if (this.warning != null) {
         list.addAll(Arrays.asList(this.warning));
      }

      return ScreenTexts.joinLines((Collection)list);
   }

   void updateButtonStates() {
      this.publisherButton.visible = this.shouldPublisherBeVisible();
      this.trailerButton.visible = this.shouldTrailerBeVisible();
      this.selectButton.active = this.shouldSelectButtonBeActive();
   }

   private boolean shouldSelectButtonBeActive() {
      return this.selectedTemplate != -1;
   }

   private boolean shouldPublisherBeVisible() {
      return this.selectedTemplate != -1 && !this.getSelectedTemplate().link.isEmpty();
   }

   private WorldTemplate getSelectedTemplate() {
      return this.templateList.getItem(this.selectedTemplate);
   }

   private boolean shouldTrailerBeVisible() {
      return this.selectedTemplate != -1 && !this.getSelectedTemplate().trailer.isEmpty();
   }

   public void tick() {
      super.tick();
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

   }

   public void close() {
      this.callback.accept((Object)null);
   }

   void selectTemplate() {
      if (this.isSelectionValid()) {
         this.callback.accept(this.getSelectedTemplate());
      }

   }

   private boolean isSelectionValid() {
      return this.selectedTemplate >= 0 && this.selectedTemplate < this.templateList.getEntryCount();
   }

   private void onTrailer() {
      if (this.isSelectionValid()) {
         WorldTemplate lv = this.getSelectedTemplate();
         if (!"".equals(lv.trailer)) {
            Util.getOperatingSystem().open(lv.trailer);
         }
      }

   }

   private void onPublish() {
      if (this.isSelectionValid()) {
         WorldTemplate lv = this.getSelectedTemplate();
         if (!"".equals(lv.link)) {
            Util.getOperatingSystem().open(lv.link);
         }
      }

   }

   private void setPagination(final WorldTemplatePaginatedList templateList) {
      (new Thread("realms-template-fetcher") {
         public void run() {
            WorldTemplatePaginatedList lv = templateList;

            Either either;
            for(RealmsClient lv2 = RealmsClient.create(); lv != null; lv = (WorldTemplatePaginatedList)RealmsSelectWorldTemplateScreen.this.client.submit(() -> {
               if (either.right().isPresent()) {
                  RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates: {}", either.right().get());
                  if (RealmsSelectWorldTemplateScreen.this.templateList.isEmpty()) {
                     RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(I18n.translate("mco.template.select.failure"));
                  }

                  return null;
               } else {
                  WorldTemplatePaginatedList lv = (WorldTemplatePaginatedList)either.left().get();
                  Iterator var3 = lv.templates.iterator();

                  while(var3.hasNext()) {
                     WorldTemplate lv2 = (WorldTemplate)var3.next();
                     RealmsSelectWorldTemplateScreen.this.templateList.addEntry(lv2);
                  }

                  if (lv.templates.isEmpty()) {
                     if (RealmsSelectWorldTemplateScreen.this.templateList.isEmpty()) {
                        String string = I18n.translate("mco.template.select.none", "%link");
                        TextRenderingUtils.LineSegment lv3 = TextRenderingUtils.LineSegment.link(I18n.translate("mco.template.select.none.linkTitle"), "https://aka.ms/MinecraftRealmsContentCreator");
                        RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(string, lv3);
                     }

                     return null;
                  } else {
                     return lv;
                  }
               }
            }).join()) {
               either = RealmsSelectWorldTemplateScreen.this.fetchWorldTemplates(lv, lv2);
            }

         }
      }).start();
   }

   Either fetchWorldTemplates(WorldTemplatePaginatedList templateList, RealmsClient realms) {
      try {
         return Either.left(realms.fetchWorldTemplates(templateList.page + 1, templateList.size, this.worldType));
      } catch (RealmsServiceException var4) {
         return Either.right(var4.getMessage());
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.tooltip = null;
      this.currentLink = null;
      this.hoverWarning = false;
      this.renderBackground(matrices);
      this.templateList.render(matrices, mouseX, mouseY, delta);
      if (this.noTemplatesMessage != null) {
         this.renderMessages(matrices, mouseX, mouseY, this.noTemplatesMessage);
      }

      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 13, 16777215);
      if (this.displayWarning) {
         Text[] lvs = this.warning;

         int k;
         int m;
         for(k = 0; k < lvs.length; ++k) {
            int l = this.textRenderer.getWidth((StringVisitable)lvs[k]);
            m = this.width / 2 - l / 2;
            int n = row(-1 + k);
            if (mouseX >= m && mouseX <= m + l && mouseY >= n) {
               Objects.requireNonNull(this.textRenderer);
               if (mouseY <= n + 9) {
                  this.hoverWarning = true;
               }
            }
         }

         for(k = 0; k < lvs.length; ++k) {
            Text lv = lvs[k];
            m = 10526880;
            if (this.warningURL != null) {
               if (this.hoverWarning) {
                  m = 7107012;
                  lv = ((Text)lv).copy().formatted(Formatting.STRIKETHROUGH);
               } else {
                  m = 3368635;
               }
            }

            drawCenteredTextWithShadow(matrices, this.textRenderer, (Text)lv, this.width / 2, row(-1 + k), m);
         }
      }

      super.render(matrices, mouseX, mouseY, delta);
      this.renderMousehoverTooltip(matrices, this.tooltip, mouseX, mouseY);
   }

   private void renderMessages(MatrixStack matrices, int x, int y, List messages) {
      for(int k = 0; k < messages.size(); ++k) {
         TextRenderingUtils.Line lv = (TextRenderingUtils.Line)messages.get(k);
         int l = row(4 + k);
         int m = lv.segments.stream().mapToInt((segment) -> {
            return this.textRenderer.getWidth(segment.renderedText());
         }).sum();
         int n = this.width / 2 - m / 2;

         int p;
         for(Iterator var10 = lv.segments.iterator(); var10.hasNext(); n = p) {
            TextRenderingUtils.LineSegment lv2 = (TextRenderingUtils.LineSegment)var10.next();
            int o = lv2.isLink() ? 3368635 : 16777215;
            p = this.textRenderer.drawWithShadow(matrices, lv2.renderedText(), (float)n, (float)l, o);
            if (lv2.isLink() && x > n && x < p && y > l - 3 && y < l + 8) {
               this.tooltip = Text.literal(lv2.getLinkUrl());
               this.currentLink = lv2.getLinkUrl();
            }
         }
      }

   }

   protected void renderMousehoverTooltip(MatrixStack matrices, @Nullable Text tooltip, int mouseX, int mouseY) {
      if (tooltip != null) {
         int k = mouseX + 12;
         int l = mouseY - 12;
         int m = this.textRenderer.getWidth((StringVisitable)tooltip);
         fillGradient(matrices, k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
         this.textRenderer.drawWithShadow(matrices, tooltip, (float)k, (float)l, 16777215);
      }
   }

   @Environment(EnvType.CLIENT)
   private class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList {
      public WorldTemplateObjectSelectionList() {
         this(Collections.emptyList());
      }

      public WorldTemplateObjectSelectionList(Iterable templates) {
         super(RealmsSelectWorldTemplateScreen.this.width, RealmsSelectWorldTemplateScreen.this.height, RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsSelectWorldTemplateScreen.row(1) : 32, RealmsSelectWorldTemplateScreen.this.height - 40, 46);
         templates.forEach(this::addEntry);
      }

      public void addEntry(WorldTemplate template) {
         this.addEntry(RealmsSelectWorldTemplateScreen.this.new WorldTemplateObjectSelectionListEntry(template));
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (button == 0 && mouseY >= (double)this.top && mouseY <= (double)this.bottom) {
            int j = this.width / 2 - 150;
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
               Util.getOperatingSystem().open(RealmsSelectWorldTemplateScreen.this.currentLink);
            }

            int k = (int)Math.floor(mouseY - (double)this.top) - this.headerHeight + (int)this.getScrollAmount() - 4;
            int l = k / this.itemHeight;
            if (mouseX >= (double)j && mouseX < (double)this.getScrollbarPositionX() && l >= 0 && k >= 0 && l < this.getEntryCount()) {
               this.setSelected(l);
               this.itemClicked(k, l, mouseX, mouseY, this.width, button);
               if (l >= RealmsSelectWorldTemplateScreen.this.templateList.getEntryCount()) {
                  return super.mouseClicked(mouseX, mouseY, button);
               }

               RealmsSelectWorldTemplateScreen var10000 = RealmsSelectWorldTemplateScreen.this;
               var10000.clicks += 7;
               if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
                  RealmsSelectWorldTemplateScreen.this.selectTemplate();
               }

               return true;
            }
         }

         return super.mouseClicked(mouseX, mouseY, button);
      }

      public void setSelected(@Nullable WorldTemplateObjectSelectionListEntry arg) {
         super.setSelected(arg);
         RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.children().indexOf(arg);
         RealmsSelectWorldTemplateScreen.this.updateButtonStates();
      }

      public int getMaxPosition() {
         return this.getEntryCount() * 46;
      }

      public int getRowWidth() {
         return 300;
      }

      public void renderBackground(MatrixStack matrices) {
         RealmsSelectWorldTemplateScreen.this.renderBackground(matrices);
      }

      public boolean isEmpty() {
         return this.getEntryCount() == 0;
      }

      public WorldTemplate getItem(int index) {
         return ((WorldTemplateObjectSelectionListEntry)this.children().get(index)).mTemplate;
      }

      public List getValues() {
         return (List)this.children().stream().map((child) -> {
            return child.mTemplate;
         }).collect(Collectors.toList());
      }
   }

   @Environment(EnvType.CLIENT)
   private class WorldTemplateObjectSelectionListEntry extends AlwaysSelectedEntryListWidget.Entry {
      final WorldTemplate mTemplate;

      public WorldTemplateObjectSelectionListEntry(WorldTemplate template) {
         this.mTemplate = template;
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.renderWorldTemplateItem(matrices, this.mTemplate, x, y, mouseX, mouseY);
      }

      private void renderWorldTemplateItem(MatrixStack matrices, WorldTemplate template, int x, int y, int mouseX, int mouseY) {
         int m = x + 45 + 20;
         RealmsSelectWorldTemplateScreen.this.textRenderer.draw(matrices, template.name, (float)m, (float)(y + 2), 16777215);
         RealmsSelectWorldTemplateScreen.this.textRenderer.draw(matrices, template.author, (float)m, (float)(y + 15), 7105644);
         RealmsSelectWorldTemplateScreen.this.textRenderer.draw(matrices, template.version, (float)(m + 227 - RealmsSelectWorldTemplateScreen.this.textRenderer.getWidth(template.version)), (float)(y + 1), 7105644);
         if (!"".equals(template.link) || !"".equals(template.trailer) || !"".equals(template.recommendedPlayers)) {
            this.drawIcons(matrices, m - 1, y + 25, mouseX, mouseY, template.link, template.trailer, template.recommendedPlayers);
         }

         this.drawImage(matrices, x, y + 1, mouseX, mouseY, template);
      }

      private void drawImage(MatrixStack matrices, int x, int y, int mouseX, int mouseY, WorldTemplate template) {
         RenderSystem.setShaderTexture(0, RealmsTextureManager.getTextureId(template.id, template.image));
         DrawableHelper.drawTexture(matrices, x + 1, y + 1, 0.0F, 0.0F, 38, 38, 38, 38);
         RenderSystem.setShaderTexture(0, RealmsSelectWorldTemplateScreen.SLOT_FRAME);
         DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 40, 40, 40, 40);
      }

      private void drawIcons(MatrixStack matrices, int x, int y, int mouseX, int mouseY, String link, String trailer, String recommendedPlayers) {
         if (!"".equals(recommendedPlayers)) {
            RealmsSelectWorldTemplateScreen.this.textRenderer.draw(matrices, recommendedPlayers, (float)x, (float)(y + 4), 5000268);
         }

         int m = "".equals(recommendedPlayers) ? 0 : RealmsSelectWorldTemplateScreen.this.textRenderer.getWidth(recommendedPlayers) + 2;
         boolean bl = false;
         boolean bl2 = false;
         boolean bl3 = "".equals(link);
         if (mouseX >= x + m && mouseX <= x + m + 32 && mouseY >= y && mouseY <= y + 15 && mouseY < RealmsSelectWorldTemplateScreen.this.height - 15 && mouseY > 32) {
            if (mouseX <= x + 15 + m && mouseX > m) {
               if (bl3) {
                  bl2 = true;
               } else {
                  bl = true;
               }
            } else if (!bl3) {
               bl2 = true;
            }
         }

         if (!bl3) {
            RenderSystem.setShaderTexture(0, RealmsSelectWorldTemplateScreen.LINK_ICONS);
            float f = bl ? 15.0F : 0.0F;
            DrawableHelper.drawTexture(matrices, x + m, y, f, 0.0F, 15, 15, 30, 15);
         }

         if (!"".equals(trailer)) {
            RenderSystem.setShaderTexture(0, RealmsSelectWorldTemplateScreen.TRAILER_ICONS);
            int n = x + m + (bl3 ? 0 : 17);
            float g = bl2 ? 15.0F : 0.0F;
            DrawableHelper.drawTexture(matrices, n, y, g, 0.0F, 15, 15, 30, 15);
         }

         if (bl) {
            RealmsSelectWorldTemplateScreen.this.tooltip = RealmsSelectWorldTemplateScreen.INFO_TOOLTIP;
            RealmsSelectWorldTemplateScreen.this.currentLink = link;
         } else if (bl2 && !"".equals(trailer)) {
            RealmsSelectWorldTemplateScreen.this.tooltip = RealmsSelectWorldTemplateScreen.TRAILER_TOOLTIP;
            RealmsSelectWorldTemplateScreen.this.currentLink = trailer;
         }

      }

      public Text getNarration() {
         Text lv = ScreenTexts.joinLines(Text.literal(this.mTemplate.name), Text.translatable("mco.template.select.narrate.authors", this.mTemplate.author), Text.literal(this.mTemplate.recommendedPlayers), Text.translatable("mco.template.select.narrate.version", this.mTemplate.version));
         return Text.translatable("narrator.select", lv);
      }
   }
}
