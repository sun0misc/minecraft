package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

@Environment(EnvType.CLIENT)
public class EntityModelLoader implements SynchronousResourceReloader {
   private Map modelParts = ImmutableMap.of();

   public ModelPart getModelPart(EntityModelLayer layer) {
      TexturedModelData lv = (TexturedModelData)this.modelParts.get(layer);
      if (lv == null) {
         throw new IllegalArgumentException("No model for layer " + layer);
      } else {
         return lv.createModel();
      }
   }

   public void reload(ResourceManager manager) {
      this.modelParts = ImmutableMap.copyOf(EntityModels.getModels());
   }
}
