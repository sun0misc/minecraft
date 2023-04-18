package net.minecraft.text;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class Style {
   public static final Style EMPTY = new Style((TextColor)null, (Boolean)null, (Boolean)null, (Boolean)null, (Boolean)null, (Boolean)null, (ClickEvent)null, (HoverEvent)null, (String)null, (Identifier)null);
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(TextColor.CODEC.optionalFieldOf("color").forGetter((style) -> {
         return Optional.ofNullable(style.color);
      }), Codec.BOOL.optionalFieldOf("bold").forGetter((style) -> {
         return Optional.ofNullable(style.bold);
      }), Codec.BOOL.optionalFieldOf("italic").forGetter((style) -> {
         return Optional.ofNullable(style.italic);
      }), Codec.BOOL.optionalFieldOf("underlined").forGetter((style) -> {
         return Optional.ofNullable(style.underlined);
      }), Codec.BOOL.optionalFieldOf("strikethrough").forGetter((style) -> {
         return Optional.ofNullable(style.strikethrough);
      }), Codec.BOOL.optionalFieldOf("obfuscated").forGetter((style) -> {
         return Optional.ofNullable(style.obfuscated);
      }), Codec.STRING.optionalFieldOf("insertion").forGetter((style) -> {
         return Optional.ofNullable(style.insertion);
      }), Identifier.CODEC.optionalFieldOf("font").forGetter((style) -> {
         return Optional.ofNullable(style.font);
      })).apply(instance, Style::of);
   });
   public static final Identifier DEFAULT_FONT_ID = new Identifier("minecraft", "default");
   @Nullable
   final TextColor color;
   @Nullable
   final Boolean bold;
   @Nullable
   final Boolean italic;
   @Nullable
   final Boolean underlined;
   @Nullable
   final Boolean strikethrough;
   @Nullable
   final Boolean obfuscated;
   @Nullable
   final ClickEvent clickEvent;
   @Nullable
   final HoverEvent hoverEvent;
   @Nullable
   final String insertion;
   @Nullable
   final Identifier font;

   private static Style of(Optional color, Optional bold, Optional italic, Optional underlined, Optional strikethrough, Optional obfuscated, Optional insertion, Optional font) {
      return new Style((TextColor)color.orElse((Object)null), (Boolean)bold.orElse((Object)null), (Boolean)italic.orElse((Object)null), (Boolean)underlined.orElse((Object)null), (Boolean)strikethrough.orElse((Object)null), (Boolean)obfuscated.orElse((Object)null), (ClickEvent)null, (HoverEvent)null, (String)insertion.orElse((Object)null), (Identifier)font.orElse((Object)null));
   }

   Style(@Nullable TextColor color, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underlined, @Nullable Boolean strikethrough, @Nullable Boolean obfuscated, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String insertion, @Nullable Identifier font) {
      this.color = color;
      this.bold = bold;
      this.italic = italic;
      this.underlined = underlined;
      this.strikethrough = strikethrough;
      this.obfuscated = obfuscated;
      this.clickEvent = clickEvent;
      this.hoverEvent = hoverEvent;
      this.insertion = insertion;
      this.font = font;
   }

   @Nullable
   public TextColor getColor() {
      return this.color;
   }

   public boolean isBold() {
      return this.bold == Boolean.TRUE;
   }

   public boolean isItalic() {
      return this.italic == Boolean.TRUE;
   }

   public boolean isStrikethrough() {
      return this.strikethrough == Boolean.TRUE;
   }

   public boolean isUnderlined() {
      return this.underlined == Boolean.TRUE;
   }

   public boolean isObfuscated() {
      return this.obfuscated == Boolean.TRUE;
   }

   public boolean isEmpty() {
      return this == EMPTY;
   }

   @Nullable
   public ClickEvent getClickEvent() {
      return this.clickEvent;
   }

   @Nullable
   public HoverEvent getHoverEvent() {
      return this.hoverEvent;
   }

   @Nullable
   public String getInsertion() {
      return this.insertion;
   }

   public Identifier getFont() {
      return this.font != null ? this.font : DEFAULT_FONT_ID;
   }

   public Style withColor(@Nullable TextColor color) {
      return new Style(color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withColor(@Nullable Formatting color) {
      return this.withColor(color != null ? TextColor.fromFormatting(color) : null);
   }

   public Style withColor(int rgbColor) {
      return this.withColor(TextColor.fromRgb(rgbColor));
   }

   public Style withBold(@Nullable Boolean bold) {
      return new Style(this.color, bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withItalic(@Nullable Boolean italic) {
      return new Style(this.color, this.bold, italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withUnderline(@Nullable Boolean underline) {
      return new Style(this.color, this.bold, this.italic, underline, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withStrikethrough(@Nullable Boolean strikethrough) {
      return new Style(this.color, this.bold, this.italic, this.underlined, strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withObfuscated(@Nullable Boolean obfuscated) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withClickEvent(@Nullable ClickEvent clickEvent) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withHoverEvent(@Nullable HoverEvent hoverEvent) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, hoverEvent, this.insertion, this.font);
   }

   public Style withInsertion(@Nullable String insertion) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, insertion, this.font);
   }

   public Style withFont(@Nullable Identifier font) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, font);
   }

   public Style withFormatting(Formatting formatting) {
      TextColor lv = this.color;
      Boolean boolean_ = this.bold;
      Boolean boolean2 = this.italic;
      Boolean boolean3 = this.strikethrough;
      Boolean boolean4 = this.underlined;
      Boolean boolean5 = this.obfuscated;
      switch (formatting) {
         case OBFUSCATED:
            boolean5 = true;
            break;
         case BOLD:
            boolean_ = true;
            break;
         case STRIKETHROUGH:
            boolean3 = true;
            break;
         case UNDERLINE:
            boolean4 = true;
            break;
         case ITALIC:
            boolean2 = true;
            break;
         case RESET:
            return EMPTY;
         default:
            lv = TextColor.fromFormatting(formatting);
      }

      return new Style(lv, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withExclusiveFormatting(Formatting formatting) {
      TextColor lv = this.color;
      Boolean boolean_ = this.bold;
      Boolean boolean2 = this.italic;
      Boolean boolean3 = this.strikethrough;
      Boolean boolean4 = this.underlined;
      Boolean boolean5 = this.obfuscated;
      switch (formatting) {
         case OBFUSCATED:
            boolean5 = true;
            break;
         case BOLD:
            boolean_ = true;
            break;
         case STRIKETHROUGH:
            boolean3 = true;
            break;
         case UNDERLINE:
            boolean4 = true;
            break;
         case ITALIC:
            boolean2 = true;
            break;
         case RESET:
            return EMPTY;
         default:
            boolean5 = false;
            boolean_ = false;
            boolean3 = false;
            boolean4 = false;
            boolean2 = false;
            lv = TextColor.fromFormatting(formatting);
      }

      return new Style(lv, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withFormatting(Formatting... formattings) {
      TextColor lv = this.color;
      Boolean boolean_ = this.bold;
      Boolean boolean2 = this.italic;
      Boolean boolean3 = this.strikethrough;
      Boolean boolean4 = this.underlined;
      Boolean boolean5 = this.obfuscated;
      Formatting[] var8 = formattings;
      int var9 = formattings.length;

      for(int var10 = 0; var10 < var9; ++var10) {
         Formatting lv2 = var8[var10];
         switch (lv2) {
            case OBFUSCATED:
               boolean5 = true;
               break;
            case BOLD:
               boolean_ = true;
               break;
            case STRIKETHROUGH:
               boolean3 = true;
               break;
            case UNDERLINE:
               boolean4 = true;
               break;
            case ITALIC:
               boolean2 = true;
               break;
            case RESET:
               return EMPTY;
            default:
               lv = TextColor.fromFormatting(lv2);
         }
      }

      return new Style(lv, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withParent(Style parent) {
      if (this == EMPTY) {
         return parent;
      } else {
         return parent == EMPTY ? this : new Style(this.color != null ? this.color : parent.color, this.bold != null ? this.bold : parent.bold, this.italic != null ? this.italic : parent.italic, this.underlined != null ? this.underlined : parent.underlined, this.strikethrough != null ? this.strikethrough : parent.strikethrough, this.obfuscated != null ? this.obfuscated : parent.obfuscated, this.clickEvent != null ? this.clickEvent : parent.clickEvent, this.hoverEvent != null ? this.hoverEvent : parent.hoverEvent, this.insertion != null ? this.insertion : parent.insertion, this.font != null ? this.font : parent.font);
      }
   }

   public String toString() {
      final StringBuilder stringBuilder = new StringBuilder("{");

      class Writer {
         private boolean shouldAppendComma;

         private void appendComma() {
            if (this.shouldAppendComma) {
               stringBuilder.append(',');
            }

            this.shouldAppendComma = true;
         }

         void append(String key, @Nullable Boolean value) {
            if (value != null) {
               this.appendComma();
               if (!value) {
                  stringBuilder.append('!');
               }

               stringBuilder.append(key);
            }

         }

         void append(String key, @Nullable Object value) {
            if (value != null) {
               this.appendComma();
               stringBuilder.append(key);
               stringBuilder.append('=');
               stringBuilder.append(value);
            }

         }
      }

      Writer lv = new Writer();
      lv.append("color", (Object)this.color);
      lv.append("bold", this.bold);
      lv.append("italic", this.italic);
      lv.append("underlined", this.underlined);
      lv.append("strikethrough", this.strikethrough);
      lv.append("obfuscated", this.obfuscated);
      lv.append("clickEvent", (Object)this.clickEvent);
      lv.append("hoverEvent", (Object)this.hoverEvent);
      lv.append("insertion", (Object)this.insertion);
      lv.append("font", (Object)this.font);
      stringBuilder.append("}");
      return stringBuilder.toString();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Style)) {
         return false;
      } else {
         Style lv = (Style)o;
         return this.isBold() == lv.isBold() && Objects.equals(this.getColor(), lv.getColor()) && this.isItalic() == lv.isItalic() && this.isObfuscated() == lv.isObfuscated() && this.isStrikethrough() == lv.isStrikethrough() && this.isUnderlined() == lv.isUnderlined() && Objects.equals(this.getClickEvent(), lv.getClickEvent()) && Objects.equals(this.getHoverEvent(), lv.getHoverEvent()) && Objects.equals(this.getInsertion(), lv.getInsertion()) && Objects.equals(this.getFont(), lv.getFont());
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion});
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      @Nullable
      public Style deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject == null) {
               return null;
            } else {
               Boolean boolean_ = parseNullableBoolean(jsonObject, "bold");
               Boolean boolean2 = parseNullableBoolean(jsonObject, "italic");
               Boolean boolean3 = parseNullableBoolean(jsonObject, "underlined");
               Boolean boolean4 = parseNullableBoolean(jsonObject, "strikethrough");
               Boolean boolean5 = parseNullableBoolean(jsonObject, "obfuscated");
               TextColor lv = parseColor(jsonObject);
               String string = parseInsertion(jsonObject);
               ClickEvent lv2 = getClickEvent(jsonObject);
               HoverEvent lv3 = getHoverEvent(jsonObject);
               Identifier lv4 = getFont(jsonObject);
               return new Style(lv, boolean_, boolean2, boolean3, boolean4, boolean5, lv2, lv3, string, lv4);
            }
         } else {
            return null;
         }
      }

      @Nullable
      private static Identifier getFont(JsonObject root) {
         if (root.has("font")) {
            String string = JsonHelper.getString(root, "font");

            try {
               return new Identifier(string);
            } catch (InvalidIdentifierException var3) {
               throw new JsonSyntaxException("Invalid font name: " + string);
            }
         } else {
            return null;
         }
      }

      @Nullable
      private static HoverEvent getHoverEvent(JsonObject root) {
         if (root.has("hoverEvent")) {
            JsonObject jsonObject2 = JsonHelper.getObject(root, "hoverEvent");
            HoverEvent lv = HoverEvent.fromJson(jsonObject2);
            if (lv != null && lv.getAction().isParsable()) {
               return lv;
            }
         }

         return null;
      }

      @Nullable
      private static ClickEvent getClickEvent(JsonObject root) {
         if (root.has("clickEvent")) {
            JsonObject jsonObject2 = JsonHelper.getObject(root, "clickEvent");
            String string = JsonHelper.getString(jsonObject2, "action", (String)null);
            ClickEvent.Action lv = string == null ? null : ClickEvent.Action.byName(string);
            String string2 = JsonHelper.getString(jsonObject2, "value", (String)null);
            if (lv != null && string2 != null && lv.isUserDefinable()) {
               return new ClickEvent(lv, string2);
            }
         }

         return null;
      }

      @Nullable
      private static String parseInsertion(JsonObject root) {
         return JsonHelper.getString(root, "insertion", (String)null);
      }

      @Nullable
      private static TextColor parseColor(JsonObject root) {
         if (root.has("color")) {
            String string = JsonHelper.getString(root, "color");
            return TextColor.parse(string);
         } else {
            return null;
         }
      }

      @Nullable
      private static Boolean parseNullableBoolean(JsonObject root, String key) {
         return root.has(key) ? root.get(key).getAsBoolean() : null;
      }

      @Nullable
      public JsonElement serialize(Style arg, Type type, JsonSerializationContext jsonSerializationContext) {
         if (arg.isEmpty()) {
            return null;
         } else {
            JsonObject jsonObject = new JsonObject();
            if (arg.bold != null) {
               jsonObject.addProperty("bold", arg.bold);
            }

            if (arg.italic != null) {
               jsonObject.addProperty("italic", arg.italic);
            }

            if (arg.underlined != null) {
               jsonObject.addProperty("underlined", arg.underlined);
            }

            if (arg.strikethrough != null) {
               jsonObject.addProperty("strikethrough", arg.strikethrough);
            }

            if (arg.obfuscated != null) {
               jsonObject.addProperty("obfuscated", arg.obfuscated);
            }

            if (arg.color != null) {
               jsonObject.addProperty("color", arg.color.getName());
            }

            if (arg.insertion != null) {
               jsonObject.add("insertion", jsonSerializationContext.serialize(arg.insertion));
            }

            if (arg.clickEvent != null) {
               JsonObject jsonObject2 = new JsonObject();
               jsonObject2.addProperty("action", arg.clickEvent.getAction().getName());
               jsonObject2.addProperty("value", arg.clickEvent.getValue());
               jsonObject.add("clickEvent", jsonObject2);
            }

            if (arg.hoverEvent != null) {
               jsonObject.add("hoverEvent", arg.hoverEvent.toJson());
            }

            if (arg.font != null) {
               jsonObject.addProperty("font", arg.font.toString());
            }

            return jsonObject;
         }
      }

      // $FF: synthetic method
      @Nullable
      public JsonElement serialize(Object style, Type type, JsonSerializationContext context) {
         return this.serialize((Style)style, type, context);
      }

      // $FF: synthetic method
      @Nullable
      public Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, type, context);
      }
   }
}
