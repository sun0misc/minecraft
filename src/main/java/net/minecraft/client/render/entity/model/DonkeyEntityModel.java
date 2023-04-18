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
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;

@Environment(EnvType.CLIENT)
public class DonkeyEntityModel extends HorseEntityModel {
   private final ModelPart leftChest;
   private final ModelPart rightChest;

   public DonkeyEntityModel(ModelPart arg) {
      super(arg);
      this.leftChest = this.body.getChild(EntityModelPartNames.LEFT_CHEST);
      this.rightChest = this.body.getChild(EntityModelPartNames.RIGHT_CHEST);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = HorseEntityModel.getModelData(Dilation.NONE);
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.getChild(EntityModelPartNames.BODY);
      ModelPartBuilder lv4 = ModelPartBuilder.create().uv(26, 21).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
      lv3.addChild(EntityModelPartNames.LEFT_CHEST, lv4, ModelTransform.of(6.0F, -8.0F, 0.0F, 0.0F, -1.5707964F, 0.0F));
      lv3.addChild(EntityModelPartNames.RIGHT_CHEST, lv4, ModelTransform.of(-6.0F, -8.0F, 0.0F, 0.0F, 1.5707964F, 0.0F));
      ModelPartData lv5 = lv2.getChild("head_parts").getChild(EntityModelPartNames.HEAD);
      ModelPartBuilder lv6 = ModelPartBuilder.create().uv(0, 12).cuboid(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F);
      lv5.addChild(EntityModelPartNames.LEFT_EAR, lv6, ModelTransform.of(1.25F, -10.0F, 4.0F, 0.2617994F, 0.0F, 0.2617994F));
      lv5.addChild(EntityModelPartNames.RIGHT_EAR, lv6, ModelTransform.of(-1.25F, -10.0F, 4.0F, 0.2617994F, 0.0F, -0.2617994F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public void setAngles(AbstractDonkeyEntity arg, float f, float g, float h, float i, float j) {
      super.setAngles((AbstractHorseEntity)arg, f, g, h, i, j);
      if (arg.hasChest()) {
         this.leftChest.visible = true;
         this.rightChest.visible = true;
      } else {
         this.leftChest.visible = false;
         this.rightChest.visible = false;
      }

   }
}
