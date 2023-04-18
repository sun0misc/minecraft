package net.minecraft.client.gui.screen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.Navigable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.screen.narration.ScreenNarrator;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.MusicSound;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public abstract class Screen extends AbstractParentElement implements Drawable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Set ALLOWED_PROTOCOLS = Sets.newHashSet(new String[]{"http", "https"});
   private static final int field_32270 = 2;
   private static final Text SCREEN_USAGE_TEXT = Text.translatable("narrator.screen.usage");
   protected final Text title;
   private final List children = Lists.newArrayList();
   private final List selectables = Lists.newArrayList();
   @Nullable
   protected MinecraftClient client;
   private boolean screenInitialized;
   protected ItemRenderer itemRenderer;
   public int width;
   public int height;
   private final List drawables = Lists.newArrayList();
   public boolean passEvents;
   protected TextRenderer textRenderer;
   @Nullable
   private URI clickedLink;
   private static final long SCREEN_INIT_NARRATION_DELAY;
   private static final long NARRATOR_MODE_CHANGE_DELAY;
   private static final long MOUSE_MOVE_NARRATION_DELAY = 750L;
   private static final long MOUSE_PRESS_SCROLL_NARRATION_DELAY = 200L;
   private static final long KEY_PRESS_NARRATION_DELAY = 200L;
   private final ScreenNarrator narrator = new ScreenNarrator();
   private long elementNarrationStartTime = Long.MIN_VALUE;
   private long screenNarrationStartTime = Long.MAX_VALUE;
   @Nullable
   private Selectable selected;
   @Nullable
   private PositionedTooltip tooltip;

   protected Screen(Text title) {
      this.title = title;
   }

   public Text getTitle() {
      return this.title;
   }

   public Text getNarratedTitle() {
      return this.getTitle();
   }

   public final void renderWithTooltip(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.render(matrices, mouseX, mouseY, delta);
      if (this.tooltip != null) {
         this.renderPositionedTooltip(matrices, this.tooltip, mouseX, mouseY);
         this.tooltip = null;
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      Iterator var5 = this.drawables.iterator();

      while(var5.hasNext()) {
         Drawable lv = (Drawable)var5.next();
         lv.render(matrices, mouseX, mouseY, delta);
      }

   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
         this.close();
         return true;
      } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else {
         Object var10000;
         switch (keyCode) {
            case 258:
               var10000 = this.getTabNavigation();
               break;
            case 259:
            case 260:
            case 261:
            default:
               var10000 = null;
               break;
            case 262:
               var10000 = this.getArrowNavigation(NavigationDirection.RIGHT);
               break;
            case 263:
               var10000 = this.getArrowNavigation(NavigationDirection.LEFT);
               break;
            case 264:
               var10000 = this.getArrowNavigation(NavigationDirection.DOWN);
               break;
            case 265:
               var10000 = this.getArrowNavigation(NavigationDirection.UP);
         }

         GuiNavigation lv = var10000;
         if (lv != null) {
            GuiNavigationPath lv2 = super.getNavigationPath((GuiNavigation)lv);
            if (lv2 == null && lv instanceof GuiNavigation.Tab) {
               this.blur();
               lv2 = super.getNavigationPath((GuiNavigation)lv);
            }

            if (lv2 != null) {
               this.switchFocus(lv2);
            }
         }

         return false;
      }
   }

   private GuiNavigation.Tab getTabNavigation() {
      boolean bl = !hasShiftDown();
      return new GuiNavigation.Tab(bl);
   }

   private GuiNavigation.Arrow getArrowNavigation(NavigationDirection direction) {
      return new GuiNavigation.Arrow(direction);
   }

   protected void setInitialFocus(Element element) {
      GuiNavigationPath lv = GuiNavigationPath.of((ParentElement)this, (GuiNavigationPath)element.getNavigationPath(new GuiNavigation.Down()));
      if (lv != null) {
         this.switchFocus(lv);
      }

   }

   private void blur() {
      GuiNavigationPath lv = this.getFocusedPath();
      if (lv != null) {
         lv.setFocused(false);
      }

   }

   @VisibleForTesting
   protected void switchFocus(GuiNavigationPath path) {
      this.blur();
      path.setFocused(true);
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public void close() {
      this.client.setScreen((Screen)null);
   }

   protected Element addDrawableChild(Element drawableElement) {
      this.drawables.add((Drawable)drawableElement);
      return this.addSelectableChild(drawableElement);
   }

   protected Drawable addDrawable(Drawable drawable) {
      this.drawables.add(drawable);
      return drawable;
   }

   protected Element addSelectableChild(Element child) {
      this.children.add(child);
      this.selectables.add((Selectable)child);
      return child;
   }

   protected void remove(Element child) {
      if (child instanceof Drawable) {
         this.drawables.remove((Drawable)child);
      }

      if (child instanceof Selectable) {
         this.selectables.remove((Selectable)child);
      }

      this.children.remove(child);
   }

   protected void clearChildren() {
      this.drawables.clear();
      this.children.clear();
      this.selectables.clear();
   }

   protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
      this.renderTooltip(matrices, this.getTooltipFromItem(stack), stack.getTooltipData(), x, y);
   }

   public void renderTooltip(MatrixStack matrices, List lines, Optional data, int x, int y) {
      List list2 = (List)lines.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
      data.ifPresent((datax) -> {
         list2.add(1, TooltipComponent.of(datax));
      });
      this.renderTooltipFromComponents(matrices, list2, x, y, HoveredTooltipPositioner.INSTANCE);
   }

   public List getTooltipFromItem(ItemStack stack) {
      return stack.getTooltip(this.client.player, this.client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC);
   }

   public void renderTooltip(MatrixStack matrices, Text text, int x, int y) {
      this.renderOrderedTooltip(matrices, Arrays.asList(text.asOrderedText()), x, y);
   }

   public void renderTooltip(MatrixStack matrices, List lines, int x, int y) {
      this.renderOrderedTooltip(matrices, Lists.transform(lines, Text::asOrderedText), x, y);
   }

   public void renderOrderedTooltip(MatrixStack matrices, List lines, int x, int y) {
      this.renderTooltipFromComponents(matrices, (List)lines.stream().map(TooltipComponent::of).collect(Collectors.toList()), x, y, HoveredTooltipPositioner.INSTANCE);
   }

   private void renderPositionedTooltip(MatrixStack matrices, PositionedTooltip tooltip, int x, int y) {
      this.renderTooltipFromComponents(matrices, (List)tooltip.tooltip().stream().map(TooltipComponent::of).collect(Collectors.toList()), x, y, tooltip.positioner());
   }

   private void renderTooltipFromComponents(MatrixStack matrices, List components, int x, int y, TooltipPositioner positioner) {
      if (!components.isEmpty()) {
         int k = 0;
         int l = components.size() == 1 ? -2 : 0;

         TooltipComponent lv;
         for(Iterator var8 = components.iterator(); var8.hasNext(); l += lv.getHeight()) {
            lv = (TooltipComponent)var8.next();
            int m = lv.getWidth(this.textRenderer);
            if (m > k) {
               k = m;
            }
         }

         Vector2ic vector2ic = positioner.getPosition(this, x, y, k, l);
         int p = vector2ic.x();
         int q = vector2ic.y();
         matrices.push();
         int r = true;
         Tessellator lv2 = Tessellator.getInstance();
         BufferBuilder lv3 = lv2.getBuffer();
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         lv3.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         Matrix4f matrix4f = matrices.peek().getPositionMatrix();
         TooltipBackgroundRenderer.render((matrix, builder, startX, startY, endX, endY, z, colorStart, colorEnd) -> {
            DrawableHelper.fillGradient(matrix, builder, startX, startY, endX, endY, z, colorStart, colorEnd);
         }, matrix4f, lv3, p, q, k, l, 400);
         RenderSystem.enableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         BufferRenderer.drawWithGlobalProgram(lv3.end());
         VertexConsumerProvider.Immediate lv4 = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
         matrices.translate(0.0F, 0.0F, 400.0F);
         int s = q;

         int t;
         TooltipComponent lv5;
         for(t = 0; t < components.size(); ++t) {
            lv5 = (TooltipComponent)components.get(t);
            lv5.drawText(this.textRenderer, p, s, matrix4f, lv4);
            s += lv5.getHeight() + (t == 0 ? 2 : 0);
         }

         lv4.draw();
         s = q;

         for(t = 0; t < components.size(); ++t) {
            lv5 = (TooltipComponent)components.get(t);
            lv5.drawItems(this.textRenderer, p, s, matrices, this.itemRenderer);
            s += lv5.getHeight() + (t == 0 ? 2 : 0);
         }

         matrices.pop();
      }
   }

   protected void renderTextHoverEffect(MatrixStack matrices, @Nullable Style style, int x, int y) {
      if (style != null && style.getHoverEvent() != null) {
         HoverEvent lv = style.getHoverEvent();
         HoverEvent.ItemStackContent lv2 = (HoverEvent.ItemStackContent)lv.getValue(HoverEvent.Action.SHOW_ITEM);
         if (lv2 != null) {
            this.renderTooltip(matrices, lv2.asStack(), x, y);
         } else {
            HoverEvent.EntityContent lv3 = (HoverEvent.EntityContent)lv.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (lv3 != null) {
               if (this.client.options.advancedItemTooltips) {
                  this.renderTooltip(matrices, lv3.asTooltip(), x, y);
               }
            } else {
               Text lv4 = (Text)lv.getValue(HoverEvent.Action.SHOW_TEXT);
               if (lv4 != null) {
                  this.renderOrderedTooltip(matrices, this.client.textRenderer.wrapLines(lv4, Math.max(this.width / 2, 200)), x, y);
               }
            }
         }

      }
   }

   protected void insertText(String text, boolean override) {
   }

   public boolean handleTextClick(@Nullable Style style) {
      if (style == null) {
         return false;
      } else {
         ClickEvent lv = style.getClickEvent();
         if (hasShiftDown()) {
            if (style.getInsertion() != null) {
               this.insertText(style.getInsertion(), false);
            }
         } else if (lv != null) {
            URI uRI;
            if (lv.getAction() == ClickEvent.Action.OPEN_URL) {
               if (!(Boolean)this.client.options.getChatLinks().getValue()) {
                  return false;
               }

               try {
                  uRI = new URI(lv.getValue());
                  String string = uRI.getScheme();
                  if (string == null) {
                     throw new URISyntaxException(lv.getValue(), "Missing protocol");
                  }

                  if (!ALLOWED_PROTOCOLS.contains(string.toLowerCase(Locale.ROOT))) {
                     throw new URISyntaxException(lv.getValue(), "Unsupported protocol: " + string.toLowerCase(Locale.ROOT));
                  }

                  if ((Boolean)this.client.options.getChatLinksPrompt().getValue()) {
                     this.clickedLink = uRI;
                     this.client.setScreen(new ConfirmLinkScreen(this::confirmLink, lv.getValue(), false));
                  } else {
                     this.openLink(uRI);
                  }
               } catch (URISyntaxException var5) {
                  LOGGER.error("Can't open url for {}", lv, var5);
               }
            } else if (lv.getAction() == ClickEvent.Action.OPEN_FILE) {
               uRI = (new File(lv.getValue())).toURI();
               this.openLink(uRI);
            } else if (lv.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
               this.insertText(SharedConstants.stripInvalidChars(lv.getValue()), true);
            } else if (lv.getAction() == ClickEvent.Action.RUN_COMMAND) {
               String string2 = SharedConstants.stripInvalidChars(lv.getValue());
               if (string2.startsWith("/")) {
                  if (!this.client.player.networkHandler.sendCommand(string2.substring(1))) {
                     LOGGER.error("Not allowed to run command with signed argument from click event: '{}'", string2);
                  }
               } else {
                  LOGGER.error("Failed to run command without '/' prefix from click event: '{}'", string2);
               }
            } else if (lv.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD) {
               this.client.keyboard.setClipboard(lv.getValue());
            } else {
               LOGGER.error("Don't know how to handle {}", lv);
            }

            return true;
         }

         return false;
      }
   }

   public final void init(MinecraftClient client, int width, int height) {
      this.client = client;
      this.itemRenderer = client.getItemRenderer();
      this.textRenderer = client.textRenderer;
      this.width = width;
      this.height = height;
      if (!this.screenInitialized) {
         this.init();
      } else {
         this.initTabNavigation();
      }

      this.screenInitialized = true;
      this.narrateScreenIfNarrationEnabled(false);
      this.setElementNarrationDelay(SCREEN_INIT_NARRATION_DELAY);
   }

   protected void clearAndInit() {
      this.clearChildren();
      this.blur();
      this.init();
   }

   public List children() {
      return this.children;
   }

   protected void init() {
   }

   public void tick() {
   }

   public void removed() {
   }

   public void onDisplayed() {
   }

   public void renderBackground(MatrixStack matrices) {
      if (this.client.world != null) {
         fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
      } else {
         this.renderBackgroundTexture(matrices);
      }

   }

   public void renderBackgroundTexture(MatrixStack matrices) {
      RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND_TEXTURE);
      RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
      int i = true;
      drawTexture(matrices, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public boolean shouldPause() {
      return true;
   }

   private void confirmLink(boolean open) {
      if (open) {
         this.openLink(this.clickedLink);
      }

      this.clickedLink = null;
      this.client.setScreen(this);
   }

   private void openLink(URI link) {
      Util.getOperatingSystem().open(link);
   }

   public static boolean hasControlDown() {
      if (MinecraftClient.IS_SYSTEM_MAC) {
         return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 343) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 347);
      } else {
         return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL);
      }
   }

   public static boolean hasShiftDown() {
      return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
   }

   public static boolean hasAltDown() {
      return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_ALT);
   }

   public static boolean isCut(int code) {
      return code == 88 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isPaste(int code) {
      return code == 86 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isCopy(int code) {
      return code == 67 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isSelectAll(int code) {
      return code == 65 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   protected void initTabNavigation() {
      this.clearAndInit();
   }

   public void resize(MinecraftClient client, int width, int height) {
      this.width = width;
      this.height = height;
      this.initTabNavigation();
   }

   public static void wrapScreenError(Runnable task, String errorTitle, String screenName) {
      try {
         task.run();
      } catch (Throwable var6) {
         CrashReport lv = CrashReport.create(var6, errorTitle);
         CrashReportSection lv2 = lv.addElement("Affected screen");
         lv2.add("Screen name", () -> {
            return screenName;
         });
         throw new CrashException(lv);
      }
   }

   protected boolean isValidCharacterForName(String name, char character, int cursorPos) {
      int j = name.indexOf(58);
      int k = name.indexOf(47);
      if (character == ':') {
         return (k == -1 || cursorPos <= k) && j == -1;
      } else if (character == '/') {
         return cursorPos > j;
      } else {
         return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '.';
      }
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return true;
   }

   public void filesDragged(List paths) {
   }

   private void setScreenNarrationDelay(long delayMs, boolean restartElementNarration) {
      this.screenNarrationStartTime = Util.getMeasuringTimeMs() + delayMs;
      if (restartElementNarration) {
         this.elementNarrationStartTime = Long.MIN_VALUE;
      }

   }

   private void setElementNarrationDelay(long delayMs) {
      this.elementNarrationStartTime = Util.getMeasuringTimeMs() + delayMs;
   }

   public void applyMouseMoveNarratorDelay() {
      this.setScreenNarrationDelay(750L, false);
   }

   public void applyMousePressScrollNarratorDelay() {
      this.setScreenNarrationDelay(200L, true);
   }

   public void applyKeyPressNarratorDelay() {
      this.setScreenNarrationDelay(200L, true);
   }

   private boolean isNarratorActive() {
      return this.client.getNarratorManager().isActive();
   }

   public void updateNarrator() {
      if (this.isNarratorActive()) {
         long l = Util.getMeasuringTimeMs();
         if (l > this.screenNarrationStartTime && l > this.elementNarrationStartTime) {
            this.narrateScreen(true);
            this.screenNarrationStartTime = Long.MAX_VALUE;
         }
      }

   }

   public void narrateScreenIfNarrationEnabled(boolean onlyChangedNarrations) {
      if (this.isNarratorActive()) {
         this.narrateScreen(onlyChangedNarrations);
      }

   }

   private void narrateScreen(boolean onlyChangedNarrations) {
      this.narrator.buildNarrations(this::addScreenNarrations);
      String string = this.narrator.buildNarratorText(!onlyChangedNarrations);
      if (!string.isEmpty()) {
         this.client.getNarratorManager().narrate(string);
      }

   }

   protected boolean hasUsageText() {
      return true;
   }

   protected void addScreenNarrations(NarrationMessageBuilder messageBuilder) {
      messageBuilder.put(NarrationPart.TITLE, this.getNarratedTitle());
      if (this.hasUsageText()) {
         messageBuilder.put(NarrationPart.USAGE, SCREEN_USAGE_TEXT);
      }

      this.addElementNarrations(messageBuilder);
   }

   protected void addElementNarrations(NarrationMessageBuilder builder) {
      List list = (List)this.selectables.stream().filter(Selectable::isNarratable).collect(Collectors.toList());
      Collections.sort(list, Comparator.comparingInt(Navigable::getNavigationOrder));
      SelectedElementNarrationData lv = findSelectedElementData(list, this.selected);
      if (lv != null) {
         if (lv.selectType.isFocused()) {
            this.selected = lv.selectable;
         }

         if (list.size() > 1) {
            builder.put(NarrationPart.POSITION, (Text)Text.translatable("narrator.position.screen", lv.index + 1, list.size()));
            if (lv.selectType == Selectable.SelectionType.FOCUSED) {
               builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.component_list.usage"));
            }
         }

         lv.selectable.appendNarrations(builder.nextMessage());
      }

   }

   @Nullable
   public static SelectedElementNarrationData findSelectedElementData(List selectables, @Nullable Selectable selectable) {
      SelectedElementNarrationData lv = null;
      SelectedElementNarrationData lv2 = null;
      int i = 0;

      for(int j = selectables.size(); i < j; ++i) {
         Selectable lv3 = (Selectable)selectables.get(i);
         Selectable.SelectionType lv4 = lv3.getType();
         if (lv4.isFocused()) {
            if (lv3 != selectable) {
               return new SelectedElementNarrationData(lv3, i, lv4);
            }

            lv2 = new SelectedElementNarrationData(lv3, i, lv4);
         } else if (lv4.compareTo(lv != null ? lv.selectType : Selectable.SelectionType.NONE) > 0) {
            lv = new SelectedElementNarrationData(lv3, i, lv4);
         }
      }

      return lv != null ? lv : lv2;
   }

   public void applyNarratorModeChangeDelay() {
      this.setScreenNarrationDelay(NARRATOR_MODE_CHANGE_DELAY, false);
   }

   public void setTooltip(List tooltip) {
      this.setTooltip(tooltip, HoveredTooltipPositioner.INSTANCE, true);
   }

   public void setTooltip(List tooltip, TooltipPositioner positioner, boolean focused) {
      if (this.tooltip == null || focused) {
         this.tooltip = new PositionedTooltip(tooltip, positioner);
      }

   }

   protected void setTooltip(Text tooltip) {
      this.setTooltip(Tooltip.wrapLines(this.client, tooltip));
   }

   public void setTooltip(Tooltip tooltip, TooltipPositioner positioner, boolean focused) {
      this.setTooltip(tooltip.getLines(this.client), positioner, focused);
   }

   protected static void hide(ClickableWidget... widgets) {
      ClickableWidget[] var1 = widgets;
      int var2 = widgets.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         ClickableWidget lv = var1[var3];
         lv.visible = false;
      }

   }

   public ScreenRect getNavigationFocus() {
      return new ScreenRect(0, 0, this.width, this.height);
   }

   @Nullable
   public MusicSound getMusic() {
      return null;
   }

   static {
      SCREEN_INIT_NARRATION_DELAY = TimeUnit.SECONDS.toMillis(2L);
      NARRATOR_MODE_CHANGE_DELAY = SCREEN_INIT_NARRATION_DELAY;
   }

   @Environment(EnvType.CLIENT)
   private static record PositionedTooltip(List tooltip, TooltipPositioner positioner) {
      PositionedTooltip(List list, TooltipPositioner arg) {
         this.tooltip = list;
         this.positioner = arg;
      }

      public List tooltip() {
         return this.tooltip;
      }

      public TooltipPositioner positioner() {
         return this.positioner;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class SelectedElementNarrationData {
      public final Selectable selectable;
      public final int index;
      public final Selectable.SelectionType selectType;

      public SelectedElementNarrationData(Selectable selectable, int index, Selectable.SelectionType selectType) {
         this.selectable = selectable;
         this.index = index;
         this.selectType = selectType;
      }
   }
}
