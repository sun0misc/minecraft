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
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class MagmaCubeEntityModel<T extends SlimeEntity>
extends SinglePartEntityModel<T> {
    private static final int SLICES_COUNT = 8;
    private final ModelPart root;
    private final ModelPart[] slices = new ModelPart[8];

    public MagmaCubeEntityModel(ModelPart root) {
        this.root = root;
        Arrays.setAll(this.slices, index -> root.getChild(MagmaCubeEntityModel.getSliceName(index)));
    }

    private static String getSliceName(int index) {
        return "cube" + index;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        for (int i = 0; i < 8; ++i) {
            int j = 0;
            int k = i;
            if (i == 2) {
                j = 24;
                k = 10;
            } else if (i == 3) {
                j = 24;
                k = 19;
            }
            lv2.addChild(MagmaCubeEntityModel.getSliceName(i), ModelPartBuilder.create().uv(j, k).cuboid(-4.0f, 16 + i, -4.0f, 8.0f, 1.0f, 8.0f), ModelTransform.NONE);
        }
        lv2.addChild("inside_cube", ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, 18.0f, -2.0f, 4.0f, 4.0f, 4.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
    }

    @Override
    public void animateModel(T arg, float f, float g, float h) {
        float i = MathHelper.lerp(h, ((SlimeEntity)arg).lastStretch, ((SlimeEntity)arg).stretch);
        if (i < 0.0f) {
            i = 0.0f;
        }
        for (int j = 0; j < this.slices.length; ++j) {
            this.slices[j].pivotY = (float)(-(4 - j)) * i * 1.7f;
        }
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}

