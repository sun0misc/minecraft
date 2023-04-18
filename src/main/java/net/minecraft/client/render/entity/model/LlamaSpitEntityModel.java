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
public class LlamaSpitEntityModel extends SinglePartEntityModel {
   private static final String MAIN = "main";
   private final ModelPart root;

   public LlamaSpitEntityModel(ModelPart root) {
      this.root = root;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      int i = true;
      lv2.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F).cuboid(0.0F, -4.0F, 0.0F, 2.0F, 2.0F, 2.0F).cuboid(0.0F, 0.0F, -4.0F, 2.0F, 2.0F, 2.0F).cuboid(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F).cuboid(2.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F).cuboid(0.0F, 2.0F, 0.0F, 2.0F, 2.0F, 2.0F).cuboid(0.0F, 0.0F, 2.0F, 2.0F, 2.0F, 2.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 32);
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
   }

   public ModelPart getPart() {
      return this.root;
   }
}
