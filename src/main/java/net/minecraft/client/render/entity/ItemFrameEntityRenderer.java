package net.minecraft.client.render.entity;

import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class ItemFrameEntityRenderer extends EntityRenderer {
   private static final ModelIdentifier NORMAL_FRAME = ModelIdentifier.ofVanilla("item_frame", "map=false");
   private static final ModelIdentifier MAP_FRAME = ModelIdentifier.ofVanilla("item_frame", "map=true");
   private static final ModelIdentifier GLOW_FRAME = ModelIdentifier.ofVanilla("glow_item_frame", "map=false");
   private static final ModelIdentifier MAP_GLOW_FRAME = ModelIdentifier.ofVanilla("glow_item_frame", "map=true");
   public static final int GLOW_FRAME_BLOCK_LIGHT = 5;
   public static final int field_32933 = 30;
   private final ItemRenderer itemRenderer;
   private final BlockRenderManager blockRenderManager;

   public ItemFrameEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
      this.itemRenderer = arg.getItemRenderer();
      this.blockRenderManager = arg.getBlockRenderManager();
   }

   protected int getBlockLight(ItemFrameEntity arg, BlockPos arg2) {
      return arg.getType() == EntityType.GLOW_ITEM_FRAME ? Math.max(5, super.getBlockLight(arg, arg2)) : super.getBlockLight(arg, arg2);
   }

   public void render(ItemFrameEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      super.render(arg, f, g, arg2, arg3, i);
      arg2.push();
      Direction lv = arg.getHorizontalFacing();
      Vec3d lv2 = this.getPositionOffset(arg, g);
      arg2.translate(-lv2.getX(), -lv2.getY(), -lv2.getZ());
      double d = 0.46875;
      arg2.translate((double)lv.getOffsetX() * 0.46875, (double)lv.getOffsetY() * 0.46875, (double)lv.getOffsetZ() * 0.46875);
      arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(arg.getPitch()));
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - arg.getYaw()));
      boolean bl = arg.isInvisible();
      ItemStack lv3 = arg.getHeldItemStack();
      if (!bl) {
         BakedModelManager lv4 = this.blockRenderManager.getModels().getModelManager();
         ModelIdentifier lv5 = this.getModelId(arg, lv3);
         arg2.push();
         arg2.translate(-0.5F, -0.5F, -0.5F);
         this.blockRenderManager.getModelRenderer().render(arg2.peek(), arg3.getBuffer(TexturedRenderLayers.getEntitySolid()), (BlockState)null, lv4.getModel(lv5), 1.0F, 1.0F, 1.0F, i, OverlayTexture.DEFAULT_UV);
         arg2.pop();
      }

      if (!lv3.isEmpty()) {
         OptionalInt optionalInt = arg.getMapId();
         if (bl) {
            arg2.translate(0.0F, 0.0F, 0.5F);
         } else {
            arg2.translate(0.0F, 0.0F, 0.4375F);
         }

         int j = optionalInt.isPresent() ? arg.getRotation() % 4 * 2 : arg.getRotation();
         arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)j * 360.0F / 8.0F));
         if (optionalInt.isPresent()) {
            arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
            float h = 0.0078125F;
            arg2.scale(0.0078125F, 0.0078125F, 0.0078125F);
            arg2.translate(-64.0F, -64.0F, 0.0F);
            MapState lv6 = FilledMapItem.getMapState(optionalInt.getAsInt(), arg.world);
            arg2.translate(0.0F, 0.0F, -1.0F);
            if (lv6 != null) {
               int k = this.getLight(arg, LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE | 210, i);
               MinecraftClient.getInstance().gameRenderer.getMapRenderer().draw(arg2, arg3, optionalInt.getAsInt(), lv6, true, k);
            }
         } else {
            int l = this.getLight(arg, LightmapTextureManager.MAX_LIGHT_COORDINATE, i);
            arg2.scale(0.5F, 0.5F, 0.5F);
            this.itemRenderer.renderItem(lv3, ModelTransformationMode.FIXED, l, OverlayTexture.DEFAULT_UV, arg2, arg3, arg.world, arg.getId());
         }
      }

      arg2.pop();
   }

   private int getLight(ItemFrameEntity itemFrame, int glowLight, int regularLight) {
      return itemFrame.getType() == EntityType.GLOW_ITEM_FRAME ? glowLight : regularLight;
   }

   private ModelIdentifier getModelId(ItemFrameEntity entity, ItemStack stack) {
      boolean bl = entity.getType() == EntityType.GLOW_ITEM_FRAME;
      if (stack.isOf(Items.FILLED_MAP)) {
         return bl ? MAP_GLOW_FRAME : MAP_FRAME;
      } else {
         return bl ? GLOW_FRAME : NORMAL_FRAME;
      }
   }

   public Vec3d getPositionOffset(ItemFrameEntity arg, float f) {
      return new Vec3d((double)((float)arg.getHorizontalFacing().getOffsetX() * 0.3F), -0.25, (double)((float)arg.getHorizontalFacing().getOffsetZ() * 0.3F));
   }

   public Identifier getTexture(ItemFrameEntity arg) {
      return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
   }

   protected boolean hasLabel(ItemFrameEntity arg) {
      if (MinecraftClient.isHudEnabled() && !arg.getHeldItemStack().isEmpty() && arg.getHeldItemStack().hasCustomName() && this.dispatcher.targetedEntity == arg) {
         double d = this.dispatcher.getSquaredDistanceToCamera(arg);
         float f = arg.isSneaky() ? 32.0F : 64.0F;
         return d < (double)(f * f);
      } else {
         return false;
      }
   }

   protected void renderLabelIfPresent(ItemFrameEntity arg, Text arg2, MatrixStack arg3, VertexConsumerProvider arg4, int i) {
      super.renderLabelIfPresent(arg, arg.getHeldItemStack().getName(), arg3, arg4, i);
   }
}
