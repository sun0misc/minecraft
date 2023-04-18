package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;

@Environment(EnvType.CLIENT)
public class ArmorEntityModel extends BipedEntityModel {
   public ArmorEntityModel(ModelPart arg) {
      super(arg);
   }

   public static ModelData getModelData(Dilation dilation) {
      ModelData lv = BipedEntityModel.getModelData(dilation, 0.0F);
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(-0.1F)), ModelTransform.pivot(-1.9F, 12.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(-0.1F)), ModelTransform.pivot(1.9F, 12.0F, 0.0F));
      return lv;
   }
}
