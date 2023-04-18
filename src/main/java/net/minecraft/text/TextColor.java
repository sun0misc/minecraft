package net.minecraft.text;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public final class TextColor {
   private static final String RGB_PREFIX = "#";
   public static final Codec CODEC;
   private static final Map FORMATTING_TO_COLOR;
   private static final Map BY_NAME;
   private final int rgb;
   @Nullable
   private final String name;

   private TextColor(int rgb, String name) {
      this.rgb = rgb;
      this.name = name;
   }

   private TextColor(int rgb) {
      this.rgb = rgb;
      this.name = null;
   }

   public int getRgb() {
      return this.rgb;
   }

   public String getName() {
      return this.name != null ? this.name : this.getHexCode();
   }

   private String getHexCode() {
      return String.format(Locale.ROOT, "#%06X", this.rgb);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TextColor lv = (TextColor)o;
         return this.rgb == lv.rgb;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.rgb, this.name});
   }

   public String toString() {
      return this.name != null ? this.name : this.getHexCode();
   }

   @Nullable
   public static TextColor fromFormatting(Formatting formatting) {
      return (TextColor)FORMATTING_TO_COLOR.get(formatting);
   }

   public static TextColor fromRgb(int rgb) {
      return new TextColor(rgb);
   }

   @Nullable
   public static TextColor parse(String name) {
      if (name.startsWith("#")) {
         try {
            int i = Integer.parseInt(name.substring(1), 16);
            return fromRgb(i);
         } catch (NumberFormatException var2) {
            return null;
         }
      } else {
         return (TextColor)BY_NAME.get(name);
      }
   }

   static {
      CODEC = Codec.STRING.comapFlatMap((color) -> {
         TextColor lv = parse(color);
         return lv != null ? DataResult.success(lv) : DataResult.error(() -> {
            return "String is not a valid color name or hex color code";
         });
      }, TextColor::getName);
      FORMATTING_TO_COLOR = (Map)Stream.of(Formatting.values()).filter(Formatting::isColor).collect(ImmutableMap.toImmutableMap(Function.identity(), (formatting) -> {
         return new TextColor(formatting.getColorValue(), formatting.getName());
      }));
      BY_NAME = (Map)FORMATTING_TO_COLOR.values().stream().collect(ImmutableMap.toImmutableMap((textColor) -> {
         return textColor.name;
      }, Function.identity()));
   }
}
