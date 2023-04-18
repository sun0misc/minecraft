package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.datafixer.TypeReferences;

public class StructuresToConfiguredStructuresFix extends DataFix {
   private static final Map STRUCTURE_TO_CONFIGURED_STRUCTURES_MAPPING = ImmutableMap.builder().put("mineshaft", StructuresToConfiguredStructuresFix.Mapping.create(Map.of(List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"), "minecraft:mineshaft_mesa"), "minecraft:mineshaft")).put("shipwreck", StructuresToConfiguredStructuresFix.Mapping.create(Map.of(List.of("minecraft:beach", "minecraft:snowy_beach"), "minecraft:shipwreck_beached"), "minecraft:shipwreck")).put("ocean_ruin", StructuresToConfiguredStructuresFix.Mapping.create(Map.of(List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"), "minecraft:ocean_ruin_warm"), "minecraft:ocean_ruin_cold")).put("village", StructuresToConfiguredStructuresFix.Mapping.create(Map.of(List.of("minecraft:desert"), "minecraft:village_desert", List.of("minecraft:savanna"), "minecraft:village_savanna", List.of("minecraft:snowy_plains"), "minecraft:village_snowy", List.of("minecraft:taiga"), "minecraft:village_taiga"), "minecraft:village_plains")).put("ruined_portal", StructuresToConfiguredStructuresFix.Mapping.create(Map.of(List.of("minecraft:desert"), "minecraft:ruined_portal_desert", List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands", "minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:savanna_plateau", "minecraft:windswept_savanna", "minecraft:stony_shore", "minecraft:meadow", "minecraft:frozen_peaks", "minecraft:jagged_peaks", "minecraft:stony_peaks", "minecraft:snowy_slopes"), "minecraft:ruined_portal_mountain", List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle"), "minecraft:ruined_portal_jungle", List.of("minecraft:deep_frozen_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:frozen_ocean", "minecraft:ocean", "minecraft:cold_ocean", "minecraft:lukewarm_ocean", "minecraft:warm_ocean"), "minecraft:ruined_portal_ocean"), "minecraft:ruined_portal")).put("pillager_outpost", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:pillager_outpost")).put("mansion", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:mansion")).put("jungle_pyramid", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:jungle_pyramid")).put("desert_pyramid", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:desert_pyramid")).put("igloo", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:igloo")).put("swamp_hut", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:swamp_hut")).put("stronghold", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:stronghold")).put("monument", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:monument")).put("fortress", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:fortress")).put("endcity", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:end_city")).put("buried_treasure", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:buried_treasure")).put("nether_fossil", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:nether_fossil")).put("bastion_remnant", StructuresToConfiguredStructuresFix.Mapping.create("minecraft:bastion_remnant")).build();

   public StructuresToConfiguredStructuresFix(Schema schema) {
      super(schema, false);
   }

   protected TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.CHUNK);
      Type type2 = this.getInputSchema().getType(TypeReferences.CHUNK);
      return this.writeFixAndRead("StucturesToConfiguredStructures", type, type2, this::method_41012);
   }

   private Dynamic method_41012(Dynamic dynamic) {
      return dynamic.update("structures", (dynamic2) -> {
         return dynamic2.update("starts", (dynamic2x) -> {
            return this.method_41015(dynamic2x, dynamic);
         }).update("References", (dynamic2x) -> {
            return this.method_41020(dynamic2x, dynamic);
         });
      });
   }

   private Dynamic method_41015(Dynamic dynamic, Dynamic dynamic2) {
      Map map = (Map)dynamic.getMapValues().result().get();
      List list = new ArrayList();
      map.forEach((dynamicx, dynamic2x) -> {
         if (dynamic2x.get("id").asString("INVALID").equals("INVALID")) {
            list.add(dynamicx);
         }

      });

      Dynamic dynamic3;
      for(Iterator var5 = list.iterator(); var5.hasNext(); dynamic = dynamic.remove(dynamic3.asString(""))) {
         dynamic3 = (Dynamic)var5.next();
      }

      return dynamic.updateMapValues((pair) -> {
         return this.method_41010(pair, dynamic2);
      });
   }

   private Pair method_41010(Pair pair, Dynamic dynamic) {
      Dynamic dynamic2 = this.method_41022(pair, dynamic);
      return new Pair(dynamic2, ((Dynamic)pair.getSecond()).set("id", dynamic2));
   }

   private Dynamic method_41020(Dynamic dynamic, Dynamic dynamic2) {
      Map map = (Map)dynamic.getMapValues().result().get();
      List list = new ArrayList();
      map.forEach((dynamicx, dynamic2x) -> {
         if (dynamic2x.asLongStream().count() == 0L) {
            list.add(dynamicx);
         }

      });

      Dynamic dynamic3;
      for(Iterator var5 = list.iterator(); var5.hasNext(); dynamic = dynamic.remove(dynamic3.asString(""))) {
         dynamic3 = (Dynamic)var5.next();
      }

      return dynamic.updateMapValues((pair) -> {
         return this.method_41018(pair, dynamic2);
      });
   }

   private Pair method_41018(Pair pair, Dynamic dynamic) {
      return pair.mapFirst((dynamic2) -> {
         return this.method_41022(pair, dynamic);
      });
   }

   private Dynamic method_41022(Pair pair, Dynamic dynamic) {
      String string = ((Dynamic)pair.getFirst()).asString("UNKNOWN").toLowerCase(Locale.ROOT);
      Mapping lv = (Mapping)STRUCTURE_TO_CONFIGURED_STRUCTURES_MAPPING.get(string);
      if (lv == null) {
         throw new IllegalStateException("Found unknown structure: " + string);
      } else {
         Dynamic dynamic2 = (Dynamic)pair.getSecond();
         String string2 = lv.fallback;
         if (!lv.biomeMapping().isEmpty()) {
            Optional optional = this.method_41013(dynamic, lv);
            if (optional.isPresent()) {
               string2 = (String)optional.get();
            }
         }

         Dynamic dynamic3 = dynamic2.createString(string2);
         return dynamic3;
      }
   }

   private Optional method_41013(Dynamic dynamic, Mapping arg) {
      Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
      dynamic.get("sections").asList(Function.identity()).forEach((dynamicx) -> {
         dynamicx.get("biomes").get("palette").asList(Function.identity()).forEach((dynamic) -> {
            String string = (String)arg.biomeMapping().get(dynamic.asString(""));
            if (string != null) {
               object2IntArrayMap.mergeInt(string, 1, Integer::sum);
            }

         });
      });
      return object2IntArrayMap.object2IntEntrySet().stream().max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue)).map(Map.Entry::getKey);
   }

   private static record Mapping(Map biomeMapping, String fallback) {
      final String fallback;

      private Mapping(Map map, String string) {
         this.biomeMapping = map;
         this.fallback = string;
      }

      public static Mapping create(String mapping) {
         return new Mapping(Map.of(), mapping);
      }

      public static Mapping create(Map biomeMapping, String fallback) {
         return new Mapping(flattenBiomeMapping(biomeMapping), fallback);
      }

      private static Map flattenBiomeMapping(Map biomeMapping) {
         ImmutableMap.Builder builder = ImmutableMap.builder();
         Iterator var2 = biomeMapping.entrySet().iterator();

         while(var2.hasNext()) {
            Map.Entry entry = (Map.Entry)var2.next();
            ((List)entry.getKey()).forEach((string) -> {
               builder.put(string, (String)entry.getValue());
            });
         }

         return builder.build();
      }

      public Map biomeMapping() {
         return this.biomeMapping;
      }

      public String fallback() {
         return this.fallback;
      }
   }
}
