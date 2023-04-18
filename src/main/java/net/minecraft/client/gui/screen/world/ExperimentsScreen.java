package net.minecraft.client.gui.screen.world;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class ExperimentsScreen extends Screen {
   private static final int INFO_WIDTH = 310;
   private final ThreePartsLayoutWidget experimentToggleList = new ThreePartsLayoutWidget(this);
   private final Screen parent;
   private final ResourcePackManager resourcePackManager;
   private final Consumer applier;
   private final Object2BooleanMap experiments = new Object2BooleanLinkedOpenHashMap();

   protected ExperimentsScreen(Screen parent, ResourcePackManager resourcePackManager, Consumer applier) {
      super(Text.translatable("experiments_screen.title"));
      this.parent = parent;
      this.resourcePackManager = resourcePackManager;
      this.applier = applier;
      Iterator var4 = resourcePackManager.getProfiles().iterator();

      while(var4.hasNext()) {
         ResourcePackProfile lv = (ResourcePackProfile)var4.next();
         if (lv.getSource() == ResourcePackSource.FEATURE) {
            this.experiments.put(lv, resourcePackManager.getEnabledProfiles().contains(lv));
         }
      }

   }

   protected void init() {
      this.experimentToggleList.addHeader(new TextWidget(Text.translatable("selectWorld.experiments"), this.textRenderer));
      GridWidget.Adder lv = ((GridWidget)this.experimentToggleList.addFooter(new GridWidget())).createAdder(1);
      lv.add((new MultilineTextWidget(Text.translatable("selectWorld.experiments.info").formatted(Formatting.RED), this.textRenderer)).setMaxWidth(310), lv.copyPositioner().marginBottom(15));
      WorldScreenOptionGrid.Builder lv2 = WorldScreenOptionGrid.builder(310).withTooltipBox(2, true).setRowSpacing(4);
      this.experiments.forEach((pack, enabled) -> {
         lv2.add(getDataPackName(pack), () -> {
            return this.experiments.getBoolean(pack);
         }, (enabledx) -> {
            this.experiments.put(pack, enabledx);
         }).tooltip(pack.getDescription());
      });
      Objects.requireNonNull(lv);
      lv2.build(lv::add);
      GridWidget.Adder lv3 = ((GridWidget)this.experimentToggleList.addBody((new GridWidget()).setColumnSpacing(10))).createAdder(2);
      lv3.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.applyAndClose();
      }).build());
      lv3.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.close();
      }).build());
      this.experimentToggleList.forEachChild((widget) -> {
         ClickableWidget var10000 = (ClickableWidget)this.addDrawableChild(widget);
      });
      this.initTabNavigation();
   }

   private static Text getDataPackName(ResourcePackProfile packProfile) {
      String string = "dataPack." + packProfile.getName() + ".name";
      return (Text)(I18n.hasTranslation(string) ? Text.translatable(string) : packProfile.getDisplayName());
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   private void applyAndClose() {
      List list = new ArrayList(this.resourcePackManager.getEnabledProfiles());
      List list2 = new ArrayList();
      this.experiments.forEach((pack, enabled) -> {
         list.remove(pack);
         if (enabled) {
            list2.add(pack);
         }

      });
      list.addAll(Lists.reverse(list2));
      this.resourcePackManager.setEnabledProfiles(list.stream().map(ResourcePackProfile::getName).toList());
      this.applier.accept(this.resourcePackManager);
   }

   protected void initTabNavigation() {
      this.experimentToggleList.refreshPositions();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
      RenderSystem.setShaderColor(0.125F, 0.125F, 0.125F, 1.0F);
      int k = true;
      drawTexture(matrices, 0, this.experimentToggleList.getHeaderHeight(), 0.0F, 0.0F, this.width, this.height - this.experimentToggleList.getHeaderHeight() - this.experimentToggleList.getFooterHeight(), 32, 32);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
