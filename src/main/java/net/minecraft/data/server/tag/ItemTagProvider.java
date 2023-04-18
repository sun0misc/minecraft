package net.minecraft.data.server.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

public abstract class ItemTagProvider extends ValueLookupTagProvider {
   private final CompletableFuture blockTags;
   private final Map blockTagsToCopy = new HashMap();

   public ItemTagProvider(DataOutput output, CompletableFuture registryLookupFuture, CompletableFuture blockTagLookupFuture) {
      super(output, RegistryKeys.ITEM, registryLookupFuture, (item) -> {
         return item.getRegistryEntry().registryKey();
      });
      this.blockTags = blockTagLookupFuture;
   }

   public ItemTagProvider(DataOutput output, CompletableFuture registryLookupFuture, CompletableFuture parentTagLookupFuture, CompletableFuture blockTagLookupFuture) {
      super(output, RegistryKeys.ITEM, registryLookupFuture, parentTagLookupFuture, (item) -> {
         return item.getRegistryEntry().registryKey();
      });
      this.blockTags = blockTagLookupFuture;
   }

   protected void copy(TagKey blockTag, TagKey itemTag) {
      this.blockTagsToCopy.put(blockTag, itemTag);
   }

   protected CompletableFuture getRegistryLookupFuture() {
      return super.getRegistryLookupFuture().thenCombineAsync(this.blockTags, (lookup, blockTags) -> {
         this.blockTagsToCopy.forEach((blockTag, itemTag) -> {
            TagBuilder lv = this.getTagBuilder(itemTag);
            Optional optional = (Optional)blockTags.apply(blockTag);
            List var10000 = ((TagBuilder)optional.orElseThrow(() -> {
               return new IllegalStateException("Missing block tag " + itemTag.id());
            })).build();
            Objects.requireNonNull(lv);
            var10000.forEach(lv::add);
         });
         return lookup;
      });
   }
}
