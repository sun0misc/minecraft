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
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SmallTropicalFishEntityModel extends TintableCompositeModel {
   private final ModelPart root;
   private final ModelPart tail;

   public SmallTropicalFishEntityModel(ModelPart root) {
      this.root = root;
      this.tail = root.getChild(EntityModelPartNames.TAIL);
   }

   public static TexturedModelData getTexturedModelData(Dilation dilation) {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      int i = true;
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -1.5F, -3.0F, 2.0F, 3.0F, 6.0F, dilation), ModelTransform.pivot(0.0F, 22.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(22, -6).cuboid(0.0F, -1.5F, 0.0F, 0.0F, 3.0F, 6.0F, dilation), ModelTransform.pivot(0.0F, 22.0F, 3.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FIN, ModelPartBuilder.create().uv(2, 16).cuboid(-2.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, dilation), ModelTransform.of(-1.0F, 22.5F, 0.0F, 0.0F, 0.7853982F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_FIN, ModelPartBuilder.create().uv(2, 12).cuboid(0.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, dilation), ModelTransform.of(1.0F, 22.5F, 0.0F, 0.0F, -0.7853982F, 0.0F));
      lv2.addChild(EntityModelPartNames.TOP_FIN, ModelPartBuilder.create().uv(10, -5).cuboid(0.0F, -3.0F, 0.0F, 0.0F, 3.0F, 6.0F, dilation), ModelTransform.pivot(0.0F, 20.5F, -3.0F));
      return TexturedModelData.of(lv, 32, 32);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      float k = 1.0F;
      if (!entity.isTouchingWater()) {
         k = 1.5F;
      }

      this.tail.yaw = -k * 0.45F * MathHelper.sin(0.6F * animationProgress);
   }
}
