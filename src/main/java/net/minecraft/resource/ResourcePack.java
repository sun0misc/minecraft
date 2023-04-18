package net.minecraft.resource;

import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface ResourcePack extends AutoCloseable {
   String METADATA_PATH_SUFFIX = ".mcmeta";
   String PACK_METADATA_NAME = "pack.mcmeta";

   @Nullable
   InputSupplier openRoot(String... segments);

   @Nullable
   InputSupplier open(ResourceType type, Identifier id);

   void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer);

   Set getNamespaces(ResourceType type);

   @Nullable
   Object parseMetadata(ResourceMetadataReader metaReader) throws IOException;

   String getName();

   default boolean isAlwaysStable() {
      return false;
   }

   void close();

   @FunctionalInterface
   public interface ResultConsumer extends BiConsumer {
   }
}
