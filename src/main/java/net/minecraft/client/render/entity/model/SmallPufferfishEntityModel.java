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
public class SmallPufferfishEntityModel extends SinglePartEntityModel {
   private final ModelPart root;
   private final ModelPart leftFin;
   private final ModelPart rightFin;

   public SmallPufferfishEntityModel(ModelPart root) {
      this.root = root;
      this.leftFin = root.getChild(EntityModelPartNames.LEFT_FIN);
      this.rightFin = root.getChild(EntityModelPartNames.RIGHT_FIN);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      int i = true;
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 27).cuboid(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F), ModelTransform.pivot(0.0F, 23.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_EYE, ModelPartBuilder.create().uv(24, 6).cuboid(-1.5F, 0.0F, -1.5F, 1.0F, 1.0F, 1.0F), ModelTransform.pivot(0.0F, 20.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_EYE, ModelPartBuilder.create().uv(28, 6).cuboid(0.5F, 0.0F, -1.5F, 1.0F, 1.0F, 1.0F), ModelTransform.pivot(0.0F, 20.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.BACK_FIN, ModelPartBuilder.create().uv(-3, 0).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F), ModelTransform.pivot(0.0F, 22.0F, 1.5F));
      lv2.addChild(EntityModelPartNames.RIGHT_FIN, ModelPartBuilder.create().uv(25, 0).cuboid(-1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 2.0F), ModelTransform.pivot(-1.5F, 22.0F, -1.5F));
      lv2.addChild(EntityModelPartNames.LEFT_FIN, ModelPartBuilder.create().uv(25, 0).cuboid(0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 2.0F), ModelTransform.pivot(1.5F, 22.0F, -1.5F));
      return TexturedModelData.of(lv, 32, 32);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.rightFin.roll = -0.2F + 0.4F * MathHelper.sin(animationProgress * 0.2F);
      this.leftFin.roll = 0.2F - 0.4F * MathHelper.sin(animationProgress * 0.2F);
   }
}
