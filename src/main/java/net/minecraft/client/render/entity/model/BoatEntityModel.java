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
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BoatEntityModel extends CompositeEntityModel implements ModelWithWaterPatch {
   private static final String LEFT_PADDLE = "left_paddle";
   private static final String RIGHT_PADDLE = "right_paddle";
   private static final String WATER_PATCH = "water_patch";
   private static final String BOTTOM = "bottom";
   private static final String BACK = "back";
   private static final String FRONT = "front";
   private static final String RIGHT = "right";
   private static final String LEFT = "left";
   private final ModelPart leftPaddle;
   private final ModelPart rightPaddle;
   private final ModelPart waterPatch;
   private final ImmutableList parts;

   public BoatEntityModel(ModelPart root) {
      this.leftPaddle = root.getChild("left_paddle");
      this.rightPaddle = root.getChild("right_paddle");
      this.waterPatch = root.getChild("water_patch");
      this.parts = this.getParts(root).build();
   }

   protected ImmutableList.Builder getParts(ModelPart root) {
      ImmutableList.Builder builder = new ImmutableList.Builder();
      builder.add(new ModelPart[]{root.getChild("bottom"), root.getChild("back"), root.getChild("front"), root.getChild("right"), root.getChild("left"), this.leftPaddle, this.rightPaddle});
      return builder;
   }

   public static void addParts(ModelPartData modelPartData) {
      int i = true;
      int j = true;
      int k = true;
      int l = true;
      int m = true;
      modelPartData.addChild("bottom", ModelPartBuilder.create().uv(0, 0).cuboid(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F), ModelTransform.of(0.0F, 3.0F, 1.0F, 1.5707964F, 0.0F, 0.0F));
      modelPartData.addChild("back", ModelPartBuilder.create().uv(0, 19).cuboid(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F), ModelTransform.of(-15.0F, 4.0F, 4.0F, 0.0F, 4.712389F, 0.0F));
      modelPartData.addChild("front", ModelPartBuilder.create().uv(0, 27).cuboid(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F), ModelTransform.of(15.0F, 4.0F, 0.0F, 0.0F, 1.5707964F, 0.0F));
      modelPartData.addChild("right", ModelPartBuilder.create().uv(0, 35).cuboid(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F), ModelTransform.of(0.0F, 4.0F, -9.0F, 0.0F, 3.1415927F, 0.0F));
      modelPartData.addChild("left", ModelPartBuilder.create().uv(0, 43).cuboid(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F), ModelTransform.pivot(0.0F, 4.0F, 9.0F));
      int n = true;
      int o = true;
      int p = true;
      float f = -5.0F;
      modelPartData.addChild("left_paddle", ModelPartBuilder.create().uv(62, 0).cuboid(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).cuboid(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F), ModelTransform.of(3.0F, -5.0F, 9.0F, 0.0F, 0.0F, 0.19634955F));
      modelPartData.addChild("right_paddle", ModelPartBuilder.create().uv(62, 20).cuboid(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).cuboid(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F), ModelTransform.of(3.0F, -5.0F, -9.0F, 0.0F, 3.1415927F, 0.19634955F));
      modelPartData.addChild("water_patch", ModelPartBuilder.create().uv(0, 0).cuboid(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F), ModelTransform.of(0.0F, -3.0F, 1.0F, 1.5707964F, 0.0F, 0.0F));
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      addParts(lv2);
      return TexturedModelData.of(lv, 128, 64);
   }

   public void setAngles(BoatEntity arg, float f, float g, float h, float i, float j) {
      setPaddleAngle(arg, 0, this.leftPaddle, f);
      setPaddleAngle(arg, 1, this.rightPaddle, f);
   }

   public ImmutableList getParts() {
      return this.parts;
   }

   public ModelPart getWaterPatch() {
      return this.waterPatch;
   }

   private static void setPaddleAngle(BoatEntity entity, int sigma, ModelPart part, float angle) {
      float g = entity.interpolatePaddlePhase(sigma, angle);
      part.pitch = MathHelper.clampedLerp(-1.0471976F, -0.2617994F, (MathHelper.sin(-g) + 1.0F) / 2.0F);
      part.yaw = MathHelper.clampedLerp(-0.7853982F, 0.7853982F, (MathHelper.sin(-g + 1.0F) + 1.0F) / 2.0F);
      if (sigma == 1) {
         part.yaw = 3.1415927F - part.yaw;
      }

   }

   // $FF: synthetic method
   public Iterable getParts() {
      return this.getParts();
   }
}
