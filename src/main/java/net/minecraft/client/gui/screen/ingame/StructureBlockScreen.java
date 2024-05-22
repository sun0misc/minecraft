/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.ImmutableList;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class StructureBlockScreen
extends Screen {
    private static final Text STRUCTURE_NAME_TEXT = Text.translatable("structure_block.structure_name");
    private static final Text POSITION_TEXT = Text.translatable("structure_block.position");
    private static final Text SIZE_TEXT = Text.translatable("structure_block.size");
    private static final Text INTEGRITY_TEXT = Text.translatable("structure_block.integrity");
    private static final Text CUSTOM_DATA_TEXT = Text.translatable("structure_block.custom_data");
    private static final Text INCLUDE_ENTITIES_TEXT = Text.translatable("structure_block.include_entities");
    private static final Text DETECT_SIZE_TEXT = Text.translatable("structure_block.detect_size");
    private static final Text SHOW_AIR_TEXT = Text.translatable("structure_block.show_air");
    private static final Text SHOW_BOUNDING_BOX_TEXT = Text.translatable("structure_block.show_boundingbox");
    private static final ImmutableList<StructureBlockMode> MODES = ImmutableList.copyOf(StructureBlockMode.values());
    private static final ImmutableList<StructureBlockMode> MODES_EXCEPT_DATA = MODES.stream().filter(mode -> mode != StructureBlockMode.DATA).collect(ImmutableList.toImmutableList());
    private final StructureBlockBlockEntity structureBlock;
    private BlockMirror mirror = BlockMirror.NONE;
    private BlockRotation rotation = BlockRotation.NONE;
    private StructureBlockMode mode = StructureBlockMode.DATA;
    private boolean ignoreEntities;
    private boolean showAir;
    private boolean showBoundingBox;
    private TextFieldWidget inputName;
    private TextFieldWidget inputPosX;
    private TextFieldWidget inputPosY;
    private TextFieldWidget inputPosZ;
    private TextFieldWidget inputSizeX;
    private TextFieldWidget inputSizeY;
    private TextFieldWidget inputSizeZ;
    private TextFieldWidget inputIntegrity;
    private TextFieldWidget inputSeed;
    private TextFieldWidget inputMetadata;
    private ButtonWidget buttonSave;
    private ButtonWidget buttonLoad;
    private ButtonWidget buttonRotate0;
    private ButtonWidget buttonRotate90;
    private ButtonWidget buttonRotate180;
    private ButtonWidget buttonRotate270;
    private ButtonWidget buttonDetect;
    private CyclingButtonWidget<Boolean> buttonEntities;
    private CyclingButtonWidget<BlockMirror> buttonMirror;
    private CyclingButtonWidget<Boolean> buttonShowAir;
    private CyclingButtonWidget<Boolean> buttonShowBoundingBox;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");

    public StructureBlockScreen(StructureBlockBlockEntity structureBlock) {
        super(Text.translatable(Blocks.STRUCTURE_BLOCK.getTranslationKey()));
        this.structureBlock = structureBlock;
        this.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    private void done() {
        if (this.updateStructureBlock(StructureBlockBlockEntity.Action.UPDATE_DATA)) {
            this.client.setScreen(null);
        }
    }

    private void cancel() {
        this.structureBlock.setMirror(this.mirror);
        this.structureBlock.setRotation(this.rotation);
        this.structureBlock.setMode(this.mode);
        this.structureBlock.setIgnoreEntities(this.ignoreEntities);
        this.structureBlock.setShowAir(this.showAir);
        this.structureBlock.setShowBoundingBox(this.showBoundingBox);
        this.client.setScreen(null);
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());
        this.mirror = this.structureBlock.getMirror();
        this.rotation = this.structureBlock.getRotation();
        this.mode = this.structureBlock.getMode();
        this.ignoreEntities = this.structureBlock.shouldIgnoreEntities();
        this.showAir = this.structureBlock.shouldShowAir();
        this.showBoundingBox = this.structureBlock.shouldShowBoundingBox();
        this.buttonSave = this.addDrawableChild(ButtonWidget.builder(Text.translatable("structure_block.button.save"), button -> {
            if (this.structureBlock.getMode() == StructureBlockMode.SAVE) {
                this.updateStructureBlock(StructureBlockBlockEntity.Action.SAVE_AREA);
                this.client.setScreen(null);
            }
        }).dimensions(this.width / 2 + 4 + 100, 185, 50, 20).build());
        this.buttonLoad = this.addDrawableChild(ButtonWidget.builder(Text.translatable("structure_block.button.load"), button -> {
            if (this.structureBlock.getMode() == StructureBlockMode.LOAD) {
                this.updateStructureBlock(StructureBlockBlockEntity.Action.LOAD_AREA);
                this.client.setScreen(null);
            }
        }).dimensions(this.width / 2 + 4 + 100, 185, 50, 20).build());
        this.addDrawableChild(CyclingButtonWidget.builder(value -> Text.translatable("structure_block.mode." + value.asString())).values((List<StructureBlockMode>)MODES_EXCEPT_DATA, (List<StructureBlockMode>)MODES).omitKeyText().initially(this.mode).build(this.width / 2 - 4 - 150, 185, 50, 20, Text.literal("MODE"), (button, mode) -> {
            this.structureBlock.setMode((StructureBlockMode)mode);
            this.updateWidgets((StructureBlockMode)mode);
        }));
        this.buttonDetect = this.addDrawableChild(ButtonWidget.builder(Text.translatable("structure_block.button.detect_size"), button -> {
            if (this.structureBlock.getMode() == StructureBlockMode.SAVE) {
                this.updateStructureBlock(StructureBlockBlockEntity.Action.SCAN_AREA);
                this.client.setScreen(null);
            }
        }).dimensions(this.width / 2 + 4 + 100, 120, 50, 20).build());
        this.buttonEntities = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(!this.structureBlock.shouldIgnoreEntities()).omitKeyText().build(this.width / 2 + 4 + 100, 160, 50, 20, INCLUDE_ENTITIES_TEXT, (button, includeEntities) -> this.structureBlock.setIgnoreEntities(includeEntities == false)));
        this.buttonMirror = this.addDrawableChild(CyclingButtonWidget.builder(BlockMirror::getName).values((BlockMirror[])BlockMirror.values()).omitKeyText().initially(this.mirror).build(this.width / 2 - 20, 185, 40, 20, Text.literal("MIRROR"), (button, mirror) -> this.structureBlock.setMirror((BlockMirror)mirror)));
        this.buttonShowAir = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.structureBlock.shouldShowAir()).omitKeyText().build(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_AIR_TEXT, (button, showAir) -> this.structureBlock.setShowAir((boolean)showAir)));
        this.buttonShowBoundingBox = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.structureBlock.shouldShowBoundingBox()).omitKeyText().build(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_BOUNDING_BOX_TEXT, (button, showBoundingBox) -> this.structureBlock.setShowBoundingBox((boolean)showBoundingBox)));
        this.buttonRotate0 = this.addDrawableChild(ButtonWidget.builder(Text.literal("0"), button -> {
            this.structureBlock.setRotation(BlockRotation.NONE);
            this.updateRotationButton();
        }).dimensions(this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20).build());
        this.buttonRotate90 = this.addDrawableChild(ButtonWidget.builder(Text.literal("90"), button -> {
            this.structureBlock.setRotation(BlockRotation.CLOCKWISE_90);
            this.updateRotationButton();
        }).dimensions(this.width / 2 - 1 - 40 - 20, 185, 40, 20).build());
        this.buttonRotate180 = this.addDrawableChild(ButtonWidget.builder(Text.literal("180"), button -> {
            this.structureBlock.setRotation(BlockRotation.CLOCKWISE_180);
            this.updateRotationButton();
        }).dimensions(this.width / 2 + 1 + 20, 185, 40, 20).build());
        this.buttonRotate270 = this.addDrawableChild(ButtonWidget.builder(Text.literal("270"), button -> {
            this.structureBlock.setRotation(BlockRotation.COUNTERCLOCKWISE_90);
            this.updateRotationButton();
        }).dimensions(this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20).build());
        this.inputName = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 40, 300, 20, (Text)Text.translatable("structure_block.structure_name")){

            @Override
            public boolean charTyped(char chr, int modifiers) {
                if (!StructureBlockScreen.this.isValidCharacterForName(this.getText(), chr, this.getCursor())) {
                    return false;
                }
                return super.charTyped(chr, modifiers);
            }
        };
        this.inputName.setMaxLength(128);
        this.inputName.setText(this.structureBlock.getTemplateName());
        this.addSelectableChild(this.inputName);
        BlockPos lv = this.structureBlock.getOffset();
        this.inputPosX = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 80, 80, 20, Text.translatable("structure_block.position.x"));
        this.inputPosX.setMaxLength(15);
        this.inputPosX.setText(Integer.toString(lv.getX()));
        this.addSelectableChild(this.inputPosX);
        this.inputPosY = new TextFieldWidget(this.textRenderer, this.width / 2 - 72, 80, 80, 20, Text.translatable("structure_block.position.y"));
        this.inputPosY.setMaxLength(15);
        this.inputPosY.setText(Integer.toString(lv.getY()));
        this.addSelectableChild(this.inputPosY);
        this.inputPosZ = new TextFieldWidget(this.textRenderer, this.width / 2 + 8, 80, 80, 20, Text.translatable("structure_block.position.z"));
        this.inputPosZ.setMaxLength(15);
        this.inputPosZ.setText(Integer.toString(lv.getZ()));
        this.addSelectableChild(this.inputPosZ);
        Vec3i lv2 = this.structureBlock.getSize();
        this.inputSizeX = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 120, 80, 20, Text.translatable("structure_block.size.x"));
        this.inputSizeX.setMaxLength(15);
        this.inputSizeX.setText(Integer.toString(lv2.getX()));
        this.addSelectableChild(this.inputSizeX);
        this.inputSizeY = new TextFieldWidget(this.textRenderer, this.width / 2 - 72, 120, 80, 20, Text.translatable("structure_block.size.y"));
        this.inputSizeY.setMaxLength(15);
        this.inputSizeY.setText(Integer.toString(lv2.getY()));
        this.addSelectableChild(this.inputSizeY);
        this.inputSizeZ = new TextFieldWidget(this.textRenderer, this.width / 2 + 8, 120, 80, 20, Text.translatable("structure_block.size.z"));
        this.inputSizeZ.setMaxLength(15);
        this.inputSizeZ.setText(Integer.toString(lv2.getZ()));
        this.addSelectableChild(this.inputSizeZ);
        this.inputIntegrity = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 120, 80, 20, Text.translatable("structure_block.integrity.integrity"));
        this.inputIntegrity.setMaxLength(15);
        this.inputIntegrity.setText(this.decimalFormat.format(this.structureBlock.getIntegrity()));
        this.addSelectableChild(this.inputIntegrity);
        this.inputSeed = new TextFieldWidget(this.textRenderer, this.width / 2 - 72, 120, 80, 20, Text.translatable("structure_block.integrity.seed"));
        this.inputSeed.setMaxLength(31);
        this.inputSeed.setText(Long.toString(this.structureBlock.getSeed()));
        this.addSelectableChild(this.inputSeed);
        this.inputMetadata = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 120, 240, 20, Text.translatable("structure_block.custom_data"));
        this.inputMetadata.setMaxLength(128);
        this.inputMetadata.setText(this.structureBlock.getMetadata());
        this.addSelectableChild(this.inputMetadata);
        this.updateRotationButton();
        this.updateWidgets(this.mode);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.inputName);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.inputName.getText();
        String string2 = this.inputPosX.getText();
        String string3 = this.inputPosY.getText();
        String string4 = this.inputPosZ.getText();
        String string5 = this.inputSizeX.getText();
        String string6 = this.inputSizeY.getText();
        String string7 = this.inputSizeZ.getText();
        String string8 = this.inputIntegrity.getText();
        String string9 = this.inputSeed.getText();
        String string10 = this.inputMetadata.getText();
        this.init(client, width, height);
        this.inputName.setText(string);
        this.inputPosX.setText(string2);
        this.inputPosY.setText(string3);
        this.inputPosZ.setText(string4);
        this.inputSizeX.setText(string5);
        this.inputSizeY.setText(string6);
        this.inputSizeZ.setText(string7);
        this.inputIntegrity.setText(string8);
        this.inputSeed.setText(string9);
        this.inputMetadata.setText(string10);
    }

    private void updateRotationButton() {
        this.buttonRotate0.active = true;
        this.buttonRotate90.active = true;
        this.buttonRotate180.active = true;
        this.buttonRotate270.active = true;
        switch (this.structureBlock.getRotation()) {
            case NONE: {
                this.buttonRotate0.active = false;
                break;
            }
            case CLOCKWISE_180: {
                this.buttonRotate180.active = false;
                break;
            }
            case COUNTERCLOCKWISE_90: {
                this.buttonRotate270.active = false;
                break;
            }
            case CLOCKWISE_90: {
                this.buttonRotate90.active = false;
            }
        }
    }

    private void updateWidgets(StructureBlockMode mode) {
        this.inputName.setVisible(false);
        this.inputPosX.setVisible(false);
        this.inputPosY.setVisible(false);
        this.inputPosZ.setVisible(false);
        this.inputSizeX.setVisible(false);
        this.inputSizeY.setVisible(false);
        this.inputSizeZ.setVisible(false);
        this.inputIntegrity.setVisible(false);
        this.inputSeed.setVisible(false);
        this.inputMetadata.setVisible(false);
        this.buttonSave.visible = false;
        this.buttonLoad.visible = false;
        this.buttonDetect.visible = false;
        this.buttonEntities.visible = false;
        this.buttonMirror.visible = false;
        this.buttonRotate0.visible = false;
        this.buttonRotate90.visible = false;
        this.buttonRotate180.visible = false;
        this.buttonRotate270.visible = false;
        this.buttonShowAir.visible = false;
        this.buttonShowBoundingBox.visible = false;
        switch (mode) {
            case SAVE: {
                this.inputName.setVisible(true);
                this.inputPosX.setVisible(true);
                this.inputPosY.setVisible(true);
                this.inputPosZ.setVisible(true);
                this.inputSizeX.setVisible(true);
                this.inputSizeY.setVisible(true);
                this.inputSizeZ.setVisible(true);
                this.buttonSave.visible = true;
                this.buttonDetect.visible = true;
                this.buttonEntities.visible = true;
                this.buttonShowAir.visible = true;
                break;
            }
            case LOAD: {
                this.inputName.setVisible(true);
                this.inputPosX.setVisible(true);
                this.inputPosY.setVisible(true);
                this.inputPosZ.setVisible(true);
                this.inputIntegrity.setVisible(true);
                this.inputSeed.setVisible(true);
                this.buttonLoad.visible = true;
                this.buttonEntities.visible = true;
                this.buttonMirror.visible = true;
                this.buttonRotate0.visible = true;
                this.buttonRotate90.visible = true;
                this.buttonRotate180.visible = true;
                this.buttonRotate270.visible = true;
                this.buttonShowBoundingBox.visible = true;
                this.updateRotationButton();
                break;
            }
            case CORNER: {
                this.inputName.setVisible(true);
                break;
            }
            case DATA: {
                this.inputMetadata.setVisible(true);
            }
        }
    }

    private boolean updateStructureBlock(StructureBlockBlockEntity.Action action) {
        BlockPos lv = new BlockPos(this.parseInt(this.inputPosX.getText()), this.parseInt(this.inputPosY.getText()), this.parseInt(this.inputPosZ.getText()));
        Vec3i lv2 = new Vec3i(this.parseInt(this.inputSizeX.getText()), this.parseInt(this.inputSizeY.getText()), this.parseInt(this.inputSizeZ.getText()));
        float f = this.parseFloat(this.inputIntegrity.getText());
        long l = this.parseLong(this.inputSeed.getText());
        this.client.getNetworkHandler().sendPacket(new UpdateStructureBlockC2SPacket(this.structureBlock.getPos(), action, this.structureBlock.getMode(), this.inputName.getText(), lv, lv2, this.structureBlock.getMirror(), this.structureBlock.getRotation(), this.inputMetadata.getText(), this.structureBlock.shouldIgnoreEntities(), this.structureBlock.shouldShowAir(), this.structureBlock.shouldShowBoundingBox(), f, l));
        return true;
    }

    private long parseLong(String string) {
        try {
            return Long.valueOf(string);
        } catch (NumberFormatException numberFormatException) {
            return 0L;
        }
    }

    private float parseFloat(String string) {
        try {
            return Float.valueOf(string).floatValue();
        } catch (NumberFormatException numberFormatException) {
            return 1.0f;
        }
    }

    private int parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    @Override
    public void close() {
        this.cancel();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.done();
            return true;
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        StructureBlockMode lv = this.structureBlock.getMode();
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        if (lv != StructureBlockMode.DATA) {
            context.drawTextWithShadow(this.textRenderer, STRUCTURE_NAME_TEXT, this.width / 2 - 153, 30, 0xA0A0A0);
            this.inputName.render(context, mouseX, mouseY, delta);
        }
        if (lv == StructureBlockMode.LOAD || lv == StructureBlockMode.SAVE) {
            context.drawTextWithShadow(this.textRenderer, POSITION_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
            this.inputPosX.render(context, mouseX, mouseY, delta);
            this.inputPosY.render(context, mouseX, mouseY, delta);
            this.inputPosZ.render(context, mouseX, mouseY, delta);
            context.drawTextWithShadow(this.textRenderer, INCLUDE_ENTITIES_TEXT, this.width / 2 + 154 - this.textRenderer.getWidth(INCLUDE_ENTITIES_TEXT), 150, 0xA0A0A0);
        }
        if (lv == StructureBlockMode.SAVE) {
            context.drawTextWithShadow(this.textRenderer, SIZE_TEXT, this.width / 2 - 153, 110, 0xA0A0A0);
            this.inputSizeX.render(context, mouseX, mouseY, delta);
            this.inputSizeY.render(context, mouseX, mouseY, delta);
            this.inputSizeZ.render(context, mouseX, mouseY, delta);
            context.drawTextWithShadow(this.textRenderer, DETECT_SIZE_TEXT, this.width / 2 + 154 - this.textRenderer.getWidth(DETECT_SIZE_TEXT), 110, 0xA0A0A0);
            context.drawTextWithShadow(this.textRenderer, SHOW_AIR_TEXT, this.width / 2 + 154 - this.textRenderer.getWidth(SHOW_AIR_TEXT), 70, 0xA0A0A0);
        }
        if (lv == StructureBlockMode.LOAD) {
            context.drawTextWithShadow(this.textRenderer, INTEGRITY_TEXT, this.width / 2 - 153, 110, 0xA0A0A0);
            this.inputIntegrity.render(context, mouseX, mouseY, delta);
            this.inputSeed.render(context, mouseX, mouseY, delta);
            context.drawTextWithShadow(this.textRenderer, SHOW_BOUNDING_BOX_TEXT, this.width / 2 + 154 - this.textRenderer.getWidth(SHOW_BOUNDING_BOX_TEXT), 70, 0xA0A0A0);
        }
        if (lv == StructureBlockMode.DATA) {
            context.drawTextWithShadow(this.textRenderer, CUSTOM_DATA_TEXT, this.width / 2 - 153, 110, 0xA0A0A0);
            this.inputMetadata.render(context, mouseX, mouseY, delta);
        }
        context.drawTextWithShadow(this.textRenderer, lv.asText(), this.width / 2 - 153, 174, 0xA0A0A0);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

