/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TextureDimensions;

@Environment(value=EnvType.CLIENT)
public class TexturedModelData {
    private final ModelData data;
    private final TextureDimensions dimensions;

    private TexturedModelData(ModelData data, TextureDimensions dimensions) {
        this.data = data;
        this.dimensions = dimensions;
    }

    public ModelPart createModel() {
        return this.data.getRoot().createPart(this.dimensions.width, this.dimensions.height);
    }

    public static TexturedModelData of(ModelData partData, int textureWidth, int textureHeight) {
        return new TexturedModelData(partData, new TextureDimensions(textureWidth, textureHeight));
    }
}

