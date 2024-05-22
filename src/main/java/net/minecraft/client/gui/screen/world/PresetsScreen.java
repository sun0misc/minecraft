/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.world;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CustomizeFlatLevelScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FlatLevelGeneratorPresetTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.structure.StructureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.FlatLevelGeneratorPreset;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PresetsScreen
extends Screen {
    static final Identifier SLOT_TEXTURE = Identifier.method_60656("container/slot");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int ICON_SIZE = 18;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ICON_BACKGROUND_OFFSET_X = 1;
    private static final int ICON_BACKGROUND_OFFSET_Y = 1;
    private static final int ICON_OFFSET_X = 2;
    private static final int ICON_OFFSET_Y = 2;
    private static final RegistryKey<Biome> BIOME_KEY = BiomeKeys.PLAINS;
    public static final Text UNKNOWN_PRESET_TEXT = Text.translatable("flat_world_preset.unknown");
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
    private static FlatChunkGeneratorLayer parseLayerString(RegistryEntryLookup<Block> blockLookup, String layer, int layerStartHeight) {
        Optional<RegistryEntry.Reference<Block>> optional;
        int j;
        String string2;
        List<String> list = Splitter.on('*').limit(2).splitToList(layer);
        if (list.size() == 2) {
            string2 = list.get(1);
            try {
                j = Math.max(Integer.parseInt(list.get(0)), 0);
            } catch (NumberFormatException numberFormatException) {
                LOGGER.error("Error while parsing flat world string", numberFormatException);
                return null;
            }
        } else {
            string2 = list.get(0);
            j = 1;
        }
        int k = Math.min(layerStartHeight + j, DimensionType.MAX_HEIGHT);
        int l = k - layerStartHeight;
        try {
            optional = blockLookup.getOptional(RegistryKey.of(RegistryKeys.BLOCK, Identifier.method_60654(string2)));
        } catch (Exception exception) {
            LOGGER.error("Error while parsing flat world string", exception);
            return null;
        }
        if (optional.isEmpty()) {
            LOGGER.error("Error while parsing flat world string => Unknown block, {}", (Object)string2);
            return null;
        }
        return new FlatChunkGeneratorLayer(l, optional.get().value());
    }

    private static List<FlatChunkGeneratorLayer> parsePresetLayersString(RegistryEntryLookup<Block> blockLookup, String layers) {
        ArrayList<FlatChunkGeneratorLayer> list = Lists.newArrayList();
        String[] strings = layers.split(",");
        int i = 0;
        for (String string2 : strings) {
            FlatChunkGeneratorLayer lv = PresetsScreen.parseLayerString(blockLookup, string2, i);
            if (lv == null) {
                return Collections.emptyList();
            }
            list.add(lv);
            i += lv.getThickness();
        }
        return list;
    }

    public static FlatChunkGeneratorConfig parsePresetString(RegistryEntryLookup<Block> blockLookup, RegistryEntryLookup<Biome> biomeLookup, RegistryEntryLookup<StructureSet> structureSetLookup, RegistryEntryLookup<PlacedFeature> placedFeatureLookup, String preset, FlatChunkGeneratorConfig config) {
        RegistryEntry.Reference<Biome> lv;
        Iterator<String> iterator = Splitter.on(';').split(preset).iterator();
        if (!iterator.hasNext()) {
            return FlatChunkGeneratorConfig.getDefaultConfig(biomeLookup, structureSetLookup, placedFeatureLookup);
        }
        List<FlatChunkGeneratorLayer> list = PresetsScreen.parsePresetLayersString(blockLookup, iterator.next());
        if (list.isEmpty()) {
            return FlatChunkGeneratorConfig.getDefaultConfig(biomeLookup, structureSetLookup, placedFeatureLookup);
        }
        RegistryEntry<Biome> lv2 = lv = biomeLookup.getOrThrow(BIOME_KEY);
        if (iterator.hasNext()) {
            String string2 = iterator.next();
            lv2 = Optional.ofNullable(Identifier.tryParse(string2)).map(biomeId -> RegistryKey.of(RegistryKeys.BIOME, biomeId)).flatMap(biomeLookup::getOptional).orElseGet(() -> {
                LOGGER.warn("Invalid biome: {}", (Object)string2);
                return lv;
            });
        }
        return config.with(list, config.getStructureOverrides(), lv2);
    }

    static String getGeneratorConfigString(FlatChunkGeneratorConfig config) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < config.getLayers().size(); ++i) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(config.getLayers().get(i));
        }
        stringBuilder.append(";");
        stringBuilder.append(config.getBiome().getKey().map(RegistryKey::getValue).orElseThrow(() -> new IllegalStateException("Biome not registered")));
        return stringBuilder.toString();
    }

    @Override
    protected void init() {
        this.shareText = Text.translatable("createWorld.customize.presets.share");
        this.listText = Text.translatable("createWorld.customize.presets.list");
        this.customPresetField = new TextFieldWidget(this.textRenderer, 50, 40, this.width - 100, 20, this.shareText);
        this.customPresetField.setMaxLength(1230);
        GeneratorOptionsHolder lv = this.parent.parent.getWorldCreator().getGeneratorOptionsHolder();
        DynamicRegistryManager.Immutable lv2 = lv.getCombinedRegistryManager();
        FeatureSet lv3 = lv.dataConfiguration().enabledFeatures();
        RegistryWrapper.Impl<Biome> lv4 = lv2.getWrapperOrThrow(RegistryKeys.BIOME);
        RegistryWrapper.Impl<StructureSet> lv5 = lv2.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET);
        RegistryWrapper.Impl<PlacedFeature> lv6 = lv2.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE);
        RegistryWrapper.Impl<Block> lv7 = lv2.getWrapperOrThrow(RegistryKeys.BLOCK).withFeatureFilter(lv3);
        this.customPresetField.setText(PresetsScreen.getGeneratorConfigString(this.parent.getConfig()));
        this.config = this.parent.getConfig();
        this.addSelectableChild(this.customPresetField);
        this.listWidget = this.addDrawableChild(new SuperflatPresetsListWidget(lv2, lv3));
        this.selectPresetButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("createWorld.customize.presets.select"), arg5 -> {
            FlatChunkGeneratorConfig lv = PresetsScreen.parsePresetString(lv7, lv4, lv5, lv6, this.customPresetField.getText(), this.config);
            this.parent.setConfig(lv);
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.client.setScreen(this.parent)).dimensions(this.width / 2 + 5, this.height - 28, 150, 20).build());
        this.updateSelectButton(this.listWidget.getSelectedOrNull() != null);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.listWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.customPresetField.getText();
        this.init(client, width, height);
        this.customPresetField.setText(string);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 400.0f);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, this.shareText, 51, 30, 0xA0A0A0);
        context.drawTextWithShadow(this.textRenderer, this.listText, 51, 68, 0xA0A0A0);
        context.getMatrices().pop();
        this.customPresetField.render(context, mouseX, mouseY, delta);
    }

    public void updateSelectButton(boolean hasSelected) {
        this.selectPresetButton.active = hasSelected || this.customPresetField.getText().length() > 1;
    }

    @Environment(value=EnvType.CLIENT)
    class SuperflatPresetsListWidget
    extends AlwaysSelectedEntryListWidget<SuperflatPresetEntry> {
        public SuperflatPresetsListWidget(DynamicRegistryManager dynamicRegistryManager, FeatureSet featureSet) {
            super(PresetsScreen.this.client, PresetsScreen.this.width, PresetsScreen.this.height - 117, 80, 24);
            for (RegistryEntry<FlatLevelGeneratorPreset> lv : dynamicRegistryManager.get(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET).iterateEntries(FlatLevelGeneratorPresetTags.VISIBLE)) {
                Set set = lv.value().settings().getLayers().stream().map(layer -> layer.getBlockState().getBlock()).filter(block -> !block.isEnabled(featureSet)).collect(Collectors.toSet());
                if (!set.isEmpty()) {
                    LOGGER.info("Discarding flat world preset {} since it contains experimental blocks {}", (Object)lv.getKey().map(key -> key.getValue().toString()).orElse("<unknown>"), (Object)set);
                    continue;
                }
                this.addEntry(new SuperflatPresetEntry(lv));
            }
        }

        @Override
        public void setSelected(@Nullable SuperflatPresetEntry arg) {
            super.setSelected(arg);
            PresetsScreen.this.updateSelectButton(arg != null);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (super.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (KeyCodes.isToggle(keyCode) && this.getSelectedOrNull() != null) {
                ((SuperflatPresetEntry)this.getSelectedOrNull()).setPreset();
            }
            return false;
        }

        @Environment(value=EnvType.CLIENT)
        public class SuperflatPresetEntry
        extends AlwaysSelectedEntryListWidget.Entry<SuperflatPresetEntry> {
            private static final Identifier STATS_ICONS_TEXTURE = Identifier.method_60656("textures/gui/container/stats_icons.png");
            private final FlatLevelGeneratorPreset preset;
            private final Text text;

            public SuperflatPresetEntry(RegistryEntry<FlatLevelGeneratorPreset> preset) {
                this.preset = preset.value();
                this.text = preset.getKey().map(key -> Text.translatable(key.getValue().toTranslationKey("flat_world_preset"))).orElse(UNKNOWN_PRESET_TEXT);
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                this.renderIcon(context, x, y, this.preset.displayItem().value());
                context.drawText(PresetsScreen.this.textRenderer, this.text, x + 18 + 5, y + 6, 0xFFFFFF, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.setPreset();
                return super.mouseClicked(mouseX, mouseY, button);
            }

            void setPreset() {
                SuperflatPresetsListWidget.this.setSelected(this);
                PresetsScreen.this.config = this.preset.settings();
                PresetsScreen.this.customPresetField.setText(PresetsScreen.getGeneratorConfigString(PresetsScreen.this.config));
                PresetsScreen.this.customPresetField.setCursorToStart(false);
            }

            private void renderIcon(DrawContext context, int x, int y, Item iconItem) {
                this.drawIconBackground(context, x + 1, y + 1);
                context.drawItemWithoutEntity(new ItemStack(iconItem), x + 2, y + 2);
            }

            private void drawIconBackground(DrawContext context, int x, int y) {
                context.drawGuiTexture(SLOT_TEXTURE, x, y, 0, 18, 18);
            }

            @Override
            public Text getNarration() {
                return Text.translatable("narrator.select", this.text);
            }
        }
    }
}

