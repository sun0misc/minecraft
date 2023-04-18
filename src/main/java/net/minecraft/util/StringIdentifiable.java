package net.minecraft.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

public interface StringIdentifiable {
   int field_38377 = 16;

   String asString();

   static Codec createCodec(Supplier enumValues) {
      return createCodec(enumValues, (id) -> {
         return id;
      });
   }

   static Codec createCodec(Supplier enumValues, Function valueNameTransformer) {
      Enum[] enums = (Enum[])enumValues.get();
      if (enums.length > 16) {
         Map map = (Map)Arrays.stream(enums).collect(Collectors.toMap((enum_) -> {
            return (String)valueNameTransformer.apply(((StringIdentifiable)enum_).asString());
         }, (enum_) -> {
            return enum_;
         }));
         return new Codec(enums, (id) -> {
            return id == null ? null : (Enum)map.get(id);
         });
      } else {
         return new Codec(enums, (id) -> {
            Enum[] var3 = enums;
            int var4 = enums.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               Enum enum_ = var3[var5];
               if (((String)valueNameTransformer.apply(((StringIdentifiable)enum_).asString())).equals(id)) {
                  return enum_;
               }
            }

            return null;
         });
      }
   }

   static Keyable toKeyable(final StringIdentifiable[] values) {
      return new Keyable() {
         public Stream keys(DynamicOps ops) {
            Stream var10000 = Arrays.stream(values).map(StringIdentifiable::asString);
            Objects.requireNonNull(ops);
            return var10000.map(ops::createString);
         }
      };
   }

   /** @deprecated */
   @Deprecated
   public static class Codec implements com.mojang.serialization.Codec {
      private final com.mojang.serialization.Codec base;
      private final Function idToIdentifiable;

      public Codec(Enum[] values, Function idToIdentifiable) {
         this.base = Codecs.orCompressed(Codecs.idChecked((identifiable) -> {
            return ((StringIdentifiable)identifiable).asString();
         }, idToIdentifiable), Codecs.rawIdChecked((enum_) -> {
            return ((Enum)enum_).ordinal();
         }, (ordinal) -> {
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
         }, -1));
         this.idToIdentifiable = idToIdentifiable;
      }

      public DataResult decode(DynamicOps ops, Object input) {
         return this.base.decode(ops, input);
      }

      public DataResult encode(Enum enum_, DynamicOps dynamicOps, Object object) {
         return this.base.encode(enum_, dynamicOps, object);
      }

      @Nullable
      public Enum byId(@Nullable String id) {
         return (Enum)this.idToIdentifiable.apply(id);
      }

      public Enum byId(@Nullable String id, Enum fallback) {
         return (Enum)Objects.requireNonNullElse(this.byId(id), fallback);
      }

      // $FF: synthetic method
      public DataResult encode(Object input, DynamicOps ops, Object prefix) {
         return this.encode((Enum)input, ops, prefix);
      }
   }
}
