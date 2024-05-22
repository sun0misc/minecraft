/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.advancement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record Advancement(Optional<Identifier> parent, Optional<AdvancementDisplay> display, AdvancementRewards rewards, Map<String, AdvancementCriterion<?>> criteria, AdvancementRequirements requirements, boolean sendsTelemetryEvent, Optional<Text> name) {
    private static final Codec<Map<String, AdvancementCriterion<?>>> CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, AdvancementCriterion.CODEC).validate(criteria -> criteria.isEmpty() ? DataResult.error(() -> "Advancement criteria cannot be empty") : DataResult.success(criteria));
    public static final Codec<Advancement> CODEC = RecordCodecBuilder.create(instance -> instance.group(Identifier.CODEC.optionalFieldOf("parent").forGetter(Advancement::parent), AdvancementDisplay.CODEC.optionalFieldOf("display").forGetter(Advancement::display), AdvancementRewards.CODEC.optionalFieldOf("rewards", AdvancementRewards.NONE).forGetter(Advancement::rewards), ((MapCodec)CRITERIA_CODEC.fieldOf("criteria")).forGetter(Advancement::criteria), AdvancementRequirements.CODEC.optionalFieldOf("requirements").forGetter(advancement -> Optional.of(advancement.requirements())), Codec.BOOL.optionalFieldOf("sends_telemetry_event", false).forGetter(Advancement::sendsTelemetryEvent)).apply((Applicative<Advancement, ?>)instance, (parent, display, rewards, criteria, requirements, sendsTelemetryEvent) -> {
        AdvancementRequirements lv = requirements.orElseGet(() -> AdvancementRequirements.allOf(criteria.keySet()));
        return new Advancement((Optional<Identifier>)parent, (Optional<AdvancementDisplay>)display, (AdvancementRewards)rewards, (Map<String, AdvancementCriterion<?>>)criteria, lv, (boolean)sendsTelemetryEvent);
    })).validate(Advancement::validate);
    public static final PacketCodec<RegistryByteBuf, Advancement> PACKET_CODEC = PacketCodec.of(Advancement::write, Advancement::read);

    public Advancement(Optional<Identifier> parent, Optional<AdvancementDisplay> display, AdvancementRewards rewards, Map<String, AdvancementCriterion<?>> criteria, AdvancementRequirements requirements, boolean sendsTelemetryEvent) {
        this(parent, display, rewards, Map.copyOf(criteria), requirements, sendsTelemetryEvent, display.map(Advancement::createNameFromDisplay));
    }

    private static DataResult<Advancement> validate(Advancement advancement) {
        return advancement.requirements().validate(advancement.criteria().keySet()).map(validated -> advancement);
    }

    private static Text createNameFromDisplay(AdvancementDisplay display) {
        Text lv = display.getTitle();
        Formatting lv2 = display.getFrame().getTitleFormat();
        MutableText lv3 = Texts.setStyleIfAbsent(lv.copy(), Style.EMPTY.withColor(lv2)).append("\n").append(display.getDescription());
        MutableText lv4 = lv.copy().styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, lv3)));
        return Texts.bracketed(lv4).formatted(lv2);
    }

    public static Text getNameFromIdentity(AdvancementEntry identifiedAdvancement) {
        return identifiedAdvancement.value().name().orElseGet(() -> Text.literal(identifiedAdvancement.id().toString()));
    }

    private void write(RegistryByteBuf buf) {
        buf.writeOptional(this.parent, PacketByteBuf::writeIdentifier);
        AdvancementDisplay.PACKET_CODEC.collect(PacketCodecs::optional).encode(buf, this.display);
        this.requirements.writeRequirements(buf);
        buf.writeBoolean(this.sendsTelemetryEvent);
    }

    private static Advancement read(RegistryByteBuf buf) {
        return new Advancement(buf.readOptional(PacketByteBuf::readIdentifier), (Optional)AdvancementDisplay.PACKET_CODEC.collect(PacketCodecs::optional).decode(buf), AdvancementRewards.NONE, Map.of(), new AdvancementRequirements(buf), buf.readBoolean());
    }

    public boolean isRoot() {
        return this.parent.isEmpty();
    }

    public void validate(ErrorReporter errorReporter, RegistryEntryLookup.RegistryLookup lookup) {
        this.criteria.forEach((name, criterion) -> {
            LootContextPredicateValidator lv = new LootContextPredicateValidator(errorReporter.makeChild((String)name), lookup);
            criterion.conditions().validate(lv);
        });
    }

    public static class Builder {
        private Optional<Identifier> parentObj = Optional.empty();
        private Optional<AdvancementDisplay> display = Optional.empty();
        private AdvancementRewards rewards = AdvancementRewards.NONE;
        private final ImmutableMap.Builder<String, AdvancementCriterion<?>> criteria = ImmutableMap.builder();
        private Optional<AdvancementRequirements> requirements = Optional.empty();
        private AdvancementRequirements.CriterionMerger merger = AdvancementRequirements.CriterionMerger.AND;
        private boolean sendsTelemetryEvent;

        public static Builder create() {
            return new Builder().sendsTelemetryEvent();
        }

        public static Builder createUntelemetered() {
            return new Builder();
        }

        public Builder parent(AdvancementEntry parent) {
            this.parentObj = Optional.of(parent.id());
            return this;
        }

        @Deprecated(forRemoval=true)
        public Builder parent(Identifier parentId) {
            this.parentObj = Optional.of(parentId);
            return this;
        }

        public Builder display(ItemStack icon, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
            return this.display(new AdvancementDisplay(icon, title, description, Optional.ofNullable(background), frame, showToast, announceToChat, hidden));
        }

        public Builder display(ItemConvertible icon, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
            return this.display(new AdvancementDisplay(new ItemStack(icon.asItem()), title, description, Optional.ofNullable(background), frame, showToast, announceToChat, hidden));
        }

        public Builder display(AdvancementDisplay display) {
            this.display = Optional.of(display);
            return this;
        }

        public Builder rewards(AdvancementRewards.Builder builder) {
            return this.rewards(builder.build());
        }

        public Builder rewards(AdvancementRewards rewards) {
            this.rewards = rewards;
            return this;
        }

        public Builder criterion(String name, AdvancementCriterion<?> criterion) {
            this.criteria.put(name, criterion);
            return this;
        }

        public Builder criteriaMerger(AdvancementRequirements.CriterionMerger merger) {
            this.merger = merger;
            return this;
        }

        public Builder requirements(AdvancementRequirements requirements) {
            this.requirements = Optional.of(requirements);
            return this;
        }

        public Builder sendsTelemetryEvent() {
            this.sendsTelemetryEvent = true;
            return this;
        }

        public AdvancementEntry build(Identifier id) {
            ImmutableMap<String, AdvancementCriterion<?>> map = this.criteria.buildOrThrow();
            AdvancementRequirements lv = this.requirements.orElseGet(() -> this.merger.create(map.keySet()));
            return new AdvancementEntry(id, new Advancement(this.parentObj, this.display, this.rewards, map, lv, this.sendsTelemetryEvent));
        }

        public AdvancementEntry build(Consumer<AdvancementEntry> exporter, String id) {
            AdvancementEntry lv = this.build(Identifier.method_60654(id));
            exporter.accept(lv);
            return lv;
        }
    }
}

