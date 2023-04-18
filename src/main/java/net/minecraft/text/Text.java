package net.minecraft.text;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.LowercaseEnumTypeAdapterFactory;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public interface Text extends Message, StringVisitable {
   Style getStyle();

   TextContent getContent();

   default String getString() {
      return StringVisitable.super.getString();
   }

   default String asTruncatedString(int length) {
      StringBuilder stringBuilder = new StringBuilder();
      this.visit((string) -> {
         int j = length - stringBuilder.length();
         if (j <= 0) {
            return TERMINATE_VISIT;
         } else {
            stringBuilder.append(string.length() <= j ? string : string.substring(0, j));
            return Optional.empty();
         }
      });
      return stringBuilder.toString();
   }

   List getSiblings();

   default MutableText copyContentOnly() {
      return MutableText.of(this.getContent());
   }

   default MutableText copy() {
      return new MutableText(this.getContent(), new ArrayList(this.getSiblings()), this.getStyle());
   }

   OrderedText asOrderedText();

   default Optional visit(StringVisitable.StyledVisitor styledVisitor, Style style) {
      Style lv = this.getStyle().withParent(style);
      Optional optional = this.getContent().visit(styledVisitor, lv);
      if (optional.isPresent()) {
         return optional;
      } else {
         Iterator var5 = this.getSiblings().iterator();

         Optional optional2;
         do {
            if (!var5.hasNext()) {
               return Optional.empty();
            }

            Text lv2 = (Text)var5.next();
            optional2 = lv2.visit(styledVisitor, lv);
         } while(!optional2.isPresent());

         return optional2;
      }
   }

   default Optional visit(StringVisitable.Visitor visitor) {
      Optional optional = this.getContent().visit(visitor);
      if (optional.isPresent()) {
         return optional;
      } else {
         Iterator var3 = this.getSiblings().iterator();

         Optional optional2;
         do {
            if (!var3.hasNext()) {
               return Optional.empty();
            }

            Text lv = (Text)var3.next();
            optional2 = lv.visit(visitor);
         } while(!optional2.isPresent());

         return optional2;
      }
   }

   default List withoutStyle() {
      return this.getWithStyle(Style.EMPTY);
   }

   default List getWithStyle(Style style) {
      List list = Lists.newArrayList();
      this.visit((styleOverride, text) -> {
         if (!text.isEmpty()) {
            list.add(literal(text).fillStyle(styleOverride));
         }

         return Optional.empty();
      }, style);
      return list;
   }

   default boolean contains(Text text) {
      if (this.equals(text)) {
         return true;
      } else {
         List list = this.withoutStyle();
         List list2 = text.getWithStyle(this.getStyle());
         return Collections.indexOfSubList(list, list2) != -1;
      }
   }

   static Text of(@Nullable String string) {
      return (Text)(string != null ? literal(string) : ScreenTexts.EMPTY);
   }

   static MutableText literal(String string) {
      return MutableText.of(new LiteralTextContent(string));
   }

   static MutableText translatable(String key) {
      return MutableText.of(new TranslatableTextContent(key, (String)null, TranslatableTextContent.EMPTY_ARGUMENTS));
   }

   static MutableText translatable(String key, Object... args) {
      return MutableText.of(new TranslatableTextContent(key, (String)null, args));
   }

   static MutableText translatableWithFallback(String key, @Nullable String fallback) {
      return MutableText.of(new TranslatableTextContent(key, fallback, TranslatableTextContent.EMPTY_ARGUMENTS));
   }

   static MutableText translatableWithFallback(String key, @Nullable String fallback, Object... args) {
      return MutableText.of(new TranslatableTextContent(key, fallback, args));
   }

   static MutableText empty() {
      return MutableText.of(TextContent.EMPTY);
   }

   static MutableText keybind(String string) {
      return MutableText.of(new KeybindTextContent(string));
   }

   static MutableText nbt(String rawPath, boolean interpret, Optional separator, NbtDataSource dataSource) {
      return MutableText.of(new NbtTextContent(rawPath, interpret, separator, dataSource));
   }

   static MutableText score(String name, String objective) {
      return MutableText.of(new ScoreTextContent(name, objective));
   }

   static MutableText selector(String pattern, Optional separator) {
      return MutableText.of(new SelectorTextContent(pattern, separator));
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      private static final Gson GSON = (Gson)Util.make(() -> {
         GsonBuilder gsonBuilder = new GsonBuilder();
         gsonBuilder.disableHtmlEscaping();
         gsonBuilder.registerTypeHierarchyAdapter(Text.class, new Serializer());
         gsonBuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
         gsonBuilder.registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory());
         return gsonBuilder.create();
      });
      private static final Field JSON_READER_POS = (Field)Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("pos");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException var1) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", var1);
         }
      });
      private static final Field JSON_READER_LINE_START = (Field)Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("lineStart");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException var1) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", var1);
         }
      });

      public MutableText deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         if (jsonElement.isJsonPrimitive()) {
            return Text.literal(jsonElement.getAsString());
         } else {
            MutableText lv;
            if (!jsonElement.isJsonObject()) {
               if (jsonElement.isJsonArray()) {
                  JsonArray jsonArray3 = jsonElement.getAsJsonArray();
                  lv = null;
                  Iterator var17 = jsonArray3.iterator();

                  while(var17.hasNext()) {
                     JsonElement jsonElement2 = (JsonElement)var17.next();
                     MutableText lv3 = this.deserialize(jsonElement2, jsonElement2.getClass(), jsonDeserializationContext);
                     if (lv == null) {
                        lv = lv3;
                     } else {
                        lv.append((Text)lv3);
                     }
                  }

                  return lv;
               } else {
                  throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
               }
            } else {
               JsonObject jsonObject = jsonElement.getAsJsonObject();
               String string;
               if (jsonObject.has("text")) {
                  string = JsonHelper.getString(jsonObject, "text");
                  lv = string.isEmpty() ? Text.empty() : Text.literal(string);
               } else if (jsonObject.has("translate")) {
                  string = JsonHelper.getString(jsonObject, "translate");
                  String string2 = JsonHelper.getString(jsonObject, "fallback", (String)null);
                  if (jsonObject.has("with")) {
                     JsonArray jsonArray = JsonHelper.getArray(jsonObject, "with");
                     Object[] objects = new Object[jsonArray.size()];

                     for(int i = 0; i < objects.length; ++i) {
                        objects[i] = optimizeArgument(this.deserialize(jsonArray.get(i), type, jsonDeserializationContext));
                     }

                     lv = Text.translatableWithFallback(string, string2, objects);
                  } else {
                     lv = Text.translatableWithFallback(string, string2);
                  }
               } else if (jsonObject.has("score")) {
                  JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "score");
                  if (!jsonObject2.has("name") || !jsonObject2.has("objective")) {
                     throw new JsonParseException("A score component needs a least a name and an objective");
                  }

                  lv = Text.score(JsonHelper.getString(jsonObject2, "name"), JsonHelper.getString(jsonObject2, "objective"));
               } else if (jsonObject.has("selector")) {
                  Optional optional = this.getSeparator(type, jsonDeserializationContext, jsonObject);
                  lv = Text.selector(JsonHelper.getString(jsonObject, "selector"), optional);
               } else if (jsonObject.has("keybind")) {
                  lv = Text.keybind(JsonHelper.getString(jsonObject, "keybind"));
               } else {
                  if (!jsonObject.has("nbt")) {
                     throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                  }

                  string = JsonHelper.getString(jsonObject, "nbt");
                  Optional optional2 = this.getSeparator(type, jsonDeserializationContext, jsonObject);
                  boolean bl = JsonHelper.getBoolean(jsonObject, "interpret", false);
                  Object lv2;
                  if (jsonObject.has("block")) {
                     lv2 = new BlockNbtDataSource(JsonHelper.getString(jsonObject, "block"));
                  } else if (jsonObject.has("entity")) {
                     lv2 = new EntityNbtDataSource(JsonHelper.getString(jsonObject, "entity"));
                  } else {
                     if (!jsonObject.has("storage")) {
                        throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                     }

                     lv2 = new StorageNbtDataSource(new Identifier(JsonHelper.getString(jsonObject, "storage")));
                  }

                  lv = Text.nbt(string, bl, optional2, (NbtDataSource)lv2);
               }

               if (jsonObject.has("extra")) {
                  JsonArray jsonArray2 = JsonHelper.getArray(jsonObject, "extra");
                  if (jsonArray2.size() <= 0) {
                     throw new JsonParseException("Unexpected empty array of components");
                  }

                  for(int j = 0; j < jsonArray2.size(); ++j) {
                     lv.append((Text)this.deserialize(jsonArray2.get(j), type, jsonDeserializationContext));
                  }
               }

               lv.setStyle((Style)jsonDeserializationContext.deserialize(jsonElement, Style.class));
               return lv;
            }
         }
      }

      private static Object optimizeArgument(Object text) {
         if (text instanceof Text lv) {
            if (lv.getStyle().isEmpty() && lv.getSiblings().isEmpty()) {
               TextContent lv2 = lv.getContent();
               if (lv2 instanceof LiteralTextContent) {
                  LiteralTextContent lv3 = (LiteralTextContent)lv2;
                  return lv3.string();
               }
            }
         }

         return text;
      }

      private Optional getSeparator(Type type, JsonDeserializationContext context, JsonObject json) {
         return json.has("separator") ? Optional.of(this.deserialize(json.get("separator"), type, context)) : Optional.empty();
      }

      private void addStyle(Style style, JsonObject json, JsonSerializationContext context) {
         JsonElement jsonElement = context.serialize(style);
         if (jsonElement.isJsonObject()) {
            JsonObject jsonObject2 = (JsonObject)jsonElement;
            Iterator var6 = jsonObject2.entrySet().iterator();

            while(var6.hasNext()) {
               Map.Entry entry = (Map.Entry)var6.next();
               json.add((String)entry.getKey(), (JsonElement)entry.getValue());
            }
         }

      }

      public JsonElement serialize(Text arg, Type type, JsonSerializationContext jsonSerializationContext) {
         JsonObject jsonObject = new JsonObject();
         if (!arg.getStyle().isEmpty()) {
            this.addStyle(arg.getStyle(), jsonObject, jsonSerializationContext);
         }

         if (!arg.getSiblings().isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            Iterator var6 = arg.getSiblings().iterator();

            while(var6.hasNext()) {
               Text lv = (Text)var6.next();
               jsonArray.add(this.serialize((Text)lv, Text.class, jsonSerializationContext));
            }

            jsonObject.add("extra", jsonArray);
         }

         TextContent lv2 = arg.getContent();
         if (lv2 == TextContent.EMPTY) {
            jsonObject.addProperty("text", "");
         } else if (lv2 instanceof LiteralTextContent) {
            LiteralTextContent lv3 = (LiteralTextContent)lv2;
            jsonObject.addProperty("text", lv3.string());
         } else if (lv2 instanceof TranslatableTextContent) {
            TranslatableTextContent lv4 = (TranslatableTextContent)lv2;
            jsonObject.addProperty("translate", lv4.getKey());
            String string = lv4.getFallback();
            if (string != null) {
               jsonObject.addProperty("fallback", string);
            }

            if (lv4.getArgs().length > 0) {
               JsonArray jsonArray2 = new JsonArray();
               Object[] var14 = lv4.getArgs();
               int var15 = var14.length;

               for(int var16 = 0; var16 < var15; ++var16) {
                  Object object = var14[var16];
                  if (object instanceof Text) {
                     jsonArray2.add(this.serialize((Text)((Text)object), object.getClass(), jsonSerializationContext));
                  } else {
                     jsonArray2.add(new JsonPrimitive(String.valueOf(object)));
                  }
               }

               jsonObject.add("with", jsonArray2);
            }
         } else if (lv2 instanceof ScoreTextContent) {
            ScoreTextContent lv5 = (ScoreTextContent)lv2;
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("name", lv5.getName());
            jsonObject2.addProperty("objective", lv5.getObjective());
            jsonObject.add("score", jsonObject2);
         } else if (lv2 instanceof SelectorTextContent) {
            SelectorTextContent lv6 = (SelectorTextContent)lv2;
            jsonObject.addProperty("selector", lv6.getPattern());
            this.addSeparator(jsonSerializationContext, jsonObject, lv6.getSeparator());
         } else if (lv2 instanceof KeybindTextContent) {
            KeybindTextContent lv7 = (KeybindTextContent)lv2;
            jsonObject.addProperty("keybind", lv7.getKey());
         } else {
            if (!(lv2 instanceof NbtTextContent)) {
               throw new IllegalArgumentException("Don't know how to serialize " + lv2 + " as a Component");
            }

            NbtTextContent lv8 = (NbtTextContent)lv2;
            jsonObject.addProperty("nbt", lv8.getPath());
            jsonObject.addProperty("interpret", lv8.shouldInterpret());
            this.addSeparator(jsonSerializationContext, jsonObject, lv8.getSeparator());
            NbtDataSource lv9 = lv8.getDataSource();
            if (lv9 instanceof BlockNbtDataSource) {
               BlockNbtDataSource lv10 = (BlockNbtDataSource)lv9;
               jsonObject.addProperty("block", lv10.rawPos());
            } else if (lv9 instanceof EntityNbtDataSource) {
               EntityNbtDataSource lv11 = (EntityNbtDataSource)lv9;
               jsonObject.addProperty("entity", lv11.rawSelector());
            } else {
               if (!(lv9 instanceof StorageNbtDataSource)) {
                  throw new IllegalArgumentException("Don't know how to serialize " + lv2 + " as a Component");
               }

               StorageNbtDataSource lv12 = (StorageNbtDataSource)lv9;
               jsonObject.addProperty("storage", lv12.id().toString());
            }
         }

         return jsonObject;
      }

      private void addSeparator(JsonSerializationContext context, JsonObject json, Optional separator) {
         separator.ifPresent((separatorx) -> {
            json.add("separator", this.serialize((Text)separatorx, separatorx.getClass(), context));
         });
      }

      public static String toJson(Text text) {
         return GSON.toJson(text);
      }

      public static String toSortedJsonString(Text text) {
         return JsonHelper.toSortedString(toJsonTree(text));
      }

      public static JsonElement toJsonTree(Text text) {
         return GSON.toJsonTree(text);
      }

      @Nullable
      public static MutableText fromJson(String json) {
         return (MutableText)JsonHelper.deserializeNullable(GSON, json, MutableText.class, false);
      }

      @Nullable
      public static MutableText fromJson(JsonElement json) {
         return (MutableText)GSON.fromJson(json, MutableText.class);
      }

      @Nullable
      public static MutableText fromLenientJson(String json) {
         return (MutableText)JsonHelper.deserializeNullable(GSON, json, MutableText.class, true);
      }

      public static MutableText fromJson(com.mojang.brigadier.StringReader reader) {
         try {
            JsonReader jsonReader = new JsonReader(new StringReader(reader.getRemaining()));
            jsonReader.setLenient(false);
            MutableText lv = (MutableText)GSON.getAdapter(MutableText.class).read(jsonReader);
            reader.setCursor(reader.getCursor() + getPosition(jsonReader));
            return lv;
         } catch (StackOverflowError | IOException var3) {
            throw new JsonParseException(var3);
         }
      }

      private static int getPosition(JsonReader reader) {
         try {
            return JSON_READER_POS.getInt(reader) - JSON_READER_LINE_START.getInt(reader) + 1;
         } catch (IllegalAccessException var2) {
            throw new IllegalStateException("Couldn't read position of JsonReader", var2);
         }
      }

      // $FF: synthetic method
      public JsonElement serialize(Object text, Type type, JsonSerializationContext context) {
         return this.serialize((Text)text, type, context);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, type, context);
      }
   }
}
