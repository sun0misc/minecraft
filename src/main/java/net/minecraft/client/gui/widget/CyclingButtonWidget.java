/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.widget;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CyclingButtonWidget<T>
extends PressableWidget {
    public static final BooleanSupplier HAS_ALT_DOWN = Screen::hasAltDown;
    private static final List<Boolean> BOOLEAN_VALUES = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
    private final Text optionText;
    private int index;
    private T value;
    private final Values<T> values;
    private final Function<T, Text> valueToText;
    private final Function<CyclingButtonWidget<T>, MutableText> narrationMessageFactory;
    private final UpdateCallback<T> callback;
    private final boolean optionTextOmitted;
    private final SimpleOption.TooltipFactory<T> tooltipFactory;

    CyclingButtonWidget(int x, int y, int width, int height, Text message, Text optionText, int index, T value, Values<T> values, Function<T, Text> valueToText, Function<CyclingButtonWidget<T>, MutableText> narrationMessageFactory, UpdateCallback<T> callback, SimpleOption.TooltipFactory<T> tooltipFactory, boolean optionTextOmitted) {
        super(x, y, width, height, message);
        this.optionText = optionText;
        this.index = index;
        this.value = value;
        this.values = values;
        this.valueToText = valueToText;
        this.narrationMessageFactory = narrationMessageFactory;
        this.callback = callback;
        this.optionTextOmitted = optionTextOmitted;
        this.tooltipFactory = tooltipFactory;
        this.refreshTooltip();
    }

    private void refreshTooltip() {
        this.setTooltip(this.tooltipFactory.apply(this.value));
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycle(-1);
        } else {
            this.cycle(1);
        }
    }

    private void cycle(int amount) {
        List<T> list = this.values.getCurrent();
        this.index = MathHelper.floorMod(this.index + amount, list.size());
        T object = list.get(this.index);
        this.internalSetValue(object);
        this.callback.onValueChange(this, object);
    }

    private T getValue(int offset) {
        List<T> list = this.values.getCurrent();
        return list.get(MathHelper.floorMod(this.index + offset, list.size()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0.0) {
            this.cycle(-1);
        } else if (verticalAmount < 0.0) {
            this.cycle(1);
        }
        return true;
    }

    public void setValue(T value) {
        List<T> list = this.values.getCurrent();
        int i = list.indexOf(value);
        if (i != -1) {
            this.index = i;
        }
        this.internalSetValue(value);
    }

    private void internalSetValue(T value) {
        Text lv = this.composeText(value);
        this.setMessage(lv);
        this.value = value;
        this.refreshTooltip();
    }

    private Text composeText(T value) {
        return this.optionTextOmitted ? this.valueToText.apply(value) : this.composeGenericOptionText(value);
    }

    private MutableText composeGenericOptionText(T value) {
        return ScreenTexts.composeGenericOptionText(this.optionText, this.valueToText.apply(value));
    }

    public T getValue() {
        return this.value;
    }

    @Override
    protected MutableText getNarrationMessage() {
        return this.narrationMessageFactory.apply(this);
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, (Text)this.getNarrationMessage());
        if (this.active) {
            T object = this.getValue(1);
            Text lv = this.composeText(object);
            if (this.isFocused()) {
                builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.cycle_button.usage.focused", lv));
            } else {
                builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.cycle_button.usage.hovered", lv));
            }
        }
    }

    public MutableText getGenericNarrationMessage() {
        return CyclingButtonWidget.getNarrationMessage(this.optionTextOmitted ? this.composeGenericOptionText(this.value) : this.getMessage());
    }

    public static <T> Builder<T> builder(Function<T, Text> valueToText) {
        return new Builder<T>(valueToText);
    }

    public static Builder<Boolean> onOffBuilder(Text on, Text off) {
        return new Builder<Boolean>(value -> value != false ? on : off).values((Collection<Boolean>)BOOLEAN_VALUES);
    }

    public static Builder<Boolean> onOffBuilder() {
        return new Builder<Boolean>(value -> value != false ? ScreenTexts.ON : ScreenTexts.OFF).values((Collection<Boolean>)BOOLEAN_VALUES);
    }

    public static Builder<Boolean> onOffBuilder(boolean initialValue) {
        return CyclingButtonWidget.onOffBuilder().initially(initialValue);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Values<T> {
        public List<T> getCurrent();

        public List<T> getDefaults();

        public static <T> Values<T> of(Collection<T> values) {
            final ImmutableList<T> list = ImmutableList.copyOf(values);
            return new Values<T>(){

                @Override
                public List<T> getCurrent() {
                    return list;
                }

                @Override
                public List<T> getDefaults() {
                    return list;
                }
            };
        }

        public static <T> Values<T> of(final BooleanSupplier alternativeToggle, List<T> defaults, List<T> alternatives) {
            final ImmutableList<T> list3 = ImmutableList.copyOf(defaults);
            final ImmutableList<T> list4 = ImmutableList.copyOf(alternatives);
            return new Values<T>(){

                @Override
                public List<T> getCurrent() {
                    return alternativeToggle.getAsBoolean() ? list4 : list3;
                }

                @Override
                public List<T> getDefaults() {
                    return list3;
                }
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface UpdateCallback<T> {
        public void onValueChange(CyclingButtonWidget<T> var1, T var2);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T value;
        private final Function<T, Text> valueToText;
        private SimpleOption.TooltipFactory<T> tooltipFactory = value -> null;
        private Function<CyclingButtonWidget<T>, MutableText> narrationMessageFactory = CyclingButtonWidget::getGenericNarrationMessage;
        private Values<T> values = Values.of(ImmutableList.of());
        private boolean optionTextOmitted;

        public Builder(Function<T, Text> valueToText) {
            this.valueToText = valueToText;
        }

        public Builder<T> values(Collection<T> values) {
            return this.values(Values.of(values));
        }

        @SafeVarargs
        public final Builder<T> values(T ... values) {
            return this.values((Collection<T>)ImmutableList.copyOf(values));
        }

        public Builder<T> values(List<T> defaults, List<T> alternatives) {
            return this.values(Values.of(HAS_ALT_DOWN, defaults, alternatives));
        }

        public Builder<T> values(BooleanSupplier alternativeToggle, List<T> defaults, List<T> alternatives) {
            return this.values(Values.of(alternativeToggle, defaults, alternatives));
        }

        public Builder<T> values(Values<T> values) {
            this.values = values;
            return this;
        }

        public Builder<T> tooltip(SimpleOption.TooltipFactory<T> tooltipFactory) {
            this.tooltipFactory = tooltipFactory;
            return this;
        }

        public Builder<T> initially(T value) {
            this.value = value;
            int i = this.values.getDefaults().indexOf(value);
            if (i != -1) {
                this.initialIndex = i;
            }
            return this;
        }

        public Builder<T> narration(Function<CyclingButtonWidget<T>, MutableText> narrationMessageFactory) {
            this.narrationMessageFactory = narrationMessageFactory;
            return this;
        }

        public Builder<T> omitKeyText() {
            this.optionTextOmitted = true;
            return this;
        }

        public CyclingButtonWidget<T> build(Text optionText, UpdateCallback<T> callback) {
            return this.build(0, 0, 150, 20, optionText, callback);
        }

        public CyclingButtonWidget<T> build(int x, int y, int width, int height, Text optionText) {
            return this.build(x, y, width, height, optionText, (button, value) -> {});
        }

        public CyclingButtonWidget<T> build(int x, int y, int width, int height, Text optionText, UpdateCallback<T> callback) {
            List<T> list = this.values.getDefaults();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            }
            T object = this.value != null ? this.value : list.get(this.initialIndex);
            Text lv = this.valueToText.apply(object);
            Text lv2 = this.optionTextOmitted ? lv : ScreenTexts.composeGenericOptionText(optionText, lv);
            return new CyclingButtonWidget<T>(x, y, width, height, lv2, optionText, this.initialIndex, object, this.values, this.valueToText, this.narrationMessageFactory, callback, this.tooltipFactory, this.optionTextOmitted);
        }
    }
}

