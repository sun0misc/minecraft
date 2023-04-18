package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class ConduitBlockEntityRenderer implements BlockEntityRenderer {
   public static final SpriteIdentifier BASE_TEXTURE;
   public static final SpriteIdentifier CAGE_TEXTURE;
   public static final SpriteIdentifier WIND_TEXTURE;
   public static final SpriteIdentifier WIND_VERTICAL_TEXTURE;
   public static final SpriteIdentifier OPEN_EYE_TEXTURE;
   public static final SpriteIdentifier CLOSED_EYE_TEXTURE;
   private final ModelPart conduitEye;
   private final ModelPart conduitWind;
   private final ModelPart conduitShell;
   private final ModelPart conduit;
   private final BlockEntityRenderDispatcher dispatcher;

   public ConduitBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      this.dispatcher = ctx.getRenderDispatcher();
      this.conduitEye = ctx.getLayerModelPart(EntityModelLayers.CONDUIT_EYE);
      this.conduitWind = ctx.getLayerModelPart(EntityModelLayers.CONDUIT_WIND);
      this.conduitShell = ctx.getLayerModelPart(EntityModelLayers.CONDUIT_SHELL);
      this.conduit = ctx.getLayerModelPart(EntityModelLayers.CONDUIT);
   }

   public static TexturedModelData getEyeTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("eye", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new Dilation(0.01F)), ModelTransform.NONE);
      return TexturedModelData.of(lv, 16, 16);
   }

   public static TexturedModelData getWindTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("wind", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 32);
   }

   public static TexturedModelData getShellTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("shell", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 32, 16);
   }

   public static TexturedModelData getPlainTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("shell", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 32, 16);
   }

   public void render(ConduitBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      float g = (float)arg.ticks + f;
      float h;
      if (!arg.isActive()) {
         h = arg.getRotation(0.0F);
         VertexConsumer lv = BASE_TEXTURE.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
         arg2.push();
         arg2.translate(0.5F, 0.5F, 0.5F);
         arg2.multiply((new Quaternionf()).rotationY(h * 0.017453292F));
         this.conduitShell.render(arg2, lv, i, j);
         arg2.pop();
      } else {
         h = arg.getRotation(f) * 57.295776F;
         float k = MathHelper.sin(g * 0.1F) / 2.0F + 0.5F;
         k += k * k;
         arg2.push();
         arg2.translate(0.5F, 0.3F + k * 0.2F, 0.5F);
         Vector3f vector3f = (new Vector3f(0.5F, 1.0F, 0.5F)).normalize();
         arg2.multiply((new Quaternionf()).rotationAxis(h * 0.017453292F, vector3f));
         this.conduit.render(arg2, CAGE_TEXTURE.getVertexConsumer(arg3, RenderLayer::getEntityCutoutNoCull), i, j);
         arg2.pop();
         int l = arg.ticks / 66 % 3;
         arg2.push();
         arg2.translate(0.5F, 0.5F, 0.5F);
         if (l == 1) {
            arg2.multiply((new Quaternionf()).rotationX(1.5707964F));
         } else if (l == 2) {
            arg2.multiply((new Quaternionf()).rotationZ(1.5707964F));
         }

         VertexConsumer lv2 = (l == 1 ? WIND_VERTICAL_TEXTURE : WIND_TEXTURE).getVertexConsumer(arg3, RenderLayer::getEntityCutoutNoCull);
         this.conduitWind.render(arg2, lv2, i, j);
         arg2.pop();
         arg2.push();
         arg2.translate(0.5F, 0.5F, 0.5F);
         arg2.scale(0.875F, 0.875F, 0.875F);
         arg2.multiply((new Quaternionf()).rotationXYZ(3.1415927F, 0.0F, 3.1415927F));
         this.conduitWind.render(arg2, lv2, i, j);
         arg2.pop();
         Camera lv3 = this.dispatcher.camera;
         arg2.push();
         arg2.translate(0.5F, 0.3F + k * 0.2F, 0.5F);
         arg2.scale(0.5F, 0.5F, 0.5F);
         float m = -lv3.getYaw();
         arg2.multiply((new Quaternionf()).rotationYXZ(m * 0.017453292F, lv3.getPitch() * 0.017453292F, 3.1415927F));
         float n = 1.3333334F;
         arg2.scale(1.3333334F, 1.3333334F, 1.3333334F);
         this.conduitEye.render(arg2, (arg.isEyeOpen() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE).getVertexConsumer(arg3, RenderLayer::getEntityCutoutNoCull), i, j);
         arg2.pop();
      }
   }

   static {
      BASE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/base"));
      CAGE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/cage"));
      WIND_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/wind"));
      WIND_VERTICAL_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/wind_vertical"));
      OPEN_EYE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/open_eye"));
      CLOSED_EYE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/closed_eye"));
   }
}
