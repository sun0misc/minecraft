package net.minecraft.client.render.entity.model;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class SquidEntityModel extends SinglePartEntityModel {
   private final ModelPart[] tentacles = new ModelPart[8];
   private final ModelPart root;

   public SquidEntityModel(ModelPart root) {
      this.root = root;
      Arrays.setAll(this.tentacles, (index) -> {
         return root.getChild(getTentacleName(index));
      });
   }

   private static String getTentacleName(int index) {
      return "tentacle" + index;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      Dilation lv3 = new Dilation(0.02F);
      int i = true;
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 12.0F, lv3), ModelTransform.pivot(0.0F, 8.0F, 0.0F));
      int j = true;
      ModelPartBuilder lv4 = ModelPartBuilder.create().uv(48, 0).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F);

      for(int k = 0; k < 8; ++k) {
         double d = (double)k * Math.PI * 2.0 / 8.0;
         float f = (float)Math.cos(d) * 5.0F;
         float g = 15.0F;
         float h = (float)Math.sin(d) * 5.0F;
         d = (double)k * Math.PI * -2.0 / 8.0 + 1.5707963267948966;
         float l = (float)d;
         lv2.addChild(getTentacleName(k), lv4, ModelTransform.of(f, 15.0F, h, 0.0F, l, 0.0F));
      }

      return TexturedModelData.of(lv, 64, 32);
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      ModelPart[] var7 = this.tentacles;
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         ModelPart lv = var7[var9];
         lv.pitch = animationProgress;
      }

   }

   public ModelPart getPart() {
      return this.root;
   }
}
