/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.class_9792;
import net.minecraft.class_9793;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

public record class_9790(Optional<RegistryEntryList<class_9793>> song) implements ComponentSubPredicate<class_9792>
{
    public static final Codec<class_9790> field_52020 = RecordCodecBuilder.create(instance -> instance.group(RegistryCodecs.entryList(RegistryKeys.JUKEBOX_SONG).optionalFieldOf("song").forGetter(class_9790::song)).apply((Applicative<class_9790, ?>)instance, class_9790::new));

    @Override
    public ComponentType<class_9792> getComponentType() {
        return DataComponentTypes.JUKEBOX_PLAYABLE;
    }

    @Override
    public boolean test(ItemStack arg, class_9792 arg2) {
        if (this.song.isPresent()) {
            boolean bl = false;
            for (RegistryEntry registryEntry : this.song.get()) {
                Optional optional = registryEntry.getKey();
                if (optional.isEmpty() || optional.get() != arg2.song().key()) continue;
                bl = true;
                break;
            }
            return bl;
        }
        return true;
    }

    public static class_9790 method_60732() {
        return new class_9790(Optional.empty());
    }
}

