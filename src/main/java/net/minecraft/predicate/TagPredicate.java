package net.minecraft.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class TagPredicate {
   private final TagKey tag;
   private final boolean expected;

   public TagPredicate(TagKey tag, boolean expected) {
      this.tag = tag;
      this.expected = expected;
   }

   public static TagPredicate expected(TagKey tag) {
      return new TagPredicate(tag, true);
   }

   public static TagPredicate unexpected(TagKey tag) {
      return new TagPredicate(tag, false);
   }

   public boolean test(RegistryEntry registryEntry) {
      return registryEntry.isIn(this.tag) == this.expected;
   }

   public JsonElement toJson() {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("id", this.tag.id().toString());
      jsonObject.addProperty("expected", this.expected);
      return jsonObject;
   }

   public static TagPredicate fromJson(@Nullable JsonElement json, RegistryKey registry) {
      if (json == null) {
         throw new JsonParseException("Expected a tag predicate");
      } else {
         JsonObject jsonObject = JsonHelper.asObject(json, "Tag Predicate");
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "id"));
         boolean bl = JsonHelper.getBoolean(jsonObject, "expected");
         return new TagPredicate(TagKey.of(registry, lv), bl);
      }
   }
}
