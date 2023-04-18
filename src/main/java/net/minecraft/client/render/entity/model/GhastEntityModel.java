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
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class GhastEntityModel extends SinglePartEntityModel {
   private final ModelPart root;
   private final ModelPart[] tentacles = new ModelPart[9];

   public GhastEntityModel(ModelPart root) {
      this.root = root;

      for(int i = 0; i < this.tentacles.length; ++i) {
         this.tentacles[i] = root.getChild(getTentacleName(i));
      }

   }

   private static String getTentacleName(int index) {
      return "tentacle" + index;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), ModelTransform.pivot(0.0F, 17.6F, 0.0F));
      Random lv3 = Random.create(1660L);

      for(int i = 0; i < 9; ++i) {
         float f = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
         float g = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
         int j = lv3.nextInt(7) + 8;
         lv2.addChild(getTentacleName(i), ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, (float)j, 2.0F), ModelTransform.pivot(f, 24.6F, g));
      }

      return TexturedModelData.of(lv, 64, 32);
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      for(int k = 0; k < this.tentacles.length; ++k) {
         this.tentacles[k].pitch = 0.2F * MathHelper.sin(animationProgress * 0.3F + (float)k) + 0.4F;
      }

   }

   public ModelPart getPart() {
      return this.root;
   }
}
