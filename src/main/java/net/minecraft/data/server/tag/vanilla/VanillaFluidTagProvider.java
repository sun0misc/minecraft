/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.tag.ValueLookupTagProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;

public class VanillaFluidTagProvider
extends ValueLookupTagProvider<Fluid> {
    public VanillaFluidTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        super(output, RegistryKeys.FLUID, registryLookupFuture, (T fluid) -> fluid.getRegistryEntry().registryKey());
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup lookup) {
        ((ValueLookupTagProvider.ObjectBuilder)this.getOrCreateTagBuilder((TagKey)FluidTags.WATER)).add(Fluids.WATER, Fluids.FLOWING_WATER);
        ((ValueLookupTagProvider.ObjectBuilder)this.getOrCreateTagBuilder((TagKey)FluidTags.LAVA)).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
    }
}

