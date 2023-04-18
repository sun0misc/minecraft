package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BipedEntityModel extends AnimalModel implements ModelWithArms, ModelWithHead {
   public static final float field_32505 = 0.25F;
   public static final float field_32506 = 0.5F;
   public static final float field_42513 = -0.1F;
   private static final float field_42512 = 0.005F;
   private static final float SPYGLASS_ARM_YAW_OFFSET = 0.2617994F;
   private static final float SPYGLASS_ARM_PITCH_OFFSET = 1.9198622F;
   private static final float SPYGLASS_SNEAKING_ARM_PITCH_OFFSET = 0.2617994F;
   public static final float field_39069 = 1.4835298F;
   public static final float field_39070 = 0.5235988F;
   public final ModelPart head;
   public final ModelPart hat;
   public final ModelPart body;
   public final ModelPart rightArm;
   public final ModelPart leftArm;
   public final ModelPart rightLeg;
   public final ModelPart leftLeg;
   public ArmPose leftArmPose;
   public ArmPose rightArmPose;
   public boolean sneaking;
   public float leaningPitch;

   public BipedEntityModel(ModelPart root) {
      this(root, RenderLayer::getEntityCutoutNoCull);
   }

   public BipedEntityModel(ModelPart root, Function renderLayerFactory) {
      super(renderLayerFactory, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
      this.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
      this.rightArmPose = BipedEntityModel.ArmPose.EMPTY;
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.hat = root.getChild(EntityModelPartNames.HAT);
      this.body = root.getChild(EntityModelPartNames.BODY);
      this.rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
      this.leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
      this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
      this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
   }

   public static ModelData getModelData(Dilation dilation, float pivotOffsetY) {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
      lv2.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation.add(0.5F)), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(-5.0F, 2.0F + pivotOffsetY, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(5.0F, 2.0F + pivotOffsetY, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(-1.9F, 12.0F + pivotOffsetY, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(1.9F, 12.0F + pivotOffsetY, 0.0F));
      return lv;
   }

   protected Iterable getHeadParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
   }

   public void animateModel(LivingEntity arg, float f, float g, float h) {
      this.leaningPitch = arg.getLeaningPitch(h);
      super.animateModel(arg, f, g, h);
   }

   public void setAngles(LivingEntity arg, float f, float g, float h, float i, float j) {
      boolean bl = arg.getRoll() > 4;
      boolean bl2 = arg.isInSwimmingPose();
      this.head.yaw = i * 0.017453292F;
      if (bl) {
         this.head.pitch = -0.7853982F;
      } else if (this.leaningPitch > 0.0F) {
         if (bl2) {
            this.head.pitch = this.lerpAngle(this.leaningPitch, this.head.pitch, -0.7853982F);
         } else {
            this.head.pitch = this.lerpAngle(this.leaningPitch, this.head.pitch, j * 0.017453292F);
         }
      } else {
         this.head.pitch = j * 0.017453292F;
      }

      this.body.yaw = 0.0F;
      this.rightArm.pivotZ = 0.0F;
      this.rightArm.pivotX = -5.0F;
      this.leftArm.pivotZ = 0.0F;
      this.leftArm.pivotX = 5.0F;
      float k = 1.0F;
      if (bl) {
         k = (float)arg.getVelocity().lengthSquared();
         k /= 0.2F;
         k *= k * k;
      }

      if (k < 1.0F) {
         k = 1.0F;
      }

      this.rightArm.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 2.0F * g * 0.5F / k;
      this.leftArm.pitch = MathHelper.cos(f * 0.6662F) * 2.0F * g * 0.5F / k;
      this.rightArm.roll = 0.0F;
      this.leftArm.roll = 0.0F;
      this.rightLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g / k;
      this.leftLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g / k;
      this.rightLeg.yaw = 0.005F;
      this.leftLeg.yaw = -0.005F;
      this.rightLeg.roll = 0.005F;
      this.leftLeg.roll = -0.005F;
      ModelPart var10000;
      if (this.riding) {
         var10000 = this.rightArm;
         var10000.pitch += -0.62831855F;
         var10000 = this.leftArm;
         var10000.pitch += -0.62831855F;
         this.rightLeg.pitch = -1.4137167F;
         this.rightLeg.yaw = 0.31415927F;
         this.rightLeg.roll = 0.07853982F;
         this.leftLeg.pitch = -1.4137167F;
         this.leftLeg.yaw = -0.31415927F;
         this.leftLeg.roll = -0.07853982F;
      }

      this.rightArm.yaw = 0.0F;
      this.leftArm.yaw = 0.0F;
      boolean bl3 = arg.getMainArm() == Arm.RIGHT;
      boolean bl4;
      if (arg.isUsingItem()) {
         bl4 = arg.getActiveHand() == Hand.MAIN_HAND;
         if (bl4 == bl3) {
            this.positionRightArm(arg);
         } else {
            this.positionLeftArm(arg);
         }
      } else {
         bl4 = bl3 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
         if (bl3 != bl4) {
            this.positionLeftArm(arg);
            this.positionRightArm(arg);
         } else {
            this.positionRightArm(arg);
            this.positionLeftArm(arg);
         }
      }

      this.animateArms(arg, h);
      if (this.sneaking) {
         this.body.pitch = 0.5F;
         var10000 = this.rightArm;
         var10000.pitch += 0.4F;
         var10000 = this.leftArm;
         var10000.pitch += 0.4F;
         this.rightLeg.pivotZ = 4.0F;
         this.leftLeg.pivotZ = 4.0F;
         this.rightLeg.pivotY = 12.2F;
         this.leftLeg.pivotY = 12.2F;
         this.head.pivotY = 4.2F;
         this.body.pivotY = 3.2F;
         this.leftArm.pivotY = 5.2F;
         this.rightArm.pivotY = 5.2F;
      } else {
         this.body.pitch = 0.0F;
         this.rightLeg.pivotZ = 0.0F;
         this.leftLeg.pivotZ = 0.0F;
         this.rightLeg.pivotY = 12.0F;
         this.leftLeg.pivotY = 12.0F;
         this.head.pivotY = 0.0F;
         this.body.pivotY = 0.0F;
         this.leftArm.pivotY = 2.0F;
         this.rightArm.pivotY = 2.0F;
      }

      if (this.rightArmPose != BipedEntityModel.ArmPose.SPYGLASS) {
         CrossbowPosing.swingArm(this.rightArm, h, 1.0F);
      }

      if (this.leftArmPose != BipedEntityModel.ArmPose.SPYGLASS) {
         CrossbowPosing.swingArm(this.leftArm, h, -1.0F);
      }

      if (this.leaningPitch > 0.0F) {
         float l = f % 26.0F;
         Arm lv = this.getPreferredArm(arg);
         float m = lv == Arm.RIGHT && this.handSwingProgress > 0.0F ? 0.0F : this.leaningPitch;
         float n = lv == Arm.LEFT && this.handSwingProgress > 0.0F ? 0.0F : this.leaningPitch;
         float o;
         if (!arg.isUsingItem()) {
            if (l < 14.0F) {
               this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, 0.0F);
               this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, 0.0F);
               this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, 3.1415927F);
               this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, 3.1415927F);
               this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, 3.1415927F + 1.8707964F * this.method_2807(l) / this.method_2807(14.0F));
               this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, 3.1415927F - 1.8707964F * this.method_2807(l) / this.method_2807(14.0F));
            } else if (l >= 14.0F && l < 22.0F) {
               o = (l - 14.0F) / 8.0F;
               this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, 1.5707964F * o);
               this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, 1.5707964F * o);
               this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, 3.1415927F);
               this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, 3.1415927F);
               this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, 5.012389F - 1.8707964F * o);
               this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, 1.2707963F + 1.8707964F * o);
            } else if (l >= 22.0F && l < 26.0F) {
               o = (l - 22.0F) / 4.0F;
               this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, 1.5707964F - 1.5707964F * o);
               this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, 1.5707964F - 1.5707964F * o);
               this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, 3.1415927F);
               this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, 3.1415927F);
               this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, 3.1415927F);
               this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, 3.1415927F);
            }
         }

         o = 0.3F;
         float p = 0.33333334F;
         this.leftLeg.pitch = MathHelper.lerp(this.leaningPitch, this.leftLeg.pitch, 0.3F * MathHelper.cos(f * 0.33333334F + 3.1415927F));
         this.rightLeg.pitch = MathHelper.lerp(this.leaningPitch, this.rightLeg.pitch, 0.3F * MathHelper.cos(f * 0.33333334F));
      }

      this.hat.copyTransform(this.head);
   }

   private void positionRightArm(LivingEntity entity) {
      switch (this.rightArmPose) {
         case EMPTY:
            this.rightArm.yaw = 0.0F;
            break;
         case BLOCK:
            this.rightArm.pitch = this.rightArm.pitch * 0.5F - 0.9424779F;
            this.rightArm.yaw = -0.5235988F;
            break;
         case ITEM:
            this.rightArm.pitch = this.rightArm.pitch * 0.5F - 0.31415927F;
            this.rightArm.yaw = 0.0F;
            break;
         case THROW_SPEAR:
            this.rightArm.pitch = this.rightArm.pitch * 0.5F - 3.1415927F;
            this.rightArm.yaw = 0.0F;
            break;
         case BOW_AND_ARROW:
            this.rightArm.yaw = -0.1F + this.head.yaw;
            this.leftArm.yaw = 0.1F + this.head.yaw + 0.4F;
            this.rightArm.pitch = -1.5707964F + this.head.pitch;
            this.leftArm.pitch = -1.5707964F + this.head.pitch;
            break;
         case CROSSBOW_CHARGE:
            CrossbowPosing.charge(this.rightArm, this.leftArm, entity, true);
            break;
         case CROSSBOW_HOLD:
            CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, true);
            break;
         case BRUSH:
            this.rightArm.pitch = this.rightArm.pitch * 0.5F - 0.62831855F;
            this.rightArm.yaw = 0.0F;
            break;
         case SPYGLASS:
            this.rightArm.pitch = MathHelper.clamp(this.head.pitch - 1.9198622F - (entity.isInSneakingPose() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            this.rightArm.yaw = this.head.yaw - 0.2617994F;
            break;
         case TOOT_HORN:
            this.rightArm.pitch = MathHelper.clamp(this.head.pitch, -1.2F, 1.2F) - 1.4835298F;
            this.rightArm.yaw = this.head.yaw - 0.5235988F;
      }

   }

   private void positionLeftArm(LivingEntity entity) {
      switch (this.leftArmPose) {
         case EMPTY:
            this.leftArm.yaw = 0.0F;
            break;
         case BLOCK:
            this.leftArm.pitch = this.leftArm.pitch * 0.5F - 0.9424779F;
            this.leftArm.yaw = 0.5235988F;
            break;
         case ITEM:
            this.leftArm.pitch = this.leftArm.pitch * 0.5F - 0.31415927F;
            this.leftArm.yaw = 0.0F;
            break;
         case THROW_SPEAR:
            this.leftArm.pitch = this.leftArm.pitch * 0.5F - 3.1415927F;
            this.leftArm.yaw = 0.0F;
            break;
         case BOW_AND_ARROW:
            this.rightArm.yaw = -0.1F + this.head.yaw - 0.4F;
            this.leftArm.yaw = 0.1F + this.head.yaw;
            this.rightArm.pitch = -1.5707964F + this.head.pitch;
            this.leftArm.pitch = -1.5707964F + this.head.pitch;
            break;
         case CROSSBOW_CHARGE:
            CrossbowPosing.charge(this.rightArm, this.leftArm, entity, false);
            break;
         case CROSSBOW_HOLD:
            CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, false);
            break;
         case BRUSH:
            this.leftArm.pitch = this.leftArm.pitch * 0.5F - 0.62831855F;
            this.leftArm.yaw = 0.0F;
            break;
         case SPYGLASS:
            this.leftArm.pitch = MathHelper.clamp(this.head.pitch - 1.9198622F - (entity.isInSneakingPose() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            this.leftArm.yaw = this.head.yaw + 0.2617994F;
            break;
         case TOOT_HORN:
            this.leftArm.pitch = MathHelper.clamp(this.head.pitch, -1.2F, 1.2F) - 1.4835298F;
            this.leftArm.yaw = this.head.yaw + 0.5235988F;
      }

   }

   protected void animateArms(LivingEntity entity, float animationProgress) {
      if (!(this.handSwingProgress <= 0.0F)) {
         Arm lv = this.getPreferredArm(entity);
         ModelPart lv2 = this.getArm(lv);
         float g = this.handSwingProgress;
         this.body.yaw = MathHelper.sin(MathHelper.sqrt(g) * 6.2831855F) * 0.2F;
         ModelPart var10000;
         if (lv == Arm.LEFT) {
            var10000 = this.body;
            var10000.yaw *= -1.0F;
         }

         this.rightArm.pivotZ = MathHelper.sin(this.body.yaw) * 5.0F;
         this.rightArm.pivotX = -MathHelper.cos(this.body.yaw) * 5.0F;
         this.leftArm.pivotZ = -MathHelper.sin(this.body.yaw) * 5.0F;
         this.leftArm.pivotX = MathHelper.cos(this.body.yaw) * 5.0F;
         var10000 = this.rightArm;
         var10000.yaw += this.body.yaw;
         var10000 = this.leftArm;
         var10000.yaw += this.body.yaw;
         var10000 = this.leftArm;
         var10000.pitch += this.body.yaw;
         g = 1.0F - this.handSwingProgress;
         g *= g;
         g *= g;
         g = 1.0F - g;
         float h = MathHelper.sin(g * 3.1415927F);
         float i = MathHelper.sin(this.handSwingProgress * 3.1415927F) * -(this.head.pitch - 0.7F) * 0.75F;
         lv2.pitch -= h * 1.2F + i;
         lv2.yaw += this.body.yaw * 2.0F;
         lv2.roll += MathHelper.sin(this.handSwingProgress * 3.1415927F) * -0.4F;
      }
   }

   protected float lerpAngle(float angleOne, float angleTwo, float magnitude) {
      float i = (magnitude - angleTwo) % 6.2831855F;
      if (i < -3.1415927F) {
         i += 6.2831855F;
      }

      if (i >= 3.1415927F) {
         i -= 6.2831855F;
      }

      return angleTwo + angleOne * i;
   }

   private float method_2807(float f) {
      return -65.0F * f + f * f;
   }

   public void copyBipedStateTo(BipedEntityModel model) {
      super.copyStateTo(model);
      model.leftArmPose = this.leftArmPose;
      model.rightArmPose = this.rightArmPose;
      model.sneaking = this.sneaking;
      model.head.copyTransform(this.head);
      model.hat.copyTransform(this.hat);
      model.body.copyTransform(this.body);
      model.rightArm.copyTransform(this.rightArm);
      model.leftArm.copyTransform(this.leftArm);
      model.rightLeg.copyTransform(this.rightLeg);
      model.leftLeg.copyTransform(this.leftLeg);
   }

   public void setVisible(boolean visible) {
      this.head.visible = visible;
      this.hat.visible = visible;
      this.body.visible = visible;
      this.rightArm.visible = visible;
      this.leftArm.visible = visible;
      this.rightLeg.visible = visible;
      this.leftLeg.visible = visible;
   }

   public void setArmAngle(Arm arm, MatrixStack matrices) {
      this.getArm(arm).rotate(matrices);
   }

   protected ModelPart getArm(Arm arm) {
      return arm == Arm.LEFT ? this.leftArm : this.rightArm;
   }

   public ModelPart getHead() {
      return this.head;
   }

   private Arm getPreferredArm(LivingEntity entity) {
      Arm lv = entity.getMainArm();
      return entity.preferredHand == Hand.MAIN_HAND ? lv : lv.getOpposite();
   }

   @Environment(EnvType.CLIENT)
   public static enum ArmPose {
      EMPTY(false),
      ITEM(false),
      BLOCK(false),
      BOW_AND_ARROW(true),
      THROW_SPEAR(false),
      CROSSBOW_CHARGE(true),
      CROSSBOW_HOLD(true),
      SPYGLASS(false),
      TOOT_HORN(false),
      BRUSH(false);

      private final boolean twoHanded;

      private ArmPose(boolean twoHanded) {
         this.twoHanded = twoHanded;
      }

      public boolean isTwoHanded() {
         return this.twoHanded;
      }

      // $FF: synthetic method
      private static ArmPose[] method_36892() {
         return new ArmPose[]{EMPTY, ITEM, BLOCK, BOW_AND_ARROW, THROW_SPEAR, CROSSBOW_CHARGE, CROSSBOW_HOLD, SPYGLASS, TOOT_HORN, BRUSH};
      }
   }
}
