/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class TurtleEntityModel<T extends TurtleEntity>
extends QuadrupedEntityModel<T> {
    private static final String EGG_BELLY = "egg_belly";
    private final ModelPart plastron;

    public TurtleEntityModel(ModelPart root) {
        super(root, true, 120.0f, 0.0f, 9.0f, 6.0f, 120);
        this.plastron = root.getChild(EGG_BELLY);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(3, 0).cuboid(-3.0f, -1.0f, -3.0f, 6.0f, 5.0f, 6.0f), ModelTransform.pivot(0.0f, 19.0f, -10.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(7, 37).cuboid("shell", -9.5f, 3.0f, -10.0f, 19.0f, 20.0f, 6.0f).uv(31, 1).cuboid("belly", -5.5f, 3.0f, -13.0f, 11.0f, 18.0f, 3.0f), ModelTransform.of(0.0f, 11.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
        lv2.addChild(EGG_BELLY, ModelPartBuilder.create().uv(70, 33).cuboid(-4.5f, 3.0f, -14.0f, 9.0f, 18.0f, 1.0f), ModelTransform.of(0.0f, 11.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
        boolean i = true;
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(1, 23).cuboid(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 10.0f), ModelTransform.pivot(-3.5f, 22.0f, 11.0f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(1, 12).cuboid(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 10.0f), ModelTransform.pivot(3.5f, 22.0f, 11.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(27, 30).cuboid(-13.0f, 0.0f, -2.0f, 13.0f, 1.0f, 5.0f), ModelTransform.pivot(-5.0f, 21.0f, -4.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(27, 24).cuboid(0.0f, 0.0f, -2.0f, 13.0f, 1.0f, 5.0f), ModelTransform.pivot(5.0f, 21.0f, -4.0f));
        return TexturedModelData.of(lv, 128, 64);
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return Iterables.concat(super.getBodyParts(), ImmutableList.of(this.plastron));
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        super.setAngles(arg, f, g, h, i, j);
        this.rightHindLeg.pitch = MathHelper.cos(f * 0.6662f * 0.6f) * 0.5f * g;
        this.leftHindLeg.pitch = MathHelper.cos(f * 0.6662f * 0.6f + (float)Math.PI) * 0.5f * g;
        this.rightFrontLeg.roll = MathHelper.cos(f * 0.6662f * 0.6f + (float)Math.PI) * 0.5f * g;
        this.leftFrontLeg.roll = MathHelper.cos(f * 0.6662f * 0.6f) * 0.5f * g;
        this.rightFrontLeg.pitch = 0.0f;
        this.leftFrontLeg.pitch = 0.0f;
        this.rightFrontLeg.yaw = 0.0f;
        this.leftFrontLeg.yaw = 0.0f;
        this.rightHindLeg.yaw = 0.0f;
        this.leftHindLeg.yaw = 0.0f;
        if (!((Entity)arg).isTouchingWater() && ((Entity)arg).isOnGround()) {
            float k = ((TurtleEntity)arg).isDiggingSand() ? 4.0f : 1.0f;
            float l = ((TurtleEntity)arg).isDiggingSand() ? 2.0f : 1.0f;
            float m = 5.0f;
            this.rightFrontLeg.yaw = MathHelper.cos(k * f * 5.0f + (float)Math.PI) * 8.0f * g * l;
            this.rightFrontLeg.roll = 0.0f;
            this.leftFrontLeg.yaw = MathHelper.cos(k * f * 5.0f) * 8.0f * g * l;
            this.leftFrontLeg.roll = 0.0f;
            this.rightHindLeg.yaw = MathHelper.cos(f * 5.0f + (float)Math.PI) * 3.0f * g;
            this.rightHindLeg.pitch = 0.0f;
            this.leftHindLeg.yaw = MathHelper.cos(f * 5.0f) * 3.0f * g;
            this.leftHindLeg.pitch = 0.0f;
        }
        this.plastron.visible = !this.child && ((TurtleEntity)arg).hasEgg();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int k) {
        boolean bl = this.plastron.visible;
        if (bl) {
            matrices.push();
            matrices.translate(0.0f, -0.08f, 0.0f);
        }
        super.render(matrices, vertices, light, overlay, k);
        if (bl) {
            matrices.pop();
        }
    }
}

