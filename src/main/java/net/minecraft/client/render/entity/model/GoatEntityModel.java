package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.passive.GoatEntity;

@Environment(EnvType.CLIENT)
public class GoatEntityModel extends QuadrupedEntityModel {
   public GoatEntityModel(ModelPart root) {
      super(root, true, 19.0F, 1.0F, 2.5F, 2.0F, 24);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(2, 61).cuboid("right ear", -6.0F, -11.0F, -10.0F, 3.0F, 2.0F, 1.0F).uv(2, 61).mirrored().cuboid("left ear", 2.0F, -11.0F, -10.0F, 3.0F, 2.0F, 1.0F).uv(23, 52).cuboid("goatee", -0.5F, -3.0F, -14.0F, 0.0F, 7.0F, 5.0F), ModelTransform.pivot(1.0F, 14.0F, 0.0F));
      lv3.addChild(EntityModelPartNames.LEFT_HORN, ModelPartBuilder.create().uv(12, 55).cuboid(-0.01F, -16.0F, -10.0F, 2.0F, 7.0F, 2.0F), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
      lv3.addChild(EntityModelPartNames.RIGHT_HORN, ModelPartBuilder.create().uv(12, 55).cuboid(-2.99F, -16.0F, -10.0F, 2.0F, 7.0F, 2.0F), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
      lv3.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(34, 46).cuboid(-3.0F, -4.0F, -8.0F, 5.0F, 7.0F, 10.0F), ModelTransform.of(0.0F, -8.0F, -8.0F, 0.9599F, 0.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(1, 1).cuboid(-4.0F, -17.0F, -7.0F, 9.0F, 11.0F, 16.0F).uv(0, 28).cuboid(-5.0F, -18.0F, -8.0F, 11.0F, 14.0F, 11.0F), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(36, 29).cuboid(0.0F, 4.0F, 0.0F, 3.0F, 6.0F, 3.0F), ModelTransform.pivot(1.0F, 14.0F, 4.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(49, 29).cuboid(0.0F, 4.0F, 0.0F, 3.0F, 6.0F, 3.0F), ModelTransform.pivot(-3.0F, 14.0F, 4.0F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(49, 2).cuboid(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F), ModelTransform.pivot(1.0F, 14.0F, -6.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(35, 2).cuboid(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F), ModelTransform.pivot(-3.0F, 14.0F, -6.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public void setAngles(GoatEntity arg, float f, float g, float h, float i, float j) {
      this.head.getChild(EntityModelPartNames.LEFT_HORN).visible = arg.hasLeftHorn();
      this.head.getChild(EntityModelPartNames.RIGHT_HORN).visible = arg.hasRightHorn();
      super.setAngles(arg, f, g, h, i, j);
      float k = arg.getHeadPitch();
      if (k != 0.0F) {
         this.head.pitch = k;
      }

   }
}
