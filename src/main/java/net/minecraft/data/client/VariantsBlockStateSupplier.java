package net.minecraft.data.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.util.Util;

public class VariantsBlockStateSupplier implements BlockStateSupplier {
   private final Block block;
   private final List variants;
   private final Set definedProperties = Sets.newHashSet();
   private final List variantMaps = Lists.newArrayList();

   private VariantsBlockStateSupplier(Block block, List variants) {
      this.block = block;
      this.variants = variants;
   }

   public VariantsBlockStateSupplier coordinate(BlockStateVariantMap map) {
      map.getProperties().forEach((property) -> {
         if (this.block.getStateManager().getProperty(property.getName()) != property) {
            throw new IllegalStateException("Property " + property + " is not defined for block " + this.block);
         } else if (!this.definedProperties.add(property)) {
            throw new IllegalStateException("Values of property " + property + " already defined for block " + this.block);
         }
      });
      this.variantMaps.add(map);
      return this;
   }

   public JsonElement get() {
      Stream stream = Stream.of(Pair.of(PropertiesMap.empty(), this.variants));

      Map map;
      for(Iterator var2 = this.variantMaps.iterator(); var2.hasNext(); stream = stream.flatMap((pair) -> {
         return map.entrySet().stream().map((entry) -> {
            PropertiesMap lv = ((PropertiesMap)pair.getFirst()).copyOf((PropertiesMap)entry.getKey());
            List list = intersect((List)pair.getSecond(), (List)entry.getValue());
            return Pair.of(lv, list);
         });
      })) {
         BlockStateVariantMap lv = (BlockStateVariantMap)var2.next();
         map = lv.getVariants();
      }

      Map map2 = new TreeMap();
      stream.forEach((pair) -> {
         map2.put(((PropertiesMap)pair.getFirst()).asString(), BlockStateVariant.toJson((List)pair.getSecond()));
      });
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("variants", (JsonElement)Util.make(new JsonObject(), (json) -> {
         Objects.requireNonNull(json);
         map2.forEach(json::add);
      }));
      return jsonObject;
   }

   private static List intersect(List left, List right) {
      ImmutableList.Builder builder = ImmutableList.builder();
      left.forEach((leftVariant) -> {
         right.forEach((rightVariant) -> {
            builder.add(BlockStateVariant.union(leftVariant, rightVariant));
         });
      });
      return builder.build();
   }

   public Block getBlock() {
      return this.block;
   }

   public static VariantsBlockStateSupplier create(Block block) {
      return new VariantsBlockStateSupplier(block, ImmutableList.of(BlockStateVariant.create()));
   }

   public static VariantsBlockStateSupplier create(Block block, BlockStateVariant variant) {
      return new VariantsBlockStateSupplier(block, ImmutableList.of(variant));
   }

   public static VariantsBlockStateSupplier create(Block block, BlockStateVariant... variants) {
      return new VariantsBlockStateSupplier(block, ImmutableList.copyOf(variants));
   }

   // $FF: synthetic method
   public Object get() {
      return this.get();
   }
}
