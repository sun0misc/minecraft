package net.minecraft.data.client;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BlockStateVariant implements Supplier {
   private final Map properties = Maps.newLinkedHashMap();

   public BlockStateVariant put(VariantSetting key, Object value) {
      VariantSetting.Value lv = (VariantSetting.Value)this.properties.put(key, key.evaluate(value));
      if (lv != null) {
         throw new IllegalStateException("Replacing value of " + lv + " with " + value);
      } else {
         return this;
      }
   }

   public static BlockStateVariant create() {
      return new BlockStateVariant();
   }

   public static BlockStateVariant union(BlockStateVariant first, BlockStateVariant second) {
      BlockStateVariant lv = new BlockStateVariant();
      lv.properties.putAll(first.properties);
      lv.properties.putAll(second.properties);
      return lv;
   }

   public JsonElement get() {
      JsonObject jsonObject = new JsonObject();
      this.properties.values().forEach((value) -> {
         value.writeTo(jsonObject);
      });
      return jsonObject;
   }

   public static JsonElement toJson(List variants) {
      if (variants.size() == 1) {
         return ((BlockStateVariant)variants.get(0)).get();
      } else {
         JsonArray jsonArray = new JsonArray();
         variants.forEach((variant) -> {
            jsonArray.add(variant.get());
         });
         return jsonArray;
      }
   }

   // $FF: synthetic method
   public Object get() {
      return this.get();
   }
}
