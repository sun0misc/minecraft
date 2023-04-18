package net.minecraft.client.render.item;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class HeldItemRenderer {
   private static final RenderLayer MAP_BACKGROUND = RenderLayer.getText(new Identifier("textures/map/map_background.png"));
   private static final RenderLayer MAP_BACKGROUND_CHECKERBOARD = RenderLayer.getText(new Identifier("textures/map/map_background_checkerboard.png"));
   private static final float field_32735 = -0.4F;
   private static final float field_32736 = 0.2F;
   private static final float field_32737 = -0.2F;
   private static final float field_32738 = -0.6F;
   private static final float EQUIP_OFFSET_TRANSLATE_X = 0.56F;
   private static final float EQUIP_OFFSET_TRANSLATE_Y = -0.52F;
   private static final float EQUIP_OFFSET_TRANSLATE_Z = -0.72F;
   private static final float field_32742 = 45.0F;
   private static final float field_32743 = -80.0F;
   private static final float field_32744 = -20.0F;
   private static final float field_32745 = -20.0F;
   private static final float EAT_OR_DRINK_X_ANGLE_MULTIPLIER = 10.0F;
   private static final float EAT_OR_DRINK_Y_ANGLE_MULTIPLIER = 90.0F;
   private static final float EAT_OR_DRINK_Z_ANGLE_MULTIPLIER = 30.0F;
   private static final float field_32749 = 0.6F;
   private static final float field_32750 = -0.5F;
   private static final float field_32751 = 0.0F;
   private static final double field_32752 = 27.0;
   private static final float field_32753 = 0.8F;
   private static final float field_32754 = 0.1F;
   private static final float field_32755 = -0.3F;
   private static final float field_32756 = 0.4F;
   private static final float field_32757 = -0.4F;
   private static final float ARM_HOLDING_ITEM_SECOND_Y_ANGLE_MULTIPLIER = 70.0F;
   private static final float ARM_HOLDING_ITEM_FIRST_Z_ANGLE_MULTIPLIER = -20.0F;
   private static final float field_32690 = -0.6F;
   private static final float field_32691 = 0.8F;
   private static final float field_32692 = 0.8F;
   private static final float field_32693 = -0.75F;
   private static final float field_32694 = -0.9F;
   private static final float field_32695 = 45.0F;
   private static final float field_32696 = -1.0F;
   private static final float field_32697 = 3.6F;
   private static final float field_32698 = 3.5F;
   private static final float ARM_HOLDING_ITEM_TRANSLATE_X = 5.6F;
   private static final int ARM_HOLDING_ITEM_X_ANGLE_MULTIPLIER = 200;
   private static final int ARM_HOLDING_ITEM_THIRD_Y_ANGLE_MULTIPLIER = -135;
   private static final int ARM_HOLDING_ITEM_SECOND_Z_ANGLE_MULTIPLIER = 120;
   private static final float field_32703 = -0.4F;
   private static final float field_32704 = -0.2F;
   private static final float field_32705 = 0.0F;
   private static final float field_32706 = 0.04F;
   private static final float field_32707 = -0.72F;
   private static final float field_32708 = -1.2F;
   private static final float field_32709 = -0.5F;
   private static final float field_32710 = 45.0F;
   private static final float field_32711 = -85.0F;
   private static final float ARM_X_ANGLE_MULTIPLIER = 45.0F;
   private static final float ARM_Y_ANGLE_MULTIPLIER = 92.0F;
   private static final float ARM_Z_ANGLE_MULTIPLIER = -41.0F;
   private static final float ARM_TRANSLATE_X = 0.3F;
   private static final float ARM_TRANSLATE_Y = -1.1F;
   private static final float ARM_TRANSLATE_Z = 0.45F;
   private static final float field_32718 = 20.0F;
   private static final float FIRST_PERSON_MAP_FIRST_SCALE = 0.38F;
   private static final float FIRST_PERSON_MAP_TRANSLATE_X = -0.5F;
   private static final float FIRST_PERSON_MAP_TRANSLATE_Y = -0.5F;
   private static final float FIRST_PERSON_MAP_TRANSLATE_Z = 0.0F;
   private static final float FIRST_PERSON_MAP_SECOND_SCALE = 0.0078125F;
   private static final int field_32724 = 7;
   private static final int field_32725 = 128;
   private static final int field_32726 = 128;
   private static final float field_32727 = 0.0F;
   private static final float field_32728 = 0.0F;
   private static final float field_32729 = 0.04F;
   private static final float field_32730 = 0.0F;
   private static final float field_32731 = 0.004F;
   private static final float field_32732 = 0.0F;
   private static final float field_32733 = 0.2F;
   private static final float field_32734 = 0.1F;
   private final MinecraftClient client;
   private ItemStack mainHand;
   private ItemStack offHand;
   private float equipProgressMainHand;
   private float prevEquipProgressMainHand;
   private float equipProgressOffHand;
   private float prevEquipProgressOffHand;
   private final EntityRenderDispatcher entityRenderDispatcher;
   private final ItemRenderer itemRenderer;

   public HeldItemRenderer(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
      this.mainHand = ItemStack.EMPTY;
      this.offHand = ItemStack.EMPTY;
      this.client = client;
      this.entityRenderDispatcher = entityRenderDispatcher;
      this.itemRenderer = itemRenderer;
   }

   public void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (!stack.isEmpty()) {
         this.itemRenderer.renderItem(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, entity.world, light, OverlayTexture.DEFAULT_UV, entity.getId() + renderMode.ordinal());
      }
   }

   private float getMapAngle(float tickDelta) {
      float g = 1.0F - tickDelta / 45.0F + 0.1F;
      g = MathHelper.clamp(g, 0.0F, 1.0F);
      g = -MathHelper.cos(g * 3.1415927F) * 0.5F + 0.5F;
      return g;
   }

   private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Arm arm) {
      RenderSystem.setShaderTexture(0, this.client.player.getSkinTexture());
      PlayerEntityRenderer lv = (PlayerEntityRenderer)this.entityRenderDispatcher.getRenderer(this.client.player);
      matrices.push();
      float f = arm == Arm.RIGHT ? 1.0F : -1.0F;
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(92.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * -41.0F));
      matrices.translate(f * 0.3F, -1.1F, 0.45F);
      if (arm == Arm.RIGHT) {
         lv.renderRightArm(matrices, vertexConsumers, light, this.client.player);
      } else {
         lv.renderLeftArm(matrices, vertexConsumers, light, this.client.player);
      }

      matrices.pop();
   }

   private void renderMapInOneHand(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, Arm arm, float swingProgress, ItemStack stack) {
      float h = arm == Arm.RIGHT ? 1.0F : -1.0F;
      matrices.translate(h * 0.125F, -0.125F, 0.0F);
      if (!this.client.player.isInvisible()) {
         matrices.push();
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(h * 10.0F));
         this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
         matrices.pop();
      }

      matrices.push();
      matrices.translate(h * 0.51F, -0.08F + equipProgress * -1.2F, -0.75F);
      float j = MathHelper.sqrt(swingProgress);
      float k = MathHelper.sin(j * 3.1415927F);
      float l = -0.5F * k;
      float m = 0.4F * MathHelper.sin(j * 6.2831855F);
      float n = -0.3F * MathHelper.sin(swingProgress * 3.1415927F);
      matrices.translate(h * l, m - 0.3F * k, n);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k * -45.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h * k * -30.0F));
      this.renderFirstPersonMap(matrices, vertexConsumers, light, stack);
      matrices.pop();
   }

   private void renderMapInBothHands(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float pitch, float equipProgress, float swingProgress) {
      float j = MathHelper.sqrt(swingProgress);
      float k = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
      float l = -0.4F * MathHelper.sin(j * 3.1415927F);
      matrices.translate(0.0F, -k / 2.0F, l);
      float m = this.getMapAngle(pitch);
      matrices.translate(0.0F, 0.04F + equipProgress * -1.2F + m * -0.5F, -0.72F);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m * -85.0F));
      if (!this.client.player.isInvisible()) {
         matrices.push();
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
         this.renderArm(matrices, vertexConsumers, light, Arm.RIGHT);
         this.renderArm(matrices, vertexConsumers, light, Arm.LEFT);
         matrices.pop();
      }

      float n = MathHelper.sin(j * 3.1415927F);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(n * 20.0F));
      matrices.scale(2.0F, 2.0F, 2.0F);
      this.renderFirstPersonMap(matrices, vertexConsumers, light, this.mainHand);
   }

   private void renderFirstPersonMap(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int swingProgress, ItemStack stack) {
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
      matrices.scale(0.38F, 0.38F, 0.38F);
      matrices.translate(-0.5F, -0.5F, 0.0F);
      matrices.scale(0.0078125F, 0.0078125F, 0.0078125F);
      Integer integer = FilledMapItem.getMapId(stack);
      MapState lv = FilledMapItem.getMapState((Integer)integer, this.client.world);
      VertexConsumer lv2 = vertexConsumers.getBuffer(lv == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      lv2.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(swingProgress).next();
      lv2.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(swingProgress).next();
      lv2.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(swingProgress).next();
      lv2.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(swingProgress).next();
      if (lv != null) {
         this.client.gameRenderer.getMapRenderer().draw(matrices, vertexConsumers, integer, lv, false, swingProgress);
      }

   }

   private void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm) {
      boolean bl = arm != Arm.LEFT;
      float h = bl ? 1.0F : -1.0F;
      float j = MathHelper.sqrt(swingProgress);
      float k = -0.3F * MathHelper.sin(j * 3.1415927F);
      float l = 0.4F * MathHelper.sin(j * 6.2831855F);
      float m = -0.4F * MathHelper.sin(swingProgress * 3.1415927F);
      matrices.translate(h * (k + 0.64000005F), l + -0.6F + equipProgress * -0.6F, m + -0.71999997F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h * 45.0F));
      float n = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
      float o = MathHelper.sin(j * 3.1415927F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h * o * 70.0F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(h * n * -20.0F));
      AbstractClientPlayerEntity lv = this.client.player;
      RenderSystem.setShaderTexture(0, lv.getSkinTexture());
      matrices.translate(h * -1.0F, 3.6F, 3.5F);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(h * 120.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h * -135.0F));
      matrices.translate(h * 5.6F, 0.0F, 0.0F);
      PlayerEntityRenderer lv2 = (PlayerEntityRenderer)this.entityRenderDispatcher.getRenderer(lv);
      if (bl) {
         lv2.renderRightArm(matrices, vertexConsumers, light, lv);
      } else {
         lv2.renderLeftArm(matrices, vertexConsumers, light, lv);
      }

   }

   private void applyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack) {
      float g = (float)this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F;
      float h = g / (float)stack.getMaxUseTime();
      float i;
      if (h < 0.8F) {
         i = MathHelper.abs(MathHelper.cos(g / 4.0F * 3.1415927F) * 0.1F);
         matrices.translate(0.0F, i, 0.0F);
      }

      i = 1.0F - (float)Math.pow((double)h, 27.0);
      int j = arm == Arm.RIGHT ? 1 : -1;
      matrices.translate(i * 0.6F * (float)j, i * -0.5F, i * 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)j * i * 90.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i * 10.0F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)j * i * 30.0F));
   }

   private void applyBrushTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, float equipProgress) {
      this.applyEquipOffset(matrices, arm, equipProgress);
      float h = (float)(this.client.player.getItemUseTimeLeft() % 10);
      float i = h - tickDelta + 1.0F;
      float j = 1.0F - i / 10.0F;
      float k = -90.0F;
      float l = 60.0F;
      float m = 150.0F;
      float n = -15.0F;
      int o = true;
      float p = -15.0F + 75.0F * MathHelper.cos(j * 2.0F * 3.1415927F);
      if (arm != Arm.RIGHT) {
         matrices.translate(0.1, 0.83, 0.35);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p));
         matrices.translate(-0.3, 0.22, 0.35);
      } else {
         matrices.translate(-0.25, 0.22, 0.35);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p));
      }

   }

   private void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) {
      int i = arm == Arm.RIGHT ? 1 : -1;
      float g = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0F + g * -20.0F)));
      float h = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * h * -20.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * -80.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -45.0F));
   }

   private void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) {
      int i = arm == Arm.RIGHT ? 1 : -1;
      matrices.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
   }

   public void renderItem(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light) {
      float g = player.getHandSwingProgress(tickDelta);
      Hand lv = (Hand)MoreObjects.firstNonNull(player.preferredHand, Hand.MAIN_HAND);
      float h = MathHelper.lerp(tickDelta, player.prevPitch, player.getPitch());
      HandRenderType lv2 = getHandRenderType(player);
      float j = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
      float k = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((player.getPitch(tickDelta) - j) * 0.1F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((player.getYaw(tickDelta) - k) * 0.1F));
      float l;
      float m;
      if (lv2.renderMainHand) {
         l = lv == Hand.MAIN_HAND ? g : 0.0F;
         m = 1.0F - MathHelper.lerp(tickDelta, this.prevEquipProgressMainHand, this.equipProgressMainHand);
         this.renderFirstPersonItem(player, tickDelta, h, Hand.MAIN_HAND, l, this.mainHand, m, matrices, vertexConsumers, light);
      }

      if (lv2.renderOffHand) {
         l = lv == Hand.OFF_HAND ? g : 0.0F;
         m = 1.0F - MathHelper.lerp(tickDelta, this.prevEquipProgressOffHand, this.equipProgressOffHand);
         this.renderFirstPersonItem(player, tickDelta, h, Hand.OFF_HAND, l, this.offHand, m, matrices, vertexConsumers, light);
      }

      vertexConsumers.draw();
   }

   @VisibleForTesting
   static HandRenderType getHandRenderType(ClientPlayerEntity player) {
      ItemStack lv = player.getMainHandStack();
      ItemStack lv2 = player.getOffHandStack();
      boolean bl = lv.isOf(Items.BOW) || lv2.isOf(Items.BOW);
      boolean bl2 = lv.isOf(Items.CROSSBOW) || lv2.isOf(Items.CROSSBOW);
      if (!bl && !bl2) {
         return HeldItemRenderer.HandRenderType.RENDER_BOTH_HANDS;
      } else if (player.isUsingItem()) {
         return getUsingItemHandRenderType(player);
      } else {
         return isChargedCrossbow(lv) ? HeldItemRenderer.HandRenderType.RENDER_MAIN_HAND_ONLY : HeldItemRenderer.HandRenderType.RENDER_BOTH_HANDS;
      }
   }

   private static HandRenderType getUsingItemHandRenderType(ClientPlayerEntity player) {
      ItemStack lv = player.getActiveItem();
      Hand lv2 = player.getActiveHand();
      if (!lv.isOf(Items.BOW) && !lv.isOf(Items.CROSSBOW)) {
         return lv2 == Hand.MAIN_HAND && isChargedCrossbow(player.getOffHandStack()) ? HeldItemRenderer.HandRenderType.RENDER_MAIN_HAND_ONLY : HeldItemRenderer.HandRenderType.RENDER_BOTH_HANDS;
      } else {
         return HeldItemRenderer.HandRenderType.shouldOnlyRender(lv2);
      }
   }

   private static boolean isChargedCrossbow(ItemStack stack) {
      return stack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(stack);
   }

   private void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (!player.isUsingSpyglass()) {
         boolean bl = hand == Hand.MAIN_HAND;
         Arm lv = bl ? player.getMainArm() : player.getMainArm().getOpposite();
         matrices.push();
         if (item.isEmpty()) {
            if (bl && !player.isInvisible()) {
               this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, lv);
            }
         } else if (item.isOf(Items.FILLED_MAP)) {
            if (bl && this.offHand.isEmpty()) {
               this.renderMapInBothHands(matrices, vertexConsumers, light, pitch, equipProgress, swingProgress);
            } else {
               this.renderMapInOneHand(matrices, vertexConsumers, light, equipProgress, lv, swingProgress, item);
            }
         } else {
            boolean bl2;
            float l;
            float m;
            float n;
            float o;
            if (item.isOf(Items.CROSSBOW)) {
               bl2 = CrossbowItem.isCharged(item);
               boolean bl3 = lv == Arm.RIGHT;
               int k = bl3 ? 1 : -1;
               if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                  this.applyEquipOffset(matrices, lv, equipProgress);
                  matrices.translate((float)k * -0.4785682F, -0.094387F, 0.05731531F);
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)k * 65.3F));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)k * -9.785F));
                  l = (float)item.getMaxUseTime() - ((float)this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                  m = l / (float)CrossbowItem.getPullTime(item);
                  if (m > 1.0F) {
                     m = 1.0F;
                  }

                  if (m > 0.1F) {
                     n = MathHelper.sin((l - 0.1F) * 1.3F);
                     o = m - 0.1F;
                     float p = n * o;
                     matrices.translate(p * 0.0F, p * 0.004F, p * 0.0F);
                  }

                  matrices.translate(m * 0.0F, m * 0.0F, m * 0.04F);
                  matrices.scale(1.0F, 1.0F, 1.0F + m * 0.2F);
                  matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)k * 45.0F));
               } else {
                  l = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                  m = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
                  n = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
                  matrices.translate((float)k * l, m, n);
                  this.applyEquipOffset(matrices, lv, equipProgress);
                  this.applySwingOffset(matrices, lv, swingProgress);
                  if (bl2 && swingProgress < 0.001F && bl) {
                     matrices.translate((float)k * -0.641864F, 0.0F, 0.0F);
                     matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)k * 10.0F));
                  }
               }

               this.renderItem(player, item, bl3 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl3, matrices, vertexConsumers, light);
            } else {
               bl2 = lv == Arm.RIGHT;
               int q;
               float r;
               if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                  q = bl2 ? 1 : -1;
                  switch (item.getUseAction()) {
                     case NONE:
                        this.applyEquipOffset(matrices, lv, equipProgress);
                        break;
                     case EAT:
                     case DRINK:
                        this.applyEatOrDrinkTransformation(matrices, tickDelta, lv, item);
                        this.applyEquipOffset(matrices, lv, equipProgress);
                        break;
                     case BLOCK:
                        this.applyEquipOffset(matrices, lv, equipProgress);
                        break;
                     case BOW:
                        this.applyEquipOffset(matrices, lv, equipProgress);
                        matrices.translate((float)q * -0.2785682F, 0.18344387F, 0.15731531F);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)q * 35.3F));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)q * -9.785F));
                        r = (float)item.getMaxUseTime() - ((float)this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                        l = r / 20.0F;
                        l = (l * l + l * 2.0F) / 3.0F;
                        if (l > 1.0F) {
                           l = 1.0F;
                        }

                        if (l > 0.1F) {
                           m = MathHelper.sin((r - 0.1F) * 1.3F);
                           n = l - 0.1F;
                           o = m * n;
                           matrices.translate(o * 0.0F, o * 0.004F, o * 0.0F);
                        }

                        matrices.translate(l * 0.0F, l * 0.0F, l * 0.04F);
                        matrices.scale(1.0F, 1.0F, 1.0F + l * 0.2F);
                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)q * 45.0F));
                        break;
                     case SPEAR:
                        this.applyEquipOffset(matrices, lv, equipProgress);
                        matrices.translate((float)q * -0.5F, 0.7F, 0.1F);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)q * 35.3F));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)q * -9.785F));
                        r = (float)item.getMaxUseTime() - ((float)this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                        l = r / 10.0F;
                        if (l > 1.0F) {
                           l = 1.0F;
                        }

                        if (l > 0.1F) {
                           m = MathHelper.sin((r - 0.1F) * 1.3F);
                           n = l - 0.1F;
                           o = m * n;
                           matrices.translate(o * 0.0F, o * 0.004F, o * 0.0F);
                        }

                        matrices.translate(0.0F, 0.0F, l * 0.2F);
                        matrices.scale(1.0F, 1.0F, 1.0F + l * 0.2F);
                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)q * 45.0F));
                        break;
                     case BRUSH:
                        this.applyBrushTransformation(matrices, tickDelta, lv, item, equipProgress);
                  }
               } else if (player.isUsingRiptide()) {
                  this.applyEquipOffset(matrices, lv, equipProgress);
                  q = bl2 ? 1 : -1;
                  matrices.translate((float)q * -0.4F, 0.8F, 0.3F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)q * 65.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)q * -85.0F));
               } else {
                  float s = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                  r = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
                  l = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
                  int t = bl2 ? 1 : -1;
                  matrices.translate((float)t * s, r, l);
                  this.applyEquipOffset(matrices, lv, equipProgress);
                  this.applySwingOffset(matrices, lv, swingProgress);
               }

               this.renderItem(player, item, bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl2, matrices, vertexConsumers, light);
            }
         }

         matrices.pop();
      }
   }

   public void updateHeldItems() {
      this.prevEquipProgressMainHand = this.equipProgressMainHand;
      this.prevEquipProgressOffHand = this.equipProgressOffHand;
      ClientPlayerEntity lv = this.client.player;
      ItemStack lv2 = lv.getMainHandStack();
      ItemStack lv3 = lv.getOffHandStack();
      if (ItemStack.areEqual(this.mainHand, lv2)) {
         this.mainHand = lv2;
      }

      if (ItemStack.areEqual(this.offHand, lv3)) {
         this.offHand = lv3;
      }

      if (lv.isRiding()) {
         this.equipProgressMainHand = MathHelper.clamp(this.equipProgressMainHand - 0.4F, 0.0F, 1.0F);
         this.equipProgressOffHand = MathHelper.clamp(this.equipProgressOffHand - 0.4F, 0.0F, 1.0F);
      } else {
         float f = lv.getAttackCooldownProgress(1.0F);
         this.equipProgressMainHand += MathHelper.clamp((this.mainHand == lv2 ? f * f * f : 0.0F) - this.equipProgressMainHand, -0.4F, 0.4F);
         this.equipProgressOffHand += MathHelper.clamp((float)(this.offHand == lv3 ? 1 : 0) - this.equipProgressOffHand, -0.4F, 0.4F);
      }

      if (this.equipProgressMainHand < 0.1F) {
         this.mainHand = lv2;
      }

      if (this.equipProgressOffHand < 0.1F) {
         this.offHand = lv3;
      }

   }

   public void resetEquipProgress(Hand hand) {
      if (hand == Hand.MAIN_HAND) {
         this.equipProgressMainHand = 0.0F;
      } else {
         this.equipProgressOffHand = 0.0F;
      }

   }

   @Environment(EnvType.CLIENT)
   @VisibleForTesting
   static enum HandRenderType {
      RENDER_BOTH_HANDS(true, true),
      RENDER_MAIN_HAND_ONLY(true, false),
      RENDER_OFF_HAND_ONLY(false, true);

      final boolean renderMainHand;
      final boolean renderOffHand;

      private HandRenderType(boolean renderMainHand, boolean renderOffHand) {
         this.renderMainHand = renderMainHand;
         this.renderOffHand = renderOffHand;
      }

      public static HandRenderType shouldOnlyRender(Hand hand) {
         return hand == Hand.MAIN_HAND ? RENDER_MAIN_HAND_ONLY : RENDER_OFF_HAND_ONLY;
      }

      // $FF: synthetic method
      private static HandRenderType[] method_36915() {
         return new HandRenderType[]{RENDER_BOTH_HANDS, RENDER_MAIN_HAND_ONLY, RENDER_OFF_HAND_ONLY};
      }
   }
}
