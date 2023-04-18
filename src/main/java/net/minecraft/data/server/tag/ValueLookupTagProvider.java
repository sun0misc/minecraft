package net.minecraft.data.server.tag;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.data.DataOutput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

public abstract class ValueLookupTagProvider extends TagProvider {
   private final Function valueToKey;

   public ValueLookupTagProvider(DataOutput output, RegistryKey registryRef, CompletableFuture registryLookupFuture, Function valueToKey) {
      super(output, registryRef, registryLookupFuture);
      this.valueToKey = valueToKey;
   }

   public ValueLookupTagProvider(DataOutput output, RegistryKey registryRef, CompletableFuture registryLookupFuture, CompletableFuture parentTagLookupFuture, Function valueToKey) {
      super(output, registryRef, registryLookupFuture, parentTagLookupFuture);
      this.valueToKey = valueToKey;
   }

   protected ObjectBuilder getOrCreateTagBuilder(TagKey arg) {
      TagBuilder lv = this.getTagBuilder(arg);
      return new ObjectBuilder(lv, this.valueToKey);
   }

   // $FF: synthetic method
   protected TagProvider.ProvidedTagBuilder getOrCreateTagBuilder(TagKey tag) {
      return this.getOrCreateTagBuilder(tag);
   }

   protected static class ObjectBuilder extends TagProvider.ProvidedTagBuilder {
      private final Function valueToKey;

      ObjectBuilder(TagBuilder builder, Function valueToKey) {
         super(builder);
         this.valueToKey = valueToKey;
      }

      public ObjectBuilder addTag(TagKey arg) {
         super.addTag(arg);
         return this;
      }

      public final ObjectBuilder add(Object value) {
         this.add((RegistryKey)((RegistryKey)this.valueToKey.apply(value)));
         return this;
      }

      @SafeVarargs
      public final ObjectBuilder add(Object... values) {
         Stream.of(values).map(this.valueToKey).forEach(this::add);
         return this;
      }

      // $FF: synthetic method
      public TagProvider.ProvidedTagBuilder addTag(TagKey identifiedTag) {
         return this.addTag(identifiedTag);
      }
   }
}
