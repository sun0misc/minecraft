package net.minecraft.client.render.model.json;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WeightedUnbakedModel implements UnbakedModel {
   private final List variants;

   public WeightedUnbakedModel(List variants) {
      this.variants = variants;
   }

   public List getVariants() {
      return this.variants;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o instanceof WeightedUnbakedModel) {
         WeightedUnbakedModel lv = (WeightedUnbakedModel)o;
         return this.variants.equals(lv.variants);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.variants.hashCode();
   }

   public Collection getModelDependencies() {
      return (Collection)this.getVariants().stream().map(ModelVariant::getLocation).collect(Collectors.toSet());
   }

   public void setParents(Function modelLoader) {
      this.getVariants().stream().map(ModelVariant::getLocation).distinct().forEach((id) -> {
         ((UnbakedModel)modelLoader.apply(id)).setParents(modelLoader);
      });
   }

   @Nullable
   public BakedModel bake(Baker baker, Function textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
      if (this.getVariants().isEmpty()) {
         return null;
      } else {
         WeightedBakedModel.Builder lv = new WeightedBakedModel.Builder();
         Iterator var6 = this.getVariants().iterator();

         while(var6.hasNext()) {
            ModelVariant lv2 = (ModelVariant)var6.next();
            BakedModel lv3 = baker.bake(lv2.getLocation(), lv2);
            lv.add(lv3, lv2.getWeight());
         }

         return lv.build();
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public WeightedUnbakedModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         List list = Lists.newArrayList();
         if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            if (jsonArray.size() == 0) {
               throw new JsonParseException("Empty variant array");
            }

            Iterator var6 = jsonArray.iterator();

            while(var6.hasNext()) {
               JsonElement jsonElement2 = (JsonElement)var6.next();
               list.add((ModelVariant)jsonDeserializationContext.deserialize(jsonElement2, ModelVariant.class));
            }
         } else {
            list.add((ModelVariant)jsonDeserializationContext.deserialize(jsonElement, ModelVariant.class));
         }

         return new WeightedUnbakedModel(list);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement functionJson, Type unused, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(functionJson, unused, context);
      }
   }
}
