package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class VillagerResemblingModel extends SinglePartEntityModel implements ModelWithHead, ModelWithHat {
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart hat;
   private final ModelPart hatRim;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;
   protected final ModelPart nose;

   public VillagerResemblingModel(ModelPart root) {
      this.root = root;
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.hat = this.head.getChild(EntityModelPartNames.HAT);
      this.hatRim = this.hat.getChild(EntityModelPartNames.HAT_RIM);
      this.nose = this.head.getChild(EntityModelPartNames.NOSE);
      this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
      this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
   }

   public static ModelData getModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      float f = 0.5F;
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), ModelTransform.NONE);
      ModelPartData lv4 = lv3.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new Dilation(0.51F)), ModelTransform.NONE);
      lv4.addChild(EntityModelPartNames.HAT_RIM, ModelPartBuilder.create().uv(30, 47).cuboid(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F), ModelTransform.rotation(-1.5707964F, 0.0F, 0.0F));
      lv3.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(24, 0).cuboid(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F), ModelTransform.pivot(0.0F, -2.0F, 0.0F));
      ModelPartData lv5 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 20).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F), ModelTransform.NONE);
      lv5.addChild(EntityModelPartNames.JACKET, ModelPartBuilder.create().uv(0, 38).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 20.0F, 6.0F, new Dilation(0.5F)), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.ARMS, ModelPartBuilder.create().uv(44, 22).cuboid(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F).uv(44, 22).cuboid(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, true).uv(40, 38).cuboid(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F), ModelTransform.of(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 22).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), ModelTransform.pivot(-2.0F, 12.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 22).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), ModelTransform.pivot(2.0F, 12.0F, 0.0F));
      return lv;
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      boolean bl = false;
      if (entity instanceof MerchantEntity) {
         bl = ((MerchantEntity)entity).getHeadRollingTimeLeft() > 0;
      }

      this.head.yaw = headYaw * 0.017453292F;
      this.head.pitch = headPitch * 0.017453292F;
      if (bl) {
         this.head.roll = 0.3F * MathHelper.sin(0.45F * animationProgress);
         this.head.pitch = 0.4F;
      } else {
         this.head.roll = 0.0F;
      }

      this.rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance * 0.5F;
      this.leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance * 0.5F;
      this.rightLeg.yaw = 0.0F;
      this.leftLeg.yaw = 0.0F;
   }

   public ModelPart getHead() {
      return this.head;
   }

   public void setHatVisible(boolean visible) {
      this.head.visible = visible;
      this.hat.visible = visible;
      this.hatRim.visible = visible;
   }
}
