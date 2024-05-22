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
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class MediumPufferfishEntityModel<T extends Entity>
extends SinglePartEntityModel<T> {
    private final ModelPart root;
    private final ModelPart leftBlueFin;
    private final ModelPart rightBlueFin;

    public MediumPufferfishEntityModel(ModelPart root) {
        this.root = root;
        this.leftBlueFin = root.getChild(EntityModelPartNames.LEFT_BLUE_FIN);
        this.rightBlueFin = root.getChild(EntityModelPartNames.RIGHT_BLUE_FIN);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        int i = 22;
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(12, 22).cuboid(-2.5f, -5.0f, -2.5f, 5.0f, 5.0f, 5.0f), ModelTransform.pivot(0.0f, 22.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_BLUE_FIN, ModelPartBuilder.create().uv(24, 0).cuboid(-2.0f, 0.0f, 0.0f, 2.0f, 0.0f, 2.0f), ModelTransform.pivot(-2.5f, 17.0f, -1.5f));
        lv2.addChild(EntityModelPartNames.LEFT_BLUE_FIN, ModelPartBuilder.create().uv(24, 3).cuboid(0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 2.0f), ModelTransform.pivot(2.5f, 17.0f, -1.5f));
        lv2.addChild("top_front_fin", ModelPartBuilder.create().uv(15, 16).cuboid(-2.5f, -1.0f, 0.0f, 5.0f, 1.0f, 1.0f), ModelTransform.of(0.0f, 17.0f, -2.5f, 0.7853982f, 0.0f, 0.0f));
        lv2.addChild("top_back_fin", ModelPartBuilder.create().uv(10, 16).cuboid(-2.5f, -1.0f, -1.0f, 5.0f, 1.0f, 1.0f), ModelTransform.of(0.0f, 17.0f, 2.5f, -0.7853982f, 0.0f, 0.0f));
        lv2.addChild("right_front_fin", ModelPartBuilder.create().uv(8, 16).cuboid(-1.0f, -5.0f, 0.0f, 1.0f, 5.0f, 1.0f), ModelTransform.of(-2.5f, 22.0f, -2.5f, 0.0f, -0.7853982f, 0.0f));
        lv2.addChild("right_back_fin", ModelPartBuilder.create().uv(8, 16).cuboid(-1.0f, -5.0f, 0.0f, 1.0f, 5.0f, 1.0f), ModelTransform.of(-2.5f, 22.0f, 2.5f, 0.0f, 0.7853982f, 0.0f));
        lv2.addChild("left_back_fin", ModelPartBuilder.create().uv(4, 16).cuboid(0.0f, -5.0f, 0.0f, 1.0f, 5.0f, 1.0f), ModelTransform.of(2.5f, 22.0f, 2.5f, 0.0f, -0.7853982f, 0.0f));
        lv2.addChild("left_front_fin", ModelPartBuilder.create().uv(0, 16).cuboid(0.0f, -5.0f, 0.0f, 1.0f, 5.0f, 1.0f), ModelTransform.of(2.5f, 22.0f, -2.5f, 0.0f, 0.7853982f, 0.0f));
        lv2.addChild("bottom_back_fin", ModelPartBuilder.create().uv(8, 22).cuboid(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f), ModelTransform.of(0.5f, 22.0f, 2.5f, 0.7853982f, 0.0f, 0.0f));
        lv2.addChild("bottom_front_fin", ModelPartBuilder.create().uv(17, 21).cuboid(-2.5f, 0.0f, 0.0f, 5.0f, 1.0f, 1.0f), ModelTransform.of(0.0f, 22.0f, -2.5f, -0.7853982f, 0.0f, 0.0f));
        return TexturedModelData.of(lv, 32, 32);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.rightBlueFin.roll = -0.2f + 0.4f * MathHelper.sin(animationProgress * 0.2f);
        this.leftBlueFin.roll = 0.2f - 0.4f * MathHelper.sin(animationProgress * 0.2f);
    }
}

