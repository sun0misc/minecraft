package net.minecraft.client.world;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.DataPackContents;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.WorldGenSettings;

@Environment(EnvType.CLIENT)
public record GeneratorOptionsHolder(GeneratorOptions generatorOptions, Registry dimensionOptionsRegistry, DimensionOptionsRegistryHolder selectedDimensions, CombinedDynamicRegistries combinedDynamicRegistries, DataPackContents dataPackContents, DataConfiguration dataConfiguration) {
   public GeneratorOptionsHolder(WorldGenSettings worldGenSettings, CombinedDynamicRegistries combinedDynamicRegistries, DataPackContents dataPackContents, DataConfiguration dataConfiguration) {
      this(worldGenSettings.generatorOptions(), worldGenSettings.dimensionOptionsRegistryHolder(), combinedDynamicRegistries, dataPackContents, dataConfiguration);
   }

   public GeneratorOptionsHolder(GeneratorOptions generatorOptions, DimensionOptionsRegistryHolder selectedDimensions, CombinedDynamicRegistries combinedDynamicRegistries, DataPackContents dataPackContents, DataConfiguration dataConfiguration) {
      this(generatorOptions, combinedDynamicRegistries.get(ServerDynamicRegistryType.DIMENSIONS).get(RegistryKeys.DIMENSION), selectedDimensions, combinedDynamicRegistries.with(ServerDynamicRegistryType.DIMENSIONS, (DynamicRegistryManager.Immutable[])()), dataPackContents, dataConfiguration);
   }

   public GeneratorOptionsHolder(GeneratorOptions arg, Registry arg2, DimensionOptionsRegistryHolder arg3, CombinedDynamicRegistries arg4, DataPackContents arg5, DataConfiguration arg6) {
      this.generatorOptions = arg;
      this.dimensionOptionsRegistry = arg2;
      this.selectedDimensions = arg3;
      this.combinedDynamicRegistries = arg4;
      this.dataPackContents = arg5;
      this.dataConfiguration = arg6;
   }

   public GeneratorOptionsHolder with(GeneratorOptions generatorOptions, DimensionOptionsRegistryHolder selectedDimensions) {
      return new GeneratorOptionsHolder(generatorOptions, this.dimensionOptionsRegistry, selectedDimensions, this.combinedDynamicRegistries, this.dataPackContents, this.dataConfiguration);
   }

   public GeneratorOptionsHolder apply(Modifier modifier) {
      return new GeneratorOptionsHolder((GeneratorOptions)modifier.apply(this.generatorOptions), this.dimensionOptionsRegistry, this.selectedDimensions, this.combinedDynamicRegistries, this.dataPackContents, this.dataConfiguration);
   }

   public GeneratorOptionsHolder apply(RegistryAwareModifier modifier) {
      return new GeneratorOptionsHolder(this.generatorOptions, this.dimensionOptionsRegistry, (DimensionOptionsRegistryHolder)modifier.apply(this.getCombinedRegistryManager(), this.selectedDimensions), this.combinedDynamicRegistries, this.dataPackContents, this.dataConfiguration);
   }

   public DynamicRegistryManager.Immutable getCombinedRegistryManager() {
      return this.combinedDynamicRegistries.getCombinedRegistryManager();
   }

   public GeneratorOptions generatorOptions() {
      return this.generatorOptions;
   }

   public Registry dimensionOptionsRegistry() {
      return this.dimensionOptionsRegistry;
   }

   public DimensionOptionsRegistryHolder selectedDimensions() {
      return this.selectedDimensions;
   }

   public CombinedDynamicRegistries combinedDynamicRegistries() {
      return this.combinedDynamicRegistries;
   }

   public DataPackContents dataPackContents() {
      return this.dataPackContents;
   }

   public DataConfiguration dataConfiguration() {
      return this.dataConfiguration;
   }

   @Environment(EnvType.CLIENT)
   public interface Modifier extends UnaryOperator {
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface RegistryAwareModifier extends BiFunction {
   }
}
