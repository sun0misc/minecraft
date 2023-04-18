package net.minecraft.client.render.entity.feature;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ArmorFeatureRenderer extends FeatureRenderer {
   private static final Map ARMOR_TEXTURE_CACHE = Maps.newHashMap();
   private final BipedEntityModel innerModel;
   private final BipedEntityModel outerModel;
   private final SpriteAtlasTexture armorTrimsAtlas;

   public ArmorFeatureRenderer(FeatureRendererContext context, BipedEntityModel innerModel, BipedEntityModel outerModel, BakedModelManager bakery) {
      super(context);
      this.innerModel = innerModel;
      this.outerModel = outerModel;
      this.armorTrimsAtlas = bakery.getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE);
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LivingEntity arg3, float f, float g, float h, float j, float k, float l) {
      this.renderArmor(arg, arg2, arg3, EquipmentSlot.CHEST, i, this.getModel(EquipmentSlot.CHEST));
      this.renderArmor(arg, arg2, arg3, EquipmentSlot.LEGS, i, this.getModel(EquipmentSlot.LEGS));
      this.renderArmor(arg, arg2, arg3, EquipmentSlot.FEET, i, this.getModel(EquipmentSlot.FEET));
      this.renderArmor(arg, arg2, arg3, EquipmentSlot.HEAD, i, this.getModel(EquipmentSlot.HEAD));
   }

   private void renderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, LivingEntity entity, EquipmentSlot armorSlot, int light, BipedEntityModel model) {
      ItemStack lv = entity.getEquippedStack(armorSlot);
      Item var9 = lv.getItem();
      if (var9 instanceof ArmorItem lv2) {
         if (lv2.getSlotType() == armorSlot) {
            ((BipedEntityModel)this.getContextModel()).copyBipedStateTo(model);
            this.setVisible(model, armorSlot);
            boolean bl = this.usesInnerModel(armorSlot);
            boolean bl2 = lv.hasGlint();
            if (lv2 instanceof DyeableArmorItem) {
               int j = ((DyeableArmorItem)lv2).getColor(lv);
               float f = (float)(j >> 16 & 255) / 255.0F;
               float g = (float)(j >> 8 & 255) / 255.0F;
               float h = (float)(j & 255) / 255.0F;
               this.renderArmorParts(matrices, vertexConsumers, light, lv2, bl2, model, bl, f, g, h, (String)null);
               this.renderArmorParts(matrices, vertexConsumers, light, lv2, bl2, model, bl, 1.0F, 1.0F, 1.0F, "overlay");
            } else {
               this.renderArmorParts(matrices, vertexConsumers, light, lv2, bl2, model, bl, 1.0F, 1.0F, 1.0F, (String)null);
            }

            ArmorTrim.getTrim(entity.world.getRegistryManager(), lv).ifPresent((trim) -> {
               this.renderTrim(lv2.getMaterial(), matrices, vertexConsumers, light, trim, bl2, model, bl, 1.0F, 1.0F, 1.0F);
            });
         }
      }
   }

   protected void setVisible(BipedEntityModel bipedModel, EquipmentSlot slot) {
      bipedModel.setVisible(false);
      switch (slot) {
         case HEAD:
            bipedModel.head.visible = true;
            bipedModel.hat.visible = true;
            break;
         case CHEST:
            bipedModel.body.visible = true;
            bipedModel.rightArm.visible = true;
            bipedModel.leftArm.visible = true;
            break;
         case LEGS:
            bipedModel.body.visible = true;
            bipedModel.rightLeg.visible = true;
            bipedModel.leftLeg.visible = true;
            break;
         case FEET:
            bipedModel.rightLeg.visible = true;
            bipedModel.leftLeg.visible = true;
      }

   }

   private void renderArmorParts(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ArmorItem item, boolean glint, BipedEntityModel model, boolean secondTextureLayer, float red, float green, float blue, @Nullable String overlay) {
      VertexConsumer lv = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(this.getArmorTexture(item, secondTextureLayer, overlay)), false, glint);
      model.render(matrices, lv, light, OverlayTexture.DEFAULT_UV, red, green, blue, 1.0F);
   }

   private void renderTrim(ArmorMaterial material, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ArmorTrim trim, boolean glint, BipedEntityModel model, boolean leggings, float red, float green, float blue) {
      Sprite lv = this.armorTrimsAtlas.getSprite(leggings ? trim.getLeggingsModelId(material) : trim.getGenericModelId(material));
      VertexConsumer lv2 = lv.getTextureSpecificVertexConsumer(ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, TexturedRenderLayers.getArmorTrims(), true, glint));
      model.render(matrices, lv2, light, OverlayTexture.DEFAULT_UV, red, green, blue, 1.0F);
   }

   private BipedEntityModel getModel(EquipmentSlot slot) {
      return this.usesInnerModel(slot) ? this.innerModel : this.outerModel;
   }

   private boolean usesInnerModel(EquipmentSlot slot) {
      return slot == EquipmentSlot.LEGS;
   }

   private Identifier getArmorTexture(ArmorItem item, boolean secondLayer, @Nullable String overlay) {
      String var10000 = item.getMaterial().getName();
      String string2 = "textures/models/armor/" + var10000 + "_layer_" + (secondLayer ? 2 : 1) + (overlay == null ? "" : "_" + overlay) + ".png";
      return (Identifier)ARMOR_TEXTURE_CACHE.computeIfAbsent(string2, Identifier::new);
   }
}
