/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.task.RealmsPrepareConnectionTask;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsTermsScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text TITLE = Text.translatable("mco.terms.title");
    private static final Text SENTENCE_ONE_TEXT = Text.translatable("mco.terms.sentence.1");
    private static final Text SENTENCE_TWO_TEXT = ScreenTexts.space().append(Text.translatable("mco.terms.sentence.2").fillStyle(Style.EMPTY.withUnderline(true)));
    private final Screen parent;
    private final RealmsServer realmsServer;
    private boolean onLink;

    public RealmsTermsScreen(Screen parent, RealmsServer realmsServer) {
        super(TITLE);
        this.parent = parent;
        this.realmsServer = realmsServer;
    }

    @Override
    public void init() {
        int i = this.width / 4 - 2;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.terms.buttons.agree"), button -> this.agreedToTos()).dimensions(this.width / 4, RealmsTermsScreen.row(12), i, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.terms.buttons.disagree"), button -> this.client.setScreen(this.parent)).dimensions(this.width / 2 + 4, RealmsTermsScreen.row(12), i, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void agreedToTos() {
        RealmsClient lv = RealmsClient.create();
        try {
            lv.agreeToTos();
            this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent, new RealmsPrepareConnectionTask(this.parent, this.realmsServer)));
        } catch (RealmsServiceException lv2) {
            LOGGER.error("Couldn't agree to TOS", lv2);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.onLink) {
            this.client.keyboard.setClipboard("https://aka.ms/MinecraftRealmsTerms");
            Util.getOperatingSystem().open("https://aka.ms/MinecraftRealmsTerms");
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), SENTENCE_ONE_TEXT).append(ScreenTexts.SPACE).append(SENTENCE_TWO_TEXT);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 17, Colors.WHITE);
        context.drawText(this.textRenderer, SENTENCE_ONE_TEXT, this.width / 2 - 120, RealmsTermsScreen.row(5), Colors.WHITE, false);
        int k = this.textRenderer.getWidth(SENTENCE_ONE_TEXT);
        int l = this.width / 2 - 121 + k;
        int m = RealmsTermsScreen.row(5);
        int n = l + this.textRenderer.getWidth(SENTENCE_TWO_TEXT) + 1;
        int o = m + 1 + this.textRenderer.fontHeight;
        this.onLink = l <= mouseX && mouseX <= n && m <= mouseY && mouseY <= o;
        context.drawText(this.textRenderer, SENTENCE_TWO_TEXT, this.width / 2 - 120 + k, RealmsTermsScreen.row(5), this.onLink ? 7107012 : 0x3366BB, false);
    }
}

