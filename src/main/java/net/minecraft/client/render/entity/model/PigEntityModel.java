package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

@Environment(EnvType.CLIENT)
public class PigEntityModel extends QuadrupedEntityModel {
   public PigEntityModel(ModelPart root) {
      super(root, false, 4.0F, 4.0F, 2.0F, 2.0F, 24);
   }

   public static TexturedModelData getTexturedModelData(Dilation dilation) {
      ModelData lv = QuadrupedEntityModel.getModelData(6, dilation);
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, dilation).uv(16, 16).cuboid(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F, dilation), ModelTransform.pivot(0.0F, 12.0F, -6.0F));
      return TexturedModelData.of(lv, 64, 32);
   }
}
