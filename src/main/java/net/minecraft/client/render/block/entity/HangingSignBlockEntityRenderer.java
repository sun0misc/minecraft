package net.minecraft.client.render.block.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class HangingSignBlockEntityRenderer extends SignBlockEntityRenderer {
   private static final String PLANK = "plank";
   private static final String V_CHAINS = "vChains";
   private static final String NORMAL_CHAINS = "normalChains";
   private static final String CHAIN_L1 = "chainL1";
   private static final String CHAIN_L2 = "chainL2";
   private static final String CHAIN_R1 = "chainR1";
   private static final String CHAIN_R2 = "chainR2";
   private static final String BOARD = "board";
   private static final float MODEL_SCALE = 1.0F;
   private static final float TEXT_SCALE = 0.9F;
   private static final Vec3d TEXT_OFFSET = new Vec3d(0.0, -0.3199999928474426, 0.0729999989271164);
   private final Map MODELS;

   public HangingSignBlockEntityRenderer(BlockEntityRendererFactory.Context arg) {
      super(arg);
      this.MODELS = (Map)WoodType.stream().collect(ImmutableMap.toImmutableMap((argx) -> {
         return argx;
      }, (type) -> {
         return new HangingSignModel(arg.getLayerModelPart(EntityModelLayers.createHangingSign(type)));
      }));
   }

   public float getSignScale() {
      return 1.0F;
   }

   public float getTextScale() {
      return 0.9F;
   }

   public void render(SignBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      BlockState lv = arg.getCachedState();
      AbstractSignBlock lv2 = (AbstractSignBlock)lv.getBlock();
      WoodType lv3 = AbstractSignBlock.getWoodType(lv2);
      HangingSignModel lv4 = (HangingSignModel)this.MODELS.get(lv3);
      lv4.updateVisibleParts(lv);
      this.render(arg, arg2, arg3, i, j, lv, lv2, lv3, lv4);
   }

   void setAngles(MatrixStack matrices, float rotationDegrees, BlockState state) {
      matrices.translate(0.5, 0.9375, 0.5);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));
      matrices.translate(0.0F, -0.3125F, 0.0F);
   }

   void renderSignModel(MatrixStack matrices, int light, int overlay, Model model, VertexConsumer vertexConsumers) {
      HangingSignModel lv = (HangingSignModel)model;
      lv.root.render(matrices, vertexConsumers, light, overlay);
   }

   SpriteIdentifier getTextureId(WoodType signType) {
      return TexturedRenderLayers.getHangingSignTextureId(signType);
   }

   Vec3d getTextOffset() {
      return TEXT_OFFSET;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("board", ModelPartBuilder.create().uv(0, 12).cuboid(-7.0F, 0.0F, -1.0F, 14.0F, 10.0F, 2.0F), ModelTransform.NONE);
      lv2.addChild("plank", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -6.0F, -2.0F, 16.0F, 2.0F, 4.0F), ModelTransform.NONE);
      ModelPartData lv3 = lv2.addChild("normalChains", ModelPartBuilder.create(), ModelTransform.NONE);
      lv3.addChild("chainL1", ModelPartBuilder.create().uv(0, 6).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F), ModelTransform.of(-5.0F, -6.0F, 0.0F, 0.0F, -0.7853982F, 0.0F));
      lv3.addChild("chainL2", ModelPartBuilder.create().uv(6, 6).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F), ModelTransform.of(-5.0F, -6.0F, 0.0F, 0.0F, 0.7853982F, 0.0F));
      lv3.addChild("chainR1", ModelPartBuilder.create().uv(0, 6).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F), ModelTransform.of(5.0F, -6.0F, 0.0F, 0.0F, -0.7853982F, 0.0F));
      lv3.addChild("chainR2", ModelPartBuilder.create().uv(6, 6).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F), ModelTransform.of(5.0F, -6.0F, 0.0F, 0.0F, 0.7853982F, 0.0F));
      lv2.addChild("vChains", ModelPartBuilder.create().uv(14, 6).cuboid(-6.0F, -6.0F, 0.0F, 12.0F, 6.0F, 0.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 32);
   }

   @Environment(EnvType.CLIENT)
   public static final class HangingSignModel extends Model {
      public final ModelPart root;
      public final ModelPart plank;
      public final ModelPart vChains;
      public final ModelPart normalChains;

      public HangingSignModel(ModelPart root) {
         super(RenderLayer::getEntityCutoutNoCull);
         this.root = root;
         this.plank = root.getChild("plank");
         this.normalChains = root.getChild("normalChains");
         this.vChains = root.getChild("vChains");
      }

      public void updateVisibleParts(BlockState state) {
         boolean bl = !(state.getBlock() instanceof HangingSignBlock);
         this.plank.visible = bl;
         this.vChains.visible = false;
         this.normalChains.visible = true;
         if (!bl) {
            boolean bl2 = (Boolean)state.get(Properties.ATTACHED);
            this.normalChains.visible = !bl2;
            this.vChains.visible = bl2;
         }

      }

      public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
         this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
      }
   }
}
