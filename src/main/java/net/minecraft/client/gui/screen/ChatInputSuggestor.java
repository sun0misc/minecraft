/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
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
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatInputSuggestor {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    private static final Style ERROR_STYLE = Style.EMPTY.withColor(Formatting.RED);
    private static final Style INFO_STYLE = Style.EMPTY.withColor(Formatting.GRAY);
    private static final List<Style> HIGHLIGHT_STYLES = Stream.of(Formatting.AQUA, Formatting.YELLOW, Formatting.GREEN, Formatting.LIGHT_PURPLE, Formatting.GOLD).map(Style.EMPTY::withColor).collect(ImmutableList.toImmutableList());
    final MinecraftClient client;
    private final Screen owner;
    final TextFieldWidget textField;
    final TextRenderer textRenderer;
    private final boolean slashOptional;
    private final boolean suggestingWhenEmpty;
    final int inWindowIndexOffset;
    final int maxSuggestionSize;
    final boolean chatScreenSized;
    final int color;
    private final List<OrderedText> messages = Lists.newArrayList();
    private int x;
    private int width;
    @Nullable
    private ParseResults<CommandSource> parse;
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Nullable
    private SuggestionWindow window;
    private boolean windowActive;
    boolean completingSuggestions;
    private boolean canLeave = true;

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

    public void setCanLeave(boolean canLeave) {
        this.canLeave = canLeave;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean bl;
        boolean bl2 = bl = this.window != null;
        if (bl && this.window.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.owner.getFocused() == this.textField && keyCode == 258 && (!this.canLeave || bl)) {
            this.show(true);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double amount) {
        return this.window != null && this.window.mouseScrolled(MathHelper.clamp(amount, -1.0, 1.0));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.window != null && this.window.mouseClicked((int)mouseX, (int)mouseY, button);
    }

    public void show(boolean narrateFirstSuggestion) {
        Suggestions suggestions;
        if (this.pendingSuggestions != null && this.pendingSuggestions.isDone() && !(suggestions = this.pendingSuggestions.join()).isEmpty()) {
            int i = 0;
            for (Suggestion suggestion : suggestions.getList()) {
                i = Math.max(i, this.textRenderer.getWidth(suggestion.getText()));
            }
            int j = MathHelper.clamp(this.textField.getCharacterX(suggestions.getRange().getStart()), 0, this.textField.getCharacterX(0) + this.textField.getInnerWidth() - i);
            int k = this.chatScreenSized ? this.owner.height - 12 : 72;
            this.window = new SuggestionWindow(j, k, i, this.sortSuggestions(suggestions), narrateFirstSuggestion);
        }
    }

    public boolean isOpen() {
        return this.window != null;
    }

    public Text getSuggestionUsageNarrationText() {
        if (this.window != null && this.window.completed) {
            if (this.canLeave) {
                return Text.translatable("narration.suggestion.usage.cycle.hidable");
            }
            return Text.translatable("narration.suggestion.usage.cycle.fixed");
        }
        if (this.canLeave) {
            return Text.translatable("narration.suggestion.usage.fill.hidable");
        }
        return Text.translatable("narration.suggestion.usage.fill.fixed");
    }

    public void clearWindow() {
        this.window = null;
    }

    private List<Suggestion> sortSuggestions(Suggestions suggestions) {
        String string = this.textField.getText().substring(0, this.textField.getCursor());
        int i = ChatInputSuggestor.getStartOfCurrentWord(string);
        String string2 = string.substring(i).toLowerCase(Locale.ROOT);
        ArrayList<Suggestion> list = Lists.newArrayList();
        ArrayList<Suggestion> list2 = Lists.newArrayList();
        for (Suggestion suggestion : suggestions.getList()) {
            if (suggestion.getText().startsWith(string2) || suggestion.getText().startsWith("minecraft:" + string2)) {
                list.add(suggestion);
                continue;
            }
            list2.add(suggestion);
        }
        list.addAll(list2);
        return list;
    }

    public void refresh() {
        boolean bl;
        String string = this.textField.getText();
        if (this.parse != null && !this.parse.getReader().getString().equals(string)) {
            this.parse = null;
        }
        if (!this.completingSuggestions) {
            this.textField.setSuggestion(null);
            this.window = null;
        }
        this.messages.clear();
        StringReader stringReader = new StringReader(string);
        boolean bl2 = bl = stringReader.canRead() && stringReader.peek() == '/';
        if (bl) {
            stringReader.skip();
        }
        boolean bl22 = this.slashOptional || bl;
        int i = this.textField.getCursor();
        if (bl22) {
            int j;
            CommandDispatcher<CommandSource> commandDispatcher = this.client.player.networkHandler.getCommandDispatcher();
            if (this.parse == null) {
                this.parse = commandDispatcher.parse(stringReader, (CommandSource)this.client.player.networkHandler.getCommandSource());
            }
            int n = j = this.suggestingWhenEmpty ? stringReader.getCursor() : 1;
            if (!(i < j || this.window != null && this.completingSuggestions)) {
                this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.parse, i);
                this.pendingSuggestions.thenRun(() -> {
                    if (!this.pendingSuggestions.isDone()) {
                        return;
                    }
                    this.showCommandSuggestions();
                });
            }
        } else {
            String string2 = string.substring(0, i);
            int j = ChatInputSuggestor.getStartOfCurrentWord(string2);
            Collection<String> collection = this.client.player.networkHandler.getCommandSource().getChatSuggestions();
            this.pendingSuggestions = CommandSource.suggestMatching(collection, new SuggestionsBuilder(string2, j));
        }
    }

    private static int getStartOfCurrentWord(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return 0;
        }
        int i = 0;
        Matcher matcher = WHITESPACE_PATTERN.matcher(input);
        while (matcher.find()) {
            i = matcher.end();
        }
        return i;
    }

    private static OrderedText formatException(CommandSyntaxException exception) {
        Text lv = Texts.toText(exception.getRawMessage());
        String string = exception.getContext();
        if (string == null) {
            return lv.asOrderedText();
        }
        return Text.translatable("command.context.parse_error", lv, exception.getCursor(), string).asOrderedText();
    }

    private void showCommandSuggestions() {
        boolean bl = false;
        if (this.textField.getCursor() == this.textField.getText().length()) {
            if (this.pendingSuggestions.join().isEmpty() && !this.parse.getExceptions().isEmpty()) {
                int i = 0;
                for (Map.Entry<CommandNode<CommandSource>, CommandSyntaxException> entry : this.parse.getExceptions().entrySet()) {
                    CommandSyntaxException commandSyntaxException = entry.getValue();
                    if (commandSyntaxException.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                        ++i;
                        continue;
                    }
                    this.messages.add(ChatInputSuggestor.formatException(commandSyntaxException));
                }
                if (i > 0) {
                    this.messages.add(ChatInputSuggestor.formatException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()));
                }
            } else if (this.parse.getReader().canRead()) {
                bl = true;
            }
        }
        this.x = 0;
        this.width = this.owner.width;
        if (this.messages.isEmpty() && !this.showUsages(Formatting.GRAY) && bl) {
            this.messages.add(ChatInputSuggestor.formatException(CommandManager.getException(this.parse)));
        }
        this.window = null;
        if (this.windowActive && this.client.options.getAutoSuggestions().getValue().booleanValue()) {
            this.show(false);
        }
    }

    private boolean showUsages(Formatting formatting) {
        CommandContextBuilder<CommandSource> commandContextBuilder = this.parse.getContext();
        SuggestionContext<CommandSource> suggestionContext = commandContextBuilder.findSuggestionContext(this.textField.getCursor());
        Map<CommandNode<CommandSource>, String> map = this.client.player.networkHandler.getCommandDispatcher().getSmartUsage(suggestionContext.parent, this.client.player.networkHandler.getCommandSource());
        ArrayList<OrderedText> list = Lists.newArrayList();
        int i = 0;
        Style lv = Style.EMPTY.withColor(formatting);
        for (Map.Entry<CommandNode<CommandSource>, String> entry : map.entrySet()) {
            if (entry.getKey() instanceof LiteralCommandNode) continue;
            list.add(OrderedText.styledForwardsVisitedString(entry.getValue(), lv));
            i = Math.max(i, this.textRenderer.getWidth(entry.getValue()));
        }
        if (!list.isEmpty()) {
            this.messages.addAll(list);
            this.x = MathHelper.clamp(this.textField.getCharacterX(suggestionContext.startPos), 0, this.textField.getCharacterX(0) + this.textField.getInnerWidth() - i);
            this.width = i;
            return true;
        }
        return false;
    }

    private OrderedText provideRenderText(String original, int firstCharacterIndex) {
        if (this.parse != null) {
            return ChatInputSuggestor.highlight(this.parse, original, firstCharacterIndex);
        }
        return OrderedText.styledForwardsVisitedString(original, Style.EMPTY);
    }

    @Nullable
    static String getSuggestionSuffix(String original, String suggestion) {
        if (suggestion.startsWith(original)) {
            return suggestion.substring(original.length());
        }
        return null;
    }

    private static OrderedText highlight(ParseResults<CommandSource> parse, String original, int firstCharacterIndex) {
        int n;
        ArrayList<OrderedText> list = Lists.newArrayList();
        int j = 0;
        int k = -1;
        CommandContextBuilder<CommandSource> commandContextBuilder = parse.getContext().getLastChild();
        for (ParsedArgument<CommandSource, ?> parsedArgument : commandContextBuilder.getArguments().values()) {
            int l;
            if (++k >= HIGHLIGHT_STYLES.size()) {
                k = 0;
            }
            if ((l = Math.max(parsedArgument.getRange().getStart() - firstCharacterIndex, 0)) >= original.length()) break;
            int m = Math.min(parsedArgument.getRange().getEnd() - firstCharacterIndex, original.length());
            if (m <= 0) continue;
            list.add(OrderedText.styledForwardsVisitedString(original.substring(j, l), INFO_STYLE));
            list.add(OrderedText.styledForwardsVisitedString(original.substring(l, m), HIGHLIGHT_STYLES.get(k)));
            j = m;
        }
        if (parse.getReader().canRead() && (n = Math.max(parse.getReader().getCursor() - firstCharacterIndex, 0)) < original.length()) {
            int o = Math.min(n + parse.getReader().getRemainingLength(), original.length());
            list.add(OrderedText.styledForwardsVisitedString(original.substring(j, n), INFO_STYLE));
            list.add(OrderedText.styledForwardsVisitedString(original.substring(n, o), ERROR_STYLE));
            j = o;
        }
        list.add(OrderedText.styledForwardsVisitedString(original.substring(j), INFO_STYLE));
        return OrderedText.concat(list);
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        if (!this.tryRenderWindow(context, mouseX, mouseY)) {
            this.renderMessages(context);
        }
    }

    public boolean tryRenderWindow(DrawContext context, int mouseX, int mouseY) {
        if (this.window != null) {
            this.window.render(context, mouseX, mouseY);
            return true;
        }
        return false;
    }

    public void renderMessages(DrawContext context) {
        int i = 0;
        for (OrderedText lv : this.messages) {
            int j = this.chatScreenSized ? this.owner.height - 14 - 13 - 12 * i : 72 + 12 * i;
            context.fill(this.x - 1, j, this.x + this.width + 1, j + 12, this.color);
            context.drawTextWithShadow(this.textRenderer, lv, this.x, j + 2, -1);
            ++i;
        }
    }

    public Text getNarration() {
        if (this.window != null) {
            return ScreenTexts.LINE_BREAK.copy().append(this.window.getNarration());
        }
        return ScreenTexts.EMPTY;
    }

    @Environment(value=EnvType.CLIENT)
    public class SuggestionWindow {
        private final Rect2i area;
        private final String typedText;
        private final List<Suggestion> suggestions;
        private int inWindowIndex;
        private int selection;
        private Vec2f mouse = Vec2f.ZERO;
        boolean completed;
        private int lastNarrationIndex;

        SuggestionWindow(int x, int y, int width, List<Suggestion> suggestions, boolean narrateFirstSuggestion) {
            int l = x - (ChatInputSuggestor.this.textField.drawsBackground() ? 0 : 1);
            int m = ChatInputSuggestor.this.chatScreenSized ? y - 3 - Math.min(suggestions.size(), ChatInputSuggestor.this.maxSuggestionSize) * 12 : y - (ChatInputSuggestor.this.textField.drawsBackground() ? 1 : 0);
            this.area = new Rect2i(l, m, width + 1, Math.min(suggestions.size(), ChatInputSuggestor.this.maxSuggestionSize) * 12);
            this.typedText = ChatInputSuggestor.this.textField.getText();
            this.lastNarrationIndex = narrateFirstSuggestion ? -1 : 0;
            this.suggestions = suggestions;
            this.select(0);
        }

        public void render(DrawContext context, int mouseX, int mouseY) {
            Message message;
            boolean bl4;
            int k = Math.min(this.suggestions.size(), ChatInputSuggestor.this.maxSuggestionSize);
            int l = -5592406;
            boolean bl = this.inWindowIndex > 0;
            boolean bl2 = this.suggestions.size() > this.inWindowIndex + k;
            boolean bl3 = bl || bl2;
            boolean bl5 = bl4 = this.mouse.x != (float)mouseX || this.mouse.y != (float)mouseY;
            if (bl4) {
                this.mouse = new Vec2f(mouseX, mouseY);
            }
            if (bl3) {
                int m;
                context.fill(this.area.getX(), this.area.getY() - 1, this.area.getX() + this.area.getWidth(), this.area.getY(), ChatInputSuggestor.this.color);
                context.fill(this.area.getX(), this.area.getY() + this.area.getHeight(), this.area.getX() + this.area.getWidth(), this.area.getY() + this.area.getHeight() + 1, ChatInputSuggestor.this.color);
                if (bl) {
                    for (m = 0; m < this.area.getWidth(); ++m) {
                        if (m % 2 != 0) continue;
                        context.fill(this.area.getX() + m, this.area.getY() - 1, this.area.getX() + m + 1, this.area.getY(), Colors.WHITE);
                    }
                }
                if (bl2) {
                    for (m = 0; m < this.area.getWidth(); ++m) {
                        if (m % 2 != 0) continue;
                        context.fill(this.area.getX() + m, this.area.getY() + this.area.getHeight(), this.area.getX() + m + 1, this.area.getY() + this.area.getHeight() + 1, Colors.WHITE);
                    }
                }
            }
            boolean bl52 = false;
            for (int n = 0; n < k; ++n) {
                Suggestion suggestion = this.suggestions.get(n + this.inWindowIndex);
                context.fill(this.area.getX(), this.area.getY() + 12 * n, this.area.getX() + this.area.getWidth(), this.area.getY() + 12 * n + 12, ChatInputSuggestor.this.color);
                if (mouseX > this.area.getX() && mouseX < this.area.getX() + this.area.getWidth() && mouseY > this.area.getY() + 12 * n && mouseY < this.area.getY() + 12 * n + 12) {
                    if (bl4) {
                        this.select(n + this.inWindowIndex);
                    }
                    bl52 = true;
                }
                context.drawTextWithShadow(ChatInputSuggestor.this.textRenderer, suggestion.getText(), this.area.getX() + 1, this.area.getY() + 2 + 12 * n, n + this.inWindowIndex == this.selection ? Colors.YELLOW : -5592406);
            }
            if (bl52 && (message = this.suggestions.get(this.selection).getTooltip()) != null) {
                context.drawTooltip(ChatInputSuggestor.this.textRenderer, Texts.toText(message), mouseX, mouseY);
            }
        }

        public boolean mouseClicked(int x, int y, int button) {
            if (!this.area.contains(x, y)) {
                return false;
            }
            int l = (y - this.area.getY()) / 12 + this.inWindowIndex;
            if (l >= 0 && l < this.suggestions.size()) {
                this.select(l);
                this.complete();
            }
            return true;
        }

        public boolean mouseScrolled(double amount) {
            int j;
            int i = (int)(ChatInputSuggestor.this.client.mouse.getX() * (double)ChatInputSuggestor.this.client.getWindow().getScaledWidth() / (double)ChatInputSuggestor.this.client.getWindow().getWidth());
            if (this.area.contains(i, j = (int)(ChatInputSuggestor.this.client.mouse.getY() * (double)ChatInputSuggestor.this.client.getWindow().getScaledHeight() / (double)ChatInputSuggestor.this.client.getWindow().getHeight()))) {
                this.inWindowIndex = MathHelper.clamp((int)((double)this.inWindowIndex - amount), 0, Math.max(this.suggestions.size() - ChatInputSuggestor.this.maxSuggestionSize, 0));
                return true;
            }
            return false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == 265) {
                this.scroll(-1);
                this.completed = false;
                return true;
            }
            if (keyCode == 264) {
                this.scroll(1);
                this.completed = false;
                return true;
            }
            if (keyCode == 258) {
                if (this.completed) {
                    this.scroll(Screen.hasShiftDown() ? -1 : 1);
                }
                this.complete();
                return true;
            }
            if (keyCode == 256) {
                ChatInputSuggestor.this.clearWindow();
                ChatInputSuggestor.this.textField.setSuggestion(null);
                return true;
            }
            return false;
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
            Suggestion suggestion = this.suggestions.get(this.selection);
            ChatInputSuggestor.this.textField.setSuggestion(ChatInputSuggestor.getSuggestionSuffix(ChatInputSuggestor.this.textField.getText(), suggestion.apply(this.typedText)));
            if (this.lastNarrationIndex != this.selection) {
                ChatInputSuggestor.this.client.getNarratorManager().narrate(this.getNarration());
            }
        }

        public void complete() {
            Suggestion suggestion = this.suggestions.get(this.selection);
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
            Suggestion suggestion = this.suggestions.get(this.selection);
            Message message = suggestion.getTooltip();
            if (message != null) {
                return Text.translatable("narration.suggestion.tooltip", this.selection + 1, this.suggestions.size(), suggestion.getText(), Text.of(message));
            }
            return Text.translatable("narration.suggestion", this.selection + 1, this.suggestions.size(), suggestion.getText());
        }
    }
}

