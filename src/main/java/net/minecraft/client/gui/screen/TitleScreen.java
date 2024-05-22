/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.AccessibilityOnboardingButtons;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TitleScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text NARRATOR_SCREEN_TITLE = Text.translatable("narrator.screen.title");
    private static final Text COPYRIGHT = Text.translatable("title.credits");
    private static final String DEMO_WORLD_NAME = "Demo_World";
    private static final float field_49900 = 2000.0f;
    @Nullable
    private SplashTextRenderer splashText;
    private ButtonWidget buttonResetDemo;
    @Nullable
    private RealmsNotificationsScreen realmsNotificationGui;
    private float backgroundAlpha = 1.0f;
    private boolean doBackgroundFade;
    private long backgroundFadeStart;
    @Nullable
    private DeprecationNotice deprecationNotice;
    private final LogoDrawer logoDrawer;

    public TitleScreen() {
        this(false);
    }

    public TitleScreen(boolean doBackgroundFade) {
        this(doBackgroundFade, null);
    }

    public TitleScreen(boolean doBackgroundFade, @Nullable LogoDrawer logoDrawer) {
        super(NARRATOR_SCREEN_TITLE);
        this.doBackgroundFade = doBackgroundFade;
        this.logoDrawer = Objects.requireNonNullElseGet(logoDrawer, () -> new LogoDrawer(false));
    }

    private boolean isRealmsNotificationsGuiDisplayed() {
        return this.realmsNotificationGui != null;
    }

    @Override
    public void tick() {
        if (this.isRealmsNotificationsGuiDisplayed()) {
            this.realmsNotificationGui.tick();
        }
    }

    public static CompletableFuture<Void> loadTexturesAsync(TextureManager textureManager, Executor executor) {
        return CompletableFuture.allOf(textureManager.loadTextureAsync(LogoDrawer.LOGO_TEXTURE, executor), textureManager.loadTextureAsync(LogoDrawer.EDITION_TEXTURE, executor), textureManager.loadTextureAsync(RotatingCubeMapRenderer.OVERLAY_TEXTURE, executor), PANORAMA_RENDERER.loadTexturesAsync(textureManager, executor));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        if (this.splashText == null) {
            this.splashText = this.client.getSplashTextLoader().get();
        }
        int i = this.textRenderer.getWidth(COPYRIGHT);
        int j = this.width - i - 2;
        int k = 24;
        int l = this.height / 4 + 48;
        if (this.client.isDemo()) {
            this.initWidgetsDemo(l, 24);
        } else {
            this.initWidgetsNormal(l, 24);
        }
        TextIconButtonWidget lv = this.addDrawableChild(AccessibilityOnboardingButtons.createLanguageButton(20, button -> this.client.setScreen(new LanguageOptionsScreen((Screen)this, this.client.options, this.client.getLanguageManager())), true));
        lv.setPosition(this.width / 2 - 124, l + 72 + 12);
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.options"), button -> this.client.setScreen(new OptionsScreen(this, this.client.options))).dimensions(this.width / 2 - 100, l + 72 + 12, 98, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.quit"), button -> this.client.scheduleStop()).dimensions(this.width / 2 + 2, l + 72 + 12, 98, 20).build());
        TextIconButtonWidget lv2 = this.addDrawableChild(AccessibilityOnboardingButtons.createAccessibilityButton(20, button -> this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options)), true));
        lv2.setPosition(this.width / 2 + 104, l + 72 + 12);
        this.addDrawableChild(new PressableTextWidget(j, this.height - 10, i, 10, COPYRIGHT, button -> this.client.setScreen(new CreditsAndAttributionScreen(this)), this.textRenderer));
        if (this.realmsNotificationGui == null) {
            this.realmsNotificationGui = new RealmsNotificationsScreen();
        }
        if (this.isRealmsNotificationsGuiDisplayed()) {
            this.realmsNotificationGui.init(this.client, this.width, this.height);
        }
    }

    private void initWidgetsNormal(int y, int spacingY) {
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.singleplayer"), button -> this.client.setScreen(new SelectWorldScreen(this))).dimensions(this.width / 2 - 100, y, 200, 20).build());
        Text lv = this.getMultiplayerDisabledText();
        boolean bl = lv == null;
        Tooltip lv2 = lv != null ? Tooltip.of(lv) : null;
        this.addDrawableChild(ButtonWidget.builder((Text)Text.translatable((String)"menu.multiplayer"), (ButtonWidget.PressAction)(ButtonWidget.PressAction)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/widget/ButtonWidget;)V, onMultiplayerButtonPressed(net.minecraft.client.gui.widget.ButtonWidget ), (Lnet/minecraft/client/gui/widget/ButtonWidget;)V)((TitleScreen)this)).dimensions((int)(this.width / 2 - 100), (int)(y + spacingY * 1), (int)200, (int)20).tooltip((Tooltip)lv2).build()).active = bl;
        this.addDrawableChild(ButtonWidget.builder((Text)Text.translatable((String)"menu.online"), (ButtonWidget.PressAction)(ButtonWidget.PressAction)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/widget/ButtonWidget;)V, method_55814(net.minecraft.client.gui.widget.ButtonWidget ), (Lnet/minecraft/client/gui/widget/ButtonWidget;)V)((TitleScreen)this)).dimensions((int)(this.width / 2 - 100), (int)(y + spacingY * 2), (int)200, (int)20).tooltip((Tooltip)lv2).build()).active = bl;
    }

    @Nullable
    private Text getMultiplayerDisabledText() {
        if (this.client.isMultiplayerEnabled()) {
            return null;
        }
        if (this.client.isUsernameBanned()) {
            return Text.translatable("title.multiplayer.disabled.banned.name");
        }
        BanDetails banDetails = this.client.getMultiplayerBanDetails();
        if (banDetails != null) {
            if (banDetails.expires() != null) {
                return Text.translatable("title.multiplayer.disabled.banned.temporary");
            }
            return Text.translatable("title.multiplayer.disabled.banned.permanent");
        }
        return Text.translatable("title.multiplayer.disabled");
    }

    private void initWidgetsDemo(int y, int spacingY) {
        boolean bl = this.canReadDemoWorldData();
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.playdemo"), button -> {
            if (bl) {
                this.client.createIntegratedServerLoader().start(DEMO_WORLD_NAME, () -> this.client.setScreen(this));
            } else {
                this.client.createIntegratedServerLoader().createAndStart(DEMO_WORLD_NAME, MinecraftServer.DEMO_LEVEL_INFO, GeneratorOptions.DEMO_OPTIONS, WorldPresets::createDemoOptions, this);
            }
        }).dimensions(this.width / 2 - 100, y, 200, 20).build());
        this.buttonResetDemo = this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.resetdemo"), button -> {
            LevelStorage lv = this.client.getLevelStorage();
            try (LevelStorage.Session lv2 = lv.createSessionWithoutSymlinkCheck(DEMO_WORLD_NAME);){
                if (lv2.levelDatExists()) {
                    this.client.setScreen(new ConfirmScreen(this::onDemoDeletionConfirmed, Text.translatable("selectWorld.deleteQuestion"), Text.translatable("selectWorld.deleteWarning", MinecraftServer.DEMO_LEVEL_INFO.getLevelName()), Text.translatable("selectWorld.deleteButton"), ScreenTexts.CANCEL));
                }
            } catch (IOException iOException) {
                SystemToast.addWorldAccessFailureToast(this.client, DEMO_WORLD_NAME);
                LOGGER.warn("Failed to access demo world", iOException);
            }
        }).dimensions(this.width / 2 - 100, y + spacingY * 1, 200, 20).build());
        this.buttonResetDemo.active = bl;
    }

    private boolean canReadDemoWorldData() {
        boolean bl;
        block8: {
            LevelStorage.Session lv = this.client.getLevelStorage().createSessionWithoutSymlinkCheck(DEMO_WORLD_NAME);
            try {
                bl = lv.levelDatExists();
                if (lv == null) break block8;
            } catch (Throwable throwable) {
                try {
                    if (lv != null) {
                        try {
                            lv.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException iOException) {
                    SystemToast.addWorldAccessFailureToast(this.client, DEMO_WORLD_NAME);
                    LOGGER.warn("Failed to read demo world data", iOException);
                    return false;
                }
            }
            lv.close();
        }
        return bl;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
            this.backgroundFadeStart = Util.getMeasuringTimeMs();
        }
        float g = 1.0f;
        if (this.doBackgroundFade) {
            float h = (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 2000.0f;
            if (h > 1.0f) {
                this.doBackgroundFade = false;
                this.backgroundAlpha = 1.0f;
            } else {
                h = MathHelper.clamp(h, 0.0f, 1.0f);
                g = MathHelper.clampedMap(h, 0.5f, 1.0f, 0.0f, 1.0f);
                this.backgroundAlpha = MathHelper.clampedMap(h, 0.0f, 0.5f, 0.0f, 1.0f);
            }
            this.setWidgetAlpha(g);
        }
        this.renderPanoramaBackground(context, delta);
        int k = MathHelper.ceil(g * 255.0f) << 24;
        if ((k & 0xFC000000) == 0) {
            return;
        }
        super.render(context, mouseX, mouseY, delta);
        this.logoDrawer.draw(context, this.width, g);
        if (this.deprecationNotice != null) {
            this.deprecationNotice.render(context, k);
        }
        if (this.splashText != null && !this.client.options.getHideSplashTexts().getValue().booleanValue()) {
            this.splashText.render(context, this.width, this.textRenderer, k);
        }
        String string = "Minecraft " + SharedConstants.getGameVersion().getName();
        string = this.client.isDemo() ? string + " Demo" : string + (String)("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType());
        if (MinecraftClient.getModStatus().isModded()) {
            string = string + I18n.translate("menu.modded", new Object[0]);
        }
        context.drawTextWithShadow(this.textRenderer, string, 2, this.height - 10, 0xFFFFFF | k);
        if (this.isRealmsNotificationsGuiDisplayed() && g >= 1.0f) {
            RenderSystem.enableDepthTest();
            this.realmsNotificationGui.render(context, mouseX, mouseY, delta);
        }
    }

    private void setWidgetAlpha(float alpha) {
        for (Element element : this.children()) {
            if (!(element instanceof ClickableWidget)) continue;
            ClickableWidget lv2 = (ClickableWidget)element;
            lv2.setAlpha(alpha);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected void renderPanoramaBackground(DrawContext context, float delta) {
        ROTATING_PANORAMA_RENDERER.render(context, this.width, this.height, this.backgroundAlpha, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return this.isRealmsNotificationsGuiDisplayed() && this.realmsNotificationGui.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        if (this.realmsNotificationGui != null) {
            this.realmsNotificationGui.removed();
        }
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        if (this.realmsNotificationGui != null) {
            this.realmsNotificationGui.onDisplayed();
        }
    }

    private void onDemoDeletionConfirmed(boolean delete) {
        if (delete) {
            try (LevelStorage.Session lv = this.client.getLevelStorage().createSessionWithoutSymlinkCheck(DEMO_WORLD_NAME);){
                lv.deleteSessionLock();
            } catch (IOException iOException) {
                SystemToast.addWorldDeleteFailureToast(this.client, DEMO_WORLD_NAME);
                LOGGER.warn("Failed to delete demo world", iOException);
            }
        }
        this.client.setScreen(this);
    }

    private /* synthetic */ void method_55814(ButtonWidget arg) {
        this.client.setScreen(new RealmsMainScreen(this));
    }

    private /* synthetic */ void onMultiplayerButtonPressed(ButtonWidget button) {
        Screen lv = this.client.options.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this);
        this.client.setScreen(lv);
    }

    @Environment(value=EnvType.CLIENT)
    record DeprecationNotice(TextRenderer textRenderer, MultilineText label, int x, int y) {
        public void render(DrawContext context, int color) {
            this.label.fillBackground(context, this.x, this.y, this.textRenderer.fontHeight, 2, 0x200000 | Math.min(color, 0x55000000));
            this.label.drawCenterWithShadow(context, this.x, this.y, this.textRenderer.fontHeight, 0xFFFFFF | color);
        }
    }
}

