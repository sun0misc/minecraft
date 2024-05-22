/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class LlamaEntityModel<T extends AbstractDonkeyEntity>
extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightChest;
    private final ModelPart leftChest;

    public LlamaEntityModel(ModelPart root) {
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.body = root.getChild(EntityModelPartNames.BODY);
        this.rightChest = root.getChild(EntityModelPartNames.RIGHT_CHEST);
        this.leftChest = root.getChild(EntityModelPartNames.LEFT_CHEST);
        this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
        this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
        this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
        this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
    }

    public static TexturedModelData getTexturedModelData(Dilation dilation) {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-2.0f, -14.0f, -10.0f, 4.0f, 4.0f, 9.0f, dilation).uv(0, 14).cuboid(EntityModelPartNames.NECK, -4.0f, -16.0f, -6.0f, 8.0f, 18.0f, 6.0f, dilation).uv(17, 0).cuboid("ear", -4.0f, -19.0f, -4.0f, 3.0f, 3.0f, 2.0f, dilation).uv(17, 0).cuboid("ear", 1.0f, -19.0f, -4.0f, 3.0f, 3.0f, 2.0f, dilation), ModelTransform.pivot(0.0f, 7.0f, -6.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(29, 0).cuboid(-6.0f, -10.0f, -7.0f, 12.0f, 18.0f, 10.0f, dilation), ModelTransform.of(0.0f, 5.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_CHEST, ModelPartBuilder.create().uv(45, 28).cuboid(-3.0f, 0.0f, 0.0f, 8.0f, 8.0f, 3.0f, dilation), ModelTransform.of(-8.5f, 3.0f, 3.0f, 0.0f, 1.5707964f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_CHEST, ModelPartBuilder.create().uv(45, 41).cuboid(-3.0f, 0.0f, 0.0f, 8.0f, 8.0f, 3.0f, dilation), ModelTransform.of(5.5f, 3.0f, 3.0f, 0.0f, 1.5707964f, 0.0f));
        int i = 4;
        int j = 14;
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(29, 29).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 14.0f, 4.0f, dilation);
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-3.5f, 10.0f, 6.0f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv3, ModelTransform.pivot(3.5f, 10.0f, 6.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv3, ModelTransform.pivot(-3.5f, 10.0f, -5.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv3, ModelTransform.pivot(3.5f, 10.0f, -5.0f));
        return TexturedModelData.of(lv, 128, 64);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        boolean bl;
        this.head.pitch = j * ((float)Math.PI / 180);
        this.head.yaw = i * ((float)Math.PI / 180);
        this.rightHindLeg.pitch = MathHelper.cos(f * 0.6662f) * 1.4f * g;
        this.leftHindLeg.pitch = MathHelper.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.rightFrontLeg.pitch = MathHelper.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leftFrontLeg.pitch = MathHelper.cos(f * 0.6662f) * 1.4f * g;
        this.rightChest.visible = bl = !((PassiveEntity)arg).isBaby() && ((AbstractDonkeyEntity)arg).hasChest();
        this.leftChest.visible = bl;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int k) {
        if (this.child) {
            float f = 2.0f;
            matrices.push();
            float g = 0.7f;
            matrices.scale(0.71428573f, 0.64935064f, 0.7936508f);
            matrices.translate(0.0f, 1.3125f, 0.22f);
            this.head.render(matrices, vertices, light, overlay, k);
            matrices.pop();
            matrices.push();
            float h = 1.1f;
            matrices.scale(0.625f, 0.45454544f, 0.45454544f);
            matrices.translate(0.0f, 2.0625f, 0.0f);
            this.body.render(matrices, vertices, light, overlay, k);
            matrices.pop();
            matrices.push();
            matrices.scale(0.45454544f, 0.41322312f, 0.45454544f);
            matrices.translate(0.0f, 2.0625f, 0.0f);
            ImmutableList.of(this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach(arg3 -> arg3.render(matrices, vertices, light, overlay, k));
            matrices.pop();
        } else {
            ImmutableList.of(this.head, this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach(arg3 -> arg3.render(matrices, vertices, light, overlay, k));
        }
    }
}

