/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DeathScreen
extends Screen {
    private static final Identifier DRAFT_REPORT_ICON_TEXTURE = Identifier.method_60656("icon/draft_report");
    private int ticksSinceDeath;
    private final Text message;
    private final boolean isHardcore;
    private Text scoreText;
    private final List<ButtonWidget> buttons = Lists.newArrayList();
    @Nullable
    private ButtonWidget titleScreenButton;

    public DeathScreen(@Nullable Text message, boolean isHardcore) {
        super(Text.translatable(isHardcore ? "deathScreen.title.hardcore" : "deathScreen.title"));
        this.message = message;
        this.isHardcore = isHardcore;
    }

    @Override
    protected void init() {
        this.ticksSinceDeath = 0;
        this.buttons.clear();
        MutableText lv = this.isHardcore ? Text.translatable("deathScreen.spectate") : Text.translatable("deathScreen.respawn");
        this.buttons.add(this.addDrawableChild(ButtonWidget.builder(lv, button -> {
            this.client.player.requestRespawn();
            button.active = false;
        }).dimensions(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));
        this.titleScreenButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("deathScreen.titleScreen"), button -> this.client.getAbuseReportContext().tryShowDraftScreen(this.client, this, this::onTitleScreenButtonClicked, true)).dimensions(this.width / 2 - 100, this.height / 4 + 96, 200, 20).build());
        this.buttons.add(this.titleScreenButton);
        this.setButtonsActive(false);
        this.scoreText = Text.translatable("deathScreen.score.value", Text.literal(Integer.toString(this.client.player.getScore())).formatted(Formatting.YELLOW));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void onTitleScreenButtonClicked() {
        if (this.isHardcore) {
            this.quitLevel();
            return;
        }
        TitleScreenConfirmScreen lv = new TitleScreenConfirmScreen(confirmed -> {
            if (confirmed) {
                this.quitLevel();
            } else {
                this.client.player.requestRespawn();
                this.client.setScreen(null);
            }
        }, Text.translatable("deathScreen.quit.confirm"), ScreenTexts.EMPTY, Text.translatable("deathScreen.titleScreen"), Text.translatable("deathScreen.respawn"));
        this.client.setScreen(lv);
        lv.disableButtons(20);
    }

    private void quitLevel() {
        if (this.client.world != null) {
            this.client.world.disconnect();
        }
        this.client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
        this.client.setScreen(new TitleScreen());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.getMatrices().push();
        context.getMatrices().scale(2.0f, 2.0f, 2.0f);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2 / 2, 30, 0xFFFFFF);
        context.getMatrices().pop();
        if (this.message != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, this.message, this.width / 2, 85, 0xFFFFFF);
        }
        context.drawCenteredTextWithShadow(this.textRenderer, this.scoreText, this.width / 2, 100, 0xFFFFFF);
        if (this.message != null && mouseY > 85 && mouseY < 85 + this.textRenderer.fontHeight) {
            Style lv = this.getTextComponentUnderMouse(mouseX);
            context.drawHoverEvent(this.textRenderer, lv, mouseX, mouseY);
        }
        if (this.titleScreenButton != null && this.client.getAbuseReportContext().hasDraft()) {
            context.drawGuiTexture(DRAFT_REPORT_ICON_TEXTURE, this.titleScreenButton.getX() + this.titleScreenButton.getWidth() - 17, this.titleScreenButton.getY() + 3, 15, 15);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        DeathScreen.fillBackgroundGradient(context, this.width, this.height);
    }

    static void fillBackgroundGradient(DrawContext context, int width, int height) {
        context.fillGradient(0, 0, width, height, 0x60500000, -1602211792);
    }

    @Nullable
    private Style getTextComponentUnderMouse(int mouseX) {
        if (this.message == null) {
            return null;
        }
        int j = this.client.textRenderer.getWidth(this.message);
        int k = this.width / 2 - j / 2;
        int l = this.width / 2 + j / 2;
        if (mouseX < k || mouseX > l) {
            return null;
        }
        return this.client.textRenderer.getTextHandler().getStyleAt(this.message, mouseX - k);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Style lv;
        if (this.message != null && mouseY > 85.0 && mouseY < (double)(85 + this.textRenderer.fontHeight) && (lv = this.getTextComponentUnderMouse((int)mouseX)) != null && lv.getClickEvent() != null && lv.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
            this.handleTextClick(lv);
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        ++this.ticksSinceDeath;
        if (this.ticksSinceDeath == 20) {
            this.setButtonsActive(true);
        }
    }

    private void setButtonsActive(boolean active) {
        for (ButtonWidget lv : this.buttons) {
            lv.active = active;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class TitleScreenConfirmScreen
    extends ConfirmScreen {
        public TitleScreenConfirmScreen(BooleanConsumer booleanConsumer, Text arg, Text arg2, Text arg3, Text arg4) {
            super(booleanConsumer, arg, arg2, arg3, arg4);
        }

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
            DeathScreen.fillBackgroundGradient(context, this.width, this.height);
        }
    }
}

