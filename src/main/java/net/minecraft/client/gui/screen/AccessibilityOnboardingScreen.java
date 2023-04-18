package net.minecraft.client.gui.screen;

import com.mojang.text2speech.Narrator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AccessibilityOnboardingScreen extends Screen {
   private static final Text NARRATOR_PROMPT = Text.translatable("accessibility.onboarding.screen.narrator");
   private static final int field_41838 = 4;
   private static final int field_41839 = 16;
   private final RotatingCubeMapRenderer backgroundRenderer;
   private final LogoDrawer logoDrawer;
   private final GameOptions gameOptions;
   private final boolean isNarratorUsable;
   private boolean narratorPrompted;
   private float narratorPromptTimer;
   @Nullable
   private NarratedMultilineTextWidget textWidget;

   public AccessibilityOnboardingScreen(GameOptions gameOptions) {
      super(Text.translatable("accessibility.onboarding.screen.title"));
      this.backgroundRenderer = new RotatingCubeMapRenderer(TitleScreen.PANORAMA_CUBE_MAP);
      this.gameOptions = gameOptions;
      this.logoDrawer = new LogoDrawer(true);
      this.isNarratorUsable = MinecraftClient.getInstance().getNarratorManager().isActive();
   }

   public void init() {
      int i = this.yMargin();
      SimplePositioningWidget lv = new SimplePositioningWidget(this.width, this.height - i);
      lv.getMainPositioner().alignTop().margin(4);
      GridWidget lv2 = (GridWidget)lv.add(new GridWidget());
      lv2.getMainPositioner().alignHorizontalCenter().margin(4);
      GridWidget.Adder lv3 = lv2.createAdder(1);
      lv3.getMainPositioner().margin(2);
      this.textWidget = new NarratedMultilineTextWidget(this.textRenderer, this.title, this.width);
      lv3.add(this.textWidget, lv3.copyPositioner().marginBottom(16));
      ClickableWidget lv4 = this.gameOptions.getNarrator().createWidget(this.gameOptions, 0, 0, 150);
      lv4.active = this.isNarratorUsable;
      lv3.add(lv4);
      if (this.isNarratorUsable) {
         this.setInitialFocus(lv4);
      }

      lv3.add(AccessibilityOnboardingButtons.createAccessibilityButton((button) -> {
         this.setScreen(new AccessibilityOptionsScreen(this, this.client.options));
      }));
      lv3.add(AccessibilityOnboardingButtons.createLanguageButton((button) -> {
         this.setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager()));
      }));
      lv.add(ButtonWidget.builder(ScreenTexts.CONTINUE, (button) -> {
         this.close();
      }).build(), lv.copyPositioner().alignBottom().margin(8));
      lv.refreshPositions();
      SimplePositioningWidget.setPos(lv, 0, i, this.width, this.height, 0.5F, 0.0F);
      lv.forEachChild(this::addDrawableChild);
   }

   private int yMargin() {
      return 90;
   }

   public void close() {
      this.setScreen(new TitleScreen(true, this.logoDrawer));
   }

   private void setScreen(Screen screen) {
      this.gameOptions.onboardAccessibility = false;
      this.gameOptions.write();
      Narrator.getNarrator().clear();
      this.client.setScreen(screen);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.tickNarratorPrompt();
      this.backgroundRenderer.render(0.0F, 1.0F);
      fill(matrices, 0, 0, this.width, this.height, -1877995504);
      this.logoDrawer.draw(matrices, this.width, 1.0F);
      if (this.textWidget != null) {
         this.textWidget.render(matrices, mouseX, mouseY, delta);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }

   private void tickNarratorPrompt() {
      if (!this.narratorPrompted && this.isNarratorUsable) {
         if (this.narratorPromptTimer < 40.0F) {
            ++this.narratorPromptTimer;
         } else if (this.client.isWindowFocused()) {
            Narrator.getNarrator().say(NARRATOR_PROMPT.getString(), true);
            this.narratorPrompted = true;
         }
      }

   }
}
