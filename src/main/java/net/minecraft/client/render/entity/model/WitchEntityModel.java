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
public class WitchEntityModel extends VillagerResemblingModel {
   private boolean liftingNose;

   public WitchEntityModel(ModelPart arg) {
      super(arg);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = VillagerResemblingModel.getModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), ModelTransform.NONE);
      ModelPartData lv4 = lv3.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(0, 64).cuboid(0.0F, 0.0F, 0.0F, 10.0F, 2.0F, 10.0F), ModelTransform.pivot(-5.0F, -10.03125F, -5.0F));
      ModelPartData lv5 = lv4.addChild("hat2", ModelPartBuilder.create().uv(0, 76).cuboid(0.0F, 0.0F, 0.0F, 7.0F, 4.0F, 7.0F), ModelTransform.of(1.75F, -4.0F, 2.0F, -0.05235988F, 0.0F, 0.02617994F));
      ModelPartData lv6 = lv5.addChild("hat3", ModelPartBuilder.create().uv(0, 87).cuboid(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F), ModelTransform.of(1.75F, -4.0F, 2.0F, -0.10471976F, 0.0F, 0.05235988F));
      lv6.addChild("hat4", ModelPartBuilder.create().uv(0, 95).cuboid(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.25F)), ModelTransform.of(1.75F, -2.0F, 2.0F, -0.20943952F, 0.0F, 0.10471976F));
      ModelPartData lv7 = lv3.getChild(EntityModelPartNames.NOSE);
      lv7.addChild("mole", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, 3.0F, -6.75F, 1.0F, 1.0F, 1.0F, new Dilation(-0.25F)), ModelTransform.pivot(0.0F, -2.0F, 0.0F));
      return TexturedModelData.of(lv, 64, 128);
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      this.nose.setPivot(0.0F, -2.0F, 0.0F);
      float k = 0.01F * (float)(entity.getId() % 10);
      this.nose.pitch = MathHelper.sin((float)entity.age * k) * 4.5F * 0.017453292F;
      this.nose.yaw = 0.0F;
      this.nose.roll = MathHelper.cos((float)entity.age * k) * 2.5F * 0.017453292F;
      if (this.liftingNose) {
         this.nose.setPivot(0.0F, 1.0F, -1.5F);
         this.nose.pitch = -0.9F;
      }

   }

   public ModelPart getNose() {
      return this.nose;
   }

   public void setLiftingNose(boolean liftingNose) {
      this.liftingNose = liftingNose;
   }
}
