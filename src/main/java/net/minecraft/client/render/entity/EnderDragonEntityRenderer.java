package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class EnderDragonEntityRenderer extends EntityRenderer {
   public static final Identifier CRYSTAL_BEAM_TEXTURE = new Identifier("textures/entity/end_crystal/end_crystal_beam.png");
   private static final Identifier EXPLOSION_TEXTURE = new Identifier("textures/entity/enderdragon/dragon_exploding.png");
   private static final Identifier TEXTURE = new Identifier("textures/entity/enderdragon/dragon.png");
   private static final Identifier EYE_TEXTURE = new Identifier("textures/entity/enderdragon/dragon_eyes.png");
   private static final RenderLayer DRAGON_CUTOUT;
   private static final RenderLayer DRAGON_DECAL;
   private static final RenderLayer DRAGON_EYES;
   private static final RenderLayer CRYSTAL_BEAM_LAYER;
   private static final float HALF_SQRT_3;
   private final DragonEntityModel model;

   public EnderDragonEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
      this.shadowRadius = 0.5F;
      this.model = new DragonEntityModel(arg.getPart(EntityModelLayers.ENDER_DRAGON));
   }

   public void render(EnderDragonEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      arg2.push();
      float h = (float)arg.getSegmentProperties(7, g)[0];
      float j = (float)(arg.getSegmentProperties(5, g)[1] - arg.getSegmentProperties(10, g)[1]);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-h));
      arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(j * 10.0F));
      arg2.translate(0.0F, 0.0F, 1.0F);
      arg2.scale(-1.0F, -1.0F, 1.0F);
      arg2.translate(0.0F, -1.501F, 0.0F);
      boolean bl = arg.hurtTime > 0;
      this.model.animateModel(arg, 0.0F, 0.0F, g);
      VertexConsumer lv3;
      if (arg.ticksSinceDeath > 0) {
         float k = (float)arg.ticksSinceDeath / 200.0F;
         VertexConsumer lv = arg3.getBuffer(RenderLayer.getEntityAlpha(EXPLOSION_TEXTURE));
         this.model.render(arg2, lv, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, k);
         VertexConsumer lv2 = arg3.getBuffer(DRAGON_DECAL);
         this.model.render(arg2, lv2, i, OverlayTexture.getUv(0.0F, bl), 1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         lv3 = arg3.getBuffer(DRAGON_CUTOUT);
         this.model.render(arg2, lv3, i, OverlayTexture.getUv(0.0F, bl), 1.0F, 1.0F, 1.0F, 1.0F);
      }

      lv3 = arg3.getBuffer(DRAGON_EYES);
      this.model.render(arg2, lv3, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
      float l;
      float m;
      if (arg.ticksSinceDeath > 0) {
         l = ((float)arg.ticksSinceDeath + g) / 200.0F;
         m = Math.min(l > 0.8F ? (l - 0.8F) / 0.2F : 0.0F, 1.0F);
         Random lv4 = Random.create(432L);
         VertexConsumer lv5 = arg3.getBuffer(RenderLayer.getLightning());
         arg2.push();
         arg2.translate(0.0F, -1.0F, -2.0F);

         for(int n = 0; (float)n < (l + l * l) / 2.0F * 60.0F; ++n) {
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(lv4.nextFloat() * 360.0F));
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lv4.nextFloat() * 360.0F));
            arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(lv4.nextFloat() * 360.0F));
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(lv4.nextFloat() * 360.0F));
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lv4.nextFloat() * 360.0F));
            arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(lv4.nextFloat() * 360.0F + l * 90.0F));
            float o = lv4.nextFloat() * 20.0F + 5.0F + m * 10.0F;
            float p = lv4.nextFloat() * 2.0F + 1.0F + m * 2.0F;
            Matrix4f matrix4f = arg2.peek().getPositionMatrix();
            int q = (int)(255.0F * (1.0F - m));
            putDeathLightSourceVertex(lv5, matrix4f, q);
            putDeathLightNegativeXTerminalVertex(lv5, matrix4f, o, p);
            putDeathLightPositiveXTerminalVertex(lv5, matrix4f, o, p);
            putDeathLightSourceVertex(lv5, matrix4f, q);
            putDeathLightPositiveXTerminalVertex(lv5, matrix4f, o, p);
            putDeathLightPositiveZTerminalVertex(lv5, matrix4f, o, p);
            putDeathLightSourceVertex(lv5, matrix4f, q);
            putDeathLightPositiveZTerminalVertex(lv5, matrix4f, o, p);
            putDeathLightNegativeXTerminalVertex(lv5, matrix4f, o, p);
         }

         arg2.pop();
      }

      arg2.pop();
      if (arg.connectedCrystal != null) {
         arg2.push();
         l = (float)(arg.connectedCrystal.getX() - MathHelper.lerp((double)g, arg.prevX, arg.getX()));
         m = (float)(arg.connectedCrystal.getY() - MathHelper.lerp((double)g, arg.prevY, arg.getY()));
         float r = (float)(arg.connectedCrystal.getZ() - MathHelper.lerp((double)g, arg.prevZ, arg.getZ()));
         renderCrystalBeam(l, m + EndCrystalEntityRenderer.getYOffset(arg.connectedCrystal, g), r, g, arg.age, arg2, arg3, i);
         arg2.pop();
      }

      super.render(arg, f, g, arg2, arg3, i);
   }

   private static void putDeathLightSourceVertex(VertexConsumer buffer, Matrix4f matrix, int alpha) {
      buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).color(255, 255, 255, alpha).next();
   }

   private static void putDeathLightNegativeXTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width) {
      buffer.vertex(matrix, -HALF_SQRT_3 * width, radius, -0.5F * width).color(255, 0, 255, 0).next();
   }

   private static void putDeathLightPositiveXTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width) {
      buffer.vertex(matrix, HALF_SQRT_3 * width, radius, -0.5F * width).color(255, 0, 255, 0).next();
   }

   private static void putDeathLightPositiveZTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width) {
      buffer.vertex(matrix, 0.0F, radius, 1.0F * width).color(255, 0, 255, 0).next();
   }

   public static void renderCrystalBeam(float dx, float dy, float dz, float tickDelta, int age, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      float l = MathHelper.sqrt(dx * dx + dz * dz);
      float m = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
      matrices.push();
      matrices.translate(0.0F, 2.0F, 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float)(-Math.atan2((double)dz, (double)dx)) - 1.5707964F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotation((float)(-Math.atan2((double)l, (double)dy)) - 1.5707964F));
      VertexConsumer lv = vertexConsumers.getBuffer(CRYSTAL_BEAM_LAYER);
      float n = 0.0F - ((float)age + tickDelta) * 0.01F;
      float o = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) / 32.0F - ((float)age + tickDelta) * 0.01F;
      int p = true;
      float q = 0.0F;
      float r = 0.75F;
      float s = 0.0F;
      MatrixStack.Entry lv2 = matrices.peek();
      Matrix4f matrix4f = lv2.getPositionMatrix();
      Matrix3f matrix3f = lv2.getNormalMatrix();

      for(int t = 1; t <= 8; ++t) {
         float u = MathHelper.sin((float)t * 6.2831855F / 8.0F) * 0.75F;
         float v = MathHelper.cos((float)t * 6.2831855F / 8.0F) * 0.75F;
         float w = (float)t / 8.0F;
         lv.vertex(matrix4f, q * 0.2F, r * 0.2F, 0.0F).color(0, 0, 0, 255).texture(s, n).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).next();
         lv.vertex(matrix4f, q, r, m).color(255, 255, 255, 255).texture(s, o).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).next();
         lv.vertex(matrix4f, u, v, m).color(255, 255, 255, 255).texture(w, o).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).next();
         lv.vertex(matrix4f, u * 0.2F, v * 0.2F, 0.0F).color(0, 0, 0, 255).texture(w, n).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).next();
         q = u;
         r = v;
         s = w;
      }

      matrices.pop();
   }

   public Identifier getTexture(EnderDragonEntity arg) {
      return TEXTURE;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      float f = -16.0F;
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().cuboid("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, 176, 44).cuboid("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, 112, 30).mirrored().cuboid("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0).cuboid("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0).mirrored().cuboid("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0).cuboid("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0), ModelTransform.NONE);
      lv3.addChild(EntityModelPartNames.JAW, ModelPartBuilder.create().cuboid(EntityModelPartNames.JAW, -6.0F, 0.0F, -16.0F, 12, 4, 16, 176, 65), ModelTransform.pivot(0.0F, 4.0F, -8.0F));
      lv2.addChild(EntityModelPartNames.NECK, ModelPartBuilder.create().cuboid("box", -5.0F, -5.0F, -5.0F, 10, 10, 10, 192, 104).cuboid("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6, 48, 0), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().cuboid(EntityModelPartNames.BODY, -12.0F, 0.0F, -16.0F, 24, 24, 64, 0, 0).cuboid("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12, 220, 53).cuboid("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12, 220, 53).cuboid("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12, 220, 53), ModelTransform.pivot(0.0F, 4.0F, 8.0F));
      ModelPartData lv4 = lv2.addChild(EntityModelPartNames.LEFT_WING, ModelPartBuilder.create().mirrored().cuboid(EntityModelPartNames.BONE, 0.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88).cuboid("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88), ModelTransform.pivot(12.0F, 5.0F, 2.0F));
      lv4.addChild(EntityModelPartNames.LEFT_WING_TIP, ModelPartBuilder.create().mirrored().cuboid(EntityModelPartNames.BONE, 0.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136).cuboid("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144), ModelTransform.pivot(56.0F, 0.0F, 0.0F));
      ModelPartData lv5 = lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().cuboid("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, 112, 104), ModelTransform.pivot(12.0F, 20.0F, 2.0F));
      ModelPartData lv6 = lv5.addChild(EntityModelPartNames.LEFT_FRONT_LEG_TIP, ModelPartBuilder.create().cuboid("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, 226, 138), ModelTransform.pivot(0.0F, 20.0F, -1.0F));
      lv6.addChild(EntityModelPartNames.LEFT_FRONT_FOOT, ModelPartBuilder.create().cuboid("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, 144, 104), ModelTransform.pivot(0.0F, 23.0F, 0.0F));
      ModelPartData lv7 = lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().cuboid("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, 0, 0), ModelTransform.pivot(16.0F, 16.0F, 42.0F));
      ModelPartData lv8 = lv7.addChild(EntityModelPartNames.LEFT_HIND_LEG_TIP, ModelPartBuilder.create().cuboid("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, 196, 0), ModelTransform.pivot(0.0F, 32.0F, -4.0F));
      lv8.addChild(EntityModelPartNames.LEFT_HIND_FOOT, ModelPartBuilder.create().cuboid("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, 112, 0), ModelTransform.pivot(0.0F, 31.0F, 4.0F));
      ModelPartData lv9 = lv2.addChild(EntityModelPartNames.RIGHT_WING, ModelPartBuilder.create().cuboid(EntityModelPartNames.BONE, -56.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88).cuboid("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88), ModelTransform.pivot(-12.0F, 5.0F, 2.0F));
      lv9.addChild(EntityModelPartNames.RIGHT_WING_TIP, ModelPartBuilder.create().cuboid(EntityModelPartNames.BONE, -56.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136).cuboid("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144), ModelTransform.pivot(-56.0F, 0.0F, 0.0F));
      ModelPartData lv10 = lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().cuboid("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, 112, 104), ModelTransform.pivot(-12.0F, 20.0F, 2.0F));
      ModelPartData lv11 = lv10.addChild(EntityModelPartNames.RIGHT_FRONT_LEG_TIP, ModelPartBuilder.create().cuboid("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, 226, 138), ModelTransform.pivot(0.0F, 20.0F, -1.0F));
      lv11.addChild(EntityModelPartNames.RIGHT_FRONT_FOOT, ModelPartBuilder.create().cuboid("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, 144, 104), ModelTransform.pivot(0.0F, 23.0F, 0.0F));
      ModelPartData lv12 = lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().cuboid("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, 0, 0), ModelTransform.pivot(-16.0F, 16.0F, 42.0F));
      ModelPartData lv13 = lv12.addChild(EntityModelPartNames.RIGHT_HIND_LEG_TIP, ModelPartBuilder.create().cuboid("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, 196, 0), ModelTransform.pivot(0.0F, 32.0F, -4.0F));
      lv13.addChild(EntityModelPartNames.RIGHT_HIND_FOOT, ModelPartBuilder.create().cuboid("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, 112, 0), ModelTransform.pivot(0.0F, 31.0F, 4.0F));
      return TexturedModelData.of(lv, 256, 256);
   }

   static {
      DRAGON_CUTOUT = RenderLayer.getEntityCutoutNoCull(TEXTURE);
      DRAGON_DECAL = RenderLayer.getEntityDecal(TEXTURE);
      DRAGON_EYES = RenderLayer.getEyes(EYE_TEXTURE);
      CRYSTAL_BEAM_LAYER = RenderLayer.getEntitySmoothCutout(CRYSTAL_BEAM_TEXTURE);
      HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
   }

   @Environment(EnvType.CLIENT)
   public static class DragonEntityModel extends EntityModel {
      private final ModelPart head;
      private final ModelPart neck;
      private final ModelPart jaw;
      private final ModelPart body;
      private final ModelPart leftWing;
      private final ModelPart leftWingTip;
      private final ModelPart leftFrontLeg;
      private final ModelPart leftFrontLegTip;
      private final ModelPart leftFrontFoot;
      private final ModelPart leftHindLeg;
      private final ModelPart leftHindLegTip;
      private final ModelPart leftHindFoot;
      private final ModelPart rightWing;
      private final ModelPart rightWingTip;
      private final ModelPart rightFrontLeg;
      private final ModelPart rightFrontLegTip;
      private final ModelPart rightFrontFoot;
      private final ModelPart rightHindLeg;
      private final ModelPart rightHindLegTip;
      private final ModelPart rightHindFoot;
      @Nullable
      private EnderDragonEntity dragon;
      private float tickDelta;

      public DragonEntityModel(ModelPart part) {
         this.head = part.getChild(EntityModelPartNames.HEAD);
         this.jaw = this.head.getChild(EntityModelPartNames.JAW);
         this.neck = part.getChild(EntityModelPartNames.NECK);
         this.body = part.getChild(EntityModelPartNames.BODY);
         this.leftWing = part.getChild(EntityModelPartNames.LEFT_WING);
         this.leftWingTip = this.leftWing.getChild(EntityModelPartNames.LEFT_WING_TIP);
         this.leftFrontLeg = part.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
         this.leftFrontLegTip = this.leftFrontLeg.getChild(EntityModelPartNames.LEFT_FRONT_LEG_TIP);
         this.leftFrontFoot = this.leftFrontLegTip.getChild(EntityModelPartNames.LEFT_FRONT_FOOT);
         this.leftHindLeg = part.getChild(EntityModelPartNames.LEFT_HIND_LEG);
         this.leftHindLegTip = this.leftHindLeg.getChild(EntityModelPartNames.LEFT_HIND_LEG_TIP);
         this.leftHindFoot = this.leftHindLegTip.getChild(EntityModelPartNames.LEFT_HIND_FOOT);
         this.rightWing = part.getChild(EntityModelPartNames.RIGHT_WING);
         this.rightWingTip = this.rightWing.getChild(EntityModelPartNames.RIGHT_WING_TIP);
         this.rightFrontLeg = part.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
         this.rightFrontLegTip = this.rightFrontLeg.getChild(EntityModelPartNames.RIGHT_FRONT_LEG_TIP);
         this.rightFrontFoot = this.rightFrontLegTip.getChild(EntityModelPartNames.RIGHT_FRONT_FOOT);
         this.rightHindLeg = part.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
         this.rightHindLegTip = this.rightHindLeg.getChild(EntityModelPartNames.RIGHT_HIND_LEG_TIP);
         this.rightHindFoot = this.rightHindLegTip.getChild(EntityModelPartNames.RIGHT_HIND_FOOT);
      }

      public void animateModel(EnderDragonEntity arg, float f, float g, float h) {
         this.dragon = arg;
         this.tickDelta = h;
      }

      public void setAngles(EnderDragonEntity arg, float f, float g, float h, float i, float j) {
      }

      public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
         matrices.push();
         float l = MathHelper.lerp(this.tickDelta, this.dragon.prevWingPosition, this.dragon.wingPosition);
         this.jaw.pitch = (float)(Math.sin((double)(l * 6.2831855F)) + 1.0) * 0.2F;
         float m = (float)(Math.sin((double)(l * 6.2831855F - 1.0F)) + 1.0);
         m = (m * m + m * 2.0F) * 0.05F;
         matrices.translate(0.0F, m - 2.0F, -3.0F);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m * 2.0F));
         float n = 0.0F;
         float o = 20.0F;
         float p = -12.0F;
         float q = 1.5F;
         double[] ds = this.dragon.getSegmentProperties(6, this.tickDelta);
         float r = MathHelper.wrapDegrees((float)(this.dragon.getSegmentProperties(5, this.tickDelta)[0] - this.dragon.getSegmentProperties(10, this.tickDelta)[0]));
         float s = MathHelper.wrapDegrees((float)(this.dragon.getSegmentProperties(5, this.tickDelta)[0] + (double)(r / 2.0F)));
         float t = l * 6.2831855F;

         float v;
         for(int u = 0; u < 5; ++u) {
            double[] es = this.dragon.getSegmentProperties(5 - u, this.tickDelta);
            v = (float)Math.cos((double)((float)u * 0.45F + t)) * 0.15F;
            this.neck.yaw = MathHelper.wrapDegrees((float)(es[0] - ds[0])) * 0.017453292F * 1.5F;
            this.neck.pitch = v + this.dragon.getChangeInNeckPitch(u, ds, es) * 0.017453292F * 1.5F * 5.0F;
            this.neck.roll = -MathHelper.wrapDegrees((float)(es[0] - (double)s)) * 0.017453292F * 1.5F;
            this.neck.pivotY = o;
            this.neck.pivotZ = p;
            this.neck.pivotX = n;
            o += MathHelper.sin(this.neck.pitch) * 10.0F;
            p -= MathHelper.cos(this.neck.yaw) * MathHelper.cos(this.neck.pitch) * 10.0F;
            n -= MathHelper.sin(this.neck.yaw) * MathHelper.cos(this.neck.pitch) * 10.0F;
            this.neck.render(matrices, vertices, light, overlay, 1.0F, 1.0F, 1.0F, alpha);
         }

         this.head.pivotY = o;
         this.head.pivotZ = p;
         this.head.pivotX = n;
         double[] fs = this.dragon.getSegmentProperties(0, this.tickDelta);
         this.head.yaw = MathHelper.wrapDegrees((float)(fs[0] - ds[0])) * 0.017453292F;
         this.head.pitch = MathHelper.wrapDegrees(this.dragon.getChangeInNeckPitch(6, ds, fs)) * 0.017453292F * 1.5F * 5.0F;
         this.head.roll = -MathHelper.wrapDegrees((float)(fs[0] - (double)s)) * 0.017453292F;
         this.head.render(matrices, vertices, light, overlay, 1.0F, 1.0F, 1.0F, alpha);
         matrices.push();
         matrices.translate(0.0F, 1.0F, 0.0F);
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-r * 1.5F));
         matrices.translate(0.0F, -1.0F, 0.0F);
         this.body.roll = 0.0F;
         this.body.render(matrices, vertices, light, overlay, 1.0F, 1.0F, 1.0F, alpha);
         float w = l * 6.2831855F;
         this.leftWing.pitch = 0.125F - (float)Math.cos((double)w) * 0.2F;
         this.leftWing.yaw = -0.25F;
         this.leftWing.roll = -((float)(Math.sin((double)w) + 0.125)) * 0.8F;
         this.leftWingTip.roll = (float)(Math.sin((double)(w + 2.0F)) + 0.5) * 0.75F;
         this.rightWing.pitch = this.leftWing.pitch;
         this.rightWing.yaw = -this.leftWing.yaw;
         this.rightWing.roll = -this.leftWing.roll;
         this.rightWingTip.roll = -this.leftWingTip.roll;
         this.setLimbRotation(matrices, vertices, light, overlay, m, this.leftWing, this.leftFrontLeg, this.leftFrontLegTip, this.leftFrontFoot, this.leftHindLeg, this.leftHindLegTip, this.leftHindFoot, alpha);
         this.setLimbRotation(matrices, vertices, light, overlay, m, this.rightWing, this.rightFrontLeg, this.rightFrontLegTip, this.rightFrontFoot, this.rightHindLeg, this.rightHindLegTip, this.rightHindFoot, alpha);
         matrices.pop();
         v = -MathHelper.sin(l * 6.2831855F) * 0.0F;
         t = l * 6.2831855F;
         o = 10.0F;
         p = 60.0F;
         n = 0.0F;
         ds = this.dragon.getSegmentProperties(11, this.tickDelta);

         for(int x = 0; x < 12; ++x) {
            fs = this.dragon.getSegmentProperties(12 + x, this.tickDelta);
            v += MathHelper.sin((float)x * 0.45F + t) * 0.05F;
            this.neck.yaw = (MathHelper.wrapDegrees((float)(fs[0] - ds[0])) * 1.5F + 180.0F) * 0.017453292F;
            this.neck.pitch = v + (float)(fs[1] - ds[1]) * 0.017453292F * 1.5F * 5.0F;
            this.neck.roll = MathHelper.wrapDegrees((float)(fs[0] - (double)s)) * 0.017453292F * 1.5F;
            this.neck.pivotY = o;
            this.neck.pivotZ = p;
            this.neck.pivotX = n;
            o += MathHelper.sin(this.neck.pitch) * 10.0F;
            p -= MathHelper.cos(this.neck.yaw) * MathHelper.cos(this.neck.pitch) * 10.0F;
            n -= MathHelper.sin(this.neck.yaw) * MathHelper.cos(this.neck.pitch) * 10.0F;
            this.neck.render(matrices, vertices, light, overlay, 1.0F, 1.0F, 1.0F, alpha);
         }

         matrices.pop();
      }

      private void setLimbRotation(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float offset, ModelPart wing, ModelPart frontLeg, ModelPart frontLegTip, ModelPart frontFoot, ModelPart hindLeg, ModelPart hindLegTip, ModelPart hindFoot, float alpha) {
         hindLeg.pitch = 1.0F + offset * 0.1F;
         hindLegTip.pitch = 0.5F + offset * 0.1F;
         hindFoot.pitch = 0.75F + offset * 0.1F;
         frontLeg.pitch = 1.3F + offset * 0.1F;
         frontLegTip.pitch = -0.5F - offset * 0.1F;
         frontFoot.pitch = 0.75F + offset * 0.1F;
         wing.render(matrices, vertices, light, overlay, 1.0F, 1.0F, 1.0F, alpha);
         frontLeg.render(matrices, vertices, light, overlay, 1.0F, 1.0F, 1.0F, alpha);
         hindLeg.render(matrices, vertices, light, overlay, 1.0F, 1.0F, 1.0F, alpha);
      }
   }
}
