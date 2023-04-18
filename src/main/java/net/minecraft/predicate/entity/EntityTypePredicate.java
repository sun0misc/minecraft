package net.minecraft.predicate.entity;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public abstract class EntityTypePredicate {
   public static final EntityTypePredicate ANY = new EntityTypePredicate() {
      public boolean matches(EntityType type) {
         return true;
      }

      public JsonElement toJson() {
         return JsonNull.INSTANCE;
      }
   };
   private static final Joiner COMMA_JOINER = Joiner.on(", ");

   public abstract boolean matches(EntityType type);

   public abstract JsonElement toJson();

   public static EntityTypePredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         String string = JsonHelper.asString(json, "type");
         Identifier lv;
         if (string.startsWith("#")) {
            lv = new Identifier(string.substring(1));
            return new Tagged(TagKey.of(RegistryKeys.ENTITY_TYPE, lv));
         } else {
            lv = new Identifier(string);
            EntityType lv2 = (EntityType)Registries.ENTITY_TYPE.getOrEmpty(lv).orElseThrow(() -> {
               return new JsonSyntaxException("Unknown entity type '" + lv + "', valid types are: " + COMMA_JOINER.join(Registries.ENTITY_TYPE.getIds()));
            });
            return new Single(lv2);
         }
      } else {
         return ANY;
      }
   }

   public static EntityTypePredicate create(EntityType type) {
      return new Single(type);
   }

   public static EntityTypePredicate create(TagKey tag) {
      return new Tagged(tag);
   }

   private static class Tagged extends EntityTypePredicate {
      private final TagKey tag;

      public Tagged(TagKey tag) {
         this.tag = tag;
      }

      public boolean matches(EntityType type) {
         return type.isIn(this.tag);
      }

      public JsonElement toJson() {
         return new JsonPrimitive("#" + this.tag.id());
      }
   }

   private static class Single extends EntityTypePredicate {
      private final EntityType type;

      public Single(EntityType type) {
         this.type = type;
      }

      public boolean matches(EntityType type) {
         return this.type == type;
      }

      public JsonElement toJson() {
         return new JsonPrimitive(Registries.ENTITY_TYPE.getId(this.type).toString());
      }
   }
}
