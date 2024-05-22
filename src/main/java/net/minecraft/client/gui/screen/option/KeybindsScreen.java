/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class KeybindsScreen
extends GameOptionsScreen {
    private static final Text TITLE_TEXT = Text.translatable("controls.keybinds.title");
    @Nullable
    public KeyBinding selectedKeyBinding;
    public long lastKeyCodeUpdateTime;
    private ControlsListWidget controlsList;
    private ButtonWidget resetAllButton;

    public KeybindsScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, TITLE_TEXT);
    }

    @Override
    protected void initBody() {
        this.controlsList = this.layout.addBody(new ControlsListWidget(this, this.client));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void initFooter() {
        this.resetAllButton = ButtonWidget.builder(Text.translatable("controls.resetAll"), button -> {
            for (KeyBinding lv : this.gameOptions.allKeys) {
                lv.setBoundKey(lv.getDefaultKey());
            }
            this.controlsList.update();
        }).build();
        DirectionalLayoutWidget lv = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        lv.add(this.resetAllButton);
        lv.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).build());
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
        this.controlsList.position(this.width, this.layout);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.selectedKeyBinding != null) {
            this.gameOptions.setKeyCode(this.selectedKeyBinding, InputUtil.Type.MOUSE.createFromCode(button));
            this.selectedKeyBinding = null;
            this.controlsList.update();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.selectedKeyBinding != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.gameOptions.setKeyCode(this.selectedKeyBinding, InputUtil.UNKNOWN_KEY);
            } else {
                this.gameOptions.setKeyCode(this.selectedKeyBinding, InputUtil.fromKeyCode(keyCode, scanCode));
            }
            this.selectedKeyBinding = null;
            this.lastKeyCodeUpdateTime = Util.getMeasuringTimeMs();
            this.controlsList.update();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        boolean bl = false;
        for (KeyBinding lv : this.gameOptions.allKeys) {
            if (lv.isDefault()) continue;
            bl = true;
            break;
        }
        this.resetAllButton.active = bl;
    }
}

