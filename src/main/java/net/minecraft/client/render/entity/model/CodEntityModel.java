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
public class CodEntityModel extends SinglePartEntityModel {
   private final ModelPart root;
   private final ModelPart tailFin;

   public CodEntityModel(ModelPart root) {
      this.root = root;
      this.tailFin = root.getChild(EntityModelPartNames.TAIL_FIN);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      int i = true;
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -2.0F, 0.0F, 2.0F, 4.0F, 7.0F), ModelTransform.pivot(0.0F, 22.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(11, 0).cuboid(-1.0F, -2.0F, -3.0F, 2.0F, 4.0F, 3.0F), ModelTransform.pivot(0.0F, 22.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 1.0F), ModelTransform.pivot(0.0F, 22.0F, -3.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FIN, ModelPartBuilder.create().uv(22, 1).cuboid(-2.0F, 0.0F, -1.0F, 2.0F, 0.0F, 2.0F), ModelTransform.of(-1.0F, 23.0F, 0.0F, 0.0F, 0.0F, -0.7853982F));
      lv2.addChild(EntityModelPartNames.LEFT_FIN, ModelPartBuilder.create().uv(22, 4).cuboid(0.0F, 0.0F, -1.0F, 2.0F, 0.0F, 2.0F), ModelTransform.of(1.0F, 23.0F, 0.0F, 0.0F, 0.0F, 0.7853982F));
      lv2.addChild(EntityModelPartNames.TAIL_FIN, ModelPartBuilder.create().uv(22, 3).cuboid(0.0F, -2.0F, 0.0F, 0.0F, 4.0F, 4.0F), ModelTransform.pivot(0.0F, 22.0F, 7.0F));
      lv2.addChild(EntityModelPartNames.TOP_FIN, ModelPartBuilder.create().uv(20, -6).cuboid(0.0F, -1.0F, -1.0F, 0.0F, 1.0F, 6.0F), ModelTransform.pivot(0.0F, 20.0F, 0.0F));
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

      this.tailFin.yaw = -k * 0.45F * MathHelper.sin(0.6F * animationProgress);
   }
}
