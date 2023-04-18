package net.minecraft.client.render.block.entity;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.OrderedText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class SignBlockEntityRenderer implements BlockEntityRenderer {
   private static final String STICK = "stick";
   private static final int GLOWING_BLACK_COLOR = -988212;
   private static final int RENDER_DISTANCE = MathHelper.square(16);
   private static final float SCALE = 0.6666667F;
   private static final Vec3d TEXT_OFFSET = new Vec3d(0.0, 0.3333333432674408, 0.046666666865348816);
   private final Map typeToModel;
   private final TextRenderer textRenderer;

   public SignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      this.typeToModel = (Map)WoodType.stream().collect(ImmutableMap.toImmutableMap((signType) -> {
         return signType;
      }, (signType) -> {
         return new SignModel(ctx.getLayerModelPart(EntityModelLayers.createSign(signType)));
      }));
      this.textRenderer = ctx.getTextRenderer();
   }

   public void render(SignBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      BlockState lv = arg.getCachedState();
      AbstractSignBlock lv2 = (AbstractSignBlock)lv.getBlock();
      WoodType lv3 = AbstractSignBlock.getWoodType(lv2);
      SignModel lv4 = (SignModel)this.typeToModel.get(lv3);
      lv4.stick.visible = lv.getBlock() instanceof SignBlock;
      this.render(arg, arg2, arg3, i, j, lv, lv2, lv3, lv4);
   }

   public float getSignScale() {
      return 0.6666667F;
   }

   public float getTextScale() {
      return 0.6666667F;
   }

   void render(SignBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BlockState state, AbstractSignBlock block, WoodType woodType, Model model) {
      matrices.push();
      this.setAngles(matrices, -block.getRotationDegrees(state), state);
      this.renderSign(matrices, vertexConsumers, light, overlay, woodType, model);
      this.renderText(entity.getPos(), entity.getFrontText(), matrices, vertexConsumers, light, entity.getTextLineHeight(), entity.getMaxTextWidth(), true);
      this.renderText(entity.getPos(), entity.getBackText(), matrices, vertexConsumers, light, entity.getTextLineHeight(), entity.getMaxTextWidth(), false);
      matrices.pop();
   }

   void setAngles(MatrixStack matrices, float rotationDegrees, BlockState state) {
      matrices.translate(0.5F, 0.75F * this.getSignScale(), 0.5F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));
      if (!(state.getBlock() instanceof SignBlock)) {
         matrices.translate(0.0F, -0.3125F, -0.4375F);
      }

   }

   void renderSign(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, WoodType woodType, Model model) {
      matrices.push();
      float f = this.getSignScale();
      matrices.scale(f, -f, -f);
      SpriteIdentifier lv = this.getTextureId(woodType);
      Objects.requireNonNull(model);
      VertexConsumer lv2 = lv.getVertexConsumer(vertexConsumers, model::getLayer);
      this.renderSignModel(matrices, light, overlay, model, lv2);
      matrices.pop();
   }

   void renderSignModel(MatrixStack matrices, int light, int overlay, Model model, VertexConsumer vertexConsumers) {
      SignModel lv = (SignModel)model;
      lv.root.render(matrices, vertexConsumers, light, overlay);
   }

   SpriteIdentifier getTextureId(WoodType signType) {
      return TexturedRenderLayers.getSignTextureId(signType);
   }

   void renderText(BlockPos pos, SignText signText, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int lineHeight, int lineWidth, boolean front) {
      matrices.push();
      this.setTextAngles(matrices, front, this.getTextOffset());
      int l = getColor(signText);
      int m = 4 * lineHeight / 2;
      OrderedText[] lvs = signText.getOrderedMessages(MinecraftClient.getInstance().shouldFilterText(), (text) -> {
         List list = this.textRenderer.wrapLines(text, lineWidth);
         return list.isEmpty() ? OrderedText.EMPTY : (OrderedText)list.get(0);
      });
      int n;
      boolean bl2;
      int o;
      if (signText.isGlowing()) {
         n = signText.getColor().getSignColor();
         bl2 = shouldRender(pos, n);
         o = 15728880;
      } else {
         n = l;
         bl2 = false;
         o = light;
      }

      for(int p = 0; p < 4; ++p) {
         OrderedText lv = lvs[p];
         float f = (float)(-this.textRenderer.getWidth(lv) / 2);
         if (bl2) {
            this.textRenderer.drawWithOutline(lv, f, (float)(p * lineHeight - m), n, l, matrices.peek().getPositionMatrix(), vertexConsumers, o);
         } else {
            this.textRenderer.draw((OrderedText)lv, f, (float)(p * lineHeight - m), n, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, o);
         }
      }

      matrices.pop();
   }

   private void setTextAngles(MatrixStack matrices, boolean front, Vec3d translation) {
      if (!front) {
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
      }

      float f = 0.015625F * this.getTextScale();
      matrices.translate(translation.x, translation.y, translation.z);
      matrices.scale(f, -f, f);
   }

   Vec3d getTextOffset() {
      return TEXT_OFFSET;
   }

   static boolean shouldRender(BlockPos pos, int signColor) {
      if (signColor == DyeColor.BLACK.getSignColor()) {
         return true;
      } else {
         MinecraftClient lv = MinecraftClient.getInstance();
         ClientPlayerEntity lv2 = lv.player;
         if (lv2 != null && lv.options.getPerspective().isFirstPerson() && lv2.isUsingSpyglass()) {
            return true;
         } else {
            Entity lv3 = lv.getCameraEntity();
            return lv3 != null && lv3.squaredDistanceTo(Vec3d.ofCenter(pos)) < (double)RENDER_DISTANCE;
         }
      }
   }

   static int getColor(SignText sign) {
      int i = sign.getColor().getSignColor();
      if (i == DyeColor.BLACK.getSignColor() && sign.isGlowing()) {
         return -988212;
      } else {
         double d = 0.4;
         int j = (int)((double)ColorHelper.Argb.getRed(i) * 0.4);
         int k = (int)((double)ColorHelper.Argb.getGreen(i) * 0.4);
         int l = (int)((double)ColorHelper.Argb.getBlue(i) * 0.4);
         return ColorHelper.Argb.getArgb(0, j, k, l);
      }
   }

   public static SignModel createSignModel(EntityModelLoader entityModelLoader, WoodType type) {
      return new SignModel(entityModelLoader.getModelPart(EntityModelLayers.createSign(type)));
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("sign", ModelPartBuilder.create().uv(0, 0).cuboid(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), ModelTransform.NONE);
      lv2.addChild("stick", ModelPartBuilder.create().uv(0, 14).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 32);
   }

   @Environment(EnvType.CLIENT)
   public static final class SignModel extends Model {
      public final ModelPart root;
      public final ModelPart stick;

      public SignModel(ModelPart root) {
         super(RenderLayer::getEntityCutoutNoCull);
         this.root = root;
         this.stick = root.getChild("stick");
      }

      public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
         this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
      }
   }
}
