package net.minecraft.client.gui.screen;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FlatLevelGeneratorPresetTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.FlatLevelGeneratorPreset;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class PresetsScreen extends Screen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int ICON_TEXTURE_SIZE = 128;
   private static final int ICON_SIZE = 18;
   private static final int BUTTON_HEIGHT = 20;
   private static final int ICON_BACKGROUND_OFFSET_X = 1;
   private static final int ICON_BACKGROUND_OFFSET_Y = 1;
   private static final int ICON_OFFSET_X = 2;
   private static final int ICON_OFFSET_Y = 2;
   private static final RegistryKey BIOME_KEY;
   public static final Text UNKNOWN_PRESET_TEXT;
   private final CustomizeFlatLevelScreen parent;
   private Text shareText;
   private Text listText;
   private SuperflatPresetsListWidget listWidget;
   private ButtonWidget selectPresetButton;
   TextFieldWidget customPresetField;
   FlatChunkGeneratorConfig config;

   public PresetsScreen(CustomizeFlatLevelScreen parent) {
      super(Text.translatable("createWorld.customize.presets.title"));
      this.parent = parent;
   }

   @Nullable
   private static FlatChunkGeneratorLayer parseLayerString(RegistryEntryLookup blockLookup, String layer, int layerStartHeight) {
      List list = Splitter.on('*').limit(2).splitToList(layer);
      int j;
      String string2;
      if (list.size() == 2) {
         string2 = (String)list.get(1);

         try {
            j = Math.max(Integer.parseInt((String)list.get(0)), 0);
         } catch (NumberFormatException var11) {
            LOGGER.error("Error while parsing flat world string", var11);
            return null;
         }
      } else {
         string2 = (String)list.get(0);
         j = 1;
      }

      int k = Math.min(layerStartHeight + j, DimensionType.MAX_HEIGHT);
      int l = k - layerStartHeight;

      Optional optional;
      try {
         optional = blockLookup.getOptional(RegistryKey.of(RegistryKeys.BLOCK, new Identifier(string2)));
      } catch (Exception var10) {
         LOGGER.error("Error while parsing flat world string", var10);
         return null;
      }

      if (optional.isEmpty()) {
         LOGGER.error("Error while parsing flat world string => Unknown block, {}", string2);
         return null;
      } else {
         return new FlatChunkGeneratorLayer(l, (Block)((RegistryEntry.Reference)optional.get()).value());
      }
   }

   private static List parsePresetLayersString(RegistryEntryLookup blockLookup, String layers) {
      List list = Lists.newArrayList();
      String[] strings = layers.split(",");
      int i = 0;
      String[] var5 = strings;
      int var6 = strings.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         String string2 = var5[var7];
         FlatChunkGeneratorLayer lv = parseLayerString(blockLookup, string2, i);
         if (lv == null) {
            return Collections.emptyList();
         }

         list.add(lv);
         i += lv.getThickness();
      }

      return list;
   }

   public static FlatChunkGeneratorConfig parsePresetString(RegistryEntryLookup blockLookup, RegistryEntryLookup biomeLookup, RegistryEntryLookup structureSetLookup, RegistryEntryLookup placedFeatureLookup, String preset, FlatChunkGeneratorConfig config) {
      Iterator iterator = Splitter.on(';').split(preset).iterator();
      if (!iterator.hasNext()) {
         return FlatChunkGeneratorConfig.getDefaultConfig(biomeLookup, structureSetLookup, placedFeatureLookup);
      } else {
         List list = parsePresetLayersString(blockLookup, (String)iterator.next());
         if (list.isEmpty()) {
            return FlatChunkGeneratorConfig.getDefaultConfig(biomeLookup, structureSetLookup, placedFeatureLookup);
         } else {
            RegistryEntry.Reference lv = biomeLookup.getOrThrow(BIOME_KEY);
            RegistryEntry lv2 = lv;
            if (iterator.hasNext()) {
               String string2 = (String)iterator.next();
               Optional var10000 = Optional.ofNullable(Identifier.tryParse(string2)).map((biomeId) -> {
                  return RegistryKey.of(RegistryKeys.BIOME, biomeId);
               });
               Objects.requireNonNull(biomeLookup);
               lv2 = (RegistryEntry)var10000.flatMap(biomeLookup::getOptional).orElseGet(() -> {
                  LOGGER.warn("Invalid biome: {}", string2);
                  return lv;
               });
            }

            return config.with(list, config.getStructureOverrides(), (RegistryEntry)lv2);
         }
      }
   }

   static String getGeneratorConfigString(FlatChunkGeneratorConfig config) {
      StringBuilder stringBuilder = new StringBuilder();

      for(int i = 0; i < config.getLayers().size(); ++i) {
         if (i > 0) {
            stringBuilder.append(",");
         }

         stringBuilder.append(config.getLayers().get(i));
      }

      stringBuilder.append(";");
      stringBuilder.append(config.getBiome().getKey().map(RegistryKey::getValue).orElseThrow(() -> {
         return new IllegalStateException("Biome not registered");
      }));
      return stringBuilder.toString();
   }

   protected void init() {
      this.shareText = Text.translatable("createWorld.customize.presets.share");
      this.listText = Text.translatable("createWorld.customize.presets.list");
      this.customPresetField = new TextFieldWidget(this.textRenderer, 50, 40, this.width - 100, 20, this.shareText);
      this.customPresetField.setMaxLength(1230);
      GeneratorOptionsHolder lv = this.parent.parent.getWorldCreator().getGeneratorOptionsHolder();
      DynamicRegistryManager lv2 = lv.getCombinedRegistryManager();
      FeatureSet lv3 = lv.dataConfiguration().enabledFeatures();
      RegistryEntryLookup lv4 = lv2.getWrapperOrThrow(RegistryKeys.BIOME);
      RegistryEntryLookup lv5 = lv2.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET);
      RegistryEntryLookup lv6 = lv2.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE);
      RegistryEntryLookup lv7 = lv2.getWrapperOrThrow(RegistryKeys.BLOCK).withFeatureFilter(lv3);
      this.customPresetField.setText(getGeneratorConfigString(this.parent.getConfig()));
      this.config = this.parent.getConfig();
      this.addSelectableChild(this.customPresetField);
      this.listWidget = new SuperflatPresetsListWidget(lv2, lv3);
      this.addSelectableChild(this.listWidget);
      this.selectPresetButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("createWorld.customize.presets.select"), (arg5) -> {
         FlatChunkGeneratorConfig lv = parsePresetString(lv7, lv4, lv5, lv6, this.customPresetField.getText(), this.config);
         this.parent.setConfig(lv);
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 155, this.height - 28, 150, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 + 5, this.height - 28, 150, 20).build());
      this.updateSelectButton(this.listWidget.getSelectedOrNull() != null);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.listWidget.mouseScrolled(mouseX, mouseY, amount);
   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.customPresetField.getText();
      this.init(client, width, height);
      this.customPresetField.setText(string);
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.listWidget.render(matrices, mouseX, mouseY, delta);
      matrices.push();
      matrices.translate(0.0F, 0.0F, 400.0F);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
      drawTextWithShadow(matrices, this.textRenderer, this.shareText, 50, 30, 10526880);
      drawTextWithShadow(matrices, this.textRenderer, this.listText, 50, 70, 10526880);
      matrices.pop();
      this.customPresetField.render(matrices, mouseX, mouseY, delta);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public void tick() {
      this.customPresetField.tick();
      super.tick();
   }

   public void updateSelectButton(boolean hasSelected) {
      this.selectPresetButton.active = hasSelected || this.customPresetField.getText().length() > 1;
   }

   static {
      BIOME_KEY = BiomeKeys.PLAINS;
      UNKNOWN_PRESET_TEXT = Text.translatable("flat_world_preset.unknown");
   }

   @Environment(EnvType.CLIENT)
   private class SuperflatPresetsListWidget extends AlwaysSelectedEntryListWidget {
      public SuperflatPresetsListWidget(DynamicRegistryManager dynamicRegistryManager, FeatureSet featureSet) {
         super(PresetsScreen.this.client, PresetsScreen.this.width, PresetsScreen.this.height, 80, PresetsScreen.this.height - 37, 24);
         Iterator var4 = dynamicRegistryManager.get(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET).iterateEntries(FlatLevelGeneratorPresetTags.VISIBLE).iterator();

         while(var4.hasNext()) {
            RegistryEntry lv = (RegistryEntry)var4.next();
            Set set = (Set)((FlatLevelGeneratorPreset)lv.value()).settings().getLayers().stream().map((layer) -> {
               return layer.getBlockState().getBlock();
            }).filter((block) -> {
               return !block.isEnabled(featureSet);
            }).collect(Collectors.toSet());
            if (!set.isEmpty()) {
               PresetsScreen.LOGGER.info("Discarding flat world preset {} since it contains experimental blocks {}", lv.getKey().map((key) -> {
                  return key.getValue().toString();
               }).orElse("<unknown>"), set);
            } else {
               this.addEntry(new SuperflatPresetEntry(lv));
            }
         }

      }

      public void setSelected(@Nullable SuperflatPresetEntry arg) {
         super.setSelected(arg);
         PresetsScreen.this.updateSelectButton(arg != null);
      }

      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
         } else {
            if (KeyCodes.isToggle(keyCode) && this.getSelectedOrNull() != null) {
               ((SuperflatPresetEntry)this.getSelectedOrNull()).setPreset();
            }

            return false;
         }
      }

      @Environment(EnvType.CLIENT)
      public class SuperflatPresetEntry extends AlwaysSelectedEntryListWidget.Entry {
         private final FlatLevelGeneratorPreset preset;
         private final Text text;

         public SuperflatPresetEntry(RegistryEntry preset) {
            this.preset = (FlatLevelGeneratorPreset)preset.value();
            this.text = (Text)preset.getKey().map((key) -> {
               return Text.translatable(key.getValue().toTranslationKey("flat_world_preset"));
            }).orElse(PresetsScreen.UNKNOWN_PRESET_TEXT);
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.renderIcon(matrices, x, y, (Item)this.preset.displayItem().value());
            PresetsScreen.this.textRenderer.draw(matrices, this.text, (float)(x + 18 + 5), (float)(y + 6), 16777215);
         }

         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
               this.setPreset();
            }

            return false;
         }

         void setPreset() {
            SuperflatPresetsListWidget.this.setSelected(this);
            PresetsScreen.this.config = this.preset.settings();
            PresetsScreen.this.customPresetField.setText(PresetsScreen.getGeneratorConfigString(PresetsScreen.this.config));
            PresetsScreen.this.customPresetField.setCursorToStart();
         }

         private void renderIcon(MatrixStack matrices, int x, int y, Item iconItem) {
            this.drawIconBackground(matrices, x + 1, y + 1);
            PresetsScreen.this.itemRenderer.renderGuiItemIcon(matrices, new ItemStack(iconItem), x + 2, y + 2);
         }

         private void drawIconBackground(MatrixStack matrices, int x, int y) {
            RenderSystem.setShaderTexture(0, DrawableHelper.STATS_ICON_TEXTURE);
            DrawableHelper.drawTexture(matrices, x, y, 0, 0.0F, 0.0F, 18, 18, 128, 128);
         }

         public Text getNarration() {
            return Text.translatable("narrator.select", this.text);
         }
      }
   }
}
