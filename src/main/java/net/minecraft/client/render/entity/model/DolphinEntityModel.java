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
public class DolphinEntityModel extends SinglePartEntityModel {
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart tail;
   private final ModelPart tailFin;

   public DolphinEntityModel(ModelPart root) {
      this.root = root;
      this.body = root.getChild(EntityModelPartNames.BODY);
      this.tail = this.body.getChild(EntityModelPartNames.TAIL);
      this.tailFin = this.tail.getChild(EntityModelPartNames.TAIL_FIN);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      float f = 18.0F;
      float g = -8.0F;
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(22, 0).cuboid(-4.0F, -7.0F, 0.0F, 8.0F, 7.0F, 13.0F), ModelTransform.pivot(0.0F, 22.0F, -5.0F));
      lv3.addChild(EntityModelPartNames.BACK_FIN, ModelPartBuilder.create().uv(51, 0).cuboid(-0.5F, 0.0F, 8.0F, 1.0F, 4.0F, 5.0F), ModelTransform.rotation(1.0471976F, 0.0F, 0.0F));
      lv3.addChild(EntityModelPartNames.LEFT_FIN, ModelPartBuilder.create().uv(48, 20).mirrored().cuboid(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 7.0F), ModelTransform.of(2.0F, -2.0F, 4.0F, 1.0471976F, 0.0F, 2.0943952F));
      lv3.addChild(EntityModelPartNames.RIGHT_FIN, ModelPartBuilder.create().uv(48, 20).cuboid(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 7.0F), ModelTransform.of(-2.0F, -2.0F, 4.0F, 1.0471976F, 0.0F, -2.0943952F));
      ModelPartData lv4 = lv3.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(0, 19).cuboid(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 11.0F), ModelTransform.of(0.0F, -2.5F, 11.0F, -0.10471976F, 0.0F, 0.0F));
      lv4.addChild(EntityModelPartNames.TAIL_FIN, ModelPartBuilder.create().uv(19, 20).cuboid(-5.0F, -0.5F, 0.0F, 10.0F, 1.0F, 6.0F), ModelTransform.pivot(0.0F, 0.0F, 9.0F));
      ModelPartData lv5 = lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -3.0F, -3.0F, 8.0F, 7.0F, 6.0F), ModelTransform.pivot(0.0F, -4.0F, -3.0F));
      lv5.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(0, 13).cuboid(-1.0F, 2.0F, -7.0F, 2.0F, 2.0F, 4.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 64);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.body.pitch = headPitch * 0.017453292F;
      this.body.yaw = headYaw * 0.017453292F;
      if (entity.getVelocity().horizontalLengthSquared() > 1.0E-7) {
         ModelPart var10000 = this.body;
         var10000.pitch += -0.05F - 0.05F * MathHelper.cos(animationProgress * 0.3F);
         this.tail.pitch = -0.1F * MathHelper.cos(animationProgress * 0.3F);
         this.tailFin.pitch = -0.2F * MathHelper.cos(animationProgress * 0.3F);
      }

   }
}
