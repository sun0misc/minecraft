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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ShulkerEntityModel extends CompositeEntityModel {
   private static final String LID = "lid";
   private static final String BASE = "base";
   private final ModelPart base;
   private final ModelPart lid;
   private final ModelPart head;

   public ShulkerEntityModel(ModelPart root) {
      super(RenderLayer::getEntityCutoutNoCullZOffset);
      this.lid = root.getChild("lid");
      this.base = root.getChild("base");
      this.head = root.getChild(EntityModelPartNames.HEAD);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -16.0F, -8.0F, 16.0F, 12.0F, 16.0F), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
      lv2.addChild("base", ModelPartBuilder.create().uv(0, 28).cuboid(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 52).cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F), ModelTransform.pivot(0.0F, 12.0F, 0.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public void setAngles(ShulkerEntity arg, float f, float g, float h, float i, float j) {
      float k = h - (float)arg.age;
      float l = (0.5F + arg.getOpenProgress(k)) * 3.1415927F;
      float m = -1.0F + MathHelper.sin(l);
      float n = 0.0F;
      if (l > 3.1415927F) {
         n = MathHelper.sin(h * 0.1F) * 0.7F;
      }

      this.lid.setPivot(0.0F, 16.0F + MathHelper.sin(l) * 8.0F + n, 0.0F);
      if (arg.getOpenProgress(k) > 0.3F) {
         this.lid.yaw = m * m * m * m * 3.1415927F * 0.125F;
      } else {
         this.lid.yaw = 0.0F;
      }

      this.head.pitch = j * 0.017453292F;
      this.head.yaw = (arg.headYaw - 180.0F - arg.bodyYaw) * 0.017453292F;
   }

   public Iterable getParts() {
      return ImmutableList.of(this.base, this.lid);
   }

   public ModelPart getLid() {
      return this.lid;
   }

   public ModelPart getHead() {
      return this.head;
   }
}
