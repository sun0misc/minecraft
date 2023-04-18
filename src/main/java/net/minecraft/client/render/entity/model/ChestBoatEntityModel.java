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

@Environment(EnvType.CLIENT)
public class ChestBoatEntityModel extends BoatEntityModel {
   private static final String CHEST_BOTTOM = "chest_bottom";
   private static final String CHEST_LID = "chest_lid";
   private static final String CHEST_LOCK = "chest_lock";

   public ChestBoatEntityModel(ModelPart arg) {
      super(arg);
   }

   protected ImmutableList.Builder getParts(ModelPart root) {
      ImmutableList.Builder builder = super.getParts(root);
      builder.add(root.getChild("chest_bottom"));
      builder.add(root.getChild("chest_lid"));
      builder.add(root.getChild("chest_lock"));
      return builder;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      BoatEntityModel.addParts(lv2);
      lv2.addChild("chest_bottom", ModelPartBuilder.create().uv(0, 76).cuboid(0.0F, 0.0F, 0.0F, 12.0F, 8.0F, 12.0F), ModelTransform.of(-2.0F, -5.0F, -6.0F, 0.0F, -1.5707964F, 0.0F));
      lv2.addChild("chest_lid", ModelPartBuilder.create().uv(0, 59).cuboid(0.0F, 0.0F, 0.0F, 12.0F, 4.0F, 12.0F), ModelTransform.of(-2.0F, -9.0F, -6.0F, 0.0F, -1.5707964F, 0.0F));
      lv2.addChild("chest_lock", ModelPartBuilder.create().uv(0, 59).cuboid(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 1.0F), ModelTransform.of(-1.0F, -6.0F, -1.0F, 0.0F, -1.5707964F, 0.0F));
      return TexturedModelData.of(lv, 128, 128);
   }
}
