package net.minecraft.client.render.entity.model;

import java.util.Arrays;
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
public class BlazeEntityModel extends SinglePartEntityModel {
   private final ModelPart root;
   private final ModelPart[] rods;
   private final ModelPart head;

   public BlazeEntityModel(ModelPart root) {
      this.root = root;
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.rods = new ModelPart[12];
      Arrays.setAll(this.rods, (index) -> {
         return root.getChild(getRodName(index));
      });
   }

   private static String getRodName(int index) {
      return "part" + index;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), ModelTransform.NONE);
      float f = 0.0F;
      ModelPartBuilder lv3 = ModelPartBuilder.create().uv(0, 16).cuboid(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F);

      int i;
      float g;
      float h;
      float j;
      for(i = 0; i < 4; ++i) {
         g = MathHelper.cos(f) * 9.0F;
         h = -2.0F + MathHelper.cos((float)(i * 2) * 0.25F);
         j = MathHelper.sin(f) * 9.0F;
         lv2.addChild(getRodName(i), lv3, ModelTransform.pivot(g, h, j));
         ++f;
      }

      f = 0.7853982F;

      for(i = 4; i < 8; ++i) {
         g = MathHelper.cos(f) * 7.0F;
         h = 2.0F + MathHelper.cos((float)(i * 2) * 0.25F);
         j = MathHelper.sin(f) * 7.0F;
         lv2.addChild(getRodName(i), lv3, ModelTransform.pivot(g, h, j));
         ++f;
      }

      f = 0.47123894F;

      for(i = 8; i < 12; ++i) {
         g = MathHelper.cos(f) * 5.0F;
         h = 11.0F + MathHelper.cos((float)i * 1.5F * 0.5F);
         j = MathHelper.sin(f) * 5.0F;
         lv2.addChild(getRodName(i), lv3, ModelTransform.pivot(g, h, j));
         ++f;
      }

      return TexturedModelData.of(lv, 64, 32);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      float k = animationProgress * 3.1415927F * -0.1F;

      int l;
      for(l = 0; l < 4; ++l) {
         this.rods[l].pivotY = -2.0F + MathHelper.cos(((float)(l * 2) + animationProgress) * 0.25F);
         this.rods[l].pivotX = MathHelper.cos(k) * 9.0F;
         this.rods[l].pivotZ = MathHelper.sin(k) * 9.0F;
         ++k;
      }

      k = 0.7853982F + animationProgress * 3.1415927F * 0.03F;

      for(l = 4; l < 8; ++l) {
         this.rods[l].pivotY = 2.0F + MathHelper.cos(((float)(l * 2) + animationProgress) * 0.25F);
         this.rods[l].pivotX = MathHelper.cos(k) * 7.0F;
         this.rods[l].pivotZ = MathHelper.sin(k) * 7.0F;
         ++k;
      }

      k = 0.47123894F + animationProgress * 3.1415927F * -0.05F;

      for(l = 8; l < 12; ++l) {
         this.rods[l].pivotY = 11.0F + MathHelper.cos(((float)l * 1.5F + animationProgress) * 0.5F);
         this.rods[l].pivotX = MathHelper.cos(k) * 5.0F;
         this.rods[l].pivotZ = MathHelper.sin(k) * 5.0F;
         ++k;
      }

      this.head.yaw = headYaw * 0.017453292F;
      this.head.pitch = headPitch * 0.017453292F;
   }
}
