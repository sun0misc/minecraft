package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class LlamaEntityModel extends EntityModel {
   private final ModelPart head;
   private final ModelPart body;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightChest;
   private final ModelPart leftChest;

   public LlamaEntityModel(ModelPart root) {
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.body = root.getChild(EntityModelPartNames.BODY);
      this.rightChest = root.getChild(EntityModelPartNames.RIGHT_CHEST);
      this.leftChest = root.getChild(EntityModelPartNames.LEFT_CHEST);
      this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
      this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
      this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
      this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
   }

   public static TexturedModelData getTexturedModelData(Dilation dilation) {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -14.0F, -10.0F, 4.0F, 4.0F, 9.0F, dilation).uv(0, 14).cuboid(EntityModelPartNames.NECK, -4.0F, -16.0F, -6.0F, 8.0F, 18.0F, 6.0F, dilation).uv(17, 0).cuboid("ear", -4.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, dilation).uv(17, 0).cuboid("ear", 1.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, dilation), ModelTransform.pivot(0.0F, 7.0F, -6.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(29, 0).cuboid(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, dilation), ModelTransform.of(0.0F, 5.0F, 2.0F, 1.5707964F, 0.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_CHEST, ModelPartBuilder.create().uv(45, 28).cuboid(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, dilation), ModelTransform.of(-8.5F, 3.0F, 3.0F, 0.0F, 1.5707964F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_CHEST, ModelPartBuilder.create().uv(45, 41).cuboid(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, dilation), ModelTransform.of(5.5F, 3.0F, 3.0F, 0.0F, 1.5707964F, 0.0F));
      int i = true;
      int j = true;
      ModelPartBuilder lv3 = ModelPartBuilder.create().uv(29, 29).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, dilation);
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-3.5F, 10.0F, 6.0F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv3, ModelTransform.pivot(3.5F, 10.0F, 6.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv3, ModelTransform.pivot(-3.5F, 10.0F, -5.0F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv3, ModelTransform.pivot(3.5F, 10.0F, -5.0F));
      return TexturedModelData.of(lv, 128, 64);
   }

   public void setAngles(AbstractDonkeyEntity arg, float f, float g, float h, float i, float j) {
      this.head.pitch = j * 0.017453292F;
      this.head.yaw = i * 0.017453292F;
      this.rightHindLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g;
      this.leftHindLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
      this.rightFrontLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
      this.leftFrontLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g;
      boolean bl = !arg.isBaby() && arg.hasChest();
      this.rightChest.visible = bl;
      this.leftChest.visible = bl;
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      if (this.child) {
         float l = 2.0F;
         matrices.push();
         float m = 0.7F;
         matrices.scale(0.71428573F, 0.64935064F, 0.7936508F);
         matrices.translate(0.0F, 1.3125F, 0.22F);
         this.head.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         matrices.pop();
         matrices.push();
         float n = 1.1F;
         matrices.scale(0.625F, 0.45454544F, 0.45454544F);
         matrices.translate(0.0F, 2.0625F, 0.0F);
         this.body.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         matrices.pop();
         matrices.push();
         matrices.scale(0.45454544F, 0.41322312F, 0.45454544F);
         matrices.translate(0.0F, 2.0625F, 0.0F);
         ImmutableList.of(this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach((part) -> {
            part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         });
         matrices.pop();
      } else {
         ImmutableList.of(this.head, this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach((part) -> {
            part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         });
      }

   }
}
