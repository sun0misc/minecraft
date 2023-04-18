package net.minecraft.data.server.tag;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagFile;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public abstract class TagProvider implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final DataOutput.PathResolver pathResolver;
   private final CompletableFuture registryLookupFuture;
   private final CompletableFuture registryLoadFuture;
   private final CompletableFuture parentTagLookupFuture;
   protected final RegistryKey registryRef;
   private final Map tagBuilders;

   protected TagProvider(DataOutput output, RegistryKey registryRef, CompletableFuture registryLookupFuture) {
      this(output, registryRef, registryLookupFuture, CompletableFuture.completedFuture(TagProvider.TagLookup.empty()));
   }

   protected TagProvider(DataOutput output, RegistryKey registryRef, CompletableFuture registryLookupFuture, CompletableFuture parentTagLookupFuture) {
      this.registryLoadFuture = new CompletableFuture();
      this.tagBuilders = Maps.newLinkedHashMap();
      this.pathResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, TagManagerLoader.getPath(registryRef));
      this.registryRef = registryRef;
      this.parentTagLookupFuture = parentTagLookupFuture;
      this.registryLookupFuture = registryLookupFuture;
   }

   public final String getName() {
      return "Tags for " + this.registryRef.getValue();
   }

   protected abstract void configure(RegistryWrapper.WrapperLookup lookup);

   public CompletableFuture run(DataWriter writer) {
      return this.getRegistryLookupFuture().thenApply((registryLookupFuture) -> {
         this.registryLoadFuture.complete((Object)null);
         return registryLookupFuture;
      }).thenCombineAsync(this.parentTagLookupFuture, (lookup, parent) -> {
         record RegistryInfo(RegistryWrapper.WrapperLookup contents, TagLookup parent) {
            final RegistryWrapper.WrapperLookup contents;
            final TagLookup parent;

            RegistryInfo(RegistryWrapper.WrapperLookup arg, TagLookup arg2) {
               this.contents = arg;
               this.parent = arg2;
            }

            public RegistryWrapper.WrapperLookup contents() {
               return this.contents;
            }

            public TagLookup parent() {
               return this.parent;
            }
         }

         return new RegistryInfo(lookup, parent);
      }).thenCompose((info) -> {
         RegistryWrapper.Impl lv = info.contents.getWrapperOrThrow(this.registryRef);
         Predicate predicate = (id) -> {
            return lv.getOptional(RegistryKey.of(this.registryRef, id)).isPresent();
         };
         Predicate predicate2 = (id) -> {
            return this.tagBuilders.containsKey(id) || info.parent.contains(TagKey.of(this.registryRef, id));
         };
         return CompletableFuture.allOf((CompletableFuture[])this.tagBuilders.entrySet().stream().map((entry) -> {
            Identifier lv = (Identifier)entry.getKey();
            TagBuilder lv2 = (TagBuilder)entry.getValue();
            List list = lv2.build();
            List list2 = list.stream().filter((tagEntry) -> {
               return !tagEntry.canAdd(predicate, predicate2);
            }).toList();
            if (!list2.isEmpty()) {
               throw new IllegalArgumentException(String.format(Locale.ROOT, "Couldn't define tag %s as it is missing following references: %s", lv, list2.stream().map(Objects::toString).collect(Collectors.joining(","))));
            } else {
               DataResult var10000 = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(list, false));
               Logger var10002 = LOGGER;
               Objects.requireNonNull(var10002);
               JsonElement jsonElement = (JsonElement)var10000.getOrThrow(false, var10002::error);
               Path path = this.pathResolver.resolveJson(lv);
               return DataProvider.writeToPath(writer, jsonElement, path);
            }
         }).toArray((i) -> {
            return new CompletableFuture[i];
         }));
      });
   }

   protected ProvidedTagBuilder getOrCreateTagBuilder(TagKey tag) {
      TagBuilder lv = this.getTagBuilder(tag);
      return new ProvidedTagBuilder(lv);
   }

   protected TagBuilder getTagBuilder(TagKey tag) {
      return (TagBuilder)this.tagBuilders.computeIfAbsent(tag.id(), (id) -> {
         return TagBuilder.create();
      });
   }

   public CompletableFuture getTagLookupFuture() {
      return this.registryLoadFuture.thenApply((void_) -> {
         return (tag) -> {
            return Optional.ofNullable((TagBuilder)this.tagBuilders.get(tag.id()));
         };
      });
   }

   protected CompletableFuture getRegistryLookupFuture() {
      return this.registryLookupFuture.thenApply((lookup) -> {
         this.tagBuilders.clear();
         this.configure(lookup);
         return lookup;
      });
   }

   @FunctionalInterface
   public interface TagLookup extends Function {
      static TagLookup empty() {
         return (tag) -> {
            return Optional.empty();
         };
      }

      default boolean contains(TagKey tag) {
         return ((Optional)this.apply(tag)).isPresent();
      }
   }

   protected static class ProvidedTagBuilder {
      private final TagBuilder builder;

      protected ProvidedTagBuilder(TagBuilder builder) {
         this.builder = builder;
      }

      public final ProvidedTagBuilder add(RegistryKey key) {
         this.builder.add(key.getValue());
         return this;
      }

      @SafeVarargs
      public final ProvidedTagBuilder add(RegistryKey... keys) {
         RegistryKey[] var2 = keys;
         int var3 = keys.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            RegistryKey lv = var2[var4];
            this.builder.add(lv.getValue());
         }

         return this;
      }

      public ProvidedTagBuilder addOptional(Identifier id) {
         this.builder.addOptional(id);
         return this;
      }

      public ProvidedTagBuilder addTag(TagKey identifiedTag) {
         this.builder.addTag(identifiedTag.id());
         return this;
      }

      public ProvidedTagBuilder addOptionalTag(Identifier id) {
         this.builder.addOptionalTag(id);
         return this;
      }
   }
}
