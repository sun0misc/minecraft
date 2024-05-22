/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;

@Environment(value=EnvType.CLIENT)
public class DragonHeadEntityModel
extends SkullBlockEntityModel {
    private final ModelPart head;
    private final ModelPart jaw;

    public DragonHeadEntityModel(ModelPart root) {
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.jaw = this.head.getChild(EntityModelPartNames.JAW);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        float f = -16.0f;
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().cuboid("upper_lip", -6.0f, -1.0f, -24.0f, 12, 5, 16, 176, 44).cuboid("upper_head", -8.0f, -8.0f, -10.0f, 16, 16, 16, 112, 30).mirrored(true).cuboid("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).cuboid("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0).mirrored(false).cuboid("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).cuboid("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0), ModelTransform.NONE);
        lv3.addChild(EntityModelPartNames.JAW, ModelPartBuilder.create().uv(176, 65).cuboid(EntityModelPartNames.JAW, -6.0f, 0.0f, -16.0f, 12.0f, 4.0f, 16.0f), ModelTransform.pivot(0.0f, 4.0f, -8.0f));
        return TexturedModelData.of(lv, 256, 256);
    }

    @Override
    public void setHeadRotation(float animationProgress, float yaw, float pitch) {
        this.jaw.pitch = (float)(Math.sin(animationProgress * (float)Math.PI * 0.2f) + 1.0) * 0.2f;
        this.head.yaw = yaw * ((float)Math.PI / 180);
        this.head.pitch = pitch * ((float)Math.PI / 180);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int k) {
        matrices.push();
        matrices.translate(0.0f, -0.374375f, 0.0f);
        matrices.scale(0.75f, 0.75f, 0.75f);
        this.head.render(matrices, vertices, light, overlay, k);
        matrices.pop();
    }
}

