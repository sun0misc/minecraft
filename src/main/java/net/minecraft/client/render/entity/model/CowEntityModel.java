package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

@Environment(EnvType.CLIENT)
public class CowEntityModel extends QuadrupedEntityModel {
   public CowEntityModel(ModelPart root) {
      super(root, false, 10.0F, 4.0F, 2.0F, 2.0F, 24);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      int i = true;
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F).uv(22, 0).cuboid(EntityModelPartNames.RIGHT_HORN, -5.0F, -5.0F, -4.0F, 1.0F, 3.0F, 1.0F).uv(22, 0).cuboid(EntityModelPartNames.LEFT_HORN, 4.0F, -5.0F, -4.0F, 1.0F, 3.0F, 1.0F), ModelTransform.pivot(0.0F, 4.0F, -8.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(18, 4).cuboid(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F).uv(52, 0).cuboid(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F), ModelTransform.of(0.0F, 5.0F, 2.0F, 1.5707964F, 0.0F, 0.0F));
      ModelPartBuilder lv3 = ModelPartBuilder.create().uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F);
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-4.0F, 12.0F, 7.0F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv3, ModelTransform.pivot(4.0F, 12.0F, 7.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv3, ModelTransform.pivot(-4.0F, 12.0F, -6.0F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv3, ModelTransform.pivot(4.0F, 12.0F, -6.0F));
      return TexturedModelData.of(lv, 64, 32);
   }

   public ModelPart getHead() {
      return this.head;
   }
}
