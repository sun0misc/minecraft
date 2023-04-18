package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class MinecartEntityModel extends SinglePartEntityModel {
   private final ModelPart root;

   public MinecartEntityModel(ModelPart root) {
      this.root = root;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      int i = true;
      int j = true;
      int k = true;
      int l = true;
      lv2.addChild("bottom", ModelPartBuilder.create().uv(0, 10).cuboid(-10.0F, -8.0F, -1.0F, 20.0F, 16.0F, 2.0F), ModelTransform.of(0.0F, 4.0F, 0.0F, 1.5707964F, 0.0F, 0.0F));
      lv2.addChild("front", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F), ModelTransform.of(-9.0F, 4.0F, 0.0F, 0.0F, 4.712389F, 0.0F));
      lv2.addChild("back", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F), ModelTransform.of(9.0F, 4.0F, 0.0F, 0.0F, 1.5707964F, 0.0F));
      lv2.addChild("left", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F), ModelTransform.of(0.0F, 4.0F, -7.0F, 0.0F, 3.1415927F, 0.0F));
      lv2.addChild("right", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F), ModelTransform.pivot(0.0F, 4.0F, 7.0F));
      return TexturedModelData.of(lv, 64, 32);
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
   }

   public ModelPart getPart() {
      return this.root;
   }
}
