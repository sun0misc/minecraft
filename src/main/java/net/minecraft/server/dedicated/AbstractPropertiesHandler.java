package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.registry.DynamicRegistryManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractPropertiesHandler {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final Properties properties;

   public AbstractPropertiesHandler(Properties properties) {
      this.properties = properties;
   }

   public static Properties loadProperties(Path path) {
      Properties properties = new Properties();

      try {
         InputStream inputStream = Files.newInputStream(path);

         try {
            properties.load(inputStream);
         } catch (Throwable var6) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (inputStream != null) {
            inputStream.close();
         }
      } catch (IOException var7) {
         LOGGER.error("Failed to load properties from file: {}", path);
      }

      return properties;
   }

   public void saveProperties(Path path) {
      try {
         OutputStream outputStream = Files.newOutputStream(path);

         try {
            this.properties.store(outputStream, "Minecraft server properties");
         } catch (Throwable var6) {
            if (outputStream != null) {
               try {
                  outputStream.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (outputStream != null) {
            outputStream.close();
         }
      } catch (IOException var7) {
         LOGGER.error("Failed to store properties to file: {}", path);
      }

   }

   private static Function wrapNumberParser(Function parser) {
      return (string) -> {
         try {
            return (Number)parser.apply(string);
         } catch (NumberFormatException var3) {
            return null;
         }
      };
   }

   protected static Function combineParser(IntFunction intParser, Function fallbackParser) {
      return (string) -> {
         try {
            return intParser.apply(Integer.parseInt(string));
         } catch (NumberFormatException var4) {
            return fallbackParser.apply(string);
         }
      };
   }

   @Nullable
   private String getStringValue(String key) {
      return (String)this.properties.get(key);
   }

   @Nullable
   protected Object getDeprecated(String key, Function stringifier) {
      String string2 = this.getStringValue(key);
      if (string2 == null) {
         return null;
      } else {
         this.properties.remove(key);
         return stringifier.apply(string2);
      }
   }

   protected Object get(String key, Function parser, Function stringifier, Object fallback) {
      String string2 = this.getStringValue(key);
      Object object2 = MoreObjects.firstNonNull(string2 != null ? parser.apply(string2) : null, fallback);
      this.properties.put(key, stringifier.apply(object2));
      return object2;
   }

   protected PropertyAccessor accessor(String key, Function parser, Function stringifier, Object fallback) {
      String string2 = this.getStringValue(key);
      Object object2 = MoreObjects.firstNonNull(string2 != null ? parser.apply(string2) : null, fallback);
      this.properties.put(key, stringifier.apply(object2));
      return new PropertyAccessor(key, object2, stringifier);
   }

   protected Object get(String key, Function parser, UnaryOperator parsedTransformer, Function stringifier, Object fallback) {
      return this.get(key, (value) -> {
         Object object = parser.apply(value);
         return object != null ? parsedTransformer.apply(object) : null;
      }, stringifier, fallback);
   }

   protected Object get(String key, Function parser, Object fallback) {
      return this.get(key, parser, Objects::toString, fallback);
   }

   protected PropertyAccessor accessor(String key, Function parser, Object fallback) {
      return this.accessor(key, parser, Objects::toString, fallback);
   }

   protected String getString(String key, String fallback) {
      return (String)this.get(key, Function.identity(), Function.identity(), fallback);
   }

   @Nullable
   protected String getDeprecatedString(String key) {
      return (String)this.getDeprecated(key, Function.identity());
   }

   protected int getInt(String key, int fallback) {
      return (Integer)this.get(key, wrapNumberParser(Integer::parseInt), fallback);
   }

   protected PropertyAccessor intAccessor(String key, int fallback) {
      return this.accessor(key, wrapNumberParser(Integer::parseInt), fallback);
   }

   protected int transformedParseInt(String key, UnaryOperator transformer, int fallback) {
      return (Integer)this.get(key, wrapNumberParser(Integer::parseInt), transformer, Objects::toString, fallback);
   }

   protected long parseLong(String key, long fallback) {
      return (Long)this.get(key, wrapNumberParser(Long::parseLong), fallback);
   }

   protected boolean parseBoolean(String key, boolean fallback) {
      return (Boolean)this.get(key, Boolean::valueOf, fallback);
   }

   protected PropertyAccessor booleanAccessor(String key, boolean fallback) {
      return this.accessor(key, Boolean::valueOf, fallback);
   }

   @Nullable
   protected Boolean getDeprecatedBoolean(String key) {
      return (Boolean)this.getDeprecated(key, Boolean::valueOf);
   }

   protected Properties copyProperties() {
      Properties properties = new Properties();
      properties.putAll(this.properties);
      return properties;
   }

   protected abstract AbstractPropertiesHandler create(DynamicRegistryManager registryManager, Properties properties);

   public class PropertyAccessor implements Supplier {
      private final String key;
      private final Object value;
      private final Function stringifier;

      PropertyAccessor(String key, Object value, Function stringifier) {
         this.key = key;
         this.value = value;
         this.stringifier = stringifier;
      }

      public Object get() {
         return this.value;
      }

      public AbstractPropertiesHandler set(DynamicRegistryManager registryManager, Object value) {
         Properties properties = AbstractPropertiesHandler.this.copyProperties();
         properties.put(this.key, this.stringifier.apply(value));
         return AbstractPropertiesHandler.this.create(registryManager, properties);
      }
   }
}
