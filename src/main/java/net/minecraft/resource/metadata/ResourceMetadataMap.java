package net.minecraft.resource.metadata;

import java.util.Map;

public class ResourceMetadataMap {
   private static final ResourceMetadataMap EMPTY = new ResourceMetadataMap(Map.of());
   private final Map values;

   private ResourceMetadataMap(Map values) {
      this.values = values;
   }

   public Object get(ResourceMetadataReader reader) {
      return this.values.get(reader);
   }

   public static ResourceMetadataMap of() {
      return EMPTY;
   }

   public static ResourceMetadataMap of(ResourceMetadataReader reader, Object value) {
      return new ResourceMetadataMap(Map.of(reader, value));
   }

   public static ResourceMetadataMap of(ResourceMetadataReader reader, Object value, ResourceMetadataReader reader2, Object value2) {
      return new ResourceMetadataMap(Map.of(reader, value, reader2, value2));
   }
}
