package net.minecraft.client.render.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MultipartUnbakedModel implements UnbakedModel {
   private final StateManager stateFactory;
   private final List components;

   public MultipartUnbakedModel(StateManager stateFactory, List components) {
      this.stateFactory = stateFactory;
      this.components = components;
   }

   public List getComponents() {
      return this.components;
   }

   public Set getModels() {
      Set set = Sets.newHashSet();
      Iterator var2 = this.components.iterator();

      while(var2.hasNext()) {
         MultipartModelComponent lv = (MultipartModelComponent)var2.next();
         set.add(lv.getModel());
      }

      return set;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof MultipartUnbakedModel)) {
         return false;
      } else {
         MultipartUnbakedModel lv = (MultipartUnbakedModel)o;
         return Objects.equals(this.stateFactory, lv.stateFactory) && Objects.equals(this.components, lv.components);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.stateFactory, this.components});
   }

   public Collection getModelDependencies() {
      return (Collection)this.getComponents().stream().flatMap((component) -> {
         return component.getModel().getModelDependencies().stream();
      }).collect(Collectors.toSet());
   }

   public void setParents(Function modelLoader) {
      this.getComponents().forEach((component) -> {
         component.getModel().setParents(modelLoader);
      });
   }

   @Nullable
   public BakedModel bake(Baker baker, Function textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
      MultipartBakedModel.Builder lv = new MultipartBakedModel.Builder();
      Iterator var6 = this.getComponents().iterator();

      while(var6.hasNext()) {
         MultipartModelComponent lv2 = (MultipartModelComponent)var6.next();
         BakedModel lv3 = lv2.getModel().bake(baker, textureGetter, rotationContainer, modelId);
         if (lv3 != null) {
            lv.addComponent(lv2.getPredicate(this.stateFactory), lv3);
         }
      }

      return lv.build();
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      private final ModelVariantMap.DeserializationContext context;

      public Deserializer(ModelVariantMap.DeserializationContext context) {
         this.context = context;
      }

      public MultipartUnbakedModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         return new MultipartUnbakedModel(this.context.getStateFactory(), this.deserializeComponents(jsonDeserializationContext, jsonElement.getAsJsonArray()));
      }

      private List deserializeComponents(JsonDeserializationContext context, JsonArray array) {
         List list = Lists.newArrayList();
         Iterator var4 = array.iterator();

         while(var4.hasNext()) {
            JsonElement jsonElement = (JsonElement)var4.next();
            list.add((MultipartModelComponent)context.deserialize(jsonElement, MultipartModelComponent.class));
         }

         return list;
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, type, context);
      }
   }
}
