package net.minecraft.predicate.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillagerDataContainer;
import org.jetbrains.annotations.Nullable;

public interface TypeSpecificPredicate {
   TypeSpecificPredicate ANY = new TypeSpecificPredicate() {
      public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
         return true;
      }

      public JsonObject typeSpecificToJson() {
         return new JsonObject();
      }

      public Deserializer getDeserializer() {
         return TypeSpecificPredicate.Deserializers.ANY;
      }
   };

   static TypeSpecificPredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(json, "type_specific");
         String string = JsonHelper.getString(jsonObject, "type", (String)null);
         if (string == null) {
            return ANY;
         } else {
            Deserializer lv = (Deserializer)TypeSpecificPredicate.Deserializers.TYPES.get(string);
            if (lv == null) {
               throw new JsonSyntaxException("Unknown sub-predicate type: " + string);
            } else {
               return lv.deserialize(jsonObject);
            }
         }
      } else {
         return ANY;
      }
   }

   boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos);

   JsonObject typeSpecificToJson();

   default JsonElement toJson() {
      if (this.getDeserializer() == TypeSpecificPredicate.Deserializers.ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = this.typeSpecificToJson();
         String string = (String)TypeSpecificPredicate.Deserializers.TYPES.inverse().get(this.getDeserializer());
         jsonObject.addProperty("type", string);
         return jsonObject;
      }
   }

   Deserializer getDeserializer();

   static TypeSpecificPredicate cat(CatVariant variant) {
      return TypeSpecificPredicate.Deserializers.CAT.createPredicate(variant);
   }

   static TypeSpecificPredicate frog(FrogVariant variant) {
      return TypeSpecificPredicate.Deserializers.FROG.createPredicate(variant);
   }

   public static final class Deserializers {
      public static final Deserializer ANY = (json) -> {
         return TypeSpecificPredicate.ANY;
      };
      public static final Deserializer LIGHTNING = LightningBoltPredicate::fromJson;
      public static final Deserializer FISHING_HOOK = FishingHookPredicate::fromJson;
      public static final Deserializer PLAYER = PlayerPredicate::fromJson;
      public static final Deserializer SLIME = SlimePredicate::fromJson;
      public static final VariantPredicates CAT;
      public static final VariantPredicates FROG;
      public static final VariantPredicates AXOLOTL;
      public static final VariantPredicates BOAT;
      public static final VariantPredicates FOX;
      public static final VariantPredicates MOOSHROOM;
      public static final VariantPredicates PAINTING;
      public static final VariantPredicates RABBIT;
      public static final VariantPredicates HORSE;
      public static final VariantPredicates LLAMA;
      public static final VariantPredicates VILLAGER;
      public static final VariantPredicates PARROT;
      public static final VariantPredicates TROPICAL_FISH;
      public static final BiMap TYPES;

      static {
         CAT = VariantPredicates.create(Registries.CAT_VARIANT, (entity) -> {
            Optional var10000;
            if (entity instanceof CatEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         FROG = VariantPredicates.create(Registries.FROG_VARIANT, (entity) -> {
            Optional var10000;
            if (entity instanceof FrogEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         AXOLOTL = VariantPredicates.create(AxolotlEntity.Variant.CODEC, (entity) -> {
            Optional var10000;
            if (entity instanceof AxolotlEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         BOAT = VariantPredicates.create((Codec)BoatEntity.Type.CODEC, (entity) -> {
            Optional var10000;
            if (entity instanceof BoatEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         FOX = VariantPredicates.create((Codec)FoxEntity.Type.CODEC, (entity) -> {
            Optional var10000;
            if (entity instanceof FoxEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         MOOSHROOM = VariantPredicates.create((Codec)MooshroomEntity.Type.CODEC, (entity) -> {
            Optional var10000;
            if (entity instanceof MooshroomEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         PAINTING = VariantPredicates.create(Registries.PAINTING_VARIANT.createEntryCodec(), (entity) -> {
            Optional var10000;
            if (entity instanceof PaintingEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         RABBIT = VariantPredicates.create(RabbitEntity.RabbitType.CODEC, (entity) -> {
            Optional var10000;
            if (entity instanceof RabbitEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         HORSE = VariantPredicates.create(HorseColor.CODEC, (entity) -> {
            Optional var10000;
            if (entity instanceof HorseEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         LLAMA = VariantPredicates.create(LlamaEntity.Variant.CODEC, (entity) -> {
            Optional var10000;
            if (entity instanceof LlamaEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         VILLAGER = VariantPredicates.create(Registries.VILLAGER_TYPE.getCodec(), (entity) -> {
            Optional var10000;
            if (entity instanceof VillagerDataContainer lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         PARROT = VariantPredicates.create(ParrotEntity.Variant.CODEC, (entity) -> {
            Optional var10000;
            if (entity instanceof ParrotEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         TROPICAL_FISH = VariantPredicates.create(TropicalFishEntity.Variety.CODEC, (entity) -> {
            Optional var10000;
            if (entity instanceof TropicalFishEntity lv) {
               var10000 = Optional.of(lv.getVariant());
            } else {
               var10000 = Optional.empty();
            }

            return var10000;
         });
         TYPES = ImmutableBiMap.builder().put("any", ANY).put("lightning", LIGHTNING).put("fishing_hook", FISHING_HOOK).put("player", PLAYER).put("slime", SLIME).put("cat", CAT.getDeserializer()).put("frog", FROG.getDeserializer()).put("axolotl", AXOLOTL.getDeserializer()).put("boat", BOAT.getDeserializer()).put("fox", FOX.getDeserializer()).put("mooshroom", MOOSHROOM.getDeserializer()).put("painting", PAINTING.getDeserializer()).put("rabbit", RABBIT.getDeserializer()).put("horse", HORSE.getDeserializer()).put("llama", LLAMA.getDeserializer()).put("villager", VILLAGER.getDeserializer()).put("parrot", PARROT.getDeserializer()).put("tropical_fish", TROPICAL_FISH.getDeserializer()).buildOrThrow();
      }
   }

   public interface Deserializer {
      TypeSpecificPredicate deserialize(JsonObject json);
   }
}
