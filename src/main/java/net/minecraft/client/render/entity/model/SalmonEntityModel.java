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
public class SalmonEntityModel extends SinglePartEntityModel {
   private static final String BODY_FRONT = "body_front";
   private static final String BODY_BACK = "body_back";
   private final ModelPart root;
   private final ModelPart tail;

   public SalmonEntityModel(ModelPart root) {
      this.root = root;
      this.tail = root.getChild("body_back");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      int i = true;
      ModelPartData lv3 = lv2.addChild("body_front", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 8.0F), ModelTransform.pivot(0.0F, 20.0F, 0.0F));
      ModelPartData lv4 = lv2.addChild("body_back", ModelPartBuilder.create().uv(0, 13).cuboid(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 8.0F), ModelTransform.pivot(0.0F, 20.0F, 8.0F));
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(22, 0).cuboid(-1.0F, -2.0F, -3.0F, 2.0F, 4.0F, 3.0F), ModelTransform.pivot(0.0F, 20.0F, 0.0F));
      lv4.addChild(EntityModelPartNames.BACK_FIN, ModelPartBuilder.create().uv(20, 10).cuboid(0.0F, -2.5F, 0.0F, 0.0F, 5.0F, 6.0F), ModelTransform.pivot(0.0F, 0.0F, 8.0F));
      lv3.addChild("top_front_fin", ModelPartBuilder.create().uv(2, 1).cuboid(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 3.0F), ModelTransform.pivot(0.0F, -4.5F, 5.0F));
      lv4.addChild("top_back_fin", ModelPartBuilder.create().uv(0, 2).cuboid(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 4.0F), ModelTransform.pivot(0.0F, -4.5F, -1.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FIN, ModelPartBuilder.create().uv(-4, 0).cuboid(-2.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F), ModelTransform.of(-1.5F, 21.5F, 0.0F, 0.0F, 0.0F, -0.7853982F));
      lv2.addChild(EntityModelPartNames.LEFT_FIN, ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F), ModelTransform.of(1.5F, 21.5F, 0.0F, 0.0F, 0.0F, 0.7853982F));
      return TexturedModelData.of(lv, 32, 32);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      float k = 1.0F;
      float l = 1.0F;
      if (!entity.isTouchingWater()) {
         k = 1.3F;
         l = 1.7F;
      }

      this.tail.yaw = -k * 0.25F * MathHelper.sin(l * 0.6F * animationProgress);
   }
}
