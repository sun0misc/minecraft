package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModelPartData {
   private final List cuboidData;
   private final ModelTransform rotationData;
   private final Map children = Maps.newHashMap();

   ModelPartData(List cuboidData, ModelTransform rotationData) {
      this.cuboidData = cuboidData;
      this.rotationData = rotationData;
   }

   public ModelPartData addChild(String name, ModelPartBuilder builder, ModelTransform rotationData) {
      ModelPartData lv = new ModelPartData(builder.build(), rotationData);
      ModelPartData lv2 = (ModelPartData)this.children.put(name, lv);
      if (lv2 != null) {
         lv.children.putAll(lv2.children);
      }

      return lv;
   }

   public ModelPart createPart(int textureWidth, int textureHeight) {
      Object2ObjectArrayMap object2ObjectArrayMap = (Object2ObjectArrayMap)this.children.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> {
         return ((ModelPartData)entry.getValue()).createPart(textureWidth, textureHeight);
      }, (arg, arg2) -> {
         return arg;
      }, Object2ObjectArrayMap::new));
      List list = (List)this.cuboidData.stream().map((arg) -> {
         return arg.createCuboid(textureWidth, textureHeight);
      }).collect(ImmutableList.toImmutableList());
      ModelPart lv = new ModelPart(list, object2ObjectArrayMap);
      lv.setDefaultTransform(this.rotationData);
      lv.setTransform(this.rotationData);
      return lv;
   }

   public ModelPartData getChild(String name) {
      return (ModelPartData)this.children.get(name);
   }
}
