/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import java.util.Arrays;
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
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class SquidEntityModel<T extends Entity>
extends SinglePartEntityModel<T> {
    private final ModelPart[] tentacles = new ModelPart[8];
    private final ModelPart root;

    public SquidEntityModel(ModelPart root) {
        this.root = root;
        Arrays.setAll(this.tentacles, index -> root.getChild(SquidEntityModel.getTentacleName(index)));
    }

    private static String getTentacleName(int index) {
        return "tentacle" + index;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        Dilation lv3 = new Dilation(0.02f);
        int i = -16;
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-6.0f, -8.0f, -6.0f, 12.0f, 16.0f, 12.0f, lv3), ModelTransform.pivot(0.0f, 8.0f, 0.0f));
        int j = 8;
        ModelPartBuilder lv4 = ModelPartBuilder.create().uv(48, 0).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 18.0f, 2.0f);
        for (int k = 0; k < 8; ++k) {
            double d = (double)k * Math.PI * 2.0 / 8.0;
            float f = (float)Math.cos(d) * 5.0f;
            float g = 15.0f;
            float h = (float)Math.sin(d) * 5.0f;
            d = (double)k * Math.PI * -2.0 / 8.0 + 1.5707963267948966;
            float l = (float)d;
            lv2.addChild(SquidEntityModel.getTentacleName(k), lv4, ModelTransform.of(f, 15.0f, h, 0.0f, l, 0.0f));
        }
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        for (ModelPart lv : this.tentacles) {
            lv.pitch = animationProgress;
        }
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}

