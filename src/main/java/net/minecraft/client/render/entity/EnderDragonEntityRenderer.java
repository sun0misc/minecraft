/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
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
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class EnderDragonEntityRenderer
extends EntityRenderer<EnderDragonEntity> {
    public static final Identifier CRYSTAL_BEAM_TEXTURE = Identifier.method_60656("textures/entity/end_crystal/end_crystal_beam.png");
    private static final Identifier EXPLOSION_TEXTURE = Identifier.method_60656("textures/entity/enderdragon/dragon_exploding.png");
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/enderdragon/dragon.png");
    private static final Identifier EYE_TEXTURE = Identifier.method_60656("textures/entity/enderdragon/dragon_eyes.png");
    private static final RenderLayer DRAGON_CUTOUT = RenderLayer.getEntityCutoutNoCull(TEXTURE);
    private static final RenderLayer DRAGON_DECAL = RenderLayer.getEntityDecal(TEXTURE);
    private static final RenderLayer DRAGON_EYES = RenderLayer.getEyes(EYE_TEXTURE);
    private static final RenderLayer CRYSTAL_BEAM_LAYER = RenderLayer.getEntitySmoothCutout(CRYSTAL_BEAM_TEXTURE);
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final DragonEntityModel model;

    public EnderDragonEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.shadowRadius = 0.5f;
        this.model = new DragonEntityModel(arg.getPart(EntityModelLayers.ENDER_DRAGON));
    }

    @Override
    public void render(EnderDragonEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        arg2.push();
        float h = (float)arg.getSegmentProperties(7, g)[0];
        float j = (float)(arg.getSegmentProperties(5, g)[1] - arg.getSegmentProperties(10, g)[1]);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-h));
        arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(j * 10.0f));
        arg2.translate(0.0f, 0.0f, 1.0f);
        arg2.scale(-1.0f, -1.0f, 1.0f);
        arg2.translate(0.0f, -1.501f, 0.0f);
        boolean bl = arg.hurtTime > 0;
        this.model.animateModel(arg, 0.0f, 0.0f, g);
        if (arg.ticksSinceDeath > 0) {
            float k = (float)arg.ticksSinceDeath / 200.0f;
            int l = ColorHelper.Argb.withAlpha(MathHelper.floor(k * 255.0f), -1);
            VertexConsumer lv = arg3.getBuffer(RenderLayer.getEntityAlpha(EXPLOSION_TEXTURE));
            this.model.render(arg2, lv, i, OverlayTexture.DEFAULT_UV, l);
            VertexConsumer lv2 = arg3.getBuffer(DRAGON_DECAL);
            this.model.method_60879(arg2, lv2, i, OverlayTexture.getUv(0.0f, bl));
        } else {
            VertexConsumer lv3 = arg3.getBuffer(DRAGON_CUTOUT);
            this.model.method_60879(arg2, lv3, i, OverlayTexture.getUv(0.0f, bl));
        }
        VertexConsumer lv3 = arg3.getBuffer(DRAGON_EYES);
        this.model.method_60879(arg2, lv3, i, OverlayTexture.DEFAULT_UV);
        if (arg.ticksSinceDeath > 0) {
            float m = ((float)arg.ticksSinceDeath + g) / 200.0f;
            float n = Math.min(m > 0.8f ? (m - 0.8f) / 0.2f : 0.0f, 1.0f);
            Random lv4 = Random.create(432L);
            VertexConsumer lv5 = arg3.getBuffer(RenderLayer.getLightning());
            arg2.push();
            arg2.translate(0.0f, -1.0f, -2.0f);
            int o = 0;
            while ((float)o < (m + m * m) / 2.0f * 60.0f) {
                arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(lv4.nextFloat() * 360.0f));
                arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lv4.nextFloat() * 360.0f));
                arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(lv4.nextFloat() * 360.0f));
                arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(lv4.nextFloat() * 360.0f));
                arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lv4.nextFloat() * 360.0f));
                arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(lv4.nextFloat() * 360.0f + m * 90.0f));
                float p = lv4.nextFloat() * 20.0f + 5.0f + n * 10.0f;
                float q = lv4.nextFloat() * 2.0f + 1.0f + n * 2.0f;
                Matrix4f matrix4f = arg2.peek().getPositionMatrix();
                int r = (int)(255.0f * (1.0f - n));
                EnderDragonEntityRenderer.putDeathLightSourceVertex(lv5, matrix4f, r);
                EnderDragonEntityRenderer.putDeathLightNegativeXTerminalVertex(lv5, matrix4f, p, q);
                EnderDragonEntityRenderer.putDeathLightPositiveXTerminalVertex(lv5, matrix4f, p, q);
                EnderDragonEntityRenderer.putDeathLightSourceVertex(lv5, matrix4f, r);
                EnderDragonEntityRenderer.putDeathLightPositiveXTerminalVertex(lv5, matrix4f, p, q);
                EnderDragonEntityRenderer.putDeathLightPositiveZTerminalVertex(lv5, matrix4f, p, q);
                EnderDragonEntityRenderer.putDeathLightSourceVertex(lv5, matrix4f, r);
                EnderDragonEntityRenderer.putDeathLightPositiveZTerminalVertex(lv5, matrix4f, p, q);
                EnderDragonEntityRenderer.putDeathLightNegativeXTerminalVertex(lv5, matrix4f, p, q);
                ++o;
            }
            arg2.pop();
        }
        arg2.pop();
        if (arg.connectedCrystal != null) {
            arg2.push();
            float m = (float)(arg.connectedCrystal.getX() - MathHelper.lerp((double)g, arg.prevX, arg.getX()));
            float n = (float)(arg.connectedCrystal.getY() - MathHelper.lerp((double)g, arg.prevY, arg.getY()));
            float s = (float)(arg.connectedCrystal.getZ() - MathHelper.lerp((double)g, arg.prevZ, arg.getZ()));
            EnderDragonEntityRenderer.renderCrystalBeam(m, n + EndCrystalEntityRenderer.getYOffset(arg.connectedCrystal, g), s, g, arg.age, arg2, arg3, i);
            arg2.pop();
        }
        super.render(arg, f, g, arg2, arg3, i);
    }

    private static void putDeathLightSourceVertex(VertexConsumer buffer, Matrix4f matrix, int alpha) {
        buffer.vertex(matrix, 0.0f, 0.0f, 0.0f).method_60832(alpha);
    }

    private static void putDeathLightNegativeXTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width) {
        buffer.vertex(matrix, -HALF_SQRT_3 * width, radius, -0.5f * width).color(0xFF00FF);
    }

    private static void putDeathLightPositiveXTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width) {
        buffer.vertex(matrix, HALF_SQRT_3 * width, radius, -0.5f * width).color(0xFF00FF);
    }

    private static void putDeathLightPositiveZTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width) {
        buffer.vertex(matrix, 0.0f, radius, 1.0f * width).color(0xFF00FF);
    }

    public static void renderCrystalBeam(float dx, float dy, float dz, float tickDelta, int age, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        float l = MathHelper.sqrt(dx * dx + dz * dz);
        float m = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        matrices.push();
        matrices.translate(0.0f, 2.0f, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float)(-Math.atan2(dz, dx)) - 1.5707964f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation((float)(-Math.atan2(l, dy)) - 1.5707964f));
        VertexConsumer lv = vertexConsumers.getBuffer(CRYSTAL_BEAM_LAYER);
        float n = 0.0f - ((float)age + tickDelta) * 0.01f;
        float o = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) / 32.0f - ((float)age + tickDelta) * 0.01f;
        int p = 8;
        float q = 0.0f;
        float r = 0.75f;
        float s = 0.0f;
        MatrixStack.Entry lv2 = matrices.peek();
        for (int t = 1; t <= 8; ++t) {
            float u = MathHelper.sin((float)t * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float v = MathHelper.cos((float)t * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float w = (float)t / 8.0f;
            lv.vertex(lv2, q * 0.2f, r * 0.2f, 0.0f).color(Colors.BLACK).texture(s, n).overlay(OverlayTexture.DEFAULT_UV).method_60803(light).method_60831(lv2, 0.0f, -1.0f, 0.0f);
            lv.vertex(lv2, q, r, m).color(Colors.WHITE).texture(s, o).overlay(OverlayTexture.DEFAULT_UV).method_60803(light).method_60831(lv2, 0.0f, -1.0f, 0.0f);
            lv.vertex(lv2, u, v, m).color(Colors.WHITE).texture(w, o).overlay(OverlayTexture.DEFAULT_UV).method_60803(light).method_60831(lv2, 0.0f, -1.0f, 0.0f);
            lv.vertex(lv2, u * 0.2f, v * 0.2f, 0.0f).color(Colors.BLACK).texture(w, n).overlay(OverlayTexture.DEFAULT_UV).method_60803(light).method_60831(lv2, 0.0f, -1.0f, 0.0f);
            q = u;
            r = v;
            s = w;
        }
        matrices.pop();
    }

    @Override
    public Identifier getTexture(EnderDragonEntity arg) {
        return TEXTURE;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        float f = -16.0f;
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().cuboid("upperlip", -6.0f, -1.0f, -24.0f, 12, 5, 16, 176, 44).cuboid("upperhead", -8.0f, -8.0f, -10.0f, 16, 16, 16, 112, 30).mirrored().cuboid("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).cuboid("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0).mirrored().cuboid("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).cuboid("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0), ModelTransform.NONE);
        lv3.addChild(EntityModelPartNames.JAW, ModelPartBuilder.create().cuboid(EntityModelPartNames.JAW, -6.0f, 0.0f, -16.0f, 12, 4, 16, 176, 65), ModelTransform.pivot(0.0f, 4.0f, -8.0f));
        lv2.addChild(EntityModelPartNames.NECK, ModelPartBuilder.create().cuboid("box", -5.0f, -5.0f, -5.0f, 10, 10, 10, 192, 104).cuboid("scale", -1.0f, -9.0f, -3.0f, 2, 4, 6, 48, 0), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().cuboid(EntityModelPartNames.BODY, -12.0f, 0.0f, -16.0f, 24, 24, 64, 0, 0).cuboid("scale", -1.0f, -6.0f, -10.0f, 2, 6, 12, 220, 53).cuboid("scale", -1.0f, -6.0f, 10.0f, 2, 6, 12, 220, 53).cuboid("scale", -1.0f, -6.0f, 30.0f, 2, 6, 12, 220, 53), ModelTransform.pivot(0.0f, 4.0f, 8.0f));
        ModelPartData lv4 = lv2.addChild(EntityModelPartNames.LEFT_WING, ModelPartBuilder.create().mirrored().cuboid(EntityModelPartNames.BONE, 0.0f, -4.0f, -4.0f, 56, 8, 8, 112, 88).cuboid("skin", 0.0f, 0.0f, 2.0f, 56, 0, 56, -56, 88), ModelTransform.pivot(12.0f, 5.0f, 2.0f));
        lv4.addChild(EntityModelPartNames.LEFT_WING_TIP, ModelPartBuilder.create().mirrored().cuboid(EntityModelPartNames.BONE, 0.0f, -2.0f, -2.0f, 56, 4, 4, 112, 136).cuboid("skin", 0.0f, 0.0f, 2.0f, 56, 0, 56, -56, 144), ModelTransform.pivot(56.0f, 0.0f, 0.0f));
        ModelPartData lv5 = lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().cuboid("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, 112, 104), ModelTransform.pivot(12.0f, 20.0f, 2.0f));
        ModelPartData lv6 = lv5.addChild(EntityModelPartNames.LEFT_FRONT_LEG_TIP, ModelPartBuilder.create().cuboid("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, 226, 138), ModelTransform.pivot(0.0f, 20.0f, -1.0f));
        lv6.addChild(EntityModelPartNames.LEFT_FRONT_FOOT, ModelPartBuilder.create().cuboid("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, 144, 104), ModelTransform.pivot(0.0f, 23.0f, 0.0f));
        ModelPartData lv7 = lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().cuboid("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, 0, 0), ModelTransform.pivot(16.0f, 16.0f, 42.0f));
        ModelPartData lv8 = lv7.addChild(EntityModelPartNames.LEFT_HIND_LEG_TIP, ModelPartBuilder.create().cuboid("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, 196, 0), ModelTransform.pivot(0.0f, 32.0f, -4.0f));
        lv8.addChild(EntityModelPartNames.LEFT_HIND_FOOT, ModelPartBuilder.create().cuboid("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, 112, 0), ModelTransform.pivot(0.0f, 31.0f, 4.0f));
        ModelPartData lv9 = lv2.addChild(EntityModelPartNames.RIGHT_WING, ModelPartBuilder.create().cuboid(EntityModelPartNames.BONE, -56.0f, -4.0f, -4.0f, 56, 8, 8, 112, 88).cuboid("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, -56, 88), ModelTransform.pivot(-12.0f, 5.0f, 2.0f));
        lv9.addChild(EntityModelPartNames.RIGHT_WING_TIP, ModelPartBuilder.create().cuboid(EntityModelPartNames.BONE, -56.0f, -2.0f, -2.0f, 56, 4, 4, 112, 136).cuboid("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, -56, 144), ModelTransform.pivot(-56.0f, 0.0f, 0.0f));
        ModelPartData lv10 = lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().cuboid("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, 112, 104), ModelTransform.pivot(-12.0f, 20.0f, 2.0f));
        ModelPartData lv11 = lv10.addChild(EntityModelPartNames.RIGHT_FRONT_LEG_TIP, ModelPartBuilder.create().cuboid("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, 226, 138), ModelTransform.pivot(0.0f, 20.0f, -1.0f));
        lv11.addChild(EntityModelPartNames.RIGHT_FRONT_FOOT, ModelPartBuilder.create().cuboid("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, 144, 104), ModelTransform.pivot(0.0f, 23.0f, 0.0f));
        ModelPartData lv12 = lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().cuboid("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, 0, 0), ModelTransform.pivot(-16.0f, 16.0f, 42.0f));
        ModelPartData lv13 = lv12.addChild(EntityModelPartNames.RIGHT_HIND_LEG_TIP, ModelPartBuilder.create().cuboid("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, 196, 0), ModelTransform.pivot(0.0f, 32.0f, -4.0f));
        lv13.addChild(EntityModelPartNames.RIGHT_HIND_FOOT, ModelPartBuilder.create().cuboid("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, 112, 0), ModelTransform.pivot(0.0f, 31.0f, 4.0f));
        return TexturedModelData.of(lv, 256, 256);
    }

    @Environment(value=EnvType.CLIENT)
    public static class DragonEntityModel
    extends EntityModel<EnderDragonEntity> {
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

        @Override
        public void animateModel(EnderDragonEntity arg, float f, float g, float h) {
            this.dragon = arg;
            this.tickDelta = h;
        }

        @Override
        public void setAngles(EnderDragonEntity arg, float f, float g, float h, float i, float j) {
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int k) {
            float s;
            matrices.push();
            float f = MathHelper.lerp(this.tickDelta, this.dragon.prevWingPosition, this.dragon.wingPosition);
            this.jaw.pitch = (float)(Math.sin(f * ((float)Math.PI * 2)) + 1.0) * 0.2f;
            float g = (float)(Math.sin(f * ((float)Math.PI * 2) - 1.0f) + 1.0);
            g = (g * g + g * 2.0f) * 0.05f;
            matrices.translate(0.0f, g - 2.0f, -3.0f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * 2.0f));
            float h = 0.0f;
            float l = 20.0f;
            float m = -12.0f;
            float n = 1.5f;
            double[] ds = this.dragon.getSegmentProperties(6, this.tickDelta);
            float o = MathHelper.wrapDegrees((float)(this.dragon.getSegmentProperties(5, this.tickDelta)[0] - this.dragon.getSegmentProperties(10, this.tickDelta)[0]));
            float p = MathHelper.wrapDegrees((float)(this.dragon.getSegmentProperties(5, this.tickDelta)[0] + (double)(o / 2.0f)));
            float q = f * ((float)Math.PI * 2);
            for (int r = 0; r < 5; ++r) {
                double[] es = this.dragon.getSegmentProperties(5 - r, this.tickDelta);
                s = (float)Math.cos((float)r * 0.45f + q) * 0.15f;
                this.neck.yaw = MathHelper.wrapDegrees((float)(es[0] - ds[0])) * ((float)Math.PI / 180) * 1.5f;
                this.neck.pitch = s + this.dragon.getChangeInNeckPitch(r, ds, es) * ((float)Math.PI / 180) * 1.5f * 5.0f;
                this.neck.roll = -MathHelper.wrapDegrees((float)(es[0] - (double)p)) * ((float)Math.PI / 180) * 1.5f;
                this.neck.pivotY = l;
                this.neck.pivotZ = m;
                this.neck.pivotX = h;
                l += MathHelper.sin(this.neck.pitch) * 10.0f;
                m -= MathHelper.cos(this.neck.yaw) * MathHelper.cos(this.neck.pitch) * 10.0f;
                h -= MathHelper.sin(this.neck.yaw) * MathHelper.cos(this.neck.pitch) * 10.0f;
                this.neck.render(matrices, vertices, light, overlay, k);
            }
            this.head.pivotY = l;
            this.head.pivotZ = m;
            this.head.pivotX = h;
            double[] fs = this.dragon.getSegmentProperties(0, this.tickDelta);
            this.head.yaw = MathHelper.wrapDegrees((float)(fs[0] - ds[0])) * ((float)Math.PI / 180);
            this.head.pitch = MathHelper.wrapDegrees(this.dragon.getChangeInNeckPitch(6, ds, fs)) * ((float)Math.PI / 180) * 1.5f * 5.0f;
            this.head.roll = -MathHelper.wrapDegrees((float)(fs[0] - (double)p)) * ((float)Math.PI / 180);
            this.head.render(matrices, vertices, light, overlay, k);
            matrices.push();
            matrices.translate(0.0f, 1.0f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-o * 1.5f));
            matrices.translate(0.0f, -1.0f, 0.0f);
            this.body.roll = 0.0f;
            this.body.render(matrices, vertices, light, overlay, k);
            float t = f * ((float)Math.PI * 2);
            this.leftWing.pitch = 0.125f - (float)Math.cos(t) * 0.2f;
            this.leftWing.yaw = -0.25f;
            this.leftWing.roll = -((float)(Math.sin(t) + 0.125)) * 0.8f;
            this.leftWingTip.roll = (float)(Math.sin(t + 2.0f) + 0.5) * 0.75f;
            this.rightWing.pitch = this.leftWing.pitch;
            this.rightWing.yaw = -this.leftWing.yaw;
            this.rightWing.roll = -this.leftWing.roll;
            this.rightWingTip.roll = -this.leftWingTip.roll;
            this.setLimbRotation(matrices, vertices, light, overlay, g, this.leftWing, this.leftFrontLeg, this.leftFrontLegTip, this.leftFrontFoot, this.leftHindLeg, this.leftHindLegTip, this.leftHindFoot, k);
            this.setLimbRotation(matrices, vertices, light, overlay, g, this.rightWing, this.rightFrontLeg, this.rightFrontLegTip, this.rightFrontFoot, this.rightHindLeg, this.rightHindLegTip, this.rightHindFoot, k);
            matrices.pop();
            s = -MathHelper.sin(f * ((float)Math.PI * 2)) * 0.0f;
            q = f * ((float)Math.PI * 2);
            l = 10.0f;
            m = 60.0f;
            h = 0.0f;
            ds = this.dragon.getSegmentProperties(11, this.tickDelta);
            for (int u = 0; u < 12; ++u) {
                fs = this.dragon.getSegmentProperties(12 + u, this.tickDelta);
                this.neck.yaw = (MathHelper.wrapDegrees((float)(fs[0] - ds[0])) * 1.5f + 180.0f) * ((float)Math.PI / 180);
                this.neck.pitch = (s += MathHelper.sin((float)u * 0.45f + q) * 0.05f) + (float)(fs[1] - ds[1]) * ((float)Math.PI / 180) * 1.5f * 5.0f;
                this.neck.roll = MathHelper.wrapDegrees((float)(fs[0] - (double)p)) * ((float)Math.PI / 180) * 1.5f;
                this.neck.pivotY = l;
                this.neck.pivotZ = m;
                this.neck.pivotX = h;
                l += MathHelper.sin(this.neck.pitch) * 10.0f;
                m -= MathHelper.cos(this.neck.yaw) * MathHelper.cos(this.neck.pitch) * 10.0f;
                h -= MathHelper.sin(this.neck.yaw) * MathHelper.cos(this.neck.pitch) * 10.0f;
                this.neck.render(matrices, vertices, light, overlay, k);
            }
            matrices.pop();
        }

        private void setLimbRotation(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float offset, ModelPart wing, ModelPart frontLeg, ModelPart frontLegTip, ModelPart frontFoot, ModelPart hindLeg, ModelPart hindLegTip, ModelPart hindFoot, int k) {
            hindLeg.pitch = 1.0f + offset * 0.1f;
            hindLegTip.pitch = 0.5f + offset * 0.1f;
            hindFoot.pitch = 0.75f + offset * 0.1f;
            frontLeg.pitch = 1.3f + offset * 0.1f;
            frontLegTip.pitch = -0.5f - offset * 0.1f;
            frontFoot.pitch = 0.75f + offset * 0.1f;
            wing.render(matrices, vertices, light, overlay, k);
            frontLeg.render(matrices, vertices, light, overlay, k);
            hindLeg.render(matrices, vertices, light, overlay, k);
        }
    }
}

