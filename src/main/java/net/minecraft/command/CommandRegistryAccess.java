/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;

public interface CommandRegistryAccess
extends RegistryWrapper.WrapperLookup {
    public static CommandRegistryAccess of(final RegistryWrapper.WrapperLookup wrapperLookup, final FeatureSet enabledFeatures) {
        return new CommandRegistryAccess(){

            @Override
            public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
                return wrapperLookup.streamAllRegistryKeys();
            }

            @Override
            public <T> Optional<RegistryWrapper.Impl<T>> getOptionalWrapper(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return wrapperLookup.getOptionalWrapper(registryRef).map(wrapper -> wrapper.withFeatureFilter(enabledFeatures));
            }
        };
    }
}

