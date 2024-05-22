/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.state.property;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.state.property.Property;

public class BooleanProperty
extends Property<Boolean> {
    private final ImmutableSet<Boolean> values = ImmutableSet.of(Boolean.valueOf(true), Boolean.valueOf(false));

    protected BooleanProperty(String name) {
        super(name, Boolean.class);
    }

    @Override
    public Collection<Boolean> getValues() {
        return this.values;
    }

    public static BooleanProperty of(String name) {
        return new BooleanProperty(name);
    }

    @Override
    public Optional<Boolean> parse(String name) {
        if ("true".equals(name) || "false".equals(name)) {
            return Optional.of(Boolean.valueOf(name));
        }
        return Optional.empty();
    }

    @Override
    public String name(Boolean boolean_) {
        return boolean_.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof BooleanProperty) {
            BooleanProperty lv = (BooleanProperty)object;
            if (super.equals(object)) {
                return this.values.equals(lv.values);
            }
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return 31 * super.computeHashCode() + this.values.hashCode();
    }
}

