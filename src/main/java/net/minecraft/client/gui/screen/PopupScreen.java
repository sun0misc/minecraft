/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PopupScreen
extends Screen {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.method_60656("popup/background");
    private static final int VERTICAL_SPACING = 12;
    private static final int MARGIN_WIDTH = 18;
    private static final int BUTTON_HORIZONTAL_SPACING = 6;
    private static final int IMAGE_WIDTH = 130;
    private static final int IMAGE_HEIGHT = 64;
    private static final int DEFAULT_WIDTH = 250;
    private final Screen backgroundScreen;
    @Nullable
    private final Identifier image;
    private final Text message;
    private final List<Button> buttons;
    @Nullable
    private final Runnable onClosed;
    private final int innerWidth;
    private final DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();

    PopupScreen(Screen backgroundScreen, int width, @Nullable Identifier image, Text title, Text message, List<Button> buttons, @Nullable Runnable onClosed) {
        super(title);
        this.backgroundScreen = backgroundScreen;
        this.image = image;
        this.message = message;
        this.buttons = buttons;
        this.onClosed = onClosed;
        this.innerWidth = width - 36;
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        this.backgroundScreen.blur();
    }

    @Override
    protected void init() {
        this.backgroundScreen.init(this.client, this.width, this.height);
        this.layout.spacing(12).getMainPositioner().alignHorizontalCenter();
        this.layout.add(new MultilineTextWidget(this.title.copy().formatted(Formatting.BOLD), this.textRenderer).setMaxWidth(this.innerWidth).setCentered(true));
        if (this.image != null) {
            this.layout.add(IconWidget.create(130, 64, this.image, 130, 64));
        }
        this.layout.add(new MultilineTextWidget(this.message, this.textRenderer).setMaxWidth(this.innerWidth).setCentered(true));
        this.layout.add(this.createButtonLayout());
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.initTabNavigation();
    }

    private DirectionalLayoutWidget createButtonLayout() {
        int i = 6 * (this.buttons.size() - 1);
        int j = Math.min((this.innerWidth - i) / this.buttons.size(), 150);
        DirectionalLayoutWidget lv = DirectionalLayoutWidget.horizontal();
        lv.spacing(6);
        for (Button lv2 : this.buttons) {
            lv.add(ButtonWidget.builder(lv2.message(), button -> lv2.action().accept(this)).width(j).build());
        }
        return lv;
    }

    @Override
    protected void initTabNavigation() {
        this.backgroundScreen.resize(this.client, this.width, this.height);
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, this.getNavigationFocus());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.backgroundScreen.render(context, -1, -1, delta);
        context.draw();
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        this.renderInGameBackground(context);
        context.drawGuiTexture(BACKGROUND_TEXTURE, this.layout.getX() - 18, this.layout.getY() - 18, this.layout.getWidth() + 36, this.layout.getHeight() + 36);
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(this.title, this.message);
    }

    @Override
    public void close() {
        if (this.onClosed != null) {
            this.onClosed.run();
        }
        this.client.setScreen(this.backgroundScreen);
    }

    @Environment(value=EnvType.CLIENT)
    record Button(Text message, Consumer<PopupScreen> action) {
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Screen backgroundScreen;
        private final Text title;
        private Text message = ScreenTexts.EMPTY;
        private int width = 250;
        @Nullable
        private Identifier image;
        private final List<Button> buttons = new ArrayList<Button>();
        @Nullable
        private Runnable onClosed = null;

        public Builder(Screen backgroundScreen, Text title) {
            this.backgroundScreen = backgroundScreen;
            this.title = title;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder image(Identifier image) {
            this.image = image;
            return this;
        }

        public Builder message(Text message) {
            this.message = message;
            return this;
        }

        public Builder button(Text message, Consumer<PopupScreen> action) {
            this.buttons.add(new Button(message, action));
            return this;
        }

        public Builder onClosed(Runnable onClosed) {
            this.onClosed = onClosed;
            return this;
        }

        public PopupScreen build() {
            if (this.buttons.isEmpty()) {
                throw new IllegalStateException("Popup must have at least one button");
            }
            return new PopupScreen(this.backgroundScreen, this.width, this.image, this.title, this.message, List.copyOf(this.buttons), this.onClosed);
        }
    }
}

