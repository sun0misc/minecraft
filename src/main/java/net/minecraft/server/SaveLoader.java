package net.minecraft.server;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.world.SaveProperties;

public record SaveLoader(LifecycledResourceManager resourceManager, DataPackContents dataPackContents, CombinedDynamicRegistries combinedDynamicRegistries, SaveProperties saveProperties) implements AutoCloseable {
   public SaveLoader(LifecycledResourceManager arg, DataPackContents arg2, CombinedDynamicRegistries arg3, SaveProperties arg4) {
      this.resourceManager = arg;
      this.dataPackContents = arg2;
      this.combinedDynamicRegistries = arg3;
      this.saveProperties = arg4;
   }

   public void close() {
      this.resourceManager.close();
   }

   public LifecycledResourceManager resourceManager() {
      return this.resourceManager;
   }

   public DataPackContents dataPackContents() {
      return this.dataPackContents;
   }

   public CombinedDynamicRegistries combinedDynamicRegistries() {
      return this.combinedDynamicRegistries;
   }

   public SaveProperties saveProperties() {
      return this.saveProperties;
   }
}
