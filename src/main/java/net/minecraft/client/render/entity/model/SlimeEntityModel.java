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
public class SlimeEntityModel extends SinglePartEntityModel {
   private final ModelPart root;

   public SlimeEntityModel(ModelPart root) {
      this.root = root;
   }

   public static TexturedModelData getOuterTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.CUBE, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 32);
   }

   public static TexturedModelData getInnerTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.CUBE, ModelPartBuilder.create().uv(0, 16).cuboid(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.RIGHT_EYE, ModelPartBuilder.create().uv(32, 0).cuboid(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.LEFT_EYE, ModelPartBuilder.create().uv(32, 4).cuboid(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.MOUTH, ModelPartBuilder.create().uv(32, 8).cuboid(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 32);
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
   }

   public ModelPart getPart() {
      return this.root;
   }
}
