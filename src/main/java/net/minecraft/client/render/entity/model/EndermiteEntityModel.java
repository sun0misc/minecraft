package net.minecraft.client.render.entity.model;

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
public class EndermiteEntityModel extends SinglePartEntityModel {
   private static final int BODY_SEGMENTS_COUNT = 4;
   private static final int[][] SEGMENT_DIMENSIONS = new int[][]{{4, 3, 2}, {6, 4, 5}, {3, 3, 1}, {1, 2, 1}};
   private static final int[][] SEGMENT_UVS = new int[][]{{0, 0}, {0, 5}, {0, 14}, {0, 18}};
   private final ModelPart root;
   private final ModelPart[] bodySegments;

   public EndermiteEntityModel(ModelPart root) {
      this.root = root;
      this.bodySegments = new ModelPart[4];

      for(int i = 0; i < 4; ++i) {
         this.bodySegments[i] = root.getChild(getSegmentName(i));
      }

   }

   private static String getSegmentName(int index) {
      return "segment" + index;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      float f = -3.5F;

      for(int i = 0; i < 4; ++i) {
         lv2.addChild(getSegmentName(i), ModelPartBuilder.create().uv(SEGMENT_UVS[i][0], SEGMENT_UVS[i][1]).cuboid((float)SEGMENT_DIMENSIONS[i][0] * -0.5F, 0.0F, (float)SEGMENT_DIMENSIONS[i][2] * -0.5F, (float)SEGMENT_DIMENSIONS[i][0], (float)SEGMENT_DIMENSIONS[i][1], (float)SEGMENT_DIMENSIONS[i][2]), ModelTransform.pivot(0.0F, (float)(24 - SEGMENT_DIMENSIONS[i][1]), f));
         if (i < 3) {
            f += (float)(SEGMENT_DIMENSIONS[i][2] + SEGMENT_DIMENSIONS[i + 1][2]) * 0.5F;
         }
      }

      return TexturedModelData.of(lv, 64, 32);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      for(int k = 0; k < this.bodySegments.length; ++k) {
         this.bodySegments[k].yaw = MathHelper.cos(animationProgress * 0.9F + (float)k * 0.15F * 3.1415927F) * 3.1415927F * 0.01F * (float)(1 + Math.abs(k - 2));
         this.bodySegments[k].pivotX = MathHelper.sin(animationProgress * 0.9F + (float)k * 0.15F * 3.1415927F) * 3.1415927F * 0.1F * (float)Math.abs(k - 2);
      }

   }
}
