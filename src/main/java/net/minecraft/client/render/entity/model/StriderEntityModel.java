package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class StriderEntityModel extends SinglePartEntityModel {
   private static final String RIGHT_BOTTOM_BRISTLE = "right_bottom_bristle";
   private static final String RIGHT_MIDDLE_BRISTLE = "right_middle_bristle";
   private static final String RIGHT_TOP_BRISTLE = "right_top_bristle";
   private static final String LEFT_TOP_BRISTLE = "left_top_bristle";
   private static final String LEFT_MIDDLE_BRISTLE = "left_middle_bristle";
   private static final String LEFT_BOTTOM_BRISTLE = "left_bottom_bristle";
   private final ModelPart root;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;
   private final ModelPart body;
   private final ModelPart rightBottomBristle;
   private final ModelPart rightMiddleBristle;
   private final ModelPart rightTopBristle;
   private final ModelPart leftTopBristle;
   private final ModelPart leftMiddleBristle;
   private final ModelPart leftBottomBristle;

   public StriderEntityModel(ModelPart root) {
      this.root = root;
      this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
      this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
      this.body = root.getChild(EntityModelPartNames.BODY);
      this.rightBottomBristle = this.body.getChild("right_bottom_bristle");
      this.rightMiddleBristle = this.body.getChild("right_middle_bristle");
      this.rightTopBristle = this.body.getChild("right_top_bristle");
      this.leftTopBristle = this.body.getChild("left_top_bristle");
      this.leftMiddleBristle = this.body.getChild("left_middle_bristle");
      this.leftBottomBristle = this.body.getChild("left_bottom_bristle");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 32).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F), ModelTransform.pivot(-4.0F, 8.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 55).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F), ModelTransform.pivot(4.0F, 8.0F, 0.0F));
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -6.0F, -8.0F, 16.0F, 14.0F, 16.0F), ModelTransform.pivot(0.0F, 1.0F, 0.0F));
      lv3.addChild("right_bottom_bristle", ModelPartBuilder.create().uv(16, 65).cuboid(-12.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, true), ModelTransform.of(-8.0F, 4.0F, -8.0F, 0.0F, 0.0F, -1.2217305F));
      lv3.addChild("right_middle_bristle", ModelPartBuilder.create().uv(16, 49).cuboid(-12.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, true), ModelTransform.of(-8.0F, -1.0F, -8.0F, 0.0F, 0.0F, -1.134464F));
      lv3.addChild("right_top_bristle", ModelPartBuilder.create().uv(16, 33).cuboid(-12.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, true), ModelTransform.of(-8.0F, -5.0F, -8.0F, 0.0F, 0.0F, -0.87266463F));
      lv3.addChild("left_top_bristle", ModelPartBuilder.create().uv(16, 33).cuboid(0.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F), ModelTransform.of(8.0F, -6.0F, -8.0F, 0.0F, 0.0F, 0.87266463F));
      lv3.addChild("left_middle_bristle", ModelPartBuilder.create().uv(16, 49).cuboid(0.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F), ModelTransform.of(8.0F, -2.0F, -8.0F, 0.0F, 0.0F, 1.134464F));
      lv3.addChild("left_bottom_bristle", ModelPartBuilder.create().uv(16, 65).cuboid(0.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F), ModelTransform.of(8.0F, 3.0F, -8.0F, 0.0F, 0.0F, 1.2217305F));
      return TexturedModelData.of(lv, 64, 128);
   }

   public void setAngles(StriderEntity arg, float f, float g, float h, float i, float j) {
      g = Math.min(0.25F, g);
      if (!arg.hasPassengers()) {
         this.body.pitch = j * 0.017453292F;
         this.body.yaw = i * 0.017453292F;
      } else {
         this.body.pitch = 0.0F;
         this.body.yaw = 0.0F;
      }

      float k = 1.5F;
      this.body.roll = 0.1F * MathHelper.sin(f * 1.5F) * 4.0F * g;
      this.body.pivotY = 2.0F;
      ModelPart var10000 = this.body;
      var10000.pivotY -= 2.0F * MathHelper.cos(f * 1.5F) * 2.0F * g;
      this.leftLeg.pitch = MathHelper.sin(f * 1.5F * 0.5F) * 2.0F * g;
      this.rightLeg.pitch = MathHelper.sin(f * 1.5F * 0.5F + 3.1415927F) * 2.0F * g;
      this.leftLeg.roll = 0.17453292F * MathHelper.cos(f * 1.5F * 0.5F) * g;
      this.rightLeg.roll = 0.17453292F * MathHelper.cos(f * 1.5F * 0.5F + 3.1415927F) * g;
      this.leftLeg.pivotY = 8.0F + 2.0F * MathHelper.sin(f * 1.5F * 0.5F + 3.1415927F) * 2.0F * g;
      this.rightLeg.pivotY = 8.0F + 2.0F * MathHelper.sin(f * 1.5F * 0.5F) * 2.0F * g;
      this.rightBottomBristle.roll = -1.2217305F;
      this.rightMiddleBristle.roll = -1.134464F;
      this.rightTopBristle.roll = -0.87266463F;
      this.leftTopBristle.roll = 0.87266463F;
      this.leftMiddleBristle.roll = 1.134464F;
      this.leftBottomBristle.roll = 1.2217305F;
      float l = MathHelper.cos(f * 1.5F + 3.1415927F) * g;
      var10000 = this.rightBottomBristle;
      var10000.roll += l * 1.3F;
      var10000 = this.rightMiddleBristle;
      var10000.roll += l * 1.2F;
      var10000 = this.rightTopBristle;
      var10000.roll += l * 0.6F;
      var10000 = this.leftTopBristle;
      var10000.roll += l * 0.6F;
      var10000 = this.leftMiddleBristle;
      var10000.roll += l * 1.2F;
      var10000 = this.leftBottomBristle;
      var10000.roll += l * 1.3F;
      float m = 1.0F;
      float n = 1.0F;
      var10000 = this.rightBottomBristle;
      var10000.roll += 0.05F * MathHelper.sin(h * 1.0F * -0.4F);
      var10000 = this.rightMiddleBristle;
      var10000.roll += 0.1F * MathHelper.sin(h * 1.0F * 0.2F);
      var10000 = this.rightTopBristle;
      var10000.roll += 0.1F * MathHelper.sin(h * 1.0F * 0.4F);
      var10000 = this.leftTopBristle;
      var10000.roll += 0.1F * MathHelper.sin(h * 1.0F * 0.4F);
      var10000 = this.leftMiddleBristle;
      var10000.roll += 0.1F * MathHelper.sin(h * 1.0F * 0.2F);
      var10000 = this.leftBottomBristle;
      var10000.roll += 0.05F * MathHelper.sin(h * 1.0F * -0.4F);
   }

   public ModelPart getPart() {
      return this.root;
   }
}
