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
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public abstract class TintableCompositeModel<E extends Entity>
extends SinglePartEntityModel<E> {
    private int field_52152 = -1;

    public void setColorMultiplier(int i) {
        this.field_52152 = i;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int k) {
        super.render(matrices, vertices, light, overlay, ColorHelper.Argb.mixColor(k, this.field_52152));
    }
}

