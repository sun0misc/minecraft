package net.minecraft.loot.context;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.loot.LootDataLookup;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class LootContext {
   private final Random random;
   private final float luck;
   private final ServerWorld world;
   private final LootDataLookup dataLookup;
   private final Set activeEntries = Sets.newLinkedHashSet();
   private final Map parameters;
   private final Map drops;

   LootContext(Random random, float luck, ServerWorld world, LootDataLookup dataLookup, Map parameters, Map drops) {
      this.random = random;
      this.luck = luck;
      this.world = world;
      this.dataLookup = dataLookup;
      this.parameters = ImmutableMap.copyOf(parameters);
      this.drops = ImmutableMap.copyOf(drops);
   }

   public boolean hasParameter(LootContextParameter parameter) {
      return this.parameters.containsKey(parameter);
   }

   public Object requireParameter(LootContextParameter parameter) {
      Object object = this.parameters.get(parameter);
      if (object == null) {
         throw new NoSuchElementException(parameter.getId().toString());
      } else {
         return object;
      }
   }

   public void drop(Identifier id, Consumer lootConsumer) {
      Dropper lv = (Dropper)this.drops.get(id);
      if (lv != null) {
         lv.add(this, lootConsumer);
      }

   }

   @Nullable
   public Object get(LootContextParameter parameter) {
      return this.parameters.get(parameter);
   }

   public boolean isActive(Entry entry) {
      return this.activeEntries.contains(entry);
   }

   public boolean markActive(Entry entry) {
      return this.activeEntries.add(entry);
   }

   public void markInactive(Entry entry) {
      this.activeEntries.remove(entry);
   }

   public LootDataLookup getDataLookup() {
      return this.dataLookup;
   }

   public Random getRandom() {
      return this.random;
   }

   public float getLuck() {
      return this.luck;
   }

   public ServerWorld getWorld() {
      return this.world;
   }

   public static Entry table(LootTable table) {
      return new Entry(LootDataType.LOOT_TABLES, table);
   }

   public static Entry predicate(LootCondition predicate) {
      return new Entry(LootDataType.PREDICATES, predicate);
   }

   public static Entry itemModifier(LootFunction itemModifier) {
      return new Entry(LootDataType.ITEM_MODIFIERS, itemModifier);
   }

   @FunctionalInterface
   public interface Dropper {
      void add(LootContext context, Consumer consumer);
   }

   public static record Entry(LootDataType type, Object value) {
      public Entry(LootDataType arg, Object object) {
         this.type = arg;
         this.value = object;
      }

      public LootDataType type() {
         return this.type;
      }

      public Object value() {
         return this.value;
      }
   }

   public static enum EntityTarget {
      THIS("this", LootContextParameters.THIS_ENTITY),
      KILLER("killer", LootContextParameters.KILLER_ENTITY),
      DIRECT_KILLER("direct_killer", LootContextParameters.DIRECT_KILLER_ENTITY),
      KILLER_PLAYER("killer_player", LootContextParameters.LAST_DAMAGE_PLAYER);

      final String type;
      private final LootContextParameter parameter;

      private EntityTarget(String type, LootContextParameter parameter) {
         this.type = type;
         this.parameter = parameter;
      }

      public LootContextParameter getParameter() {
         return this.parameter;
      }

      public static EntityTarget fromString(String type) {
         EntityTarget[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            EntityTarget lv = var1[var3];
            if (lv.type.equals(type)) {
               return lv;
            }
         }

         throw new IllegalArgumentException("Invalid entity target " + type);
      }

      // $FF: synthetic method
      private static EntityTarget[] method_36793() {
         return new EntityTarget[]{THIS, KILLER, DIRECT_KILLER, KILLER_PLAYER};
      }

      public static class Serializer extends TypeAdapter {
         public void write(JsonWriter jsonWriter, EntityTarget arg) throws IOException {
            jsonWriter.value(arg.type);
         }

         public EntityTarget read(JsonReader jsonReader) throws IOException {
            return LootContext.EntityTarget.fromString(jsonReader.nextString());
         }

         // $FF: synthetic method
         public Object read(JsonReader reader) throws IOException {
            return this.read(reader);
         }

         // $FF: synthetic method
         public void write(JsonWriter writer, Object entity) throws IOException {
            this.write(writer, (EntityTarget)entity);
         }
      }
   }

   public static class Builder {
      private final ServerWorld world;
      private final Map parameters = Maps.newIdentityHashMap();
      private final Map drops = Maps.newHashMap();
      @Nullable
      private Random random;
      private float luck;

      public Builder(ServerWorld world) {
         this.world = world;
      }

      public Builder random(Random random) {
         this.random = random;
         return this;
      }

      public Builder random(long seed) {
         if (seed != 0L) {
            this.random = Random.create(seed);
         }

         return this;
      }

      public Builder random(long seed, Random random) {
         if (seed == 0L) {
            this.random = random;
         } else {
            this.random = Random.create(seed);
         }

         return this;
      }

      public Builder luck(float luck) {
         this.luck = luck;
         return this;
      }

      public Builder parameter(LootContextParameter key, Object value) {
         this.parameters.put(key, value);
         return this;
      }

      public Builder optionalParameter(LootContextParameter key, @Nullable Object value) {
         if (value == null) {
            this.parameters.remove(key);
         } else {
            this.parameters.put(key, value);
         }

         return this;
      }

      public Builder putDrop(Identifier id, Dropper value) {
         Dropper lv = (Dropper)this.drops.put(id, value);
         if (lv != null) {
            throw new IllegalStateException("Duplicated dynamic drop '" + this.drops + "'");
         } else {
            return this;
         }
      }

      public ServerWorld getWorld() {
         return this.world;
      }

      public Object get(LootContextParameter parameter) {
         Object object = this.parameters.get(parameter);
         if (object == null) {
            throw new IllegalArgumentException("No parameter " + parameter);
         } else {
            return object;
         }
      }

      @Nullable
      public Object getNullable(LootContextParameter parameter) {
         return this.parameters.get(parameter);
      }

      public LootContext build(LootContextType type) {
         Set set = Sets.difference(this.parameters.keySet(), type.getAllowed());
         if (!set.isEmpty()) {
            throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
         } else {
            Set set2 = Sets.difference(type.getRequired(), this.parameters.keySet());
            if (!set2.isEmpty()) {
               throw new IllegalArgumentException("Missing required parameters: " + set2);
            } else {
               Random lv = this.random;
               if (lv == null) {
                  lv = Random.create();
               }

               MinecraftServer minecraftServer = this.world.getServer();
               return new LootContext(lv, this.luck, this.world, minecraftServer.getLootManager(), this.parameters, this.drops);
            }
         }
      }
   }
}
