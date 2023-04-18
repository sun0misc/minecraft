package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
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
public class ChickenEntityModel extends AnimalModel {
   public static final String RED_THING = "red_thing";
   private final ModelPart head;
   private final ModelPart body;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;
   private final ModelPart rightWing;
   private final ModelPart leftWing;
   private final ModelPart beak;
   private final ModelPart wattle;

   public ChickenEntityModel(ModelPart root) {
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.beak = root.getChild(EntityModelPartNames.BEAK);
      this.wattle = root.getChild("red_thing");
      this.body = root.getChild(EntityModelPartNames.BODY);
      this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
      this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
      this.rightWing = root.getChild(EntityModelPartNames.RIGHT_WING);
      this.leftWing = root.getChild(EntityModelPartNames.LEFT_WING);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      int i = true;
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F), ModelTransform.pivot(0.0F, 15.0F, -4.0F));
      lv2.addChild(EntityModelPartNames.BEAK, ModelPartBuilder.create().uv(14, 0).cuboid(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F), ModelTransform.pivot(0.0F, 15.0F, -4.0F));
      lv2.addChild("red_thing", ModelPartBuilder.create().uv(14, 4).cuboid(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F), ModelTransform.pivot(0.0F, 15.0F, -4.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 9).cuboid(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F), ModelTransform.of(0.0F, 16.0F, 0.0F, 1.5707964F, 0.0F, 0.0F));
      ModelPartBuilder lv3 = ModelPartBuilder.create().uv(26, 0).cuboid(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F);
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, lv3, ModelTransform.pivot(-2.0F, 19.0F, 1.0F));
      lv2.addChild(EntityModelPartNames.LEFT_LEG, lv3, ModelTransform.pivot(1.0F, 19.0F, 1.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_WING, ModelPartBuilder.create().uv(24, 13).cuboid(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), ModelTransform.pivot(-4.0F, 13.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_WING, ModelPartBuilder.create().uv(24, 13).cuboid(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), ModelTransform.pivot(4.0F, 13.0F, 0.0F));
      return TexturedModelData.of(lv, 64, 32);
   }

   protected Iterable getHeadParts() {
      return ImmutableList.of(this.head, this.beak, this.wattle);
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.body, this.rightLeg, this.leftLeg, this.rightWing, this.leftWing);
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.head.pitch = headPitch * 0.017453292F;
      this.head.yaw = headYaw * 0.017453292F;
      this.beak.pitch = this.head.pitch;
      this.beak.yaw = this.head.yaw;
      this.wattle.pitch = this.head.pitch;
      this.wattle.yaw = this.head.yaw;
      this.rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
      this.leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance;
      this.rightWing.roll = animationProgress;
      this.leftWing.roll = -animationProgress;
   }
}
