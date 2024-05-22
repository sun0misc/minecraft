/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.Property;
import net.minecraft.util.Util;

public record DebugStickStateComponent(Map<RegistryEntry<Block>, Property<?>> properties) {
    public static final DebugStickStateComponent DEFAULT = new DebugStickStateComponent(Map.of());
    public static final Codec<DebugStickStateComponent> CODEC = Codec.dispatchedMap(Registries.BLOCK.getEntryCodec(), block -> Codec.STRING.comapFlatMap(property -> {
        Property<?> lv = ((Block)block.value()).getStateManager().getProperty((String)property);
        return lv != null ? DataResult.success(lv) : DataResult.error(() -> "No property on " + block.getIdAsString() + " with name: " + property);
    }, Property::getName)).xmap(DebugStickStateComponent::new, DebugStickStateComponent::properties);

    public DebugStickStateComponent with(RegistryEntry<Block> block, Property<?> property) {
        return new DebugStickStateComponent(Util.mapWith(this.properties, block, property));
    }
}

