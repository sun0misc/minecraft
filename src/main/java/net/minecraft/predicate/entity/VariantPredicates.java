package net.minecraft.predicate.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class VariantPredicates {
   private static final String VARIANT_KEY = "variant";
   final Codec codec;
   final Function variantGetter;
   final TypeSpecificPredicate.Deserializer deserializer;

   public static VariantPredicates create(Registry registry, Function variantGetter) {
      return new VariantPredicates(registry.getCodec(), variantGetter);
   }

   public static VariantPredicates create(Codec codec, Function variantGetter) {
      return new VariantPredicates(codec, variantGetter);
   }

   private VariantPredicates(Codec codec, Function variantGetter) {
      this.codec = codec;
      this.variantGetter = variantGetter;
      this.deserializer = (json) -> {
         JsonElement jsonElement = json.get("variant");
         if (jsonElement == null) {
            throw new JsonParseException("Missing variant field");
         } else {
            Object object = ((Pair)Util.getResult(codec.decode(new Dynamic(JsonOps.INSTANCE, jsonElement)), JsonParseException::new)).getFirst();
            return this.createPredicate(object);
         }
      };
   }

   public TypeSpecificPredicate.Deserializer getDeserializer() {
      return this.deserializer;
   }

   public TypeSpecificPredicate createPredicate(final Object variant) {
      return new TypeSpecificPredicate() {
         public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
            return ((Optional)VariantPredicates.this.variantGetter.apply(entity)).filter((variantx) -> {
               return variantx.equals(variant);
            }).isPresent();
         }

         public JsonObject typeSpecificToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("variant", (JsonElement)Util.getResult(VariantPredicates.this.codec.encodeStart(JsonOps.INSTANCE, variant), (string) -> {
               return new JsonParseException("Can't serialize variant " + variant + ", message " + string);
            }));
            return jsonObject;
         }

         public TypeSpecificPredicate.Deserializer getDeserializer() {
            return VariantPredicates.this.deserializer;
         }
      };
   }
}
