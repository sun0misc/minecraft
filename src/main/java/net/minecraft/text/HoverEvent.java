package net.minecraft.text;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class HoverEvent {
   static final Logger LOGGER = LogUtils.getLogger();
   private final Action action;
   private final Object contents;

   public HoverEvent(Action action, Object contents) {
      this.action = action;
      this.contents = contents;
   }

   public Action getAction() {
      return this.action;
   }

   @Nullable
   public Object getValue(Action action) {
      return this.action == action ? action.cast(this.contents) : null;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         HoverEvent lv = (HoverEvent)o;
         return this.action == lv.action && Objects.equals(this.contents, lv.contents);
      } else {
         return false;
      }
   }

   public String toString() {
      return "HoverEvent{action=" + this.action + ", value='" + this.contents + "'}";
   }

   public int hashCode() {
      int i = this.action.hashCode();
      i = 31 * i + (this.contents != null ? this.contents.hashCode() : 0);
      return i;
   }

   @Nullable
   public static HoverEvent fromJson(JsonObject json) {
      String string = JsonHelper.getString(json, "action", (String)null);
      if (string == null) {
         return null;
      } else {
         Action lv = HoverEvent.Action.byName(string);
         if (lv == null) {
            return null;
         } else {
            JsonElement jsonElement = json.get("contents");
            if (jsonElement != null) {
               return lv.buildHoverEvent(jsonElement);
            } else {
               Text lv2 = Text.Serializer.fromJson(json.get("value"));
               return lv2 != null ? lv.buildHoverEvent((Text)lv2) : null;
            }
         }
      }
   }

   public JsonObject toJson() {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("action", this.action.getName());
      jsonObject.add("contents", this.action.contentsToJson(this.contents));
      return jsonObject;
   }

   public static class Action {
      public static final Action SHOW_TEXT = new Action("show_text", true, Text.Serializer::fromJson, Text.Serializer::toJsonTree, Function.identity());
      public static final Action SHOW_ITEM = new Action("show_item", true, ItemStackContent::parse, ItemStackContent::toJson, ItemStackContent::parse);
      public static final Action SHOW_ENTITY = new Action("show_entity", true, EntityContent::parse, EntityContent::toJson, EntityContent::parse);
      private static final Map BY_NAME;
      private final String name;
      private final boolean parsable;
      private final Function deserializer;
      private final Function serializer;
      private final Function legacyDeserializer;

      public Action(String name, boolean parsable, Function deserializer, Function serializer, Function legacyDeserializer) {
         this.name = name;
         this.parsable = parsable;
         this.deserializer = deserializer;
         this.serializer = serializer;
         this.legacyDeserializer = legacyDeserializer;
      }

      public boolean isParsable() {
         return this.parsable;
      }

      public String getName() {
         return this.name;
      }

      @Nullable
      public static Action byName(String name) {
         return (Action)BY_NAME.get(name);
      }

      Object cast(Object o) {
         return o;
      }

      @Nullable
      public HoverEvent buildHoverEvent(JsonElement contents) {
         Object object = this.deserializer.apply(contents);
         return object == null ? null : new HoverEvent(this, object);
      }

      @Nullable
      public HoverEvent buildHoverEvent(Text value) {
         Object object = this.legacyDeserializer.apply(value);
         return object == null ? null : new HoverEvent(this, object);
      }

      public JsonElement contentsToJson(Object contents) {
         return (JsonElement)this.serializer.apply(this.cast(contents));
      }

      public String toString() {
         return "<action " + this.name + ">";
      }

      static {
         BY_NAME = (Map)Stream.of(SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY).collect(ImmutableMap.toImmutableMap(Action::getName, (action) -> {
            return action;
         }));
      }
   }

   public static class ItemStackContent {
      private final Item item;
      private final int count;
      @Nullable
      private final NbtCompound nbt;
      @Nullable
      private ItemStack stack;

      ItemStackContent(Item item, int count, @Nullable NbtCompound nbt) {
         this.item = item;
         this.count = count;
         this.nbt = nbt;
      }

      public ItemStackContent(ItemStack stack) {
         this(stack.getItem(), stack.getCount(), stack.getNbt() != null ? stack.getNbt().copy() : null);
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ItemStackContent lv = (ItemStackContent)o;
            return this.count == lv.count && this.item.equals(lv.item) && Objects.equals(this.nbt, lv.nbt);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.item.hashCode();
         i = 31 * i + this.count;
         i = 31 * i + (this.nbt != null ? this.nbt.hashCode() : 0);
         return i;
      }

      public ItemStack asStack() {
         if (this.stack == null) {
            this.stack = new ItemStack(this.item, this.count);
            if (this.nbt != null) {
               this.stack.setNbt(this.nbt);
            }
         }

         return this.stack;
      }

      private static ItemStackContent parse(JsonElement json) {
         if (json.isJsonPrimitive()) {
            return new ItemStackContent((Item)Registries.ITEM.get(new Identifier(json.getAsString())), 1, (NbtCompound)null);
         } else {
            JsonObject jsonObject = JsonHelper.asObject(json, "item");
            Item lv = (Item)Registries.ITEM.get(new Identifier(JsonHelper.getString(jsonObject, "id")));
            int i = JsonHelper.getInt(jsonObject, "count", 1);
            if (jsonObject.has("tag")) {
               String string = JsonHelper.getString(jsonObject, "tag");

               try {
                  NbtCompound lv2 = StringNbtReader.parse(string);
                  return new ItemStackContent(lv, i, lv2);
               } catch (CommandSyntaxException var6) {
                  HoverEvent.LOGGER.warn("Failed to parse tag: {}", string, var6);
               }
            }

            return new ItemStackContent(lv, i, (NbtCompound)null);
         }
      }

      @Nullable
      private static ItemStackContent parse(Text text) {
         try {
            NbtCompound lv = StringNbtReader.parse(text.getString());
            return new ItemStackContent(ItemStack.fromNbt(lv));
         } catch (CommandSyntaxException var2) {
            HoverEvent.LOGGER.warn("Failed to parse item tag: {}", text, var2);
            return null;
         }
      }

      private JsonElement toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("id", Registries.ITEM.getId(this.item).toString());
         if (this.count != 1) {
            jsonObject.addProperty("count", this.count);
         }

         if (this.nbt != null) {
            jsonObject.addProperty("tag", this.nbt.toString());
         }

         return jsonObject;
      }
   }

   public static class EntityContent {
      public final EntityType entityType;
      public final UUID uuid;
      @Nullable
      public final Text name;
      @Nullable
      private List tooltip;

      public EntityContent(EntityType entityType, UUID uuid, @Nullable Text name) {
         this.entityType = entityType;
         this.uuid = uuid;
         this.name = name;
      }

      @Nullable
      public static EntityContent parse(JsonElement json) {
         if (!json.isJsonObject()) {
            return null;
         } else {
            JsonObject jsonObject = json.getAsJsonObject();
            EntityType lv = (EntityType)Registries.ENTITY_TYPE.get(new Identifier(JsonHelper.getString(jsonObject, "type")));
            UUID uUID = UUID.fromString(JsonHelper.getString(jsonObject, "id"));
            Text lv2 = Text.Serializer.fromJson(jsonObject.get("name"));
            return new EntityContent(lv, uUID, lv2);
         }
      }

      @Nullable
      public static EntityContent parse(Text text) {
         try {
            NbtCompound lv = StringNbtReader.parse(text.getString());
            Text lv2 = Text.Serializer.fromJson(lv.getString("name"));
            EntityType lv3 = (EntityType)Registries.ENTITY_TYPE.get(new Identifier(lv.getString("type")));
            UUID uUID = UUID.fromString(lv.getString("id"));
            return new EntityContent(lv3, uUID, lv2);
         } catch (Exception var5) {
            return null;
         }
      }

      public JsonElement toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("type", Registries.ENTITY_TYPE.getId(this.entityType).toString());
         jsonObject.addProperty("id", this.uuid.toString());
         if (this.name != null) {
            jsonObject.add("name", Text.Serializer.toJsonTree(this.name));
         }

         return jsonObject;
      }

      public List asTooltip() {
         if (this.tooltip == null) {
            this.tooltip = Lists.newArrayList();
            if (this.name != null) {
               this.tooltip.add(this.name);
            }

            this.tooltip.add(Text.translatable("gui.entity_tooltip.type", this.entityType.getName()));
            this.tooltip.add(Text.literal(this.uuid.toString()));
         }

         return this.tooltip;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            EntityContent lv = (EntityContent)o;
            return this.entityType.equals(lv.entityType) && this.uuid.equals(lv.uuid) && Objects.equals(this.name, lv.name);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.entityType.hashCode();
         i = 31 * i + this.uuid.hashCode();
         i = 31 * i + (this.name != null ? this.name.hashCode() : 0);
         return i;
      }
   }
}
