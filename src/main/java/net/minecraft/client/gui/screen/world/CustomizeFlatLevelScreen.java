/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.world;

import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.PresetsScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CustomizeFlatLevelScreen
extends Screen {
    static final Identifier SLOT_TEXTURE = Identifier.method_60656("container/slot");
    private static final int ICON_SIZE = 18;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ICON_BACKGROUND_OFFSET_X = 1;
    private static final int ICON_BACKGROUND_OFFSET_Y = 1;
    private static final int ICON_OFFSET_X = 2;
    private static final int ICON_OFFSET_Y = 2;
    protected final CreateWorldScreen parent;
    private final Consumer<FlatChunkGeneratorConfig> configConsumer;
    FlatChunkGeneratorConfig config;
    private Text tileText;
    private Text heightText;
    private SuperflatLayersListWidget layers;
    private ButtonWidget widgetButtonRemoveLayer;

    public CustomizeFlatLevelScreen(CreateWorldScreen parent, Consumer<FlatChunkGeneratorConfig> configConsumer, FlatChunkGeneratorConfig config) {
        super(Text.translatable("createWorld.customize.flat.title"));
        this.parent = parent;
        this.configConsumer = configConsumer;
        this.config = config;
    }

    public FlatChunkGeneratorConfig getConfig() {
        return this.config;
    }

    public void setConfig(FlatChunkGeneratorConfig config) {
        this.config = config;
    }

    @Override
    protected void init() {
        this.tileText = Text.translatable("createWorld.customize.flat.tile");
        this.heightText = Text.translatable("createWorld.customize.flat.height");
        this.layers = this.addDrawableChild(new SuperflatLayersListWidget());
        this.widgetButtonRemoveLayer = this.addDrawableChild(ButtonWidget.builder(Text.translatable("createWorld.customize.flat.removeLayer"), button -> {
            if (!this.hasLayerSelected()) {
                return;
            }
            List<FlatChunkGeneratorLayer> list = this.config.getLayers();
            int i = this.layers.children().indexOf(this.layers.getSelectedOrNull());
            int j = list.size() - i - 1;
            list.remove(j);
            this.layers.setSelected(list.isEmpty() ? null : (SuperflatLayersListWidget.SuperflatLayerEntry)this.layers.children().get(Math.min(i, list.size() - 1)));
            this.config.updateLayerBlocks();
            this.layers.updateLayers();
            this.updateRemoveLayerButton();
        }).dimensions(this.width / 2 - 155, this.height - 52, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("createWorld.customize.presets"), button -> {
            this.client.setScreen(new PresetsScreen(this));
            this.config.updateLayerBlocks();
            this.updateRemoveLayerButton();
        }).dimensions(this.width / 2 + 5, this.height - 52, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            this.configConsumer.accept(this.config);
            this.client.setScreen(this.parent);
            this.config.updateLayerBlocks();
        }).dimensions(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            this.client.setScreen(this.parent);
            this.config.updateLayerBlocks();
        }).dimensions(this.width / 2 + 5, this.height - 28, 150, 20).build());
        this.config.updateLayerBlocks();
        this.updateRemoveLayerButton();
    }

    void updateRemoveLayerButton() {
        this.widgetButtonRemoveLayer.active = this.hasLayerSelected();
    }

    private boolean hasLayerSelected() {
        return this.layers.getSelectedOrNull() != null;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        int k = this.width / 2 - 92 - 16;
        context.drawTextWithShadow(this.textRenderer, this.tileText, k, 32, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, this.heightText, k + 2 + 213 - this.textRenderer.getWidth(this.heightText), 32, 0xFFFFFF);
    }

    @Environment(value=EnvType.CLIENT)
    class SuperflatLayersListWidget
    extends AlwaysSelectedEntryListWidget<SuperflatLayerEntry> {
        public SuperflatLayersListWidget() {
            super(CustomizeFlatLevelScreen.this.client, CustomizeFlatLevelScreen.this.width, CustomizeFlatLevelScreen.this.height - 103, 43, 24);
            for (int i = 0; i < CustomizeFlatLevelScreen.this.config.getLayers().size(); ++i) {
                this.addEntry(new SuperflatLayerEntry());
            }
        }

        @Override
        public void setSelected(@Nullable SuperflatLayerEntry arg) {
            super.setSelected(arg);
            CustomizeFlatLevelScreen.this.updateRemoveLayerButton();
        }

        public void updateLayers() {
            int i = this.children().indexOf(this.getSelectedOrNull());
            this.clearEntries();
            for (int j = 0; j < CustomizeFlatLevelScreen.this.config.getLayers().size(); ++j) {
                this.addEntry(new SuperflatLayerEntry());
            }
            List list = this.children();
            if (i >= 0 && i < list.size()) {
                this.setSelected((SuperflatLayerEntry)list.get(i));
            }
        }

        @Environment(value=EnvType.CLIENT)
        class SuperflatLayerEntry
        extends AlwaysSelectedEntryListWidget.Entry<SuperflatLayerEntry> {
            SuperflatLayerEntry() {
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                FlatChunkGeneratorLayer lv = CustomizeFlatLevelScreen.this.config.getLayers().get(CustomizeFlatLevelScreen.this.config.getLayers().size() - index - 1);
                BlockState lv2 = lv.getBlockState();
                ItemStack lv3 = this.createItemStackFor(lv2);
                this.renderIcon(context, x, y, lv3);
                context.drawText(CustomizeFlatLevelScreen.this.textRenderer, lv3.getName(), x + 18 + 5, y + 3, 0xFFFFFF, false);
                MutableText lv4 = index == 0 ? Text.translatable("createWorld.customize.flat.layer.top", lv.getThickness()) : (index == CustomizeFlatLevelScreen.this.config.getLayers().size() - 1 ? Text.translatable("createWorld.customize.flat.layer.bottom", lv.getThickness()) : Text.translatable("createWorld.customize.flat.layer", lv.getThickness()));
                context.drawText(CustomizeFlatLevelScreen.this.textRenderer, lv4, x + 2 + 213 - CustomizeFlatLevelScreen.this.textRenderer.getWidth(lv4), y + 3, 0xFFFFFF, false);
            }

            private ItemStack createItemStackFor(BlockState state) {
                Item lv = state.getBlock().asItem();
                if (lv == Items.AIR) {
                    if (state.isOf(Blocks.WATER)) {
                        lv = Items.WATER_BUCKET;
                    } else if (state.isOf(Blocks.LAVA)) {
                        lv = Items.LAVA_BUCKET;
                    }
                }
                return new ItemStack(lv);
            }

            @Override
            public Text getNarration() {
                FlatChunkGeneratorLayer lv = CustomizeFlatLevelScreen.this.config.getLayers().get(CustomizeFlatLevelScreen.this.config.getLayers().size() - SuperflatLayersListWidget.this.children().indexOf(this) - 1);
                ItemStack lv2 = this.createItemStackFor(lv.getBlockState());
                if (!lv2.isEmpty()) {
                    return Text.translatable("narrator.select", lv2.getName());
                }
                return ScreenTexts.EMPTY;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                SuperflatLayersListWidget.this.setSelected(this);
                return super.mouseClicked(mouseX, mouseY, button);
            }

            private void renderIcon(DrawContext context, int x, int y, ItemStack iconItem) {
                this.renderIconBackgroundTexture(context, x + 1, y + 1);
                if (!iconItem.isEmpty()) {
                    context.drawItemWithoutEntity(iconItem, x + 2, y + 2);
                }
            }

            private void renderIconBackgroundTexture(DrawContext context, int x, int y) {
                context.drawGuiTexture(SLOT_TEXTURE, x, y, 0, 18, 18);
            }
        }
    }
}

