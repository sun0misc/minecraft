/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.context;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContextAware;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.util.ErrorReporter;

public class LootContextType {
    private final Set<LootContextParameter<?>> required;
    private final Set<LootContextParameter<?>> allowed;

    LootContextType(Set<LootContextParameter<?>> required, Set<LootContextParameter<?>> allowed) {
        this.required = ImmutableSet.copyOf(required);
        this.allowed = ImmutableSet.copyOf(Sets.union(required, allowed));
    }

    public boolean isAllowed(LootContextParameter<?> parameter) {
        return this.allowed.contains(parameter);
    }

    public Set<LootContextParameter<?>> getRequired() {
        return this.required;
    }

    public Set<LootContextParameter<?>> getAllowed() {
        return this.allowed;
    }

    public String toString() {
        return "[" + Joiner.on(", ").join(this.allowed.stream().map(parameter -> (this.required.contains(parameter) ? "!" : "") + String.valueOf(parameter.getId())).iterator()) + "]";
    }

    public void validate(LootTableReporter lootTableReporter, LootContextAware parameterConsumer) {
        this.validate(lootTableReporter.getErrorReporter(), parameterConsumer);
    }

    public void validate(ErrorReporter errorReporter, LootContextAware parameterConsumer) {
        Set<LootContextParameter<?>> set = parameterConsumer.getRequiredParameters();
        Sets.SetView<LootContextParameter<?>> set2 = Sets.difference(set, this.allowed);
        if (!set2.isEmpty()) {
            errorReporter.report("Parameters " + String.valueOf(set2) + " are not provided in this context");
        }
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private final Set<LootContextParameter<?>> required = Sets.newIdentityHashSet();
        private final Set<LootContextParameter<?>> allowed = Sets.newIdentityHashSet();

        public Builder require(LootContextParameter<?> parameter) {
            if (this.allowed.contains(parameter)) {
                throw new IllegalArgumentException("Parameter " + String.valueOf(parameter.getId()) + " is already optional");
            }
            this.required.add(parameter);
            return this;
        }

        public Builder allow(LootContextParameter<?> parameter) {
            if (this.required.contains(parameter)) {
                throw new IllegalArgumentException("Parameter " + String.valueOf(parameter.getId()) + " is already required");
            }
            this.allowed.add(parameter);
            return this;
        }

        public LootContextType build() {
            return new LootContextType(this.required, this.allowed);
        }
    }
}

