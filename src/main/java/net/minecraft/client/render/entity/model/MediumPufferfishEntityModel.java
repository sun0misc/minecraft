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
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class MediumPufferfishEntityModel extends SinglePartEntityModel {
   private final ModelPart root;
   private final ModelPart leftBlueFin;
   private final ModelPart rightBlueFin;

   public MediumPufferfishEntityModel(ModelPart root) {
      this.root = root;
      this.leftBlueFin = root.getChild(EntityModelPartNames.LEFT_BLUE_FIN);
      this.rightBlueFin = root.getChild(EntityModelPartNames.RIGHT_BLUE_FIN);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      int i = true;
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(12, 22).cuboid(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F), ModelTransform.pivot(0.0F, 22.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_BLUE_FIN, ModelPartBuilder.create().uv(24, 0).cuboid(-2.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F), ModelTransform.pivot(-2.5F, 17.0F, -1.5F));
      lv2.addChild(EntityModelPartNames.LEFT_BLUE_FIN, ModelPartBuilder.create().uv(24, 3).cuboid(0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F), ModelTransform.pivot(2.5F, 17.0F, -1.5F));
      lv2.addChild("top_front_fin", ModelPartBuilder.create().uv(15, 16).cuboid(-2.5F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F), ModelTransform.of(0.0F, 17.0F, -2.5F, 0.7853982F, 0.0F, 0.0F));
      lv2.addChild("top_back_fin", ModelPartBuilder.create().uv(10, 16).cuboid(-2.5F, -1.0F, -1.0F, 5.0F, 1.0F, 1.0F), ModelTransform.of(0.0F, 17.0F, 2.5F, -0.7853982F, 0.0F, 0.0F));
      lv2.addChild("right_front_fin", ModelPartBuilder.create().uv(8, 16).cuboid(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F), ModelTransform.of(-2.5F, 22.0F, -2.5F, 0.0F, -0.7853982F, 0.0F));
      lv2.addChild("right_back_fin", ModelPartBuilder.create().uv(8, 16).cuboid(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F), ModelTransform.of(-2.5F, 22.0F, 2.5F, 0.0F, 0.7853982F, 0.0F));
      lv2.addChild("left_back_fin", ModelPartBuilder.create().uv(4, 16).cuboid(0.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F), ModelTransform.of(2.5F, 22.0F, 2.5F, 0.0F, -0.7853982F, 0.0F));
      lv2.addChild("left_front_fin", ModelPartBuilder.create().uv(0, 16).cuboid(0.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F), ModelTransform.of(2.5F, 22.0F, -2.5F, 0.0F, 0.7853982F, 0.0F));
      lv2.addChild("bottom_back_fin", ModelPartBuilder.create().uv(8, 22).cuboid(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F), ModelTransform.of(0.5F, 22.0F, 2.5F, 0.7853982F, 0.0F, 0.0F));
      lv2.addChild("bottom_front_fin", ModelPartBuilder.create().uv(17, 21).cuboid(-2.5F, 0.0F, 0.0F, 5.0F, 1.0F, 1.0F), ModelTransform.of(0.0F, 22.0F, -2.5F, -0.7853982F, 0.0F, 0.0F));
      return TexturedModelData.of(lv, 32, 32);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.rightBlueFin.roll = -0.2F + 0.4F * MathHelper.sin(animationProgress * 0.2F);
      this.leftBlueFin.roll = 0.2F - 0.4F * MathHelper.sin(animationProgress * 0.2F);
   }
}
