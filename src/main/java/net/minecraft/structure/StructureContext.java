package net.minecraft.structure;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public record StructureContext(ResourceManager resourceManager, DynamicRegistryManager registryManager, StructureTemplateManager structureTemplateManager) {
   public StructureContext(ResourceManager arg, DynamicRegistryManager arg2, StructureTemplateManager arg3) {
      this.resourceManager = arg;
      this.registryManager = arg2;
      this.structureTemplateManager = arg3;
   }

   public static StructureContext from(ServerWorld world) {
      MinecraftServer minecraftServer = world.getServer();
      return new StructureContext(minecraftServer.getResourceManager(), minecraftServer.getRegistryManager(), minecraftServer.getStructureTemplateManager());
   }

   public ResourceManager resourceManager() {
      return this.resourceManager;
   }

   public DynamicRegistryManager registryManager() {
      return this.registryManager;
   }

   public StructureTemplateManager structureTemplateManager() {
      return this.structureTemplateManager;
   }
}
