package net.minecraft.predicate.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.Objects;
import net.minecraft.block.CampfireBlock;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.FluidPredicate;
import net.minecraft.predicate.LightPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LocationPredicate {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LocationPredicate ANY;
   private final NumberRange.FloatRange x;
   private final NumberRange.FloatRange y;
   private final NumberRange.FloatRange z;
   @Nullable
   private final RegistryKey biome;
   @Nullable
   private final RegistryKey feature;
   @Nullable
   private final RegistryKey dimension;
   @Nullable
   private final Boolean smokey;
   private final LightPredicate light;
   private final BlockPredicate block;
   private final FluidPredicate fluid;

   public LocationPredicate(NumberRange.FloatRange x, NumberRange.FloatRange y, NumberRange.FloatRange z, @Nullable RegistryKey biome, @Nullable RegistryKey feature, @Nullable RegistryKey dimension, @Nullable Boolean smokey, LightPredicate light, BlockPredicate block, FluidPredicate fluid) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.biome = biome;
      this.feature = feature;
      this.dimension = dimension;
      this.smokey = smokey;
      this.light = light;
      this.block = block;
      this.fluid = fluid;
   }

   public static LocationPredicate biome(RegistryKey biome) {
      return new LocationPredicate(NumberRange.FloatRange.ANY, NumberRange.FloatRange.ANY, NumberRange.FloatRange.ANY, biome, (RegistryKey)null, (RegistryKey)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate dimension(RegistryKey dimension) {
      return new LocationPredicate(NumberRange.FloatRange.ANY, NumberRange.FloatRange.ANY, NumberRange.FloatRange.ANY, (RegistryKey)null, (RegistryKey)null, dimension, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate feature(RegistryKey feature) {
      return new LocationPredicate(NumberRange.FloatRange.ANY, NumberRange.FloatRange.ANY, NumberRange.FloatRange.ANY, (RegistryKey)null, feature, (RegistryKey)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate y(NumberRange.FloatRange y) {
      return new LocationPredicate(NumberRange.FloatRange.ANY, y, NumberRange.FloatRange.ANY, (RegistryKey)null, (RegistryKey)null, (RegistryKey)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public boolean test(ServerWorld world, double x, double y, double z) {
      if (!this.x.test(x)) {
         return false;
      } else if (!this.y.test(y)) {
         return false;
      } else if (!this.z.test(z)) {
         return false;
      } else if (this.dimension != null && this.dimension != world.getRegistryKey()) {
         return false;
      } else {
         BlockPos lv = BlockPos.ofFloored(x, y, z);
         boolean bl = world.canSetBlock(lv);
         if (this.biome != null && (!bl || !world.getBiome(lv).matchesKey(this.biome))) {
            return false;
         } else if (this.feature != null && (!bl || !world.getStructureAccessor().getStructureContaining(lv, this.feature).hasChildren())) {
            return false;
         } else if (this.smokey == null || bl && this.smokey == CampfireBlock.isLitCampfireInRange(world, lv)) {
            if (!this.light.test(world, lv)) {
               return false;
            } else if (!this.block.test(world, lv)) {
               return false;
            } else {
               return this.fluid.test(world, lv);
            }
         } else {
            return false;
         }
      }
   }

   public JsonElement toJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         if (!this.x.isDummy() || !this.y.isDummy() || !this.z.isDummy()) {
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.add("x", this.x.toJson());
            jsonObject2.add("y", this.y.toJson());
            jsonObject2.add("z", this.z.toJson());
            jsonObject.add("position", jsonObject2);
         }

         if (this.dimension != null) {
            DataResult var10000 = World.CODEC.encodeStart(JsonOps.INSTANCE, this.dimension);
            Logger var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            var10000.resultOrPartial(var10001::error).ifPresent((json) -> {
               jsonObject.add("dimension", json);
            });
         }

         if (this.feature != null) {
            jsonObject.addProperty("structure", this.feature.getValue().toString());
         }

         if (this.biome != null) {
            jsonObject.addProperty("biome", this.biome.getValue().toString());
         }

         if (this.smokey != null) {
            jsonObject.addProperty("smokey", this.smokey);
         }

         jsonObject.add("light", this.light.toJson());
         jsonObject.add("block", this.block.toJson());
         jsonObject.add("fluid", this.fluid.toJson());
         return jsonObject;
      }
   }

   public static LocationPredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(json, "location");
         JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "position", new JsonObject());
         NumberRange.FloatRange lv = NumberRange.FloatRange.fromJson(jsonObject2.get("x"));
         NumberRange.FloatRange lv2 = NumberRange.FloatRange.fromJson(jsonObject2.get("y"));
         NumberRange.FloatRange lv3 = NumberRange.FloatRange.fromJson(jsonObject2.get("z"));
         RegistryKey var14;
         DataResult var10000;
         Logger var10001;
         if (jsonObject.has("dimension")) {
            var10000 = Identifier.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("dimension"));
            var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            var14 = (RegistryKey)var10000.resultOrPartial(var10001::error).map((arg) -> {
               return RegistryKey.of(RegistryKeys.WORLD, arg);
            }).orElse((Object)null);
         } else {
            var14 = null;
         }

         RegistryKey lv4 = var14;
         if (jsonObject.has("structure")) {
            var10000 = Identifier.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("structure"));
            var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            var14 = (RegistryKey)var10000.resultOrPartial(var10001::error).map((arg) -> {
               return RegistryKey.of(RegistryKeys.STRUCTURE, arg);
            }).orElse((Object)null);
         } else {
            var14 = null;
         }

         RegistryKey lv5 = var14;
         RegistryKey lv6 = null;
         if (jsonObject.has("biome")) {
            Identifier lv7 = new Identifier(JsonHelper.getString(jsonObject, "biome"));
            lv6 = RegistryKey.of(RegistryKeys.BIOME, lv7);
         }

         Boolean boolean_ = jsonObject.has("smokey") ? jsonObject.get("smokey").getAsBoolean() : null;
         LightPredicate lv8 = LightPredicate.fromJson(jsonObject.get("light"));
         BlockPredicate lv9 = BlockPredicate.fromJson(jsonObject.get("block"));
         FluidPredicate lv10 = FluidPredicate.fromJson(jsonObject.get("fluid"));
         return new LocationPredicate(lv, lv2, lv3, lv6, lv5, lv4, boolean_, lv8, lv9, lv10);
      } else {
         return ANY;
      }
   }

   static {
      ANY = new LocationPredicate(NumberRange.FloatRange.ANY, NumberRange.FloatRange.ANY, NumberRange.FloatRange.ANY, (RegistryKey)null, (RegistryKey)null, (RegistryKey)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static class Builder {
      private NumberRange.FloatRange x;
      private NumberRange.FloatRange y;
      private NumberRange.FloatRange z;
      @Nullable
      private RegistryKey biome;
      @Nullable
      private RegistryKey feature;
      @Nullable
      private RegistryKey dimension;
      @Nullable
      private Boolean smokey;
      private LightPredicate light;
      private BlockPredicate block;
      private FluidPredicate fluid;

      public Builder() {
         this.x = NumberRange.FloatRange.ANY;
         this.y = NumberRange.FloatRange.ANY;
         this.z = NumberRange.FloatRange.ANY;
         this.light = LightPredicate.ANY;
         this.block = BlockPredicate.ANY;
         this.fluid = FluidPredicate.ANY;
      }

      public static Builder create() {
         return new Builder();
      }

      public Builder x(NumberRange.FloatRange x) {
         this.x = x;
         return this;
      }

      public Builder y(NumberRange.FloatRange y) {
         this.y = y;
         return this;
      }

      public Builder z(NumberRange.FloatRange z) {
         this.z = z;
         return this;
      }

      public Builder biome(@Nullable RegistryKey biome) {
         this.biome = biome;
         return this;
      }

      public Builder feature(@Nullable RegistryKey feature) {
         this.feature = feature;
         return this;
      }

      public Builder dimension(@Nullable RegistryKey dimension) {
         this.dimension = dimension;
         return this;
      }

      public Builder light(LightPredicate light) {
         this.light = light;
         return this;
      }

      public Builder block(BlockPredicate block) {
         this.block = block;
         return this;
      }

      public Builder fluid(FluidPredicate fluid) {
         this.fluid = fluid;
         return this;
      }

      public Builder smokey(Boolean smokey) {
         this.smokey = smokey;
         return this;
      }

      public LocationPredicate build() {
         return new LocationPredicate(this.x, this.y, this.z, this.biome, this.feature, this.dimension, this.smokey, this.light, this.block, this.fluid);
      }
   }
}
