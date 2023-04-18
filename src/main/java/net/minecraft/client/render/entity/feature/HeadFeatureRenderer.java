package net.minecraft.client.render.entity.feature;

import com.mojang.authlib.GameProfile;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class HeadFeatureRenderer extends FeatureRenderer {
   private final float scaleX;
   private final float scaleY;
   private final float scaleZ;
   private final Map headModels;
   private final HeldItemRenderer heldItemRenderer;

   public HeadFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader, HeldItemRenderer heldItemRenderer) {
      this(context, loader, 1.0F, 1.0F, 1.0F, heldItemRenderer);
   }

   public HeadFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader, float scaleX, float scaleY, float scaleZ, HeldItemRenderer heldItemRenderer) {
      super(context);
      this.scaleX = scaleX;
      this.scaleY = scaleY;
      this.scaleZ = scaleZ;
      this.headModels = SkullBlockEntityRenderer.getModels(loader);
      this.heldItemRenderer = heldItemRenderer;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LivingEntity arg3, float f, float g, float h, float j, float k, float l) {
      ItemStack lv = arg3.getEquippedStack(EquipmentSlot.HEAD);
      if (!lv.isEmpty()) {
         Item lv2 = lv.getItem();
         arg.push();
         arg.scale(this.scaleX, this.scaleY, this.scaleZ);
         boolean bl = arg3 instanceof VillagerEntity || arg3 instanceof ZombieVillagerEntity;
         float n;
         if (arg3.isBaby() && !(arg3 instanceof VillagerEntity)) {
            float m = 2.0F;
            n = 1.4F;
            arg.translate(0.0F, 0.03125F, 0.0F);
            arg.scale(0.7F, 0.7F, 0.7F);
            arg.translate(0.0F, 1.0F, 0.0F);
         }

         ((ModelWithHead)this.getContextModel()).getHead().rotate(arg);
         if (lv2 instanceof BlockItem && ((BlockItem)lv2).getBlock() instanceof AbstractSkullBlock) {
            n = 1.1875F;
            arg.scale(1.1875F, -1.1875F, -1.1875F);
            if (bl) {
               arg.translate(0.0F, 0.0625F, 0.0F);
            }

            GameProfile gameProfile = null;
            if (lv.hasNbt()) {
               NbtCompound lv3 = lv.getNbt();
               if (lv3.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
                  gameProfile = NbtHelper.toGameProfile(lv3.getCompound("SkullOwner"));
               }
            }

            arg.translate(-0.5, 0.0, -0.5);
            SkullBlock.SkullType lv4 = ((AbstractSkullBlock)((BlockItem)lv2).getBlock()).getSkullType();
            SkullBlockEntityModel lv5 = (SkullBlockEntityModel)this.headModels.get(lv4);
            RenderLayer lv6 = SkullBlockEntityRenderer.getRenderLayer(lv4, gameProfile);
            Entity var22 = arg3.getVehicle();
            LimbAnimator lv8;
            if (var22 instanceof LivingEntity) {
               LivingEntity lv7 = (LivingEntity)var22;
               lv8 = lv7.limbAnimator;
            } else {
               lv8 = arg3.limbAnimator;
            }

            float o = lv8.getPos(h);
            SkullBlockEntityRenderer.renderSkull((Direction)null, 180.0F, o, arg, arg2, i, lv5, lv6);
         } else {
            label60: {
               if (lv2 instanceof ArmorItem) {
                  ArmorItem lv9 = (ArmorItem)lv2;
                  if (lv9.getSlotType() == EquipmentSlot.HEAD) {
                     break label60;
                  }
               }

               translate(arg, bl);
               this.heldItemRenderer.renderItem(arg3, lv, ModelTransformationMode.HEAD, false, arg, arg2, i);
            }
         }

         arg.pop();
      }
   }

   public static void translate(MatrixStack matrices, boolean villager) {
      float f = 0.625F;
      matrices.translate(0.0F, -0.25F, 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
      matrices.scale(0.625F, -0.625F, -0.625F);
      if (villager) {
         matrices.translate(0.0F, 0.1875F, 0.0F);
      }

   }
}
