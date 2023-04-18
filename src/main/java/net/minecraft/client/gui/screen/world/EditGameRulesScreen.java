package net.minecraft.client.gui.screen.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class EditGameRulesScreen extends Screen {
   private final Consumer ruleSaver;
   private RuleListWidget ruleListWidget;
   private final Set invalidRuleWidgets = Sets.newHashSet();
   private ButtonWidget doneButton;
   @Nullable
   private List field_24297;
   private final GameRules gameRules;

   public EditGameRulesScreen(GameRules gameRules, Consumer ruleSaveConsumer) {
      super(Text.translatable("editGamerule.title"));
      this.gameRules = gameRules;
      this.ruleSaver = ruleSaveConsumer;
   }

   protected void init() {
      this.ruleListWidget = new RuleListWidget(this.gameRules);
      this.addSelectableChild(this.ruleListWidget);
      GridWidget.Adder lv = (new GridWidget()).setColumnSpacing(10).createAdder(2);
      this.doneButton = (ButtonWidget)lv.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.ruleSaver.accept(Optional.of(this.gameRules));
      }).build());
      lv.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.ruleSaver.accept(Optional.empty());
      }).build());
      lv.getGridWidget().forEachChild((child) -> {
         ClickableWidget var10000 = (ClickableWidget)this.addDrawableChild(child);
      });
      lv.getGridWidget().setPosition(this.width / 2 - 155, this.height - 28);
      lv.getGridWidget().refreshPositions();
   }

   public void close() {
      this.ruleSaver.accept(Optional.empty());
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.field_24297 = null;
      this.ruleListWidget.render(matrices, mouseX, mouseY, delta);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }

   private void updateDoneButton() {
      this.doneButton.active = this.invalidRuleWidgets.isEmpty();
   }

   void markInvalid(AbstractRuleWidget ruleWidget) {
      this.invalidRuleWidgets.add(ruleWidget);
      this.updateDoneButton();
   }

   void markValid(AbstractRuleWidget ruleWidget) {
      this.invalidRuleWidgets.remove(ruleWidget);
      this.updateDoneButton();
   }

   @Environment(EnvType.CLIENT)
   public class RuleListWidget extends ElementListWidget {
      public RuleListWidget(final GameRules gameRules) {
         super(EditGameRulesScreen.this.client, EditGameRulesScreen.this.width, EditGameRulesScreen.this.height, 43, EditGameRulesScreen.this.height - 32, 24);
         final Map map = Maps.newHashMap();
         GameRules.accept(new GameRules.Visitor() {
            public void visitBoolean(GameRules.Key key, GameRules.Type type) {
               this.createRuleWidget(key, (name, description, ruleName, rule) -> {
                  return EditGameRulesScreen.thisx.new BooleanRuleWidget(name, description, ruleName, rule);
               });
            }

            public void visitInt(GameRules.Key key, GameRules.Type type) {
               this.createRuleWidget(key, (name, description, ruleName, rule) -> {
                  return EditGameRulesScreen.thisx.new IntRuleWidget(name, description, ruleName, rule);
               });
            }

            private void createRuleWidget(GameRules.Key key, RuleWidgetFactory widgetFactory) {
               Text lv = Text.translatable(key.getTranslationKey());
               Text lv2 = Text.literal(key.getName()).formatted(Formatting.YELLOW);
               GameRules.Rule lv3 = gameRules.get(key);
               String string = lv3.serialize();
               Text lv4 = Text.translatable("editGamerule.default", Text.literal(string)).formatted(Formatting.GRAY);
               String string2 = key.getTranslationKey() + ".description";
               ImmutableList list;
               String string3;
               if (I18n.hasTranslation(string2)) {
                  ImmutableList.Builder builder = ImmutableList.builder().add(lv2.asOrderedText());
                  Text lv5 = Text.translatable(string2);
                  List var10000 = EditGameRulesScreen.this.textRenderer.wrapLines(lv5, 150);
                  Objects.requireNonNull(builder);
                  var10000.forEach(builder::add);
                  list = builder.add(lv4.asOrderedText()).build();
                  String var13 = lv5.getString();
                  string3 = var13 + "\n" + lv4.getString();
               } else {
                  list = ImmutableList.of(lv2.asOrderedText(), lv4.asOrderedText());
                  string3 = lv4.getString();
               }

               ((Map)map.computeIfAbsent(key.getCategory(), (category) -> {
                  return Maps.newHashMap();
               })).put(key, widgetFactory.create(lv, list, string3, lv3));
            }
         });
         map.entrySet().stream().sorted(Entry.comparingByKey()).forEach((entry) -> {
            this.addEntry(EditGameRulesScreen.this.new RuleCategoryWidget(Text.translatable(((GameRules.Category)entry.getKey()).getCategory()).formatted(Formatting.BOLD, Formatting.YELLOW)));
            ((Map)entry.getValue()).entrySet().stream().sorted(Entry.comparingByKey(Comparator.comparing(GameRules.Key::getName))).forEach((e) -> {
               this.addEntry((AbstractRuleWidget)e.getValue());
            });
         });
      }

      public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         super.render(matrices, mouseX, mouseY, delta);
         AbstractRuleWidget lv = (AbstractRuleWidget)this.getHoveredEntry();
         if (lv != null && lv.description != null) {
            EditGameRulesScreen.this.setTooltip(lv.description);
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public class IntRuleWidget extends NamedRuleWidget {
      private final TextFieldWidget valueWidget;

      public IntRuleWidget(Text name, List description, String ruleName, GameRules.IntRule rule) {
         super(description, name);
         this.valueWidget = new TextFieldWidget(EditGameRulesScreen.this.client.textRenderer, 10, 5, 42, 20, name.copy().append("\n").append(ruleName).append("\n"));
         this.valueWidget.setText(Integer.toString(rule.get()));
         this.valueWidget.setChangedListener((value) -> {
            if (rule.validate(value)) {
               this.valueWidget.setEditableColor(14737632);
               EditGameRulesScreen.this.markValid(this);
            } else {
               this.valueWidget.setEditableColor(16711680);
               EditGameRulesScreen.this.markInvalid(this);
            }

         });
         this.children.add(this.valueWidget);
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.drawName(matrices, y, x);
         this.valueWidget.setX(x + entryWidth - 44);
         this.valueWidget.setY(y);
         this.valueWidget.render(matrices, mouseX, mouseY, tickDelta);
      }
   }

   @Environment(EnvType.CLIENT)
   public class BooleanRuleWidget extends NamedRuleWidget {
      private final CyclingButtonWidget toggleButton;

      public BooleanRuleWidget(Text name, List description, String ruleName, GameRules.BooleanRule rule) {
         super(description, name);
         this.toggleButton = CyclingButtonWidget.onOffBuilder(rule.get()).omitKeyText().narration((button) -> {
            return button.getGenericNarrationMessage().append("\n").append(ruleName);
         }).build(10, 5, 44, 20, name, (button, value) -> {
            rule.set(value, (MinecraftServer)null);
         });
         this.children.add(this.toggleButton);
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.drawName(matrices, y, x);
         this.toggleButton.setX(x + entryWidth - 45);
         this.toggleButton.setY(y);
         this.toggleButton.render(matrices, mouseX, mouseY, tickDelta);
      }
   }

   @Environment(EnvType.CLIENT)
   public abstract class NamedRuleWidget extends AbstractRuleWidget {
      private final List name;
      protected final List children = Lists.newArrayList();

      public NamedRuleWidget(@Nullable List description, Text name) {
         super(description);
         this.name = EditGameRulesScreen.this.client.textRenderer.wrapLines(name, 175);
      }

      public List children() {
         return this.children;
      }

      public List selectableChildren() {
         return this.children;
      }

      protected void drawName(MatrixStack matrices, int x, int y) {
         if (this.name.size() == 1) {
            EditGameRulesScreen.this.client.textRenderer.draw(matrices, (OrderedText)this.name.get(0), (float)y, (float)(x + 5), 16777215);
         } else if (this.name.size() >= 2) {
            EditGameRulesScreen.this.client.textRenderer.draw(matrices, (OrderedText)this.name.get(0), (float)y, (float)x, 16777215);
            EditGameRulesScreen.this.client.textRenderer.draw(matrices, (OrderedText)this.name.get(1), (float)y, (float)(x + 10), 16777215);
         }

      }
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   private interface RuleWidgetFactory {
      AbstractRuleWidget create(Text name, List description, String ruleName, GameRules.Rule rule);
   }

   @Environment(EnvType.CLIENT)
   public class RuleCategoryWidget extends AbstractRuleWidget {
      final Text name;

      public RuleCategoryWidget(Text text) {
         super((List)null);
         this.name = text;
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         DrawableHelper.drawCenteredTextWithShadow(matrices, EditGameRulesScreen.this.client.textRenderer, this.name, x + entryWidth / 2, y + 5, 16777215);
      }

      public List children() {
         return ImmutableList.of();
      }

      public List selectableChildren() {
         return ImmutableList.of(new Selectable() {
            public Selectable.SelectionType getType() {
               return Selectable.SelectionType.HOVERED;
            }

            public void appendNarrations(NarrationMessageBuilder builder) {
               builder.put(NarrationPart.TITLE, RuleCategoryWidget.this.name);
            }
         });
      }
   }

   @Environment(EnvType.CLIENT)
   public abstract static class AbstractRuleWidget extends ElementListWidget.Entry {
      @Nullable
      final List description;

      public AbstractRuleWidget(@Nullable List description) {
         this.description = description;
      }
   }
}
