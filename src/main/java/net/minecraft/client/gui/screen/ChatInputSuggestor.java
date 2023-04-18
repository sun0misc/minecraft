package net.minecraft.client.gui.screen;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChatInputSuggestor {
   private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
   private static final Style ERROR_STYLE;
   private static final Style INFO_STYLE;
   private static final List HIGHLIGHT_STYLES;
   final MinecraftClient client;
   final Screen owner;
   final TextFieldWidget textField;
   final TextRenderer textRenderer;
   private final boolean slashOptional;
   private final boolean suggestingWhenEmpty;
   final int inWindowIndexOffset;
   final int maxSuggestionSize;
   final boolean chatScreenSized;
   final int color;
   private final List messages = Lists.newArrayList();
   private int x;
   private int width;
   @Nullable
   private ParseResults parse;
   @Nullable
   private CompletableFuture pendingSuggestions;
   @Nullable
   private SuggestionWindow window;
   private boolean windowActive;
   boolean completingSuggestions;

   public ChatInputSuggestor(MinecraftClient client, Screen owner, TextFieldWidget textField, TextRenderer textRenderer, boolean slashOptional, boolean suggestingWhenEmpty, int inWindowIndexOffset, int maxSuggestionSize, boolean chatScreenSized, int color) {
      this.client = client;
      this.owner = owner;
      this.textField = textField;
      this.textRenderer = textRenderer;
      this.slashOptional = slashOptional;
      this.suggestingWhenEmpty = suggestingWhenEmpty;
      this.inWindowIndexOffset = inWindowIndexOffset;
      this.maxSuggestionSize = maxSuggestionSize;
      this.chatScreenSized = chatScreenSized;
      this.color = color;
      textField.setRenderTextProvider(this::provideRenderText);
   }

   public void setWindowActive(boolean windowActive) {
      this.windowActive = windowActive;
      if (!windowActive) {
         this.window = null;
      }

   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.window != null && this.window.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (this.owner.getFocused() == this.textField && keyCode == 258) {
         this.show(true);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double amount) {
      return this.window != null && this.window.mouseScrolled(MathHelper.clamp(amount, -1.0, 1.0));
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.window != null && this.window.mouseClicked((int)mouseX, (int)mouseY, button);
   }

   public void show(boolean narrateFirstSuggestion) {
      if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
         Suggestions suggestions = (Suggestions)this.pendingSuggestions.join();
         if (!suggestions.isEmpty()) {
            int i = 0;

            Suggestion suggestion;
            for(Iterator var4 = suggestions.getList().iterator(); var4.hasNext(); i = Math.max(i, this.textRenderer.getWidth(suggestion.getText()))) {
               suggestion = (Suggestion)var4.next();
            }

            int j = MathHelper.clamp(this.textField.getCharacterX(suggestions.getRange().getStart()), 0, this.textField.getCharacterX(0) + this.textField.getInnerWidth() - i);
            int k = this.chatScreenSized ? this.owner.height - 12 : 72;
            this.window = new SuggestionWindow(j, k, i, this.sortSuggestions(suggestions), narrateFirstSuggestion);
         }
      }

   }

   public void clearWindow() {
      this.window = null;
   }

   private List sortSuggestions(Suggestions suggestions) {
      String string = this.textField.getText().substring(0, this.textField.getCursor());
      int i = getStartOfCurrentWord(string);
      String string2 = string.substring(i).toLowerCase(Locale.ROOT);
      List list = Lists.newArrayList();
      List list2 = Lists.newArrayList();
      Iterator var7 = suggestions.getList().iterator();

      while(true) {
         while(var7.hasNext()) {
            Suggestion suggestion = (Suggestion)var7.next();
            if (!suggestion.getText().startsWith(string2) && !suggestion.getText().startsWith("minecraft:" + string2)) {
               list2.add(suggestion);
            } else {
               list.add(suggestion);
            }
         }

         list.addAll(list2);
         return list;
      }
   }

   public void refresh() {
      String string = this.textField.getText();
      if (this.parse != null && !this.parse.getReader().getString().equals(string)) {
         this.parse = null;
      }

      if (!this.completingSuggestions) {
         this.textField.setSuggestion((String)null);
         this.window = null;
      }

      this.messages.clear();
      StringReader stringReader = new StringReader(string);
      boolean bl = stringReader.canRead() && stringReader.peek() == '/';
      if (bl) {
         stringReader.skip();
      }

      boolean bl2 = this.slashOptional || bl;
      int i = this.textField.getCursor();
      int j;
      if (bl2) {
         CommandDispatcher commandDispatcher = this.client.player.networkHandler.getCommandDispatcher();
         if (this.parse == null) {
            this.parse = commandDispatcher.parse(stringReader, this.client.player.networkHandler.getCommandSource());
         }

         j = this.suggestingWhenEmpty ? stringReader.getCursor() : 1;
         if (i >= j && (this.window == null || !this.completingSuggestions)) {
            this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.parse, i);
            this.pendingSuggestions.thenRun(() -> {
               if (this.pendingSuggestions.isDone()) {
                  this.showCommandSuggestions();
               }
            });
         }
      } else {
         String string2 = string.substring(0, i);
         j = getStartOfCurrentWord(string2);
         Collection collection = this.client.player.networkHandler.getCommandSource().getChatSuggestions();
         this.pendingSuggestions = CommandSource.suggestMatching((Iterable)collection, new SuggestionsBuilder(string2, j));
      }

   }

   private static int getStartOfCurrentWord(String input) {
      if (Strings.isNullOrEmpty(input)) {
         return 0;
      } else {
         int i = 0;

         for(Matcher matcher = WHITESPACE_PATTERN.matcher(input); matcher.find(); i = matcher.end()) {
         }

         return i;
      }
   }

   private static OrderedText formatException(CommandSyntaxException exception) {
      Text lv = Texts.toText(exception.getRawMessage());
      String string = exception.getContext();
      return string == null ? lv.asOrderedText() : Text.translatable("command.context.parse_error", lv, exception.getCursor(), string).asOrderedText();
   }

   private void showCommandSuggestions() {
      if (this.textField.getCursor() == this.textField.getText().length()) {
         if (((Suggestions)this.pendingSuggestions.join()).isEmpty() && !this.parse.getExceptions().isEmpty()) {
            int i = 0;
            Iterator var2 = this.parse.getExceptions().entrySet().iterator();

            while(var2.hasNext()) {
               Map.Entry entry = (Map.Entry)var2.next();
               CommandSyntaxException commandSyntaxException = (CommandSyntaxException)entry.getValue();
               if (commandSyntaxException.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                  ++i;
               } else {
                  this.messages.add(formatException(commandSyntaxException));
               }
            }

            if (i > 0) {
               this.messages.add(formatException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()));
            }
         } else if (this.parse.getReader().canRead()) {
            this.messages.add(formatException(CommandManager.getException(this.parse)));
         }
      }

      this.x = 0;
      this.width = this.owner.width;
      if (this.messages.isEmpty()) {
         this.showUsages(Formatting.GRAY);
      }

      this.window = null;
      if (this.windowActive && (Boolean)this.client.options.getAutoSuggestions().getValue()) {
         this.show(false);
      }

   }

   private void showUsages(Formatting formatting) {
      CommandContextBuilder commandContextBuilder = this.parse.getContext();
      SuggestionContext suggestionContext = commandContextBuilder.findSuggestionContext(this.textField.getCursor());
      Map map = this.client.player.networkHandler.getCommandDispatcher().getSmartUsage(suggestionContext.parent, this.client.player.networkHandler.getCommandSource());
      List list = Lists.newArrayList();
      int i = 0;
      Style lv = Style.EMPTY.withColor(formatting);
      Iterator var8 = map.entrySet().iterator();

      while(var8.hasNext()) {
         Map.Entry entry = (Map.Entry)var8.next();
         if (!(entry.getKey() instanceof LiteralCommandNode)) {
            list.add(OrderedText.styledForwardsVisitedString((String)entry.getValue(), lv));
            i = Math.max(i, this.textRenderer.getWidth((String)entry.getValue()));
         }
      }

      if (!list.isEmpty()) {
         this.messages.addAll(list);
         this.x = MathHelper.clamp(this.textField.getCharacterX(suggestionContext.startPos), 0, this.textField.getCharacterX(0) + this.textField.getInnerWidth() - i);
         this.width = i;
      }

   }

   private OrderedText provideRenderText(String original, int firstCharacterIndex) {
      return this.parse != null ? highlight(this.parse, original, firstCharacterIndex) : OrderedText.styledForwardsVisitedString(original, Style.EMPTY);
   }

   @Nullable
   static String getSuggestionSuffix(String original, String suggestion) {
      return suggestion.startsWith(original) ? suggestion.substring(original.length()) : null;
   }

   private static OrderedText highlight(ParseResults parse, String original, int firstCharacterIndex) {
      List list = Lists.newArrayList();
      int j = 0;
      int k = -1;
      CommandContextBuilder commandContextBuilder = parse.getContext().getLastChild();
      Iterator var7 = commandContextBuilder.getArguments().values().iterator();

      while(var7.hasNext()) {
         ParsedArgument parsedArgument = (ParsedArgument)var7.next();
         ++k;
         if (k >= HIGHLIGHT_STYLES.size()) {
            k = 0;
         }

         int l = Math.max(parsedArgument.getRange().getStart() - firstCharacterIndex, 0);
         if (l >= original.length()) {
            break;
         }

         int m = Math.min(parsedArgument.getRange().getEnd() - firstCharacterIndex, original.length());
         if (m > 0) {
            list.add(OrderedText.styledForwardsVisitedString(original.substring(j, l), INFO_STYLE));
            list.add(OrderedText.styledForwardsVisitedString(original.substring(l, m), (Style)HIGHLIGHT_STYLES.get(k)));
            j = m;
         }
      }

      if (parse.getReader().canRead()) {
         int n = Math.max(parse.getReader().getCursor() - firstCharacterIndex, 0);
         if (n < original.length()) {
            int o = Math.min(n + parse.getReader().getRemainingLength(), original.length());
            list.add(OrderedText.styledForwardsVisitedString(original.substring(j, n), INFO_STYLE));
            list.add(OrderedText.styledForwardsVisitedString(original.substring(n, o), ERROR_STYLE));
            j = o;
         }
      }

      list.add(OrderedText.styledForwardsVisitedString(original.substring(j), INFO_STYLE));
      return OrderedText.concat((List)list);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY) {
      if (!this.tryRenderWindow(matrices, mouseX, mouseY)) {
         this.renderMessages(matrices);
      }

   }

   public boolean tryRenderWindow(MatrixStack matrices, int mouseX, int mouseY) {
      if (this.window != null) {
         this.window.render(matrices, mouseX, mouseY);
         return true;
      } else {
         return false;
      }
   }

   public void renderMessages(MatrixStack matrices) {
      int i = 0;

      for(Iterator var3 = this.messages.iterator(); var3.hasNext(); ++i) {
         OrderedText lv = (OrderedText)var3.next();
         int j = this.chatScreenSized ? this.owner.height - 14 - 13 - 12 * i : 72 + 12 * i;
         DrawableHelper.fill(matrices, this.x - 1, j, this.x + this.width + 1, j + 12, this.color);
         this.textRenderer.drawWithShadow(matrices, (OrderedText)lv, (float)this.x, (float)(j + 2), -1);
      }

   }

   public Text getNarration() {
      return (Text)(this.window != null ? ScreenTexts.LINE_BREAK.copy().append(this.window.getNarration()) : ScreenTexts.EMPTY);
   }

   static {
      ERROR_STYLE = Style.EMPTY.withColor(Formatting.RED);
      INFO_STYLE = Style.EMPTY.withColor(Formatting.GRAY);
      Stream var10000 = Stream.of(Formatting.AQUA, Formatting.YELLOW, Formatting.GREEN, Formatting.LIGHT_PURPLE, Formatting.GOLD);
      Style var10001 = Style.EMPTY;
      Objects.requireNonNull(var10001);
      HIGHLIGHT_STYLES = (List)var10000.map(var10001::withColor).collect(ImmutableList.toImmutableList());
   }

   @Environment(EnvType.CLIENT)
   public class SuggestionWindow {
      private final Rect2i area;
      private final String typedText;
      private final List suggestions;
      private int inWindowIndex;
      private int selection;
      private Vec2f mouse;
      private boolean completed;
      private int lastNarrationIndex;

      SuggestionWindow(int x, int y, int width, List suggestions, boolean narrateFirstSuggestion) {
         this.mouse = Vec2f.ZERO;
         int l = x - 1;
         int m = ChatInputSuggestor.this.chatScreenSized ? y - 3 - Math.min(suggestions.size(), ChatInputSuggestor.this.maxSuggestionSize) * 12 : y;
         this.area = new Rect2i(l, m, width + 1, Math.min(suggestions.size(), ChatInputSuggestor.this.maxSuggestionSize) * 12);
         this.typedText = ChatInputSuggestor.this.textField.getText();
         this.lastNarrationIndex = narrateFirstSuggestion ? -1 : 0;
         this.suggestions = suggestions;
         this.select(0);
      }

      public void render(MatrixStack matrices, int mouseX, int mouseY) {
         int k = Math.min(this.suggestions.size(), ChatInputSuggestor.this.maxSuggestionSize);
         int l = -5592406;
         boolean bl = this.inWindowIndex > 0;
         boolean bl2 = this.suggestions.size() > this.inWindowIndex + k;
         boolean bl3 = bl || bl2;
         boolean bl4 = this.mouse.x != (float)mouseX || this.mouse.y != (float)mouseY;
         if (bl4) {
            this.mouse = new Vec2f((float)mouseX, (float)mouseY);
         }

         if (bl3) {
            DrawableHelper.fill(matrices, this.area.getX(), this.area.getY() - 1, this.area.getX() + this.area.getWidth(), this.area.getY(), ChatInputSuggestor.this.color);
            DrawableHelper.fill(matrices, this.area.getX(), this.area.getY() + this.area.getHeight(), this.area.getX() + this.area.getWidth(), this.area.getY() + this.area.getHeight() + 1, ChatInputSuggestor.this.color);
            int m;
            if (bl) {
               for(m = 0; m < this.area.getWidth(); ++m) {
                  if (m % 2 == 0) {
                     DrawableHelper.fill(matrices, this.area.getX() + m, this.area.getY() - 1, this.area.getX() + m + 1, this.area.getY(), -1);
                  }
               }
            }

            if (bl2) {
               for(m = 0; m < this.area.getWidth(); ++m) {
                  if (m % 2 == 0) {
                     DrawableHelper.fill(matrices, this.area.getX() + m, this.area.getY() + this.area.getHeight(), this.area.getX() + m + 1, this.area.getY() + this.area.getHeight() + 1, -1);
                  }
               }
            }
         }

         boolean bl5 = false;

         for(int n = 0; n < k; ++n) {
            Suggestion suggestion = (Suggestion)this.suggestions.get(n + this.inWindowIndex);
            DrawableHelper.fill(matrices, this.area.getX(), this.area.getY() + 12 * n, this.area.getX() + this.area.getWidth(), this.area.getY() + 12 * n + 12, ChatInputSuggestor.this.color);
            if (mouseX > this.area.getX() && mouseX < this.area.getX() + this.area.getWidth() && mouseY > this.area.getY() + 12 * n && mouseY < this.area.getY() + 12 * n + 12) {
               if (bl4) {
                  this.select(n + this.inWindowIndex);
               }

               bl5 = true;
            }

            ChatInputSuggestor.this.textRenderer.drawWithShadow(matrices, suggestion.getText(), (float)(this.area.getX() + 1), (float)(this.area.getY() + 2 + 12 * n), n + this.inWindowIndex == this.selection ? -256 : -5592406);
         }

         if (bl5) {
            Message message = ((Suggestion)this.suggestions.get(this.selection)).getTooltip();
            if (message != null) {
               ChatInputSuggestor.this.owner.renderTooltip(matrices, Texts.toText(message), mouseX, mouseY);
            }
         }

      }

      public boolean mouseClicked(int x, int y, int button) {
         if (!this.area.contains(x, y)) {
            return false;
         } else {
            int l = (y - this.area.getY()) / 12 + this.inWindowIndex;
            if (l >= 0 && l < this.suggestions.size()) {
               this.select(l);
               this.complete();
            }

            return true;
         }
      }

      public boolean mouseScrolled(double amount) {
         int i = (int)(ChatInputSuggestor.this.client.mouse.getX() * (double)ChatInputSuggestor.this.client.getWindow().getScaledWidth() / (double)ChatInputSuggestor.this.client.getWindow().getWidth());
         int j = (int)(ChatInputSuggestor.this.client.mouse.getY() * (double)ChatInputSuggestor.this.client.getWindow().getScaledHeight() / (double)ChatInputSuggestor.this.client.getWindow().getHeight());
         if (this.area.contains(i, j)) {
            this.inWindowIndex = MathHelper.clamp((int)((double)this.inWindowIndex - amount), 0, Math.max(this.suggestions.size() - ChatInputSuggestor.this.maxSuggestionSize, 0));
            return true;
         } else {
            return false;
         }
      }

      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         if (keyCode == 265) {
            this.scroll(-1);
            this.completed = false;
            return true;
         } else if (keyCode == 264) {
            this.scroll(1);
            this.completed = false;
            return true;
         } else if (keyCode == 258) {
            if (this.completed) {
               this.scroll(Screen.hasShiftDown() ? -1 : 1);
            }

            this.complete();
            return true;
         } else if (keyCode == 256) {
            ChatInputSuggestor.this.clearWindow();
            return true;
         } else {
            return false;
         }
      }

      public void scroll(int offset) {
         this.select(this.selection + offset);
         int j = this.inWindowIndex;
         int k = this.inWindowIndex + ChatInputSuggestor.this.maxSuggestionSize - 1;
         if (this.selection < j) {
            this.inWindowIndex = MathHelper.clamp(this.selection, 0, Math.max(this.suggestions.size() - ChatInputSuggestor.this.maxSuggestionSize, 0));
         } else if (this.selection > k) {
            this.inWindowIndex = MathHelper.clamp(this.selection + ChatInputSuggestor.this.inWindowIndexOffset - ChatInputSuggestor.this.maxSuggestionSize, 0, Math.max(this.suggestions.size() - ChatInputSuggestor.this.maxSuggestionSize, 0));
         }

      }

      public void select(int index) {
         this.selection = index;
         if (this.selection < 0) {
            this.selection += this.suggestions.size();
         }

         if (this.selection >= this.suggestions.size()) {
            this.selection -= this.suggestions.size();
         }

         Suggestion suggestion = (Suggestion)this.suggestions.get(this.selection);
         ChatInputSuggestor.this.textField.setSuggestion(ChatInputSuggestor.getSuggestionSuffix(ChatInputSuggestor.this.textField.getText(), suggestion.apply(this.typedText)));
         if (this.lastNarrationIndex != this.selection) {
            ChatInputSuggestor.this.client.getNarratorManager().narrate(this.getNarration());
         }

      }

      public void complete() {
         Suggestion suggestion = (Suggestion)this.suggestions.get(this.selection);
         ChatInputSuggestor.this.completingSuggestions = true;
         ChatInputSuggestor.this.textField.setText(suggestion.apply(this.typedText));
         int i = suggestion.getRange().getStart() + suggestion.getText().length();
         ChatInputSuggestor.this.textField.setSelectionStart(i);
         ChatInputSuggestor.this.textField.setSelectionEnd(i);
         this.select(this.selection);
         ChatInputSuggestor.this.completingSuggestions = false;
         this.completed = true;
      }

      Text getNarration() {
         this.lastNarrationIndex = this.selection;
         Suggestion suggestion = (Suggestion)this.suggestions.get(this.selection);
         Message message = suggestion.getTooltip();
         return message != null ? Text.translatable("narration.suggestion.tooltip", this.selection + 1, this.suggestions.size(), suggestion.getText(), message) : Text.translatable("narration.suggestion", this.selection + 1, this.suggestions.size(), suggestion.getText());
      }
   }
}
