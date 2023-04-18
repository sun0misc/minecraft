package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class PiglinHeadEntityModel extends SkullBlockEntityModel {
   private final ModelPart head;
   private final ModelPart leftEar;
   private final ModelPart rightEar;

   public PiglinHeadEntityModel(ModelPart root) {
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.leftEar = this.head.getChild(EntityModelPartNames.LEFT_EAR);
      this.rightEar = this.head.getChild(EntityModelPartNames.RIGHT_EAR);
   }

   public static ModelData getModelData() {
      ModelData lv = new ModelData();
      PiglinEntityModel.addHead(Dilation.NONE, lv);
      return lv;
   }

   public void setHeadRotation(float animationProgress, float yaw, float pitch) {
      this.head.yaw = yaw * 0.017453292F;
      this.head.pitch = pitch * 0.017453292F;
      float i = 1.2F;
      this.leftEar.roll = (float)(-(Math.cos((double)(animationProgress * 3.1415927F * 0.2F * 1.2F)) + 2.5)) * 0.2F;
      this.rightEar.roll = (float)(Math.cos((double)(animationProgress * 3.1415927F * 0.2F)) + 2.5) * 0.2F;
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.head.render(matrices, vertices, light, overlay, red, green, blue, alpha);
   }
}
