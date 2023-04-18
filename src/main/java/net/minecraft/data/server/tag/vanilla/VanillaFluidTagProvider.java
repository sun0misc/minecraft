package net.minecraft.data.server.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.tag.ValueLookupTagProvider;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.FluidTags;

public class VanillaFluidTagProvider extends ValueLookupTagProvider {
   public VanillaFluidTagProvider(DataOutput output, CompletableFuture registryLookupFuture) {
      super(output, RegistryKeys.FLUID, registryLookupFuture, (fluid) -> {
         return fluid.getRegistryEntry().registryKey();
      });
   }

   protected void configure(RegistryWrapper.WrapperLookup lookup) {
      this.getOrCreateTagBuilder(FluidTags.WATER).add((Object[])(Fluids.WATER, Fluids.FLOWING_WATER));
      this.getOrCreateTagBuilder(FluidTags.LAVA).add((Object[])(Fluids.LAVA, Fluids.FLOWING_LAVA));
   }
}
