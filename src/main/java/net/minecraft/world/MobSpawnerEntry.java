package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.dynamic.Range;

public record MobSpawnerEntry(NbtCompound entity, Optional customSpawnRules) {
   public static final String ENTITY_KEY = "entity";
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(NbtCompound.CODEC.fieldOf("entity").forGetter((entry) -> {
         return entry.entity;
      }), MobSpawnerEntry.CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter((entry) -> {
         return entry.customSpawnRules;
      })).apply(instance, MobSpawnerEntry::new);
   });
   public static final Codec DATA_POOL_CODEC;

   public MobSpawnerEntry() {
      this(new NbtCompound(), Optional.empty());
   }

   public MobSpawnerEntry(NbtCompound arg, Optional optional) {
      if (arg.contains("id")) {
         Identifier lv = Identifier.tryParse(arg.getString("id"));
         if (lv != null) {
            arg.putString("id", lv.toString());
         } else {
            arg.remove("id");
         }
      }

      this.entity = arg;
      this.customSpawnRules = optional;
   }

   public NbtCompound getNbt() {
      return this.entity;
   }

   public Optional getCustomSpawnRules() {
      return this.customSpawnRules;
   }

   public NbtCompound entity() {
      return this.entity;
   }

   public Optional customSpawnRules() {
      return this.customSpawnRules;
   }

   static {
      DATA_POOL_CODEC = DataPool.createEmptyAllowedCodec(CODEC);
   }

   public static record CustomSpawnRules(Range blockLightLimit, Range skyLightLimit) {
      private static final Range DEFAULT = new Range(0, 15);
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Range.CODEC.optionalFieldOf("block_light_limit", DEFAULT).flatXmap(CustomSpawnRules::validate, CustomSpawnRules::validate).forGetter((rules) -> {
            return rules.blockLightLimit;
         }), Range.CODEC.optionalFieldOf("sky_light_limit", DEFAULT).flatXmap(CustomSpawnRules::validate, CustomSpawnRules::validate).forGetter((rules) -> {
            return rules.skyLightLimit;
         })).apply(instance, CustomSpawnRules::new);
      });

      public CustomSpawnRules(Range arg, Range arg2) {
         this.blockLightLimit = arg;
         this.skyLightLimit = arg2;
      }

      private static DataResult validate(Range provider) {
         return !DEFAULT.contains(provider) ? DataResult.error(() -> {
            return "Light values must be withing range " + DEFAULT;
         }) : DataResult.success(provider);
      }

      public Range blockLightLimit() {
         return this.blockLightLimit;
      }

      public Range skyLightLimit() {
         return this.skyLightLimit;
      }
   }
}
