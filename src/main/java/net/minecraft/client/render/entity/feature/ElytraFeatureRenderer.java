package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ElytraFeatureRenderer extends FeatureRenderer {
   private static final Identifier SKIN = new Identifier("textures/entity/elytra.png");
   private final ElytraEntityModel elytra;

   public ElytraFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.elytra = new ElytraEntityModel(loader.getModelPart(EntityModelLayers.ELYTRA));
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LivingEntity arg3, float f, float g, float h, float j, float k, float l) {
      ItemStack lv = arg3.getEquippedStack(EquipmentSlot.CHEST);
      if (lv.isOf(Items.ELYTRA)) {
         Identifier lv3;
         if (arg3 instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity lv2 = (AbstractClientPlayerEntity)arg3;
            if (lv2.canRenderElytraTexture() && lv2.getElytraTexture() != null) {
               lv3 = lv2.getElytraTexture();
            } else if (lv2.canRenderCapeTexture() && lv2.getCapeTexture() != null && lv2.isPartVisible(PlayerModelPart.CAPE)) {
               lv3 = lv2.getCapeTexture();
            } else {
               lv3 = SKIN;
            }
         } else {
            lv3 = SKIN;
         }

         arg.push();
         arg.translate(0.0F, 0.0F, 0.125F);
         this.getContextModel().copyStateTo(this.elytra);
         this.elytra.setAngles(arg3, f, g, j, k, l);
         VertexConsumer lv4 = ItemRenderer.getArmorGlintConsumer(arg2, RenderLayer.getArmorCutoutNoCull(lv3), false, lv.hasGlint());
         this.elytra.render(arg, lv4, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
         arg.pop();
      }
   }
}
