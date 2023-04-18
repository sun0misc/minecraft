package net.minecraft.client.gui.hud;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Nullables;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ChatHud extends DrawableHelper {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_MESSAGES = 100;
   private static final int MISSING_MESSAGE_INDEX = -1;
   private static final int field_39772 = 4;
   private static final int field_39773 = 4;
   private static final int field_40389 = 40;
   private static final int field_40390 = 60;
   private static final Text DELETED_MARKER_TEXT;
   private final MinecraftClient client;
   private final List messageHistory = Lists.newArrayList();
   private final List messages = Lists.newArrayList();
   private final List visibleMessages = Lists.newArrayList();
   private int scrolledLines;
   private boolean hasUnreadNewMessages;
   private final List removalQueue = new ArrayList();

   public ChatHud(MinecraftClient client) {
      this.client = client;
   }

   public void tickRemovalQueueIfExists() {
      if (!this.removalQueue.isEmpty()) {
         this.tickRemovalQueue();
      }

   }

   public void render(MatrixStack matrices, int currentTick, int mouseX, int mouseY) {
      if (!this.isChatHidden()) {
         int l = this.getVisibleLineCount();
         int m = this.visibleMessages.size();
         if (m > 0) {
            boolean bl = this.isChatFocused();
            float f = (float)this.getChatScale();
            int n = MathHelper.ceil((float)this.getWidth() / f);
            int o = this.client.getWindow().getScaledHeight();
            matrices.push();
            matrices.scale(f, f, 1.0F);
            matrices.translate(4.0F, 0.0F, 0.0F);
            int p = MathHelper.floor((float)(o - 40) / f);
            int q = this.getMessageIndex(this.toChatLineX((double)mouseX), this.toChatLineY((double)mouseY));
            double d = (Double)this.client.options.getChatOpacity().getValue() * 0.8999999761581421 + 0.10000000149011612;
            double e = (Double)this.client.options.getTextBackgroundOpacity().getValue();
            double g = (Double)this.client.options.getChatLineSpacing().getValue();
            int r = this.getLineHeight();
            int s = (int)Math.round(-8.0 * (g + 1.0) + 4.0 * g);
            int t = 0;

            int w;
            int x;
            int y;
            int aa;
            for(int u = 0; u + this.scrolledLines < this.visibleMessages.size() && u < l; ++u) {
               int v = u + this.scrolledLines;
               ChatHudLine.Visible lv = (ChatHudLine.Visible)this.visibleMessages.get(v);
               if (lv != null) {
                  w = currentTick - lv.addedTime();
                  if (w < 200 || bl) {
                     double h = bl ? 1.0 : getMessageOpacityMultiplier(w);
                     x = (int)(255.0 * h * d);
                     y = (int)(255.0 * h * e);
                     ++t;
                     if (x > 3) {
                        int z = false;
                        aa = p - u * r;
                        int ab = aa + s;
                        matrices.push();
                        matrices.translate(0.0F, 0.0F, 50.0F);
                        fill(matrices, -4, aa - r, 0 + n + 4 + 4, aa, y << 24);
                        MessageIndicator lv2 = lv.indicator();
                        if (lv2 != null) {
                           int ac = lv2.indicatorColor() | x << 24;
                           fill(matrices, -4, aa - r, -2, aa, ac);
                           if (v == q && lv2.icon() != null) {
                              int ad = this.getIndicatorX(lv);
                              Objects.requireNonNull(this.client.textRenderer);
                              int ae = ab + 9;
                              this.drawIndicatorIcon(matrices, ad, ae, lv2.icon());
                           }
                        }

                        matrices.translate(0.0F, 0.0F, 50.0F);
                        this.client.textRenderer.drawWithShadow(matrices, lv.content(), 0.0F, (float)ab, 16777215 + (x << 24));
                        matrices.pop();
                     }
                  }
               }
            }

            long af = this.client.getMessageHandler().getUnprocessedMessageCount();
            int ag;
            if (af > 0L) {
               ag = (int)(128.0 * d);
               w = (int)(255.0 * e);
               matrices.push();
               matrices.translate(0.0F, (float)p, 50.0F);
               fill(matrices, -2, 0, n + 4, 9, w << 24);
               matrices.translate(0.0F, 0.0F, 50.0F);
               this.client.textRenderer.drawWithShadow(matrices, (Text)Text.translatable("chat.queue", af), 0.0F, 1.0F, 16777215 + (ag << 24));
               matrices.pop();
            }

            if (bl) {
               ag = this.getLineHeight();
               w = m * ag;
               int ah = t * ag;
               int ai = this.scrolledLines * ah / m - p;
               x = ah * ah / w;
               if (w != ah) {
                  y = ai > 0 ? 170 : 96;
                  int z = this.hasUnreadNewMessages ? 13382451 : 3355562;
                  aa = n + 4;
                  fill(matrices, aa, -ai, aa + 2, -ai - x, z + (y << 24));
                  fill(matrices, aa + 2, -ai, aa + 1, -ai - x, 13421772 + (y << 24));
               }
            }

            matrices.pop();
         }
      }
   }

   private void drawIndicatorIcon(MatrixStack matrices, int x, int y, MessageIndicator.Icon icon) {
      int k = y - icon.height - 1;
      icon.draw(matrices, x, k);
   }

   private int getIndicatorX(ChatHudLine.Visible line) {
      return this.client.textRenderer.getWidth(line.content()) + 4;
   }

   private boolean isChatHidden() {
      return this.client.options.getChatVisibility().getValue() == ChatVisibility.HIDDEN;
   }

   private static double getMessageOpacityMultiplier(int age) {
      double d = (double)age / 200.0;
      d = 1.0 - d;
      d *= 10.0;
      d = MathHelper.clamp(d, 0.0, 1.0);
      d *= d;
      return d;
   }

   public void clear(boolean clearHistory) {
      this.client.getMessageHandler().processAll();
      this.removalQueue.clear();
      this.visibleMessages.clear();
      this.messages.clear();
      if (clearHistory) {
         this.messageHistory.clear();
      }

   }

   public void addMessage(Text message) {
      this.addMessage(message, (MessageSignatureData)null, this.client.isConnectedToLocalServer() ? MessageIndicator.singlePlayer() : MessageIndicator.system());
   }

   public void addMessage(Text message, @Nullable MessageSignatureData signature, @Nullable MessageIndicator indicator) {
      this.logChatMessage(message, indicator);
      this.addMessage(message, signature, this.client.inGameHud.getTicks(), indicator, false);
   }

   private void logChatMessage(Text message, @Nullable MessageIndicator indicator) {
      String string = message.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
      String string2 = (String)Nullables.map(indicator, MessageIndicator::loggedName);
      if (string2 != null) {
         LOGGER.info("[{}] [CHAT] {}", string2, string);
      } else {
         LOGGER.info("[CHAT] {}", string);
      }

   }

   private void addMessage(Text message, @Nullable MessageSignatureData signature, int ticks, @Nullable MessageIndicator indicator, boolean refresh) {
      int j = MathHelper.floor((double)this.getWidth() / this.getChatScale());
      if (indicator != null && indicator.icon() != null) {
         j -= indicator.icon().width + 4 + 2;
      }

      List list = ChatMessages.breakRenderedChatMessageLines(message, j, this.client.textRenderer);
      boolean bl2 = this.isChatFocused();

      for(int k = 0; k < list.size(); ++k) {
         OrderedText lv = (OrderedText)list.get(k);
         if (bl2 && this.scrolledLines > 0) {
            this.hasUnreadNewMessages = true;
            this.scroll(1);
         }

         boolean bl3 = k == list.size() - 1;
         this.visibleMessages.add(0, new ChatHudLine.Visible(ticks, lv, indicator, bl3));
      }

      while(this.visibleMessages.size() > 100) {
         this.visibleMessages.remove(this.visibleMessages.size() - 1);
      }

      if (!refresh) {
         this.messages.add(0, new ChatHudLine(ticks, message, signature, indicator));

         while(this.messages.size() > 100) {
            this.messages.remove(this.messages.size() - 1);
         }
      }

   }

   private void tickRemovalQueue() {
      int i = this.client.inGameHud.getTicks();
      this.removalQueue.removeIf((message) -> {
         if (i >= message.deletableAfter()) {
            return this.queueForRemoval(message.signature()) == null;
         } else {
            return false;
         }
      });
   }

   public void removeMessage(MessageSignatureData signature) {
      RemovalQueuedMessage lv = this.queueForRemoval(signature);
      if (lv != null) {
         this.removalQueue.add(lv);
      }

   }

   @Nullable
   private RemovalQueuedMessage queueForRemoval(MessageSignatureData signature) {
      int i = this.client.inGameHud.getTicks();
      ListIterator listIterator = this.messages.listIterator();

      ChatHudLine lv;
      do {
         if (!listIterator.hasNext()) {
            return null;
         }

         lv = (ChatHudLine)listIterator.next();
      } while(!signature.equals(lv.signature()));

      int j = lv.creationTick() + 60;
      if (i >= j) {
         listIterator.set(this.createRemovalMarker(lv));
         this.refresh();
         return null;
      } else {
         return new RemovalQueuedMessage(signature, j);
      }
   }

   private ChatHudLine createRemovalMarker(ChatHudLine original) {
      return new ChatHudLine(original.creationTick(), DELETED_MARKER_TEXT, (MessageSignatureData)null, MessageIndicator.system());
   }

   public void reset() {
      this.resetScroll();
      this.refresh();
   }

   private void refresh() {
      this.visibleMessages.clear();

      for(int i = this.messages.size() - 1; i >= 0; --i) {
         ChatHudLine lv = (ChatHudLine)this.messages.get(i);
         this.addMessage(lv.content(), lv.signature(), lv.creationTick(), lv.indicator(), true);
      }

   }

   public List getMessageHistory() {
      return this.messageHistory;
   }

   public void addToMessageHistory(String message) {
      if (this.messageHistory.isEmpty() || !((String)this.messageHistory.get(this.messageHistory.size() - 1)).equals(message)) {
         this.messageHistory.add(message);
      }

   }

   public void resetScroll() {
      this.scrolledLines = 0;
      this.hasUnreadNewMessages = false;
   }

   public void scroll(int scroll) {
      this.scrolledLines += scroll;
      int j = this.visibleMessages.size();
      if (this.scrolledLines > j - this.getVisibleLineCount()) {
         this.scrolledLines = j - this.getVisibleLineCount();
      }

      if (this.scrolledLines <= 0) {
         this.scrolledLines = 0;
         this.hasUnreadNewMessages = false;
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY) {
      if (this.isChatFocused() && !this.client.options.hudHidden && !this.isChatHidden()) {
         MessageHandler lv = this.client.getMessageHandler();
         if (lv.getUnprocessedMessageCount() == 0L) {
            return false;
         } else {
            double f = mouseX - 2.0;
            double g = (double)this.client.getWindow().getScaledHeight() - mouseY - 40.0;
            if (f <= (double)MathHelper.floor((double)this.getWidth() / this.getChatScale()) && g < 0.0 && g > (double)MathHelper.floor(-9.0 * this.getChatScale())) {
               lv.process();
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @Nullable
   public Style getTextStyleAt(double x, double y) {
      double f = this.toChatLineX(x);
      double g = this.toChatLineY(y);
      int i = this.getMessageLineIndex(f, g);
      if (i >= 0 && i < this.visibleMessages.size()) {
         ChatHudLine.Visible lv = (ChatHudLine.Visible)this.visibleMessages.get(i);
         return this.client.textRenderer.getTextHandler().getStyleAt(lv.content(), MathHelper.floor(f));
      } else {
         return null;
      }
   }

   @Nullable
   public MessageIndicator getIndicatorAt(double mouseX, double mouseY) {
      double f = this.toChatLineX(mouseX);
      double g = this.toChatLineY(mouseY);
      int i = this.getMessageIndex(f, g);
      if (i >= 0 && i < this.visibleMessages.size()) {
         ChatHudLine.Visible lv = (ChatHudLine.Visible)this.visibleMessages.get(i);
         MessageIndicator lv2 = lv.indicator();
         if (lv2 != null && this.isXInsideIndicatorIcon(f, lv, lv2)) {
            return lv2;
         }
      }

      return null;
   }

   private boolean isXInsideIndicatorIcon(double x, ChatHudLine.Visible line, MessageIndicator indicator) {
      if (x < 0.0) {
         return true;
      } else {
         MessageIndicator.Icon lv = indicator.icon();
         if (lv == null) {
            return false;
         } else {
            int i = this.getIndicatorX(line);
            int j = i + lv.width;
            return x >= (double)i && x <= (double)j;
         }
      }
   }

   private double toChatLineX(double x) {
      return x / this.getChatScale() - 4.0;
   }

   private double toChatLineY(double y) {
      double e = (double)this.client.getWindow().getScaledHeight() - y - 40.0;
      return e / (this.getChatScale() * (double)this.getLineHeight());
   }

   private int getMessageIndex(double chatLineX, double chatLineY) {
      int i = this.getMessageLineIndex(chatLineX, chatLineY);
      if (i == -1) {
         return -1;
      } else {
         while(i >= 0) {
            if (((ChatHudLine.Visible)this.visibleMessages.get(i)).endOfEntry()) {
               return i;
            }

            --i;
         }

         return i;
      }
   }

   private int getMessageLineIndex(double chatLineX, double chatLineY) {
      if (this.isChatFocused() && !this.client.options.hudHidden && !this.isChatHidden()) {
         if (!(chatLineX < -4.0) && !(chatLineX > (double)MathHelper.floor((double)this.getWidth() / this.getChatScale()))) {
            int i = Math.min(this.getVisibleLineCount(), this.visibleMessages.size());
            if (chatLineY >= 0.0 && chatLineY < (double)i) {
               int j = MathHelper.floor(chatLineY + (double)this.scrolledLines);
               if (j >= 0 && j < this.visibleMessages.size()) {
                  return j;
               }
            }

            return -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private boolean isChatFocused() {
      return this.client.currentScreen instanceof ChatScreen;
   }

   public int getWidth() {
      return getWidth((Double)this.client.options.getChatWidth().getValue());
   }

   public int getHeight() {
      return getHeight(this.isChatFocused() ? (Double)this.client.options.getChatHeightFocused().getValue() : (Double)this.client.options.getChatHeightUnfocused().getValue());
   }

   public double getChatScale() {
      return (Double)this.client.options.getChatScale().getValue();
   }

   public static int getWidth(double widthOption) {
      int i = true;
      int j = true;
      return MathHelper.floor(widthOption * 280.0 + 40.0);
   }

   public static int getHeight(double heightOption) {
      int i = true;
      int j = true;
      return MathHelper.floor(heightOption * 160.0 + 20.0);
   }

   public static double getDefaultUnfocusedHeight() {
      int i = true;
      int j = true;
      return 70.0 / (double)(getHeight(1.0) - 20);
   }

   public int getVisibleLineCount() {
      return this.getHeight() / this.getLineHeight();
   }

   private int getLineHeight() {
      Objects.requireNonNull(this.client.textRenderer);
      return (int)(9.0 * ((Double)this.client.options.getChatLineSpacing().getValue() + 1.0));
   }

   static {
      DELETED_MARKER_TEXT = Text.translatable("chat.deleted_marker").formatted(Formatting.GRAY, Formatting.ITALIC);
   }

   @Environment(EnvType.CLIENT)
   private static record RemovalQueuedMessage(MessageSignatureData signature, int deletableAfter) {
      RemovalQueuedMessage(MessageSignatureData arg, int i) {
         this.signature = arg;
         this.deletableAfter = i;
      }

      public MessageSignatureData signature() {
         return this.signature;
      }

      public int deletableAfter() {
         return this.deletableAfter;
      }
   }
}
