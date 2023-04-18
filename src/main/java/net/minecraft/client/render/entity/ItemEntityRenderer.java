package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class ItemEntityRenderer extends EntityRenderer {
   private static final float field_32924 = 0.15F;
   private static final int MAX_COUNT_FOR_4_ITEMS_RENDERED = 48;
   private static final int MAX_COUNT_FOR_3_ITEMS_RENDERED = 32;
   private static final int MAX_COUNT_FOR_2_ITEMS_RENDERED = 16;
   private static final int MAX_COUNT_FOR_1_ITEM_RENDERED = 1;
   private static final float field_32929 = 0.0F;
   private static final float field_32930 = 0.0F;
   private static final float field_32931 = 0.09375F;
   private final ItemRenderer itemRenderer;
   private final Random random = Random.create();

   public ItemEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
      this.itemRenderer = arg.getItemRenderer();
      this.shadowRadius = 0.15F;
      this.shadowOpacity = 0.75F;
   }

   private int getRenderedAmount(ItemStack stack) {
      int i = 1;
      if (stack.getCount() > 48) {
         i = 5;
      } else if (stack.getCount() > 32) {
         i = 4;
      } else if (stack.getCount() > 16) {
         i = 3;
      } else if (stack.getCount() > 1) {
         i = 2;
      }

      return i;
   }

   public void render(ItemEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      arg2.push();
      ItemStack lv = arg.getStack();
      int j = lv.isEmpty() ? 187 : Item.getRawId(lv.getItem()) + lv.getDamage();
      this.random.setSeed((long)j);
      BakedModel lv2 = this.itemRenderer.getModel(lv, arg.world, (LivingEntity)null, arg.getId());
      boolean bl = lv2.hasDepth();
      int k = this.getRenderedAmount(lv);
      float h = 0.25F;
      float l = MathHelper.sin(((float)arg.getItemAge() + g) / 10.0F + arg.uniqueOffset) * 0.1F + 0.1F;
      float m = lv2.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y();
      arg2.translate(0.0F, l + 0.25F * m, 0.0F);
      float n = arg.getRotation(g);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotation(n));
      float o = lv2.getTransformation().ground.scale.x();
      float p = lv2.getTransformation().ground.scale.y();
      float q = lv2.getTransformation().ground.scale.z();
      float s;
      float t;
      if (!bl) {
         float r = -0.0F * (float)(k - 1) * 0.5F * o;
         s = -0.0F * (float)(k - 1) * 0.5F * p;
         t = -0.09375F * (float)(k - 1) * 0.5F * q;
         arg2.translate(r, s, t);
      }

      for(int u = 0; u < k; ++u) {
         arg2.push();
         if (u > 0) {
            if (bl) {
               s = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               t = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float v = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               arg2.translate(s, t, v);
            } else {
               s = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               t = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               arg2.translate(s, t, 0.0F);
            }
         }

         this.itemRenderer.renderItem(lv, ModelTransformationMode.GROUND, false, arg2, arg3, i, OverlayTexture.DEFAULT_UV, lv2);
         arg2.pop();
         if (!bl) {
            arg2.translate(0.0F * o, 0.0F * p, 0.09375F * q);
         }
      }

      arg2.pop();
      super.render(arg, f, g, arg2, arg3, i);
   }

   public Identifier getTexture(ItemEntity arg) {
      return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
   }
}
