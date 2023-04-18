package net.minecraft.world.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SpawnSettings {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float field_30983 = 0.1F;
   public static final Pool EMPTY_ENTRY_POOL = Pool.empty();
   public static final SpawnSettings INSTANCE = (new Builder()).build();
   public static final MapCodec CODEC = RecordCodecBuilder.mapCodec((instance) -> {
      RecordCodecBuilder var10001 = Codec.floatRange(0.0F, 0.9999999F).optionalFieldOf("creature_spawn_probability", 0.1F).forGetter((arg) -> {
         return arg.creatureSpawnProbability;
      });
      Codec var10002 = SpawnGroup.CODEC;
      Codec var10003 = Pool.createCodec(SpawnSettings.SpawnEntry.CODEC);
      Logger var10005 = LOGGER;
      Objects.requireNonNull(var10005);
      return instance.group(var10001, Codec.simpleMap(var10002, var10003.promotePartial(Util.addPrefix("Spawn data: ", var10005::error)), StringIdentifiable.toKeyable(SpawnGroup.values())).fieldOf("spawners").forGetter((arg) -> {
         return arg.spawners;
      }), Codec.simpleMap(Registries.ENTITY_TYPE.getCodec(), SpawnSettings.SpawnDensity.CODEC, Registries.ENTITY_TYPE).fieldOf("spawn_costs").forGetter((arg) -> {
         return arg.spawnCosts;
      })).apply(instance, SpawnSettings::new);
   });
   private final float creatureSpawnProbability;
   private final Map spawners;
   private final Map spawnCosts;

   SpawnSettings(float creatureSpawnProbability, Map spawners, Map spawnCosts) {
      this.creatureSpawnProbability = creatureSpawnProbability;
      this.spawners = ImmutableMap.copyOf(spawners);
      this.spawnCosts = ImmutableMap.copyOf(spawnCosts);
   }

   public Pool getSpawnEntries(SpawnGroup spawnGroup) {
      return (Pool)this.spawners.getOrDefault(spawnGroup, EMPTY_ENTRY_POOL);
   }

   @Nullable
   public SpawnDensity getSpawnDensity(EntityType entityType) {
      return (SpawnDensity)this.spawnCosts.get(entityType);
   }

   public float getCreatureSpawnProbability() {
      return this.creatureSpawnProbability;
   }

   public static record SpawnDensity(double gravityLimit, double mass) {
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.DOUBLE.fieldOf("energy_budget").forGetter((spawnDensity) -> {
            return spawnDensity.gravityLimit;
         }), Codec.DOUBLE.fieldOf("charge").forGetter((spawnDensity) -> {
            return spawnDensity.mass;
         })).apply(instance, SpawnDensity::new);
      });

      public SpawnDensity(double gravityLimit, double mass) {
         this.gravityLimit = gravityLimit;
         this.mass = mass;
      }

      public double gravityLimit() {
         return this.gravityLimit;
      }

      public double mass() {
         return this.mass;
      }
   }

   public static class SpawnEntry extends Weighted.Absent {
      public static final Codec CODEC = Codecs.validate(RecordCodecBuilder.create((instance) -> {
         return instance.group(Registries.ENTITY_TYPE.getCodec().fieldOf("type").forGetter((spawnEntry) -> {
            return spawnEntry.type;
         }), Weight.CODEC.fieldOf("weight").forGetter(Weighted.Absent::getWeight), Codecs.POSITIVE_INT.fieldOf("minCount").forGetter((spawnEntry) -> {
            return spawnEntry.minGroupSize;
         }), Codecs.POSITIVE_INT.fieldOf("maxCount").forGetter((spawnEntry) -> {
            return spawnEntry.maxGroupSize;
         })).apply(instance, SpawnEntry::new);
      }), (spawnEntry) -> {
         return spawnEntry.minGroupSize > spawnEntry.maxGroupSize ? DataResult.error(() -> {
            return "minCount needs to be smaller or equal to maxCount";
         }) : DataResult.success(spawnEntry);
      });
      public final EntityType type;
      public final int minGroupSize;
      public final int maxGroupSize;

      public SpawnEntry(EntityType type, int weight, int minGroupSize, int maxGroupSize) {
         this(type, Weight.of(weight), minGroupSize, maxGroupSize);
      }

      public SpawnEntry(EntityType type, Weight weight, int minGroupSize, int maxGroupSize) {
         super(weight);
         this.type = type.getSpawnGroup() == SpawnGroup.MISC ? EntityType.PIG : type;
         this.minGroupSize = minGroupSize;
         this.maxGroupSize = maxGroupSize;
      }

      public String toString() {
         Identifier var10000 = EntityType.getId(this.type);
         return "" + var10000 + "*(" + this.minGroupSize + "-" + this.maxGroupSize + "):" + this.getWeight();
      }
   }

   public static class Builder {
      private final Map spawners = (Map)Stream.of(SpawnGroup.values()).collect(ImmutableMap.toImmutableMap((arg) -> {
         return arg;
      }, (arg) -> {
         return Lists.newArrayList();
      }));
      private final Map spawnCosts = Maps.newLinkedHashMap();
      private float creatureSpawnProbability = 0.1F;

      public Builder spawn(SpawnGroup spawnGroup, SpawnEntry spawnEntry) {
         ((List)this.spawners.get(spawnGroup)).add(spawnEntry);
         return this;
      }

      public Builder spawnCost(EntityType entityType, double mass, double gravityLimit) {
         this.spawnCosts.put(entityType, new SpawnDensity(gravityLimit, mass));
         return this;
      }

      public Builder creatureSpawnProbability(float probability) {
         this.creatureSpawnProbability = probability;
         return this;
      }

      public SpawnSettings build() {
         return new SpawnSettings(this.creatureSpawnProbability, (Map)this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry) -> {
            return Pool.of((List)entry.getValue());
         })), ImmutableMap.copyOf(this.spawnCosts));
      }
   }
}
