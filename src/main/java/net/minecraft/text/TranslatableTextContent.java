package net.minecraft.text;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

public class TranslatableTextContent implements TextContent {
   public static final Object[] EMPTY_ARGUMENTS = new Object[0];
   private static final StringVisitable LITERAL_PERCENT_SIGN = StringVisitable.plain("%");
   private static final StringVisitable NULL_ARGUMENT = StringVisitable.plain("null");
   private final String key;
   @Nullable
   private final String fallback;
   private final Object[] args;
   @Nullable
   private Language languageCache;
   private List translations = ImmutableList.of();
   private static final Pattern ARG_FORMAT = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

   public TranslatableTextContent(String key, @Nullable String fallback, Object[] args) {
      this.key = key;
      this.fallback = fallback;
      this.args = args;
   }

   private void updateTranslations() {
      Language lv = Language.getInstance();
      if (lv != this.languageCache) {
         this.languageCache = lv;
         String string = this.fallback != null ? lv.get(this.key, this.fallback) : lv.get(this.key);

         try {
            ImmutableList.Builder builder = ImmutableList.builder();
            Objects.requireNonNull(builder);
            this.forEachPart(string, builder::add);
            this.translations = builder.build();
         } catch (TranslationException var4) {
            this.translations = ImmutableList.of(StringVisitable.plain(string));
         }

      }
   }

   private void forEachPart(String translation, Consumer partsConsumer) {
      Matcher matcher = ARG_FORMAT.matcher(translation);

      try {
         int i = 0;

         int j;
         int l;
         for(j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String string2;
            if (k > j) {
               string2 = translation.substring(j, k);
               if (string2.indexOf(37) != -1) {
                  throw new IllegalArgumentException();
               }

               partsConsumer.accept(StringVisitable.plain(string2));
            }

            string2 = matcher.group(2);
            String string3 = translation.substring(k, l);
            if ("%".equals(string2) && "%%".equals(string3)) {
               partsConsumer.accept(LITERAL_PERCENT_SIGN);
            } else {
               if (!"s".equals(string2)) {
                  throw new TranslationException(this, "Unsupported format: '" + string3 + "'");
               }

               String string4 = matcher.group(1);
               int m = string4 != null ? Integer.parseInt(string4) - 1 : i++;
               partsConsumer.accept(this.getArg(m));
            }
         }

         if (j < translation.length()) {
            String string5 = translation.substring(j);
            if (string5.indexOf(37) != -1) {
               throw new IllegalArgumentException();
            }

            partsConsumer.accept(StringVisitable.plain(string5));
         }

      } catch (IllegalArgumentException var12) {
         throw new TranslationException(this, var12);
      }
   }

   private StringVisitable getArg(int index) {
      if (index >= 0 && index < this.args.length) {
         Object object = this.args[index];
         if (object instanceof Text) {
            return (Text)object;
         } else {
            return object == null ? NULL_ARGUMENT : StringVisitable.plain(object.toString());
         }
      } else {
         throw new TranslationException(this, index);
      }
   }

   public Optional visit(StringVisitable.StyledVisitor visitor, Style style) {
      this.updateTranslations();
      Iterator var3 = this.translations.iterator();

      Optional optional;
      do {
         if (!var3.hasNext()) {
            return Optional.empty();
         }

         StringVisitable lv = (StringVisitable)var3.next();
         optional = lv.visit(visitor, style);
      } while(!optional.isPresent());

      return optional;
   }

   public Optional visit(StringVisitable.Visitor visitor) {
      this.updateTranslations();
      Iterator var2 = this.translations.iterator();

      Optional optional;
      do {
         if (!var2.hasNext()) {
            return Optional.empty();
         }

         StringVisitable lv = (StringVisitable)var2.next();
         optional = lv.visit(visitor);
      } while(!optional.isPresent());

      return optional;
   }

   public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
      Object[] objects = new Object[this.args.length];

      for(int j = 0; j < objects.length; ++j) {
         Object object = this.args[j];
         if (object instanceof Text) {
            objects[j] = Texts.parse(source, (Text)object, sender, depth);
         } else {
            objects[j] = object;
         }
      }

      return MutableText.of(new TranslatableTextContent(this.key, this.fallback, objects));
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         boolean var10000;
         if (o instanceof TranslatableTextContent) {
            TranslatableTextContent lv = (TranslatableTextContent)o;
            if (Objects.equals(this.key, lv.key) && Objects.equals(this.fallback, lv.fallback) && Arrays.equals(this.args, lv.args)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   }

   public int hashCode() {
      int i = Objects.hashCode(this.key);
      i = 31 * i + Objects.hashCode(this.fallback);
      i = 31 * i + Arrays.hashCode(this.args);
      return i;
   }

   public String toString() {
      String var10000 = this.key;
      return "translation{key='" + var10000 + "'" + (this.fallback != null ? ", fallback='" + this.fallback + "'" : "") + ", args=" + Arrays.toString(this.args) + "}";
   }

   public String getKey() {
      return this.key;
   }

   @Nullable
   public String getFallback() {
      return this.fallback;
   }

   public Object[] getArgs() {
      return this.args;
   }
}
