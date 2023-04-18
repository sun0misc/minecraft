package net.minecraft.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.lang.reflect.Type;
import java.util.function.UnaryOperator;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class Identifier implements Comparable {
   public static final Codec CODEC;
   private static final SimpleCommandExceptionType COMMAND_EXCEPTION;
   public static final char NAMESPACE_SEPARATOR = ':';
   public static final String DEFAULT_NAMESPACE = "minecraft";
   public static final String REALMS_NAMESPACE = "realms";
   private final String namespace;
   private final String path;

   protected Identifier(String namespace, String path, @Nullable ExtraData extraData) {
      this.namespace = namespace;
      this.path = path;
   }

   public Identifier(String namespace, String path) {
      this(validateNamespace(namespace, path), validatePath(namespace, path), (ExtraData)null);
   }

   private Identifier(String[] id) {
      this(id[0], id[1]);
   }

   public Identifier(String id) {
      this(split(id, ':'));
   }

   public static Identifier splitOn(String id, char delimiter) {
      return new Identifier(split(id, delimiter));
   }

   @Nullable
   public static Identifier tryParse(String id) {
      try {
         return new Identifier(id);
      } catch (InvalidIdentifierException var2) {
         return null;
      }
   }

   @Nullable
   public static Identifier of(String namespace, String path) {
      try {
         return new Identifier(namespace, path);
      } catch (InvalidIdentifierException var3) {
         return null;
      }
   }

   protected static String[] split(String id, char delimiter) {
      String[] strings = new String[]{"minecraft", id};
      int i = id.indexOf(delimiter);
      if (i >= 0) {
         strings[1] = id.substring(i + 1);
         if (i >= 1) {
            strings[0] = id.substring(0, i);
         }
      }

      return strings;
   }

   public static DataResult validate(String id) {
      try {
         return DataResult.success(new Identifier(id));
      } catch (InvalidIdentifierException var2) {
         return DataResult.error(() -> {
            return "Not a valid resource location: " + id + " " + var2.getMessage();
         });
      }
   }

   public String getPath() {
      return this.path;
   }

   public String getNamespace() {
      return this.namespace;
   }

   public Identifier withPath(String path) {
      return new Identifier(this.namespace, validatePath(this.namespace, path), (ExtraData)null);
   }

   public Identifier withPath(UnaryOperator pathFunction) {
      return this.withPath((String)pathFunction.apply(this.path));
   }

   public Identifier withPrefixedPath(String prefix) {
      return this.withPath(prefix + this.path);
   }

   public Identifier withSuffixedPath(String suffix) {
      return this.withPath(this.path + suffix);
   }

   public String toString() {
      return this.namespace + ":" + this.path;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Identifier)) {
         return false;
      } else {
         Identifier lv = (Identifier)o;
         return this.namespace.equals(lv.namespace) && this.path.equals(lv.path);
      }
   }

   public int hashCode() {
      return 31 * this.namespace.hashCode() + this.path.hashCode();
   }

   public int compareTo(Identifier arg) {
      int i = this.path.compareTo(arg.path);
      if (i == 0) {
         i = this.namespace.compareTo(arg.namespace);
      }

      return i;
   }

   public String toUnderscoreSeparatedString() {
      return this.toString().replace('/', '_').replace(':', '_');
   }

   public String toTranslationKey() {
      return this.namespace + "." + this.path;
   }

   public String toShortTranslationKey() {
      return this.namespace.equals("minecraft") ? this.path : this.toTranslationKey();
   }

   public String toTranslationKey(String prefix) {
      return prefix + "." + this.toTranslationKey();
   }

   public String toTranslationKey(String prefix, String suffix) {
      return prefix + "." + this.toTranslationKey() + "." + suffix;
   }

   public static Identifier fromCommandInput(StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();

      while(reader.canRead() && isCharValid(reader.peek())) {
         reader.skip();
      }

      String string = reader.getString().substring(i, reader.getCursor());

      try {
         return new Identifier(string);
      } catch (InvalidIdentifierException var4) {
         reader.setCursor(i);
         throw COMMAND_EXCEPTION.createWithContext(reader);
      }
   }

   public static boolean isCharValid(char c) {
      return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
   }

   private static boolean isPathValid(String path) {
      for(int i = 0; i < path.length(); ++i) {
         if (!isPathCharacterValid(path.charAt(i))) {
            return false;
         }
      }

      return true;
   }

   private static boolean isNamespaceValid(String namespace) {
      for(int i = 0; i < namespace.length(); ++i) {
         if (!isNamespaceCharacterValid(namespace.charAt(i))) {
            return false;
         }
      }

      return true;
   }

   private static String validateNamespace(String namespace, String path) {
      if (!isNamespaceValid(namespace)) {
         throw new InvalidIdentifierException("Non [a-z0-9_.-] character in namespace of location: " + namespace + ":" + path);
      } else {
         return namespace;
      }
   }

   public static boolean isPathCharacterValid(char character) {
      return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '/' || character == '.';
   }

   private static boolean isNamespaceCharacterValid(char character) {
      return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '.';
   }

   public static boolean isValid(String id) {
      String[] strings = split(id, ':');
      return isNamespaceValid(StringUtils.isEmpty(strings[0]) ? "minecraft" : strings[0]) && isPathValid(strings[1]);
   }

   private static String validatePath(String namespace, String path) {
      if (!isPathValid(path)) {
         throw new InvalidIdentifierException("Non [a-z0-9/._-] character in path of location: " + namespace + ":" + path);
      } else {
         return path;
      }
   }

   // $FF: synthetic method
   public int compareTo(Object other) {
      return this.compareTo((Identifier)other);
   }

   static {
      CODEC = Codec.STRING.comapFlatMap(Identifier::validate, Identifier::toString).stable();
      COMMAND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.id.invalid"));
   }

   protected interface ExtraData {
   }

   public static class Serializer implements JsonDeserializer, com.google.gson.JsonSerializer {
      public Identifier deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         return new Identifier(JsonHelper.asString(jsonElement, "location"));
      }

      public JsonElement serialize(Identifier arg, Type type, JsonSerializationContext jsonSerializationContext) {
         return new JsonPrimitive(arg.toString());
      }

      // $FF: synthetic method
      public JsonElement serialize(Object id, Type type, JsonSerializationContext context) {
         return this.serialize((Identifier)id, type, context);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, type, context);
      }
   }
}
