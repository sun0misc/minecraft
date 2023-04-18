package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

@Environment(EnvType.CLIENT)
public class ShoulderParrotFeatureRenderer extends FeatureRenderer {
   private final ParrotEntityModel model;

   public ShoulderParrotFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.model = new ParrotEntityModel(loader.getModelPart(EntityModelLayers.PARROT));
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, PlayerEntity arg3, float f, float g, float h, float j, float k, float l) {
      this.renderShoulderParrot(arg, arg2, i, arg3, f, g, k, l, true);
      this.renderShoulderParrot(arg, arg2, i, arg3, f, g, k, l, false);
   }

   private void renderShoulderParrot(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntity player, float limbAngle, float limbDistance, float headYaw, float headPitch, boolean leftShoulder) {
      NbtCompound lv = leftShoulder ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
      EntityType.get(lv.getString("id")).filter((type) -> {
         return type == EntityType.PARROT;
      }).ifPresent((type) -> {
         matrices.push();
         matrices.translate(leftShoulder ? 0.4F : -0.4F, player.isInSneakingPose() ? -1.3F : -1.5F, 0.0F);
         ParrotEntity.Variant lvx = ParrotEntity.Variant.byIndex(lv.getInt("Variant"));
         VertexConsumer lv2 = vertexConsumers.getBuffer(this.model.getLayer(ParrotEntityRenderer.getTexture(lvx)));
         this.model.poseOnShoulder(matrices, lv2, light, OverlayTexture.DEFAULT_UV, limbAngle, limbDistance, headYaw, headPitch, player.age);
         matrices.pop();
      });
   }
}
