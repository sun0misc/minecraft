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
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.passive.SheepEntity;

@Environment(value=EnvType.CLIENT)
public class SheepWoolEntityModel<T extends SheepEntity>
extends QuadrupedEntityModel<T> {
    private float headAngle;

    public SheepWoolEntityModel(ModelPart root) {
        super(root, false, 8.0f, 4.0f, 2.0f, 2.0f, 24);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -4.0f, -4.0f, 6.0f, 6.0f, 6.0f, new Dilation(0.6f)), ModelTransform.pivot(0.0f, 6.0f, -8.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(28, 8).cuboid(-4.0f, -10.0f, -7.0f, 8.0f, 16.0f, 6.0f, new Dilation(1.75f)), ModelTransform.of(0.0f, 5.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f, new Dilation(0.5f));
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-3.0f, 12.0f, 7.0f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv3, ModelTransform.pivot(3.0f, 12.0f, 7.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv3, ModelTransform.pivot(-3.0f, 12.0f, -5.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv3, ModelTransform.pivot(3.0f, 12.0f, -5.0f));
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public void animateModel(T arg, float f, float g, float h) {
        super.animateModel(arg, f, g, h);
        this.head.pivotY = 6.0f + ((SheepEntity)arg).getNeckAngle(h) * 9.0f;
        this.headAngle = ((SheepEntity)arg).getHeadAngle(h);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        super.setAngles(arg, f, g, h, i, j);
        this.head.pitch = this.headAngle;
    }
}

