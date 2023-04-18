package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class DragonHeadEntityModel extends SkullBlockEntityModel {
   private final ModelPart head;
   private final ModelPart jaw;

   public DragonHeadEntityModel(ModelPart root) {
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.jaw = this.head.getChild(EntityModelPartNames.JAW);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      float f = -16.0F;
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().cuboid("upper_lip", -6.0F, -1.0F, -24.0F, 12, 5, 16, 176, 44).cuboid("upper_head", -8.0F, -8.0F, -10.0F, 16, 16, 16, 112, 30).mirrored(true).cuboid("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0).cuboid("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0).mirrored(false).cuboid("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0).cuboid("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0), ModelTransform.NONE);
      lv3.addChild(EntityModelPartNames.JAW, ModelPartBuilder.create().uv(176, 65).cuboid(EntityModelPartNames.JAW, -6.0F, 0.0F, -16.0F, 12.0F, 4.0F, 16.0F), ModelTransform.pivot(0.0F, 4.0F, -8.0F));
      return TexturedModelData.of(lv, 256, 256);
   }

   public void setHeadRotation(float animationProgress, float yaw, float pitch) {
      this.jaw.pitch = (float)(Math.sin((double)(animationProgress * 3.1415927F * 0.2F)) + 1.0) * 0.2F;
      this.head.yaw = yaw * 0.017453292F;
      this.head.pitch = pitch * 0.017453292F;
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      matrices.push();
      matrices.translate(0.0F, -0.374375F, 0.0F);
      matrices.scale(0.75F, 0.75F, 0.75F);
      this.head.render(matrices, vertices, light, overlay, red, green, blue, alpha);
      matrices.pop();
   }
}
