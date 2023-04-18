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
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class SkullEntityModel extends SkullBlockEntityModel {
   private final ModelPart root;
   protected final ModelPart head;

   public SkullEntityModel(ModelPart root) {
      this.root = root;
      this.head = root.getChild(EntityModelPartNames.HEAD);
   }

   public static ModelData getModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), ModelTransform.NONE);
      return lv;
   }

   public static TexturedModelData getHeadTexturedModelData() {
      ModelData lv = getModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.getChild(EntityModelPartNames.HEAD).addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.25F)), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 64);
   }

   public static TexturedModelData getSkullTexturedModelData() {
      ModelData lv = getModelData();
      return TexturedModelData.of(lv, 64, 32);
   }

   public void setHeadRotation(float animationProgress, float yaw, float pitch) {
      this.head.yaw = yaw * 0.017453292F;
      this.head.pitch = pitch * 0.017453292F;
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
   }
}
