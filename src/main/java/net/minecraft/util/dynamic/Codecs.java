package net.minecraft.util.dynamic;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Codecs {
   public static final Codec JSON_ELEMENT;
   public static final Codec TEXT;
   public static final Codec STRINGIFIED_TEXT;
   public static final Codec VECTOR_3F;
   public static final Codec QUATERNIONF;
   public static final Codec AXIS_ANGLE4F;
   public static final Codec ROTATION;
   public static Codec MATRIX4F;
   public static final Codec NONNEGATIVE_INT;
   public static final Codec POSITIVE_INT;
   public static final Codec POSITIVE_FLOAT;
   public static final Codec REGULAR_EXPRESSION;
   public static final Codec INSTANT;
   public static final Codec BASE_64;
   public static final Codec TAG_ENTRY_ID;
   public static final Function OPTIONAL_OF_LONG_TO_OPTIONAL_LONG;
   public static final Function OPTIONAL_LONG_TO_OPTIONAL_OF_LONG;
   public static final Codec BIT_SET;
   private static final Codec GAME_PROFILE_PROPERTY;
   @VisibleForTesting
   public static final Codec GAME_PROFILE_PROPERTY_MAP;
   public static final Codec GAME_PROFILE;
   public static final Codec NON_EMPTY_STRING;

   public static Codec xor(Codec first, Codec second) {
      return new Xor(first, second);
   }

   public static Codec createCodecForPairObject(Codec codec, String leftFieldName, String rightFieldName, BiFunction combineFunction, Function leftFunction, Function rightFunction) {
      Codec codec2 = Codec.list(codec).comapFlatMap((list) -> {
         return Util.toArray((List)list, 2).flatMap((listx) -> {
            Object object = listx.get(0);
            Object object2 = listx.get(1);
            return (DataResult)combineFunction.apply(object, object2);
         });
      }, (pair) -> {
         return ImmutableList.of(leftFunction.apply(pair), rightFunction.apply(pair));
      });
      Codec codec3 = RecordCodecBuilder.create((instance) -> {
         return instance.group(codec.fieldOf(leftFieldName).forGetter(Pair::getFirst), codec.fieldOf(rightFieldName).forGetter(Pair::getSecond)).apply(instance, Pair::of);
      }).comapFlatMap((pair) -> {
         return (DataResult)combineFunction.apply(pair.getFirst(), pair.getSecond());
      }, (pair) -> {
         return Pair.of(leftFunction.apply(pair), rightFunction.apply(pair));
      });
      Codec codec4 = (new Either(codec2, codec3)).xmap((either) -> {
         return either.map((object) -> {
            return object;
         }, (object) -> {
            return object;
         });
      }, com.mojang.datafixers.util.Either::left);
      return Codec.either(codec, codec4).comapFlatMap((either) -> {
         return (DataResult)either.map((object) -> {
            return (DataResult)combineFunction.apply(object, object);
         }, DataResult::success);
      }, (pair) -> {
         Object object2 = leftFunction.apply(pair);
         Object object3 = rightFunction.apply(pair);
         return Objects.equals(object2, object3) ? com.mojang.datafixers.util.Either.left(object2) : com.mojang.datafixers.util.Either.right(pair);
      });
   }

   public static Codec.ResultFunction orElsePartial(final Object object) {
      return new Codec.ResultFunction() {
         public DataResult apply(DynamicOps ops, Object input, DataResult result) {
            MutableObject mutableObject = new MutableObject();
            Objects.requireNonNull(mutableObject);
            Optional optional = result.resultOrPartial(mutableObject::setValue);
            return optional.isPresent() ? result : DataResult.error(() -> {
               return "(" + (String)mutableObject.getValue() + " -> using default)";
            }, Pair.of(object, input));
         }

         public DataResult coApply(DynamicOps ops, Object input, DataResult result) {
            return result;
         }

         public String toString() {
            return "OrElsePartial[" + object + "]";
         }
      };
   }

   public static Codec rawIdChecked(ToIntFunction elementToRawId, IntFunction rawIdToElement, int errorRawId) {
      return Codec.INT.flatXmap((rawId) -> {
         return (DataResult)Optional.ofNullable(rawIdToElement.apply(rawId)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               return "Unknown element id: " + rawId;
            });
         });
      }, (element) -> {
         int j = elementToRawId.applyAsInt(element);
         return j == errorRawId ? DataResult.error(() -> {
            return "Element with unknown id: " + element;
         }) : DataResult.success(j);
      });
   }

   public static Codec idChecked(Function elementToId, Function idToElement) {
      return Codec.STRING.flatXmap((id) -> {
         return (DataResult)Optional.ofNullable(idToElement.apply(id)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               return "Unknown element name:" + id;
            });
         });
      }, (element) -> {
         return (DataResult)Optional.ofNullable((String)elementToId.apply(element)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               return "Element with unknown name: " + element;
            });
         });
      });
   }

   public static Codec orCompressed(final Codec uncompressedCodec, final Codec compressedCodec) {
      return new Codec() {
         public DataResult encode(Object input, DynamicOps ops, Object prefix) {
            return ops.compressMaps() ? compressedCodec.encode(input, ops, prefix) : uncompressedCodec.encode(input, ops, prefix);
         }

         public DataResult decode(DynamicOps ops, Object input) {
            return ops.compressMaps() ? compressedCodec.decode(ops, input) : uncompressedCodec.decode(ops, input);
         }

         public String toString() {
            return uncompressedCodec + " orCompressed " + compressedCodec;
         }
      };
   }

   public static Codec withLifecycle(Codec originalCodec, final Function entryLifecycleGetter, final Function lifecycleGetter) {
      return originalCodec.mapResult(new Codec.ResultFunction() {
         public DataResult apply(DynamicOps ops, Object input, DataResult result) {
            return (DataResult)result.result().map((pair) -> {
               return result.setLifecycle((Lifecycle)entryLifecycleGetter.apply(pair.getFirst()));
            }).orElse(result);
         }

         public DataResult coApply(DynamicOps ops, Object input, DataResult result) {
            return result.setLifecycle((Lifecycle)lifecycleGetter.apply(input));
         }

         public String toString() {
            return "WithLifecycle[" + entryLifecycleGetter + " " + lifecycleGetter + "]";
         }
      });
   }

   public static Codec validate(Codec codec, Function validator) {
      return codec.flatXmap(validator, validator);
   }

   private static Codec rangedInt(int min, int max, Function messageFactory) {
      return validate(Codec.INT, (value) -> {
         return value.compareTo(min) >= 0 && value.compareTo(max) <= 0 ? DataResult.success(value) : DataResult.error(() -> {
            return (String)messageFactory.apply(value);
         });
      });
   }

   public static Codec rangedInt(int min, int max) {
      return rangedInt(min, max, (value) -> {
         return "Value must be within range [" + min + ";" + max + "]: " + value;
      });
   }

   private static Codec rangedFloat(float min, float max, Function messageFactory) {
      return validate(Codec.FLOAT, (value) -> {
         return value.compareTo(min) > 0 && value.compareTo(max) <= 0 ? DataResult.success(value) : DataResult.error(() -> {
            return (String)messageFactory.apply(value);
         });
      });
   }

   public static Codec nonEmptyList(Codec originalCodec) {
      return validate(originalCodec, (list) -> {
         return list.isEmpty() ? DataResult.error(() -> {
            return "List must have contents";
         }) : DataResult.success(list);
      });
   }

   public static Codec nonEmptyEntryList(Codec originalCodec) {
      return validate(originalCodec, (entryList) -> {
         return entryList.getStorage().right().filter(List::isEmpty).isPresent() ? DataResult.error(() -> {
            return "List must have contents";
         }) : DataResult.success(entryList);
      });
   }

   public static Codec createLazy(Supplier supplier) {
      return new Lazy(supplier);
   }

   public static MapCodec createContextRetrievalCodec(final Function retriever) {
      class ContextRetrievalCodec extends MapCodec {
         public RecordBuilder encode(Object input, DynamicOps ops, RecordBuilder prefix) {
            return prefix;
         }

         public DataResult decode(DynamicOps ops, MapLike input) {
            return (DataResult)retriever.apply(ops);
         }

         public String toString() {
            return "ContextRetrievalCodec[" + retriever + "]";
         }

         public Stream keys(DynamicOps ops) {
            return Stream.empty();
         }
      }

      return new ContextRetrievalCodec();
   }

   public static Function createEqualTypeChecker(Function typeGetter) {
      return (collection) -> {
         Iterator iterator = collection.iterator();
         if (iterator.hasNext()) {
            Object object = typeGetter.apply(iterator.next());

            while(iterator.hasNext()) {
               Object object2 = iterator.next();
               Object object3 = typeGetter.apply(object2);
               if (object3 != object) {
                  return DataResult.error(() -> {
                     return "Mixed type list: element " + object2 + " had type " + object3 + ", but list is of type " + object;
                  });
               }
            }
         }

         return DataResult.success(collection, Lifecycle.stable());
      };
   }

   public static Codec exceptionCatching(final Codec codec) {
      return Codec.of(codec, new Decoder() {
         public DataResult decode(DynamicOps ops, Object input) {
            try {
               return codec.decode(ops, input);
            } catch (Exception var4) {
               return DataResult.error(() -> {
                  return "Caught exception decoding " + input + ": " + var4.getMessage();
               });
            }
         }
      });
   }

   public static Codec instant(DateTimeFormatter formatter) {
      PrimitiveCodec var10000 = Codec.STRING;
      Function var10001 = (dateTimeString) -> {
         try {
            return DataResult.success(Instant.from(formatter.parse(dateTimeString)));
         } catch (Exception var3) {
            Objects.requireNonNull(var3);
            return DataResult.error(var3::getMessage);
         }
      };
      Objects.requireNonNull(formatter);
      return var10000.comapFlatMap(var10001, formatter::format);
   }

   public static MapCodec optionalLong(MapCodec codec) {
      return codec.xmap(OPTIONAL_OF_LONG_TO_OPTIONAL_LONG, OPTIONAL_LONG_TO_OPTIONAL_OF_LONG);
   }

   private static DataResult createGameProfileFromPair(Pair pair) {
      try {
         return DataResult.success(new GameProfile((UUID)((Optional)pair.getFirst()).orElse((Object)null), (String)((Optional)pair.getSecond()).orElse((Object)null)));
      } catch (Throwable var2) {
         Objects.requireNonNull(var2);
         return DataResult.error(var2::getMessage);
      }
   }

   private static DataResult createPairFromGameProfile(GameProfile profile) {
      return DataResult.success(Pair.of(Optional.ofNullable(profile.getId()), Optional.ofNullable(profile.getName())));
   }

   public static Codec string(int minLength, int maxLength) {
      return validate(Codec.STRING, (string) -> {
         int k = string.length();
         if (k < minLength) {
            return DataResult.error(() -> {
               return "String \"" + string + "\" is too short: " + k + ", expected range [" + minLength + "-" + maxLength + "]";
            });
         } else {
            return k > maxLength ? DataResult.error(() -> {
               return "String \"" + string + "\" is too long: " + k + ", expected range [" + minLength + "-" + maxLength + "]";
            }) : DataResult.success(string);
         }
      });
   }

   static {
      JSON_ELEMENT = Codec.PASSTHROUGH.xmap((dynamic) -> {
         return (JsonElement)dynamic.convert(JsonOps.INSTANCE).getValue();
      }, (element) -> {
         return new Dynamic(JsonOps.INSTANCE, element);
      });
      TEXT = JSON_ELEMENT.flatXmap((element) -> {
         try {
            return DataResult.success(Text.Serializer.fromJson(element));
         } catch (JsonParseException var2) {
            Objects.requireNonNull(var2);
            return DataResult.error(var2::getMessage);
         }
      }, (text) -> {
         try {
            return DataResult.success(Text.Serializer.toJsonTree(text));
         } catch (IllegalArgumentException var2) {
            Objects.requireNonNull(var2);
            return DataResult.error(var2::getMessage);
         }
      });
      STRINGIFIED_TEXT = Codec.STRING.flatXmap((json) -> {
         try {
            return DataResult.success(Text.Serializer.fromJson(json));
         } catch (JsonParseException var2) {
            Objects.requireNonNull(var2);
            return DataResult.error(var2::getMessage);
         }
      }, (text) -> {
         try {
            return DataResult.success(Text.Serializer.toJson(text));
         } catch (IllegalArgumentException var2) {
            Objects.requireNonNull(var2);
            return DataResult.error(var2::getMessage);
         }
      });
      VECTOR_3F = Codec.FLOAT.listOf().comapFlatMap((list) -> {
         return Util.toArray((List)list, 3).map((listx) -> {
            return new Vector3f((Float)listx.get(0), (Float)listx.get(1), (Float)listx.get(2));
         });
      }, (vec3f) -> {
         return List.of(vec3f.x(), vec3f.y(), vec3f.z());
      });
      QUATERNIONF = Codec.FLOAT.listOf().comapFlatMap((list) -> {
         return Util.toArray((List)list, 4).map((listx) -> {
            return new Quaternionf((Float)listx.get(0), (Float)listx.get(1), (Float)listx.get(2), (Float)listx.get(3));
         });
      }, (quaternion) -> {
         return List.of(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
      });
      AXIS_ANGLE4F = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.FLOAT.fieldOf("angle").forGetter((axisAngle) -> {
            return axisAngle.angle;
         }), VECTOR_3F.fieldOf("axis").forGetter((axisAngle) -> {
            return new Vector3f(axisAngle.x, axisAngle.y, axisAngle.z);
         })).apply(instance, AxisAngle4f::new);
      });
      ROTATION = Codec.either(QUATERNIONF, AXIS_ANGLE4F.xmap(Quaternionf::new, AxisAngle4f::new)).xmap((either) -> {
         return (Quaternionf)either.map((quaternion) -> {
            return quaternion;
         }, (quaternion) -> {
            return quaternion;
         });
      }, com.mojang.datafixers.util.Either::left);
      MATRIX4F = Codec.FLOAT.listOf().comapFlatMap((list) -> {
         return Util.toArray((List)list, 16).map((listx) -> {
            Matrix4f matrix4f = new Matrix4f();

            for(int i = 0; i < listx.size(); ++i) {
               matrix4f.setRowColumn(i >> 2, i & 3, (Float)listx.get(i));
            }

            return matrix4f.determineProperties();
         });
      }, (matrix4f) -> {
         FloatList floatList = new FloatArrayList(16);

         for(int i = 0; i < 16; ++i) {
            floatList.add(matrix4f.getRowColumn(i >> 2, i & 3));
         }

         return floatList;
      });
      NONNEGATIVE_INT = rangedInt(0, Integer.MAX_VALUE, (v) -> {
         return "Value must be non-negative: " + v;
      });
      POSITIVE_INT = rangedInt(1, Integer.MAX_VALUE, (v) -> {
         return "Value must be positive: " + v;
      });
      POSITIVE_FLOAT = rangedFloat(0.0F, Float.MAX_VALUE, (v) -> {
         return "Value must be positive: " + v;
      });
      REGULAR_EXPRESSION = Codec.STRING.comapFlatMap((pattern) -> {
         try {
            return DataResult.success(Pattern.compile(pattern));
         } catch (PatternSyntaxException var2) {
            return DataResult.error(() -> {
               return "Invalid regex pattern '" + pattern + "': " + var2.getMessage();
            });
         }
      }, Pattern::pattern);
      INSTANT = instant(DateTimeFormatter.ISO_INSTANT);
      BASE_64 = Codec.STRING.comapFlatMap((encoded) -> {
         try {
            return DataResult.success(Base64.getDecoder().decode(encoded));
         } catch (IllegalArgumentException var2) {
            return DataResult.error(() -> {
               return "Malformed base64 string";
            });
         }
      }, (data) -> {
         return Base64.getEncoder().encodeToString(data);
      });
      TAG_ENTRY_ID = Codec.STRING.comapFlatMap((tagEntry) -> {
         return tagEntry.startsWith("#") ? Identifier.validate(tagEntry.substring(1)).map((id) -> {
            return new TagEntryId(id, true);
         }) : Identifier.validate(tagEntry).map((id) -> {
            return new TagEntryId(id, false);
         });
      }, TagEntryId::asString);
      OPTIONAL_OF_LONG_TO_OPTIONAL_LONG = (optional) -> {
         return (OptionalLong)optional.map(OptionalLong::of).orElseGet(OptionalLong::empty);
      };
      OPTIONAL_LONG_TO_OPTIONAL_OF_LONG = (optionalLong) -> {
         return optionalLong.isPresent() ? Optional.of(optionalLong.getAsLong()) : Optional.empty();
      };
      BIT_SET = Codec.LONG_STREAM.xmap((stream) -> {
         return BitSet.valueOf(stream.toArray());
      }, (set) -> {
         return Arrays.stream(set.toLongArray());
      });
      GAME_PROFILE_PROPERTY = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.STRING.fieldOf("name").forGetter(Property::getName), Codec.STRING.fieldOf("value").forGetter(Property::getValue), Codec.STRING.optionalFieldOf("signature").forGetter((property) -> {
            return Optional.ofNullable(property.getSignature());
         })).apply(instance, (key, value, signature) -> {
            return new Property(key, value, (String)signature.orElse((Object)null));
         });
      });
      GAME_PROFILE_PROPERTY_MAP = Codec.either(Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()), GAME_PROFILE_PROPERTY.listOf()).xmap((either) -> {
         PropertyMap propertyMap = new PropertyMap();
         either.ifLeft((map) -> {
            map.forEach((key, values) -> {
               Iterator var3 = values.iterator();

               while(var3.hasNext()) {
                  String string2 = (String)var3.next();
                  propertyMap.put(key, new Property(key, string2));
               }

            });
         }).ifRight((properties) -> {
            Iterator var2 = properties.iterator();

            while(var2.hasNext()) {
               Property property = (Property)var2.next();
               propertyMap.put(property.getName(), property);
            }

         });
         return propertyMap;
      }, (properties) -> {
         return com.mojang.datafixers.util.Either.right(properties.values().stream().toList());
      });
      GAME_PROFILE = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.mapPair(Uuids.CODEC.xmap(Optional::of, (optional) -> {
            return (UUID)optional.orElse((Object)null);
         }).optionalFieldOf("id", Optional.empty()), Codec.STRING.xmap(Optional::of, (optional) -> {
            return (String)optional.orElse((Object)null);
         }).optionalFieldOf("name", Optional.empty())).flatXmap(Codecs::createGameProfileFromPair, Codecs::createPairFromGameProfile).forGetter(Function.identity()), GAME_PROFILE_PROPERTY_MAP.optionalFieldOf("properties", new PropertyMap()).forGetter(GameProfile::getProperties)).apply(instance, (profile, properties) -> {
            properties.forEach((key, property) -> {
               profile.getProperties().put(key, property);
            });
            return profile;
         });
      });
      NON_EMPTY_STRING = validate(Codec.STRING, (string) -> {
         return string.isEmpty() ? DataResult.error(() -> {
            return "Expected non-empty string";
         }) : DataResult.success(string);
      });
   }

   private static final class Xor implements Codec {
      private final Codec first;
      private final Codec second;

      public Xor(Codec first, Codec second) {
         this.first = first;
         this.second = second;
      }

      public DataResult decode(DynamicOps ops, Object input) {
         DataResult dataResult = this.first.decode(ops, input).map((pair) -> {
            return pair.mapFirst(com.mojang.datafixers.util.Either::left);
         });
         DataResult dataResult2 = this.second.decode(ops, input).map((pair) -> {
            return pair.mapFirst(com.mojang.datafixers.util.Either::right);
         });
         Optional optional = dataResult.result();
         Optional optional2 = dataResult2.result();
         if (optional.isPresent() && optional2.isPresent()) {
            return DataResult.error(() -> {
               Object var10000 = optional.get();
               return "Both alternatives read successfully, can not pick the correct one; first: " + var10000 + " second: " + optional2.get();
            }, (Pair)optional.get());
         } else {
            return optional.isPresent() ? dataResult : dataResult2;
         }
      }

      public DataResult encode(com.mojang.datafixers.util.Either either, DynamicOps dynamicOps, Object object) {
         return (DataResult)either.map((left) -> {
            return this.first.encode(left, dynamicOps, object);
         }, (right) -> {
            return this.second.encode(right, dynamicOps, object);
         });
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Xor lv = (Xor)o;
            return Objects.equals(this.first, lv.first) && Objects.equals(this.second, lv.second);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.first, this.second});
      }

      public String toString() {
         return "XorCodec[" + this.first + ", " + this.second + "]";
      }

      // $FF: synthetic method
      public DataResult encode(Object input, DynamicOps ops, Object prefix) {
         return this.encode((com.mojang.datafixers.util.Either)input, ops, prefix);
      }
   }

   static final class Either implements Codec {
      private final Codec first;
      private final Codec second;

      public Either(Codec first, Codec second) {
         this.first = first;
         this.second = second;
      }

      public DataResult decode(DynamicOps ops, Object input) {
         DataResult dataResult = this.first.decode(ops, input).map((pair) -> {
            return pair.mapFirst(com.mojang.datafixers.util.Either::left);
         });
         if (!dataResult.error().isPresent()) {
            return dataResult;
         } else {
            DataResult dataResult2 = this.second.decode(ops, input).map((pair) -> {
               return pair.mapFirst(com.mojang.datafixers.util.Either::right);
            });
            return !dataResult2.error().isPresent() ? dataResult2 : dataResult.apply2((pair, pair2) -> {
               return pair2;
            }, dataResult2);
         }
      }

      public DataResult encode(com.mojang.datafixers.util.Either either, DynamicOps dynamicOps, Object object) {
         return (DataResult)either.map((left) -> {
            return this.first.encode(left, dynamicOps, object);
         }, (right) -> {
            return this.second.encode(right, dynamicOps, object);
         });
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Either lv = (Either)o;
            return Objects.equals(this.first, lv.first) && Objects.equals(this.second, lv.second);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.first, this.second});
      }

      public String toString() {
         return "EitherCodec[" + this.first + ", " + this.second + "]";
      }

      // $FF: synthetic method
      public DataResult encode(Object input, DynamicOps ops, Object prefix) {
         return this.encode((com.mojang.datafixers.util.Either)input, ops, prefix);
      }
   }

   static record Lazy(Supplier delegate) implements Codec {
      Lazy(Supplier supplier) {
         Objects.requireNonNull(supplier);
         Supplier supplier = Suppliers.memoize(supplier::get);
         this.delegate = supplier;
      }

      public DataResult decode(DynamicOps ops, Object input) {
         return ((Codec)this.delegate.get()).decode(ops, input);
      }

      public DataResult encode(Object input, DynamicOps ops, Object prefix) {
         return ((Codec)this.delegate.get()).encode(input, ops, prefix);
      }

      public Supplier delegate() {
         return this.delegate;
      }
   }

   public static record TagEntryId(Identifier id, boolean tag) {
      public TagEntryId(Identifier arg, boolean bl) {
         this.id = arg;
         this.tag = bl;
      }

      public String toString() {
         return this.asString();
      }

      private String asString() {
         return this.tag ? "#" + this.id : this.id.toString();
      }

      public Identifier id() {
         return this.id;
      }

      public boolean tag() {
         return this.tag;
      }
   }
}
