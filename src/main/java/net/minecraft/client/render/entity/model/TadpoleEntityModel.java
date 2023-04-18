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
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class TadpoleEntityModel extends AnimalModel {
   private final ModelPart root;
   private final ModelPart tail;

   public TadpoleEntityModel(ModelPart root) {
      super(true, 8.0F, 3.35F);
      this.root = root;
      this.tail = root.getChild(EntityModelPartNames.TAIL);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      float f = 0.0F;
      float g = 22.0F;
      float h = -3.0F;
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 3.0F), ModelTransform.pivot(0.0F, 22.0F, -3.0F));
      lv2.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -1.0F, 0.0F, 0.0F, 2.0F, 7.0F), ModelTransform.pivot(0.0F, 22.0F, 0.0F));
      return TexturedModelData.of(lv, 16, 16);
   }

   protected Iterable getHeadParts() {
      return ImmutableList.of(this.root);
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.tail);
   }

   public void setAngles(TadpoleEntity arg, float f, float g, float h, float i, float j) {
      float k = arg.isTouchingWater() ? 1.0F : 1.5F;
      this.tail.yaw = -k * 0.25F * MathHelper.sin(0.3F * h);
   }
}
